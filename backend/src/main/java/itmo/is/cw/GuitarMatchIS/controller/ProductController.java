package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import itmo.is.cw.GuitarMatchIS.dto.ProductDTO;
import itmo.is.cw.GuitarMatchIS.models.BodyMaterial;
import itmo.is.cw.GuitarMatchIS.models.Color;
import itmo.is.cw.GuitarMatchIS.models.GuitarForm;
import itmo.is.cw.GuitarMatchIS.models.PickupConfiguration;
import itmo.is.cw.GuitarMatchIS.models.TipMaterial;
import itmo.is.cw.GuitarMatchIS.models.TypeComboAmplifier;
import itmo.is.cw.GuitarMatchIS.models.TypeOfProduct;
import itmo.is.cw.GuitarMatchIS.service.ProductService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {
   private final ProductService productService;

   @GetMapping("/brand/{brandId}")
   public List<ProductDTO> getProductsByBrandId(@PathVariable Long brandId, @RequestParam int from,
         @RequestParam int size) {
      return productService.getProductsByBrandId(brandId, from, size);
   }

   @GetMapping("/type/{typeOfProduct}")
   public List<ProductDTO> getProductsByTypeOfProduct(@PathVariable TypeOfProduct typeOfProduct, @RequestParam int from,
         @RequestParam int size) {
      return productService.getProductsByTypeOfProduct(typeOfProduct, from, size);
   }

   @GetMapping("/name/{name}")
   public List<ProductDTO> getProductsByNameContains(@PathVariable String name, @RequestParam int from,
         @RequestParam int size) {
      return productService.getProductsByNameContains(name, from, size);
   }

   @GetMapping("/filter")
   public List<ProductDTO> getProductsByFilter(
         @RequestParam(required = false) String name,
         @RequestParam Float minRate,
         @RequestParam Float maxRate,
         @RequestParam(required = false) Long brandId,
         @RequestParam(required = false) GuitarForm guitarForm,
         @RequestParam(required = false) TypeOfProduct typeOfProduct,
         @RequestParam(required = false) Integer lads,
         @RequestParam Double minPrice,
         @RequestParam Double maxPrice,
         @RequestParam(required = false) Color color,
         @RequestParam(required = false) Integer strings,
         @RequestParam(required = false) TipMaterial tipMaterial,
         @RequestParam(required = false) BodyMaterial bodyMaterial,
         @RequestParam(required = false) PickupConfiguration pickupConfiguration,
         @RequestParam(required = false) TypeComboAmplifier typeComboAmplifier,
         @RequestParam int from,
         @RequestParam int size) {
      return productService.getProductsByFilter(name, minRate, maxRate, brandId, guitarForm, typeOfProduct, lads,
            minPrice,
            maxPrice, color, strings, tipMaterial, bodyMaterial, pickupConfiguration, typeComboAmplifier, from, size);
   }
}
