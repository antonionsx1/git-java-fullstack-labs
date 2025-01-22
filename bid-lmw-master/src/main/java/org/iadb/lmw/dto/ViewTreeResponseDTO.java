package org.iadb.lmw.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ViewTreeResponseDTO implements Serializable{

	@Serial
	private static final long serialVersionUID = 1L;

	private String id;
	
	private String name;
	private List<ViewTreeResponseDTO> childrens;
	
}
