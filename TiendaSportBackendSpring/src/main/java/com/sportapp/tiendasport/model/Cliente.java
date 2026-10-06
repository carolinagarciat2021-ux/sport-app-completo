package com.sportapp.tiendasport.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entidad Cliente. Implementa UserDetails para que Spring Security pueda
 * usarla directamente como el "usuario autenticado" del sistema, sin tener
 * que mantener una clase aparte solo para login.
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clientes")
    private Integer idClientes;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, unique = true, length = 20)
    private String identificacion;

    @Column(nullable = false, length = 15)
    private String telefono;

    @Column(nullable = false, length = 100)
    private String direccion;

    @Column(nullable = false, length = 150)
    private String correo;

    // Aquí siempre se guarda el HASH de BCrypt, nunca la contraseña en texto plano.
    @Column(length = 255)
    private String password;

    @Column(name = "id_rol")
    private Integer idRol;

    // ---------- Métodos que exige la interfaz UserDetails de Spring Security ----------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convierte el id_rol numérico en un "rol" que Spring Security entiende,
        // ej. id_rol=1 -> "ROLE_ADMINISTRADOR". Esto permite proteger endpoints
        // con anotaciones como @PreAuthorize("hasRole('ADMINISTRADOR')").
        String nombreRol = switch (idRol) {
            case 1 -> "ADMINISTRADOR";
            case 3 -> "VENDEDOR";
            default -> "CLIENTE";
        };
        return List.of(new SimpleGrantedAuthority("ROLE_" + nombreRol));
    }

    @Override
    public String getUsername() { return correo; }

    @Override
    public String getPassword() { return password; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
