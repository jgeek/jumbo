package com.jumbo.adapter.in.web;

import com.jumbo.application.domain.model.Store;
import com.jumbo.application.port.in.NearByRequest;
import com.jumbo.application.port.in.NearByUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Store Location API", description = "API for finding nearby Jumbo stores")
public class StoreController {

    private final NearByUseCase nearByService;

    @Operation(
            summary = "Get closest stores",
            description = "Returns a list of the closest stores to the given latitude and longitude.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of closest stores",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Store.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/nearby")
    public ResponseEntity<List<Store>> getClosestStores(
            @Parameter(description = "Latitude of the location", required = true, example = "52.3702")
            @RequestParam("latitude")
            @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
                    double latitude,

            @Parameter(description = "Longitude of the location", required = true, example = "4.8952")
            @RequestParam("longitude")
            @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
                    double longitude,

            @Parameter(description = "Maximum number of stores to return", example = "5")
            @RequestParam(name = "limit", defaultValue = "5")
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit cannot exceed 50")
                    int limit,

            @Parameter(description = "Whether to return only open stores", example = "false")
            @RequestParam(name = "onlyOpen", defaultValue = "false")
                    boolean onlyOpen
    ) {
        log.info("Finding nearby stores for coordinates: lat={}, lon={}, limit={}, onlyOpen={}",
                latitude, longitude, limit, onlyOpen);

        NearByRequest request = new NearByRequest(latitude, longitude, limit, onlyOpen);
        List<Store> stores = nearByService.findNearByStores(request);

        log.info("Found {} nearby stores", stores.size());
        return ResponseEntity.ok(stores);
    }
}
