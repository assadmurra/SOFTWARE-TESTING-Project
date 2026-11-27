package edu.swer312.driver;

import edu.swer312.config.TestConfig;
import java.nio.file.Path;
import java.util.Map;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Cross-browser testing: creates a Chrome, Firefox or Edge WebDriver.
 *
 * <p>The browser is chosen on the command line, e.g. {@code mvn test -Dbrowser=firefox}.
 * Selenium Manager downloads the matching driver automatically. Every browser is configured
 * the same way: same window size, downloads saved to the scenario's own folder without asking,
 * and JavaScript alerts left open so the tests can read them.
 */
public final class DriverFactory {

    /** Large enough to show every control of the desktop layout. */
    private static final Dimension WINDOW_SIZE = new Dimension(1440, 1100);

    private DriverFactory() {
    }

    /**
     * Creates a new browser for one scenario.
     *
     * @param browser        "chrome", "firefox" or "edge"
     * @param downloadFolder where downloaded files are saved
     * @param headless       true to run without a visible window
     */
    public static WebDriver createDriver(String browser, Path downloadFolder, boolean headless) {
        WebDriver driver = switch (browser.toLowerCase()) {
            case "chrome" -> new ChromeDriver(chromeOptions(downloadFolder, headless));
            case "firefox" -> new FirefoxDriver(firefoxOptions(downloadFolder, headless));
            case "edge" -> new EdgeDriver(edgeOptions(downloadFolder, headless));
            default -> throw new IllegalArgumentException(
                    "Unknown browser '" + browser + "': use chrome, firefox or edge");
        };
        driver.manage().window().setSize(WINDOW_SIZE);
        return driver;
    }

    private static ChromeOptions chromeOptions(Path downloadFolder, boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromiumDownloadPrefs(downloadFolder));
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1440,1100");
        if (TestConfig.browserBinary() != null) {
            options.setBinary(TestConfig.browserBinary());
        }
        return options;
    }

    private static FirefoxOptions firefoxOptions(Path downloadFolder, boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("browser.download.folderList", 2); // 2 = use the folder below
        options.addPreference("browser.download.dir", downloadFolder.toString());
        options.addPreference("browser.download.useDownloadDir", true);
        options.addPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/json,text/csv,image/png,image/svg+xml");
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
        if (headless) {
            options.addArguments("-headless");
        }
        if (TestConfig.browserBinary() != null) {
            options.setBinary(TestConfig.browserBinary());
        }
        return options;
    }

    private static EdgeOptions edgeOptions(Path downloadFolder, boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.setExperimentalOption("prefs", chromiumDownloadPrefs(downloadFolder));
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        if (TestConfig.browserBinary() != null) {
            options.setBinary(TestConfig.browserBinary());
        }
        return options;
    }

    /** Chrome and Edge share the same download settings. */
    private static Map<String, Object> chromiumDownloadPrefs(Path downloadFolder) {
        return Map.of(
                "download.default_directory", downloadFolder.toString(),
                "download.prompt_for_download", false);
    }
}
