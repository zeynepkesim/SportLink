package com.sportlink.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.sportlink.booking.exception.SchedulingServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Synchronous client for the Scheduling Service. Encapsulates WebClient
 * usage so that {@link com.sportlink.booking.service.BookingService}
 * doesn't depend directly on Spring's web layer - making it trivial to mock
 * in unit tests.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulingClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.scheduling.url:http://localhost:8085}")
    private String schedulingServiceUrl;

    /**
     * Reserves the given time slot for the supplied facility.
     * Throws {@link SchedulingServiceException} if the Scheduling Service
     * is unreachable or returns a non-success status.
     */
    public void reserveTimeSlot(String facilityId, String timeSlotId) {
        try {
            webClientBuilder
                    .build()
                    .post()
                    .uri(schedulingServiceUrl + "/api/timeslots/reserve")
                    .bodyValue(new ReserveRequest(facilityId, timeSlotId))
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to reserve time slot {} via Scheduling Service: {}",
                    timeSlotId, e.getMessage());
            throw new SchedulingServiceException(
                    "Could not reserve time slot via Scheduling Service", e);
        }
    }

    /**
     * Releases a previously-reserved time slot.
     * Throws {@link SchedulingServiceException} if the Scheduling Service
     * is unreachable or returns a non-success status.
     */
    public void releaseTimeSlot(String timeSlotId) {
        try {
            webClientBuilder
                    .build()
                    .delete()
                    .uri(schedulingServiceUrl + "/api/timeslots/{timeSlotId}/release", timeSlotId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to release time slot {} via Scheduling Service: {}",
                    timeSlotId, e.getMessage());
            throw new SchedulingServiceException(
                    "Could not release time slot via Scheduling Service", e);
        }
    }

    /** Body for POST /api/timeslots/reserve. */
    private record ReserveRequest(String facilityId, String timeSlotId) {}
}
