package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.UserMusician;
import itmo.is.cw.GuitarMatchIS.models.keys.UserMusicianId;

public interface UserMusicianRepository extends JpaRepository<UserMusician, UserMusicianId> {
   @Procedure(procedureName = "subscribe_to_musician")
   void subscribeToMusician(
         @Param("p_user_id") Long userId,
         @Param("p_musician_id") Long musicianId);

   boolean existsByUserAndMusician(User user, Musician musician);

   void deleteByUserAndMusician(User user, Musician musician);
}
