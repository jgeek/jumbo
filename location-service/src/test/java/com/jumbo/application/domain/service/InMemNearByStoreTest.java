package com.jumbo.application.domain.service;

import com.jumbo.application.domain.model.Store;
import com.jumbo.application.domain.servcie.HaversineDistanceCalculator;
import com.jumbo.application.domain.servcie.InMemNearByStore;
import com.jumbo.application.port.in.NearByUseCase;
import com.jumbo.application.port.out.StoreRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemNearByStoreTest extends AbstractNearByServiceTest {

    private NearByUseCase service;

    @Mock
    private StoreRepository storeRepository;

    @Override
    protected NearByUseCase createServiceWithStores(Store... stores) throws Exception {
        when(storeRepository.findAll()).thenReturn(Arrays.asList(stores));
        InMemNearByStore service = new InMemNearByStore(storeRepository, new HaversineDistanceCalculator());
        service.init();
        return service;
    }
}