package com.jumbo.application.domain.servcie;

import com.jumbo.application.port.in.NearByRequest;
import com.jumbo.application.port.in.NearByUseCase;
import com.jumbo.application.domain.model.Store;
import com.jumbo.application.port.out.StoreRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InMemNearByStore implements NearByUseCase {

    private List<Store> stores;
    private final StoreRepository storeRepository;
    private final DistanceCalculator distanceCalculator;


    public InMemNearByStore(StoreRepository storeRepository, DistanceCalculator distanceCalculator) {
        this.storeRepository = storeRepository;
        this.distanceCalculator = distanceCalculator;
    }

    @PostConstruct
    public void init() throws Exception {
        stores = storeRepository.findAll();
    }

    public List<Store> findNearByStores(NearByRequest req, LocalTime now) {
        return stores.stream()
                .filter(store -> !req.onlyOpen() || store.isOpen(now))
                .peek(store -> store.setDistance(distanceCalculator.distanceInKm(
                        req.latitude(), req.longitude(),
                        store.getLatitude(), store.getLongitude())))
                .sorted(Comparator.comparingDouble(Store::getDistance))
                .peek(s -> System.out.println("Store " + s.getUuid() + " is " + s.getDistance() + " km away"))
                .filter(s -> s.getDistance() <= req.maxRadiusKm())
                .limit(req.limit())
                .collect(Collectors.toList());
    }
}
