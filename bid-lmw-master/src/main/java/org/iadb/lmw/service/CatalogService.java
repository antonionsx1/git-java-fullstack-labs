package org.iadb.lmw.service;

import java.io.FileNotFoundException;
import java.util.List;

import org.iadb.lmw.dto.CreateCountryRequestDto;
import org.iadb.lmw.dto.CreateCountryResponseDto;
import org.iadb.lmw.dto.UpdateCountryRequestDto;
import org.iadb.lmw.dto.UpdateCountryResponseDto;
import org.iadb.lmw.dto.ViewCountryResponseDto;
import org.iadb.lmw.dto.ViewTreeResponseDTO;

public interface CatalogService {

    CreateCountryResponseDto createCountry(CreateCountryRequestDto request);

    UpdateCountryResponseDto updateCountry(UpdateCountryRequestDto request) throws FileNotFoundException;

    List<ViewCountryResponseDto> viewAllCountries(String lang) throws FileNotFoundException;

    ViewCountryResponseDto findCountry(String name, String lang) throws FileNotFoundException;

    List<ViewTreeResponseDTO> viewTree(String lang) throws FileNotFoundException;

}
