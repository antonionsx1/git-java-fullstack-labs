package org.iadb.lmw.api;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.security.sasl.AuthenticationException;

import org.iadb.lmw.dto.CommentsFileResponseDto;
import org.iadb.lmw.dto.CreateCommentRequestDto;
import org.iadb.lmw.dto.CreateCommentResponseDto;
import org.iadb.lmw.dto.DeleteCommentResponseDto;
import org.iadb.lmw.dto.DeleteCommentsRequestDto;
import org.iadb.lmw.dto.DownloadCommentsRequestDto;
import org.iadb.lmw.dto.ReadCommentResponseDto;
import org.iadb.lmw.dto.ReadCommentsRequestDto;
import org.iadb.lmw.dto.ViewCommentResponseDto;
import org.iadb.lmw.service.CommentService;
import org.iadb.lmw.utils.AuthUtil;
import org.iadb.lmw.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/comment")
@CrossOrigin(origins = "${security.cors.origin}", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
		RequestMethod.DELETE })
public class CommentApi {

	private static final Logger log = LoggerFactory.getLogger(CommentApi.class);
	
	@Autowired
	private CommentService commentService;
	
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
	
	@PostMapping(path = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
	public CreateCommentResponseDto registerAdmin(@Valid @RequestBody CreateCommentRequestDto request) 
			 {
		log.info("POST:/comment/create");
		return commentService.createComment(request);
	}
	
	@GetMapping(path = "/view/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ViewCommentResponseDto> viewAll(@RequestHeader("authorization") String token) throws AuthenticationException {
		log.info("POST:/comment/view/all");
		authUtil.getAuthorization(jwtUtil.validateToken(token), true, false);
		return commentService.viewAllComments();
	}
	
	@PostMapping(path = "/read", produces = MediaType.APPLICATION_JSON_VALUE)
	public ReadCommentResponseDto viewAll(@RequestHeader("authorization") String token, @Valid @RequestBody ReadCommentsRequestDto request) throws FileNotFoundException, AuthenticationException {
		log.info("POST:/comment/view/all");
		authUtil.getAuthorization(jwtUtil.validateToken(token), true, true);
		return commentService.readComment(request);
	}
	
	@PostMapping(path = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public DeleteCommentResponseDto deleteComment(@RequestHeader("authorization") String token,@Valid @RequestBody DeleteCommentsRequestDto request) 
			throws FileNotFoundException, AuthenticationException {
		log.info("POST:/comment/delete/id");
		authUtil.getAuthorization(jwtUtil.validateToken(token), false, true);
		return commentService.deleteComment(request);
	}
	
	@PostMapping(path = "/download")
	public ResponseEntity<Resource> downloadCommentsById(@RequestHeader("authorization") String token, @Valid @RequestBody DownloadCommentsRequestDto request) throws AuthenticationException {	
		authUtil.getAuthorization(jwtUtil.validateToken(token), true, false);
		
		CommentsFileResponseDto comments = commentService.downloadCommentsFile(request);
		InputStreamResource file = comments.getFile();
		
		String[] filenameAux = comments.getName().split("/");
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = " + filenameAux[filenameAux.length -1])
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(file);
	}
	
}
