package itmo.is.cw.GuitarMatchIS.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.MusicianTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.keys.MusicianTypeOfMusicianId;
import jakarta.transaction.Transactional;

public interface MusicianTypeOfMusicianRepository extends JpaRepository<MusicianTypeOfMusician, MusicianTypeOfMusicianId> {
   List<MusicianTypeOfMusician> findByMusician(Musician musician);

   @Transactional
   @Modifying
   @Query(value = "INSERT INTO type_of_musician_musician (musician_id, type_of_musician) VALUES (:musicianId, CAST(:typeOfMusician AS type_of_musician_enum))", nativeQuery = true)
   void saveByMusicianIdAndTypeOfMusician(@Param("musicianId") Long musicianId, @Param("typeOfMusician") String typeOfMusician);
}
