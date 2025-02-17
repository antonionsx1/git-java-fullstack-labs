package org.iadb.lmw.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.iadb.lmw.dao.DatasetBackupDao;
import org.iadb.lmw.dao.HeaderDao;
import org.iadb.lmw.dto.DatasetFileResponseDTO;
import org.iadb.lmw.dto.DatesResponseDto;
import org.iadb.lmw.dto.FilterDTO;
import org.iadb.lmw.dto.LangRequestDto;
import org.iadb.lmw.dto.QueryRequestDTO;
import org.iadb.lmw.dto.ReplaceDatasetRequestDto;
import org.iadb.lmw.dto.SingleResponse;
import org.iadb.lmw.dto.SummaryDTO;
import org.iadb.lmw.model.Admin;
import org.iadb.lmw.model.Backup;
import org.iadb.lmw.model.Header;
import org.iadb.lmw.service.AdminService;
import org.iadb.lmw.service.DynamicSQLQueryService;
import org.iadb.lmw.status.StatusCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Assert;

/**
 *
 */
@SpringBootTest
public class DataManagementServiceImplTest {

    @Value("${data.dataset.path}")
    private String path;
    @Value("${data.dataset.download-path}")
    private String downloadPath;
    @Value("${data.dataset.prefix}")
    private String prefix;
    @Value("${data.dataset.ext}")
    private String ext;

    @Mock
    DynamicSQLQueryService dynamicSQLQuery;
    @Mock
    DatasetBackupDao datasetBackupDao;
    @Mock
    HeaderDao headerDao;
    @Mock
    AdminService adminService;

    @InjectMocks
    private DataManagementServiceImpl dataManagement;
    private static String tableName;
    private static String schema;
    private static String[] csvContents = {
            "lmwUUID,Es_01_Área,En_01_Area,Es_02_Tema,En_02_Topic,Es_03_Indicador,En_03_Indicator,En_04_Unit,Es_04_Unidad,Es_Frecuencia,En_Frequency,Es_País,En_Country,Es_Fuente,En_Source,Es_Definición,En_Definition,Es_Otras Frecuencias,En_Other Frequencies,ES_Fecha,EN_Date,ES_Valor,EN_Value\r\n10101010101,01_Actividad Económica y Empleo,01_Economic Activity and Employment,01_Cuentas Nacionales,01_National Accounts,PIB,GDP,millions of US$,millones de US$,Anual,Annual,Argentina,Argentina,Cálculos de Latin Macro Watch basados en datos de Instituto Nacional de Estadística y Censos,Latin Macro Watch calculations based on Instituto Nacional de Estadística y Censos data,\"PIB, índice real. La definición para este indicador puede variar dependiendo del país.\",Real GDP index. The index is defined as GDP t / GDP base period * 100. The base period is 2004.,Trimestral,Quarterly,1990-03-01 0:00:00,1990-03-01 0:00:00,10,10",
            "lmwUUIDtest,Es_01_Área,En_01_Area,Es_02_Tema,En_02_Topic,Es_03_Indicador,En_03_Indicator,En_04_Unit,Es_04_Unidad,Es_Frecuencia,En_Frequency,Es_País,En_Country,Es_Fuente,En_Source,Es_Definición,En_Definition,Es_Otras Frecuencias,En_Other Frequencies,ES_Fecha,EN_Date,ES_Valor,EN_Value\r\n10101010101,01_Actividad Económica y Empleo,01_Economic Activity and Employment,01_Cuentas Nacionales,01_National Accounts,PIB,GDP,millions of US$,millones de US$,Anual,Annual,Argentina,Argentina,Cálculos de Latin Macro Watch basados en datos de Instituto Nacional de Estadística y Censos,Latin Macro Watch calculations based on Instituto Nacional de Estadística y Censos data,\"PIB, índice real. La definición para este indicador puede variar dependiendo del país.\",Real GDP index. The index is defined as GDP t / GDP base period * 100. The base period is 2004.,Trimestral,Quarterly,1990-03-01 0:00:00,1990-03-01 0:00:00,10,10",
            "lmwUUID,Es_01_Área,En_01_Area,Es_02_Tema,En_02_Topic,Es_03_Indicador,En_03_Indicator,En_04_Unit,Es_04_Unidad,Es_Frecuencia,En_Frequency,Es_País,En_Country,Es_Fuente,En_Source,Es_Definición,En_Definition,Es_Otras Frecuencias,En_Other Frequencies,ES_Fecha,EN_Date,ES_Valor,EN_Value\r\n",
            "lmwUUID,Es_01_Área,En_01_Area\r\n"
    };

