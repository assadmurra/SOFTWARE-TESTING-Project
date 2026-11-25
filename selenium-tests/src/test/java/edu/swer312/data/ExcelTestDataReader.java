package edu.swer312.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Reads test cases from an Excel workbook using Apache POI.
 *
 * <ul>
 *   <li>Every sheet becomes its own {@link DataSet}, named {@code workbook.xlsx#sheet}.</li>
 *   <li>Sheets whose name starts with {@code _} (e.g. {@code _README}) are documentation and are skipped.</li>
 *   <li>The first row of each sheet is the header row; completely blank rows are ignored.</li>
 * </ul>
 */
public class ExcelTestDataReader implements TestDataReader {

    /** Formats cells exactly as Excel displays them, so the number 5 is read as "5", not "5.0". */
    private final DataFormatter formatter = new DataFormatter();

    @Override
    public boolean supports(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return name.endsWith(".xlsx") && !name.startsWith("~$"); // ~$ = Excel's lock file while open
    }

    @Override
    public List<DataSet> read(Path file) throws IOException {
        String workbookName = file.getFileName().toString();
        List<DataSet> dataSets = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.toFile(), null, true)) {
            for (Sheet sheet : workbook) {
                if (sheet.getSheetName().startsWith("_")) {
                    continue;
                }
                String source = workbookName + "#" + sheet.getSheetName();
                dataSets.add(new DataSet(source, readSheet(sheet, source)));
            }
        }
        return dataSets;
    }

    private List<SearchTestCase> readSheet(Sheet sheet, String source) {
        List<SearchTestCase> rows = new ArrayList<>();
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        if (headerRow == null) {
            return rows;
        }
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(formatter.formatCellValue(cell).trim().toLowerCase());
        }

        for (int r = headerRow.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            Map<String, String> columns = readRow(row, headers);
            if (columns.values().stream().allMatch(String::isBlank)) {
                continue;
            }
            // Excel shows rows starting from 1, so report the row number the way a person sees it.
            rows.add(SearchTestCase.fromColumns(source, r + 1, columns));
        }
        return rows;
    }

    private Map<String, String> readRow(Row row, List<String> headers) {
        Map<String, String> columns = new HashMap<>();
        for (int c = 0; c < headers.size(); c++) {
            Cell cell = row == null ? null : row.getCell(c);
            columns.put(headers.get(c), cell == null ? "" : formatter.formatCellValue(cell));
        }
        return columns;
    }
}
