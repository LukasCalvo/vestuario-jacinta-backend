package jacinta.backend.controller;

import jacinta.backend.service.CategoriaService;
import jacinta.backend.dto.categoria.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController extends ControllerErrors {
    private final CategoriaService categorias;

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categorias.listar();
    }

    @GetMapping("/{id}")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return categorias.obtener(id);
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest d) {
        return ResponseEntity.status(201).body(categorias.guardar(null, d));
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CategoriaResponse editar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest d) {
        return categorias.guardar(id, d);
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categorias.eliminar(id);
    }
}
