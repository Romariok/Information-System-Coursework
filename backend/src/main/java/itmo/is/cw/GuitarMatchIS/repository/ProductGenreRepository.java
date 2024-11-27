package itmo.is.cw.GuitarMatchIS.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.ProductGenre;
import itmo.is.cw.GuitarMatchIS.models.keys.ProductGenreId;

public interface ProductGenreRepository extends JpaRepository<ProductGenre, ProductGenreId> {
   List<ProductGenre> findByProduct(Product product);
}
