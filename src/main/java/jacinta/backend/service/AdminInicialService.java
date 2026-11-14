package jacinta.backend.service;

import jacinta.backend.model.Usuario;
import jacinta.backend.model.enums.Rol;
import jacinta.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminInicialService {
    private final UsuarioRepository usuarios;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void crearSiNoExiste() {
        if (usuarios.existsByCorreo("admin@duoc.cl")) {
            return;
        }

        if (usuarios.existsByRun("000000000")) {
            throw new IllegalStateException("El RUN de prueba del administrador inicial ya está ocupado");
        }

        var admin = new Usuario();
        admin.setRun("000000000");
        admin.setNombre("Admin");
        admin.setApellidos("Jacinta");
        admin.setCorreo("admin@duoc.cl");
        admin.setContrasena(passwordEncoder.encode("admin1234"));
        admin.setTelefono("+56000000000");
        admin.setRegion("Valparaíso");
        admin.setComuna("Viña del Mar");
        admin.setDireccion("Cuenta de prueba Jacinta");
        admin.setTipoUsuario(Rol.ADMINISTRADOR);
        admin.setActivo(true);
        usuarios.saveAndFlush(admin);
    }
}
