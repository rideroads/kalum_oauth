package edu.kalum.oauth.core.models.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "rol")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @NotEmpty(message = "Es necesario ingresar un role válido")
    @Column(name = "nombre",unique = true)
    private String nombre;
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToMany(mappedBy = "roles")
    private List<Usuario> usuarios;
}
