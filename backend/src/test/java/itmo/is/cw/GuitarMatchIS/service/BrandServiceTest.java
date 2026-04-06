package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.BrandDTO;
import itmo.is.cw.GuitarMatchIS.models.Brand;
import itmo.is.cw.GuitarMatchIS.repository.BrandRepository;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.BrandNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    @Test
    void getBrands_returnsList() {
        Brand brand = TestDataFactory.buildBrand();
        when(brandRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(brand)));

        List<BrandDTO> result = brandService.getBrands(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(brand.getId());
        assertThat(result.get(0).getName()).isEqualTo(brand.getName());
    }

    @Test
    void getBrands_emptyResult_returnsEmptyList() {
        when(brandRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        List<BrandDTO> result = brandService.getBrands(0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getBrandById_notFound_throwsBrandNotFoundException() {
        when(brandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class,
                () -> brandService.getBrandById(99L));
    }

    @Test
    void getBrandById_found_returnsDTO() {
        Brand brand = TestDataFactory.buildBrand();
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        BrandDTO result = brandService.getBrandById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(brand.getId());
        assertThat(result.getName()).isEqualTo(brand.getName());
    }
}
