package itmo.is.cw.GuitarMatchIS.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.AddProductMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.BrandDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianGenreDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianTypeOfMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.SubscribeDTO;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.MusicianGenre;
import itmo.is.cw.GuitarMatchIS.models.MusicianProduct;
import itmo.is.cw.GuitarMatchIS.models.MusicianTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.MusicianGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianTypeOfMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.MusicianAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.MusicianNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductMusicianAlreadyExists;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductMusicianNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.SubscriptionAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.SubscriptionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class MusicianService {
   private final MusicianRepository musicianRepository;
   private final MusicianGenreRepository musicianGenreRepository;
   private final MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;
   private final JwtUtils jwtUtils;
   private final UserMusicianRepository userMusicianRepository;
   private final SimpMessagingTemplate simpMessagingTemplate;
   private final UserRepository userRepository;
   private final MusicianProductRepository musicianProductRepository;
   private final ProductRepository productRepository;

   public List<MusicianDTO> getMusician(int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Musician> musicians = musicianRepository.findAll(page).getContent();

      return musicians
            .stream()
            .map(musician1 -> MusicianDTO.builder()
                  .id(musician1.getId())
                  .name(musician1.getName())
                  .subscribers(musician1.getSubscribers())
                  .build())
            .sorted(new Comparator<MusicianDTO>() {
               @Override
               public int compare(MusicianDTO o1, MusicianDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   @Transactional
   public MusicianDTO createMusician(CreateMusicianDTO createMusicianDTO, HttpServletRequest request) {
      if (musicianRepository.existsByName(createMusicianDTO.getName()))
         throw new MusicianAlreadyExistsException("Musician %s already exists".formatted(createMusicianDTO.getName()));

      Musician musician = Musician.builder()
            .name(createMusicianDTO.getName())
            .subscribers(0)
            .build();

      musician = musicianRepository.save(musician);
      simpMessagingTemplate.convertAndSend("/musicians", "New musician added");

      return new MusicianDTO(musician.getId(), musician.getName(), musician.getSubscribers());
   }

   public Boolean subscribeToMusician(SubscribeDTO subscribeDTO, HttpServletRequest request) {
      User user = findUserByRequest(request);

      Musician musician = musicianRepository.findById(subscribeDTO.getMusicianId())
            .orElseThrow(() -> new MusicianNotFoundException(
                  "Musician with id %s not found".formatted(subscribeDTO.getMusicianId())));

      if (userMusicianRepository.existsByUserAndMusician(user, musician)) {
         throw new SubscriptionAlreadyExistsException("You are already subscribed to this musician");
      }
      simpMessagingTemplate.convertAndSend("/musicians", "New subscriber to musician");
      userMusicianRepository.subscribeToMusician(user.getId(), subscribeDTO.getMusicianId());
      return true;
   }

   @Transactional
   public Boolean unsubscribeFromMusician(SubscribeDTO subscribeDTO, HttpServletRequest request) {
      User user = findUserByRequest(request);

      Musician musician = musicianRepository.findById(subscribeDTO.getMusicianId())
            .orElseThrow(() -> new MusicianNotFoundException(
                  "Musician with id %s not found".formatted(subscribeDTO.getMusicianId())));

      if (!userMusicianRepository.existsByUserAndMusician(user, musician)) {
         throw new SubscriptionNotFoundException("You have no subscription to this musician");
      }
      simpMessagingTemplate.convertAndSend("/musicians", "Deleted subscription to musician");
      userMusicianRepository.deleteByUserAndMusician(user, musician);
      return true;
   }

   public List<MusicianDTO> searchMusicians(String name, int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);
      List<Musician> musicians = musicianRepository.findAllByNameContains(name, page).getContent();

      return musicians
            .stream()
            .map(musician1 -> MusicianDTO.builder()
                  .id(musician1.getId())
                  .name(musician1.getName())
                  .subscribers(musician1.getSubscribers())
                  .build())
            .sorted(new Comparator<MusicianDTO>() {
               @Override
               public int compare(MusicianDTO o1, MusicianDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public MusicianGenreDTO getMusiciansByGenre(Long musicianId) {
      Musician musician = musicianRepository.findById(musicianId)
            .orElseThrow(() -> new MusicianNotFoundException(
                  "Musician with id %s not found".formatted(musicianId)));

      List<MusicianGenre> musicianGenres = musicianGenreRepository.findByMusician(musician);

      return MusicianGenreDTO.builder()
            .musician(musician)
            .genres(musicianGenres.stream().map(MusicianGenre::getGenre).toList())
            .build();
   }

   public MusicianTypeOfMusicianDTO getMusiciansByTypeOfMusician(Long musicianId) {
      Musician musician = musicianRepository.findById(musicianId)
            .orElseThrow(() -> new MusicianNotFoundException(
                  "Musician with id %s not found".formatted(musicianId)));

      List<MusicianTypeOfMusician> musicianTypeOfMusicians = musicianTypeOfMusicianRepository.findByMusician(musician);

      return MusicianTypeOfMusicianDTO.builder()
            .musician(musician)
            .typeOfMusicians(musicianTypeOfMusicians.stream().map(MusicianTypeOfMusician::getTypeOfMusician).toList())
            .build();
   }

   public MusicianProductDTO getMusicianProducts(String name) {
      if (!musicianRepository.existsByName(name))
         throw new MusicianNotFoundException("Musician with name %s not found".formatted(name));

      Musician musician = musicianRepository.findByName(name);

      List<MusicianProduct> musicianProducts = musicianProductRepository.findByMusician(musician);

      return MusicianProductDTO.builder()
            .musician(MusicianDTO.builder()
                  .id(musician.getId())
                  .name(musician.getName())
                  .subscribers(musician.getSubscribers())
                  .build())
            .products(musicianProducts.stream().map(MusicianProduct::getProduct).map(this::convertToDTO).toList())
            .build();
   }

   @Transactional
   public Boolean addProductToMusician(AddProductMusicianDTO addProductMusicianDTO, HttpServletRequest request) {
      if (!musicianRepository.existsByName(addProductMusicianDTO.getMusicianName()))
         throw new MusicianNotFoundException(
               "Musician with name %s not found".formatted(addProductMusicianDTO.getMusicianName()));

      if (!productRepository.existsById(addProductMusicianDTO.getProductId()))
         throw new ProductNotFoundException(
               "Product with id %s not found".formatted(addProductMusicianDTO.getProductId()));

      findUserByRequest(request);
      Musician musician = musicianRepository.findByName(addProductMusicianDTO.getMusicianName());
      Product product = productRepository.findById(addProductMusicianDTO.getProductId()).get();

      if (musicianProductRepository.existsByMusicianAndProduct(musician, product))
         throw new ProductMusicianAlreadyExists("Product %s already exists in musician %s".formatted(product.getName(),
               musician.getName()));

      musicianProductRepository
            .save(MusicianProduct.builder().musicianId(musician.getId()).productId(product.getId()).build());
      return true;
   }

   @Transactional
   public Boolean deleteProductFromMusician(AddProductMusicianDTO addProductMusicianDTO, HttpServletRequest request) {
      if (!musicianRepository.existsByName(addProductMusicianDTO.getMusicianName()))
         throw new MusicianNotFoundException(
               "Musician with name %s not found".formatted(addProductMusicianDTO.getMusicianName()));

      if (!productRepository.existsById(addProductMusicianDTO.getProductId()))
         throw new ProductNotFoundException(
               "Product with id %s not found".formatted(addProductMusicianDTO.getProductId()));
      findUserByRequest(request);
      Musician musician = musicianRepository.findByName(addProductMusicianDTO.getMusicianName());
      Product product = productRepository.findById(addProductMusicianDTO.getProductId()).get();

      if (!musicianProductRepository.existsByMusicianAndProduct(musician, product))
         throw new ProductMusicianNotFoundException("Product %s not found in musician %s".formatted(product.getName(),
               musician.getName()));

      musicianProductRepository.deleteByMusicianAndProduct(musician, product);
      return true;
   }

   private User findUserByRequest(HttpServletRequest request) {
      String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
      return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                  String.format("Username %s not found", username)));
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
