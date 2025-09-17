package com.jumbo.adapter.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.application.domain.model.Store;
import com.jumbo.application.port.out.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    @Value("${jumbo.location.stores.data-file}")
    private String storesDataFile;

    private final StoreMapper storeMapper;
    private final ResourceLoader resourceLoader;

    public List<Store> findAll() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = resourceLoader.getResource(storesDataFile).getInputStream()) {
            List<StoreEntity> entities = mapper.readTree(is).get("stores")
                    .traverse(mapper)
                    .readValueAs(new TypeReference<List<StoreEntity>>() {
                    });

            return storeMapper.toDomainList(entities);
        }
    }
}
