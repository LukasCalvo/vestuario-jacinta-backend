package jacinta.backend.security;

import jacinta.backend.repository.UsuarioRepository;
import jacinta.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;

    private final UsuarioRepository usuarios;

    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            try {
                usuarios.findById(jwt.validar(h.substring(7), "access")).filter(u -> u.isActivo()).ifPresent(u -> {
                    var auth = new UsernamePasswordAuthenticationToken(u.getId().toString(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + u.getTipoUsuario().name())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (ResponseStatusException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
}
