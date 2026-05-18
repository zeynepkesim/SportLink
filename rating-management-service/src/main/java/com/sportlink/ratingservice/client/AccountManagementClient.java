package com.sportlink.ratingservice.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AccountManagementClient {

    private final WebClient.Builder webClientBuilder;
    private final boolean mock;
    private final String accountManagementUrl;

    public AccountManagementClient(WebClient.Builder webClientBuilder,
                                   @Value("${sportlink.mock.dependencies:true}") boolean mock,
                                   @Value("${sportlink.services.account-management.url:http://localhost:8081}") String accountManagementUrl) {
        this.webClientBuilder = webClientBuilder;
        this.mock = mock;
        this.accountManagementUrl = accountManagementUrl;
    }

    public boolean userExists(UUID userId) {
        if (mock) {
            log.debug("Mock mode: assuming user {} exists", userId);
            return true;
        }

        try {
            webClientBuilder.build()
                    .get()
                    .uri(accountManagementUrl + "/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check if user {} exists: {}", userId, e.getMessage());
            return false;
        }
    }
}