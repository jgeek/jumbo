package com.jumbo.application.domain.servcie;

import com.jumbo.application.port.in.NearByRequest;
import com.jumbo.application.port.in.NearByUseCase;
import com.jumbo.application.domain.model.Store;
import com.jumbo.application.port.out.StoreRepository;
import jakarta.annotation.PostConstruct;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuadTreeNearByService implements NearByUseCase {

    private Quadtree quadtree;
    private final StoreRepository storeRepository;
    private final DistanceCalculator distanceCalculator;


    public QuadTreeNearByService(StoreRepository storeRepository, DistanceCalculator distanceCalculator) {
        this.storeRepository = storeRepository;
        this.distanceCalculator = distanceCalculator;
    }


    @PostConstruct
    public void init() throws Exception {
        List<Store> stores = this.storeRepository.findAll();

        quadtree = new Quadtree();
        for (Store s : stores) {
            Envelope env = new Envelope(s.getLongitude(), s.getLongitude(), s.getLatitude(), s.getLatitude());
            quadtree.insert(env, s);
        }
    }

    @Override
    public List<Store> findNearByStores(NearByRequest req) {
        double searchRadiusKm = 1.0; // start with 1 km
        double maxRadiusKm = 100.0;  // max search limit

        while (searchRadiusKm <= maxRadiusKm) {
            Envelope env = makeEnvelope(req.latitude(), req.longitude(), searchRadiusKm);
            @SuppressWarnings("unchecked")
            List<Store> found = quadtree.query(env);

            List<Store> filtered = found.stream()
                    .filter(s -> !req.onlyOpen() || s.isOpen())
                    .toList();

            if (filtered.size() >= req.limit() || searchRadiusKm == maxRadiusKm) {
                return filtered.stream()
                        .peek(store -> store.setDistance(distanceCalculator.distanceInKm(
                                req.latitude(), req.longitude(),
                                store.getLatitude(), store.getLongitude())))
                        .sorted(Comparator.comparingDouble(Store::getDistance))
                        .limit(req.limit())
                        .collect(Collectors.toList());
            }
            searchRadiusKm *= 2; // expand search area
        }
        return List.of();
    }

    /*
    Creates creates a square bounding box (Envelope) around a geographic point (latitude, longitude)
    with a given search radius in kilometers. It approximates the conversion from kilometers to degrees
    using 1 degree ≈ 111 km (valid near the equator).
    The method calculates the degree offset (delta) for the radius,
    then constructs an Envelope with min/max longitude and latitude expanded by delta from the center point.
    This envelope is used to query the quadtree for nearby stores.
     */
    private Envelope makeEnvelope(double lat, double lon, double radiusKm) {
        // rough conversion: 1 degree lat ~ 111 km
        double delta = radiusKm / 111.0;
        return new Envelope(lon - delta, lon + delta, lat - delta, lat + delta);
    }
}
