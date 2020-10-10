package edu.kalum.oauth.core.models.entity.dao.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class UploadFileServiceImpl implements IUploadFileService {
    private final Logger log = LoggerFactory.getLogger(UploadFileServiceImpl.class);
    @Override
    public Resource cargar(String nombreFoto) throws MalformedURLException {
        Path rutaArchivo = getPath(nombreFoto);
        log.info(rutaArchivo.toString());
        Resource resource = null;
        resource = new UrlResource(rutaArchivo.toUri());
        if(!resource.exists() && !resource.isReadable()){
            rutaArchivo = getPath("default.png");
            resource = new UrlResource(rutaArchivo.toUri());
            log.info("Error, no se puedo cargar la imagen ".concat(nombreFoto));
        }
        return resource;
    }

    @Override
    public String copiar(MultipartFile archivo) throws IOException {
        String nombre = UUID.randomUUID().toString().concat("_").concat(archivo.getOriginalFilename().replace(" ",""));
        Path rutaArchivo = getPath(nombre);
        log.info(rutaArchivo.toString());
        Files.copy(archivo.getInputStream(),rutaArchivo);
        return nombre;
    }

    @Override
    public boolean eliminar(String nombreFoto) {
        boolean resultado = false;
        if(nombreFoto != null && nombreFoto.length() > 0){
            Path rutaFotoAnterior = getPath(nombreFoto);
            File archivoFotoAnterior = rutaFotoAnterior.toFile();
            if(archivoFotoAnterior.exists() && archivoFotoAnterior.canRead()){
                archivoFotoAnterior.delete();
                resultado = true;
            }
        }
        return resultado;
    }

    @Override
    public Path getPath(String nombreFoto) {
        return Paths.get("uploads").resolve(nombreFoto).toAbsolutePath();
    }
}
