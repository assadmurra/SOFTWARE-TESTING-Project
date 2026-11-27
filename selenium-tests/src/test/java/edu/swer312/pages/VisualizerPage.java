package edu.swer312.pages;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object for the AI Search Algorithm Visualizer (a single-page application).
 *
 * <p>All locators are declared once, at the top of this class. All Selenium code - finding
 * elements, clicking, typing and waiting - lives in this class, so the step definitions only
 * describe WHAT the user does and WHAT should be true, never HOW it is done in the browser.
 *
 * <p>Waiting strategy: only explicit waits ({@link WebDriverWait}, {@link FluentWait} and
 * {@link ExpectedConditions}); no implicit waits and no {@code Thread.sleep}.
 */
public class VisualizerPage {

    // =====================================================================================
    // Locators
    // =====================================================================================

    // Graph statistics and file loading
    private static final By AVERAGE_DEGREE_STAT = By.id("stat-avg-degree");
    private static final By NODE_COUNT_STAT = By.id("stat-nodes");
    private static final By EDGE_COUNT_STAT = By.id("stat-edges");
    private static final By FILE_INPUT = By.id("file-input");
    private static final By SAVE_GRAPH_BUTTON = By.id("btn-save-graph");

    // Algorithm settings
    private static final By ALGORITHM_SELECT = By.id("algorithm-select");
    private static final By DEPTH_LIMIT_INPUT = By.id("depth-limit");
    private static final By ALGORITHM_INFO_TITLE = By.cssSelector("#algorithm-info h4");
    private static final By SPEED_SLIDER = By.id("speed-slider");
    private static final By SPEED_VALUE = By.id("speed-value");

    // Animation controls
    private static final By START_BUTTON = By.id("btn-start");
    private static final By PAUSE_BUTTON = By.id("btn-pause");
    private static final By STOP_BUTTON = By.id("btn-stop");
    private static final By STEP_FORWARD_BUTTON = By.id("btn-step-forward");
    private static final By STEP_BACK_BUTTON = By.id("btn-step-back");
    private static final By CLEAR_PATH_BUTTON = By.id("btn-clear-path");

    // Search result panel
    private static final By SEARCH_RESULTS = By.id("search-results");
    private static final By RESULT_FINISHED =
            By.cssSelector("#search-results .status-success, #search-results .status-failure");
    private static final By RESULT_SUCCESS = By.cssSelector("#search-results .status-success");
    private static final By RESULT_PENDING = By.cssSelector("#search-results .status-pending");
    private static final By PATH_COST = By.xpath("//*[@id='search-results']"
            + "//*[contains(@class,'result-metric')][span[1]='Path Cost:']/span[2]");
    private static final By NODES_EXPLORED = By.xpath("//*[@id='search-results']"
            + "//*[contains(@class,'result-metric')][span[1]='Nodes Explored:']/span[2]");

    // Live data panel
    private static final By FRINGE_LIST = By.id("fringe-list");
    private static final By VISITED_LIST = By.id("visited-list");
    private static final By TRAVERSAL_LIST = By.id("traversal-list");
    private static final By PATH_LIST = By.id("path-list");
    private static final By PATH_ITEMS = By.cssSelector("#path-list .array-item");
    private static final By TRAVERSAL_ITEMS = By.cssSelector("#traversal-list .array-item");

    // Canvas and dialogs
    private static final By CANVAS = By.id("graph-canvas");
    private static final By GRAPH_TYPE_DIALOG = By.id("graph-type-modal");
    private static final By NODE_NAME_DIALOG = By.id("node-name-modal");
    private static final By NODE_NAME_INPUT = By.id("node-name-input");
    private static final By NODE_NAME_OK_BUTTON = By.id("modal-btn-node-ok");
    private static final By NODE_NAME_CANCEL_BUTTON = By.id("modal-btn-node-cancel");

    // Graph type and theme
    private static final By GRAPH_TYPE_SELECT = By.id("graph-type-select");
    private static final By GRAPH_TYPE_INDICATOR = By.id("graph-type-text");
    private static final By THEME_TOGGLE = By.id("theme-toggle");
    private static final By PAGE_BODY = By.tagName("body");

    /** Animation control buttons by the label used in the feature files. */
    private static final Map<String, By> CONTROL_BUTTONS = Map.of(
            "Start", START_BUTTON,
            "Pause", PAUSE_BUTTON,
            "Stop", STOP_BUTTON,
            "Step forward", STEP_FORWARD_BUTTON,
            "Step back", STEP_BACK_BUTTON);

