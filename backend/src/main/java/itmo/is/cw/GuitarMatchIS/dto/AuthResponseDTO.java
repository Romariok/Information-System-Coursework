package itmo.is.cw.GuitarMatchIS.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class AuthResponseDTO {
    private String username;
    private Boolean isAdmin;
    private Integer subscriptions;
    private LocalDateTime createdAt;
    private String token;
    private final String tokenType = "Bearer ";

    public AuthResponseDTO(String username, Boolean isAdmin, Integer subscriptions, LocalDateTime createdAt, String token) {
        this.username = username;
        this.isAdmin = isAdmin;
        this.subscriptions = subscriptions;
        this.createdAt = createdAt;
        this.token = token;
    }
}