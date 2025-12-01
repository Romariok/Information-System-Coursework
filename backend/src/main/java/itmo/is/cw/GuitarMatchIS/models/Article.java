package itmo.is.cw.GuitarMatchIS.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "articles")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Article {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "header", nullable = false)
   private String header;

   @Column(name = "text", nullable = false)
   private String text;

   @ManyToOne
   @JoinColumn(name = "author_id")
   private User author;

   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt;

   @Column(name = "accepted", nullable = false)
   private Boolean accepted;

   @Column(name = "html_content")
   private String htmlContent;
}
