package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itmo.is.cw.GuitarMatchIS.models.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
   boolean existsByName(String name);

   Brand findByName(String name);
}
