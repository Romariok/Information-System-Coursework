package itmo.is.cw.GuitarMatchIS.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.MusicianProduct;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.keys.MusicianProductId;

public interface MusicianProductRepository extends JpaRepository<MusicianProduct, MusicianProductId> {
   List<MusicianProduct> findByMusician(Musician musician);
      
   boolean existsByMusicianAndProduct(Musician musician, Product product);

   void deleteByMusicianAndProduct(Musician musician, Product product);
}
