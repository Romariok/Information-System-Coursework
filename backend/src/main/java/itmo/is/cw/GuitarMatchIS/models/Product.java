package itmo.is.cw.GuitarMatchIS.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "name", nullable = false)
   private String name;

   @Column(name = "description")
   private String description;

   @Column(name = "rate", nullable = false)
   private Float rate;

   @ManyToOne
   @JoinColumn(name = "brand_id", nullable = false)
   private Brand brand;

   @Column(name = "guitar_form")
   @Enumerated(EnumType.STRING)
   private GuitarForm guitarForm;

   @Column(name = "type_of_product", nullable = false)
   @Enumerated(EnumType.STRING)
   private TypeOfProduct typeOfProduct;

   @Column(name = "lads")
   private Integer lads;

   @Column(name = "avg_price", nullable = false)
   private Double avgPrice;

   @Column(name = "color", nullable = false)
   @Enumerated(EnumType.STRING)
   private Color color;

   @Column(name = "strings")
   private Integer strings;

   @Column(name = "tip_material")
   @Enumerated(EnumType.STRING)
   private TipMaterial tipMaterial;

   @Column(name = "body_material")
   @Enumerated(EnumType.STRING)
   private BodyMaterial bodyMaterial;

   @Column(name = "pickup_configuration")
   @Enumerated(EnumType.STRING)
   private PickupConfiguration pickupConfiguration;

   @Column(name = "type_combo_amplifier")
   @Enumerated(EnumType.STRING)
   private TypeComboAmplifier typeComboAmplifier;
}
