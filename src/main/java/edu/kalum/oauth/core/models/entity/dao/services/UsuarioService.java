package edu.kalum.oauth.core.models.entity.dao.services;

import edu.kalum.oauth.core.models.entity.Usuario;
import edu.kalum.oauth.core.models.entity.dao.IUsuarioDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("UsuarioService")
public class UsuarioService implements UserDetailsService, IUsuarioService {
    private Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    @Autowired
    private IUsuarioDao usuarioDao;
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioDao.findByUsername(username);
        if(usuario == null){
            logger.error("Error login: no existe el usuario con el username [".concat(username).concat(("]")));
            throw new UsernameNotFoundException("Error login: no existe el usuario con el username [".concat(username).concat(("]")));
        }
        List<GrantedAuthority> authorities = usuario.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getNombre()))
                .peek(authority -> logger.info("Role: ".concat(authority.getAuthority())))
                .collect(Collectors.toList());
        return new User(usuario.getUsername(), usuario.getPassword(),
                usuario.isEnabled(),true,true,true,authorities);
    }
    public Usuario save(Usuario usuario){
        return usuarioDao.save(usuario);
    }


    @Override
    public Usuario findByUsername(String username) {
        return this.usuarioDao.findByUsername(username);
    }

    @Override
    public Usuario findById(Long id){
        return this.usuarioDao.findById(id).orElse(null);
    }
    @Override
    public void deleteById(Long id){
        this.usuarioDao.deleteById(id);
    }
    @Override
    public List<Usuario> findAll(){
        return this.usuarioDao.findAll();
    }
}
