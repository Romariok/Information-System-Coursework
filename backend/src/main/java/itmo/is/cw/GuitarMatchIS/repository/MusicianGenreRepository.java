package itmo.is.cw.GuitarMatchIS.repository;

import java.util.List;

import itmo.is.cw.GuitarMatchIS.models.MusicianGenre;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.keys.MusicianGenreId;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MusicianGenreRepository extends JpaRepository<MusicianGenre, MusicianGenreId> {
   List<MusicianGenre> findByMusician(Musician musician);

   List<MusicianGenre> findByMusicianIdIn(List<Long> musicianIds);

   @Transactional
   @Modifying
   @Query(value = "INSERT INTO musician_genre (musician_id, genre) VALUES (:musicianId, CAST(:genre AS genre_enum))", nativeQuery = true)
   void saveByMusicianIdAndGenre(@Param("musicianId") Long musicianId, @Param("genre") String genre);
}
