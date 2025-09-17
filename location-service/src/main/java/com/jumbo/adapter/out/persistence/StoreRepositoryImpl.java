package com.jumbo.adapter.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.application.domain.model.Store;
import com.jumbo.application.port.out.StoreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class StoreRepositoryImpl implements StoreRepository {

    private final String storesDataFile;
    private final StoreMapper storeMapper;
    private final ResourceLoader resourceLoader;

    public StoreRepositoryImpl(StoreMapper storeMapper, ResourceLoader resourceLoader,
                               @Value("${jumbo.location.stores.data-file}") String storesDataFile) {
        this.storeMapper = storeMapper;
        this.resourceLoader = resourceLoader;
        this.storesDataFile = storesDataFile;
    }

    public List<Store> findAll() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = resourceLoader.getResource(storesDataFile).getInputStream()) {
            List<StoreEntity> entities = mapper.readValue(
                    mapper.readTree(is).get("stores").traverse(mapper),
                    new TypeReference<>() {
                    }
            );

            return storeMapper.toDomainList(entities);
        }
    }
}
