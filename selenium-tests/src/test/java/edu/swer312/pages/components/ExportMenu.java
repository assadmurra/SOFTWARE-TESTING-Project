package edu.swer312.pages.components;

import edu.swer312.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** "Save Graph" and the export buttons (PNG, SVG, JSON, CSV, ...). */
public class ExportMenu extends BasePage {

    // Locators
    private static final By SAVE_GRAPH_BUTTON = By.id("btn-save-graph");

    /** Export button: "png", "svg", "json", "csv", "pdf", "gif" or "sequence". */
    private static By exportButton(String format) {
        return By.id("btn-export-" + format);
    }

    public ExportMenu(WebDriver driver) {
        super(driver);
    }

    public void export(String format) {
        click(exportButton(format));
    }

    public void saveGraph() {
        click(SAVE_GRAPH_BUTTON);
    }
}
