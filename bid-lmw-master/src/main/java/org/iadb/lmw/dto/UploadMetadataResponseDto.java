package org.iadb.lmw.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class UploadMetadataResponseDto implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String message;
}
