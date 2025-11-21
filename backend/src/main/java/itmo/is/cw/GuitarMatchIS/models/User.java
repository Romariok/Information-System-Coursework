package itmo.is.cw.GuitarMatchIS.models;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class User implements UserDetails {
   private static final long  serialVersionUID = 1L;
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "username", unique = true, nullable = false)
   private String username;

   @Column(name = "password", unique = true, nullable = false)
   private String password;

   @Column(name = "is_admin", nullable = false)
   private Boolean isAdmin;

   @Column(name = "created_at")
   private LocalDateTime createdAt;

   @Column(name = "subscriptions", nullable = false)
   private Integer subscriptions;

   @Override
   public boolean isAccountNonExpired() {
      return true;
   }

   @Override
   public boolean isAccountNonLocked() {
      return true;
   }

   @Override
   public boolean isCredentialsNonExpired() {
      return true;
   }

   @Override
   public boolean isEnabled() {
      return true;
   }

   @Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
      return List.of(isAdmin ? Role.ADMIN : Role.USER);
   }
}
