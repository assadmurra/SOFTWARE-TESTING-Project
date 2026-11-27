package edu.swer312.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Strategy for reading one external test-data file format.
 * Add a new implementation (e.g. JSON or YAML) and register it in
 * {@link TestDataRepository} to support another format.
 */
public interface TestDataReader {

    /** @return true if this reader understands the given file (normally decided by extension). */
    boolean supports(Path file);

    /** Reads the file into one or more data sets (an Excel workbook yields one per sheet). */
    List<DataSet> read(Path file) throws IOException;
}
