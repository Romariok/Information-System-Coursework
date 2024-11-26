package itmo.is.cw.GuitarMatchIS.models;

import org.hibernate.annotations.ColumnTransformer;

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
   @ColumnTransformer(write = "CAST(? AS guitar_form_enum)", read = "CAST(guitar_form AS VARCHAR)")
   private GuitarForm guitarForm;

   @Column(name = "type_of_product", nullable = false)
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(write = "CAST(? AS type_of_product_enum)", read = "CAST(type_of_product AS VARCHAR)")
   private TypeOfProduct typeOfProduct;

   @Column(name = "lads")
   private Integer lads;

   @Column(name = "avg_price", nullable = false)
   private Double avgPrice;

   @Column(name = "color", nullable = false)
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(write = "CAST(? AS color_enum)", read = "CAST(color AS VARCHAR)")
   private Color color;

   @Column(name = "strings")
   private Integer strings;

   @Column(name = "tip_material")
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(write = "CAST(? AS tip_material_enum)", read = "CAST(tip_material AS VARCHAR)")
   private TipMaterial tipMaterial;

   @Column(name = "body_material")
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(write = "CAST(? AS body_material_enum)", read = "CAST(body_material AS VARCHAR)")
   private BodyMaterial bodyMaterial;

   @Column(name = "pickup_configuration")
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(write = "CAST(? AS pickup_configuration_enum)", read = "CAST(pickup_configuration AS VARCHAR)")
   private PickupConfiguration pickupConfiguration;

   @Column(name = "type_combo_amplifier")
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(write = "CAST(? AS type_combo_amplifier_enum)", read = "CAST(type_combo_amplifier AS VARCHAR)")
   private TypeComboAmplifier typeComboAmplifier;
}
