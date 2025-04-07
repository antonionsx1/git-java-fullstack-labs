package org.iadb.lmw.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.security.sasl.AuthenticationException;

import org.iadb.lmw.dto.DatasetFileResponseDTO;
import org.iadb.lmw.dto.DatesResponseDto;
import org.iadb.lmw.dto.JwtPayloadDto;
import org.iadb.lmw.dto.LangRequestDto;
import org.iadb.lmw.dto.QueryRequestDTO;
import org.iadb.lmw.dto.ReplaceDatasetRequestDto;
import org.iadb.lmw.dto.SingleResponse;
import org.iadb.lmw.dto.SummaryDTO;
import org.iadb.lmw.model.Backup;
import org.iadb.lmw.service.DataManagementService;
import org.iadb.lmw.status.StatusCode;
import org.iadb.lmw.utils.AuthUtil;
import org.iadb.lmw.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

/**
 *
 */
@RestController
@RequestMapping("/datasets")
@CrossOrigin(origins = "${security.cors.origin}", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
		RequestMethod.DELETE })
public class DatabaseApi {

	@Autowired
	DataManagementService dataManagementService;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private AuthUtil authUtil;
	
	@Value("${data.dataset.path}") 
	private String path;
	@Value("${data.dataset.download-path}") 
	private String downloadPath;
	@Value("${data.dataset.prefix}") 
	private String prefix;
	@Value("${data.dataset.ext}") 
	private String ext;
	@Value("${data.sql.lmw-table-name}") 
	private String tableName;
	@Value("${data.sql.schema}") 
	private String schema;
	
	private static final String MEDIA_TYPE = "application/csv"; 

	@ExceptionHandler({ AuthenticationException.class })
	public HttpEntity<List<String>> handleAuthenticationExpception(AuthenticationException ex) {
		List<String> errors = new ArrayList<>();
		errors.add(ex.getMessage());
		return new ResponseEntity<>(errors, HttpStatus.UNAUTHORIZED);
	}
	
	@PostMapping(value = "/verify",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public SingleResponse<SummaryDTO> verify(@RequestHeader("authorization") String token,
			@RequestParam("file") MultipartFile file
			) throws UnsupportedEncodingException, IOException {
		authUtil.getAuthorization(jwtUtil.validateToken(token), false, false);
		return dataManagementService.verify(file, path, prefix, ext);
	}

	@PostMapping("/replace")
	public SingleResponse<SummaryDTO> replaceDataset(
			@RequestHeader("authorization") String token,
			@Valid @RequestBody ReplaceDatasetRequestDto request
			) throws FileNotFoundException, IOException {
		
		JwtPayloadDto payload = jwtUtil.validateToken(token);
		authUtil.getAuthorization(payload, false, true);
		return dataManagementService.replaceDataset(path, request, payload.getId(), ext, tableName, schema, prefix);
	}
	
	@PostMapping("/update")
	public SingleResponse<SummaryDTO> updateDataset(
			@RequestHeader("authorization") String token,
			@Valid @RequestBody ReplaceDatasetRequestDto request
			) throws FileNotFoundException, IOException {
		
		JwtPayloadDto payload = jwtUtil.validateToken(token);
		authUtil.getAuthorization(payload, false, true);
		return dataManagementService.updateDataset(path, request, payload.getId(), ext, tableName, schema, prefix);
	}
	
	@PostMapping("/headers")
	public SingleResponse<SummaryDTO> getHeaders(@RequestBody LangRequestDto query) {
		return dataManagementService.getStoredHeaders(query);
	}

	@PostMapping("/download")
	public ResponseEntity<Resource> downloadCSV(
			@RequestBody QueryRequestDTO query) {
		SingleResponse<DatasetFileResponseDTO> response = dataManagementService.downloadCSV(query, tableName, downloadPath, prefix, ext);
			
		if(!response.getCode().equals(StatusCode.OK.getCode()))
			return ResponseEntity.badRequest()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = Error")
					.contentType(MediaType.parseMediaType(MEDIA_TYPE))
					.body(null);
		
		DatasetFileResponseDTO dataset = response.getData();
		InputStreamResource file = dataset.getFile();
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = " + dataset.getName())
				.contentType(MediaType.parseMediaType(MEDIA_TYPE))
				.body(file);
	}
	
	@PostMapping("/visualize")
	public SingleResponse<SummaryDTO> vizualizeDataset(
			@RequestBody QueryRequestDTO query) {
		
		return dataManagementService.getDataForVisualization(query, tableName);
	}
	
	@GetMapping("/backups/list")
	public SingleResponse<List<Backup>> listBackups(@RequestHeader("authorization") String token) throws AuthenticationException{
		authUtil.getAuthorization(jwtUtil.validateToken(token), true, false);
		
		List<Backup> result = dataManagementService.listBackups();
		Collections.reverse(result);
		
		return new SingleResponse<List<Backup>>(StatusCode.OK.getCode(), result);
	}

	@GetMapping("/backups/last")
	public SingleResponse<LocalDateTime> lastBackup(){
		
		List<Backup> result = dataManagementService.listBackups();
		Collections.reverse(result);
		
		return new SingleResponse<LocalDateTime>(StatusCode.OK.getCode(), result.get(0).getTimeStamp());
	}
	
	@PostMapping("/backups/restore")
	public SingleResponse<SummaryDTO> restoreDatasetFromBackup(
			@RequestHeader("authorization") String token,
			@RequestParam("id") long id) throws FileNotFoundException, IOException {
		
		JwtPayloadDto payload = jwtUtil.validateToken(token);
		authUtil.getAuthorization(payload, false, true);
		return dataManagementService.restoreFromBackupById(id, payload.getId(), ext, tableName, schema, prefix);
	}
	
	@GetMapping("/backups/download/{id}")
	public ResponseEntity<Resource> downloadBackup(@RequestHeader("authorization") String token, @PathVariable Long id) {
		SingleResponse<DatasetFileResponseDTO> response = dataManagementService.downloadBackup(id);
			
		if(!response.getCode().equals(StatusCode.OK.getCode()))
			return ResponseEntity.badRequest()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = Error")
					.contentType(MediaType.parseMediaType(MEDIA_TYPE))
					.body(null);
		
		DatasetFileResponseDTO dataset = response.getData();
		InputStreamResource file = dataset.getFile();
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = " + dataset.getName() + ext)
				.header("filename",  dataset.getName() + ext)
				.contentType(MediaType.parseMediaType(MEDIA_TYPE))
				.body(file);
	}
	
	@GetMapping("/dates")
	public SingleResponse<DatesResponseDto> getStoredDates(){
		return dataManagementService.getStoredDates(tableName);
	}
}
