package itmo.is.cw.GuitarMatchIS;

import itmo.is.cw.GuitarMatchIS.models.Article;
import itmo.is.cw.GuitarMatchIS.models.BodyMaterial;
import itmo.is.cw.GuitarMatchIS.models.Brand;
import itmo.is.cw.GuitarMatchIS.models.Color;
import itmo.is.cw.GuitarMatchIS.models.Country;
import itmo.is.cw.GuitarMatchIS.models.GuitarForm;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.PickupConfiguration;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.TipMaterial;
import itmo.is.cw.GuitarMatchIS.models.TypeOfProduct;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.BrandRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that JPA @Column / @JoinColumn / @Enumerated + @ColumnTransformer mappings
 * match the real PostgreSQL schema (custom ENUM types, CAST expressions, FK constraints,
 * DB-level triggers).
 *
 * Without this test, a mismatch between the Java mapping and the DDL (e.g. wrong enum
 * name, missing CAST) is only discovered at runtime in production.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Testcontainers
class JpaMappingIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.cache.type", () -> "none");
    }

    @Autowired TestEntityManager em;
    @Autowired BrandRepository brandRepository;
    @Autowired ProductRepository productRepository;
    @Autowired MusicianRepository musicianRepository;

    // =========================================================================
    // Brand — country_enum with @ColumnTransformer(write = "CAST(? AS country_enum)")
    // =========================================================================

    @Test
    void brand_countryEnum_columnTransformerRoundTrip() {
        Brand brand = Brand.builder()
                .name("Fender")
                .country(Country.USA)
                .website("https://fender.com")
                .email("info@fender.com")
                .build();

        Brand saved = brandRepository.saveAndFlush(brand);
        em.clear();

        Brand loaded = em.find(Brand.class, saved.getId());
        assertThat(loaded.getName()).isEqualTo("Fender");
        assertThat(loaded.getCountry()).isEqualTo(Country.USA);
    }

    @Test
    void brand_allCountryEnumValues_persistAndReloadCorrectly() {
        for (Country c : new Country[]{Country.JAPAN, Country.GERMANY, Country.RUSSIA}) {
            Brand brand = Brand.builder().name("Brand_" + c.name()).country(c).build();
            Brand saved = brandRepository.saveAndFlush(brand);
            em.clear();
            assertThat(em.find(Brand.class, saved.getId()).getCountry()).isEqualTo(c);
        }
    }

    // =========================================================================
    // Product — multiple enum columns, all with @ColumnTransformer CAST expressions.
    // TypeOfProduct, Color, GuitarForm, BodyMaterial, TipMaterial, PickupConfiguration
    // must all survive a save → flush → clear → reload cycle.
    // =========================================================================

    @Test
    void product_allEnumColumnsWithColumnTransformer_roundTripCorrectly() {
        Brand brand = brandRepository.saveAndFlush(
                Brand.builder().name("Gibson").country(Country.USA).build());

        Product product = Product.builder()
                .name("Les Paul Standard")
                .typeOfProduct(TypeOfProduct.ELECTRIC_GUITAR)
                .color(Color.BLACK)
                .guitarForm(GuitarForm.LES_PAUL)
                .bodyMaterial(BodyMaterial.MAHOGANY)
                .tipMaterial(TipMaterial.WOOD)
                .pickupConfiguration(PickupConfiguration.HH)
                .lads(22)
                .strings(6)
                .rate(0.0f)
                .avgPrice(0.0)
                .brand(brand)
                .build();

        Product saved = productRepository.saveAndFlush(product);
        em.clear();

        Product loaded = em.find(Product.class, saved.getId());
        assertThat(loaded.getTypeOfProduct()).isEqualTo(TypeOfProduct.ELECTRIC_GUITAR);
        assertThat(loaded.getColor()).isEqualTo(Color.BLACK);
        assertThat(loaded.getGuitarForm()).isEqualTo(GuitarForm.LES_PAUL);
        assertThat(loaded.getBodyMaterial()).isEqualTo(BodyMaterial.MAHOGANY);
        assertThat(loaded.getTipMaterial()).isEqualTo(TipMaterial.WOOD);
        assertThat(loaded.getPickupConfiguration()).isEqualTo(PickupConfiguration.HH);
    }

    @Test
    void product_nullableEnumColumns_persistNullCorrectly() {
        Brand brand = brandRepository.saveAndFlush(
                Brand.builder().name("Yamaha").country(Country.JAPAN).build());

        // Amplifier has no guitarForm/bodyMaterial/tipMaterial/pickupConfiguration
        Product product = Product.builder()
                .name("Peavey 6505")
                .typeOfProduct(TypeOfProduct.AMPLIFIER)
                .color(Color.BLACK)
                .rate(0.0f)
                .avgPrice(0.0)
                .brand(brand)
                .build();

        Product saved = productRepository.saveAndFlush(product);
        em.clear();

        Product loaded = em.find(Product.class, saved.getId());
        assertThat(loaded.getGuitarForm()).isNull();
        assertThat(loaded.getBodyMaterial()).isNull();
        assertThat(loaded.getPickupConfiguration()).isNull();
    }

    // =========================================================================
    // DB trigger: update_user_subscriptions + update_musician_subscribers
    // fires AFTER INSERT ON user_musician_subscription
    // =========================================================================

    @Test
    void subscriptionTrigger_onInsert_incrementsCountersOnBothSides() {
        User user = em.persistAndFlush(User.builder()
                .username("trigger_user")
                .password("pwd")
                .isAdmin(false)
                .subscriptions(0)
                .createdAt(LocalDateTime.now())
                .build());

        Musician musician = em.persistAndFlush(Musician.builder()
                .name("trigger_musician")
                .subscribers(0)
                .build());

        em.getEntityManager()
                .createNativeQuery(
                        "INSERT INTO user_musician_subscription (user_id, musician_id) VALUES (:uid, :mid)")
                .setParameter("uid", user.getId())
                .setParameter("mid", musician.getId())
                .executeUpdate();
        em.flush();
        em.clear();

        assertThat(em.find(User.class, user.getId()).getSubscriptions()).isEqualTo(1);
        assertThat(em.find(Musician.class, musician.getId()).getSubscribers()).isEqualTo(1);
    }

    @Test
    void subscriptionTrigger_onDelete_decrementsCountersOnBothSides() {
        User user = em.persistAndFlush(User.builder()
                .username("trigger_del_user")
                .password("pwd")
                .isAdmin(false)
                .subscriptions(0)
                .createdAt(LocalDateTime.now())
                .build());

        Musician musician = em.persistAndFlush(Musician.builder()
                .name("trigger_del_musician")
                .subscribers(0)
                .build());

        em.getEntityManager()
                .createNativeQuery(
                        "INSERT INTO user_musician_subscription (user_id, musician_id) VALUES (:uid, :mid)")
                .setParameter("uid", user.getId())
                .setParameter("mid", musician.getId())
                .executeUpdate();
        em.flush();
        em.clear();

        em.getEntityManager()
                .createNativeQuery(
                        "DELETE FROM user_musician_subscription WHERE user_id = :uid AND musician_id = :mid")
                .setParameter("uid", user.getId())
                .setParameter("mid", musician.getId())
                .executeUpdate();
        em.flush();
        em.clear();

        assertThat(em.find(User.class, user.getId()).getSubscriptions()).isEqualTo(0);
        assertThat(em.find(Musician.class, musician.getId()).getSubscribers()).isEqualTo(0);
    }

    // =========================================================================
    // DB trigger: update_product_average_price
    // fires AFTER INSERT ON shop_product
    // =========================================================================

    @Test
    void avgPriceTrigger_onShopProductInsert_updatesProductAvgPrice() {
        Brand brand = brandRepository.saveAndFlush(
                Brand.builder().name("PRS").country(Country.USA).build());

        Product product = productRepository.saveAndFlush(Product.builder()
                .name("Custom 24")
                .typeOfProduct(TypeOfProduct.ELECTRIC_GUITAR)
                .color(Color.RED)
                .rate(0.0f)
                .avgPrice(0.0)
                .brand(brand)
                .build());

        Long shopId = ((Number) em.getEntityManager()
                .createNativeQuery("INSERT INTO shop (name) VALUES ('Test Shop') RETURNING id")
                .getSingleResult()).longValue();

        em.getEntityManager()
                .createNativeQuery(
                        "INSERT INTO shop_product (shop_id, product_id, price, available) VALUES (:sid, :pid, :price, true)")
                .setParameter("sid", shopId)
                .setParameter("pid", product.getId())
                .setParameter("price", 3500.00)
                .executeUpdate();
        em.flush();
        em.clear();

        Product updated = em.find(Product.class, product.getId());
        assertThat(updated.getAvgPrice()).isEqualTo(3500.0);
    }

    // =========================================================================
    // FK constraint: article references app_user; LocalDateTime column mapping
    // =========================================================================

    @Test
    void article_foreignKeyAndLocalDateTimeColumn_persistAndReloadCorrectly() {
        User author = em.persistAndFlush(User.builder()
                .username("author_jpa")
                .password("pwd")
                .isAdmin(false)
                .subscriptions(0)
                .createdAt(LocalDateTime.now())
                .build());

        LocalDateTime articleTime = LocalDateTime.of(2024, 3, 15, 10, 30);
        Article article = Article.builder()
                .header("JPA Mapping Test")
                .text("Verifying @Column and @ManyToOne mappings")
                .author(author)
                .createdAt(articleTime)
                .accepted(false)
                .build();

        em.persistAndFlush(article);
        em.clear();

        Article loaded = em.find(Article.class, article.getId());
        assertThat(loaded.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(loaded.getAccepted()).isFalse();
        assertThat(loaded.getCreatedAt()).isEqualTo(articleTime);
    }
}
