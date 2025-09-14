package com.jumbo.service;

import com.jumbo.model.Store;

import java.util.List;

public interface NearByService {
    List<Store> findNearByStores(NearByRequest req);
}
