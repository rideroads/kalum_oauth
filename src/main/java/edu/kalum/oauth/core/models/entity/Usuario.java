package edu.kalum.oauth.core.models.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotEmpty(message = "El campo username no debe estar vacio")
    @Column(name = "username", unique = true)
    private String username;
    @NotEmpty(message = "Es necesario ingresar una contraseña")
    @Column(name = "password")
    private String password;
    @Column(name = "enabled")
    private boolean enabled;
    @NotEmpty(message = "El campo correo eléctronico no debe estar vacio")
    @Email(message = "Correo eléctronico invalido")
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "bio")
    private String bio;
    @NotEmpty(message = "El campo apellido no debe estar vacio")
    @Column(name = "apellidos")
    private String apellidos;
    @NotEmpty(message = "El campo nombre no debe estar vacio")
    @Column(name="nombres")
    private String nombres;
    @Column(name="foto")
    private String foto;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "usuario_role",joinColumns = @JoinColumn(name = "usuario_id"),inverseJoinColumns = @JoinColumn(name = "role_id"),
        uniqueConstraints = {@UniqueConstraint(columnNames = {"usuario_id","role_id"})})
    private List<Role> roles;
}
