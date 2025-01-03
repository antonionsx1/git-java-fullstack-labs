package org.iadb.lmw.api;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.security.sasl.AuthenticationException;

import org.iadb.lmw.cache.TreeCache;
import org.iadb.lmw.dto.CreateCountryRequestDto;
import org.iadb.lmw.dto.CreateCountryResponseDto;
import org.iadb.lmw.dto.TreeDto;
import org.iadb.lmw.dto.UpdateCountryRequestDto;
import org.iadb.lmw.dto.UpdateCountryResponseDto;
import org.iadb.lmw.dto.ViewCountryResponseDto;
import org.iadb.lmw.dto.ViewTreeResponseDTO;
import org.iadb.lmw.service.CatalogService;
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
@RequestMapping("/catalog")
@CrossOrigin(origins = "${security.cors.origin}", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
public class CatalogApi {

	private static final Logger log = LoggerFactory.getLogger(CatalogApi.class);

	@Autowired
	private CatalogService catalogService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AuthUtil authUtil;
	
	@Autowired
	private TreeCache treeCache;

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

	@PostMapping(path = "/create/country", produces = MediaType.APPLICATION_JSON_VALUE)
	public CreateCountryResponseDto createCountry(@RequestHeader("authorization") String token,
			@Valid @RequestBody CreateCountryRequestDto request)
			throws IllegalArgumentException, AuthenticationException {
		log.info("POST:/catalog/create/country");
		authUtil.getAuthorization(jwtUtil.validateToken(token), false, true);
		return catalogService.createCountry(request);
	}

	@PostMapping(path = "/update/country", produces = MediaType.APPLICATION_JSON_VALUE)
	public UpdateCountryResponseDto createCountry(@RequestHeader("authorization") String token,
			@Valid @RequestBody UpdateCountryRequestDto request)
			throws IllegalArgumentException, AuthenticationException, FileNotFoundException {
		log.info("POST:/catalog/create/country");
		authUtil.getAuthorization(jwtUtil.validateToken(token), false, true);
		return catalogService.updateCountry(request);
	}

	@GetMapping(path = "/view/all/countries/{lang}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ViewCountryResponseDto> viewAll(@PathVariable String lang)
			throws FileNotFoundException {
		log.info("POST:/catalog/view/all/countries/{lang}");
		return catalogService.viewAllCountries(lang);
	}

	@GetMapping(path = "/find/country/{lang}/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ViewCountryResponseDto findCountry(@PathVariable String lang, @PathVariable String name)
			throws FileNotFoundException {
		log.info("POST:/catalog/find/country/{lang}/{name}");
		return catalogService.findCountry(name, lang);
	}

	@GetMapping(path = "/view/tree/{lang}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ViewTreeResponseDTO> viewTree(@PathVariable String lang)
			throws FileNotFoundException {
		log.info("POST:/catalog/view/all/countries/{lang}");
		
		Optional<TreeDto> tree = treeCache.findById(lang);
		
		if(tree.isEmpty()) {
			TreeDto cache = new TreeDto();
			cache.setId(lang);
			cache.setTree(catalogService.viewTree(lang));
			treeCache.save(cache);
			return cache.getTree();
		} else {
			return tree.get().getTree();
		}				 	
	}
}
