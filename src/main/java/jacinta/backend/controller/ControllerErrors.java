package jacinta.backend.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

public abstract class ControllerErrors {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> error(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode())
            .body(Map.of("mensaje", error.getReason() == null ? "Solicitud inválida" : error.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validacion(MethodArgumentNotValidException error) {
        var campos = error.getBindingResult()
                         .getFieldErrors()
                         .stream()
                         .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                         .toList();
        return ResponseEntity.badRequest().body(Map.of("mensaje", "Datos inválidos", "errores", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "JSON inválido"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflicto() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("mensaje", "Los datos no se pueden registrar o ya existen"));
    }
}
