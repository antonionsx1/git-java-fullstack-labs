package org.iadb.lmw.dao;

import org.iadb.lmw.model.Backup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public interface DatasetBackupDao extends CrudRepository<Backup, Long> {

}
