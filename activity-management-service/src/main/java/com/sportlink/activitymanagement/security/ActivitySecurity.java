package com.sportlink.activitymanagement.security;

import com.sportlink.activitymanagement.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * SpEL helper for @PreAuthorize expressions on ActivityController.
 *
 * Allows rules like:
 *   @PreAuthorize("hasRole('MODERATOR') or @activitySecurity.isOrganizer(#activityId, authentication.name)")
 *
 * authentication.name is the JWT subject (account UUID as string).
 */
@Component("activitySecurity")
@RequiredArgsConstructor
public class ActivitySecurity {

    private final ActivityRepository activityRepository;

    /**
     * Returns true if the given principal (JWT subject) is the organizer
     * of the given activity. Returns false if the activity doesn't exist —
     * the controller will throw 404 later for the same reason.
     */
    public boolean isOrganizer(UUID activityId, String principalId) {
        if (activityId == null || principalId == null) {
            return false;
        }
        return activityRepository.findById(activityId)
                .map(a -> a.getOrganizerId().toString().equals(principalId))
                .orElse(false);
    }
}