package com.jumbo.service;

import com.jumbo.model.Store;
import com.jumbo.repositoy.StoreRepository;
import com.jumbo.utils.DistanceCalculator;
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
public class QuadTreeNearByService implements NearByService {

    private Quadtree quadtree;
    private final StoreRepository storeRepository;

    public QuadTreeNearByService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
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
                        .map(s -> new AbstractMap.SimpleEntry<>
                                (s, DistanceCalculator.distance(req.latitude(), req.longitude(), s.getLatitude(), s.getLongitude())))
                        .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                        .limit(req.limit())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
            }
            searchRadiusKm *= 2; // expand search area
        }
        return List.of();
    }

    private Envelope makeEnvelope(double lat, double lon, double radiusKm) {
        // rough conversion: 1 degree lat ~ 111 km
        double delta = radiusKm / 111.0;
        return new Envelope(lon - delta, lon + delta, lat - delta, lat + delta);
    }
}
