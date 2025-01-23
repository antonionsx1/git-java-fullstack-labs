package org.iadb.lmw.task;

import org.iadb.lmw.service.DataManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TemporalFilesTask {

    private static final Logger log = LoggerFactory.getLogger(TemporalFilesTask.class);

    @Autowired
    DataManagementService dataManagementService;

    @Value("${data.dataset.download-path}")
    private String downloadPath;
    @Value("${data.dataset.path}")
    private String path;

    @Scheduled(cron = "0 0 3 * * *")////Everyday at 3 o'clock
    public void deleteTask() {

        log.info("Scheduled Task Delete Temporal Files: Started");

        dataManagementService.deleteAllFilesIn(path);
        dataManagementService.deleteAllFilesIn(downloadPath);

        log.info("Scheduled Task Delete Temporal Files: Finished");
    }
}
