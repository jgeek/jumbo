package com.jumbo.repositoy;

import com.jumbo.model.Store;

import java.io.IOException;
import java.util.List;

public interface StoreRepository {
    List<Store> findAll() throws IOException;
}
