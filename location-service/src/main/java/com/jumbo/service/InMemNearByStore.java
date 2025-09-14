package com.jumbo.service;

import com.jumbo.model.Store;
import com.jumbo.repositoy.StoreRepository;
import com.jumbo.utils.DistanceCalculator;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InMemNearByStore implements NearByService {

    private List<Store> stores;
    private final StoreRepository storeRepository;

    public InMemNearByStore(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @PostConstruct
    public void init() throws Exception {
        stores = storeRepository.findAll();
    }

    public List<Store> findNearByStores(NearByRequest req) {
        return stores.stream()
                .filter(store -> !req.onlyOpen() || store.isOpen())
                .peek(store -> store.setDistance(DistanceCalculator.distance(
                        req.latitude(), req.longitude(),
                        store.getLatitude(), store.getLongitude())))
                .sorted(Comparator.comparingDouble(Store::getDistance))
                .limit(req.limit())
                .collect(Collectors.toList());
    }
}
