package org.iadb.lmw.service;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.iadb.lmw.dto.LangRequestDto;
import org.iadb.lmw.dto.QueryRequestDTO;

public interface DynamicSQLQueryService {

    boolean tableExists(String tableName, String schemaName);

    boolean createTable(String tableName, List<String> columns);

    void dropTableIfExists(String tableName);

    void insertValuesIntoTable(String tableName, List<String> cleanColumns, List<String> columns, List<CSVRecord> values);

    List<String> getLMWTableColumns(LangRequestDto query);

    List<HashMap<String, String>> executeDynamicQuery(QueryRequestDTO query, String tableName);

    Boolean executeDynamicQuerySaveToFile(QueryRequestDTO query, String tableName, String filePath);

    boolean downloadCommentsFile(List<Long> ids, String filePath);

    int totalRowCount(String tableName);

    List<String> getTableColumns(String tableName);

    String getStartDate(String tableName);

    String getEndDate(String tableName);

    int countDynamicQueryRows(QueryRequestDTO query, String tableName);

}