package com.jumbo.adapter.in.web;

import com.jumbo.application.domain.model.Store;
import com.jumbo.application.port.in.NearByRequest;
import com.jumbo.application.port.in.NearByUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NearByUseCase nearByService;

    @Test
    void getClosestStores_ValidRequest_ReturnsStores() throws Exception {

        Store store1 = createTestStore("store1", 52.3702, 4.8952, "Amsterdam");
        Store store2 = createTestStore("store2", 52.3603, 4.8849, "Amsterdam");
        List<Store> expectedStores = Arrays.asList(store1, store2);

        when(nearByService.findNearByStores(any(NearByRequest.class), any(LocalTime.class)))
                .thenReturn(expectedStores);

        mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", "52.3702")
                        .param("longitude", "4.8952")
                        .param("limit", "5")
                        .param("onlyOpen", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].uuid").value("store1"))
                .andExpect(jsonPath("$[0].city").value("Amsterdam"))
                .andExpect(jsonPath("$[1].uuid").value("store2"));
    }

    @Test
    void getClosestStores_InvalidLatitude_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", "91.0")  // Invalid latitude
                        .param("longitude", "4.8952")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClosestStores_InvalidLongitude_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", "52.3702")
                        .param("longitude", "181.0")  // Invalid longitude
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClosestStores_MissingLatitude_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("longitude", "4.8952")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClosestStores_InvalidLimit_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", "52.3702")
                        .param("longitude", "4.8952")
                        .param("limit", "0")  // Invalid limit
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private Store createTestStore(String uuid, double lat, double lon, String city) {
        Store store = new Store();
        store.setUuid(uuid);
        store.setLatitude(lat);
        store.setLongitude(lon);
        store.setCity(city);
        store.setStreet("Test Street 123");
        store.setPostalCode("1234 AB");
        store.opensAt(8, 0);
        store.closesAt(22, 0);
        store.setLocationType("supermarket");
        return store;
    }
}
