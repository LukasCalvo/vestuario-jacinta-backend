package jacinta.backend.service;

import jacinta.backend.model.Categoria;
import jacinta.backend.repository.CategoriaRepository;
import jacinta.backend.dto.categoria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categorias;

    private Categoria buscar(Long id) {
        return categorias.findById(id).orElseThrow(
            () -> new ResponseStatusException(NOT_FOUND, "Categoría no encontrada"));
    }

    private CategoriaResponse respuesta(Categoria c) {
        return new CategoriaResponse(
            c.getId(), c.getNombre(), c.getDescripcion(), categorias.contarProductos(c.getId()));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categorias.findAll().stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return respuesta(buscar(id));
    }

    @Transactional
    public CategoriaResponse guardar(Long id, CategoriaRequest d) {
        if (categorias.existsByNombreIgnoreCaseAndIdNot(d.nombre().strip(), id == null ? -1L : id))
            throw new ResponseStatusException(CONFLICT, "Ya existe una categoría con ese nombre");
        var c = id == null ? new Categoria() : buscar(id);
        c.setNombre(d.nombre().strip());
        c.setDescripcion(d.descripcion() == null ? "" : d.descripcion().strip());
        return respuesta(categorias.saveAndFlush(c));
    }

    @Transactional
    public void eliminar(Long id) {
        var c = buscar(id);
        if (categorias.contarProductos(id) > 0)
            throw new ResponseStatusException(CONFLICT, "La categoría tiene productos asociados");
        categorias.delete(c);
        categorias.flush();
    }
}
