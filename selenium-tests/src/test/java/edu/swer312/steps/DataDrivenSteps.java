package edu.swer312.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.swer312.context.ScenarioContext;
import edu.swer312.pages.components.ResultsPanel;
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
        ResultsPanel results = context.getPage().results();
        List<String> alerts = context.getAlerts();
        Map<String, String> expected = table.asMap(String.class, String.class);
        String outcome = cell(expected, "outcome");
        String message = cell(expected, "message");

        switch (outcome) {
            case "found" -> {
                assertTrue(alerts.isEmpty(), "Unexpected alert(s): " + alerts);
                SearchSteps.assertPathFound(results, cell(expected, "path"));
                assertEquals(Double.parseDouble(cell(expected, "cost")), results.pathCost(), 0.00001, "path cost");
            }
            case "no_path" -> {
                assertTrue(alerts.isEmpty(), "Unexpected alert(s): " + alerts);
                assertFalse(results.isSuccess(), "Expected no path, but got: " + results.text());
                assertTrue(results.path().isEmpty(), "Expected an empty path, but got " + results.path());
                if (!message.isEmpty()) {
                    assertTrue(results.text().contains(message),
                            "Status '" + results.text() + "' should contain '" + message + "'");
                }
            }
            case "alert" -> {
                assertTrue(alerts.stream().anyMatch(alert -> alert.contains(message)),
                        "Expected an alert containing '" + message + "', alerts were " + alerts);
                assertFalse(results.isSuccess(), "Search should not have succeeded after an alert");
            }
            default -> fail("Unknown outcome '" + outcome + "'");
        }

        // The traversal order is optional and can be checked for successful and failed searches.
        String traversal = cell(expected, "traversal");
        if (!traversal.isEmpty()) {
            assertEquals(List.of(traversal.split(">")), results.traversal(), "traversal");
        }
    }

    /** Empty Gherkin table cells arrive as null. */
    private static String cell(Map<String, String> row, String key) {
        String value = row.get(key);
        return value == null ? "" : value.trim();
    }
}
