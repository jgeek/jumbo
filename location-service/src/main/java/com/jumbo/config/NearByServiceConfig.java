package com.jumbo.config;

import com.jumbo.repositoy.StoreRepository;
import com.jumbo.service.nearby.InMemNearByStore;
import com.jumbo.service.nearby.NearByService;
import com.jumbo.service.nearby.QuadTreeNearByService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class NearByServiceConfig {

    @Bean
    public NearByService nearByService(@Value("${app.nearby.strategy}") String strategy, StoreRepository storeRepository) {
        return switch (strategy.toLowerCase()) {
            case "quadtree" -> new QuadTreeNearByService(storeRepository);
            case "in-memory" -> new InMemNearByStore(storeRepository);
            default -> throw new IllegalArgumentException("Invalid strategy: " + strategy);
        };
    }
}