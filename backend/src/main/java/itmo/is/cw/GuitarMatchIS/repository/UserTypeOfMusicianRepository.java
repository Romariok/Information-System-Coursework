package itmo.is.cw.GuitarMatchIS.repository;

import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.models.UserTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.keys.UserTypeOfMusicianId;
import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTypeOfMusicianRepository extends JpaRepository<UserTypeOfMusician, UserTypeOfMusicianId> {
   List<UserTypeOfMusician> findByUser(User user);

   @Transactional
   @Modifying
   @Query(value = "INSERT INTO type_of_musician_user (user_id, type_of_musician) VALUES (:userId, CAST(:typeOfMusician AS type_of_musician_enum))", nativeQuery = true)
   void saveByUserIdAndTypeOfMusician(@Param("userId") Long userId, @Param("typeOfMusician") String typeOfMusician);
   
   @Transactional
   @Modifying
   @Query(value = "DELETE FROM type_of_musician_user WHERE user_id = :userId", nativeQuery = true)
   void deleteAllByUser(@Param("userId") Long userId);
}