    /** Building tool button, e.g. "add-node", "add-edge", "set-goal", "set-source", "delete-node". */
    private static By toolButton(String tool) {
        return By.id("tool-" + tool);
    }

    /** Button in the "Choose Graph Type" dialog: "directed" or "undirected". */
    private static By graphTypeButton(String type) {
        return By.id("modal-btn-" + type);
    }

    /** Example graph button: "simple", "tree", "grid" or "weighted". */
    private static By exampleButton(String example) {
        return By.id("example-" + example);
    }

    /** Export button: "png", "svg", "json", "csv", "pdf", "gif" or "sequence". */
    private static By exportButton(String format) {
        return By.id("btn-export-" + format);
    }

    // =====================================================================================
    // Set-up and shared helpers
    // =====================================================================================

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(45);
    /** Normal start-up takes 2-7 s; 30 s means a request was lost, not that the app is slow. */
    private static final Duration APP_START_TIMEOUT = Duration.ofSeconds(30);
    private static final int APP_START_ATTEMPTS = 2;
    private static final Duration STATS_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration POLLING_INTERVAL = Duration.ofMillis(200);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    /**
     * @param driver  the browser of the current scenario
     * @param baseUrl address of the application, e.g. http://localhost:8000
     */
    public VisualizerPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    private WebElement find(By locator) {
        return driver.findElement(locator);
    }

