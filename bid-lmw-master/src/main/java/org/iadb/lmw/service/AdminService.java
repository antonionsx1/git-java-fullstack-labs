package org.iadb.lmw.service;

import java.util.List;

import javax.security.sasl.AuthenticationException;

import org.iadb.lmw.dto.DeleteAdminResponseDto;
import org.iadb.lmw.dto.GenerateRequestDto;
import org.iadb.lmw.dto.GenerateResponseDto;
import org.iadb.lmw.dto.LoginRequestDto;
import org.iadb.lmw.dto.LoginResponseDto;
import org.iadb.lmw.dto.RecoverPassResponseDto;
import org.iadb.lmw.dto.RecoverPasswordDto;
import org.iadb.lmw.dto.RegisterRequestDto;
import org.iadb.lmw.dto.RegisterResponseDto;
import org.iadb.lmw.dto.ViewAdminResponseDto;
import org.iadb.lmw.model.Admin;

public interface AdminService {

	LoginResponseDto login(LoginRequestDto request) throws AuthenticationException;

	GenerateResponseDto generatePassword(GenerateRequestDto request);

	List<ViewAdminResponseDto> viewAll() throws AuthenticationException;

	DeleteAdminResponseDto delete(Integer id);
	
	Admin findById(Integer id);

	RegisterResponseDto register(RegisterRequestDto request, String lang)
			throws IllegalArgumentException, AuthenticationException;

	RecoverPassResponseDto recoverPassword(RecoverPasswordDto request, String lang)
			throws IllegalArgumentException, AuthenticationException;

}
