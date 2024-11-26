package itmo.is.cw.GuitarMatchIS.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.ShopDTO;
import itmo.is.cw.GuitarMatchIS.models.Shop;
import itmo.is.cw.GuitarMatchIS.repository.ShopRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopService {
   private final ShopRepository shopRepository;

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
   
}
