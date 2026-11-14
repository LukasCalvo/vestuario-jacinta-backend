package jacinta.backend.dto.auth;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(max = 72) String contrasena
) {}
