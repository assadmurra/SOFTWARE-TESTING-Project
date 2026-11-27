package edu.swer312.context;

import edu.swer312.pages.VisualizerPage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.WebDriver;

/**
 * Everything the steps of ONE scenario share: its browser, its page object, the alerts
 * the application showed and its download folder.
 *
 * <p>Cucumber (with cucumber-picocontainer) creates a new ScenarioContext for every scenario
 * and passes the same instance to the constructor of every step class and of {@code Hooks}.
 * Parallel scenarios therefore never share a browser or any other state.
 */
public class ScenarioContext {

    private WebDriver driver;
    private VisualizerPage page;
    private Path downloadFolder;

    /** Text of every JavaScript alert the application showed during this scenario, in order. */
    private final List<String> alerts = new ArrayList<>();

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public VisualizerPage getPage() {
        return page;
    }

    public void setPage(VisualizerPage page) {
        this.page = page;
    }

    public Path getDownloadFolder() {
        return downloadFolder;
    }

    public void setDownloadFolder(Path downloadFolder) {
        this.downloadFolder = downloadFolder;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    /** Remembers an alert returned by a page-object action, if there was one. */
    public void recordAlert(Optional<String> alert) {
        alert.ifPresent(alerts::add);
    }
}
