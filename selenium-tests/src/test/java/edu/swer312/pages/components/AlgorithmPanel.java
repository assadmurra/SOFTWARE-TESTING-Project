package edu.swer312.pages.components;

import edu.swer312.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/** Algorithm drop-down, depth limit for DLS / IDS and the algorithm info panel. */
public class AlgorithmPanel extends BasePage {

    // Locators
    private static final By ALGORITHM_SELECT = By.id("algorithm-select");
    private static final By DEPTH_LIMIT_INPUT = By.id("depth-limit");
    private static final By ALGORITHM_INFO_TITLE = By.cssSelector("#algorithm-info h4");

    public AlgorithmPanel(WebDriver driver) {
        super(driver);
    }

    /** Selects an algorithm; DLS and IDS also get the depth limit. */
    public void select(String algorithm, int depth) {
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

    public String infoTitle() {
        return find(ALGORITHM_INFO_TITLE).getText();
    }
}
