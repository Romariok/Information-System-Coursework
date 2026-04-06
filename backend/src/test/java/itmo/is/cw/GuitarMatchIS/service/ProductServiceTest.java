package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.ProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductGenreDTO;
import itmo.is.cw.GuitarMatchIS.models.Brand;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.ProductSort;
import itmo.is.cw.GuitarMatchIS.models.TypeOfProduct;
import itmo.is.cw.GuitarMatchIS.repository.*;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductArticleRepository productArticleRepository;

    @Mock
    private ProductGenreRepository productGenreRepository;

    @Mock
    private MusicianProductRepository musicianProductRepository;

    @Mock
    private MusicianGenreRepository musicianGenreRepository;

    @Mock
    private MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;

    @Mock
    private ShopProductRepository shopProductRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductsById_notFound_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductsById(99L));
    }

    @Test
    void getProductsById_found_returnsProductGenreDTO() {
        Product product = TestDataFactory.buildProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productGenreRepository.findByProduct(product)).thenReturn(List.of());

        ProductGenreDTO result = productService.getProductsById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getProduct().getId()).isEqualTo(1L);
    }

    @Test
    void getGenresByProductName_productNotFound_throwsProductNotFoundException() {
        when(productRepository.existsByName("UnknownProduct")).thenReturn(false);

        assertThrows(ProductNotFoundException.class,
                () -> productService.getGenresByProductName("UnknownProduct"));
    }

    @Test
    void getGenresByProductName_withUnderscores_returnsResult() {
        Product product = TestDataFactory.buildProduct();
        when(productRepository.existsByName("Gibson_Les_Paul")).thenReturn(true);
        when(productRepository.findByName("Gibson_Les_Paul")).thenReturn(product);
        when(productGenreRepository.findByProduct(product)).thenReturn(List.of());

        ProductGenreDTO result = productService.getGenresByProductName("Gibson_Les_Paul");

        assertThat(result).isNotNull();
        assertThat(result.getProduct().getName()).isEqualTo(product.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProductsByFilter_allNullFilters_returnsProductList() {
        Product product = TestDataFactory.buildProduct();
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        List<ProductDTO> result = productService.getProductsByFilter(
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null,
                ProductSort.NAME, true, 0, 10);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(product.getId());
    }

    @Test
    void getProductsById_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> productService.getProductsById((Long) null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProductsByFilter_invalidEnum_treatedAsNullFilter_returnsList() {
        Product product = TestDataFactory.buildProduct();
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        List<ProductDTO> result = productService.getProductsByFilter(
                "Gibson", 3.0f, 5.0f, null, null, null,
                22, 1000.0, 2000.0, null, 6, null,
                null, null, null,
                ProductSort.NAME, true, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(product.getId());
    }

    @Test
    void getProductsByBrandName_withPagination_returnsList() {
        Brand brand = TestDataFactory.buildBrand();
        Product product = TestDataFactory.buildProduct();
        Page<Product> productPage = new PageImpl<>(List.of(product));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(brandRepository.existsByName("Gibson")).thenReturn(true);
        when(brandRepository.findByName("Gibson")).thenReturn(brand);
        when(productRepository.findAllByBrand(eq(brand), pageableCaptor.capture())).thenReturn(productPage);
        when(productGenreRepository.findByProduct(product)).thenReturn(List.of());

        List<ProductGenreDTO> result = productService.getProductsByBrandName("Gibson", 20, 10);

        assertThat(result).hasSize(1);
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void getProductArticles_productNotFound_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductArticles(99L, 0, 10));
    }

    @Test
    void getMusiciansByProductId_productNotFound_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getMusiciansByProductId(99L, 0, 10));
    }

    @Test
    void getProductsByTypeOfProduct_returnsList() {
        Product product = TestDataFactory.buildProduct();
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAllByTypeOfProduct(eq(TypeOfProduct.ELECTRIC_GUITAR), any(Pageable.class)))
                .thenReturn(productPage);
        when(productGenreRepository.findByProduct(product)).thenReturn(List.of());

        List<ProductGenreDTO> result = productService.getProductsByTypeOfProduct(
                TypeOfProduct.ELECTRIC_GUITAR, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    void getProductsByNameContains_blankName_returnsEmptyList() {
        when(productRepository.findAllByNameContains(eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<ProductGenreDTO> result = productService.getProductsByNameContains("", 0, 10);

        assertThat(result).isEmpty();
    }
}
