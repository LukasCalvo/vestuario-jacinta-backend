package jacinta.backend.security;

import jacinta.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public UserDetails loadUserByUsername(String correo) {
        var u = usuarios.findByCorreo(correo.strip().toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
        return User.withUsername(u.getId().toString())
            .password(u.getContrasena())
            .roles(u.getTipoUsuario().name())
            .disabled(!u.isActivo())
            .build();
    }
}
