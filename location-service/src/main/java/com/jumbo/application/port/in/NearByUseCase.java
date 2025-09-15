package com.jumbo.application.port.in;

import com.jumbo.application.domain.model.Store;

import java.util.List;

public interface NearByUseCase {
    List<Store> findNearByStores(NearByRequest req);
}
