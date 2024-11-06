package itmo.is.cw.GuitarMatchIS.guitar_form.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guitar_form")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GuitarForm {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "name", nullable = false)
   private String name;
}
