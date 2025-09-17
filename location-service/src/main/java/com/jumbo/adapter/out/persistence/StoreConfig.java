package com.jumbo.adapter.out.persistence;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jumbo.location.stores")
public class StoreConfig {
    private String dataFile;
}
