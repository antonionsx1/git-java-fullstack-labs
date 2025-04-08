package org.iadb.lmw.api;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.security.sasl.AuthenticationException;

import org.iadb.lmw.dto.DeleteAdminResponseDto;
import org.iadb.lmw.dto.GenerateRequestDto;
import org.iadb.lmw.dto.GenerateResponseDto;
import org.iadb.lmw.dto.LoginRequestDto;
import org.iadb.lmw.dto.LoginResponseDto;
import org.iadb.lmw.dto.RecoverPassResponseDto;
import org.iadb.lmw.dto.RecoverPasswordDto;
import org.iadb.lmw.dto.RegisterRequestDto;
import org.iadb.lmw.dto.RegisterResponseDto;
import org.iadb.lmw.dto.ViewAdminResponseDto;
import org.iadb.lmw.service.AdminService;
import org.iadb.lmw.utils.AuthUtil;
import org.iadb.lmw.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "${security.cors.origin}", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
		RequestMethod.DELETE })
public class AdminApi {

	private static final Logger log = LoggerFactory.getLogger(AdminApi.class);

	@Autowired
	private AdminService adminService;

	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private AuthUtil authUtil;
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public HttpEntity<List<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		List<String> errors = new ArrayList<>();
		ex.getBindingResult().getAllErrors().forEach(error ->
			errors.add(error.getDefaultMessage())
		);
		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ AuthenticationException.class })
	public HttpEntity<List<String>> handleAuthenticationExpception(AuthenticationException ex) {
		List<String> errors = new ArrayList<>();
		errors.add(ex.getMessage());
		return new ResponseEntity<>(errors, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler({ FileNotFoundException.class })
	public HttpEntity<List<String>> handleFileNotFoundException(FileNotFoundException ex) {
		List<String> errors = new ArrayList<>();
		errors.add(ex.getMessage());
		return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler({ IllegalArgumentException.class })
	public HttpEntity<List<String>> handleIOExpception(IllegalArgumentException ex) {
		List<String> errors = new ArrayList<>();
		errors.add(ex.getMessage());
		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}
	
	@PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request)
			throws AuthenticationException {
		log.info("POST:/admin/login");
		return adminService.login(request);
	}
	
	@PostMapping(path = "/register/new/{lang}", produces = MediaType.APPLICATION_JSON_VALUE)
	public RegisterResponseDto registerAdmin(@RequestHeader("authorization") String token, @PathVariable String lang, @Valid @RequestBody RegisterRequestDto request) 
			throws IllegalArgumentException, AuthenticationException {
		log.info("POST:/admin/register");
		authUtil.getAuthorization(jwtUtil.validateToken(token), true, true);
		return adminService.register(request, lang);
	}
	
	@PostMapping(path = "/generate/password", produces = MediaType.APPLICATION_JSON_VALUE)
	public GenerateResponseDto generatePassword(@Valid @RequestBody GenerateRequestDto request) {
		log.info("POST:/admin/generate/password");
		return adminService.generatePassword(request);
	}
	
	@PostMapping(path = "/recover/password/{lang}", produces = MediaType.APPLICATION_JSON_VALUE)
	public RecoverPassResponseDto recoverPassword(@PathVariable String lang, @Valid @RequestBody RecoverPasswordDto request) 
			throws IllegalArgumentException, AuthenticationException {
		log.info("POST:/admin/recover/password");
		return adminService.recoverPassword(request,lang);
	}
	
	@DeleteMapping(path = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public DeleteAdminResponseDto deleteAdmin(@RequestHeader("authorization") String token,@PathVariable Integer id) 
			throws IllegalArgumentException, AuthenticationException {
		log.info("POST:/admin/recover/password");
		authUtil.getAuthorization(jwtUtil.validateToken(token), true, true);
		return adminService.delete(id);
	}
	
	@GetMapping(path = "/view/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ViewAdminResponseDto> viewAll(@RequestHeader("authorization") String token) throws AuthenticationException {
		log.info("POST:/admin/view/all");
		authUtil.getAuthorization(jwtUtil.validateToken(token), true, true);
		return adminService.viewAll();
	}
}
