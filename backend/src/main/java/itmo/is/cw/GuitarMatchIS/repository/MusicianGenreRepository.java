package itmo.is.cw.GuitarMatchIS.repository;

import java.util.List;

import itmo.is.cw.GuitarMatchIS.models.MusicianGenre;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.keys.MusicianGenreId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicianGenreRepository extends JpaRepository<MusicianGenre, MusicianGenreId> {
   List<MusicianGenre> findByMusician(Musician musician);
}
