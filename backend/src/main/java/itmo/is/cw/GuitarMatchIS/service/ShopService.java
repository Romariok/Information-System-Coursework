package itmo.is.cw.GuitarMatchIS.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.ProductOfShopDTO;
import itmo.is.cw.GuitarMatchIS.dto.ShopDTO;
import itmo.is.cw.GuitarMatchIS.dto.ShopProductDTO;
import itmo.is.cw.GuitarMatchIS.models.Shop;
import itmo.is.cw.GuitarMatchIS.models.ShopProduct;
import itmo.is.cw.GuitarMatchIS.repository.ShopProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.ShopRepository;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ShopNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopService {
   private final ShopRepository shopRepository;
   private final ShopProductRepository shopProductRepository;

   public List<ShopDTO> getShops(int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Shop> shops = shopRepository.findAll(page).getContent();

      return shops
            .stream()
            .map(shop1 -> new ShopDTO(
                  shop1.getId(),
                  shop1.getName(),
                  shop1.getDescription(),
                  shop1.getWebsite(),
                  shop1.getEmail(),
                  shop1.getAddress()))
            .sorted(new Comparator<ShopDTO>() {
               @Override
               public int compare(ShopDTO o1, ShopDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public ShopProductDTO getShopProducts(Long shopId, int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);

      Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new ShopNotFoundException("Shop with id " + shopId + " not found"));

      List<ShopProduct> shopProducts = shopProductRepository.findAllByShop(shop, page).getContent();

      return ShopProductDTO.builder()
            .shop(shop)
            .products(shopProducts.stream().map(this::convertToDTO).toList())
            .build();
   }

   public ShopDTO getShopById(Long id) {
      return convertToDTO(shopRepository.findById(id).orElseThrow(() -> new ShopNotFoundException(String.format("Shop with id %s not found", id))));
   }

   private ProductOfShopDTO convertToDTO(ShopProduct shopProduct) {
      return ProductOfShopDTO.builder()
            .product(shopProduct.getProduct())
            .price(shopProduct.getPrice())
            .available(shopProduct.getAvailable())
            .build();
   }

    private ShopDTO convertToDTO(Shop shop) {
        return new ShopDTO(
            shop.getId(),
            shop.getName(),
            shop.getDescription(),
            shop.getWebsite(),
            shop.getEmail(),
            shop.getAddress()
        );
    }
}
