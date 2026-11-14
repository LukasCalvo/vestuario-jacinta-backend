package jacinta.backend.model;

import jakarta.persistence.*;
import jacinta.backend.model.enums.Rol;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String run;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 254)
    private String correo;

    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(nullable = false, length = 25)
    private String telefono;

    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 150)
    private String region;

    @Column(nullable = false, length = 150)
    private String comuna;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol tipoUsuario = Rol.CLIENTE;

    @Column(nullable = false)
    private boolean activo = true;
}
