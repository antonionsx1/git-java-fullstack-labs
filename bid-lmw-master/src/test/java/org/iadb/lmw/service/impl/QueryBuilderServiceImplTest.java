package org.iadb.lmw.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.iadb.lmw.dto.FilterDTO;
import org.iadb.lmw.dto.LangRequestDto;
import org.iadb.lmw.dto.QueryRequestDTO;
import org.iadb.lmw.service.QueryBuilderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

@SpringBootTest
public class QueryBuilderServiceImplTest {

    @Autowired
    private QueryBuilderService queryBuilderService;

    private static String tableName;
    private String result;
    private static List<String> _columns;

    @BeforeAll
    static void setUp() throws Exception {

        tableName = "latinmacrowatch";
        _columns = new ArrayList<String>();

        _columns.add("col1");
        _columns.add("col2");
    }

    @Test
    public void testTableExists() {
        result = queryBuilderService.tableExists();

        Assert.hasText(result, "The query was not generated correctly");
    }

    @ParameterizedTest
    @MethodSource("columnsProvider")
    public void testCreateTable(List<String> columns) {
        result = queryBuilderService.createTable(tableName, columns);
        Assert.isTrue(result.contains(tableName), "Missing table name");

        if (columns.isEmpty())
            return;

        for (String column : columns) {
            Assert.isTrue(result.contains(column), "Missing column");
        }
    }

    static Stream<List<String>> columnsProvider() {
        return Stream.of(new ArrayList<String>(), _columns);
    }

    @Test
    public void testDeleteTable() {
        result = queryBuilderService.deleteTable(tableName);
        Assert.isTrue(result.contains(tableName), "Missing table name");
    }

    @ParameterizedTest
    @MethodSource("columnsAndValuesListsProvider")
    public void testInsertValuesIntoTable(List<String> columns, List<CSVRecord> values) {
        result = queryBuilderService.insertValuesIntoTable(tableName, columns, values);
        Assert.isTrue(result.contains(tableName), "Missing table name");

        if (columns.isEmpty())
            return;

        for (String column : columns) {
            Assert.isTrue(result.contains(column), "Missing column");
        }
    }

    static Stream<Arguments> columnsAndValuesListsProvider() {
        return Stream.of(
                Arguments.of(new ArrayList<String>(), new ArrayList<CSVRecord>()),
                Arguments.of(_columns, new ArrayList<CSVRecord>())
        );
    }

    @ParameterizedTest
    @MethodSource("langQueryProvider")
    public void testGetLMWTableColumns(LangRequestDto query) {
        result = queryBuilderService.getLMWTableColumns(query);
        Assert.hasText(result, "Bad request");
    }

    static Stream<LangRequestDto> langQueryProvider() {
        return Stream.of(
                new LangRequestDto(false, ""),
                new LangRequestDto(true, "es"),
                new LangRequestDto(true, "en")
        );
    }

    @ParameterizedTest
    @MethodSource("dynamicQueryProvider")
    public void testPrepareDynamicQuery(QueryRequestDTO query) {

        result = queryBuilderService.prepareDynamicQuery(query, tableName);

        Assert.hasText(result, "Bad request");
    }

    static Stream<QueryRequestDTO> dynamicQueryProvider() {

        List<FilterDTO> filtersEmptyConditions = new ArrayList<FilterDTO>();
        List<FilterDTO> filters = new ArrayList<FilterDTO>();
        List<String> conditions = new ArrayList<String>();

        filtersEmptyConditions.add(new FilterDTO("header", new ArrayList<String>()));

        conditions.add("value");

        filters.add(new FilterDTO("header", conditions));

        return Stream.of(
                new QueryRequestDTO(false, false, false, "", "", 0, new ArrayList<FilterDTO>()),
                new QueryRequestDTO(true, false, false, "", "", 0, new ArrayList<FilterDTO>()),
                new QueryRequestDTO(false, true, false, "date1", "date2", 0, new ArrayList<FilterDTO>()),
                new QueryRequestDTO(false, false, true, "", "", 0, new ArrayList<FilterDTO>()),
                new QueryRequestDTO(false, false, false, "", "", 0, new ArrayList<FilterDTO>()),
                new QueryRequestDTO(false, false, false, "", "", 0, filtersEmptyConditions),
                new QueryRequestDTO(false, false, true, "", "", 0, filtersEmptyConditions),
                new QueryRequestDTO(false, false, false, "", "", 0, filters)
        );
    }

    @ParameterizedTest
    @EmptySource
    @MethodSource("longIdsProvider")
    public void testDownloadCommentsFile(List<Long> ids) {

        result = queryBuilderService.downloadCommentsFile(ids);

        if (ids.isEmpty()) {
            Assert.isTrue(result.contains("comment;"), "Missing table name");
        } else {
            Assert.isTrue(StringUtils.countMatches(result, '?') == ids.size(), "Missing parameters in query");
        }

    }

    static Stream<List<Long>> longIdsProvider() {

        List<Long> ids = new ArrayList<>();

        ids.add(1L);
        ids.add(2L);

        return Stream.of(ids);
    }

    @Test
    public void testTotalRowCount() {

        result = queryBuilderService.totalRowCount(tableName);

        Assert.hasText(result, "Missing table name");
    }

    @Test
    public void testGetTableColumns() {

        result = queryBuilderService.getTableColumns(tableName);

        Assert.hasText(result, "Missing table name");
    }

    @Test
    public void testStartDate() {

        result = queryBuilderService.startDate(tableName);

        Assert.hasText(result, "Missing table name");
    }

    @Test
    public void testEndDate() {

        result = queryBuilderService.endDate(tableName);

        Assert.hasText(result, "Missing table name");
    }

    @ParameterizedTest
    @MethodSource("dynamicQueryProvider")
    public void testCountRowsDynamicQuery(QueryRequestDTO query) {

        result = queryBuilderService.countRowsDynamicQuery(query, tableName);

        Assert.hasText(result, "Missing table name");
    }

}
