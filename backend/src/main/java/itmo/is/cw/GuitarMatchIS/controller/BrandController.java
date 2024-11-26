package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.BrandDTO;
import itmo.is.cw.GuitarMatchIS.service.BrandService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brand")
public class BrandController {
   private final BrandService brandService;

   @GetMapping
   public List<BrandDTO> getBrands(@RequestParam int from, @RequestParam int size) {
      return brandService.getBrands(from, size);
   }
}
