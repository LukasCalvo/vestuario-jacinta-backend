package jacinta.backend.repository;

import jacinta.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByRun(String run);

    boolean existsByCorreoAndIdNot(String correo, Long id);

    boolean existsByRunAndIdNot(String run, Long id);
}
