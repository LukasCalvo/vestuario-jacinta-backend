package jacinta.backend.controller;

import jacinta.backend.service.UsuarioService;
import jacinta.backend.dto.usuario.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController extends ControllerErrors {
    private final UsuarioService usuarios;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioResponse> listar() {
        return usuarios.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarios.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest d, Authentication a) {
        return ResponseEntity.status(201).body(usuarios.guardar(null, d, Long.valueOf(a.getName()), false));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioResponse editar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest d, Authentication a) {
        return usuarios.guardar(id, d, Long.valueOf(a.getName()), false);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, Authentication a) {
        usuarios.eliminar(id, Long.valueOf(a.getName()));
    }

    @PutMapping("/perfil")
    public UsuarioResponse perfil(@Valid @RequestBody UsuarioRequest d, Authentication a) {
        Long id = Long.valueOf(a.getName());
        return usuarios.guardar(id, d, id, true);
    }
}
