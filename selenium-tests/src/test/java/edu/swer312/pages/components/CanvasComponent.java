package edu.swer312.pages.components;

import edu.swer312.pages.BasePage;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** The drawing canvas, its building tools and the dialogs shown while adding a node. */
public class CanvasComponent extends BasePage {

    // Locators
    private static final By CANVAS = By.id("graph-canvas");
    private static final By GRAPH_TYPE_DIALOG = By.id("graph-type-modal");
    private static final By NODE_NAME_DIALOG = By.id("node-name-modal");
    private static final By NODE_NAME_INPUT = By.id("node-name-input");
    private static final By NODE_NAME_OK_BUTTON = By.id("modal-btn-node-ok");
    private static final By NODE_NAME_CANCEL_BUTTON = By.id("modal-btn-node-cancel");

    /** Building tool button, e.g. "add-node", "add-edge", "set-goal", "set-source", "delete-node". */
    private static By toolButton(String tool) {
        return By.id("tool-" + tool);
    }

    /** Button in the "Choose Graph Type" dialog: "directed" or "undirected". */
    private static By graphTypeButton(String type) {
        return By.id("modal-btn-" + type);
    }

    public CanvasComponent(WebDriver driver) {
        super(driver);
    }

    /** Clicks the canvas at an offset in pixels from its centre. */
    public void clickAt(int dx, int dy) {
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
        wait.until(d -> Boolean.TRUE.equals(executeScript(
                "var i = arguments[0];"
                        + "return document.activeElement === i && i.value.length > 0"
                        + " && i.selectionStart === 0 && i.selectionEnd === i.value.length;",
                input)));
        return input;
    }
}
