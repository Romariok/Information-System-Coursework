package itmo.is.cw.GuitarMatchIS.models;

import itmo.is.cw.GuitarMatchIS.models.keys.UserMusicianId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_musician_subscription")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@IdClass(UserMusicianId.class)
public class UserMusician {
   @Id
   @Column(name = "user_id")
   private Long userId;

   @Id
   @Column(name = "musician_id")
   private Long musicianId;

   @ManyToOne
   @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_musician_user"))
   private User user;

   @ManyToOne
   @JoinColumn(name = "musician_id", foreignKey = @ForeignKey(name = "fk_user_musician_musician"))
   private Musician musician;
}