    /** Waits until the element is clickable, scrolls it into view and clicks it. */
    private void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", element);
        element.click();
    }

    // =====================================================================================
    // Opening the application and loading graphs
    // =====================================================================================

    /**
     * Opens the application and waits until Brython has initialised it (it then writes 0.00
     * into the average-degree statistic). Brython loads its .py files with synchronous requests
     * that never time out, so if the simple test web server drops one connection under
     * parallel load the page hangs forever. In that case the page is reloaded once (logged);
     * a second failure fails the test.
     */
    public void open() {
        for (int attempt = 1; ; attempt++) {
            driver.get(baseUrl);
            try {
                new WebDriverWait(driver, APP_START_TIMEOUT)
                        .withMessage("Application initialization did not finish (attempt " + attempt + ")")
                        .until(ExpectedConditions.textToBe(AVERAGE_DEGREE_STAT, "0.00"));
                return;
            } catch (TimeoutException e) {
                if (attempt >= APP_START_ATTEMPTS) {
                    throw e;
                }
                System.err.println("[open] Application did not start within "
                        + APP_START_TIMEOUT.toSeconds() + " s, reloading once");
            }
        }
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

    public void saveGraph() {
        click(SAVE_GRAPH_BUTTON);
    }

    // =====================================================================================
    // Graph statistics
    // =====================================================================================

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

    // =====================================================================================
    // Choosing and running an algorithm
    // =====================================================================================

    /** Selects an algorithm; DLS and IDS also get the depth limit. */
    public void selectAlgorithm(String algorithm, int depth) {
        Select algorithmSelect = new Select(find(ALGORITHM_SELECT));
        // IDS reads the depth field too, but the app only shows that field while DLS is selected.
        algorithmSelect.selectByValue(algorithm.equals("ids") ? "dls" : algorithm);
        if (algorithm.equals("dls") || algorithm.equals("ids")) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(DEPTH_LIMIT_INPUT));
            find(DEPTH_LIMIT_INPUT).clear();
            find(DEPTH_LIMIT_INPUT).sendKeys(String.valueOf(depth));
        }
        if (algorithm.equals("ids")) {
            algorithmSelect.selectByValue("ids");
        }
    }

    public boolean isDepthControlVisible() {
        return find(DEPTH_LIMIT_INPUT).isDisplayed();
    }

    public String algorithmInfoTitle() {
        return find(ALGORITHM_INFO_TITLE).getText();
    }

    /**
     * Runs the search at the fastest speed and waits until a result is shown OR the
     * application shows a validation alert (no source, no goal, missing heuristics, ...).
     *
     * @return the alert text if the search was refused
     */
    public Optional<String> runSearch() {
        // Move the visible speed slider to the maximum with the keyboard (no fixed delays).
        find(SPEED_SLIDER).sendKeys(Keys.END);
        click(START_BUTTON);
        return waitForResult();
    }

    /** Waits until the animation has finished and a success or failure result is shown (or an alert opens). */
    public Optional<String> waitForResult() {
        return waitForAlertOr(d -> hasResult() && find(START_BUTTON).isEnabled(), "search result");
    }

    /** Clicks "Clear Path" and waits until the result panel is back to "Ready to start search". */
    public void clearResult() {
        click(CLEAR_PATH_BUTTON);
        wait.until(ExpectedConditions.presenceOfElementLocated(RESULT_PENDING));
    }

    // =====================================================================================
    // Reading the result
    // =====================================================================================

    /** True once a success or failure result is shown (checked immediately, without waiting). */
    public boolean hasResult() {
        return !driver.findElements(RESULT_FINISHED).isEmpty();
    }

    public boolean isSuccess() {
        return !driver.findElements(RESULT_SUCCESS).isEmpty();
    }

    public String resultText() {
        return find(SEARCH_RESULTS).getText();
    }

    public boolean isWaitingForSearch() {
        return resultText().contains("Ready to start search") && path().isEmpty();
    }

    public List<String> path() {
        return driver.findElements(PATH_ITEMS).stream().map(WebElement::getText).toList();
    }

    public List<String> traversal() {
        return driver.findElements(TRAVERSAL_ITEMS).stream().map(WebElement::getText).toList();
    }

    public double pathCost() {
        return Double.parseDouble(find(PATH_COST).getText());
    }

    public int nodesExplored() {
        return Integer.parseInt(find(NODES_EXPLORED).getText().trim());
    }

    // =====================================================================================
    // JavaScript alerts and confirm dialogs (the driver leaves them open so tests can read them)
    // =====================================================================================

    /** Waits for an alert or confirm dialog, accepts it (OK) and returns its text. */
    public String acceptDialog() {
        Alert dialog = wait.until(ExpectedConditions.alertIsPresent());
        String text = dialog.getText();
        dialog.accept();
        return text;
    }

    /** Waits for a confirm dialog, dismisses it (Cancel) and returns its text. */
    public String dismissDialog() {
        Alert dialog = wait.until(ExpectedConditions.alertIsPresent());
        String text = dialog.getText();
        dialog.dismiss();
        return text;
    }

    /**
     * Fluent wait that polls every 200 ms until either a JavaScript alert is open (accepted and
     * returned) or the given condition holds (empty result). A DOM lookup can throw
     * UnhandledAlertException at the moment an alert appears; that is ignored and the next poll
     * reads the alert.
     */
    private Optional<String> waitForAlertOr(Function<WebDriver, Boolean> condition, String what) {
        return new FluentWait<>(driver)
                .withTimeout(DEFAULT_TIMEOUT)
                .pollingEvery(POLLING_INTERVAL)
                .ignoring(UnhandledAlertException.class)
                .ignoring(NoSuchElementException.class)
                .withMessage("Timed out waiting for " + what + " or an alert")
                .until(d -> {
                    try {
                        Alert alert = d.switchTo().alert();
                        String text = alert.getText();
                        alert.accept();
                        return Optional.of(text);
                    } catch (NoAlertPresentException noAlert) {
                        return condition.apply(d) ? Optional.<String>empty() : null;
                    }
                });
    }

    // =====================================================================================
    // Canvas editing
    // =====================================================================================

    /** Clicks the canvas at an offset in pixels from its centre. */
    public void clickCanvas(int dx, int dy) {
        new Actions(driver).moveToElement(find(CANVAS), dx, dy).click().perform();
    }

    /** Selects a building tool, e.g. "add-node", "add-edge", "set-goal", "delete-node". */
    public void selectTool(String tool) {
        click(toolButton(tool));
        wait.until(d -> isToolActive(tool));
    }

    public boolean isToolActive(String tool) {
        return find(toolButton(tool)).getAttribute("class").contains("active");
    }

    /** Explicit wait for the "Choose Graph Type" dialog. @return true if it became visible */
    public boolean waitForGraphTypeDialog() {
        return waitUntilVisible(GRAPH_TYPE_DIALOG);
    }

    /**
     * Explicit wait for the "Name Your Node" dialog. The app opens it a moment after the
     * graph-type dialog closes, so slower browsers such as Firefox need this wait.
     *
     * @return true if it became visible
     */
    public boolean waitForNodeNameDialog() {
        return waitUntilVisible(NODE_NAME_DIALOG);
    }

    private boolean waitUntilVisible(By locator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException notShown) {
            return false;
        }
    }

    /** Answers the "Choose Graph Type" dialog shown before the first node: "directed" or "undirected". */
    public void chooseGraphType(String type) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(GRAPH_TYPE_DIALOG));
        click(graphTypeButton(type));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(GRAPH_TYPE_DIALOG));
    }

    /**
     * Types a name into the "Name Your Node" dialog and confirms it.
     *
     * @return the alert text if the application rejected the name (e.g. duplicate)
     */
    public Optional<String> nameNode(String name) {
        WebElement input = waitForNodeNameInputReady();
        input.clear();
        input.sendKeys(name);
        click(NODE_NAME_OK_BUTTON);
        return waitForAlertOr(d -> !find(NODE_NAME_DIALOG).isDisplayed(), "node name dialog to close");
    }

    public void cancelNodeName() {
        waitForNodeNameInputReady();
        click(NODE_NAME_CANCEL_BUTTON);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(NODE_NAME_DIALOG));
    }

    /**
     * The app focuses the name field after 50 ms and selects its default text after 100 ms.
     * Typing before that would let the delayed select() swallow part of our text, so wait
     * until the default text is fully selected.
     */
    private WebElement waitForNodeNameInputReady() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(NODE_NAME_DIALOG));
        WebElement input = find(NODE_NAME_INPUT);
        wait.until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript(
                "var i = arguments[0];"
                        + "return document.activeElement === i && i.value.length > 0"
                        + " && i.selectionStart === 0 && i.selectionEnd === i.value.length;",
                input)));
        return input;
    }

    // =====================================================================================
    // Animation / playback controls
    // =====================================================================================

    /** Sets the speed slider (1 = slowest, one animation step per second; 10 = fastest). */
    public void setSpeed(int speed) {
        WebElement slider = find(SPEED_SLIDER);
        slider.sendKeys(Keys.HOME);
        for (int i = 1; i < speed; i++) {
            slider.sendKeys(Keys.ARROW_RIGHT);
        }
        wait.until(d -> find(SPEED_VALUE).getText().equals(speed + "x"));
    }

    /** Clicks Start and waits until the animation is running (Pause becomes enabled). */
    public void startAnimation() {
        click(START_BUTTON);
        wait.until(d -> find(PAUSE_BUTTON).isEnabled());
    }

    public void pause() {
        click(PAUSE_BUTTON);
        wait.until(d -> pauseButtonText().contains("Resume"));
    }

    public void resume() {
        click(PAUSE_BUTTON);
        wait.until(d -> pauseButtonText().contains("Pause"));
    }

    public void stop() {
        click(STOP_BUTTON);
        wait.until(d -> find(START_BUTTON).isEnabled());
    }

    public void stepForward() {
        click(STEP_FORWARD_BUTTON);
    }

    public void stepBackward() {
        click(STEP_BACK_BUTTON);
    }

    public String pauseButtonText() {
        return find(PAUSE_BUTTON).getText();
    }

    /** @param label "Start", "Pause", "Stop", "Step forward" or "Step back" */
    public boolean isControlEnabled(String label) {
        By button = CONTROL_BUTTONS.get(label);
        if (button == null) {
            throw new IllegalArgumentException(
                    "Unknown control '" + label + "', expected one of " + CONTROL_BUTTONS.keySet());
        }
        return find(button).isEnabled();
    }

    /** Everything the Live Data Panel currently shows; changes whenever the animation advances. */
    public String liveDataSnapshot() {
        return find(FRINGE_LIST).getText() + "|" + find(VISITED_LIST).getText() + "|"
                + find(TRAVERSAL_LIST).getText() + "|" + find(PATH_LIST).getText();
    }

    /**
     * Negative wait: returns true if the value does NOT change during the whole period.
     * Implemented as an explicit wait for a change that is expected to time out (no sleep).
     */
    public boolean staysUnchanged(Supplier<String> value, Duration period) {
        String before = value.get();
        try {
            new WebDriverWait(driver, period, POLLING_INTERVAL).until(d -> !value.get().equals(before));
            return false;
        } catch (TimeoutException unchanged) {
            return true;
        }
    }

    // =====================================================================================
    // Examples, graph type, theme, exports and keyboard
    // =====================================================================================

    /** Clicks an example button. The app then asks for confirmation. */
    public void openExample(String example) {
        click(exampleButton(example));
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

    public String theme() {
        return find(PAGE_BODY).getAttribute("class");
    }

    public void toggleTheme() {
        click(THEME_TOGGLE);
    }

    public void export(String format) {
        click(exportButton(format));
    }

    /**
     * Presses a keyboard shortcut on the page itself (not in a form field), as a user would.
     *
     * @param key "Space" or a single letter, e.g. "E" (Add Edge tool) or "G" (Set Goal tool)
     */
    public void pressShortcut(String key) {
        CharSequence keys = key.equals("Space") ? Keys.SPACE : key.toLowerCase();
        ((JavascriptExecutor) driver).executeScript("if (document.activeElement) document.activeElement.blur();");
        new Actions(driver).sendKeys(keys).perform();
    }
}
