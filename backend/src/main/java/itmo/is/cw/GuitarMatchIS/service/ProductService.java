package itmo.is.cw.GuitarMatchIS.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import itmo.is.cw.GuitarMatchIS.repository.specification.ProductSpecification;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.BrandNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;
import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.ArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.BrandDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianInfoDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductGenreDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductShopDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductShopsDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserInfoDTO;
import itmo.is.cw.GuitarMatchIS.models.*;
import itmo.is.cw.GuitarMatchIS.repository.BrandRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianTypeOfMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.ShopProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Comparator;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
      private final ProductRepository productRepository;
      private final BrandRepository brandRepository;
      private final ProductArticleRepository productArticleRepository;
      private final ProductGenreRepository productGenreRepository;
      private final MusicianProductRepository musicianProductRepository;
      private final MusicianGenreRepository musicianGenreRepository;
      private final MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;
      private final ShopProductRepository shopProductRepository;

      public List<ProductGenreDTO> getProductsByBrandName(String brandName, int from, int size) {
            log.info("Fetching products for brand: {} from: {} to: {}", brandName, from, size);
            Pageable page = Pagification.createPageTemplate(from, size);

            if (!brandRepository.existsByName(brandName)) {
                  log.warn("Brand with name {} not found", brandName);
                  throw new BrandNotFoundException("Brand %s not found".formatted(brandName));
            }

            Brand brand = brandRepository.findByName(brandName);

            return productRepository.findAllByBrand(brand, page).getContent().stream()
                        .map(product -> ProductGenreDTO.builder()
                                    .product(convertToDTO(product))
                                    .genres(productGenreRepository.findByProduct(product).stream()
                                                .map(ProductGenre::getGenre)
                                                .toList())
                                    .build())
                        .sorted(Comparator.comparing(productGenreDTO -> productGenreDTO.getProduct().getId()))
                        .toList();
      }

      public List<ProductGenreDTO> getProductsByTypeOfProduct(TypeOfProduct typeOfProduct, int from, int size) {
            log.info("Fetching products by type: {} from: {} to: {}", typeOfProduct, from, size);
            Pageable page = Pagification.createPageTemplate(from, size);

            return productRepository.findAllByTypeOfProduct(typeOfProduct, page).getContent().stream()
                        .map(product -> ProductGenreDTO.builder()
                                    .product(convertToDTO(product))
                                    .genres(productGenreRepository.findByProduct(product).stream()
                                                .map(ProductGenre::getGenre)
                                                .toList())
                                    .build())
                        .sorted(Comparator.comparing(productGenreDTO -> productGenreDTO.getProduct().getId()))
                        .toList();
      }

      public ProductGenreDTO getGenresByProductName(String productName) {
            log.info("Fetching genres for product: {}", productName);
            if (!productRepository.existsByName(productName)) {
                  log.warn("Product with name {} not found", productName);
                  throw new ProductNotFoundException("Product %s not found".formatted(productName));
            }

            Product product = productRepository.findByName(productName);

            List<ProductGenre> productGenres = productGenreRepository.findByProduct(product);

            return ProductGenreDTO.builder()
                        .product(convertToDTO(product))
                        .genres(productGenres.stream().map(productGenre -> productGenre.getGenre()).toList())
                        .build();
      }

      public ProductGenreDTO getProductsById(long id) {
            log.info("Fetching product by id: {}", id);
            Product p = productRepository.findById(id).orElseThrow(() -> {
                  log.warn("Product with id {} not found", id);
                  return new ProductNotFoundException("Product id = %d not found".formatted(id));
            });
            return ProductGenreDTO.builder()
                        .product(convertToDTO(p))
                        .genres(productGenreRepository.findByProduct(p).stream()
                                    .map(ProductGenre::getGenre)
                                    .toList())
                        .build();
      }

      public List<ProductGenreDTO> getProductsByNameContains(String name, int from, int size) {
            log.info("Fetching products with name containing: {}", name);
            Pageable page = Pagification.createPageTemplate(from, size);

            return productRepository.findAllByNameContains(name, page).getContent().stream()
                        .map(product -> ProductGenreDTO.builder()
                                    .product(convertToDTO(product))
                                    .genres(productGenreRepository.findByProduct(product).stream()
                                                .map(ProductGenre::getGenre)
                                                .toList())
                                    .build())
                        .sorted(Comparator.comparing(productGenreDTO -> productGenreDTO.getProduct().getId()))
                        .toList();
      }

      public ProductArticleDTO getProductArticles(long productId, int from, int size) {
            log.info("Fetching articles for product with id: {}", productId);
            Pageable page = Pagification.createPageTemplate(from, size);

            Product product = productRepository.findById(productId).orElseThrow(() -> {
                  log.warn("Product with id {} not found while fetching articles", productId);
                  return new ProductNotFoundException("Product id = %d not found".formatted(productId));
            });

            List<ProductArticle> productArticles = productArticleRepository
                        .findByProductIdAndAccepted(productId, true, page)
                        .getContent();

            return new ProductArticleDTO(convertToDTO(product),
                        productArticles.stream().map(productArticle -> productArticle.getArticle())
                                    .map(article -> ArticleDTO.builder()
                                                .id(article.getId())
                                                .header(article.getHeader())
                                                .text(article.getText())
                                                .author(UserInfoDTO.builder().id(article.getAuthor().getId())
                                                            .username(article.getAuthor().getUsername()).build())
                                                .createdAt(article.getCreatedAt())
                                                .accepted(article.getAccepted())
                                                .build())
                                    .toList());
      }

      public ProductShopsDTO getProductShops(long productId, int from, int size) {
            log.info("Fetching shops for product with id: {}", productId);
            Pageable page = Pagification.createPageTemplate(from, size);
            Product product = productRepository.findById(productId).orElseThrow(() -> {
                  log.warn("Product with id {} not found while fetching shops", productId);
                  return new ProductNotFoundException("Product id = %d not found".formatted(productId));
            });
            List<ShopProduct> shopProducts = shopProductRepository.findAllByProduct(product, page).getContent();
            List<Shop> shops = shopProducts.stream().map(shopProduct -> shopProduct.getShop()).toList();

            return ProductShopsDTO.builder()
                        .product(convertToDTO(product))
                        .shops(shops.stream().map(shop -> ProductShopDTO.builder()
                                    .id(shop.getId())
                                    .name(shop.getName())
                                    .price(shopProducts.stream()
                                                .filter(shopProduct -> Objects.equals(shopProduct.getShop().getId(),
                                                            shop.getId()))
                                                .findFirst().orElseThrow().getPrice())
                                    .available(shopProducts.stream()
                                                .filter(shopProduct -> Objects.equals(shopProduct.getShop().getId(),
                                                            shop.getId()))
                                                .findFirst().orElseThrow().getAvailable())
                                    .build())
                                    .toList())
                        .build();
      }

      public List<ProductDTO> getProductsByFilter(String name,
                  Float minRate,
                  Float maxRate,
                  Long brandId,
                  GuitarForm guitarForm,
                  TypeOfProduct typeOfProduct,
                  Integer lads,
                  Double minPrice,
                  Double maxPrice,
                  Color color,
                  Integer strings,
                  TipMaterial tipMaterial,
                  BodyMaterial bodyMaterial,
                  PickupConfiguration pickupConfiguration,
                  TypeComboAmplifier typeComboAmplifier,
                  ProductSort sortBy,
                  boolean ascending,
                  int from, int size) {

            log.info("Fetching products by filter");
            Specification<Product> specification = Specification.where(ProductSpecification.hasBrand(brandId))
                        .and(ProductSpecification.hasName(name))
                        .and(ProductSpecification.hasRateBetween(minRate, maxRate))
                        .and(ProductSpecification.hasGuitarForm(guitarForm))
                        .and(ProductSpecification.hasTypeOfProduct(typeOfProduct))
                        .and(ProductSpecification.hasLads(lads))
                        .and(ProductSpecification.hasPriceBetween(minPrice, maxPrice))
                        .and(ProductSpecification.hasColor(color))
                        .and(ProductSpecification.hasStrings(strings))
                        .and(ProductSpecification.hasTipMaterial(tipMaterial))
                        .and(ProductSpecification.hasBodyMaterial(bodyMaterial))
                        .and(ProductSpecification.hasPickupConfiguration(pickupConfiguration))
                        .and(ProductSpecification.hasTypeComboAmplifier(typeComboAmplifier));

            Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC,
                        sortBy.getFieldName());
            Pageable page = PageRequest.of(from / size, size, sort);

            return productRepository.findAll(specification, page)
                        .stream()
                        .map(this::convertToDTO)
                        .toList();
      }

      public List<MusicianInfoDTO> getMusiciansByProductId(long productId, int from, int size) {
            log.info("Fetching musicians for product with id: {}", productId);
            Pageable page = Pagification.createPageTemplate(from, size);
            Product product = productRepository.findById(productId).orElseThrow(() -> {
                  log.warn("Product with id {} not found while fetching musicians", productId);
                  return new ProductNotFoundException("Product id = %d not found".formatted(productId));
            });
            List<MusicianProduct> musicianProducts = musicianProductRepository.findByProduct(product, page)
                        .getContent();
            List<Musician> musicians = musicianProducts.stream().map(musicianProduct -> musicianProduct.getMusician())
                        .toList();
            return musicians.stream().map(musician -> MusicianInfoDTO.builder()
                        .id(musician.getId())
                        .name(musician.getName())
                        .subscribers(musician.getSubscribers())
                        .genres(musicianGenreRepository.findByMusician(musician).stream().map(MusicianGenre::getGenre)
                                    .toList())
                        .typesOfMusicians(musicianTypeOfMusicianRepository.findByMusician(musician).stream()
                                    .map(MusicianTypeOfMusician::getTypeOfMusician).toList())
                        .products(musicianProductRepository.findByMusician(musician).stream()
                                    .map(musicianProduct -> convertToDTO(musicianProduct.getProduct()))
                                    .toList())
                        .build()).toList();
      }

      private ProductDTO convertToDTO(Product product) {
            return ProductDTO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .rate(product.getRate())
                        .brand(BrandDTO.builder()
                                    .id(product.getBrand().getId())
                                    .name(product.getBrand().getName())
                                    .country(product.getBrand().getCountry())
                                    .website(product.getBrand().getWebsite())
                                    .email(product.getBrand().getEmail())
                                    .build())
                        .guitarForm(product.getGuitarForm())
                        .typeOfProduct(product.getTypeOfProduct())
                        .lads(product.getLads())
                        .avgPrice(product.getAvgPrice())
                        .color(product.getColor())
                        .strings(product.getStrings())
                        .tipMaterial(product.getTipMaterial())
                        .bodyMaterial(product.getBodyMaterial())
                        .pickupConfiguration(product.getPickupConfiguration())
                        .typeComboAmplifier(product.getTypeComboAmplifier())
                        .build();
      }
}
