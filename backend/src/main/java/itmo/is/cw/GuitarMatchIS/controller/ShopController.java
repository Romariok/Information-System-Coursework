package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.ShopDTO;
import itmo.is.cw.GuitarMatchIS.dto.ShopProductDTO;
import itmo.is.cw.GuitarMatchIS.service.ShopService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shop")
public class ShopController {
   private final ShopService shopService;

   @GetMapping
   public List<ShopDTO> getShops(@RequestParam int from, @RequestParam int size) {
      return shopService.getShops(from, size);
   }

   @GetMapping("/{shopId}/products")
   public List<ShopProductDTO> getShopProducts(@PathVariable Long shopId, @RequestParam int from, @RequestParam int size) {
      return shopService.getShopProducts(shopId, from, size);
   }
}
