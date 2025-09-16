package com.jumbo.application.domain.servcie;

import com.jumbo.application.port.in.NearByRequest;
import com.jumbo.application.port.in.NearByUseCase;
import com.jumbo.application.domain.model.Store;
import com.jumbo.application.port.out.StoreRepository;
import jakarta.annotation.PostConstruct;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
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
    public List<Store> findNearByStores(NearByRequest req, LocalTime now) {
        double searchRadiusKm = 1.0; // start with 1 km

        Set<Store> stores = new HashSet<>();
        while (true) {
            Envelope env = makeEnvelope(req.latitude(), req.longitude(), searchRadiusKm);
            @SuppressWarnings("unchecked")
            List<Store> found = quadtree.query(env);

            List<Store> filtered = found.stream()
                    .filter(s -> !req.onlyOpen() || s.isOpen(now))
                    .toList();
            stores.addAll(filtered);

            if (stores.size() >= req.limit() || searchRadiusKm >= req.maxRadiusKm()) {
                return stores.stream()
                        .peek(store -> store.setDistance(distanceCalculator.distanceInKm(
                                req.latitude(), req.longitude(),
                                store.getLatitude(), store.getLongitude())))
                        .filter(s -> s.getDistance() <= req.maxRadiusKm())
                        .sorted(Comparator.comparingDouble(Store::getDistance))
                        .limit(req.limit())
                        .collect(Collectors.toList());
            }
            searchRadiusKm *= 2; // expand search area
        }
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
