package itmo.is.cw.GuitarMatchIS.models;

import org.hibernate.annotations.ColumnTransformer;

import itmo.is.cw.GuitarMatchIS.models.keys.UserGenreId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@IdClass(UserGenreId.class)
@Table(name = "genre_user")
@Builder(toBuilder = true)
public class UserGenre {
   @Id
   @Column(name = "user_id")
   private Long userId;

   @ManyToOne
   @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_genre_user_user"), insertable = false, updatable = false)
   private User user;

   @Id
   @Column(name = "genre")
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(read = "CAST(genre AS VARCHAR)")
   private Genre genre;
}
