package org.iadb.lmw.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.security.sasl.AuthenticationException;

import org.iadb.lmw.dao.AdminDao;
import org.iadb.lmw.dto.JwtPayloadDto;
import org.iadb.lmw.model.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);

    @Autowired
    private AdminDao adminDao;

    public void getAuthorization(JwtPayloadDto payload, Boolean read, Boolean write) throws AuthenticationException {

        log.info("Verificando permisos");

        Optional<Admin> admin = adminDao.findById(payload.getId());
        if (admin.isEmpty()) {
            throw new AuthenticationException("Usuario inválido: En token no contiene las credenciales mínimas");
        } else if (admin.get().getProfile().equals("super")) {
            log.info("Acceso autorizado como super administrador");
        } else if (admin.get().getProfile().equals("admin")) {
            String permisionsStr = admin.get().getAuth();
            if (permisionsStr != null && permisionsStr.length() != 0) {
                if (permisionsStr.length() == 1) {
                    if (Boolean.TRUE.equals(read) && !permisionsStr.equals("R")) {
                        throw new AuthenticationException("Usuario inválido: Permiso de lectura denegado");
                    }
                    if (Boolean.TRUE.equals(write) && !permisionsStr.equals("W")) {
                        throw new AuthenticationException("Usuario inválido: Permiso de escritura denegado");
                    }
                } else {
                    List<String> permisions = Arrays.asList(permisionsStr.split(","));
                    if (Boolean.TRUE.equals(read) && !permisions.contains("R")) {
                        throw new AuthenticationException("Usuario inválido: Permiso de lectura denegado");
                    }
                    if (Boolean.TRUE.equals(write) && !permisions.contains("W")) {
                        throw new AuthenticationException("Usuario inválido: Permiso de escritura denegado");
                    }
                }
            } else {
                throw new AuthenticationException("Usuario inválido: Permisos insuficientes");
            }
            log.info("Acceso autorizado como administrador");
        } else {
            throw new AuthenticationException("Usuario inválido: Perfil no autorizado");
        }
    }
}
