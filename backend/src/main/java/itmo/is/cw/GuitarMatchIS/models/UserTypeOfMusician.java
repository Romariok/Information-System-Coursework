package itmo.is.cw.GuitarMatchIS.models;

import org.hibernate.annotations.ColumnTransformer;

import itmo.is.cw.GuitarMatchIS.models.keys.UserTypeOfMusicianId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "type_of_musician_user")
@Builder(toBuilder = true)
@IdClass(UserTypeOfMusicianId.class)
public class UserTypeOfMusician {
   @Id
   @Column(name = "user_id")
   private Long userId;

   @ManyToOne
   @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_type_of_musician_user_user"), insertable = false, updatable = false)
   private User user;

   @Id
   @Column(name = "type_of_musician")
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(read = "CAST(type_of_musician AS VARCHAR)")
   private TypeOfMusician typeOfMusician;
}
