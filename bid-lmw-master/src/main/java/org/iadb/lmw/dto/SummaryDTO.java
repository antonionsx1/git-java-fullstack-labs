package org.iadb.lmw.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SummaryDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String message;
	private String fileName;
	private Integer rowsCount;
	private Integer totalTableRowsCount;
	private Integer queryRowsCount;
	private List<TableHeaderDto> headers;
	private List<HashMap<String, String>> rows;
	
}
