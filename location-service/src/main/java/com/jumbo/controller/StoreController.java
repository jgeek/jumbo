package com.jumbo.controller;

import com.jumbo.model.Store;
import com.jumbo.service.nearby.InMemNearByStore;
import com.jumbo.service.nearby.NearByRequest;
import com.jumbo.service.nearby.NearByService;
import com.jumbo.service.nearby.QuadTreeNearByService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    //    private final NearByService storeService;
    private final InMemNearByStore inMemNearByStore;
    private final QuadTreeNearByService quadTreeNearByService;

    public StoreController(InMemNearByStore inMemNearByStore, QuadTreeNearByService quadTreeNearByService) {
        this.inMemNearByStore = inMemNearByStore;
        this.quadTreeNearByService = quadTreeNearByService;
    }

    @Operation(
            summary = "Get closest stores",
            description = "Returns a list of the closest stores to the given latitude and longitude.",
            parameters = {
                    @Parameter(name = "latitude", description = "Latitude of the location", required = true, example = "52.3702"),
                    @Parameter(name = "longitude", description = "Longitude of the location", required = true, example = "4.8952"),
                    @Parameter(name = "limit", description = "Maximum number of stores to return", example = "5"),
                    @Parameter(name = "onlyOpen", description = "Whether to return only open stores", example = "false")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of closest stores",
                            content = @Content(mediaType = "application/json"))
            }
    )
    @GetMapping("/nearby")
    public List<Store> getClosestStores(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(name = "limit", defaultValue = "5") int limit,
            @RequestParam(name = "onlyOpen", defaultValue = "false") boolean onlyOpen,
            @RequestParam(name = "st", defaultValue = "in-memory") String strategy
    ) {
        if (strategy.equalsIgnoreCase("quadtree")) {
            return quadTreeNearByService.findNearByStores(new NearByRequest(latitude, longitude, limit, onlyOpen));
        } else {
            return inMemNearByStore.findNearByStores(new NearByRequest(latitude, longitude, limit, onlyOpen));
        }
//        return storeService.findNearByStores(new NearByRequest(latitude, longitude, limit, onlyOpen));
    }
}
