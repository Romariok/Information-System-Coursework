package itmo.is.cw.GuitarMatchIS.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.MusicianTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.keys.MusicianTypeOfMusicianId;

public interface MusicianTypeOfMusicianRepository extends JpaRepository<MusicianTypeOfMusician, MusicianTypeOfMusicianId> {
   List<MusicianTypeOfMusician> findByMusician(Musician musician);
}
