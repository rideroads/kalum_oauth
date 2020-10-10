package edu.kalum.oauth.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kalum.oauth.core.auth.ResourceServerConfig;
import edu.kalum.oauth.core.models.entity.Role;
import edu.kalum.oauth.core.models.entity.Usuario;
import edu.kalum.oauth.core.models.entity.dao.services.IRoleService;
import edu.kalum.oauth.core.models.entity.dao.services.IUploadFileService;
import edu.kalum.oauth.core.models.entity.dao.services.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/kalum-oauth/v1")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private IUploadFileService uploadFileService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TokenStore tokenStore;

    @GetMapping("/usuarios")
    public ResponseEntity<?> listar(){
        Map<String,Object> response = new HashMap<>();
        logger.debug("Iniciando proceso de consulta de usuarios");
        try{
            List<Usuario> usuarios = usuarioService.findAll();
            if(usuarios.isEmpty()){
                logger.warn("No existen registros");
                response.put("Mensaje","No existen registros");
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.NO_CONTENT);
            }
            logger.debug("Finalizando proceso de consulta de usuarios");
            return new ResponseEntity<List<Usuario>>(usuarios,HttpStatus.OK);
        }catch (CannotCreateTransactionException e) {
            logger.error("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al momento de conectarse a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch (DataAccessException e){
            logger.error("Error al realizar la consulta a la base de datos");
            response.put("Mensaje","Error al realizar la consulta a la base de datos");
            response.put("Error",e.getMessage().concat(": ".concat(e.getMostSpecificCause().getMessage())));
            return new ResponseEntity<Map<String,Object>>(response, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @GetMapping("/usuarios/whoami")
    public ResponseEntity<?> whoami(HttpServletRequest request){
        logger.debug("Iniciando proceso de consulta de usuario por token");
        Map<String,Object> response = new HashMap<>();
        try{
            String token = request.getHeader("Authorization").substring(7);
            logger.debug("Iniciando proceso de desencriptación de usuario");
            String username = tokenStore.readAuthentication(token).getUserAuthentication().getName();
            logger.debug("Obteniendo información del usuario por medio del username");
            Usuario usuario = usuarioService.findByUsername(username);
            return new ResponseEntity<Usuario>(usuario,HttpStatus.OK);
        }catch (CannotCreateTransactionException e) {
            logger.error("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al momento de conectarse a la base de datos");
            response.put("Error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch(DataAccessException e){
            response.put("Mensaje","Error al realizar la consulta a la base de datos");
            response.put("Error",e.getMessage().concat(": ".concat(e.getMostSpecificCause().getMessage())));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch (Exception e){
            logger.error("Error al momento de obtener la información del usuario");
            response.put("Mensaje","Error al momento de obtener la información del usuario");
            response.put("Error",e.getMessage().concat(": ").concat(e.getCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> create(@Valid @RequestBody Usuario value, BindingResult result){
        Usuario usuario = null;
        Map<String,Object> response = new HashMap<>();
        if(result.hasErrors()){
            List<String> errores = result.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            response.put("errores",response);
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
        }
        try{
            List<Role> roles = new ArrayList<Role>();
            Role role = this.roleService.findById(2L);
            roles.add(role);
            value.setEnabled(true);
            value.setPassword(passwordEncoder.encode(value.getPassword()));
            value.setRoles(roles);
            usuario = usuarioService.save(value);
        }catch (CannotCreateTransactionException e) {
            logger.error("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al momento de conectarse a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch(DataAccessException e){
            logger.error("Error al realizar el insert a la base de datos");
            response.put("Mensaje","Error al realizar el insert a la base de datos");
            response.put("Error",e.getMessage().concat(": ".concat(e.getMostSpecificCause().getMessage())));
            return new ResponseEntity<Map<String,Object>>(response, HttpStatus.SERVICE_UNAVAILABLE);
        }
        logger.debug("Finalizando preceso de insert del usuario a la base de datos");
        response.put("Mensaje","El usuario ha sido creado con éxito");
        response.put("Usuario",usuario);
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.CREATED);
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Usuario value, BindingResult result, @PathVariable Long id){
        Map<String, Object> response = new HashMap<>();
        Usuario update = null;
        if(result.hasErrors()){
            List<String> errores = result.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            response.put("errores",errores);
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
        }
        try {
            update = usuarioService.findById(id);
            if (update != null) {
                update.setEnabled(value.isEnabled());
                update.setPassword(passwordEncoder.encode(value.getPassword()));
                update.setUsername(value.getUsername());
                update.setApellidos(value.getApellidos());
                update.setNombres(value.getNombres());
                update.setBio(value.getBio());
                update.setEmail(value.getEmail());
                usuarioService.save(update);
            } else {
                logger.debug("No existe el usuario con el id ".concat(id.toString()));
                response.put("Mensaje", "El usuario con el id ".concat(id.toString()).concat(" no existe"));
                return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
            }
        }catch(CannotCreateTransactionException e) {
            logger.error("Error al intentar conectarse a la base de datos");
            response.put("Mensaje","Error al intentar conectarse a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch(DataAccessException e){
            logger.error("Error al intentar modificar el registro en la base de datos");
            response.put("Mensaje","Error al intentar modificar el registro en la base de datos");
            response.put("Error",e.getMessage().concat(" : ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }
        response.put("Mensaje","El registro se actualizo correctamente");
        response.put("usuario",update);
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        Map<String,Object> response = new HashMap<>();
        logger.debug("Iniciando proceso de eliminación del registro del usuario");
        try{
            Usuario delete = this.usuarioService.findById(id);
            uploadFileService.eliminar(delete.getFoto());
            if(delete != null){
                this.usuarioService.deleteById(id);
            }else{
                logger.warn("El usuario con el id ".concat(id.toString()).concat(" no existe"));
                response.put("Mensaje","El usuario con el ".concat(id.toString()).concat(" no existe"));
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.NOT_FOUND);
            }
        }catch (CannotCreateTransactionException e) {
            logger.error("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al momento de conectarse a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch(DataAccessException e){
            logger.error("Error al intentar eliminar el registro de la base de datos");
            response.put("Mensaje","Error al intentar eliminar el registro de la base de datos");
            response.put("Error",e.getMessage().concat(" : ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }
        response.put("Mensaje","El registro fue eliminado con éxito");
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
    }

    @PostMapping("/usuarios/uploads")
    public ResponseEntity<?> upload(@RequestParam("archivo")MultipartFile archivo, @RequestParam("id") Long id){
        Map<String,Object> response = new HashMap<String, Object>();
        Usuario usuario = null;
        if(!archivo.isEmpty()){
            String nombreArchivo = null;
            try{
                usuario = this.usuarioService.findById(id);
                nombreArchivo = uploadFileService.copiar(archivo);
                uploadFileService.eliminar(usuario.getFoto());
                usuario.setFoto(nombreArchivo);
                this.usuarioService.save(usuario);
            }catch (CannotCreateTransactionException e) {
                logger.error("Error al intentar conectarse a la base de datos");
                response.put("Mensaje","Error al intentar conectarse a la base de datos");
                response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
            }catch (DataAccessException e) {
                logger.error("Error al realizar la consulta a la base de datos");
                response.put("Mensaje","Error al realizar la consulta a la base de datos");
                response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
            }catch (IOException e){
                response.put("Mensaje","Error al subir la imagen al servidor");
                response.put("Error",e.getMessage().concat(" : ").concat(e.getCause().getMessage()));
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
            }
            response.put("Mensaje","Se ha subido correctamente la imagen: ".concat(nombreArchivo));
            response.put("usuario",usuario);
        }
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.CREATED);
    }

    @GetMapping("/usuarios/uploads/img/{nombreFoto:.+}")
    public ResponseEntity<Resource> showPicture(@PathVariable String nombreFoto){
        Resource resource = null;
        try {
            resource = uploadFileService.cargar(nombreFoto);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"".concat(resource.getFilename()).concat("\""));
        return new ResponseEntity<Resource>(resource,headers,HttpStatus.OK);
    }

    @PostMapping("/usuarios/{id-user}/role/{id-role}")
    public ResponseEntity<?> addRolToUser(@PathVariable("id-user") Long idUser, @PathVariable("id-role") Long idRole){
        Map<String,Object> response = new HashMap<>();
        Usuario usuario = null;
        logger.debug("Iniciando proceso de asignación de rol a usuario");
        try {
            usuario = usuarioService.findById(idUser);
            if (usuario != null) {
                Role role = roleService.findById(idRole);
                if(role != null){
                    usuario.getRoles().add(role);
                    usuarioService.save(usuario);
                }else{
                    logger.warn("No existe el rol con el id ".concat(idRole.toString()));
                    response.put("Mensaje","No existe el rol con el id ".concat(idRole.toString()));
                    return new ResponseEntity<Map<String,Object>>(response,HttpStatus.NOT_FOUND);
                }
            } else {
                logger.warn("No existe el usuario con el id ".concat(idUser.toString()));
                response.put("Mensaje", "No existe el usuario con el id ".concat(idUser.toString()));
                return  new ResponseEntity<Map<String,Object>>(response,HttpStatus.NOT_FOUND);
            }
        }catch(CannotCreateTransactionException e){
            logger.error("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al momento de conectarse a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch (DataAccessException e){
            logger.error("Error al intentar ejecutar la consulta a la base de datos");
            response.put("Mensaje","Error al intentar ejecutar la consulta a la base de datos");
            response.put("Error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }
        response.put("Mensaje","El rol ha sido asignado correctamente");
        response.put("usuario",usuario);
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.CREATED);
    }

    @DeleteMapping("/usuarios/{id-user}/role/{id-role}")
    public ResponseEntity<?> removeRolToUser(@PathVariable("id-user") Long idUser, @PathVariable("id-role") Long idRole){
        Map<String,Object> response = new HashMap<>();
        Usuario usuario = null;
        logger.debug("Iniciando proceso de eliminación de role");
        try{
            usuario = usuarioService.findById(idUser);
            if(usuario != null){
                Role role = roleService.findById(idRole);
                if(role != null){
                    usuario.getRoles().remove(role);
                }else{
                    logger.warn("No existe el role con el id ".concat(idRole.toString()));
                    response.put("Mensaje","No existe el role con el id ".concat(idRole.toString()));
                    return new ResponseEntity<Map<String,Object>>(response,HttpStatus.NOT_FOUND);
                }
            }else{
                logger.warn("No existe el usuario con el id ".concat(idUser.toString()));
                response.put("Mensaje","No existe el usuario con el id ".concat(idUser.toString()));
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.NOT_FOUND);
            }
        }catch (CannotCreateTransactionException e){
            logger.error("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al momento de conectarse a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch(DataAccessException e){
            logger.error("Error al momento de realizar la consulta a la base de datos");
            response.put("Mensaje","Error al momento de realizar la consulta a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }
        response.put("Mensaje","El rol ha eliminado correctamente");
        response.put("usuario",usuario);
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
    }
}
