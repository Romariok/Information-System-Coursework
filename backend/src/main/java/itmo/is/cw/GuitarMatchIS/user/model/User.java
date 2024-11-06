package itmo.is.cw.GuitarMatchIS.user.model;

import lombok.*;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class User implements UserDetails {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "username", unique = true, nullable = false)
   private String username;

   @Column(name = "password", unique = true, nullable = false)
   private String password;

   @Column(name = "is_admin", nullable = false)
   private Boolean isAdmin;

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
