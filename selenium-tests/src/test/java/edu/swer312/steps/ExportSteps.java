package edu.swer312.steps;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.swer312.context.ScenarioContext;
import edu.swer312.pages.VisualizerPage;
import edu.swer312.support.Downloads;
import edu.swer312.support.GraphFixtures;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Saving and exporting: the downloaded files are opened and their content is checked. */
public class ExportSteps {

    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration NO_DOWNLOAD_PERIOD = Duration.ofSeconds(2);
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private final ScenarioContext context;
    private Path lastDownload;

    public ExportSteps(ScenarioContext context) {
        this.context = context;
    }

    private VisualizerPage page() {
        return context.getPage();
    }

    // ---------------------------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------------------------

    @When("I export the graph as {word}")
    public void iExportTheGraphAs(String format) {
        page().export(format.toLowerCase());
    }

    @When("I try to export the metrics as CSV")
    public void iTryToExportTheMetricsAsCsv() {
        page().export("csv");
        context.getAlerts().add(page().acceptDialog());
    }

    @When("I save the graph")
    public void iSaveTheGraph() {
        page().saveGraph();
    }

    @When("I load the downloaded file")
    public void iLoadTheDownloadedFile() throws IOException {
        context.recordAlert(page().load(lastDownload, GraphFixtures.expectedNodeCount(lastDownload)));
    }

    // ---------------------------------------------------------------------------------
    // Checking the downloaded file
    // ---------------------------------------------------------------------------------

    @Then("a file {string} is downloaded")
    public void aFileIsDownloaded(String glob) {
        lastDownload = Downloads.waitForFile(context.getDownloadFolder(), glob, DOWNLOAD_TIMEOUT);
    }

    @Then("no file is downloaded")
    public void noFileIsDownloaded() {
        Path folder = context.getDownloadFolder();
        assertTrue(Downloads.staysEmpty(folder, NO_DOWNLOAD_PERIOD),
                "Expected no download, but the folder contains " + Downloads.list(folder));
    }

    @SuppressWarnings("unchecked")
    @Then("the downloaded JSON has {int} nodes and the path {string} with cost {int}")
    public void theDownloadedJsonHas(int nodes, String path, int cost) throws IOException {
        Map<String, Object> json = Downloads.readJson(lastDownload);
        Map<String, Object> graph = (Map<String, Object>) json.get("graph");
        Map<String, Object> results = (Map<String, Object>) json.get("results");

        assertEquals(nodes, ((List<?>) graph.get("nodes")).size(), "nodes in export");
        assertNotNull(results, "export should contain the search results");
        assertEquals(Boolean.TRUE, results.get("success"));
        assertEquals(List.of(path.split(">")), results.get("path"));
        assertEquals(cost, ((Number) results.get("path_cost")).doubleValue(), 0.00001);
    }

    @Then("the downloaded CSV contains:")
    public void theDownloadedCsvContains(Map<String, String> expected) throws IOException {
        Map<String, String> metrics = new HashMap<>();
        for (String line : Files.readAllLines(lastDownload)) {
            String[] parts = line.split(",", 2);
            if (parts.length == 2) {
                metrics.put(parts[0].trim(), parts[1].trim());
            }
        }
        expected.forEach((metric, value) -> assertEquals(value, metrics.get(metric), "CSV metric '" + metric + "'"));
    }

    @Then("the downloaded file is a PNG image")
    public void theDownloadedFileIsAPngImage() throws IOException {
        byte[] start = Arrays.copyOf(Files.readAllBytes(lastDownload), PNG_SIGNATURE.length);
        assertArrayEquals(PNG_SIGNATURE, start, "File does not start with the PNG signature");
    }

    @Then("the downloaded SVG draws {int} nodes")
    public void theDownloadedSvgDrawsNodes(int nodes) throws IOException {
        String svg = Files.readString(lastDownload);
        assertTrue(svg.startsWith("<svg"), "Not an SVG document");
        assertEquals(nodes, svg.split("<circle", -1).length - 1, "circle elements in the SVG");
    }
}
