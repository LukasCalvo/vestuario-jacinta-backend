package jacinta.backend.dto.categoria;
public record CategoriaResponse(
    Long id,
    String nombre,
    String descripcion,
    long productos
) {}
