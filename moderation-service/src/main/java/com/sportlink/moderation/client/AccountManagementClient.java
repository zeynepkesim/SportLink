package com.sportlink.moderation.client;

import com.sportlink.moderation.client.dto.UpdateUserStatusBody;
import com.sportlink.moderation.exception.AccountSyncException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountManagementClient {

    private final WebClient accountManagementWebClient;

    public void updateUserStatus(UUID userId, String status) {
        try {
            accountManagementWebClient
                    .put()
                    .uri("/api/users/{id}/status", userId)
                    .bodyValue(UpdateUserStatusBody.builder().status(status).build())
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(3))
                    .block();
            log.info("Account Management sync OK for user {} -> {}", userId, status);
        } catch (WebClientResponseException ex) {
            log.warn("Account Management returned {} for user {}: {}",
                    ex.getStatusCode(), userId, ex.getResponseBodyAsString());
            throw new AccountSyncException(
                    "Account Management rejected status update: " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.warn("Account Management call failed for user {}: {}", userId, ex.getMessage());
            throw new AccountSyncException(
                    "Account Management is unreachable: " + ex.getMessage(), ex);
        }
    }
}
