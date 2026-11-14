package jacinta.backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import jacinta.backend.model.Usuario;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {
    private final SecretKey key;

    private final long acceso, renovacion;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-seconds}") long acceso,
        @Value("${app.jwt.refresh-seconds}") long renovacion) {
        if (secret.isBlank())
            throw new IllegalArgumentException("Configura JWT_SECRET en Base64, mínimo 32 bytes");
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        if (acceso <= 0 || renovacion <= acceso)
            throw new IllegalArgumentException("Duración JWT inválida");
        this.acceso = acceso;
        this.renovacion = renovacion;
    }

    public long duracion() {
        return acceso;
    }

    public String acceso(Usuario u) {
        return crear(u, "access", acceso);
    }

    public String refresh(Usuario u) {
        return crear(u, "refresh", renovacion);
    }

    private String crear(Usuario u, String tipo, long segundos) {
        Instant ahora = Instant.now();
        return Jwts.builder()
            .issuer("jacinta")
            .subject(u.getId().toString())
            .id(UUID.randomUUID().toString())
            .claim("tipo", tipo)
            .claim("rol", u.getTipoUsuario().name())
            .issuedAt(Date.from(ahora))
            .expiration(Date.from(ahora.plusSeconds(segundos)))
            .signWith(key, Jwts.SIG.HS256)
            .compact();
    }

    public Long validar(String token, String tipo) {
        try {
            var c = Jwts.parser()
                        .verifyWith(key)
                        .requireIssuer("jacinta")
                        .require("tipo", tipo)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            if (c.getExpiration() == null || c.getIssuedAt() == null)
                throw new IllegalArgumentException();
            return Long.valueOf(c.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token inválido o vencido");
        }
    }
}
