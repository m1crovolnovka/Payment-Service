package org.example.paymentservice.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/payments/{paymentId}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/user/{userId}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/order/{orderId}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/sum/user/{userId}").authenticated()

                        .requestMatchers(HttpMethod.GET,"/api/payments/status").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/payments/sum/total").hasAuthority("ADMIN")
                        .anyRequest().hasAuthority("ADMIN")
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
