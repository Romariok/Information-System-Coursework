package itmo.is.cw.GuitarMatchIS.models;

import org.hibernate.annotations.ColumnTransformer;

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
@IdClass(MusicianGenreId.class)
public class MusicianGenre {
   @Id
   @Column(name = "musician_id")
   private Long musicianId;

   @ManyToOne
   @JoinColumn(name = "musician_id", foreignKey = @ForeignKey(name = "fk_musician_genre_musician"))
   private Musician musician;

   @Id
   @Column(name = "genre")
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(write = "CAST(? AS genre_enum)", read = "CAST(genre AS VARCHAR)")
   private Genre genre;
}
