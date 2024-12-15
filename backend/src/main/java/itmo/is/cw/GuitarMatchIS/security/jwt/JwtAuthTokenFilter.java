package itmo.is.cw.GuitarMatchIS.security.jwt;

import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthTokenFilter extends OncePerRequestFilter {
   @Autowired
   private JwtUtils jwtUtils;

   @Autowired
   private AuthUserDetailsService userDetailsService;

   private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

   @SuppressWarnings("null")
   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
         throws ServletException, IOException {
      try {
         UserDetails userDetails = getUserDetails(request);

         if (userDetails != null) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
         }
      } catch (Exception e) {
         logger.error("Cannot set user authentication: { }", e);
      }

      filterChain.doFilter(request, response);
   }

   public UserDetails getUserDetails(HttpServletRequest request) {
      String jwt = parseJwt(request);

      if (jwt == null || !jwtUtils.validateJwtToken(jwt))
         return null;

      String username = jwtUtils.getUserNameFromJwtToken(jwt);

      return userDetailsService.loadUserByUsername(username);
   }

   public String parseJwt(HttpServletRequest request) {
      String headerAuth = request.getHeader("Authorization");
      String bearerPrefix = "Bearer ";
      if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(bearerPrefix)) {
         return headerAuth.substring(bearerPrefix.length());
      }

      return null;
   }

}
