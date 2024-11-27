package itmo.is.cw.GuitarMatchIS.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.UserProduct;
import itmo.is.cw.GuitarMatchIS.models.keys.UserProductId;

public interface UserProductRepository extends JpaRepository<UserProduct, UserProductId> {
   List<UserProduct> findByUser(User user);
      
   boolean existsByUserAndProduct(User user, Product product);

   void deleteByUserAndProduct(User user, Product product);
}
