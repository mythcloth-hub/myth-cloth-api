package com.mesofi.mythclothapi.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/**").permitAll().anyRequest().authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }
}

// @Configuration
// public class SecurityConfig {
//  @Bean
//  public SecurityFilterChain securityFilterChain(HttpSecurity http) {

//    http.csrf(AbstractHttpConfigurer::disable)
//        .authorizeHttpRequests(
//            auth ->
//                auth.requestMatchers("/collections/**")
//                    // auth.requestMatchers("/collections/**", "/collectors/auth/**")
//                    .authenticated()
//                    .anyRequest()
//                    .permitAll())
//        .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

//    return http.build();
//  }
// }
