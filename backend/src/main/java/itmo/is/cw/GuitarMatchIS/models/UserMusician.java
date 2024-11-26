package itmo.is.cw.GuitarMatchIS.models;

import itmo.is.cw.GuitarMatchIS.models.keys.UserMusicianId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@Builder(toBuilder = true)
public class UserMusician {
   @EmbeddedId
   private UserMusicianId id;
}
