package itmo.is.cw.GuitarMatchIS.brand.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "brand")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Brand {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "name", nullable = false)
   private String name;
}
