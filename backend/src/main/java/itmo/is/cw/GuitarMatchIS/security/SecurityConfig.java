package itmo.is.cw.GuitarMatchIS.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import itmo.is.cw.GuitarMatchIS.security.jwt.JwtAuthEntryPoint;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtAuthTokenFilter;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import lombok.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

   private final JwtAuthTokenFilter jwtAuthTokenFilter;

   private final AuthUserDetailsService userDetailsService;

   @Value("#{'${app.cors.allowed-origins:http://localhost:5173}'.split(',')}")
   private List<String> corsAllowedOrigins;

   @Value("${app.security.bcrypt-strength:10}")
   private int bcryptStrength;


   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http.csrf(AbstractHttpConfigurer::disable)
      .cors(cors -> cors.configurationSource(request -> {
          CorsConfiguration corsConfiguration = new CorsConfiguration();
          corsConfiguration.setAllowedOrigins(corsAllowedOrigins);
          corsConfiguration.setAllowedMethods(List.of("POST", "GET", "PUT", "PATCH","DELETE", "OPTIONS"));
          corsConfiguration.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With", "from", "size"));
          corsConfiguration.setAllowCredentials(true);
          return corsConfiguration;
      }))
      .authorizeHttpRequests(
              request -> request
                      .requestMatchers("/index.html", "/favicon.ico", "/static/**").permitAll()
                      .requestMatchers("/actuator/**").permitAll()
                      .requestMatchers("api/auth/**", "api/user/role/**", "/ws", "/api/").permitAll()
                      .requestMatchers(HttpMethod.GET, "api/admin/**").hasRole("ADMIN")
                      .requestMatchers(HttpMethod.POST, "api/moderate/**").hasRole("ADMIN")
                      .anyRequest().authenticated()
      ).userDetailsService(userDetailsService)
      .sessionManagement(session -> session
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class)
      .exceptionHandling(exHandler -> exHandler.authenticationEntryPoint(new JwtAuthEntryPoint()))
      .build();

   }

   @Bean
   public DaoAuthenticationProvider authenticationProvider() {
      DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

      authProvider.setUserDetailsService(userDetailsService);
      authProvider.setPasswordEncoder(passwordEncoder());

      return authProvider;
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder(bcryptStrength);
   }

   @Bean
   public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
      return authConfig.getAuthenticationManager();
   }

}
