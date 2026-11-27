package edu.swer312.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.swer312.context.ScenarioContext;
import edu.swer312.pages.VisualizerPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/** General controls: algorithm selection, info panel, theme, keyboard, examples and graph type. */
public class UiSteps {

    private final ScenarioContext context;
    private String themeBefore;

    public UiSteps(ScenarioContext context) {
        this.context = context;
    }

    private VisualizerPage page() {
        return context.getPage();
    }

    // ---------------------------------------------------------------------------------
    // Algorithm selection and info panel
    // ---------------------------------------------------------------------------------

    @When("I select {string}")
    public void iSelect(String algorithm) {
        page().selectAlgorithm(algorithm, 2);
    }

    @Then("the depth control is {word}")
    public void theDepthControlIs(String state) {
        assertEquals(state.equals("visible"), page().isDepthControlVisible(), "depth control " + state);
    }

    @Then("the algorithm info shows {string}")
    public void theAlgorithmInfoShows(String title) {
        assertEquals(title, page().algorithmInfoTitle());
    }

    // ---------------------------------------------------------------------------------
    // Theme and keyboard shortcuts
    // ---------------------------------------------------------------------------------

    @When("I toggle the theme")
    public void iToggleTheTheme() {
        themeBefore = page().theme();
        page().toggleTheme();
    }

    @Then("the theme changes")
    public void theThemeChanges() {
        assertNotEquals(themeBefore, page().theme());
    }

    @When("I press the {string} key")
    public void iPressTheKey(String key) {
        page().pressShortcut(key);
    }

    @Then("the {string} tool is active")
    public void theToolIsActive(String tool) {
        assertTrue(page().isToolActive(tool.toLowerCase().replace(' ', '-')), tool + " should be the active tool");
    }

    // ---------------------------------------------------------------------------------
    // Examples menu
    // ---------------------------------------------------------------------------------

    @When("I open the {string} example")
    public void iOpenTheExample(String example) {
        page().openExample(example);
    }

    @When("I confirm clearing the graph")
    public void iConfirmClearingTheGraph() {
        String text = page().acceptDialog();
        assertTrue(text.contains("clear the entire graph"), "Unexpected dialog: " + text);
    }

    @When("I cancel clearing the graph")
    public void iCancelClearingTheGraph() {
        String text = page().dismissDialog();
        assertTrue(text.contains("clear the entire graph"), "Unexpected dialog: " + text);
    }

    // ---------------------------------------------------------------------------------
    // Directed / undirected graph type
    // ---------------------------------------------------------------------------------

    @When("I switch the graph type to {word}")
    public void iSwitchTheGraphTypeTo(String type) {
        page().selectGraphType(type);
    }

    @When("I confirm the graph type change")
    public void iConfirmTheGraphTypeChange() {
        String text = page().acceptDialog();
        assertTrue(text.contains("Changing graph type"), "Unexpected dialog: " + text);
    }

    @When("I cancel the graph type change")
    public void iCancelTheGraphTypeChange() {
        String text = page().dismissDialog();
        assertTrue(text.contains("Changing graph type"), "Unexpected dialog: " + text);
    }

    @Then("the graph type indicator shows {string}")
    public void theGraphTypeIndicatorShows(String type) {
        assertEquals(type, page().graphTypeIndicator());
    }

    @Then("the graph type selector shows {string}")
    public void theGraphTypeSelectorShows(String value) {
        assertEquals(value, page().selectedGraphType());
    }
}
