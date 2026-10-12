package com.fourspicehotpot.backend.common.security.config;

import com.fourspicehotpot.backend.common.security.filter.JwtFilter;
import com.fourspicehotpot.backend.common.security.oauth2.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {
                })
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.sendError(401);
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/customer/private/**").authenticated()
                        .requestMatchers("/api/customer/public/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/customer/auth/**").permitAll()

                        // PUBLIC API
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/**",
                                "/uploads/**",
                                "/api/payment/vnpay-return",
                                "/ws/**",
                                "/api/admin/thanh-toan/**",
                                "/api/chat/**"
                        ).permitAll()
                        // ADMIN
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "NHAN_VIEN")

                        // OTHER
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}