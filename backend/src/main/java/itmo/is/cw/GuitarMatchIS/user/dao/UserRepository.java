package itmo.is.cw.GuitarMatchIS.user.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import itmo.is.cw.GuitarMatchIS.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User> findByUsername(String username);

   boolean existsByUsername(String username);

   List<User> findAllByIsAdmin(Boolean isAdmin);
}
