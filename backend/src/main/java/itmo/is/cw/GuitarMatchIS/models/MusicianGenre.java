package itmo.is.cw.GuitarMatchIS.models;

import itmo.is.cw.GuitarMatchIS.models.keys.MusicianGenreId;
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
@Builder(toBuilder = true)
public class MusicianGenre {
   @EmbeddedId
   private MusicianGenreId id;

   // @ManyToOne
   // @MapsId("musicianId")
   // @JoinColumn(name = "musician_id")
   // private Musician musician;

   // @Enumerated(EnumType.STRING)
   // @Column(name = "genre")
   // private Genre genre;
}
