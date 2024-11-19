package itmo.is.cw.GuitarMatchIS.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "forum_post")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ForumPost {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne
   @JoinColumn(name = "topic_id", nullable = false)
   private ForumTopic topic;

   @ManyToOne
   @JoinColumn(name = "author_id")
   private User author;


   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt;

   @Column(name = "content", nullable = false)
   private String content;
}
