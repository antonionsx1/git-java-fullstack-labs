package org.iadb.lmw.dao;

import java.util.Optional;

import org.iadb.lmw.model.Header;
import org.springframework.data.repository.CrudRepository;

/**
 *
 */
public interface HeaderDao extends CrudRepository<Header, String> {

    Optional<Header> findByFrontEndHeader(String frontEndHeader);

}
