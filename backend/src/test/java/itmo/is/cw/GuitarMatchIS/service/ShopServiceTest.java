package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.ShopDTO;
import itmo.is.cw.GuitarMatchIS.models.Shop;
import itmo.is.cw.GuitarMatchIS.repository.ShopProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.ShopRepository;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ShopNotFoundException;

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
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ShopProductRepository shopProductRepository;

    @InjectMocks
    private ShopService shopService;

    @Test
    void getShops_withPagination_returnsShopList() {
        Shop shop = TestDataFactory.buildShop();
        when(shopRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(shop)));

        List<ShopDTO> result = shopService.getShops(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(shop.getId());
        assertThat(result.get(0).getName()).isEqualTo(shop.getName());
    }

    @Test
    void getShops_emptyResult_returnsEmptyList() {
        when(shopRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        List<ShopDTO> result = shopService.getShops(0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getShopProducts_shopNotFound_throwsShopNotFoundException() {
        when(shopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ShopNotFoundException.class,
                () -> shopService.getShopProducts(99L, 0, 10));
    }

    @Test
    void getShopProducts_shopFound_returnsShopProductDTO() {
        Shop shop = TestDataFactory.buildShop();
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(shopProductRepository.findAllByShop(eq(shop), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = shopService.getShopProducts(1L, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getShop().getId()).isEqualTo(shop.getId());
    }
}
