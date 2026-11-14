package jacinta.backend.service;

import jacinta.backend.dto.auth.*;
import jacinta.backend.dto.usuario.UsuarioResponse;
import jacinta.backend.repository.UsuarioRepository;
import jacinta.backend.model.Usuario;
import jacinta.backend.security.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioService usuarios;

    private final UsuarioRepository repository;

    private final UsuarioDetailsService details;

    private final PasswordEncoder encoder;

    private final JwtService jwt;

    @Transactional
    public UsuarioResponse registrar(RegistroRequest datos) {
        return usuarios.registrar(datos);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest d) {
        try {
            var user = details.loadUserByUsername(d.correo());
            if (!user.isEnabled() || d.contrasena().getBytes(StandardCharsets.UTF_8).length > 72
                || !encoder.matches(d.contrasena(), user.getPassword()))
                throw invalido();
            Usuario u = activo(Long.valueOf(user.getUsername()));
            return new AuthResponse(jwt.acceso(u), jwt.refresh(u), "Bearer", jwt.duracion(), usuarios.respuesta(u));
        } catch (UsernameNotFoundException e) {
            throw invalido();
        }
    }

    @Transactional(readOnly = true)
    public AccessTokenResponse refresh(RefreshRequest d) {
        Usuario u = activo(jwt.validar(d.refreshToken(), "refresh"));
        return new AccessTokenResponse(jwt.acceso(u), "Bearer", jwt.duracion());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse me(Long id) {
        return usuarios.respuesta(activo(id));
    }

    private Usuario activo(Long id) {
        return repository.findById(id).filter(Usuario::isActivo).orElseThrow(this::invalido);
    }

    private ResponseStatusException invalido() {
        return new ResponseStatusException(UNAUTHORIZED, "Credenciales inválidas");
    }
}
