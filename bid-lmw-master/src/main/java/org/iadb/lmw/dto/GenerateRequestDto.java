package org.iadb.lmw.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GenerateRequestDto implements Serializable {

	private static final long serialVersionUID = 1L;
	@NotBlank(message = "temporal no debe estar vacío")
	private String temporal;
	@NotBlank(message = "password no debe estar vacío")
	private String password;
	
}
