package com.sportlink.activitymanagement.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Reads JWT settings from application properties (prefix: sportlink.jwt)..
 */
@Component
@ConfigurationProperties(prefix = "sportlink.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private String issuer = "sportlink-account-management";
}