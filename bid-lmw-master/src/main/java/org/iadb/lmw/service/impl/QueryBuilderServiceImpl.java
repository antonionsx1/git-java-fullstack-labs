package org.iadb.lmw.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.iadb.lmw.dto.FilterDTO;
import org.iadb.lmw.dto.LangRequestDto;
import org.iadb.lmw.dto.QueryRequestDTO;
import org.iadb.lmw.service.QueryBuilderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueryBuilderServiceImpl implements QueryBuilderService {

    @Value("${data.sql.page-size}")
    private int pageSize;
    @Value("${data.sql.lmw-primary-key}")
    private String lmwPK;
    @Value("${data.dataset.dates-col}")
    private String datesCol;

    @Override
    public String tableExists() {
        return "SELECT EXISTS("
                + "SELECT FROM "
                + "pg_tables "
                + "WHERE "
                + "schemaname = ? AND "
                + "tablename = ? "
                + ");";
    }

    @Override
    public String createTable(String tableName, List<String> columns) {

        String columnClause = "";

        for (String name : columns) {
            columnClause += name + " VARCHAR(2048) NOT NULL, ";
        }

        columnClause += "PRIMARY KEY (" + lmwPK + ")";

        return "CREATE TABLE " + tableName + " (" + columnClause + ");";

    }

    @Override
    public String deleteTable(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName + " CASCADE";
    }

    @Override
    public String insertValuesIntoTable(String tableName, List<String> columns, List<CSVRecord> values) {

        String columnsClause = "";
        String valuesClause = "";
        String nValues = "";
        String conflictClause = "";

        for (String column : columns) {
            columnsClause += column + ", ";
            nValues += "?, ";

            if (column.equals(lmwPK.toLowerCase())) continue;

            conflictClause += column + " = EXCLUDED." + column + ", ";

        }

        columnsClause = !columnsClause.isBlank() ? columnsClause.substring(0, columnsClause.length() - ", ".length()) : "";
        conflictClause = !conflictClause.isBlank() ? conflictClause.substring(0, conflictClause.length() - ", ".length()) : "";
        nValues = !nValues.isBlank() ? "(" + nValues.substring(0, nValues.length() - ", ".length()) + ")" : "";

        for (int i = 0; i < values.size(); i++) {
            valuesClause += nValues + ", ";
        }

        valuesClause = !valuesClause.isBlank() ? valuesClause.substring(0, valuesClause.length() - ", ".length()) : "";

        return "INSERT INTO " + tableName
                + " (" + columnsClause
                + ") VALUES "
                + valuesClause
                + " ON CONFLICT (" + lmwPK.toLowerCase() + ") DO UPDATE SET " + conflictClause + ";";
    }

    @Override
    public String getLMWTableColumns(LangRequestDto query) {

        return !query.getUseLang() ? "SELECT (front_end_header) FROM header;" :

                "SELECT (front_end_header) "
                        + "FROM header "
                        + "WHERE data_base_header LIKE '" + (query.getLang().equals("es") ? "es" : "en") + "_%' "
                        + "OR data_base_header = '" + lmwPK.toLowerCase() + "';";
    }

    @Override
    public String prepareDynamicQuery(QueryRequestDTO query, String tableName) {

        List<String> columnsNames = new ArrayList<>();
        List<FilterDTO> filters = query.getFilters();
        String columnsClause;
        StringBuilder whereClause = new StringBuilder(" WHERE ");
        String selectClause = "SELECT ";
        String betweenClause = query.getUseDates() ?
                "(" + datesCol + " BETWEEN '" + query.getStartDate() + "' AND '" + query.getEndDate() + "')" :
                "";
        String orderByClause = " ORDER BY " + lmwPK.toLowerCase() + " ASC ";
        String paginationClause = query.getUsePagination() ?
                " LIMIT " + pageSize + " OFFSET " + (query.getPageOffset() * pageSize) :
                "";

        if (query.getUseDistinct()) selectClause += "DISTINCT";

        if (filters.isEmpty()) return selectClause
                +
                (query.getUseDates() ?
                        "*, " + datesCol + " FROM " :
                        "* FROM ")
                + tableName
                + (query.getUseDates() ? " WHERE " + betweenClause : "")
                + orderByClause
                + paginationClause + ";";

        orderByClause = " ORDER BY " + filters.get(0).getHeader() + " ASC ";

        for (FilterDTO filter : filters) {

            String column = filter.getHeader();
            List<String> conditions = filter.getConditions();

            columnsNames.add(column);

            if (conditions.isEmpty()) continue;

            whereClause.append("( ");

            for (int i = 0; i < conditions.size(); i++) {
                whereClause.append(column).append(" = ? OR ");
            }

            whereClause = new StringBuilder(whereClause.substring(0, whereClause.length() - "OR ".length()) + ") AND ");
        }

        if (!whereClause.toString().equals(" WHERE ")) {

            whereClause = new StringBuilder(whereClause.substring(0, whereClause.length() - "AND ".length()));

            if (query.getUseDates()) {
                betweenClause = " AND " + betweenClause;
            } else {
                betweenClause = "";
            }

        } else {
            if (!query.getUseDates())
                whereClause = new StringBuilder();
            betweenClause = "";
        }

        columnsClause = columnsNames.toString().replace('[', ' ').replace(']', ' ');

        return selectClause
                + columnsClause
                + "FROM " + tableName
                + whereClause
                + betweenClause
                + orderByClause
                + paginationClause + ";";
    }

    @Override
    public String downloadCommentsFile(List<Long> ids) {

        if (ids.isEmpty()) return "SELECT * FROM comment;";

        StringBuilder whereClause = new StringBuilder("WHERE ");

        for (int i = 0; i < ids.size(); i++) {
            whereClause.append("id = ? OR ");
        }

        whereClause = new StringBuilder(whereClause.substring(0, whereClause.length() - "OR ".length()));

        return "SELECT * FROM comment " + whereClause + ";";
    }

    @Override
    public String totalRowCount(String tableName) {
        return "SELECT count(*) FROM " + tableName + ";";
    }

    @Override
    public String getTableColumns(String tableName) {

        return "SELECT column_name, data_type "
                + "FROM information_schema.columns "
                + "WHERE table_name = ?;";

    }

    @Override
    public String startDate(String tableName) {
        return "SELECT " + datesCol + " FROM latinmacrowatch ORDER BY " + datesCol + " ASC LIMIT 1;";
    }

    @Override
    public String endDate(String tableName) {
        return "SELECT " + datesCol + " FROM latinmacrowatch ORDER BY " + datesCol + " DESC LIMIT 1;";
    }

    @Override
    public String countRowsDynamicQuery(QueryRequestDTO query, String tableName) {
        query.setUsePagination(false);
        return "SELECT COUNT (*) FROM(" + (prepareDynamicQuery(query, tableName).replace(';', ' ')) + ") query_rows;";
    }

}
