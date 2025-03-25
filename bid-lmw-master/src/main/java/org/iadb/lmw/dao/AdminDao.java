package org.iadb.lmw.dao;

import java.util.List;
import java.util.Optional;

import org.iadb.lmw.model.Admin;
import org.springframework.data.repository.CrudRepository;

public interface AdminDao extends CrudRepository<Admin, Integer>{

	/**
	 * 
	 * @param id
	 * @return
	 */
	Optional<Admin> findById(Integer id);
	
	List<Admin> findAll();
	
	List<Admin> findByEmail(String email);
	
	List<Admin> findByTemporal(String temporal);
	
}