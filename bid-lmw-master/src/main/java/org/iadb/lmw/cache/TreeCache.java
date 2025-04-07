package org.iadb.lmw.cache;

import org.iadb.lmw.dto.TreeDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public interface TreeCache extends CrudRepository<TreeDto, String>{

}