	/**
	 *
	 */
    @BeforeAll
    public static void initAll() {
        tableName = "latinmacrowatch";
        schema = "public";
    }

	/**
	 *
	 */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        String pk = "lmwUUID";
        ReflectionTestUtils.setField(dataManagement, "lmwPK", pk);
        String ext = ".csv";
        ReflectionTestUtils.setField(dataManagement, "ext", ext);
        String backupsPath = "./LMW/Test/Datasets/Backups/";
        ReflectionTestUtils.setField(dataManagement, "backupPath", backupsPath);
    }

	/**
	 *
	 * @param content
	 */
    @ParameterizedTest
    @MethodSource("verifyFileContentPrivider")
    public void testVerify(String content) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                MediaType.TEXT_PLAIN_VALUE,
                content
                        .getBytes());

        try {
            SingleResponse<SummaryDTO> result = dataManagement.verify(file, path, prefix, ext);

            if (content.contains("lmwUUIDtest"))
                Assert.isTrue(result.getCode() == StatusCode.DB_COLUMNS_MISMATCH.getCode(), "Code error");
            else
                Assert.isTrue(result.getCode() == StatusCode.OK.getCode(), "Code error");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stream<String> verifyFileContentPrivider() {
        return Stream.of(
                csvContents[0],
                csvContents[1],
                csvContents[2]
        );
    }

    @ParameterizedTest
    @MethodSource("replaceRequestProvider")
    public void testReplaceDataset(String content, Admin admin, boolean tableExist, boolean tableCreated, boolean backupSaved) {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes());

        try {
            String fileName = dataManagement.verify(file, path, prefix, ext).getData().getFileName();
            ReplaceDatasetRequestDto request = new ReplaceDatasetRequestDto(fileName, "This is a replace comment.");

            when(adminService.findById(any(Integer.class)))
                    .thenReturn(admin);
            when(dynamicSQLQuery.tableExists(any(String.class), any(String.class)))
                    .thenReturn(tableExist);
            when(dynamicSQLQuery.createTable(any(String.class), any(ArrayList.class)))
                    .thenReturn(tableCreated);
            when(dynamicSQLQuery.executeDynamicQuerySaveToFile(any(QueryRequestDTO.class), any(String.class), any(String.class)))
                    .thenReturn(backupSaved);

            SingleResponse<SummaryDTO> result = dataManagement.replaceDataset(path, request, 0, ext, tableName, schema, prefix);
            if (!tableCreated) {
                Assert.isTrue(result.getCode() == StatusCode.DB_REPLACEMENT_ERROR.getCode(), "Code error");
                return;
            }
            if (!backupSaved) {
                Assert.isTrue(result.getCode() == StatusCode.DB_BACKUP_ERROR.getCode(), "Code error");
                return;
            }
            Assert.isTrue(result.getCode() == StatusCode.OK.getCode(), "Code error");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stream<Arguments> replaceRequestProvider() {
        Admin admin = new Admin();
        admin.setFullName("Admin Name");

        return Stream.of(
                Arguments.of(csvContents[0], admin, false, true, true),
                Arguments.of(csvContents[0], admin, true, true, false),
                Arguments.of(csvContents[0], admin, true, true, true),
                Arguments.of(csvContents[0], admin, true, false, true)
        );
    }

    @ParameterizedTest
    @MethodSource("updateRequestProvider")
    public void testUpdateDataset(String content, Admin admin, List<String> columns, boolean tableExist, boolean backupSaved) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes());
        try {
            String fileName = dataManagement.verify(file, path, prefix, ext).getData().getFileName();
            ReplaceDatasetRequestDto request = new ReplaceDatasetRequestDto(fileName, "This is a update comment.");

            when(adminService.findById(any(Integer.class)))
                    .thenReturn(admin);
            when(dynamicSQLQuery.tableExists(any(String.class), any(String.class)))
                    .thenReturn(tableExist);
            when(dynamicSQLQuery.getTableColumns(any(String.class)))
                    .thenReturn(columns);
            when(dynamicSQLQuery.executeDynamicQuerySaveToFile(any(QueryRequestDTO.class), any(String.class), any(String.class)))
                    .thenReturn(backupSaved);

            SingleResponse<SummaryDTO> result = dataManagement.updateDataset(path, request, 0, ext, tableName, schema, prefix);

            if (!tableExist) {
                Assert.isTrue(result.getCode() == StatusCode.DB_TABLE_NOT_FOUND.getCode(), "Code error");
                return;
            }

            if (columns.isEmpty()) {
                Assert.isTrue(result.getCode() == StatusCode.DB_COLUMNS_MISMATCH.getCode(), "Code error");
                return;
            }

            if (!backupSaved) {
                Assert.isTrue(result.getCode() != StatusCode.DB_BACKUP_ERROR.getCode(), "Code error");
                return;
            }

            Assert.isTrue(result.getCode() != StatusCode.OK.getCode(), "Code error");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stream<Arguments> updateRequestProvider() {
        Admin admin = new Admin();
        admin.setFullName("Admin Name");

        List<String> columns = new ArrayList<String>();
        columns.add("lmwuuid");
        columns.add("es_01_área");
        columns.add("en_01_area");

        return Stream.of(
                Arguments.of(csvContents[3], admin, columns, true, true),
                Arguments.of(csvContents[3], admin, new ArrayList<String>(), false, false),
                Arguments.of(csvContents[3], admin, new ArrayList<String>(), true, false),
                Arguments.of(csvContents[3], admin, columns, true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("columnsProvider")
    public void testGetStoredHeaders(List<String> columns) {

        LangRequestDto query = new LangRequestDto(true, "es");

        when(dynamicSQLQuery.getLMWTableColumns(any(LangRequestDto.class)))
                .thenReturn(columns);

        SingleResponse<SummaryDTO> result = dataManagement.getStoredHeaders(query);

        Assert.isTrue(result.getCode() == StatusCode.OK.getCode(), "Code error");
    }

    public static Stream<List<String>> columnsProvider() {

        List<String> columns = new ArrayList<String>();
        columns.add("lmwuuid");
        columns.add("es_01_área");
        columns.add("en_01_area");

        return Stream.of(
                columns
        );
    }

    @ParameterizedTest
    @MethodSource("headerProvider")
    public void testGetDataForVisualization(Optional<Header> header) {
        QueryRequestDTO query = new QueryRequestDTO();
        List<FilterDTO> filters = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        conditions.add("some condition");
        filters.add(new FilterDTO("header", new ArrayList<>()));
        filters.add(new FilterDTO("", new ArrayList<>()));
        filters.add(new FilterDTO("header2", conditions));
        query.setFilters(filters);

        List<HashMap<String, String>> rows = new ArrayList<>();

        when(dynamicSQLQuery.executeDynamicQuery(any(QueryRequestDTO.class), any(String.class)))
                .thenReturn(rows);
        when(dynamicSQLQuery.countDynamicQueryRows(any(QueryRequestDTO.class), any(String.class)))
                .thenReturn(10);
        when(dynamicSQLQuery.totalRowCount(any(String.class)))
                .thenReturn(100);
        when(headerDao.findByFrontEndHeader(any(String.class)))
                .thenReturn(header);

        SingleResponse<SummaryDTO> result = dataManagement.getDataForVisualization(query, tableName);

        Assert.isTrue(result.getCode() == StatusCode.OK.getCode(), "Code error");
    }

    public static Stream<Optional<Header>> headerProvider() {
        return Stream.of(
                Optional.empty(),
                Optional.of(new Header())
        );
    }

    @ParameterizedTest
    @MethodSource("downloadRequestProvider")
    public void testDownloadCSV(boolean fileSaved) {

        QueryRequestDTO query = new QueryRequestDTO();
        List<FilterDTO> filters = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        conditions.add("some condition");
        filters.add(new FilterDTO("header", new ArrayList<>()));
        filters.add(new FilterDTO("", new ArrayList<>()));
        filters.add(new FilterDTO("header2", conditions));
        query.setFilters(filters);

        when(dynamicSQLQuery.executeDynamicQuerySaveToFile(any(QueryRequestDTO.class), any(String.class), any(String.class)))
                .thenReturn(fileSaved);

        SingleResponse<DatasetFileResponseDTO> result = dataManagement.downloadCSV(query, tableName, downloadPath, prefix, ext);

        if (!fileSaved) {
            Assert.isTrue(result.getCode() == StatusCode.DB_BAD_QUERY_REQUEST.getCode(), "Code error");
            return;
        }

        Assert.isTrue(result.getCode() == StatusCode.DB_FILE_NOT_FOUND.getCode(), "Code error");
    }

    public static Stream<Arguments> downloadRequestProvider() {
        return Stream.of(
                Arguments.of(false),
                Arguments.of(true)
        );
    }


    @Test
    public void testListBackups() {
        List<Backup> testList = new ArrayList<Backup>();
        testList.add(new Backup());
        when(datasetBackupDao.findAll()).thenReturn(testList);
        List<Backup> result = dataManagement.listBackups();

        Assert.isTrue(!result.isEmpty(), "Es true !");
    }


    @Test
    public void testRestoreFromBackupById() {

        Backup backup = new Backup();
        backup.setFileName("Test name");
        backup.setTimeStamp(LocalDateTime.now());
        backup.setType("Test");

        when(datasetBackupDao.findById(any(Long.class)))
                .thenReturn(Optional.of(backup));
        SummaryDTO summary = new SummaryDTO();
        summary.setFileName("Backup name");
        summary.setRowsCount(0);
        summary.setMessage("OK");
        DataManagementServiceImpl spyDataManagement = Mockito.spy(dataManagement);
        doReturn(new SingleResponse<SummaryDTO>(StatusCode.OK.getCode(), summary))
                .when(spyDataManagement).replaceDataset(any(String.class), any(ReplaceDatasetRequestDto.class), any(Integer.class), any(String.class), any(String.class), any(String.class), any(String.class));

        SingleResponse<SummaryDTO> result = spyDataManagement.restoreFromBackupById(0L, 0, ext, tableName, schema, prefix);

        Assert.isTrue(result.getCode() == StatusCode.OK.getCode(), "Code error");
    }

    /*
        @Test
        public void testDownloadBackup() {
            throw new RuntimeException("not yet implemented");
        }
    */
    @Test
    public void testGetStoredDates() {
        when(dynamicSQLQuery.getStartDate(anyString())).thenReturn("Start");
        when(dynamicSQLQuery.getEndDate(anyString())).thenReturn("End");

        SingleResponse<DatesResponseDto> result = dataManagement.getStoredDates("tableName");

        Assert.isTrue(result.getData().getStartDate().equals("Start") && result.getData().getEndDate().equals("End"), "Missing dates");

    }

}
