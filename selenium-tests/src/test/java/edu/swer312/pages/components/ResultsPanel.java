package edu.swer312.pages.components;

import edu.swer312.pages.BasePage;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** The search result panel and the Live Data Panel (fringe, visited, traversal, path). */
public class ResultsPanel extends BasePage {

    // Locators: search result panel
    private static final By SEARCH_RESULTS = By.id("search-results");
    private static final By RESULT_FINISHED =
            By.cssSelector("#search-results .status-success, #search-results .status-failure");
    private static final By RESULT_SUCCESS = By.cssSelector("#search-results .status-success");
    private static final By RESULT_PENDING = By.cssSelector("#search-results .status-pending");
    private static final By PATH_COST = By.xpath("//*[@id='search-results']"
            + "//*[contains(@class,'result-metric')][span[1]='Path Cost:']/span[2]");
    private static final By NODES_EXPLORED = By.xpath("//*[@id='search-results']"
            + "//*[contains(@class,'result-metric')][span[1]='Nodes Explored:']/span[2]");
    private static final By CLEAR_PATH_BUTTON = By.id("btn-clear-path");

    // Locators: live data panel
    private static final By FRINGE_LIST = By.id("fringe-list");
    private static final By VISITED_LIST = By.id("visited-list");
    private static final By TRAVERSAL_LIST = By.id("traversal-list");
    private static final By PATH_LIST = By.id("path-list");
    private static final By PATH_ITEMS = By.cssSelector("#path-list .array-item");
    private static final By TRAVERSAL_ITEMS = By.cssSelector("#traversal-list .array-item");

    public ResultsPanel(WebDriver driver) {
        super(driver);
    }

    /** True once a success or failure result is shown (checked immediately, without waiting). */
    public boolean hasResult() {
        return !findAll(RESULT_FINISHED).isEmpty();
    }

    public boolean isSuccess() {
        return !findAll(RESULT_SUCCESS).isEmpty();
    }

    public String text() {
        return find(SEARCH_RESULTS).getText();
    }

    public boolean isWaitingForSearch() {
        return text().contains("Ready to start search") && path().isEmpty();
    }

    public List<String> path() {
        return findAll(PATH_ITEMS).stream().map(WebElement::getText).toList();
    }

    public List<String> traversal() {
        return findAll(TRAVERSAL_ITEMS).stream().map(WebElement::getText).toList();
    }

    public double pathCost() {
        return Double.parseDouble(find(PATH_COST).getText());
    }

    public int nodesExplored() {
        return Integer.parseInt(find(NODES_EXPLORED).getText().trim());
    }

    /** Clicks "Clear Path" and waits until the result panel is back to "Ready to start search". */
    public void clear() {
        click(CLEAR_PATH_BUTTON);
        wait.until(ExpectedConditions.presenceOfElementLocated(RESULT_PENDING));
    }

    /** True if nothing in the Live Data Panel changes during the whole period (negative wait). */
    public boolean liveDataStaysUnchanged(Duration period) {
        return staysUnchanged(this::liveDataSnapshot, period);
    }

    /** Everything the Live Data Panel currently shows; changes whenever the animation advances. */
    private String liveDataSnapshot() {
        return find(FRINGE_LIST).getText() + "|" + find(VISITED_LIST).getText() + "|"
                + find(TRAVERSAL_LIST).getText() + "|" + find(PATH_LIST).getText();
    }
}
