package com.jumbo.integration;

import com.jumbo.LocationServiceApplication;
import com.jumbo.application.domain.model.Store;
import com.jumbo.application.domain.servcie.QuadTreeNearByService;
import com.jumbo.application.port.in.NearByRequest;
import com.jumbo.application.port.in.NearByUseCase;
import com.jumbo.application.port.out.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test focused on business logic and cross-cutting concerns.
 * Tests the integration between different application layers and validates
 * business rules, error handling, and data consistency.
 */
@SpringBootTest(classes = LocationServiceApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "jumbo.location.search.strategy=in-memory"
})
@DisplayName("Business Logic Integration Tests")
class BusinessLogicIntegrationTest {

    @Autowired
    private NearByUseCase nearByService;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("Should validate business rules for store search parameters")
    void shouldValidateBusinessRulesForStoreSearch() {
        // Test boundary conditions and business constraints

        // Given: Valid request within business limits
        NearByRequest validRequest = new NearByRequest(52.3702, 4.8952, 5.0, 10, false);
        LocalTime currentTime = LocalTime.of(14, 0); // 2 PM

        // When: Processing valid request
        List<Store> stores = nearByService.findNearByStores(validRequest, currentTime);

        // Then: Should return valid results
        assertThat(stores).isNotNull();
        assertThat(stores.size()).isLessThanOrEqualTo(10);

        // Validate business rules for returned stores
        stores.forEach(store -> {
            assertThat(store.getDistance()).isPositive();
            assertThat(store.getDistance()).isLessThanOrEqualTo(5.0);
            assertThat(store.getLatitude()).isBetween(50.0, 54.0); // Netherlands bounds
            assertThat(store.getLongitude()).isBetween(3.0, 8.0); // Netherlands bounds
        });
    }


    @Test
    @DisplayName("Should test edge cases for geographical boundaries")
    void shouldTestEdgeCasesForGeographicalBoundaries() {
        // Test near country borders

        // Near Belgian border
        NearByRequest belgianBorderRequest = new NearByRequest(50.8503, 4.3517, 10.0, 5, false);
        List<Store> borderStores = nearByService.findNearByStores(belgianBorderRequest, LocalTime.now());
        assertThat(borderStores).isNotNull();

        // Near German border
        NearByRequest germanBorderRequest = new NearByRequest(52.5200, 7.0982, 10.0, 5, false);
        List<Store> germanBorderStores = nearByService.findNearByStores(germanBorderRequest, LocalTime.now());
        assertThat(germanBorderStores).isNotNull();

        // Near North Sea (should still find some stores)
        NearByRequest seaRequest = new NearByRequest(52.1326, 4.2913, 20.0, 3, false);
        List<Store> seaStores = nearByService.findNearByStores(seaRequest, LocalTime.now());
        assertThat(seaStores).isNotNull();
    }

    @Test
    @DisplayName("Should verify store data quality and business constraints")
    void shouldVerifyStoreDataQualityAndBusinessConstraints() throws IOException {
        List<Store> allStores = storeRepository.findAll();

        // Verify data quality metrics
        long storesWithValidCoordinates = allStores.stream()
                .filter(store -> store.getLatitude() >= 50.0 && store.getLatitude() <= 54.0)
                .filter(store -> store.getLongitude() >= 3.0 && store.getLongitude() <= 8.0)
                .count();

        // Most stores should be within Netherlands bounds
        double validCoordinatesPercentage = (double) storesWithValidCoordinates / allStores.size() * 100;
        assertThat(validCoordinatesPercentage).isGreaterThan(95.0);

        // Verify business constraints
        long storesWithOpeningHours = allStores.stream()
                .filter(store -> store.getTodayOpen() != null && store.getTodayClose() != null)
                .count();

        // Most stores should have opening hours defined
        double openingHoursPercentage = (double) storesWithOpeningHours / allStores.size() * 100;
        assertThat(openingHoursPercentage).isGreaterThan(80.0);

        // Verify unique store identifiers
        long uniqueUuids = allStores.stream()
                .map(Store::getUuid)
                .distinct()
                .count();
        assertThat(uniqueUuids).isEqualTo(allStores.size());
    }

    @Test
    @DisplayName("Should test search result consistency and determinism")
    void shouldTestSearchResultConsistencyAndDeterminism() {
        // Same request should return same results (deterministic)
        NearByRequest request = new NearByRequest(52.3702, 4.8952, 5.0, 5, false);
        LocalTime fixedTime = LocalTime.of(12, 0);

        List<Store> firstResult = nearByService.findNearByStores(request, fixedTime);
        List<Store> secondResult = nearByService.findNearByStores(request, fixedTime);

        assertThat(firstResult).hasSize(secondResult.size());

        // Results should be in same order (sorted by distance)
        for (int i = 0; i < firstResult.size(); i++) {
            assertThat(firstResult.get(i).getUuid()).isEqualTo(secondResult.get(i).getUuid());
            assertThat(firstResult.get(i).getDistance()).isEqualTo(secondResult.get(i).getDistance());
        }
    }
}
