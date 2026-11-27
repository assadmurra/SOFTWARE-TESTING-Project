package edu.swer312.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.swer312.context.ScenarioContext;
import edu.swer312.pages.VisualizerPage;
import edu.swer312.support.GraphFixtures;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Steps shared by most features: open the app, load a graph, run a search and check the result. */
public class SearchSteps {

    private final ScenarioContext context;
    private int rememberedNodesExplored;
    private double rememberedCost;

    public SearchSteps(ScenarioContext context) {
        this.context = context;
    }

    private VisualizerPage page() {
        return context.getPage();
    }

    // ---------------------------------------------------------------------------------
    // Opening the app and loading graphs
    // ---------------------------------------------------------------------------------

    @Given("the visualizer is ready")
    public void theVisualizerIsReady() {
        page().open();
    }

    @Given("graph {string} is loaded")
    public void graphIsLoaded(String name) throws Exception {
        // Each scenario uploads its own temporary copy, deleted once the app has read it.
        Path tempFile = GraphFixtures.copyToTempFile(name);
        try {
            context.recordAlert(page().load(tempFile, GraphFixtures.expectedNodeCount(name)));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Then("the graph has {int} nodes and {int} edges")
    public void theGraphHasNodesAndEdges(int nodes, int edges) {
        page().waitForStatistics(nodes, edges);
        assertEquals(String.valueOf(nodes), page().nodeCount(), "nodes");
        assertEquals(String.valueOf(edges), page().edgeCount(), "edges");
    }

    // ---------------------------------------------------------------------------------
    // Running a search
    // ---------------------------------------------------------------------------------

    @When("I run {string} with depth {int}")
    public void iRunWithDepth(String algorithm, int depth) {
        page().selectAlgorithm(algorithm, depth);
        context.recordAlert(page().runSearch());
    }

    @When("I start the search")
    public void iStartTheSearch() {
        context.recordAlert(page().runSearch());
    }

    @When("I clear the result")
    public void iClearTheResult() {
        page().clearResult();
    }

    // ---------------------------------------------------------------------------------
    // Checking the result
    // ---------------------------------------------------------------------------------

    @Then("the path is {string} with cost {int}")
    public void thePathIsWithCost(String path, int cost) {
        assertPathFound(page(), path);
        assertEquals(cost, page().pathCost(), 0.00001, "path cost");
    }

    @Then("no route is reported")
    public void noRouteIsReported() {
        String status = page().resultText();
        assertFalse(page().isSuccess(), status);
        assertTrue(page().path().isEmpty(), status);
        assertTrue(status.toLowerCase().matches("(?s).*(no path|depth|limit|not found).*"), status);
    }

    @Then("the traversal is {string}")
    public void theTraversalIs(String traversal) {
        assertEquals(List.of(traversal.split(">")), page().traversal(), "traversal");
    }

    @Then("the result is ready for another search")
    public void theResultIsReadyForAnotherSearch() {
        assertTrue(page().isWaitingForSearch(), "Result panel shows: " + page().resultText());
    }

    @Then("the search was not started")
    public void theSearchWasNotStarted() {
        assertTrue(page().isWaitingForSearch(),
                "Result panel should still be waiting, but shows: " + page().resultText());
        assertTrue(page().isControlEnabled("Start"), "Start should still be enabled");
    }

    // ---------------------------------------------------------------------------------
    // Alerts
    // ---------------------------------------------------------------------------------

    @Then("an alert says {string}")
    public void anAlertSays(String expected) {
        List<String> alerts = context.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains(expected)),
                "Expected an alert containing '" + expected + "', alerts were " + alerts);
    }

    // ---------------------------------------------------------------------------------
    // Comparing two runs on the same graph
    // ---------------------------------------------------------------------------------

    @When("I remember the result")
    public void iRememberTheResult() {
        rememberedNodesExplored = page().nodesExplored();
        rememberedCost = page().pathCost();
    }

    @Then("fewer nodes are explored than before")
    public void fewerNodesAreExploredThanBefore() {
        assertTrue(page().nodesExplored() < rememberedNodesExplored,
                "Explored " + page().nodesExplored() + " nodes, before: " + rememberedNodesExplored);
    }

    @Then("the path cost is higher than before")
    public void thePathCostIsHigherThanBefore() {
        assertTrue(page().pathCost() > rememberedCost,
                "Cost " + page().pathCost() + ", before: " + rememberedCost);
    }

    /** A successful result whose path is exactly the given nodes, e.g. "S>B>C>G". */
    static void assertPathFound(VisualizerPage page, String path) {
        assertTrue(page.isSuccess(), page.resultText());
        assertEquals(List.of(path.split(">")), page.path(), "path");
    }
}
