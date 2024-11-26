package itmo.is.cw.GuitarMatchIS.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import itmo.is.cw.GuitarMatchIS.models.*;

public class ProductSpecification {

   public static Specification<Product> hasBrand(Long brandId) {
      return (root, query, criteriaBuilder) -> brandId == null ? null
            : criteriaBuilder.equal(root.get("brand").get("id"), brandId);
   }

   public static Specification<Product> hasName(String name) {
      return (root, query, criteriaBuilder) -> name == null || name.isEmpty() ? null
            : criteriaBuilder.like(root.get("name"), "%" + name + "%");
   }

   public static Specification<Product> hasRateBetween(Float minRate, Float maxRate) {
      return (root, query, criteriaBuilder) -> minRate == null || maxRate == null ? null
            : criteriaBuilder.between(root.get("rate"), minRate, maxRate);
   }

   public static Specification<Product> hasGuitarForm(GuitarForm guitarForm) {
      return (root, query, criteriaBuilder) -> guitarForm == null ? null
            : criteriaBuilder.equal(root.get("guitarForm"), guitarForm);
   }

   public static Specification<Product> hasTypeOfProduct(TypeOfProduct typeOfProduct) {
      return (root, query, criteriaBuilder) -> typeOfProduct == null ? null
            : criteriaBuilder.equal(root.get("typeOfProduct"), typeOfProduct);
   }

   public static Specification<Product> hasLads(Integer lads) {
      return (root, query, criteriaBuilder) -> lads == null || lads <= 0 ? null
            : criteriaBuilder.equal(root.get("lads"), lads);
   }

   public static Specification<Product> hasPriceBetween(Double minPrice, Double maxPrice) {
      return (root, query, criteriaBuilder) -> minPrice == null || maxPrice == null ? null
            : criteriaBuilder.between(root.get("avgPrice"), minPrice, maxPrice);
   }

   public static Specification<Product> hasColor(Color color) {
      return (root, query, criteriaBuilder) -> color == null ? null
            : criteriaBuilder.equal(root.get("color"), color);
   }

   public static Specification<Product> hasStrings(Integer strings) {
      return (root, query, criteriaBuilder) -> strings == null || strings <= 0 ? null
            : criteriaBuilder.equal(root.get("strings"), strings);
   }

   public static Specification<Product> hasTipMaterial(TipMaterial tipMaterial) {
      return (root, query, criteriaBuilder) -> tipMaterial == null ? null
            : criteriaBuilder.equal(root.get("tipMaterial"), tipMaterial);
   }

   public static Specification<Product> hasBodyMaterial(BodyMaterial bodyMaterial) {
      return (root, query, criteriaBuilder) -> bodyMaterial == null ? null
            : criteriaBuilder.equal(root.get("bodyMaterial"), bodyMaterial);
   }

   public static Specification<Product> hasPickupConfiguration(PickupConfiguration pickupConfiguration) {
      return (root, query, criteriaBuilder) -> pickupConfiguration == null ? null
            : criteriaBuilder.equal(root.get("pickupConfiguration"), pickupConfiguration);
   }

   public static Specification<Product> hasTypeComboAmplifier(TypeComboAmplifier typeComboAmplifier) {
      return (root, query, criteriaBuilder) -> typeComboAmplifier == null ? null
            : criteriaBuilder.equal(root.get("typeComboAmplifier"), typeComboAmplifier);
   }
}