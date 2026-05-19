package com.sportlink.activitymanagement.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Reads the current request's Authorization header so that downstream
 * service-to-service calls (AccountManagementClient, SchedulingClient)
 * can propagate the user's JWT instead of calling with no credentials.
 */
@Component
public class AuthContext {

    public String currentBearerTokenOrNull() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest req = attrs.getRequest();
        String header = req.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header : null;
    }
}