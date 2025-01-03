package org.iadb.lmw.task;

import java.time.LocalDateTime;
import java.util.List;

import org.iadb.lmw.dao.DatasetBackupDao;
import org.iadb.lmw.model.Backup;
import org.iadb.lmw.service.DataManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackupTask {

	private static final Logger log = LoggerFactory.getLogger(BackupTask.class);
	
	@Autowired
	DatasetBackupDao backupDao;
	@Autowired
	DataManagementService dataManagementService;
	@Value("${data.dataset.backup-path}") 
	private String backupPath;
	@Value("${data.dataset.ext}") 
	private String ext;
	
	//Everyday at 23 o'clock
	@Scheduled(cron = "0 0 23 * * *")
	public void deleteTask() {
		
		log.info("Scheduled Task Delete Old Backups: Started");
		List<Backup> backups = (List<Backup>) backupDao.findAll();
		long maxTimeToLiveInMonths = 1L;
		
		for (Backup backup : backups) {
			LocalDateTime backupTimeStamp = backup.getTimeStamp();
			
			backupTimeStamp = backupTimeStamp.plusMonths(maxTimeToLiveInMonths);
			
			if(backupTimeStamp.isAfter(LocalDateTime.now()))
				continue;
			
			backupDao.deleteById(backup.getId());
			dataManagementService.deleteFile(backupPath, backup.getFileName(), ext);
			
		}
		
		log.info("Scheduled Task Delete Old Backups: Finished");
	}
	
	
}
