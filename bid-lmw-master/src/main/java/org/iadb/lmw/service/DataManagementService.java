package org.iadb.lmw.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.iadb.lmw.dto.DatasetFileResponseDTO;
import org.iadb.lmw.dto.DatesResponseDto;
import org.iadb.lmw.dto.LangRequestDto;
import org.iadb.lmw.dto.QueryRequestDTO;
import org.iadb.lmw.dto.ReplaceDatasetRequestDto;
import org.iadb.lmw.dto.SingleResponse;
import org.iadb.lmw.dto.SummaryDTO;
import org.iadb.lmw.model.Backup;
import org.springframework.web.multipart.MultipartFile;

public interface DataManagementService {

	SingleResponse<SummaryDTO> verify(MultipartFile file, String path, String filePrefix, String fileExt) throws UnsupportedEncodingException, IOException;
	SingleResponse<SummaryDTO> replaceDataset(String path, ReplaceDatasetRequestDto request, int authorId, String ext, String tableName, String schema, String prefix);
	String saveFile(MultipartFile file, String path, String filePrefix, String fileExt);
	SingleResponse<SummaryDTO> updateDataset(String path, ReplaceDatasetRequestDto request, int authorId, String ext, String tableName, String schema, String prefix);
	SingleResponse<SummaryDTO> getStoredHeaders(LangRequestDto query);
	SingleResponse<SummaryDTO> getDataForVisualization(QueryRequestDTO query, String tableName);
	SingleResponse<DatasetFileResponseDTO> downloadCSV(QueryRequestDTO query, String tableName, String downloadPath, String prefix, String ext);
	boolean generateBackup(String tableName, String path, String prefix, String ext, LocalDateTime timeStamp, String author, String comment, String type);
	List<Backup> listBackups();
	SingleResponse<SummaryDTO> restoreFromBackupById(Long id, int authorId, String ext, String tableName, String schema, String prefix);
	boolean deleteFile(String path, String fileName, String ext);
	void deleteAllFilesIn(String path);
	SingleResponse<DatasetFileResponseDTO> downloadBackup(Long id);
	SingleResponse<DatesResponseDto> getStoredDates(String tableName);
	
}
