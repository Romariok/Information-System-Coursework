package itmo.is.cw.GuitarMatchIS.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.BrandDTO;
import itmo.is.cw.GuitarMatchIS.models.Brand;
import itmo.is.cw.GuitarMatchIS.repository.BrandRepository;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.BrandNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandService {
   private final BrandRepository brandRepository;

   public List<BrandDTO> getBrands(int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Brand> brands = brandRepository.findAll(page).getContent();

      return brands
            .stream()
            .map(brand1 -> new BrandDTO(
                  brand1.getId(),
                  brand1.getName(),
                  brand1.getCountry(),
                  brand1.getWebsite(),
                  brand1.getEmail()))
            .sorted(new Comparator<BrandDTO>() {
               @Override
               public int compare(BrandDTO o1, BrandDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public BrandDTO getBrandById(Long id) {
      return convertToDTO(brandRepository.findById(id).orElseThrow(() -> new BrandNotFoundException(String.format("Brand with id %s not found", id))));
   }

   private BrandDTO convertToDTO(Brand brand) {
      return new BrandDTO(
            brand.getId(),
            brand.getName(), 
            brand.getCountry(),
            brand.getWebsite(),
            brand.getEmail()
      );
   }
}
