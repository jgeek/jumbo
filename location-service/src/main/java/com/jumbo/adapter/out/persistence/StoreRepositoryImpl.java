package com.jumbo.adapter.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.application.port.out.StoreRepository;
import com.jumbo.application.domain.model.Store;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class StoreRepositoryImpl implements StoreRepository {
    public List<Store> findAll() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/stores.json");

        return mapper.readTree(is).get("stores")
                .traverse(mapper)
                .readValueAs(new TypeReference<List<Store>>() {
                });
    }
}
