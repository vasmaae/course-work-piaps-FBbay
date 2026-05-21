package com.hospital.registry.config;

import com.hospital.registry.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            log.info("Login successful: username='{}', role={}", authentication.getName(), authentication.getAuthorities());
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(isAdmin ? "/admin/dashboard" : "/dashboard");
        };
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            log.warn("Login failed: username='{}', reason={}", request.getParameter("username"), exception.getClass().getSimpleName());
            String errorType = (exception instanceof DisabledException) ? "disabled" : "credentials";
            request.getSession().setAttribute("LOGIN_ERROR", errorType);
            response.sendRedirect(request.getContextPath() + "/login?error");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/webjars/**", "/css/**", "/js/**", "/register").permitAll()
                .requestMatchers("/admin/**", "/reports/**").hasRole("ADMIN")
                .requestMatchers("/dashboard").hasRole("REGISTRAR")
                .requestMatchers("/patients/**", "/doctors/**", "/records/**", "/emr/**").hasRole("REGISTRAR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(loginSuccessHandler())
                .failureHandler(loginFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessHandler((request, response, authentication) -> {
                    if (authentication != null) {
                        log.info("Logout: username='{}'", authentication.getName());
                    }
                    response.sendRedirect("/login?logout");
                })
                .permitAll()
            );
        return http.build();
    }
}
