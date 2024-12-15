package itmo.is.cw.GuitarMatchIS.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import itmo.is.cw.GuitarMatchIS.models.Brand;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.TypeOfProduct;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
   Product findByName(String name);

   @SuppressWarnings("null")
   boolean existsById(Long id);

   @SuppressWarnings("null")
   Optional<Product> findById(Long id);

   boolean existsByName(String name);

   Page<Product> findAllByBrand(Brand brand, Pageable pageable);

   Page<Product> findAllByTypeOfProduct(TypeOfProduct typeOfProduct, Pageable pageable);

   Page<Product> findAllByNameContains(String name, Pageable pageable);
}
