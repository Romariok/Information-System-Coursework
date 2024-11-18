package itmo.is.cw.GuitarMatchIS.dto;

import lombok.Getter;

@Getter
public class AuthResponseDTO {
    private String username;
    private Boolean isAdmin;
    private Integer subscriptions;
    private String token;
    private final String tokenType = "Bearer ";

    public AuthResponseDTO(String username, Boolean isAdmin, Integer subscriptions, String token) {
        this.username = username;
        this.isAdmin = isAdmin;
        this.subscriptions = subscriptions;
        this.token = token;
    }
}