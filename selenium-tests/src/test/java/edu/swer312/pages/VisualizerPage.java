package edu.swer312.pages;

import edu.swer312.pages.components.AlgorithmPanel;
import edu.swer312.pages.components.CanvasComponent;
import edu.swer312.pages.components.ExportMenu;
import edu.swer312.pages.components.GraphPanel;
import edu.swer312.pages.components.PlaybackControls;
import edu.swer312.pages.components.ResultsPanel;
import java.time.Duration;
import java.util.Optional;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object for the AI Search Algorithm Visualizer (a single-page application).
 *
 * <p>The page is <b>composed</b> of components, one per area of the screen. Each component
 * declares its own locators and contains all Selenium code for that area:
 * <ul>
 *   <li>{@link GraphPanel} - loading graph files, statistics, graph type, example graphs</li>
 *   <li>{@link CanvasComponent} - drawing nodes and edges, building tools, node dialogs</li>
 *   <li>{@link AlgorithmPanel} - algorithm drop-down, depth limit, info panel</li>
 *   <li>{@link PlaybackControls} - speed, Start, Pause / Resume, Stop, step buttons</li>
 *   <li>{@link ResultsPanel} - search result and Live Data Panel</li>
 *   <li>{@link ExportMenu} - save and export buttons</li>
 * </ul>
 * This class keeps the actions that belong to the whole page (opening it, running a search,
 * which uses three components, browser dialogs, theme and keyboard shortcuts).
 */
public class VisualizerPage extends BasePage {

    // Locators (page level)
    private static final By AVERAGE_DEGREE_STAT = By.id("stat-avg-degree");
    private static final By THEME_TOGGLE = By.id("theme-toggle");
    private static final By PAGE_BODY = By.tagName("body");

    /** Normal start-up takes 2-7 s; 30 s means a request was lost, not that the app is slow. */
    private static final Duration APP_START_TIMEOUT = Duration.ofSeconds(30);
    private static final int APP_START_ATTEMPTS = 2;

    private final String baseUrl;
    private final GraphPanel graph;
    private final CanvasComponent canvas;
    private final AlgorithmPanel algorithm;
    private final PlaybackControls playback;
    private final ResultsPanel results;
    private final ExportMenu exports;

    /**
     * @param driver  the browser of the current scenario
     * @param baseUrl address of the application, e.g. http://localhost:8000
     */
    public VisualizerPage(WebDriver driver, String baseUrl) {
        super(driver);
        this.baseUrl = baseUrl;
        this.graph = new GraphPanel(driver);
        this.canvas = new CanvasComponent(driver);
        this.algorithm = new AlgorithmPanel(driver);
        this.playback = new PlaybackControls(driver);
        this.results = new ResultsPanel(driver);
        this.exports = new ExportMenu(driver);
    }

    // =====================================================================================
    // Components
    // =====================================================================================

    public GraphPanel graph() {
        return graph;
    }

    public CanvasComponent canvas() {
        return canvas;
    }

    public AlgorithmPanel algorithm() {
        return algorithm;
    }

    public PlaybackControls playback() {
        return playback;
    }

    public ResultsPanel results() {
        return results;
    }

    public ExportMenu exports() {
        return exports;
    }

    // =====================================================================================
    // Page-level actions
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
     * Runs the search at the fastest speed and waits until a result is shown OR the
     * application shows a validation alert (no source, no goal, missing heuristics, ...).
     *
     * @return the alert text if the search was refused
     */
    public Optional<String> runSearch() {
        playback.setFastestSpeed();
        playback.clickStart();
        return waitForResult();
    }

    /** Waits until the animation has finished and a success or failure result is shown (or an alert opens). */
    public Optional<String> waitForResult() {
        return waitForAlertOr(d -> results.hasResult() && playback.isStartEnabled(), "search result");
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

    // =====================================================================================
    // Theme and keyboard
    // =====================================================================================

    public String theme() {
        return find(PAGE_BODY).getAttribute("class");
    }

    public void toggleTheme() {
        click(THEME_TOGGLE);
    }

    /**
     * Presses a keyboard shortcut on the page itself (not in a form field), as a user would.
     *
     * @param key "Space" or a single letter, e.g. "E" (Add Edge tool) or "G" (Set Goal tool)
     */
    public void pressShortcut(String key) {
        CharSequence keys = key.equals("Space") ? Keys.SPACE : key.toLowerCase();
        executeScript("if (document.activeElement) document.activeElement.blur();");
        new Actions(driver).sendKeys(keys).perform();
    }
}
