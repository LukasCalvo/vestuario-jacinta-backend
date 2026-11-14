package jacinta.backend.dto.auth;

import jacinta.backend.dto.usuario.UsuarioResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UsuarioResponse usuario
) {}
