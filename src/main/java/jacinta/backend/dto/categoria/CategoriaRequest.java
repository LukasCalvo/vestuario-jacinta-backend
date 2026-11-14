package jacinta.backend.dto.categoria;

import jakarta.validation.constraints.*;

public record CategoriaRequest(
        @NotBlank @Size(min = 3, max = 100) String nombre,
        @Size(max = 1000) String descripcion
) {}
