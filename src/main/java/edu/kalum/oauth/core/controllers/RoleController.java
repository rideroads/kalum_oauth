package edu.kalum.oauth.core.controllers;

import edu.kalum.oauth.core.models.entity.Role;
import edu.kalum.oauth.core.models.entity.dao.services.IRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "/kalum-oauth/v1")
@CrossOrigin(origins = "*")
public class RoleController{
    private Logger logger = LoggerFactory.getLogger(RoleController.class);
    @Autowired
    private IRoleService roleService;

    @GetMapping("/roles")
    public ResponseEntity<?> listarRoles(){
        logger.debug("Iniciando proceso de consulta de roles");
        Map<Object,String> response = new HashMap<>();
        try {
            List<Role> roles = roleService.findAll();
            if(roles.isEmpty()){
                logger.warn("No existen registros");
                response.put("Mensaje","No existen registros");
               return new ResponseEntity<Map<Object,String>>(response, HttpStatus.NO_CONTENT);
            }
            logger.debug("Finalizando proceso de consulta de roles");
            return new ResponseEntity<List<Role>>(roles,HttpStatus.OK);
        }catch(CannotCreateTransactionException e) {
            logger.error("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al intentar conectarse a la base de datos");
            response.put("Error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<Object,String>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch (DataAccessException e){
            logger.error("Error al momento de realizar la consulta a la base de datos");
            response.put("Mensaje","Error al realizar la consulta a la base de datos");
            response.put("Error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<Object,String>>(response, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<?> show(@PathVariable Long id){
        Map<String,Object> response = new HashMap<>();
        logger.debug("Iniciando proceso de busqueda de registro");
        try {
            Role role = roleService.findById(id);
            if (role == null) {
                logger.warn("No existe el registro con el id ".concat(id.toString()));
                response.put("Mensaje", "No existe el registro con el id ".concat(id.toString()));
                return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<Role>(role, HttpStatus.OK);
            }
        }catch (CannotCreateTransactionException e){
            logger.debug("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al momento de conectarse a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return  new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch (DataAccessException e){
            logger.debug("Error al momento de ejecutar la consulta a la base de datos");
            response.put("Mensaje","Error al momento de ejecutar la consulta a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }
    }


    @PostMapping("/roles")
    public ResponseEntity<?> create(@Valid @RequestBody Role registro, BindingResult result){
        Role role = null;
        Map<String, Object> response = new HashMap<>();
        logger.debug("Iniciando proceso de validación de datos");
        if(result.hasErrors()){
            logger.debug("Existen errores de validación");
            List<String> errores =
                    result.getFieldErrors()
                            .stream()
                            .map(error -> error.getDefaultMessage())
                            .collect(Collectors.toList());
            response.put("Errores", errores);
            logger.warn("Errores de validación de datos de entrada");
            return new ResponseEntity<Map<String, Object>>(response,HttpStatus.BAD_REQUEST);
        }
        try{
            logger.debug("Iniciando proceso de almacenamiento de role");
            role = roleService.save(registro);
        }catch(CannotCreateTransactionException e){
            logger.error("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al conectarse a la base de datos");
            response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }
        response.put("Mensaje","El registro role fue creado con éxito");
        response.put("Role",role);
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.CREATED);
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Role registro, @PathVariable Long id , BindingResult result){
        Map<String,Object> response = new HashMap<>();
        logger.debug("Iniciando proceso de modificación de registro");
        if(result.hasErrors()){
            logger.warn("Error de validación al modificar el registro");
            List<String> errores = result.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            response.put("Errores",errores);
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
        }
        Role role = roleService.findById(id);
        if(role != null ){
            try{
                role.setNombre(registro.getNombre());
                roleService.save(role);
            }catch (CannotCreateTransactionException e){
                logger.error("Error al momento de conectarse a la base de datos");
                response.put("Mensaje","Error al momento de conectarse a la base de datos");
                response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
            }catch (DataAccessException e){
                logger.error("Error al momento de realizar la modificación a la base de datos");
                response.put("Mensaje","Error al momento de realizar la modificación a la base de datos");
                response.put("Error",e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
            }
            response.put("Mensaje","El registro se actualizo correctamente");
            response.put("role",role);
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
        }else {
            logger.warn("No existe ningún registro en la base de datos con el id ".concat(id.toString()));
            response.put("Mensaje","No existe ningún registro en la base de datos con el id ".concat(id.toString()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        Map<String,Object> response = new HashMap<>();
        logger.debug("Iniciando proceso de eliminación de registro");
        try{
            Role role = roleService.findById(id);
            if(role != null){
                roleService.delete(role);
            }else {
                logger.warn("El role con el registro ".concat(id.toString()).concat(" no existe"));
                response.put("Mensaje","El role con el id ".concat(id.toString()).concat(" no exite"));
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.NOT_FOUND);
            }
        }catch (CannotCreateTransactionException e){
            logger.error("Error al momento de conectarse a la base de datos");
            response.put("Mensaje","Error al momento de conectarse a la base de datos");
            response.put("Error",e.getMessage().concat(" :").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.SERVICE_UNAVAILABLE);
        }catch (DataAccessException e){
            logger.error("Error al momento de ejecuatar la consulta a la base de datos");
            response.put("Mensaje","Error al momento de ejecutar la consulta a la base de datos");
            response.put("Error",e.getMessage().concat(" :").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("Mensaje","El registro fue eliminado con éxito");
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
    }
}
