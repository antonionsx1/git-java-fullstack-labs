package org.iadb.lmw.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.iadb.lmw.dao.DatasetBackupDao;
import org.iadb.lmw.dao.HeaderDao;
import org.iadb.lmw.dto.DatasetFileResponseDTO;
import org.iadb.lmw.dto.DatesResponseDto;
import org.iadb.lmw.dto.FilterDTO;
import org.iadb.lmw.dto.LangRequestDto;
import org.iadb.lmw.dto.QueryRequestDTO;
import org.iadb.lmw.dto.ReplaceDatasetRequestDto;
import org.iadb.lmw.dto.SingleResponse;
import org.iadb.lmw.dto.SummaryDTO;
import org.iadb.lmw.dto.TableHeaderDto;
import org.iadb.lmw.model.Admin;
import org.iadb.lmw.model.Backup;
import org.iadb.lmw.model.Header;
import org.iadb.lmw.service.AdminService;
import org.iadb.lmw.service.DataManagementService;
import org.iadb.lmw.service.DynamicSQLQueryService;
import org.iadb.lmw.status.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DataManagementServiceImpl implements DataManagementService {
	
	private static final Logger log = LoggerFactory.getLogger(DataManagementServiceImpl.class);
	
	@Autowired
	DynamicSQLQueryService dynamicSQLQuery;
	@Autowired
	DatasetBackupDao datasetBackupDao;
	@Autowired
	HeaderDao headerDao;
	@Autowired
	AdminService adminService;
	
	@Value("${data.dataset.backup-path}") 
	private String backupPath;
	@Value("${data.sql.lmw-primary-key}") 
	private String lmwPK;
	@Value("${data.dataset.ext}") 
	private String ext;
	

	private static final String FORMAT = "yyMMddHHmmss"; 
	
	@Override
	public SingleResponse<SummaryDTO> verify(MultipartFile file, String path, String filePrefix, String fileExt) throws UnsupportedEncodingException, IOException {
		log.info("Verificación de datos");
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
		CSVParser csvParser = new CSVParser(
			fileReader,
			CSVFormat.DEFAULT.withFirstRecordAsHeader().
			withIgnoreHeaderCase().
			withTrim()
		);
		
		SummaryDTO summary = new SummaryDTO();
		List<CSVRecord> recordsCSV = csvParser.getRecords();
		int rowsCount = recordsCSV.size();
		List<String> headersStr = csvParser.getHeaderNames();
		List<TableHeaderDto> cleanHeaders = new ArrayList<>();
		List<HashMap<String, String>> preview = new ArrayList<>();
		
		csvParser.close();
		
		if(!headersStr.contains(lmwPK)) {
			summary.setMessage("Error: Missing required column " + lmwPK + ".");
			return new SingleResponse<SummaryDTO>(StatusCode.DB_COLUMNS_MISMATCH.getCode(), summary);
		}
		
		String fileName = saveFile(file, path , filePrefix, fileExt);
		
		for (String header : headersStr) {
			TableHeaderDto dto = new TableHeaderDto();
			dto.setText(header);
			dto.setValue(dto.getText());
			cleanHeaders.add(dto);
		}
		
		int firstNRows = 100;
		
		for (CSVRecord csvRecord : recordsCSV) {
			
			if(firstNRows <= 0) break;
			
			HashMap<String, String> record = new HashMap<>();

			for (String header : headersStr) {
				record.put(header, csvRecord.get(header));
			}

			preview.add(record);
			firstNRows--;
		}
		
		summary.setFileName(fileName);
		summary.setRowsCount(rowsCount);
		summary.setHeaders(cleanHeaders);
		summary.setRows(preview);
		summary.setMessage("OK");
		
		return new SingleResponse<SummaryDTO>(StatusCode.OK.getCode(), summary);	
	}

	@Override
	public SingleResponse<SummaryDTO> replaceDataset(String path, ReplaceDatasetRequestDto request, int authorId, String ext, String tableName, String schema, String prefix){
		log.info("Reemplazo de datos");
		
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		CSVParser csvParser = null;
		
		try {

			String filePath = path.replace('/', File.separatorChar) + request.getFileName() + ext;
			
			fileInputStream = new FileInputStream(filePath);
			inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			bufferedReader = new BufferedReader(inputStreamReader);

			csvParser = new CSVParser(
					bufferedReader,
				CSVFormat.DEFAULT.withFirstRecordAsHeader().
				withIgnoreHeaderCase().
				withTrim()
			);

			SummaryDTO summary = new SummaryDTO();                                                                            
			List<CSVRecord> csvRecords = csvParser.getRecords();
			List<String> headers = csvParser.getHeaderNames();
			List<String> cleanHeaders = new ArrayList<>();
			List<TableHeaderDto> tableHeaders = new ArrayList<>();
			
			headerDao.deleteAll();
			
			for (String header : headers) {
				
				Header headerModel = createHeader(header);
				headerDao.save(headerModel);
				
				TableHeaderDto dto = new TableHeaderDto();
				dto.setText(headerModel.getFrontEndHeader());
				dto.setValue(dto.getText());
				tableHeaders.add(dto);
				
				cleanHeaders.add(headerModel.getDataBaseHeader());
			}
			
			Admin admin = adminService.findById(authorId);

			if(dynamicSQLQuery.tableExists(tableName, schema) && 
					!generateBackup(tableName, backupPath, prefix, ext, LocalDateTime.now(), admin.getFullName(), request.getComment(), "Total")) {
				
				summary.setMessage("Error: couldn't generate backup file");
				return new SingleResponse<SummaryDTO>(StatusCode.DB_BACKUP_ERROR.getCode(), summary);
			}

			dynamicSQLQuery.dropTableIfExists(tableName);

			if (!dynamicSQLQuery.createTable(tableName, cleanHeaders)) {
				summary.setMessage("Error: couldn't create table for data");
				return new SingleResponse<SummaryDTO>(StatusCode.DB_REPLACEMENT_ERROR.getCode(), summary);
			}

			dynamicSQLQuery.insertValuesIntoTable(tableName, cleanHeaders, headers, csvRecords);
			
			summary.setFileName(request.getFileName());
			summary.setRowsCount(csvRecords.size());
			summary.setHeaders(tableHeaders);
			summary.setMessage("OK");
			
			return new SingleResponse<SummaryDTO>(StatusCode.OK.getCode(), summary);
		
		}catch(Exception ex) {
			
			SummaryDTO summary = new SummaryDTO();
			log.error(ex.getMessage());
			summary.setMessage("Error: couldn't replace data");
			
			return new SingleResponse<SummaryDTO>(StatusCode.DB_REPLACEMENT_ERROR.getCode(), summary);
		} finally {
			if(fileInputStream!=null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			if(bufferedReader!=null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			if(csvParser!=null) {
				try {
					csvParser.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
	}
	
	private Header createHeader(String rowHeader) {
		
		return new Header(
				rowHeader.toLowerCase().replace(' ', '_'), 
				rowHeader.toLowerCase().replace(' ', '_').replace("en_", "").replace("es_", "").replaceAll("[0-9][0-9]_", ""));

	}

	@Override
	public String saveFile(MultipartFile file, String path, String prefix, String ext) {
		log.info("Guardado de archivo");
		String fileName = "";
		
		try {
			
			path = path.replace('/', File.separatorChar);
			
			if(!new File(path).exists()) {
				new File(path).mkdirs();
			}
			
			String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMAT));
			fileName = prefix + timeStamp;
			String newFilePath = path + fileName + ext;
			
			Files.copy(file.getInputStream(), Paths.get(newFilePath), StandardCopyOption.REPLACE_EXISTING);
						
		}catch(Exception ex) {
			log.error(ex.getMessage());
		}
		
		return fileName;
	}

	@Override
	public SingleResponse<SummaryDTO> updateDataset(String path, ReplaceDatasetRequestDto request, int authorId, String ext, String tableName, String schema, String prefix) {
		log.info("Actualización de datos");
		
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		CSVParser csvParser = null;
		
		try {
			String filePath = path.replace('/', File.separatorChar) + request.getFileName() + ext;
			fileInputStream = new FileInputStream(filePath);
			inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			bufferedReader = new BufferedReader(inputStreamReader);
			
			csvParser = new CSVParser(
					bufferedReader,
				CSVFormat.DEFAULT.withFirstRecordAsHeader().
				withIgnoreHeaderCase().
				withTrim()
			);
			SummaryDTO summary = new SummaryDTO();
			List<CSVRecord> csvRecords = csvParser.getRecords();
			List<String> headers = csvParser.getHeaderNames();
			List<String> cleanHeaders = new ArrayList<>();
			List<TableHeaderDto> tableHeaders = new ArrayList<>();
			
			for (String header : headers) {
				
				Header headerModel = createHeader(header); 
				
				TableHeaderDto dto = new TableHeaderDto();
				dto.setText(headerModel.getFrontEndHeader());
				dto.setValue(dto.getText());
				tableHeaders.add(dto);
				
				cleanHeaders.add(headerModel.getDataBaseHeader());
			}
			
			if(!dynamicSQLQuery.tableExists(tableName, schema)) {
				summary.setMessage("Error: table "+ tableName +" doesn't exists");
				return new SingleResponse<SummaryDTO>(StatusCode.DB_TABLE_NOT_FOUND.getCode(), summary);
			}
			
			List<String> columnsNames = dynamicSQLQuery.getTableColumns(tableName);

			if(!cleanHeaders.equals(columnsNames)) {
				summary.setMessage("Error: column mismatch");
				return new SingleResponse<SummaryDTO>(StatusCode.DB_COLUMNS_MISMATCH.getCode(), summary);
			}
			
			Admin admin = adminService.findById(authorId);
			
			if(!generateBackup(tableName, backupPath, prefix, ext, LocalDateTime.now(), admin.getFullName(), request.getComment(), "Partial")) {
				summary.setMessage("Error: couldn't generate backup file");
				return new SingleResponse<SummaryDTO>(StatusCode.DB_BACKUP_ERROR.getCode(), summary);
			}
			
			dynamicSQLQuery.insertValuesIntoTable(tableName, cleanHeaders, headers, csvRecords);
			
			summary.setFileName(request.getFileName());
			summary.setRowsCount(csvRecords.size());
			summary.setHeaders(tableHeaders);
			summary.setMessage("OK");
			
			return new SingleResponse<SummaryDTO>(StatusCode.OK.getCode(), summary);
		
		}catch(Exception ex) {
			
			SummaryDTO summary = new SummaryDTO();
			log.error(ex.getMessage());
			summary.setMessage("Error: couldn't update data");
			
			return new SingleResponse<SummaryDTO>(StatusCode.DB_UPDATE_ERROR.getCode(), summary);
		} finally {
			if(fileInputStream!=null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			if(bufferedReader!=null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			if(csvParser!=null) {
				try {
					csvParser.close();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	@Override
	public SingleResponse<SummaryDTO> getStoredHeaders(LangRequestDto query) {
		log.info("Listado de cabeceras");
		SummaryDTO summary = new SummaryDTO();
		
		try{
			List<String> headers = dynamicSQLQuery.getLMWTableColumns(query);
			List<TableHeaderDto> tableHeaders = new ArrayList<>();
			
			for (String header : headers) {
				
				TableHeaderDto dto = new TableHeaderDto();
				dto.setText(header.toLowerCase().replace(' ', '_'));
				dto.setValue(dto.getText());
				tableHeaders.add(dto);
			}
			
			summary.setMessage("OK");
			summary.setHeaders(tableHeaders);
			
		}catch(Exception ex) {
			log.error(ex.getMessage());
			summary.setMessage("Error: Table not found");
			return new SingleResponse<SummaryDTO>(StatusCode.DB_TABLE_NOT_FOUND.getCode(), summary);
		}
		
		return new SingleResponse<SummaryDTO>(StatusCode.OK.getCode(), summary);
	}

	@Override
	public SingleResponse<SummaryDTO> getDataForVisualization(QueryRequestDTO query, String tableName) {
		log.info("Generación de datos para visualización");
		SummaryDTO summary = new SummaryDTO();
		
		try {
			List<TableHeaderDto> tableHeaders = new ArrayList<>();
			
			for (FilterDTO filter : query.getFilters()) {
				TableHeaderDto dto = new TableHeaderDto();
				dto.setText(filter.getHeader());
				dto.setValue(dto.getText());
				tableHeaders.add(dto);
			}
			
			query = toDatabaseHeaders(query);
			
			List<HashMap<String, String>> rows = dynamicSQLQuery.executeDynamicQuery(query, tableName);
			int queryRows = dynamicSQLQuery.countDynamicQueryRows(query, tableName);
			int totalRows = dynamicSQLQuery.totalRowCount(tableName);
			
			summary.setMessage("OK");
			summary.setHeaders(tableHeaders);
			summary.setRows(rows);
			summary.setQueryRowsCount(queryRows);
			summary.setTotalTableRowsCount(totalRows);
			summary.setRowsCount(rows.size());
			
		}catch(Exception ex) {
			log.error(ex.getMessage());
			summary.setMessage("Error: Can't generate query");
			return new SingleResponse<SummaryDTO>(StatusCode.DB_BAD_QUERY_REQUEST.getCode(), summary);
		}
		
		return new SingleResponse<SummaryDTO>(StatusCode.OK.getCode(), summary);
	}
	
	private QueryRequestDTO toDatabaseHeaders(QueryRequestDTO query) {
		
		for (FilterDTO filter : query.getFilters()) {
			
			Optional<Header> header = headerDao.findByFrontEndHeader(filter.getHeader());
			
			if(header.isEmpty()) continue;
			
			filter.setHeader(header.get().getDataBaseHeader());
		}
		
		return query;
	}

	@Override
	public SingleResponse<DatasetFileResponseDTO> downloadCSV(QueryRequestDTO query, String tableName, String downloadPath, String prefix, String ext) {
		log.info("Descarga de archivo de datos");
		DatasetFileResponseDTO response = new DatasetFileResponseDTO();
		downloadPath = downloadPath.replace('/', File.separatorChar);

		if(!new File(downloadPath).exists()) {
			new File(downloadPath).mkdirs();
		}
		
		String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMAT));
		String fileName = prefix + "Generated_" + timeStamp;
		String newFilePath = downloadPath + fileName + ext;
		
		query = toDatabaseHeaders(query);
		
		if(!Boolean.TRUE.equals(dynamicSQLQuery.executeDynamicQuerySaveToFile(query, tableName, newFilePath))) 
			return new SingleResponse<DatasetFileResponseDTO>(StatusCode.DB_BAD_QUERY_REQUEST.getCode(), response);
		
		InputStream targetFile = null;
		try {
			targetFile = new FileInputStream(new File(newFilePath));
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			return new SingleResponse<DatasetFileResponseDTO>(StatusCode.DB_FILE_NOT_FOUND.getCode(), response);
		}
		
		response.setFile(new InputStreamResource(targetFile));
		response.setName(newFilePath);
		
		return new SingleResponse<DatasetFileResponseDTO>(StatusCode.OK.getCode(), response);
	}

	@Override
	public boolean generateBackup(String tableName, String path, String prefix, String ext, LocalDateTime timeStamp, String author, String comment, String type) {
		log.info("Generación de archivo de respaldo");
		path = path.replace('/', File.separatorChar);
		
		if(!new File(path).exists()) {
			new File(path).mkdirs();
		}
		
		QueryRequestDTO query = new  QueryRequestDTO();
		query.setUseDistinct(false);
		query.setUsePagination(false);
		query.setUseDates(false);
		query.setPageOffset(0);
		query.setFilters(new ArrayList<>());
		
		String backupName = prefix + "Backup_" + timeStamp.format(DateTimeFormatter.ofPattern(FORMAT));
		String newBackupPath = backupPath.replace('/', File.separatorChar) + backupName + ext;
							
		if(!Boolean.TRUE.equals(dynamicSQLQuery.executeDynamicQuerySaveToFile(query , tableName , newBackupPath)))
			return false;
		
		Backup backup = new Backup();
		backup.setFileName(backupName);
		backup.setTimeStamp(timeStamp);
		backup.setType(type);
		backup.setComment(comment);
		backup.setAuthor(author);
		
		datasetBackupDao.save(backup);
		
		return true;
	}

	@Override
	public List<Backup> listBackups() {
		log.info("Listado de archivos de respaldo");
		return (List<Backup>) datasetBackupDao.findAll();
	}

	@Override
	public SingleResponse<SummaryDTO> restoreFromBackupById(Long id, int authorId, String ext, String tableName, String schema, String prefix) {
		log.info("Restaurar desde archivo de respaldo");
		SummaryDTO summary = new SummaryDTO();
		Optional<Backup> backupOpt = datasetBackupDao.findById(id);
		
		if(backupOpt.isEmpty()) {
			summary.setMessage("Error: couldn't find backup with id "+ id);
			return new SingleResponse<SummaryDTO>(StatusCode.DB_BACKUP_NOT_FOUND.getCode(), summary);
		}
		
		Backup backup = backupOpt.get();
		
		String backupName = backup.getFileName();
		ReplaceDatasetRequestDto request = new ReplaceDatasetRequestDto();
		request.setFileName(backupName);
		request.setComment("Automatically generated from backup "
		+ backupName +"-"
		+ backup.getTimeStamp().toString() +"-"
		+ backup.getType());
		
		return replaceDataset(backupPath, request, authorId, ext, tableName, schema, prefix);
	}
	
	@Override
	public SingleResponse<DatasetFileResponseDTO> downloadBackup(Long id) {
	
		DatasetFileResponseDTO response = new DatasetFileResponseDTO();
		Optional<Backup> backupOpt = datasetBackupDao.findById(id);
		
		if(backupOpt.isEmpty()) {
			return new SingleResponse<DatasetFileResponseDTO>(StatusCode.DB_BACKUP_NOT_FOUND.getCode(), response);
		}
		
		Backup backup = backupOpt.get();
		
		String backupName = backup.getFileName();
		
		String filePath = backupPath.replace('/', File.separatorChar) + backupName + ext;
		
		InputStream targetFile = null;
		try {
			targetFile = new FileInputStream(new File(filePath));
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			return new SingleResponse<DatasetFileResponseDTO>(StatusCode.DB_FILE_NOT_FOUND.getCode(), response);
		}
		
		response.setFile(new InputStreamResource(targetFile));
		response.setName(backupName);
		
		return new SingleResponse<DatasetFileResponseDTO>(StatusCode.OK.getCode(), response);
	}

	@Override
	public boolean deleteFile(String path, String fileName, String ext) {
		String filePath = path.replace('/', File.separatorChar) + fileName + ext;

		File deletedFile = new File(filePath);
		
		return deletedFile.delete();
	}

	@Override
	public void deleteAllFilesIn(String path) {
		String directoryPath = path.replace('/', File.separatorChar);
		
		File directory = new File(directoryPath);
		
		if(!directory.exists()) {
			directory.mkdirs();
		}
		
		try {
			FileUtils.cleanDirectory(directory);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	@Override
	public SingleResponse<DatesResponseDto> getStoredDates(String tableName) {
		return new SingleResponse<DatesResponseDto>(StatusCode.OK.getCode(), 
				new DatesResponseDto(
						dynamicSQLQuery.getStartDate(tableName), 
						dynamicSQLQuery.getEndDate(tableName)));
	}
	
}
