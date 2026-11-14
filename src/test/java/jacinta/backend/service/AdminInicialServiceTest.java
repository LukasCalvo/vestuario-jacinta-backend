package jacinta.backend.service;

import jacinta.backend.model.enums.Rol;
import jacinta.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:admin_inicial_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "app.admin-inicial.enabled=true",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "app.jwt.secret=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
@Transactional
class AdminInicialServiceTest {
    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    AdminInicialService adminInicial;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthService auth;

    @Test
    void creaAdministradorAlArrancarYPermiteLogin() {
        var admin = usuarios.findByCorreo("admin@duoc.cl").orElseThrow();
        assertEquals(Rol.ADMINISTRADOR, admin.getTipoUsuario());
        assertTrue(admin.isActivo());
        assertNotEquals("admin1234", admin.getContrasena());
        assertTrue(passwordEncoder.matches("admin1234", admin.getContrasena()));

        var sesion = auth.login(new jacinta.backend.dto.auth.LoginRequest("admin@duoc.cl", "admin1234"));
        assertEquals(Rol.ADMINISTRADOR, sesion.usuario().tipoUsuario());
        assertNotNull(sesion.accessToken());
    }

    @Test
    void noDuplicaNiReiniciaLosDatosEnSiguientesArranques() {
        var admin = usuarios.findByCorreo("admin@duoc.cl").orElseThrow();
        admin.setNombre("Nombre editado");
        admin.setContrasena(passwordEncoder.encode("OtraClave123"));
        admin.setTipoUsuario(Rol.CLIENTE);
        admin.setActivo(false);
        usuarios.saveAndFlush(admin);
        String hash = admin.getContrasena();

        adminInicial.crearSiNoExiste();
        adminInicial.crearSiNoExiste();

        var existente = usuarios.findByCorreo("admin@duoc.cl").orElseThrow();
        assertEquals(1, usuarios.count());
        assertEquals(admin.getId(), existente.getId());
        assertEquals("Nombre editado", existente.getNombre());
        assertEquals(hash, existente.getContrasena());
        assertEquals(Rol.CLIENTE, existente.getTipoUsuario());
        assertFalse(existente.isActivo());
    }
}
