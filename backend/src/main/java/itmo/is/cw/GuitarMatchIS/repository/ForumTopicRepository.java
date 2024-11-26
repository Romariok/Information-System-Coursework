package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import itmo.is.cw.GuitarMatchIS.models.ForumTopic;
import itmo.is.cw.GuitarMatchIS.models.User;

public interface ForumTopicRepository extends JpaRepository<ForumTopic, Long> {
   Boolean existsByTitle(String title);

   @Modifying
   @Query("UPDATE ForumTopic ft SET ft.isClosed = true WHERE ft.id = :topicId")
   @Transactional
   int closeTopic(Long topicId);

   Page<ForumTopic> findAllByAuthor(User user, Pageable pageable);
}
