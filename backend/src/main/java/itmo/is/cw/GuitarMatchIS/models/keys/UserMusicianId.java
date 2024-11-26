package itmo.is.cw.GuitarMatchIS.models.keys;

import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Embeddable
public class UserMusicianId{
   @ManyToOne
   @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_musician_user"))
   private User user;

   @ManyToOne
   @JoinColumn(name = "musician_id", foreignKey = @ForeignKey(name = "fk_user_musician_musician"))
   private Musician musician;
}
