package jacinta.backend.dto.auth;
public record AccessTokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {}
