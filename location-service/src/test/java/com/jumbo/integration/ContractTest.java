package com.jumbo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.LocationServiceApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = LocationServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Contract Integration Tests")
class ContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should validate complete API contract and response structure")
    void shouldValidateCompleteApiContractAndResponseStructure() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", "52.3702")
                        .param("longitude", "4.8952")
                        .param("limit", "3")
                        .param("maxRadius", "10.0")
                        .param("onlyOpen", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().exists("Content-Type"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);

        assertThat(jsonResponse.isArray()).isTrue();
        assertThat(jsonResponse.size()).isLessThanOrEqualTo(3);

        for (JsonNode storeNode : jsonResponse) {
            validateStoreJsonStructure(storeNode);
        }
    }

    private void validateStoreJsonStructure(JsonNode storeNode) {
        // Validate required fields
        assertThat(storeNode.has("city")).isTrue();
        assertThat(storeNode.has("postalCode")).isTrue();
        assertThat(storeNode.has("street")).isTrue();
        assertThat(storeNode.has("uuid")).isTrue();
        assertThat(storeNode.has("longitude")).isTrue();
        assertThat(storeNode.has("latitude")).isTrue();
        assertThat(storeNode.has("distance")).isTrue();

        // Validate field types and formats
        assertThat(storeNode.get("latitude").isNumber()).isTrue();
        assertThat(storeNode.get("longitude").isNumber()).isTrue();
        assertThat(storeNode.get("distance").isNumber()).isTrue();
        assertThat(storeNode.get("collectionPoint").isBoolean()).isTrue();

        // Validate coordinate ranges
        double lat = storeNode.get("latitude").asDouble();
        double lon = storeNode.get("longitude").asDouble();
        assertThat(lat).isBetween(-90.0, 90.0);
        assertThat(lon).isBetween(-180.0, 180.0);
    }

    @Test
    @DisplayName("Should handle CORS headers for cross-origin requests")
    void shouldHandleCorsHeadersForCrossOriginRequests() throws Exception {
        // When: Making request with CORS headers
        mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", "52.3702")
                        .param("longitude", "4.8952")
                        .param("limit", "3")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should validate error handling and error response structure")
    void shouldValidateErrorHandlingAndErrorResponseStructure() throws Exception {

        // Missing required parameter
        mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("longitude", "4.8952")
                        .param("limit", "3"))
                .andExpect(status().isBadRequest());

        // Invalid parameter values
        mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", "invalid")
                        .param("longitude", "4.8952")
                        .param("limit", "3"))
                .andExpect(status().isBadRequest());

        // Out of range parameters
        MvcResult errorResult = mockMvc.perform(get("/api/v1/stores/nearby")
                        .param("latitude", "95.0")
                        .param("longitude", "4.8952")
                        .param("limit", "3"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Validate error response structure
        String errorContent = errorResult.getResponse().getContentAsString();
        if (!errorContent.isEmpty()) {
            JsonNode errorNode = objectMapper.readTree(errorContent);
            // Error response should have meaningful structure
            assertThat(errorNode.has("timestamp") || errorNode.has("error") || errorNode.has("message")).isTrue();
        }
    }
}
