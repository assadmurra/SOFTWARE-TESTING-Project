package edu.swer312.data;

import java.util.List;

/**
 * A named group of test cases: one CSV file, or one sheet of an Excel workbook.
 * Each data set is turned into its own generated feature file.
 *
 * @param name source name shown in reports, e.g. {@code searches.csv} or {@code edge-cases.xlsx#negative}
 * @param rows the test cases, in file order
 */
public record DataSet(String name, List<SearchTestCase> rows) {
}
