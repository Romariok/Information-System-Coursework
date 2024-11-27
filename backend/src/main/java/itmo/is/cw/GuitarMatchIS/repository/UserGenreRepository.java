package itmo.is.cw.GuitarMatchIS.repository;


import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.models.UserGenre;
import itmo.is.cw.GuitarMatchIS.models.keys.UserGenreId;
import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserGenreRepository extends JpaRepository<UserGenre, UserGenreId> {
   List<UserGenre> findByUser(User user);

   @Transactional
   @Modifying
   @Query(value = "INSERT INTO genre_user (user_id, genre) VALUES (:userId, CAST(:genre AS genre_enum))", nativeQuery = true)
   void saveByUserIdAndGenre(@Param("userId") Long userId, @Param("genre") String genre);
   
   @Transactional
   @Modifying
   @Query(value = "DELETE FROM genre_user WHERE user_id = :userId", nativeQuery = true)
   void deleteAllByUser(@Param("userId") Long userId);
}
