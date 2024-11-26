package itmo.is.cw.GuitarMatchIS.models;

import org.hibernate.annotations.ColumnTransformer;

import itmo.is.cw.GuitarMatchIS.models.keys.MusicianTypeOfMusicianId;
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
@Table(name = "type_of_musician_musician")
@Builder(toBuilder = true)
@IdClass(MusicianTypeOfMusicianId.class)
public class MusicianTypeOfMusician {
   @Id
   @Column(name = "musician_id")
   private Long musicianId;

   @ManyToOne
   @JoinColumn(name = "musician_id", foreignKey = @ForeignKey(name = "fk_musician_genre_musician"))
   private Musician musician;

   @Id
   @Column(name = "type_of_musician")
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(write = "CAST(? AS type_of_musician_enum)", read = "CAST(type_of_musician AS VARCHAR)")
   private TypeOfMusician typeOfMusician;
}
