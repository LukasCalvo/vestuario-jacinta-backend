package jacinta.backend.service;

import jacinta.backend.dto.usuario.*;
import jacinta.backend.dto.auth.RegistroRequest;
import jacinta.backend.model.Usuario;
import jacinta.backend.model.enums.Rol;
import jacinta.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarios;

    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll().stream().filter(Usuario::isActivo).map(this::respuesta).toList();
    }

    public Usuario buscar(Long id) {
        return usuarios.findById(id)
            .filter(Usuario::isActivo)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return respuesta(buscar(id));
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest d) {
        return guardar(null,
            new UsuarioRequest(d.run(), d.nombre(), d.apellidos(), d.correo(), d.contrasena(), d.telefono(),
                d.fechaNacimiento(), d.region(), d.comuna(), d.direccion(), Rol.CLIENTE),
            null, false);
    }

    @Transactional
    public UsuarioResponse guardar(Long id, UsuarioRequest d, Long actual, boolean perfil) {
        Usuario u = id == null ? new Usuario() : buscar(id);
        String correo = d.correo().strip().toLowerCase(Locale.ROOT);
        String run = normalizarRun(d.run());
        if (usuarios.existsByCorreoAndIdNot(correo, id == null ? -1L : id)
            || usuarios.existsByRunAndIdNot(run, id == null ? -1L : id))
            throw new ResponseStatusException(CONFLICT, "El correo o RUN ya está registrado");
        Rol rol = perfil ? u.getTipoUsuario() : d.tipoUsuario();
        if (rol == null)
            throw new ResponseStatusException(BAD_REQUEST, "Falta el rol");
        if (Objects.equals(id, actual) && u.getTipoUsuario() != rol)
            throw new ResponseStatusException(CONFLICT, "No puedes cambiar tu propio rol");
        String clave = d.contrasena();
        if (id == null || (clave != null && !clave.isEmpty())) {
            if (clave == null || clave.isBlank() || clave.length() < 8
                || clave.getBytes(StandardCharsets.UTF_8).length > 72)
                throw new ResponseStatusException(BAD_REQUEST, "Contraseña de al menos 8 caracteres y máximo 72 bytes");
            u.setContrasena(encoder.encode(clave));
        }
        u.setCorreo(correo);
        u.setRun(run);
        u.setNombre(d.nombre().strip());
        u.setApellidos(d.apellidos().strip());
        u.setTelefono(d.telefono().strip());
        u.setFechaNacimiento(d.fechaNacimiento());
        u.setRegion(d.region().strip());
        u.setComuna(d.comuna().strip());
        u.setDireccion(d.direccion().strip());
        u.setTipoUsuario(rol);
        return respuesta(usuarios.saveAndFlush(u));
    }

    @Transactional
    public void eliminar(Long id, Long actual) {
        if (id.equals(actual))
            throw new ResponseStatusException(CONFLICT, "No puedes desactivar tu propia cuenta");
        Usuario u = buscar(id);
        u.setActivo(false);
        usuarios.saveAndFlush(u);
    }

    public UsuarioResponse respuesta(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(),
            u.getTelefono(), u.getFechaNacimiento(), u.getRegion(), u.getComuna(), u.getDireccion(),
            u.getTipoUsuario());
    }

    public String normalizarRun(String valor) {
        String run = valor.replace(".", "").replace("-", "").strip().toUpperCase(Locale.ROOT);
        if (!run.matches("[0-9]{7,8}[0-9K]")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUN inválido");
        }
        int suma = 0, factor = 2;
        for (int i = run.length() - 2; i >= 0; i--) {
            suma += (run.charAt(i) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resultado = 11 - suma % 11;
        char digito = resultado == 11 ? '0' : resultado == 10 ? 'K' : (char) ('0' + resultado);
        if (run.charAt(run.length() - 1) != digito) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUN inválido");
        }
        return run;
    }
}
