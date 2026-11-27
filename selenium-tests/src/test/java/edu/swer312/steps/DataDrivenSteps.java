package edu.swer312.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.swer312.context.ScenarioContext;
import edu.swer312.pages.VisualizerPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.util.List;
import java.util.Map;

/**
 * Data-driven testing: the outcome check used by the features generated from the
 * CSV and Excel rows (see {@code edu.swer312.data.DataFeatureGenerator}).
 */
public class DataDrivenSteps {

    private final ScenarioContext context;

    public DataDrivenSteps(ScenarioContext context) {
        this.context = context;
    }

    /**
     * Compares the visible result with the expected outcome of one data row:
     * <ul>
     *   <li>found - goal reached with exactly this path and cost (and traversal, if given)</li>
     *   <li>no_path - a failure result with no path (and the given status message, if any)</li>
     *   <li>alert - the application refused with an alert containing the given message</li>
     * </ul>
     */
    @Then("the search outcome matches")
    public void theSearchOutcomeMatches(DataTable table) {
        VisualizerPage page = context.getPage();
        List<String> alerts = context.getAlerts();
        Map<String, String> expected = table.asMap(String.class, String.class);
        String outcome = cell(expected, "outcome");
        String message = cell(expected, "message");

        switch (outcome) {
            case "found" -> {
                assertTrue(alerts.isEmpty(), "Unexpected alert(s): " + alerts);
                SearchSteps.assertPathFound(page, cell(expected, "path"));
                assertEquals(Double.parseDouble(cell(expected, "cost")), page.pathCost(), 0.00001, "path cost");
            }
            case "no_path" -> {
                assertTrue(alerts.isEmpty(), "Unexpected alert(s): " + alerts);
                assertFalse(page.isSuccess(), "Expected no path, but got: " + page.resultText());
                assertTrue(page.path().isEmpty(), "Expected an empty path, but got " + page.path());
                if (!message.isEmpty()) {
                    assertTrue(page.resultText().contains(message),
                            "Status '" + page.resultText() + "' should contain '" + message + "'");
                }
            }
            case "alert" -> {
                assertTrue(alerts.stream().anyMatch(alert -> alert.contains(message)),
                        "Expected an alert containing '" + message + "', alerts were " + alerts);
                assertFalse(page.isSuccess(), "Search should not have succeeded after an alert");
            }
            default -> fail("Unknown outcome '" + outcome + "'");
        }

        // The traversal order is optional and can be checked for successful and failed searches.
        String traversal = cell(expected, "traversal");
        if (!traversal.isEmpty()) {
            assertEquals(List.of(traversal.split(">")), page.traversal(), "traversal");
        }
    }

    /** Empty Gherkin table cells arrive as null. */
    private static String cell(Map<String, String> row, String key) {
        String value = row.get(key);
        return value == null ? "" : value.trim();
    }
}
