package itmo.is.cw.GuitarMatchIS;

import itmo.is.cw.GuitarMatchIS.models.Article;
import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.MusicianGenre;
import itmo.is.cw.GuitarMatchIS.models.MusicianTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.models.UserGenre;
import itmo.is.cw.GuitarMatchIS.models.UserTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianTypeOfMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserTypeOfMusicianRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for custom @Query / @Procedure methods that were previously only mocked.
 *
 * Covers the following real-DB risks:
 *  - Native INSERT with CAST to PostgreSQL ENUM types (genre_enum, type_of_musician_enum)
 *  - Native DELETE with WHERE clause
 *  - Stored procedure call via JPA @Procedure (subscribe_to_musician)
 *  - PostgreSQL function call via JPQL @Query (moderate_article)
 *
 * Uses a real PostgreSQL instance (Testcontainers) with the Flyway migration applied,
 * so SQL typos, wrong CAST syntax, or wrong column names fail here, not in production.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Testcontainers
class NativeQueryRepositoryTest {

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
    @Autowired UserMusicianRepository userMusicianRepository;
    @Autowired MusicianGenreRepository musicianGenreRepository;
    @Autowired MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;
    @Autowired UserGenreRepository userGenreRepository;
    @Autowired UserTypeOfMusicianRepository userTypeOfMusicianRepository;
    @Autowired ArticleRepository articleRepository;

    private User createUser(String username) {
        User user = User.builder()
                .username(username)
                .password("hashed_pwd")
                .isAdmin(false)
                .subscriptions(0)
                .createdAt(LocalDateTime.now())
                .build();
        return em.persistAndFlush(user);
    }

    private Musician createMusician(String name) {
        Musician musician = Musician.builder()
                .name(name)
                .subscribers(0)
                .build();
        return em.persistAndFlush(musician);
    }

    // =========================================================================
    // subscribe_to_musician — stored procedure via @Procedure
    // Previously only mocked; this verifies the SQL procedure exists and runs.
    // =========================================================================

    @Test
    void subscribeToMusician_storedProcedure_insertsSubscription() {
        User user = createUser("proc_user");
        Musician musician = createMusician("proc_musician");
        em.clear();

        userMusicianRepository.subscribeToMusician(user.getId(), musician.getId());

        boolean exists = userMusicianRepository.existsByUserAndMusician(
                em.find(User.class, user.getId()),
                em.find(Musician.class, musician.getId()));
        assertThat(exists).isTrue();
    }

    // =========================================================================
    // musicianGenreRepository.saveByMusicianIdAndGenre
    // INSERT INTO musician_genre ... CAST(:genre AS genre_enum)
    // Verifies CAST syntax and genre_enum type mapping.
    // =========================================================================

    @Test
    void saveByMusicianIdAndGenre_nativeInsertWithEnumCast_rowIsPersistedAndReadable() {
        Musician musician = createMusician("genre_musician");

        musicianGenreRepository.saveByMusicianIdAndGenre(musician.getId(), "ROCK");
        em.flush();
        em.clear();

        List<MusicianGenre> genres = musicianGenreRepository
                .findByMusician(em.find(Musician.class, musician.getId()));
        assertThat(genres).hasSize(1);
        assertThat(genres.get(0).getGenre()).isEqualTo(Genre.ROCK);
    }

    // =========================================================================
    // musicianTypeOfMusicianRepository.saveByMusicianIdAndTypeOfMusician
    // INSERT INTO type_of_musician_musician ... CAST(:typeOfMusician AS type_of_musician_enum)
    // =========================================================================

    @Test
    void saveByMusicianIdAndTypeOfMusician_nativeInsertWithEnumCast_rowIsPersistedAndReadable() {
        Musician musician = createMusician("type_musician");

        musicianTypeOfMusicianRepository
                .saveByMusicianIdAndTypeOfMusician(musician.getId(), "GUITARIST");
        em.flush();
        em.clear();

        List<MusicianTypeOfMusician> types = musicianTypeOfMusicianRepository
                .findByMusician(em.find(Musician.class, musician.getId()));
        assertThat(types).hasSize(1);
        assertThat(types.get(0).getTypeOfMusician()).isEqualTo(TypeOfMusician.GUITARIST);
    }

