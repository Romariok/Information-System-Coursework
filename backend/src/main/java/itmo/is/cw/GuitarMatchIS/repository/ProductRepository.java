package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.TypeOfProduct;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
   Page<Product> findAllByBrandId(Long brandId, Pageable pageable);

   Page<Product> findAllByTypeOfProduct(TypeOfProduct typeOfProduct, Pageable pageable);

   Page<Product> findAllByNameContains(String name, Pageable pageable);
}