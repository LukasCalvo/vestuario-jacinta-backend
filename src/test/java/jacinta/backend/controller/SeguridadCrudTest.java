package jacinta.backend.controller;

import jacinta.backend.model.*;
import jacinta.backend.model.enums.Rol;
import jacinta.backend.repository.*;
import jacinta.backend.service.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(
    properties = {"spring.datasource.url=jdbc:h2:mem:seguridad_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "app.jwt.secret=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="})
@AutoConfigureMockMvc
class SeguridadCrudTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    CategoriaRepository categorias;

    @Autowired
    JwtService jwt;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    ObjectMapper json;

    @Autowired
    JdbcTemplate sql;

    Usuario admin, vendedor, cliente;

    @BeforeEach
    void preparar() {
        sql.update("DELETE FROM producto");
        categorias.deleteAll();
        usuarios.deleteAll();
        admin = crear("admin@gmail.com", "123456785", Rol.ADMINISTRADOR);
        vendedor = crear("vendedor@gmail.com", "111111111", Rol.VENDEDOR);
        cliente = crear("cliente@gmail.com", "190110222", Rol.CLIENTE);
    }

    Usuario crear(String correo, String run, Rol rol) {
        Usuario u = new Usuario();
        u.setCorreo(correo);
        u.setRun(run);
        u.setNombre("Ana");
        u.setApellidos("Perez");
        u.setTelefono("+56912345678");
        u.setRegion("Valparaíso");
        u.setComuna("Viña del Mar");
        u.setDireccion("Calle 12345");
        u.setTipoUsuario(rol);
        u.setContrasena(encoder.encode("ClavePrueba123"));
        return usuarios.saveAndFlush(u);
    }

    record Res(int status, Map<?, ?> body) {}
    Res req(String metodo, String path, Object body, String token) throws Exception {
        var r = org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .request(org.springframework.http.HttpMethod.valueOf(metodo), path)
                    .contentType("application/json");
        if (body != null)
            r.content(json.writeValueAsString(body));
        if (token != null)
            r.header("Authorization", "Bearer " + token);
        var res = mvc.perform(r).andReturn().getResponse();
        String texto = res.getContentAsString();
        return new Res(res.getStatus(), texto.startsWith("{") ? json.readValue(texto, Map.class) : Map.of());
    }

    Map<String, Object> datos() {
        return new HashMap<>(Map.of("correo", "nuevo@gmail.com", "run", "22.222.222-2", "nombre", "Nuevo", "apellidos",
            "Perez", "telefono", "+56912345678", "region", "Valparaíso", "comuna", "Viña del Mar", "direccion",
            "Calle 12345", "contrasena", "OtraClave123", "tipoUsuario", "VENDEDOR"));
    }

    @Test
    void loginRefreshYMe() throws Exception {
        var login =
            req("POST", "/api/auth/login", Map.of("correo", "ADMIN@gmail.com", "contrasena", "ClavePrueba123"), null);
        assertEquals(200, login.status());
        String access = (String) login.body().get("accessToken"), refresh = (String) login.body().get("refreshToken");
        var me = req("GET", "/api/auth/me", null, access);
        assertEquals(200, me.status());
        assertFalse(me.body().containsKey("contrasena"));
        assertEquals("ADMINISTRADOR", me.body().get("tipoUsuario"));
        assertEquals(200, req("POST", "/api/auth/refresh", Map.of("refreshToken", refresh), null).status());
        assertEquals(401, req("POST", "/api/auth/refresh", Map.of("refreshToken", access), null).status());
        assertEquals(401, req("GET", "/api/auth/me", null, refresh).status());
        assertEquals(401, req("GET", "/api/auth/me", null, "falso").status());
        assertEquals(401,
            req("POST", "/api/auth/login", Map.of("correo", admin.getCorreo(), "contrasena", "mal"), null).status());
    }

    @Test
    void vencidosFirmasYUsuariosInactivos() throws Exception {
        var key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
            io.jsonwebtoken.io.Decoders.BASE64.decode("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="));
        String vencido = Jwts.builder()
                             .issuer("jacinta")
                             .subject(cliente.getId().toString())
                             .claim("tipo", "access")
                             .issuedAt(new Date(0))
                             .expiration(new Date(1))
                             .signWith(key)
                             .compact();
        assertEquals(401, req("GET", "/api/auth/me", null, vencido).status());
        String falso = Jwts.builder()
                           .issuer("jacinta")
                           .subject(cliente.getId().toString())
                           .claim("tipo", "access")
                           .issuedAt(new Date())
                           .expiration(new Date(System.currentTimeMillis() + 60000))
                           .signWith(Jwts.SIG.HS256.key().build())
                           .compact();
        assertEquals(401, req("GET", "/api/auth/me", null, falso).status());
        String access = jwt.acceso(cliente), refresh = jwt.refresh(cliente);
        cliente.setActivo(false);
        usuarios.saveAndFlush(cliente);
        assertEquals(401, req("GET", "/api/auth/me", null, access).status());
        assertEquals(401, req("POST", "/api/auth/refresh", Map.of("refreshToken", refresh), null).status());
    }

    @Test
    void crudUsuariosSoloAdminYPerfilSinEscalada() throws Exception {
        var d = datos();
        String ta = jwt.acceso(admin), tv = jwt.acceso(vendedor), tc = jwt.acceso(cliente);
        assertEquals(401, req("GET", "/api/usuarios", null, null).status());
        assertEquals(403, req("GET", "/api/usuarios", null, tv).status());
        assertEquals(403, req("POST", "/api/usuarios", d, tc).status());
        var creado = req("POST", "/api/usuarios", d, ta);
        assertEquals(201, creado.status());
        Long id = ((Number) creado.body().get("id")).longValue();
        assertFalse(creado.body().containsKey("contrasena"));
        String hash = usuarios.findById(id).orElseThrow().getContrasena();
        assertTrue(encoder.matches("OtraClave123", hash));
        d.put("contrasena", "");
        d.put("nombre", "Editado");
        assertEquals(200, req("PUT", "/api/usuarios/" + id, d, ta).status());
        assertEquals(hash, usuarios.findById(id).orElseThrow().getContrasena());
        d.put("contrasena", "CambioClave123");
        assertEquals(200, req("PUT", "/api/usuarios/" + id, d, ta).status());
        assertTrue(encoder.matches("CambioClave123", usuarios.findById(id).orElseThrow().getContrasena()));
        d.put("correo", cliente.getCorreo());
        d.put("run", cliente.getRun());
        d.put("tipoUsuario", "ADMINISTRADOR");
        assertEquals(200, req("PUT", "/api/usuarios/perfil", d, tc).status());
        assertEquals(Rol.CLIENTE, usuarios.findById(cliente.getId()).orElseThrow().getTipoUsuario());
        assertEquals(409, req("DELETE", "/api/usuarios/" + admin.getId(), null, ta).status());
        assertEquals(204, req("DELETE", "/api/usuarios/" + id, null, ta).status());
        assertFalse(usuarios.findById(id).orElseThrow().isActivo());
    }

    @Test
    void categoriasPublicasVendedorSoloLeeYConflictos() throws Exception {
        String ta = jwt.acceso(admin), tv = jwt.acceso(vendedor);
        assertEquals(200, req("GET", "/api/categorias", null, null).status());
        assertEquals(403, req("POST", "/api/categorias", Map.of("nombre", "Camisas"), tv).status());
        var c = req("POST", "/api/categorias", Map.of("nombre", "Camisas", "descripcion", "Ejemplo"), ta);
        assertEquals(201, c.status());
        Long id = ((Number) c.body().get("id")).longValue();
        assertEquals(200, req("GET", "/api/categorias/" + id, null, tv).status());
        assertEquals(403, req("DELETE", "/api/categorias/" + id, null, tv).status());
        assertEquals(409, req("POST", "/api/categorias", Map.of("nombre", "CAMISAS"), ta).status());
        sql.update(
            "INSERT INTO producto(categoria_id,nombre,genero,tipo,precio,tallas,colores) VALUES(?, 'Camisa', 'hombre','cam_corta',10000, '[]' FORMAT JSON,'[]' FORMAT JSON)",
            id);
        assertEquals(409, req("DELETE", "/api/categorias/" + id, null, ta).status());
        sql.update("DELETE FROM producto");
        assertEquals(200, req("PUT", "/api/categorias/" + id, Map.of("nombre", "Poleras"), ta).status());
        assertEquals(204, req("DELETE", "/api/categorias/" + id, null, ta).status());
    }
    @Test
    void permiteRegistroDesdeFrontendYRechazaOtrosOrigenes() throws Exception {
        var preflight = mvc.perform(options("/api/auth/registro")
            .header("Origin", "http://localhost:5173")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "content-type"))
            .andReturn().getResponse();
        assertEquals(200, preflight.getStatus());
        assertEquals("http://localhost:5173", preflight.getHeader("Access-Control-Allow-Origin"));

        var registro = mvc.perform(post("/api/auth/registro")
            .header("Origin", "http://localhost:5173")
            .contentType("application/json").content("{}"))
            .andReturn().getResponse();
        assertEquals(400, registro.getStatus());
        assertEquals("http://localhost:5173", registro.getHeader("Access-Control-Allow-Origin"));

        var otroOrigen = mvc.perform(options("/api/auth/registro")
            .header("Origin", "https://otro.example")
            .header("Access-Control-Request-Method", "POST"))
            .andReturn().getResponse();
        assertEquals(403, otroOrigen.getStatus());
        assertNull(otroOrigen.getHeader("Access-Control-Allow-Origin"));
    }
}
