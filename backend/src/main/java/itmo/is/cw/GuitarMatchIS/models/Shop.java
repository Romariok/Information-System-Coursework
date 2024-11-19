package itmo.is.cw.GuitarMatchIS.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Shop {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "name", nullable = false)
   private String name;


   @Column(name = "description")
   private String description;

   @Column(name = "website")
   private String website;

   @Column(name = "email")
   private String email;

   @Column(name = "address")
   private String address;
}
