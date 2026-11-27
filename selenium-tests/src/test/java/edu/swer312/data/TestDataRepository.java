package edu.swer312.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loads every supported test-data file in a directory. Files are processed in
 * alphabetical order so the generated features are stable between runs.
 */
public class TestDataRepository {

    private final List<TestDataReader> readers = List.of(
            new CsvTestDataReader(),
            new ExcelTestDataReader());

    public List<DataSet> loadAll(Path dataDirectory) throws IOException {
        List<Path> files;
        try (Stream<Path> listing = Files.list(dataDirectory)) {
            files = listing.filter(Files::isRegularFile).sorted().toList();
        }

        List<DataSet> dataSets = new ArrayList<>();
        for (Path file : files) {
            for (TestDataReader reader : readers) {
                if (reader.supports(file)) {
                    dataSets.addAll(reader.read(file));
                    break;
                }
            }
        }
        return dataSets;
    }
}
