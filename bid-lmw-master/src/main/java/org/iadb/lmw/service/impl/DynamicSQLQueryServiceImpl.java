package org.iadb.lmw.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.iadb.lmw.dto.FilterDTO;
import org.iadb.lmw.dto.LangRequestDto;
import org.iadb.lmw.dto.QueryRequestDTO;
import org.iadb.lmw.service.DynamicSQLQueryService;
import org.iadb.lmw.service.QueryBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

@Service
public class DynamicSQLQueryServiceImpl implements DynamicSQLQueryService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    QueryBuilderService queryBuilder;

    private static final Logger log = LoggerFactory.getLogger(DynamicSQLQueryServiceImpl.class);

    @Override
    public boolean tableExists(String tableName, String schemaName) {
        log.info("Comprobar existencia de la tabla");
        return Boolean.TRUE.equals(jdbcTemplate.execute(queryBuilder.tableExists(), new PreparedStatementCallback<Boolean>() {
			@Override
			public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				ps.setString(1, schemaName);
				ps.setString(2, tableName);

				ResultSet resultSet = ps.executeQuery();
				resultSet.next();

				return resultSet.getBoolean("exists");
			}
		}));
    }

    @Override
    public void dropTableIfExists(String tableName) {
        log.info("Eliminar tabla si existe");
        jdbcTemplate.execute(queryBuilder.deleteTable(tableName));
    }

    @Override
    public boolean createTable(String tableName, List<String> columns) {
        try {

            jdbcTemplate.execute(queryBuilder.createTable(tableName, columns));

            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return false;
    }

    @Override
    public void insertValuesIntoTable(String tableName, List<String> cleanColumns, List<String> columns,
                                      List<CSVRecord> values) {
        // PreparedStatement supports at most 65,535 parameters
        int maxParametersInPS = 65000;
        int columnsSize = columns.size();
        int paramCount = 0;
        List<CSVRecord> partialValues = new ArrayList<>();

        for (CSVRecord csvRecord : values) {

            partialValues.add(csvRecord);

            if (paramCount < maxParametersInPS) {
                paramCount += columnsSize;
                continue;
            }

            insertMaxValuesIntoTable(tableName, cleanColumns, columns, partialValues);
            partialValues = new ArrayList<>();
            paramCount = 0;
        }

        if (!partialValues.isEmpty())
            insertMaxValuesIntoTable(tableName, cleanColumns, columns, partialValues);

    }

    private void insertMaxValuesIntoTable(String tableName, List<String> cleanColumns, List<String> columns,
                                          List<CSVRecord> values) {
        log.info("Inserción de valores");
        jdbcTemplate.execute(queryBuilder.insertValuesIntoTable(tableName, cleanColumns, values),
                new PreparedStatementCallback<Void>() {
                    @Override
                    public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

                        int valueIdx = 1;

                        for (CSVRecord csvRecord : values) {
                            for (String column : columns) {

                                ps.setString(valueIdx, csvRecord.get(column));
                                valueIdx++;
                            }
                        }

                        ps.executeUpdate();
                        return null;
                    }
                });
    }

    @Override
    public List<String> getLMWTableColumns(LangRequestDto query) {
        log.info("Obtención de cabeceras de la tabla LMW");
        return jdbcTemplate.execute(queryBuilder.getLMWTableColumns(query),
                new PreparedStatementCallback<List<String>>() {
                    @Override
                    public List<String> doInPreparedStatement(PreparedStatement ps)
                            throws SQLException, DataAccessException {

                        ResultSet resultSet = ps.executeQuery();
                        List<String> columns = new ArrayList<>();

                        while (resultSet.next()) {
                            columns.add(resultSet.getString(1));
                        }

                        return columns;
                    }
                });
    }

    @Override
    public List<HashMap<String, String>> executeDynamicQuery(QueryRequestDTO query, String tableName) {
        log.info("Ejecución de consulta dinámica");
        return jdbcTemplate.execute(queryBuilder.prepareDynamicQuery(query, tableName),
                new PreparedStatementCallback<List<HashMap<String, String>>>() {
                    @Override
                    public List<HashMap<String, String>> doInPreparedStatement(PreparedStatement ps)
                            throws SQLException, DataAccessException {

                        List<HashMap<String, String>> result = new ArrayList<>();
                        List<FilterDTO> filters = query.getFilters();
                        int paramIdx = 1;
                        boolean filtersIsEmpty = filters.isEmpty();
                        List<String> columnNames = new ArrayList<>();

                        if (!filtersIsEmpty) {
                            for (FilterDTO filterDTO : filters) {
                                List<String> conditions = filterDTO.getConditions();

                                if (conditions.isEmpty())
                                    continue;

                                for (String condition : conditions) {
                                    ps.setString(paramIdx, condition);
                                    paramIdx++;
                                }
                            }
                        }

                        ResultSet rs = ps.executeQuery();

                        if (filtersIsEmpty) {
                            columnNames = getTableColumns(tableName);
                        }

                        while (rs.next()) {
                            HashMap<String, String> row = new HashMap<>();
                            if (filtersIsEmpty) {
                                for (String name : columnNames) {
                                    row.put(name.replace("en_", "").replace("es_", "").replaceAll("[0-9][0-9]_", ""),
                                            rs.getString(name));
                                }
                            } else {
                                for (FilterDTO filterDTO : filters) {
                                    row.put(filterDTO.getHeader().replace("en_", "").replace("es_", "")
                                            .replaceAll("[0-9][0-9]_", ""), rs.getString(filterDTO.getHeader()));
                                }
                            }

                            result.add(row);
                        }

                        return result;
                    }
                });
    }

    @Override
    public Boolean executeDynamicQuerySaveToFile(QueryRequestDTO query, String tableName, String filePath) {
        log.info("Ejecución de consulta dinámica para guardado de archivo");
        return jdbcTemplate.execute(queryBuilder.prepareDynamicQuery(query, tableName),
                new PreparedStatementCallback<Boolean>() {
                    @Override
                    public Boolean doInPreparedStatement(PreparedStatement ps) {

                        FileOutputStream fileOutputStream = null;
                        OutputStreamWriter outputFile = null;
                        CSVWriter writerCSV = null;

                        try {

                            List<FilterDTO> filters = query.getFilters();

                            int paramIdx = 1;

                            if (!filters.isEmpty()) {
                                for (FilterDTO filterDTO : filters) {
                                    List<String> conditions = filterDTO.getConditions();

                                    if (conditions.isEmpty())
                                        continue;

                                    for (String condition : conditions) {
                                        ps.setString(paramIdx, condition);
                                        paramIdx++;
                                    }
                                }
                            }

                            ResultSet rs = ps.executeQuery();

                            fileOutputStream = new FileOutputStream(filePath);
                            outputFile = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                            writerCSV = new CSVWriter(outputFile);

                            writerCSV.writeAll(rs, true, false, true);

                        } catch (Exception e) {
                            log.error(e.getMessage());
                            return false;
                        } finally {
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                            }
                            if (outputFile != null) {
                                try {
                                    outputFile.close();
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                            }
                            if (writerCSV != null) {
                                try {
                                    writerCSV.close();
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                            }
                        }

                        return true;
                    }
                });
    }

    @Override
    public boolean downloadCommentsFile(List<Long> ids, String filePath) {
        log.info("Descarga de archivo de comentarios");
        return jdbcTemplate.execute(queryBuilder.downloadCommentsFile(ids), new PreparedStatementCallback<Boolean>() {
            @Override
            public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

                int paramIdx = 1;

                if (!ids.isEmpty()) {
                    for (Long id : ids) {
                        ps.setLong(paramIdx, id);
                        paramIdx++;
                    }
                }

                File file = new File(filePath);
                ResultSet rs = ps.executeQuery();

                CSVWriter writerCSV = null;

                try {

                    FileWriter outputFile = new FileWriter(file);
                    writerCSV = new CSVWriter(outputFile);

                    writerCSV.writeAll(rs, true, false, true);

                } catch (IOException e) {
                    log.error(e.getMessage());
                    return false;
                } finally {
                    if (writerCSV != null) {
                        try {
                            writerCSV.close();
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                }

                return true;

            }
        });
    }

    @Override
    public int totalRowCount(String tableName) {
        log.info("Obtención de número de filas total en la tabla");
        return jdbcTemplate.execute(queryBuilder.totalRowCount(tableName), new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

                ResultSet rs = ps.executeQuery();

                if (rs.next())
                    return (int) rs.getFloat(1);

                return 0;
            }
        });
    }

    @Override
    public List<String> getTableColumns(String tableName) {
        log.info("Busqueda de nombres de columnas de la tabla");
        return jdbcTemplate.execute(queryBuilder.getTableColumns(tableName),
                new PreparedStatementCallback<List<String>>() {
                    @Override
                    public List<String> doInPreparedStatement(PreparedStatement ps)
                            throws SQLException, DataAccessException {

                        ps.setString(1, tableName);

                        ResultSet resultSet = ps.executeQuery();
                        List<String> columns = new ArrayList<>();

                        while (resultSet.next()) {
                            columns.add(resultSet.getString("column_name"));
                        }

                        return columns;
                    }
                });
    }

    @Override
    public String getStartDate(String tableName) {
        log.info("Obtención de fecha inicial en la tabla");
        return jdbcTemplate.execute(queryBuilder.startDate(tableName), new PreparedStatementCallback<String>() {
            @Override
            public String doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

                ResultSet rs = ps.executeQuery();

                if (rs.next())
                    return rs.getString(1);

                return "";
            }
        });
    }

    @Override
    public String getEndDate(String tableName) {
        log.info("Obtención de fecha final en la tabla");
        return jdbcTemplate.execute(queryBuilder.endDate(tableName), new PreparedStatementCallback<String>() {
            @Override
            public String doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

                ResultSet rs = ps.executeQuery();

                if (rs.next())
                    return rs.getString(1);

                return "";
            }
        });
    }

    @Override
    public int countDynamicQueryRows(QueryRequestDTO query, String tableName) {
        log.info("Ejecución de conteo de filas en consulta dinámica");
        return jdbcTemplate.execute(queryBuilder.countRowsDynamicQuery(query, tableName),
                new PreparedStatementCallback<Integer>() {
                    @Override
                    public Integer doInPreparedStatement(PreparedStatement ps)
                            throws SQLException, DataAccessException {

                        List<FilterDTO> filters = query.getFilters();
                        int paramIdx = 1;
                        boolean filtersIsEmpty = filters.isEmpty();

                        if (!filtersIsEmpty) {
                            for (FilterDTO filterDTO : filters) {
                                List<String> conditions = filterDTO.getConditions();

                                if (conditions.isEmpty())
                                    continue;

                                for (String condition : conditions) {
                                    ps.setString(paramIdx, condition);
                                    paramIdx++;
                                }
                            }
                        }

                        ResultSet rs = ps.executeQuery();

                        if (rs.next())
                            return (int) rs.getFloat(1);

                        return 0;
                    }
                });
    }
}
