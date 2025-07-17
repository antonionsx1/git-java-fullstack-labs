package org.iadb.lmw.service.impl;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.iadb.lmw.dao.CountryDao;
import org.iadb.lmw.dto.CreateCountryRequestDto;
import org.iadb.lmw.dto.CreateCountryResponseDto;
import org.iadb.lmw.dto.FilterDTO;
import org.iadb.lmw.dto.QueryRequestDTO;
import org.iadb.lmw.dto.UpdateCountryRequestDto;
import org.iadb.lmw.dto.UpdateCountryResponseDto;
import org.iadb.lmw.dto.ViewCountryResponseDto;
import org.iadb.lmw.dto.ViewTreeResponseDTO;
import org.iadb.lmw.model.Country;
import org.iadb.lmw.service.CatalogService;
import org.iadb.lmw.service.DynamicSQLQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CatalogServiceImpl implements CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogServiceImpl.class);

    @Autowired
    private CountryDao countryDao;

    @Autowired
    private DynamicSQLQueryService dynamicSQLService;

    @Value("${data.sql.lmw-table-name}")
    private String tableName;
    @Value("${data.dataset.tree-prefix-regex}")
    private String treePrefixRegex;

    @Override
    public List<ViewCountryResponseDto> viewAllCountries(String lang) throws FileNotFoundException {
        log.info("Listar paises");
        List<ViewCountryResponseDto> dtos = new ArrayList<>();
        List<Country> countries = countryDao.findAll();

        for (Country country : countries) {
            ViewCountryResponseDto dto = new ViewCountryResponseDto();
            if (lang.equals("ES")) {
                dto.setName(country.getNameEs());
            } else if (lang.equals("EN")) {
                dto.setName(country.getNameEn());
            } else {
                throw new FileNotFoundException("Lenguaje no soportado");
            }
            BeanUtils.copyProperties(country, dto);
            dtos.add(dto);
        }

        return dtos;
    }

    @Override
    public ViewCountryResponseDto findCountry(String name, String lang) throws FileNotFoundException {
        log.info("Buscar país");
        ViewCountryResponseDto dto = new ViewCountryResponseDto();
        List<Country> countries = null;
        if (lang.equals("ES")) {
            countries = countryDao.findByNameEs(name);
            BeanUtils.copyProperties(countries.get(0), dto);
            dto.setName(countries.get(0).getNameEs());
        } else if (lang.equals("EN")) {
            countries = countryDao.findByNameEn(name);
            BeanUtils.copyProperties(countries.get(0), dto);
            dto.setName(countries.get(0).getNameEn());
        } else {
            throw new FileNotFoundException("Lenguaje no soportado");
        }

        return dto;

    }

    @Override
    public CreateCountryResponseDto createCountry(CreateCountryRequestDto request) {
        log.info("Crear país");
        Country country = new Country();
        BeanUtils.copyProperties(request, country);
        country.setRegDate(new Date());
        country.setStatus(true);
        country = countryDao.save(country);

        return new CreateCountryResponseDto("Registro exitoso con ID: " + country.getId().toString());
    }

    @Override
    public UpdateCountryResponseDto updateCountry(UpdateCountryRequestDto request) throws FileNotFoundException {
        log.info("Actualizando país");

        Optional<Country> country = countryDao.findById(request.getId());

        if (country.isEmpty()) {
            throw new FileNotFoundException("País no encontrado");
        } else {

            country.get().setColor(request.getColor());
            countryDao.save(country.get());
            return new UpdateCountryResponseDto("Actualización exitosa del ID: " + request.getId().toString());
        }


    }

    @Override
    public List<ViewTreeResponseDTO> viewTree(String lang) throws FileNotFoundException {
        log.info("Generar árbol de la base de datos");
        List<String> headers = getLangHeaders(lang);

        List<ViewTreeResponseDTO> tree = treeBuilder(0, new ArrayList<>(), headers, lang);

        return tree;
    }

    private List<String> getLangHeaders(String lang) {
        List<String> headers = new ArrayList<>();

        String findRegex = lang + "_" + "[0-9][0-9]_.*";

        for (String header : dynamicSQLService.getTableColumns(tableName)) {
            if (header.matches(findRegex)) headers.add(header);
        }

        Collections.sort(headers);

        return headers;
    }

    private List<ViewTreeResponseDTO> treeBuilder(int header, ArrayList<FilterDTO> filters, List<String> headers, String lang) {

        if (header >= headers.size()) return new ArrayList<>();

        List<ViewTreeResponseDTO> tree = new ArrayList<>();
        String findRegex = lang + treePrefixRegex;

        filters.add(getInitialFilter(headers.get(header)));

        for (HashMap<String, String> result : makeQuery(filters)) {

            if (!filters.isEmpty())
                filters.remove(filters.size() - 1);

            filters.add(updateFilter(result, headers.get(header), findRegex));

            tree.add(new ViewTreeResponseDTO(
                    headers.get(header).replaceAll(findRegex, ""),
                    result.get(headers.get(header).replaceAll(findRegex, "")),
                    treeBuilder(header + 1, filters, headers, lang)
            ));

            if (!filters.isEmpty())
                filters.remove(filters.size() - 1);
        }

        return tree;
    }

    /**
     * 
     * @param result
     * @param header
     * @param findRegex
     * @return
     */
    private FilterDTO updateFilter(HashMap<String, String> result, String header, String findRegex) {
        List<String> conditions = new ArrayList<>();
        conditions.add(result.get(header.replaceAll(findRegex, "")));
        return new FilterDTO(header, conditions);
    }

    private List<HashMap<String, String>> makeQuery(ArrayList<FilterDTO> filters) {
        QueryRequestDTO query = new QueryRequestDTO(true, false, false, "", "", 0, filters);
        return dynamicSQLService.executeDynamicQuery(query, tableName);
    }

    private FilterDTO getInitialFilter(String header) {
        List<String> initialConditions = new ArrayList<>();
        return new FilterDTO(header, initialConditions);
    }
}
