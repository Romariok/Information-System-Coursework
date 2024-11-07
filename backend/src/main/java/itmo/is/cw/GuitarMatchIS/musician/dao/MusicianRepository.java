package itmo.is.cw.GuitarMatchIS.musician.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itmo.is.cw.GuitarMatchIS.musician.model.Musician;

@Repository
public interface MusicianRepository extends JpaRepository<Musician, Long> {
   boolean existsByName(String name);

   Musician findByName(String name);
}
