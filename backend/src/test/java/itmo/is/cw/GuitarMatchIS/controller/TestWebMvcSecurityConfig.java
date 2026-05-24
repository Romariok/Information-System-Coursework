package itmo.is.cw.GuitarMatchIS.controller;

import itmo.is.cw.GuitarMatchIS.security.jwt.JwtAuthEntryPoint;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Import(JwtUtils.class)
class TestWebMvcSecurityConfig {

    @Bean
    SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/user/role/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JwtAuthEntryPoint()))
                .build();
    }

    @Bean
    MockMvcBuilderCustomizer securityMockMvcConfigurer() {
        return builder -> builder.apply(SecurityMockMvcConfigurers.springSecurity());
    }
}
