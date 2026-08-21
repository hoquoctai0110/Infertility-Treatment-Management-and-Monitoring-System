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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
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
                        .requestMatchers("/ws/**",
                                "/topic/**",
                                "/app/**",
                                "/user/**",
                                "/api/manager/**").permitAll()
                        .requestMatchers("/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/register/resend-verification-email",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/register/confirm-email",
                                "/o/oauth2/v2/auth/**").permitAll()
                        .requestMatchers("/api/home/**",
                                "/api/list/**").permitAll()
                        .requestMatchers("/api/doctors/home").permitAll()
                        .requestMatchers("/api/services/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/blogs/for-user").permitAll()

                        //ACCOUNT/PROFILE
                        .requestMatchers("/api/accounts/login-info").hasAnyRole("ADMIN", "MANAGER", "STAFF", "USER", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/user/profile").hasAnyRole("USER", "DOCTOR", "MANAGER", "STAFF")
                        .requestMatchers("/api/accounts/manage/**").hasRole("ADMIN")

                        //DOCTORS
                        .requestMatchers(HttpMethod.GET, "/api/doctors/accounts").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/user/appointments/available-doctors").hasRole("USER")

                        //APPOINTMENTS
                        .requestMatchers(HttpMethod.POST, "/api/appointments/create").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/appointments/confirm-appointment").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/appointments/update-status").hasRole("STAFF")

                        //APPLICATIONS
                        .requestMatchers(HttpMethod.POST, "/api/applications").hasAnyRole("DOCTOR", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/applications/mine").hasAnyRole("DOCTOR", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/applications").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/applications").hasRole("MANAGER")

                        //CONSULTATIONS
                        .requestMatchers(HttpMethod.POST, "/api/consultations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/consultations").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/consultations").hasAnyRole("STAFF", "MANAGER")

                        //SCHEDULES
                        .requestMatchers(HttpMethod.GET, "/api/schedules/view/**").hasAnyRole("USER", "DOCTOR", "MANAGER", "STAFF")
                        .requestMatchers("/api/schedules/manage/**").hasAnyRole("DOCTOR", "ADMIN")

                        //SERVICES
                        .requestMatchers("/api/manage/services",
                                "/api/manage/services/**").hasAnyRole("ADMIN", "MANAGER")


                        //MEDICAL RECORDS
                        .requestMatchers("/api/patient-records/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/api/medical-records/authorize/**").hasRole("MANAGER")

                        //TREATMENTS
                        .requestMatchers("/api/treatments/follow-up/**").hasAnyRole("DOCTOR", "ADMIN")

                        //INVOICES / PAYMENT
                        .requestMatchers("/api/invoices/report").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/invoices/**").hasAnyRole("USER", "STAFF", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/payment/vn-pay").hasRole("USER")

                        //BLOGS
                        .requestMatchers(HttpMethod.POST, "/api/blogs").hasAnyRole("MANAGER", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/blogs/mine").hasAnyRole("DOCTOR", "MANAGER")
                        .requestMatchers("/api/blogs/manage/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/blogs").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/blogs").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/blogs").hasRole("MANAGER")

                        //REVIEWS
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("USER")
                        .requestMatchers("/api/reviews/manage/**").hasRole("ADMIN")

                        //REMINDERS
                        .requestMatchers("/api/reminders/**").hasRole("ADMIN")

                        //DASHBOARD
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN")

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
