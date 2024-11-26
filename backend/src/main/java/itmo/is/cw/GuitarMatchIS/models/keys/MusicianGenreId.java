package itmo.is.cw.GuitarMatchIS.models.keys;

import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class MusicianGenreId{
   @ManyToOne
   @JoinColumn(name = "musician_id", foreignKey = @ForeignKey(name = "fk_musician_genre_musician"))
   private Musician musician;

   @Column(name = "genre")
   @Enumerated(EnumType.STRING)
   private Genre genre;
}
