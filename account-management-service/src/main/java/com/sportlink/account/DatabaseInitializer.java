package com.sportlink.account;

import com.sportlink.account.model.FacilityProfile;
import com.sportlink.account.model.UserProfile;
import com.sportlink.account.model.enums.FacilityStatus;
import com.sportlink.account.model.valueobject.FacilityContactInfo;
import com.sportlink.account.model.valueobject.FacilityLocation;
import com.sportlink.account.repository.FacilityProfileRepository;
import com.sportlink.account.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final UserProfileRepository userProfileRepository;
    private final FacilityProfileRepository facilityProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        if (userProfileRepository.count() > 0 || facilityProfileRepository.count() > 0) {
            log.info("Skipping seed data, database already populated");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String insertUser = """
                INSERT INTO user_profiles
                (user_id, name, email, phone, password_hash, role, status, skill_level, language, sport_preferences, city, district, created_at, updated_at)
                VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbc.update(insertUser,
                "11111111-1111-1111-1111-111111111111", "Demo User", "demo@sportlink.test", "+372 555 0100",
                passwordEncoder.encode("demoPassword1"), "USER", "ACTIVE", "INTERMEDIATE",
                "en", "badminton,tennis", "Tartu", "Annelinn", now, now);

        jdbc.update(insertUser,
                "22222222-2222-2222-2222-222222222222", "Demo Organizer", "organizer@sportlink.test", null,
                passwordEncoder.encode("organizerPass1"), "ORGANIZER", "ACTIVE", "ADVANCED",
                null, null, "Tallinn", null, now, now);

        jdbc.update(insertUser,
                "33333333-3333-3333-3333-333333333333", "Demo Athlete", "athlete@sportlink.test", null,
                passwordEncoder.encode("athletePass1"), "USER", "ACTIVE", "BEGINNER",
                null, null, "Tartu", null, now, now);

        FacilityProfile demoFacility = FacilityProfile.builder()
                .name("Tartu Sports Hall")
                .passwordHash(passwordEncoder.encode("facilityPass1"))
                .sportTypes("badminton,basketball,volleyball")
                .contactInfo(FacilityContactInfo.builder()
                        .email("info@tartusportshall.test")
                        .contactName("Maria Tamm")
                        .contactPhone("+372 555 0200")
                        .website("https://tartusportshall.test")
                        .build())
                .location(FacilityLocation.builder()
                        .city("Tartu")
                        .district("Kesklinn")
                        .coordinates("58.3776,26.7290")
                        .build())
                .status(FacilityStatus.ACTIVE)
                .build();

        facilityProfileRepository.save(demoFacility);

        log.info("Seeded {} users and {} facilities",
                userProfileRepository.count(), facilityProfileRepository.count());
    }
}
