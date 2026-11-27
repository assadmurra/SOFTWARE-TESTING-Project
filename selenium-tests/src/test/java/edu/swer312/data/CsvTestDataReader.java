package edu.swer312.data;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Reads test cases from a CSV file with a header row (see {@link SearchTestCase#COLUMNS}).
 * Blank lines are ignored and lines starting with {@code #} are treated as comments.
 */
public class CsvTestDataReader implements TestDataReader {

    private static final char UTF8_BOM = '﻿';

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setCommentMarker('#')
            .setTrim(true)
            .get();

    @Override
    public boolean supports(Path file) {
        return file.getFileName().toString().toLowerCase().endsWith(".csv");
    }

    @Override
    public List<DataSet> read(Path file) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        // Excel's "CSV UTF-8" export adds a byte-order mark that would corrupt the first header name.
        if (!content.isEmpty() && content.charAt(0) == UTF8_BOM) {
            content = content.substring(1);
        }

        String source = file.getFileName().toString();
        List<SearchTestCase> rows = new ArrayList<>();
        try (CSVParser parser = FORMAT.parse(new StringReader(content))) {
            for (CSVRecord record : parser) {
                // Record numbers start at 1 for the first data row; +1 accounts for the header row.
                int rowNumber = (int) record.getRecordNumber() + 1;
                rows.add(SearchTestCase.fromColumns(source, rowNumber, record.toMap()));
            }
        }
        return List.of(new DataSet(source, rows));
    }
}
