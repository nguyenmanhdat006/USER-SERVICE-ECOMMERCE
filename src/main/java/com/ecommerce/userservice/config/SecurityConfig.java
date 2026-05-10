package com.ecommerce.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String keycloakClientId;
    private final List<String> corsAllowedOrigins;

    public SecurityConfig(
            @Value("${app.security.keycloak-client-id:ecommerce-backend}") String keycloakClientId,
            @Value("#{'${app.security.cors-allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080}'.split(',')}") List<String> corsAllowedOrigins
    ) {
        this.keycloakClientId = keycloakClientId;
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/oauth2/*"
                        ).permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api-docs/**"
                        ).permitAll()

                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Get authorities from realm_access.roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            Collection<GrantedAuthority> realmRoles = Collections.emptyList();
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
                realmRoles = roles.stream()
                        .map(String::valueOf)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            // Get authorities from resource_access.{client-id}.roles
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            Collection<GrantedAuthority> resourceRoles = List.of();

            if (resourceAccess != null && resourceAccess.containsKey(keycloakClientId)) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(keycloakClientId);
                if (clientAccess != null && clientAccess.get("roles") instanceof List<?> roles) {
                    resourceRoles = roles.stream()
                            .map(String::valueOf)
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());
                }
            }

            // Get standard scope authorities
            JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);

            // Combine all authorities
            return Stream.of(realmRoles, resourceRoles, scopeAuthorities)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        });

        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsAllowedOrigins.stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}