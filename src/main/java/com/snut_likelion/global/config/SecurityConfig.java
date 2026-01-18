package com.snut_likelion.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snut_likelion.global.auth.PermitAllUrls;
import com.snut_likelion.global.auth.filter.JwtExceptionFilter;
import com.snut_likelion.global.auth.filter.JwtVerificationFilter;
import com.snut_likelion.global.auth.filter.RestLoginFilter;
import com.snut_likelion.global.auth.handlers.CustomAccessDeniedHandler;
import com.snut_likelion.global.auth.handlers.CustomAuthenticationEntryPoint;
import com.snut_likelion.global.auth.handlers.CustomLogoutSuccessHandler;
import com.snut_likelion.global.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(authenticationProvider);
        AuthenticationManager authenticationManager = builder.build();

        http
                .csrf(CsrfConfigurer::disable)
                .rememberMe(RememberMeConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .logout(logout -> logout
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/v1/auth/logout", "POST"))
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler(jwtService))
                )
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationManager(authenticationManager)
                .authorizeHttpRequests(
                        authz -> {
                            Arrays.stream(PermitAllUrls.values()).forEach(url -> {
                                authz.requestMatchers(url.getMethod(), url.getUrl()).permitAll();
                            });

                            authz
                                    .anyRequest().authenticated();
                        }
                )
                .addFilterBefore(restLoginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtVerificationFilter(jwtService), RestLoginFilter.class)
                .addFilterBefore(new JwtExceptionFilter(objectMapper), JwtVerificationFilter.class)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new CustomAccessDeniedHandler(objectMapper))
                );

        return http.build();
    }

    private RestLoginFilter restLoginFilter(AuthenticationManager authenticationManager) {
        RestLoginFilter restLoginFilter = new RestLoginFilter(objectMapper, jwtService);
        restLoginFilter.setAuthenticationManager(authenticationManager);
        return restLoginFilter;
    }


    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173",
                "https://snut-likelion-client.vercel.app",
                "https://www.seoultech-likelion.com",
                "https://seoultech-likelion.com"
                ));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public RoleHierarchyImpl roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER > ROLE_PREVIOUS");
    }

}
