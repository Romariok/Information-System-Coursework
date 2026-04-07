package itmo.is.cw.GuitarMatchIS.utils;

import itmo.is.cw.GuitarMatchIS.models.*;

import java.time.LocalDateTime;

public class TestDataFactory {

    public static User buildUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded-password")
                .isAdmin(false)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .subscriptions(0)
                .build();
    }

    public static User buildAdmin() {
        return User.builder()
                .id(2L)
                .username("adminuser")
                .password("encoded-password")
                .isAdmin(true)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .subscriptions(0)
                .build();
    }

    public static Musician buildMusician() {
        return Musician.builder()
                .id(1L)
                .name("Test Musician")
                .subscribers(0)
                .build();
    }

    public static Brand buildBrand() {
        return Brand.builder()
                .id(1L)
                .name("Gibson")
                .country(Country.USA)
                .website("https://gibson.com")
                .email("info@gibson.com")
                .build();
    }

    public static Product buildProduct() {
        return new Product(
                1L,
                "Gibson Les Paul",
                "Classic electric guitar",
                4.5f,
                buildBrand(),
                GuitarForm.LES_PAUL,
                TypeOfProduct.ELECTRIC_GUITAR,
                22,
                1500.0,
                Color.BLACK,
                6,
                TipMaterial.WOOD,
                BodyMaterial.MAHOGANY,
                PickupConfiguration.HH,
                null
        );
    }

    public static Article buildArticle() {
        return Article.builder()
                .id(1L)
                .header("Test Article")
                .text("# Test Content")
                .author(buildUser())
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .accepted(true)
                .htmlContent("<h1>Test Content</h1>")
                .build();
    }

    public static Article buildPendingArticle() {
        return Article.builder()
                .id(2L)
                .header("Pending Article")
                .text("# Pending Content")
                .author(buildUser())
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .accepted(false)
                .htmlContent(null)
                .build();
    }

    public static Shop buildShop() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setName("Guitar Center");
        shop.setDescription("Best guitar shop");
        shop.setWebsite("https://guitarcenter.com");
        shop.setEmail("info@guitarcenter.com");
        shop.setAddress("123 Music St");
        return shop;
    }

    public static ForumTopic buildForumTopic() {
        return ForumTopic.builder()
                .id(1L)
                .title("Test Topic")
                .description("Test topic description")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .author(buildUser())
                .isClosed(false)
                .build();
    }

    public static ForumTopic buildClosedForumTopic() {
        return ForumTopic.builder()
                .id(2L)
                .title("Closed Topic")
                .description("This topic is closed")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .author(buildUser())
                .isClosed(true)
                .build();
    }

    public static ForumPost buildForumPost() {
        return ForumPost.builder()
                .id(1L)
                .topic(buildForumTopic())
                .author(buildUser())
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .content("Test post content")
                .build();
    }
}
