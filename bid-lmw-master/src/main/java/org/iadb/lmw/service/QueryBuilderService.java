package org.iadb.lmw.service;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.iadb.lmw.dto.LangRequestDto;
import org.iadb.lmw.dto.QueryRequestDTO;

public interface QueryBuilderService {
	
	String tableExists();
	String createTable(String tableName, List<String> columns);
	String deleteTable(String tableName);
	String insertValuesIntoTable(String tableName, List<String> columns, List<CSVRecord> values);
	String getLMWTableColumns(LangRequestDto query);
	String prepareDynamicQuery(QueryRequestDTO query, String tableName);
	String downloadCommentsFile(List<Long> ids);
	String totalRowCount(String mainQuery);
	String getTableColumns(String tableName);
	String startDate(String tableName);
	String endDate(String tableName);
	String countRowsDynamicQuery(QueryRequestDTO query, String tableName);

}
