package jacinta.backend.repository;

import jacinta.backend.model.Categoria;
import org.springframework.data.jpa.repository.*;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    @Query(value = "SELECT COUNT(*) FROM producto WHERE categoria_id = :id", nativeQuery = true)
    long contarProductos(Long id);
}
