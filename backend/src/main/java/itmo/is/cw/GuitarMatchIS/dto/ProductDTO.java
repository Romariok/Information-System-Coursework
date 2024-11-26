package itmo.is.cw.GuitarMatchIS.dto;

import itmo.is.cw.GuitarMatchIS.models.BodyMaterial;
import itmo.is.cw.GuitarMatchIS.models.Color;
import itmo.is.cw.GuitarMatchIS.models.GuitarForm;
import itmo.is.cw.GuitarMatchIS.models.PickupConfiguration;
import itmo.is.cw.GuitarMatchIS.models.TipMaterial;
import itmo.is.cw.GuitarMatchIS.models.TypeComboAmplifier;
import itmo.is.cw.GuitarMatchIS.models.TypeOfProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ProductDTO {
   private Long id;
   private String name;
   private String description;
   private Float rate;
   private BrandDTO brand;
   private GuitarForm guitarForm;
   private TypeOfProduct typeOfProduct;
   private Integer lads;
   private Double avgPrice;
   private Color color;
   private Integer strings;
   private TipMaterial tipMaterial;
   private BodyMaterial bodyMaterial;
   private PickupConfiguration pickupConfiguration;
   private TypeComboAmplifier typeComboAmplifier;
}
