package com.jumbo.config;

import com.jumbo.application.domain.servcie.InMemNearByStore;
import com.jumbo.application.port.in.NearByUseCase;
import com.jumbo.application.domain.servcie.QuadTreeNearByService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class NearByServiceConfig {

    @Value("${jumbo.location.search.strategy:quadtree}")
    private String searchStrategy;

    @Bean
    public NearByUseCase nearByService(InMemNearByStore inMemNearByStore,
                                       QuadTreeNearByService quadTreeNearByService) {
        log.info("Configuring NearByService with strategy: {}", searchStrategy);

        return switch (searchStrategy.toLowerCase()) {
            case "in-memory", "inmemory" -> {
                log.info("Using InMemory search strategy");
                yield inMemNearByStore;
            }
            case "quadtree", "quad-tree" -> {
                log.info("Using QuadTree search strategy");
                yield quadTreeNearByService;
            }
            default -> {
                log.warn("Unknown strategy '{}', defaulting to QuadTree", searchStrategy);
                yield quadTreeNearByService;
            }
        };
    }
}
