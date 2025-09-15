package com.jumbo.application.port.out;

import com.jumbo.application.domain.model.Store;

import java.io.IOException;
import java.util.List;

public interface StoreRepository {
    List<Store> findAll() throws IOException;
}
