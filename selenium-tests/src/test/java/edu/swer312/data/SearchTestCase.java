package edu.swer312.data;

import java.util.List;
import java.util.Map;

/**
 * One row of external test data. The same record is produced whether the row
 * came from a CSV file or from an Excel sheet, so the rest of the framework never
 * needs to know which file format was used.
 *
 * @param source      human-readable origin, e.g. {@code searches.csv} or {@code edge-cases.xlsx#negative}
 * @param rowNumber   row number in the source file (1 = header row), used for traceability in reports
 * @param id          unique test-case id, becomes part of the scenario name
 * @param description what the row verifies, becomes part of the scenario name
 * @param graph       graph fixture name (file {@code graphs/<graph>.json})
 * @param algorithm   value of the algorithm drop-down, e.g. {@code bfs}, {@code astar}
 * @param depth       depth limit used by DLS / IDS (defaults to 5 when blank)
 * @param outcome     expected outcome: {@code found}, {@code no_path} or {@code alert}
 * @param path        expected path as node names separated by {@code >} (outcome found)
 * @param cost        expected path cost (outcome found)
 * @param traversal   optional expected traversal order, separated by {@code >}
 * @param message     expected status text (no_path) or alert text (alert); matched as a substring
 * @param tags        space-separated Cucumber tags, e.g. {@code @algorithm @smoke}
 */
public record SearchTestCase(
        String source,
        int rowNumber,
        String id,
        String description,
        String graph,
        String algorithm,
        String depth,
        String outcome,
        String path,
        String cost,
        String traversal,
        String message,
        String tags) {

    /** Column headers every data source must provide (order does not matter). */
    public static final List<String> COLUMNS = List.of(
            "id", "description", "graph", "algorithm", "depth",
            "outcome", "path", "cost", "traversal", "message", "tags");

    public static final String DEFAULT_DEPTH = "5";

    /**
     * Builds a test case from a header-to-value map. Missing or blank values become
     * empty strings; a blank depth becomes {@link #DEFAULT_DEPTH}.
     */
    public static SearchTestCase fromColumns(String source, int rowNumber, Map<String, String> columns) {
        String depth = value(columns, "depth");
        return new SearchTestCase(
                source,
                rowNumber,
                value(columns, "id"),
                value(columns, "description"),
                value(columns, "graph"),
                value(columns, "algorithm").toLowerCase(),
                depth.isEmpty() ? DEFAULT_DEPTH : depth,
                value(columns, "outcome").toLowerCase(),
                value(columns, "path"),
                value(columns, "cost"),
                value(columns, "traversal"),
                value(columns, "message"),
                value(columns, "tags"));
    }

    private static String value(Map<String, String> columns, String key) {
        String raw = columns.get(key);
        return raw == null ? "" : raw.trim();
    }
}
