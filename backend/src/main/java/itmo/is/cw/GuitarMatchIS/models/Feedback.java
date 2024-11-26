package itmo.is.cw.GuitarMatchIS.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Feedback {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne
   @JoinColumn(name = "author_id", nullable = false)
   private User author;

   @ManyToOne
   @JoinColumn(name = "product_id")
   private Product product;

   @ManyToOne
   @JoinColumn(name = "article_id")
   private Article article;

   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt;

   @Column(name = "text", nullable = false)
   private String text;

   @Column(name = "stars", nullable = false)
   private Integer stars;
}
