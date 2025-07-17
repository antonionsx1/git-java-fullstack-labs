package org.iadb.lmw.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.security.sasl.AuthenticationException;

import org.iadb.lmw.dao.AdminDao;
import org.iadb.lmw.dto.DeleteAdminResponseDto;
import org.iadb.lmw.dto.GenerateRequestDto;
import org.iadb.lmw.dto.GenerateResponseDto;
import org.iadb.lmw.dto.JwtPayloadDto;
import org.iadb.lmw.dto.LoginRequestDto;
import org.iadb.lmw.dto.LoginResponseDto;
import org.iadb.lmw.dto.RecoverPassResponseDto;
import org.iadb.lmw.dto.RecoverPasswordDto;
import org.iadb.lmw.dto.RegisterRequestDto;
import org.iadb.lmw.dto.RegisterResponseDto;
import org.iadb.lmw.dto.ViewAdminResponseDto;
import org.iadb.lmw.model.Admin;
import org.iadb.lmw.service.AdminService;
import org.iadb.lmw.utils.EmailUtil;
import org.iadb.lmw.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private EmailUtil emailUtil;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    private static final String ENTER = "<br></br>";

    @Value("${security.gen-password-link}")
    private String link;

    @Value("${security.contact-email}")
    private String email;


    @Override
    public LoginResponseDto login(LoginRequestDto request) throws AuthenticationException {
        log.info("Inicio de sesión");

        List<Admin> admins = adminDao.findByEmail(request.getEmail());

        if (admins.isEmpty()) {
            throw new AuthenticationException("Usuario no registrado");
        }

        String hash = bCryptPasswordEncoder.encode(request.getEmail());

        if (bCryptPasswordEncoder.matches(request.getPassword(), admins.get(0).getPassword())) {

            admins.get(0).setTemporal(hash);
            adminDao.save(admins.get(0));

            LoginResponseDto response = new LoginResponseDto();
            BeanUtils.copyProperties(admins.get(0), response);
            response.setToken(jwtUtil.generateJwt(new JwtPayloadDto(admins.get(0).getId())));
            return response;
        } else {
            throw new AuthenticationException("Contraseña incorrecta");
        }
    }

    @Override
    public RegisterResponseDto register(RegisterRequestDto request, String lang) throws IllegalArgumentException, AuthenticationException {
        log.info("Registrando administrador");

        String hash = bCryptPasswordEncoder.encode(request.getEmail()).replaceAll("/", "");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 2);
        Date expiration = cal.getTime();

        if (adminDao.findByEmail(request.getEmail()).size() != 0) {
            throw new AuthenticationException("Correo ya registrado");
        }

        String body = "";

        if (lang.equals("es")) {
            body = "Bienvenido a Latin Macro Watch" + ENTER
                    + "Se ha dado de alta como Usuario Administrador de LMW." + ENTER
                    + "Su nombre de usuario es " + request.getEmail() + ENTER
                    + "En esta liga podrá establecer su contraseña " + link + lang + "/password/new/" + hash + ENTER
                    + "Equipo LMW";
        } else if (lang.equals("en")) {
            body = "Welcome to Latin Macro Watch" + ENTER
                    + "You have been registered as an LMW Administrator User." + ENTER
                    + "Your username is " + request.getEmail() + ENTER
                    + "In this link you can set your password " + link + lang + "/password/new/" + ENTER
                    + "LMW team";
        } else {
            throw new IllegalArgumentException("Idioma no soportado");
        }


        if (emailUtil.sendEmailTool(body, request.getEmail(), "Nuevo usuario")) {
            Admin admin = new Admin();
            BeanUtils.copyProperties(request, admin);
            admin.setRegDate(new Date());
            admin.setStatus(true);
            admin.setTemporal(hash);
            admin.setTempExp(expiration);
            if (request.getSu()) {
                admin.setProfile("su");
            } else {
                admin.setProfile("admin");
            }
            admin = adminDao.save(admin);

            return new RegisterResponseDto("Registro exitoso con ID: " + admin.getId().toString());
        } else {
            throw new IllegalArgumentException("Correo no válido");
        }
    }

    @Override
    public GenerateResponseDto generatePassword(GenerateRequestDto request) {
        log.info("Generar contraseña");
        List<Admin> admins = adminDao.findByTemporal(request.getTemporal());

        if (admins.isEmpty()) {
            throw new IllegalArgumentException("Código inválido");
        }

        if (admins.size() > 1) {
            throw new IllegalArgumentException("Conflicto con : " + Integer.toString(admins.size()) + " usuarios");
        }

        if (admins.get(0).getTempExp().compareTo(new Date()) < 0) {
            throw new IllegalArgumentException("Código caducado");
        }

        admins.get(0).setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        admins.get(0).setTemporal(null);
        adminDao.save(admins.get(0));

        GenerateResponseDto response = new GenerateResponseDto();
        BeanUtils.copyProperties(admins.get(0), response);
        response.setToken(jwtUtil.generateJwt(new JwtPayloadDto(admins.get(0).getId())));
        return response;
    }

    @Override
    public RecoverPassResponseDto recoverPassword(RecoverPasswordDto request, String lang) throws IllegalArgumentException, AuthenticationException {
        log.info("Recuperar contraseña");
        List<Admin> admins = adminDao.findByEmail(request.getEmail());

        if (admins.isEmpty()) {
            throw new AuthenticationException("Usuario no registrado");
        }

        String hash = bCryptPasswordEncoder.encode(request.getEmail()).replaceAll("/", "");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 2);
        Date expiration = cal.getTime();

        String body = "";

        if (lang.equals("es")) {
            body = "Estimado," + ENTER
                    + "Para restaurar su contraseña haga clic en este vínculo: " + link + lang + "/password/new/" + ENTER
                    + "Para efecto de seguridad, este vínculo expirará en x minutos, por favor restablezca su contraseña antes del tiempo señalado." + ENTER
                    + "Si tiene alguna duda, póngase en contacto con nosotros por correo electrónico a " + email + ENTER
                    + "Equipo LMW";
        } else if (lang.equals("en")) {
            body = "Dear Administrator," + ENTER
                    + "To reset your password click on this link: " + link + lang + "/password/new/" + ENTER
                    + "For security purposes, this link will expire in x minutes, please reset your password before the specified time." + ENTER
                    + "If you have any questions, please e-mail us at: " + email + ENTER
                    + "LMW team";
        } else {
            throw new IllegalArgumentException("Idioma no soportado");
        }


        if (emailUtil.sendEmailTool(body, request.getEmail(), "Recuperación de contraseña")) {

            admins.get(0).setTemporal(hash);
            admins.get(0).setTempExp(expiration);
            adminDao.save(admins.get(0));

            return new RecoverPassResponseDto("Solicitud enviada");
        } else {
            throw new IllegalArgumentException("Correo no válido");
        }
    }

    @Override
    public List<ViewAdminResponseDto> viewAll() throws AuthenticationException {

        List<Admin> admins = adminDao.findAll();
        List<ViewAdminResponseDto> dtos = new ArrayList<>();

        for (Admin admin : admins) {
            ViewAdminResponseDto dto = new ViewAdminResponseDto();
            BeanUtils.copyProperties(admin, dto);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public DeleteAdminResponseDto delete(Integer id) {

        Optional<Admin> admin = adminDao.findById(id);

        if (admin.isPresent()) {
            adminDao.delete(admin.get());
            return new DeleteAdminResponseDto("Usuario eliminado con éxito");
        } else {
            return new DeleteAdminResponseDto("Usuario no encontrado");
        }
    }

    @Override
    public Admin findById(Integer id) {
        Optional<Admin> admin = adminDao.findById(id);

        return admin.isPresent() ? admin.get() : new Admin();
    }


}
