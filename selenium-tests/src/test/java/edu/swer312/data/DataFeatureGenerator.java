package edu.swer312.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Turns external test data (CSV and Excel) into Cucumber feature files.
 *
 * <p>Cucumber only reads {@code Examples:} rows from {@code .feature} files, so a
 * plain Scenario Outline would force us to copy every data row into Gherkin by hand.
 * Instead, Maven runs this class after the test classes are compiled (see the
 * {@code exec-maven-plugin} in {@code pom.xml}). It:
 * <ol>
 *   <li>reads every {@code .csv} file and every Excel sheet in the data folder,</li>
 *   <li>validates the rows (unknown algorithm, missing graph, duplicate id, ...),</li>
 *   <li>fills the Scenario Outline template with one {@code Examples:} row per data row, and</li>
 *   <li>writes one feature per data set into {@code target/test-classes/features/generated/}.</li>
 * </ol>
 * The result: adding a row, a sheet or a whole data file is enough for it to run,
 * and every row shows up as its own named scenario in the reports.
 *
 * <p>Usage: {@code DataFeatureGenerator <dataDir> <graphsDir> <templateFile> <outputDir>}
 */
public final class DataFeatureGenerator {

    private static final String GENERATED_SUFFIX = ".feature";

    /** Examples table columns, in display order, with how to read each one from a row. */
    private static final Map<String, Function<SearchTestCase, String>> EXAMPLE_COLUMNS = new LinkedHashMap<>();

    static {
        EXAMPLE_COLUMNS.put("id", SearchTestCase::id);
        EXAMPLE_COLUMNS.put("description", SearchTestCase::description);
        EXAMPLE_COLUMNS.put("graph", SearchTestCase::graph);
        EXAMPLE_COLUMNS.put("algorithm", SearchTestCase::algorithm);
        EXAMPLE_COLUMNS.put("depth", SearchTestCase::depth);
        EXAMPLE_COLUMNS.put("outcome", SearchTestCase::outcome);
        EXAMPLE_COLUMNS.put("path", SearchTestCase::path);
        EXAMPLE_COLUMNS.put("cost", SearchTestCase::cost);
        EXAMPLE_COLUMNS.put("traversal", SearchTestCase::traversal);
        EXAMPLE_COLUMNS.put("message", SearchTestCase::message);
        EXAMPLE_COLUMNS.put("row", row -> String.valueOf(row.rowNumber()));
    }

    private DataFeatureGenerator() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Usage: DataFeatureGenerator <dataDir> <graphsDir> <templateFile> <outputDir>");
        }
        Path dataDirectory = Path.of(args[0]);
        Path graphsDirectory = Path.of(args[1]);
        String template = Files.readString(Path.of(args[2]), StandardCharsets.UTF_8);
        Path outputDirectory = Path.of(args[3]);

        List<DataSet> dataSets = new TestDataRepository().loadAll(dataDirectory);

        List<String> errors = new TestDataValidator(graphsDirectory).validate(dataSets);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid test data:\n  - " + String.join("\n  - ", errors));
        }

        recreateDirectory(outputDirectory);
        int scenarioCount = 0;
        for (DataSet dataSet : dataSets) {
            if (dataSet.rows().isEmpty()) {
                continue;
            }
            Path featureFile = outputDirectory.resolve(fileNameFor(dataSet.name()));
            Files.writeString(featureFile, render(template, dataSet), StandardCharsets.UTF_8);
            scenarioCount += dataSet.rows().size();
            System.out.printf("[data] %-32s -> %s (%d scenarios)%n",
                    dataSet.name(), featureFile.getFileName(), dataSet.rows().size());
        }
        System.out.printf("[data] Generated %d data-driven scenarios from %d data sets%n",
                scenarioCount, dataSets.size());
    }

    /** Fills the template's {@code {{SOURCE}}} and {@code {{EXAMPLES}}} placeholders. */
    static String render(String template, DataSet dataSet) {
        return template
                .replace("{{SOURCE}}", dataSet.name())
                .replace("{{EXAMPLES}}", renderExamples(dataSet));
    }

    /**
     * Rows that share the same tags go into one tagged {@code Examples:} block, so tags
     * from the data file (e.g. {@code @negative}) can be used to filter test runs.
     */
    private static String renderExamples(DataSet dataSet) {
        Map<String, List<SearchTestCase>> rowsByTags = new LinkedHashMap<>();
        for (SearchTestCase row : dataSet.rows()) {
            String tags = String.join(" ", row.tags().trim().split("\\s+")).trim();
            rowsByTags.computeIfAbsent(tags, key -> new ArrayList<>()).add(row);
        }

        StringBuilder out = new StringBuilder();
        rowsByTags.forEach((tags, rows) -> {
            if (!tags.isEmpty()) {
                out.append("    ").append(tags).append('\n');
            }
            String label = tags.isEmpty() ? "untagged rows" : "rows tagged " + tags;
            out.append("    Examples: ").append(dataSet.name()).append(" - ").append(label).append('\n');
            out.append(renderTable(rows)).append('\n');
        });
        return out.toString().stripTrailing() + "\n";
    }

    /** Renders an aligned Gherkin table so the generated file is easy to read. */
    private static String renderTable(List<SearchTestCase> rows) {
        List<String> headers = new ArrayList<>(EXAMPLE_COLUMNS.keySet());
        List<List<String>> cells = new ArrayList<>();
        cells.add(headers);
        for (SearchTestCase row : rows) {
            cells.add(EXAMPLE_COLUMNS.values().stream().map(read -> escapeCell(read.apply(row))).toList());
        }

        int[] widths = new int[headers.size()];
        for (List<String> line : cells) {
            for (int c = 0; c < line.size(); c++) {
                widths[c] = Math.max(widths[c], line.get(c).length());
            }
        }

        StringBuilder table = new StringBuilder();
        for (List<String> line : cells) {
            table.append("      |");
            for (int c = 0; c < line.size(); c++) {
                table.append(' ').append(padRight(line.get(c), widths[c])).append(" |");
            }
            table.append('\n');
        }
        return table.toString();
    }

    /** Gherkin table cells must escape backslashes and pipes, and cannot contain line breaks. */
    private static String escapeCell(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|").replaceAll("[\\r\\n]+", " ");
    }

    private static String padRight(String value, int width) {
        return value + " ".repeat(width - value.length());
    }

    /** {@code edge-cases.xlsx#negative} becomes {@code edge-cases-xlsx-negative.feature}. */
    private static String fileNameFor(String dataSetName) {
        return dataSetName.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "") + GENERATED_SUFFIX;
    }

    /** Removes features from an earlier run so deleted data files do not leave stale scenarios behind. */
    private static void recreateDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (Stream<Path> walk = Files.walk(directory)) {
                for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
        Files.createDirectories(directory);
    }
}
