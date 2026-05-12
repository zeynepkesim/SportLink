error id: file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/search-service/src/main/java/com/sportlink/search/controller/SearchController.java:_empty_/SearchService#searchFacilities#
file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/search-service/src/main/java/com/sportlink/search/controller/SearchController.java
empty definition using pc, found symbol in pc: _empty_/SearchService#searchFacilities#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 6365
uri: file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/search-service/src/main/java/com/sportlink/search/controller/SearchController.java
text:
```scala
package com.sportlink.search.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sportlink.search.dto.IndexEntityRequest;
import com.sportlink.search.dto.SearchIndexDto;
import com.sportlink.search.model.EntityType;
import com.sportlink.search.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // ====================================================================
    // GET /api/search - cross-entity search.
    // Accepts query parameters mapped to SearchFilters fields.
    // ====================================================================
    @GetMapping("/search")
    public List<SearchIndexDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(required = false) String activityStatus,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String facilityName,
            @RequestParam(required = false) String timeSlotId) {

        // When the Booking Service calls /api/v1/search?entityType=FACILITY&...
        // (the path documented for its SearchClient), the request is forwarded
        // through the dedicated mapping below. This handler still recognises
        // the timeSlotId query parameter so it can be invoked directly from
        // the frontend or other callers without breaking.
        if (entityType == EntityType.FACILITY
                && timeSlotId != null && !timeSlotId.isBlank()) {
            return searchService.searchFacilitiesForBooking(sportType, location, timeSlotId);
        }
        return searchService.search(q, entityType, sportType, location,
                skillLevel, activityStatus, minRating, facilityName);
    }

    // ====================================================================
    // GET /api/v1/search - alias used by the Booking Service's SearchClient.
    // Forwards to the same business logic so the existing booking-service
    // SearchClient code calls into this service without modification.
    // ====================================================================
    @GetMapping("/v1/search")
    public List<SearchIndexDto> searchV1(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(required = false) String activityStatus,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String facilityName,
            @RequestParam(required = false) String timeSlotId) {
        return search(q, entityType, sportType, location, skillLevel,
                activityStatus, minRating, facilityName, timeSlotId);
    }

    // ====================================================================
    // GET /api/search/users - search only user profiles.
    // ====================================================================
    @GetMapping("/search/users")
    public List<SearchIndexDto> searchUsers(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double userRatingScore,
            @RequestParam(required = false) String userSportPreferences) {
        return searchService.searchUsers(userName, sportType, skillLevel,
                location, userRatingScore, userSportPreferences);
    }

    // ====================================================================
    // GET /api/search/activities - search only activities.
    // ====================================================================
    @GetMapping("/search/activities")
    public List<SearchIndexDto> searchActivities(
            @RequestParam(required = false) String activityTitle,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String activityStatus,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer activityMaxParticipants,
            @RequestParam(required = false) String activityTimeSlots) {
        return searchService.searchActivities(activityTitle, sportType,
                activityStatus, location, activityMaxParticipants, activityTimeSlots);
    }

    // ====================================================================
    // GET /api/search/facilities - search only sport facilities.
    // Used by the Booking Service when organizers search for a venue.
    // If sportCenterId is supplied, this triggers a real synchronous call to
    // the Booking Service to verify booking activity for that sport center.
    // ====================================================================
    @GetMapping("/search/facilities")
    public List<SearchIndexDto> searchFacilities(
            @RequestParam(required = false) String facilityName,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String sportCenterId) {
        return searchService.searchFa@@cilities(facilityName, sportType, location, sportCenterId);
    }

    // ====================================================================
    // POST /api/search/index - upsert a single entry.
    // ====================================================================
    @PostMapping("/search/index")
    @ResponseStatus(HttpStatus.CREATED)
    public SearchIndexDto indexEntity(@RequestBody IndexEntityRequest request) {
        return searchService.upsertIndex(request);
    }

    // ====================================================================
    // GET /api/search/index/{entityType}/{entityId}.
    // ====================================================================
    @GetMapping("/search/index/{entityType}/{entityId}")
    public SearchIndexDto getIndexEntry(
            @PathVariable EntityType entityType,
            @PathVariable String entityId) {
        return searchService.getIndexEntry(entityType, entityId);
    }

    // ====================================================================
    // DELETE /api/search/index/{entityType}/{entityId}.
    // ====================================================================
    @DeleteMapping("/search/index/{entityType}/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIndexEntry(
            @PathVariable EntityType entityType,
            @PathVariable String entityId) {
        searchService.deleteIndexEntry(entityType, entityId);
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/SearchService#searchFacilities#