package edu.kalum.oauth.core.models.entity.dao.services;

import edu.kalum.oauth.core.models.entity.Usuario;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.List;

public interface IUsuarioService {
    public Usuario findByUsername(String username);
    public Usuario findById(Long id);
    public List<Usuario> findAll();
    public void deleteById(Long id);
    public Usuario findByEmail(String email);
}