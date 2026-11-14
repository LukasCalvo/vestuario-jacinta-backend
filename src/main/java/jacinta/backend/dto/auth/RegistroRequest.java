package jacinta.backend.dto.auth;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegistroRequest(
    @NotBlank @Size(max = 15) String run,
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank @Size(max = 150) String apellidos,
    @NotBlank @Email @Size(max = 254) String correo,
    @NotBlank @Size(min = 8, max = 72) String contrasena,
    @NotBlank @Size(max = 25) String telefono,
    @Past LocalDate fechaNacimiento,
    @NotBlank @Size(max = 150) String region,
    @NotBlank @Size(max = 150) String comuna,
    @NotBlank @Size(max = 255) String direccion
) {}
