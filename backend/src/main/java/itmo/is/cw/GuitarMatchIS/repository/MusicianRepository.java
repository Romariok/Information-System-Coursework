package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itmo.is.cw.GuitarMatchIS.models.Musician;

@Repository
public interface MusicianRepository extends JpaRepository<Musician, Long> {
   boolean existsByName(String name);

   Musician findByName(String name);
}
