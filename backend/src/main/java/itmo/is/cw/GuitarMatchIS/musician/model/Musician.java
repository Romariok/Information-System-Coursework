package itmo.is.cw.GuitarMatchIS.musician.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "musician")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Musician {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "name", nullable = false)
   private String name;

   @Column(name = "subscribers", nullable = false)
   private Integer subscribers;
}
