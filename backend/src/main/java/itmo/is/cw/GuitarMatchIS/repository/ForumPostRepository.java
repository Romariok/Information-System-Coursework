package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import itmo.is.cw.GuitarMatchIS.models.ForumPost;
import itmo.is.cw.GuitarMatchIS.models.ForumTopic;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
   Page<ForumPost> findAllByTopic(ForumTopic topic, Pageable pageable);
}
