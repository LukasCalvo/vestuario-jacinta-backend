package jacinta.backend.dto.usuario;

import jacinta.backend.model.enums.Rol;
import java.time.LocalDate;

public record UsuarioResponse(
    Long id,
    String run,
    String nombre,
    String apellidos,
    String correo,
    String telefono,
    LocalDate fechaNacimiento,
    String region,
    String comuna,
    String direccion,
    Rol tipoUsuario
) {}
