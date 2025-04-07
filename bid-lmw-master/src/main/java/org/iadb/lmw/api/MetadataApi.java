package org.iadb.lmw.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.sasl.AuthenticationException;

import org.iadb.lmw.dto.UploadMetadataResponseDto;
import org.iadb.lmw.service.MetadataService;
import org.iadb.lmw.utils.AuthUtil;
import org.iadb.lmw.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
@RestController
@RequestMapping("/metadata")
@CrossOrigin(origins = "${security.cors.origin}", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
		RequestMethod.DELETE })
public class MetadataApi {

	private static final Logger log = LoggerFactory.getLogger(MetadataApi.class);

	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private AuthUtil authUtil;
	
	@Autowired
	private MetadataService metadataService;
	
	@Value("${metadata.filename}")
	private String fileNane;
	
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
	
	@PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadMetadataResponseDto upload(@RequestHeader("authorization") String token, @RequestParam("file") MultipartFile file)
			throws IOException {
		log.info("POST:/metadata/upload");
		authUtil.getAuthorization(jwtUtil.validateToken(token), false, true);
		return new UploadMetadataResponseDto("Archivo guardado correctamente: " + metadataService.storeFile(file));
	}
	
	@GetMapping(path = "/download", produces = MediaType.APPLICATION_PDF_VALUE)
	@ResponseBody
	public ResponseEntity<Resource> download() throws IOException{

		Resource file = metadataService.loadFileAsResource(fileNane);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	
	}
}
