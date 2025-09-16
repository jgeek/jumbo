package com.jumbo.application.domain.service;

import com.jumbo.application.domain.model.Store;
import com.jumbo.application.domain.servcie.HaversineDistanceCalculator;
import com.jumbo.application.domain.servcie.InMemNearByStore;
import com.jumbo.application.port.in.NearByRequest;
import com.jumbo.application.port.out.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.quality.Strictness;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemNearByStoreEntityTest {

    @Mock
    private StoreRepository storeRepository;

    private Store mockStore(double lat, double lon, boolean open) {
        Store store = mock(Store.class, withSettings().strictness(Strictness.LENIENT));
        when(store.getLatitude()).thenReturn(lat);
        when(store.getLongitude()).thenReturn(lon);
        when(store.isOpen(any(LocalTime.class))).thenReturn(open);
        when(store.isOpen()).thenReturn(open);

        AtomicReference<Double> distanceRef = new AtomicReference<>(Double.NaN);
        doAnswer(inv -> {
            distanceRef.set(inv.getArgument(0));
            return null;
        }).when(store).setDistance(anyDouble());
        when(store.getDistance()).thenAnswer(inv -> distanceRef.get());

        return store;
    }

    private InMemNearByStore createServiceWithStores(Store... stores) throws Exception {
        when(storeRepository.findAll()).thenReturn(Arrays.asList(stores));
        InMemNearByStore service = new InMemNearByStore(storeRepository, new HaversineDistanceCalculator());
        service.init();
        return service;
    }

    @Test
    void returnsOnlyOpenStoresSortedByDistanceAndRespectsLimit() throws Exception {
        Store s1 = mockStore(0.0, 0.05, true);   // nearer and open
        Store s2 = mockStore(0.0, 0.20, true);   // farther and open
        Store s3 = mockStore(0.0, 0.01, false);  // closest but closed

        InMemNearByStore service = createServiceWithStores(s1, s2, s3);

        NearByRequest req = new NearByRequest(0.0, 0.0, 1, 2, true);
        List<Store> result = service.findNearByStores(req, LocalTime.now());

        assertEquals(2, result.size());
        assertIterableEquals(List.of(s1, s2), result);
        assertTrue(result.stream().allMatch(Store::isOpen));
        assertTrue(result.get(0).getDistance() <= result.get(1).getDistance());
    }

    @Test
    void returnsAllStoresSortedByDistanceWhenOnlyOpenIsFalse() throws Exception {
        Store s1 = mockStore(0.0, 0.30, false);
        Store s2 = mockStore(0.0, 0.10, true);
        Store s3 = mockStore(0.0, 0.20, false);

        InMemNearByStore service = createServiceWithStores(s1, s2, s3);

        NearByRequest req = new NearByRequest(0.0, 0.0, 1, 10, false);
        List<Store> result = service.findNearByStores(req, LocalTime.now());

        assertIterableEquals(List.of(s2, s3, s1), result);
        assertTrue(result.get(0).getDistance() <= result.get(1).getDistance());
        assertTrue(result.get(1).getDistance() <= result.get(2).getDistance());
    }

    @Test
    void returnsEmptyListWhenNoStoresAvailable() throws Exception {
        InMemNearByStore service = createServiceWithStores();

        NearByRequest req = new NearByRequest(0.0, 0.0, 1, 5, false);
        List<Store> result = service.findNearByStores(req, LocalTime.now());

        assertTrue(result.isEmpty());
    }

    @Test
    void respectsLimitSelectingNearestFirst() throws Exception {
        Store s1 = mockStore(0.0, 0.05, true); // nearest
        Store s2 = mockStore(0.0, 0.20, true);

        InMemNearByStore service = createServiceWithStores(s1, s2);

        NearByRequest req = new NearByRequest(0.0, 0.0, 1, 1, false);
        List<Store> result = service.findNearByStores(req, LocalTime.now());

        assertEquals(1, result.size());
        assertEquals(s1, result.get(0));
    }

    @Test
    void returnsEmptyWhenOnlyOpenTrueAndNoOpenStores() throws Exception {
        Store s1 = mockStore(0.0, 0.05, false);
        Store s2 = mockStore(0.0, 0.10, false);

        InMemNearByStore service = createServiceWithStores(s1, s2);

        NearByRequest req = new NearByRequest(0.0, 0.0, 1, 5, true);
        List<Store> result = service.findNearByStores(req, LocalTime.now());

        assertTrue(result.isEmpty());
    }

    @Test
    void recomputesDistancesAndResortsOnSubsequentRequests() throws Exception {
        Store s1 = mockStore(0.0, 0.15, true); // farther from (0,0)
        Store s2 = mockStore(0.05, 0.0, true); // nearer to (0,0)

        InMemNearByStore service = createServiceWithStores(s1, s2);

        List<Store> first = service.findNearByStores(new NearByRequest(0.0, 0.0, 1, 5, false), LocalTime.now());
        assertIterableEquals(List.of(s2, s1), first);

        List<Store> second = service.findNearByStores(new NearByRequest(0.0, 0.14, 1, 5, false), LocalTime.now());
        assertIterableEquals(List.of(s1, s2), second);
    }

    @Test
    void returnsOnlyStoresWithinMaxRadiusKm() throws Exception {
        Store s1 = mockStore(0.0, 0.001, true);  // very close
        Store s2 = mockStore(0.0, 0.002, true);  // farther away
        Store s3 = mockStore(0.0, 1.00, true);  // outside radius

        InMemNearByStore service = createServiceWithStores(s1, s2, s3);

        // Set maxRadiusKm to a value that includes s1 and s2, but excludes s3
        NearByRequest req = new NearByRequest(0.0, 0.0, 1, 10, false);
        List<Store> result = service.findNearByStores(req, LocalTime.now());

        assertTrue(result.contains(s1));
        assertTrue(result.contains(s2));
        assertFalse(result.contains(s3));
        assertTrue(result.stream().allMatch(store -> store.getDistance() <= req.maxRadiusKm()));
    }
}