package edu.swer312.hooks;

import edu.swer312.config.TestConfig;
import edu.swer312.context.ScenarioContext;
import edu.swer312.driver.DriverFactory;
import edu.swer312.pages.VisualizerPage;
import edu.swer312.support.AllureReporting;
import edu.swer312.support.Downloads;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import java.io.IOException;
import java.nio.file.Path;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/** Opens a fresh browser before every scenario and closes it afterwards. */
public class Hooks {

    private final ScenarioContext context;

    public Hooks(ScenarioContext context) {
        this.context = context;
    }

    @Before
    public void startBrowser(Scenario scenario) throws IOException {
        // Each scenario downloads into its own folder, so parallel scenarios never share files.
        Path downloadFolder = Downloads.createFolder();
        WebDriver driver = DriverFactory.createDriver(TestConfig.browser(), downloadFolder, TestConfig.headless());

        context.setDownloadFolder(downloadFolder);
        context.setDriver(driver);
        context.setPage(new VisualizerPage(driver, TestConfig.baseUrl()));

        // Allure report: environment info (once per run) and grouping by tag.
        AllureReporting.writeEnvironmentOnce(((HasCapabilities) driver).getCapabilities());
        AllureReporting.groupByTags(scenario.getSourceTagNames());
    }

    @After
    public void closeBrowser(Scenario scenario) {
        WebDriver driver = context.getDriver();
        // Each clean-up step runs even if the one before fails (e.g. the browser already crashed).
        try {
            if (driver != null && scenario.isFailed()) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "failure");
            }
        } catch (WebDriverException browserGone) {
            scenario.log("No screenshot: " + browserGone.getClass().getSimpleName());
        } finally {
            try {
                if (driver != null) {
                    driver.quit();
                }
            } catch (WebDriverException alreadyClosed) {
                // nothing left to close
            } finally {
                Downloads.deleteFolder(context.getDownloadFolder());
            }
        }
    }
}
