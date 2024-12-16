package itmo.is.cw.GuitarMatchIS.repository;

import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.Shop;
import itmo.is.cw.GuitarMatchIS.models.ShopProduct;
import itmo.is.cw.GuitarMatchIS.models.keys.ShopProductId;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ShopProductRepository extends JpaRepository<ShopProduct, ShopProductId> {
   Page<ShopProduct> findAllByShop(Shop shop, Pageable page);
   Page<ShopProduct> findAllByProduct(Product product, Pageable page);
}
