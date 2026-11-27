package edu.swer312.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks the external test data before any browser starts, so a typo in a CSV or
 * Excel row fails the build quickly with a clear message instead of producing a
 * confusing UI failure later.
 */
public class TestDataValidator {

    public static final Set<String> ALGORITHMS =
            Set.of("bfs", "dfs", "dls", "ids", "ucs", "bidirectional", "greedy", "astar");
    public static final Set<String> OUTCOMES = Set.of("found", "no_path", "alert");

    /** Same limits as the application's depth input ({@code min="1" max="20"}), plus 0 as a boundary. */
    private static final int MIN_DEPTH = 0;
    private static final int MAX_DEPTH = 20;

    private static final Pattern ID = Pattern.compile("[A-Za-z0-9._-]+");
    private static final Pattern NODE_SEQUENCE = Pattern.compile("[^>\\s]+(>[^>\\s]+)*");
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern TAG = Pattern.compile("@[A-Za-z0-9_-]+");

    private final Path graphsDirectory;

    public TestDataValidator(Path graphsDirectory) {
        this.graphsDirectory = graphsDirectory;
    }

    /** @return a list of human-readable problems; empty when all data is valid. */
    public List<String> validate(List<DataSet> dataSets) {
        List<String> errors = new ArrayList<>();
        Map<String, String> firstLocationOfId = new HashMap<>();

        for (DataSet dataSet : dataSets) {
            for (SearchTestCase row : dataSet.rows()) {
                String where = row.source() + " row " + row.rowNumber() + " (" + row.id() + "): ";
                validateRow(row, where, errors);

                String previous = firstLocationOfId.putIfAbsent(row.id(), row.source() + " row " + row.rowNumber());
                if (previous != null) {
                    errors.add(where + "duplicate id, already used at " + previous);
                }
            }
        }
        return errors;
    }

    private void validateRow(SearchTestCase row, String where, List<String> errors) {
        if (!ID.matcher(row.id()).matches()) {
            errors.add(where + "id must use only letters, digits, '.', '_' or '-'");
        }
        if (row.description().isEmpty()) {
            errors.add(where + "description is required");
        }
        if (row.graph().isEmpty() || !Files.isRegularFile(graphsDirectory.resolve(row.graph() + ".json"))) {
            errors.add(where + "graph fixture '" + row.graph() + ".json' not found in " + graphsDirectory);
        }
        if (!ALGORITHMS.contains(row.algorithm())) {
            errors.add(where + "unknown algorithm '" + row.algorithm() + "', expected one of " + ALGORITHMS);
        }
        validateDepth(row, where, errors);
        validateOutcome(row, where, errors);
        if (!row.traversal().isEmpty() && !NODE_SEQUENCE.matcher(row.traversal()).matches()) {
            errors.add(where + "traversal must look like S>A>G");
        }
        for (String tag : row.tags().split("\\s+")) {
            if (!tag.isEmpty() && !TAG.matcher(tag).matches()) {
                errors.add(where + "tag '" + tag + "' must look like @name");
            }
        }
    }

    private void validateDepth(SearchTestCase row, String where, List<String> errors) {
        try {
            int depth = Integer.parseInt(row.depth());
            if (depth < MIN_DEPTH || depth > MAX_DEPTH) {
                errors.add(where + "depth must be between " + MIN_DEPTH + " and " + MAX_DEPTH);
            }
        } catch (NumberFormatException e) {
            errors.add(where + "depth '" + row.depth() + "' is not a whole number");
        }
    }

    private void validateOutcome(SearchTestCase row, String where, List<String> errors) {
        if (!OUTCOMES.contains(row.outcome())) {
            errors.add(where + "unknown outcome '" + row.outcome() + "', expected one of " + OUTCOMES);
            return;
        }
        switch (row.outcome()) {
            case "found" -> {
                if (!NODE_SEQUENCE.matcher(row.path()).matches()) {
                    errors.add(where + "outcome 'found' needs a path like S>A>G");
                }
                if (!NUMBER.matcher(row.cost()).matches()) {
                    errors.add(where + "outcome 'found' needs a numeric cost");
                }
            }
            case "no_path" -> {
                if (!row.path().isEmpty() || !row.cost().isEmpty()) {
                    errors.add(where + "outcome 'no_path' must leave path and cost empty");
                }
            }
            case "alert" -> {
                if (row.message().isEmpty()) {
                    errors.add(where + "outcome 'alert' needs the expected alert message");
                }
            }
            default -> throw new IllegalStateException(row.outcome());
        }
    }
}
