package itmo.is.cw.GuitarMatchIS.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import itmo.is.cw.GuitarMatchIS.models.User;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User> findByUsername(String username);

   boolean existsByUsername(String username);

   List<User> findAllByIsAdmin(Boolean isAdmin);
}
