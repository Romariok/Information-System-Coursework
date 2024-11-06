package itmo.is.cw.GuitarMatchIS.genre.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itmo.is.cw.GuitarMatchIS.genre.model.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
   boolean existsByName(String name);

   Genre findByName(String name);
}
