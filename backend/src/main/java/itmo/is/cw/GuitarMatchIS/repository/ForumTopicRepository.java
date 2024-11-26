package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import itmo.is.cw.GuitarMatchIS.models.ForumTopic;

public interface ForumTopicRepository extends JpaRepository<ForumTopic, Long> {
   @Query("UPDATE forum_topic SET is_closed = true WHERE id = :topicId")
   void closeTopic(Long topicId);

}
