package itmo.is.cw.GuitarMatchIS.models;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "articles")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Articles {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "header", nullable = false)
   private String header;

   @Column(name = "text", nullable = false)
   private String text;

   @ManyToOne
   @JoinColumn(name = "author_id", nullable = false)
   private User author;

   @Column(name = "created_at", nullable = false)
   private Timestamp createdAt;

   @Column(name = "accepted", nullable = false)
   private Boolean accepted;
}
