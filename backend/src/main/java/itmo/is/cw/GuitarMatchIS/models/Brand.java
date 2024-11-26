package itmo.is.cw.GuitarMatchIS.models;

import org.hibernate.annotations.ColumnTransformer;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "brand")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "country", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "CAST(? AS country_enum)", read = "CAST(country AS VARCHAR)")
    private Country country;

    @Column(name = "website")
    private String website;

    @Column(name = "email")
    private String email;

}
