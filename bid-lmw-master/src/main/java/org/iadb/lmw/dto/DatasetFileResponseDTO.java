package org.iadb.lmw.dto;

import org.springframework.core.io.InputStreamResource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DatasetFileResponseDTO {

	private String name;
	private InputStreamResource file;
}
