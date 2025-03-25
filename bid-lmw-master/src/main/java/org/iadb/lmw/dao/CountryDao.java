package org.iadb.lmw.dao;

import java.util.List;

import org.iadb.lmw.model.Country;
import org.springframework.data.repository.CrudRepository;

public interface CountryDao extends CrudRepository<Country, Integer> {

    // --- Métodos de búsqueda existentes ---
    List<Country> findAll();

    /**
     * 
     * @param nameEs
     * @return
     */
    List<Country> findByNameEs(String nameEs);

    List<Country> findByNameEn(String nameEn);

}
