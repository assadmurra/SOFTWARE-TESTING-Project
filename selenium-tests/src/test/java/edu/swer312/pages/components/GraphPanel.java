package edu.swer312.pages.components;

import edu.swer312.pages.BasePage;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Graph file loading, graph statistics, directed / undirected type and the example graphs. */
public class GraphPanel extends BasePage {

    // Locators
    private static final By FILE_INPUT = By.id("file-input");
    private static final By NODE_COUNT_STAT = By.id("stat-nodes");
    private static final By EDGE_COUNT_STAT = By.id("stat-edges");
    private static final By GRAPH_TYPE_SELECT = By.id("graph-type-select");
    private static final By GRAPH_TYPE_INDICATOR = By.id("graph-type-text");

    /** Example graph button: "simple", "tree", "grid" or "weighted". */
    private static By exampleButton(String example) {
        return By.id("example-" + example);
    }

    private static final Duration STATS_TIMEOUT = Duration.ofSeconds(10);

    public GraphPanel(WebDriver driver) {
        super(driver);
    }

    /**
     * Uploads a graph file and waits until it has loaded OR the application shows an alert
     * (malformed file). expectedNodes is empty for files that are not valid graphs.
     *
     * @return the alert text if the application rejected the file
     */
    public Optional<String> load(Path file, OptionalInt expectedNodes) {
        // sendKeys is Selenium's native file upload, and it also works on hidden file inputs.
        find(FILE_INPUT).sendKeys(file.toAbsolutePath().toString());
        return waitForAlertOr(d -> {
            String graphType = graphTypeIndicator();
            boolean typeShown = graphType.equals("Directed") || graphType.equals("Undirected");
            return typeShown
                    && expectedNodes.isPresent()
                    && nodeCount().equals(String.valueOf(expectedNodes.getAsInt()));
        }, "graph upload");
    }

    public String nodeCount() {
        return find(NODE_COUNT_STAT).getText();
    }

    public String edgeCount() {
        return find(EDGE_COUNT_STAT).getText();
    }

    /**
     * The statistics update asynchronously after dialogs, so wait (up to 10 s) until they
     * show the expected numbers. If they never do, return anyway: the caller's assertions
     * then report the actual values.
     */
    public void waitForStatistics(int nodes, int edges) {
        try {
            new WebDriverWait(driver, STATS_TIMEOUT).until(d ->
                    nodeCount().equals(String.valueOf(nodes)) && edgeCount().equals(String.valueOf(edges)));
        } catch (TimeoutException ignored) {
            // fall through to the caller's assertions
        }
    }

    /** Chooses "directed" or "undirected" in the graph-type drop-down (opens a confirm dialog). */
    public void selectGraphType(String type) {
        new Select(find(GRAPH_TYPE_SELECT)).selectByValue(type);
    }

    public String selectedGraphType() {
        return new Select(find(GRAPH_TYPE_SELECT)).getFirstSelectedOption().getAttribute("value");
    }

    /** Text of the "Graph Type:" indicator, e.g. "Directed"; empty while the indicator is hidden. */
    public String graphTypeIndicator() {
        return find(GRAPH_TYPE_INDICATOR).getText();
    }

    /** Clicks an example button. The app then asks for confirmation. */
    public void openExample(String example) {
        click(exampleButton(example));
    }
}