    // =========================================================================
    // userGenreRepository.saveByUserIdAndGenre
    // INSERT INTO genre_user ... CAST(:genre AS genre_enum)
    // =========================================================================

    @Test
    void saveByUserIdAndGenre_nativeInsertWithEnumCast_rowIsPersistedAndReadable() {
        User user = createUser("genre_user");

        userGenreRepository.saveByUserIdAndGenre(user.getId(), "BLUES");
        em.flush();
        em.clear();

        List<UserGenre> genres = userGenreRepository
                .findByUser(em.find(User.class, user.getId()));
        assertThat(genres).hasSize(1);
        assertThat(genres.get(0).getGenre()).isEqualTo(Genre.BLUES);
    }

    // =========================================================================
    // userGenreRepository.deleteAllByUser
    // DELETE FROM genre_user WHERE user_id = :userId
    // Verifies table name and column name in the DELETE statement.
    // =========================================================================

    @Test
    void deleteAllByUser_nativeDelete_removesAllGenresForUser() {
        User user = createUser("del_genre_user");
        userGenreRepository.saveByUserIdAndGenre(user.getId(), "JAZZ");
        userGenreRepository.saveByUserIdAndGenre(user.getId(), "POP");
        em.flush();

        userGenreRepository.deleteAllByUser(user.getId());
        em.flush();
        em.clear();

        List<UserGenre> genres = userGenreRepository
                .findByUser(em.find(User.class, user.getId()));
        assertThat(genres).isEmpty();
    }

    // =========================================================================
    // userTypeOfMusicianRepository.saveByUserIdAndTypeOfMusician
    // INSERT INTO type_of_musician_user ... CAST(:typeOfMusician AS type_of_musician_enum)
    // =========================================================================

    @Test
    void saveByUserIdAndTypeOfMusician_nativeInsertWithEnumCast_rowIsPersistedAndReadable() {
        User user = createUser("type_user");

        userTypeOfMusicianRepository.saveByUserIdAndTypeOfMusician(user.getId(), "BASSIST");
        em.flush();
        em.clear();

        List<UserTypeOfMusician> types = userTypeOfMusicianRepository
                .findByUser(em.find(User.class, user.getId()));
        assertThat(types).hasSize(1);
        assertThat(types.get(0).getTypeOfMusician()).isEqualTo(TypeOfMusician.BASSIST);
    }

    // =========================================================================
    // userTypeOfMusicianRepository.deleteAllByUser
    // DELETE FROM type_of_musician_user WHERE user_id = :userId
    // =========================================================================

    @Test
    void deleteAllByUser_typeOfMusician_nativeDelete_removesAllTypesForUser() {
        User user = createUser("del_type_user");
        userTypeOfMusicianRepository.saveByUserIdAndTypeOfMusician(user.getId(), "SINGER");
        em.flush();

        userTypeOfMusicianRepository.deleteAllByUser(user.getId());
        em.flush();
        em.clear();

        List<UserTypeOfMusician> types = userTypeOfMusicianRepository
                .findByUser(em.find(User.class, user.getId()));
        assertThat(types).isEmpty();
    }

    // =========================================================================
    // articleRepository.moderateArticle
    // @Query("SELECT moderate_article(:articleId, :accepted, :moderatorId)")
    // Calls a PostgreSQL FUNCTION; verifies function exists, signature is correct,
    // admin check works, and UPDATE is committed within the transaction.
    // =========================================================================

    @Test
    void moderateArticle_callsPostgresFunction_updatesAcceptedFlag() {
        User admin = User.builder()
                .username("moderator_admin")
                .password("pwd")
                .isAdmin(true)
                .subscriptions(0)
                .createdAt(LocalDateTime.now())
                .build();
        em.persistAndFlush(admin);

        User author = createUser("article_author");

        Article article = Article.builder()
                .header("Test Article")
                .text("Content")
                .author(author)
                .createdAt(LocalDateTime.now())
                .accepted(false)
                .build();
        em.persistAndFlush(article);
        em.flush();
        em.clear();

        boolean result = articleRepository.moderateArticle(article.getId(), true, admin.getId());

        assertThat(result).isTrue();
        em.clear();
        Article updated = em.find(Article.class, article.getId());
        assertThat(updated.getAccepted()).isTrue();
    }
}
