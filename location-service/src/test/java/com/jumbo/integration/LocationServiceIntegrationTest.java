package com.jumbo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.LocationServiceApplication;
import com.jumbo.application.domain.model.Store;
import com.jumbo.application.port.in.NearByUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = LocationServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Location Service Integration Tests")
class LocationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("nearByService")
    private NearByUseCase nearByUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should find nearby stores in Amsterdam city center - Full Integration Test")
    void shouldFindNearbyStoresInAmsterdamCityCenter() throws Exception {
        // Given: Amsterdam city center coordinates
        double latitude = 52.3702;
        double longitude = 4.8952;
        int limit = 10;
        double maxRadius = 5.0;
        boolean onlyOpen = false;

        MvcResult result = mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", String.valueOf(latitude))
                        .param("longitude", String.valueOf(longitude))
                        .param("limit", String.valueOf(limit))
                        .param("maxRadius", String.valueOf(maxRadius))
                        .param("onlyOpen", String.valueOf(onlyOpen))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(limit)))
                .andReturn();

        // Then: Verify response structure and data quality
        String responseContent = result.getResponse().getContentAsString();
        List<Store> stores = objectMapper.readValue(responseContent,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Store.class));

        // Verify all stores are within the specified radius
        stores.forEach(store -> {
            assertThat(store.getDistance()).isLessThanOrEqualTo(maxRadius);
            assertThat(store.getCity()).isNotNull();
            assertThat(store.getLatitude()).isBetween(-90.0, 90.0);
            assertThat(store.getLongitude()).isBetween(-180.0, 180.0);
            assertThat(store.getUuid()).isNotNull();
        });

        // Verify stores are sorted by distance (closest first)
        for (int i = 1; i < stores.size(); i++) {
            assertThat(stores.get(i).getDistance()).isGreaterThanOrEqualTo(stores.get(i - 1).getDistance());
        }

        // Verify at least some Amsterdam stores are returned
        boolean hasAmsterdamStores = stores.stream()
                .anyMatch(store -> store.getCity().toLowerCase().contains("amsterdam"));
        assertThat(hasAmsterdamStores).isTrue();
    }

    @Test
    @DisplayName("Should respect onlyOpen parameter and filter closed stores")
    void shouldFilterClosedStoresWhenOnlyOpenIsTrue() throws Exception {
        // Given: Coordinates and onlyOpen=true
        double latitude = 52.3702;
        double longitude = 4.8952;
        int limit = 5;
        boolean onlyOpen = true;

        // When: Making request with onlyOpen=true
        MvcResult result = mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", String.valueOf(latitude))
                        .param("longitude", String.valueOf(longitude))
                        .param("limit", String.valueOf(limit))
                        .param("onlyOpen", String.valueOf(onlyOpen))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then: All returned stores should be open
        String responseContent = result.getResponse().getContentAsString();
        List<Store> openStores = objectMapper.readValue(responseContent,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Store.class));

        openStores.forEach(store -> {
            boolean isOpen = store.isOpen(LocalTime.now());
            assertThat(isOpen).describedAs("Store %s should be open", store.getAddressName()).isTrue();
        });
    }

    @Test
    @DisplayName("Should handle concurrent requests correctly")
    void shouldHandleConcurrentRequests() throws Exception {
        double latitude = 52.3702;
        double longitude = 4.8952;

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            futures.add(executor.submit(() -> {
                mockMvc.perform(get("/api/v1/stores/nearby")
                                .param("latitude", String.valueOf(latitude))
                                .param("longitude", String.valueOf(longitude))
                                .param("limit", "3")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(3)));
                return null;
            }));
        }
        for (Future<Void> future : futures) {
            future.get();
        }
        executor.shutdown();
    }

    @Test
    @DisplayName("Should test health endpoint integration")
    void shouldTestHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }
}
