package jacinta.backend.controller;

import jacinta.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    properties = {"spring.datasource.url=jdbc:h2:mem:registro_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "app.jwt.secret=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=", "spring.datasource.username=sa",
        "spring.datasource.password="})
@AutoConfigureMockMvc
class RegistroTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void limpiar() {
        usuarios.deleteAll();
    }

    private Map<String, Object> datos() {
        return new HashMap<>(Map.of("run", "12.345.678-5", "nombre", "Ana", "apellidos", "Perez", "correo",
            "ANA@example.com", "contrasena", "ClavePrueba123", "telefono", "+56912345678", "region", "Valparaiso",
            "comuna", "Vina del Mar", "direccion", "Calle 123"));
    }

    @Test
    void registraClienteSinExponerContrasena() throws Exception {
        var datos = datos();
        datos.put("tipoUsuario", "ADMINISTRADOR");
        datos.put("activo", false);
        var respuesta =
            mvc.perform(
                   post("/api/auth/registro").contentType("application/json").content(json.writeValueAsString(datos)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();
        var body = json.readValue(respuesta.getContentAsString(), Map.class);
        assertEquals("CLIENTE", body.get("tipoUsuario"));
        assertEquals("ana@example.com", body.get("correo"));
        assertFalse(body.containsKey("contrasena"));
        assertFalse(body.containsKey("accessToken"));
        var usuario = usuarios.findByCorreo("ana@example.com").orElseThrow();
        assertEquals("123456785", usuario.getRun());
        assertTrue(usuario.isActivo());
        assertNotEquals("ClavePrueba123", usuario.getContrasena());
        assertTrue(encoder.matches("ClavePrueba123", usuario.getContrasena()));
    }

    @Test
    void rechazaCorreoYRunDuplicados() throws Exception {
        mvc.perform(
               post("/api/auth/registro").contentType("application/json").content(json.writeValueAsString(datos())))
            .andExpect(status().isCreated());
        var correoRepetido = datos();
        correoRepetido.put("run", "11.111.111-1");
        mvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(json.writeValueAsString(correoRepetido)))
            .andExpect(status().isConflict());
        var runRepetido = datos();
        runRepetido.put("run", "123456785");
        runRepetido.put("correo", "otro@example.com");
        mvc.perform(
               post("/api/auth/registro").contentType("application/json").content(json.writeValueAsString(runRepetido)))
            .andExpect(status().isConflict());
        assertEquals(1, usuarios.count());
    }

    @Test
    void rechazaDatosInvalidosSinGuardar() throws Exception {
        for (String campo : new String[] {
                 "run", "nombre", "apellidos", "correo", "contrasena", "telefono", "region", "comuna", "direccion"}) {
            var datos = datos();
            datos.put(campo, " ");
            mvc.perform(
                   post("/api/auth/registro").contentType("application/json").content(json.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());
        }
        for (var cambio :
            Map.of("run", "12.345.678-9", "correo", "sin-correo", "contrasena", "á".repeat(40), "fechaNacimiento",
                   "2999-01-01")
                .entrySet()) {
            var datos = datos();
            datos.put(cambio.getKey(), cambio.getValue());
            mvc.perform(
                   post("/api/auth/registro").contentType("application/json").content(json.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/auth/registro").contentType("application/json").content("{"))
            .andExpect(status().isBadRequest());
        assertEquals(0, usuarios.count());
    }

    @Test
    void otrasRutasRequierenAutenticacion() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/auth/registro")).andExpect(status().isUnauthorized());
    }
}
