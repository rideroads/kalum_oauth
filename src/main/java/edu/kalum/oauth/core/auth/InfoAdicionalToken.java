package edu.kalum.oauth.core.auth;

import edu.kalum.oauth.core.models.entity.Usuario;
import edu.kalum.oauth.core.models.entity.dao.services.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class InfoAdicionalToken implements TokenEnhancer {
    @Autowired
    private IUsuarioService usuarioService;
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Usuario usuario = this.usuarioService.findByUsername(authentication.getName());
        Map<String,Object> info = new HashMap<>();
        info.put("apellidos",usuario.getApellidos());
        info.put("nombres",usuario.getNombres());
        info.put("email",usuario.getEmail());
        info.put("bio",usuario.getBio());
        ((DefaultOAuth2AccessToken)accessToken).setAdditionalInformation(info);
        return accessToken;
    }
}