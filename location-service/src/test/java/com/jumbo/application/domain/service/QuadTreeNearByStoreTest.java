package com.jumbo.application.domain.service;

import com.jumbo.application.domain.model.Store;
import com.jumbo.application.domain.servcie.HaversineDistanceCalculator;
import com.jumbo.application.domain.servcie.InMemNearByStore;
import com.jumbo.application.domain.servcie.QuadTreeNearByService;
import com.jumbo.application.port.in.NearByUseCase;
import com.jumbo.application.port.out.StoreRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuadTreeNearByStoreTest extends AbstractNearByServiceTest {

    private NearByUseCase service;

    @Mock
    private StoreRepository storeRepository;

    @Override
    protected NearByUseCase createServiceWithStores(Store... stores) throws Exception {
        when(storeRepository.findAll()).thenReturn(Arrays.asList(stores));
        QuadTreeNearByService service = new QuadTreeNearByService(storeRepository, new HaversineDistanceCalculator());
        service.init();
        return service;
    }
}