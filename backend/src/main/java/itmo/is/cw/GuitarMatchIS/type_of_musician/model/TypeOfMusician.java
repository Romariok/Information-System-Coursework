package itmo.is.cw.GuitarMatchIS.type_of_musician.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_of_musician")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TypeOfMusician {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "name", nullable = false)
   private String name;
}
