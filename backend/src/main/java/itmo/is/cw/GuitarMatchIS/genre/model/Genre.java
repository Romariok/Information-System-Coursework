package itmo.is.cw.GuitarMatchIS.genre.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "genre")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Genre {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "name", nullable = false)
   private String name;
}
