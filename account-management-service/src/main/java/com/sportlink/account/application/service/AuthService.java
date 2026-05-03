package com.sportlink.account.application.service;

import com.sportlink.account.application.dto.request.LoginRequest;
import com.sportlink.account.application.dto.response.AuthResponse;
import com.sportlink.account.domain.model.FacilityProfile;
import com.sportlink.account.domain.model.UserProfile;
import com.sportlink.account.domain.repository.FacilityProfileRepository;
import com.sportlink.account.domain.repository.UserProfileRepository;
import com.sportlink.account.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String TOKEN_PLACEHOLDER = "TODO";

    private final UserProfileRepository userProfileRepository;
    private final FacilityProfileRepository facilityProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Optional<UserProfile> userMatch = userProfileRepository.findByContactInfoEmail(request.getEmail());
        if (userMatch.isPresent()) {
            UserProfile user = userMatch.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }
            log.info("User login: {}", user.getUserId());
            return AuthResponse.builder()
                    .accountId(user.getUserId())
                    .accountType("USER")
                    .role(user.getRole().name())
                    .token(TOKEN_PLACEHOLDER)
                    .build();
        }

        Optional<FacilityProfile> facilityMatch = facilityProfileRepository.findByContactInfoEmail(request.getEmail());
        if (facilityMatch.isPresent()) {
            FacilityProfile facility = facilityMatch.get();
            if (!passwordEncoder.matches(request.getPassword(), facility.getPasswordHash())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }
            log.info("Facility login: {}", facility.getCenterId());
            return AuthResponse.builder()
                    .accountId(facility.getCenterId())
                    .accountType("FACILITY")
                    .role("FACILITY")
                    .token(TOKEN_PLACEHOLDER)
                    .build();
        }

        throw new InvalidCredentialsException("Invalid email or password");
    }
}
