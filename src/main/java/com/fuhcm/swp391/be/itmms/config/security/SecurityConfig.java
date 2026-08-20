package com.fuhcm.swp391.be.itmms.config.security;

import com.fuhcm.swp391.be.itmms.service.CustomOAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Lazy
    private final UserDetailsService userDetailsService;
    private final JWTFilter jwtFilter;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/application",
                                "/ws/**",
                                "/topic/**",
                                "/app/**",
                                "/user/**",
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/register/resend-verification-email",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/register/confirm-email",
                                "/api/home/**",
                                "/api/doctors/**",
                                "/api/services/**",
                                "/api/list/**",
                                "/api/manager/**",
                                "/o/oauth2/v2/auth/**"
                                ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/blogs/for-user").permitAll()
                        .requestMatchers("/api/accounts/login-info").hasAnyRole("ADMIN", "MANAGER", "STAFF", "USER", "DOCTOR")
                        // USER role
                        .requestMatchers(HttpMethod.PUT, "/api/appointments/confirm-appointment").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/user/profile").hasAnyRole("USER", "DOCTOR", "MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/payment/vn-pay").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/blogs").hasAnyRole("MANAGER", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/user/appointments/available-doctors").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/appointments").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/invoices/**").hasAnyRole("USER", "STAFF", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/schedules/view/**").hasAnyRole("USER", "DOCTOR", "MANAGER", "STAFF")

                        // DOCTOR role
                        .requestMatchers(HttpMethod.GET, "/api/blogs/mine").hasRole("DOCTOR")
                        .requestMatchers("/api/blogs/manage/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/api/patient-records/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/api/treatments/follow-up/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/api/schedules/manage/**").hasAnyRole("DOCTOR", "ADMIN")

                        //STAFF role
                        .requestMatchers("/api/consultation").permitAll()

                        //MANAGER role
                        .requestMatchers(HttpMethod.GET, "/api/blogs").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/blogs").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/blogs").hasRole("MANAGER")
                        .requestMatchers("/api/invoices/report").hasRole("MANAGER")

                        // ADMIN role
                        .requestMatchers("/api/reminders/**").hasRole("ADMIN")
                        .requestMatchers("/api/medical-records/authorize/**").hasRole("MANAGER")
                        .requestMatchers("/api/service/manage/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/reviews/manage/**").hasRole("ADMIN")
                        .requestMatchers("/api/accounts/manage/**").hasRole("ADMIN")
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN")

                        // Mặc định các request khác đều cần authentication
                        .anyRequest().authenticated()
                ).oauth2Login(oauth2 -> oauth2
                        .successHandler(customOAuth2SuccessHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder.bCryptPasswordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
