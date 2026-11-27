package edu.swer312.steps;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.swer312.context.ScenarioContext;
import edu.swer312.pages.VisualizerPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.LinkedHashMap;
import java.util.Map;

/** Drawing and editing a graph on the canvas with the mouse. */
public class CanvasSteps {

    /** Horizontal spacing between hand-drawn nodes, in pixels (node radius is 25). */
    private static final int NODE_SPACING = 160;
    private static final int FIRST_NODE_OFFSET = -240;

    private final ScenarioContext context;
    /** Canvas offsets (from the centre) of the nodes this scenario has drawn, by name. */
    private final Map<String, int[]> drawnNodes = new LinkedHashMap<>();
    private int nodesDrawn = 0;

    public CanvasSteps(ScenarioContext context) {
        this.context = context;
    }

    private VisualizerPage page() {
        return context.getPage();
    }

    /** Nodes are drawn left to right in a row through the centre of the canvas. */
    private int[] nextNodeOffset() {
        return new int[] {FIRST_NODE_OFFSET + NODE_SPACING * nodesDrawn++, 0};
    }

    private int[] offsetOf(String node) {
        int[] offset = drawnNodes.get(node);
        assertNotNull(offset, "Node '" + node + "' was not drawn in this scenario");
        return offset;
    }

    private void clickNodeWithTool(String tool, String node) {
        page().canvas().selectTool(tool);
        int[] offset = offsetOf(node);
        page().canvas().clickAt(offset[0], offset[1]);
    }

    // ---------------------------------------------------------------------------------
    // Adding nodes
    // ---------------------------------------------------------------------------------

    /** Draws the given nodes left to right; the first click asks for the graph type. */
    @Given("I have drawn a {word} graph with nodes {string}")
    public void iHaveDrawnAGraphWithNodes(String type, String names) {
        boolean first = true;
        for (String name : names.split("\\s*,\\s*")) {
            int[] offset = nextNodeOffset();
            page().canvas().selectTool("add-node");
            page().canvas().clickAt(offset[0], offset[1]);
            if (first) {
                page().canvas().chooseGraphType(type);
                first = false;
            }
            context.recordAlert(page().canvas().nameNode(name));
            drawnNodes.put(name, offset);
        }
    }

    @When("I add a node named {string}")
    public void iAddANodeNamed(String name) {
        int[] offset = nextNodeOffset();
        page().canvas().selectTool("add-node");
        page().canvas().clickAt(offset[0], offset[1]);
        context.recordAlert(page().canvas().nameNode(name));
        drawnNodes.putIfAbsent(name, offset);
    }

    @When("I click the empty canvas")
    public void iClickTheEmptyCanvas() {
        int[] offset = nextNodeOffset();
        page().canvas().selectTool("add-node");
        page().canvas().clickAt(offset[0], offset[1]);
    }

    @Then("I am asked to choose the graph type")
    public void iAmAskedToChooseTheGraphType() {
        assertTrue(page().canvas().waitForGraphTypeDialog(), "The graph type dialog should be open");
    }

    @When("I choose the {word} graph type")
    public void iChooseTheGraphType(String type) {
        page().canvas().chooseGraphType(type);
    }

    @Then("I am asked to name the node")
    public void iAmAskedToNameTheNode() {
        assertTrue(page().canvas().waitForNodeNameDialog(), "The node name dialog should be open");
    }

    @When("I name the node {string}")
    public void iNameTheNode(String name) {
        context.recordAlert(page().canvas().nameNode(name));
    }

    @When("I cancel naming the node")
    public void iCancelNamingTheNode() {
        page().canvas().cancelNodeName();
    }

    // ---------------------------------------------------------------------------------
    // Edges, source, goal and deleting
    // ---------------------------------------------------------------------------------

    @When("I connect {string} to {string}")
    public void iConnect(String from, String to) {
        page().canvas().selectTool("add-edge");
        int[] start = offsetOf(from);
        int[] end = offsetOf(to);
        page().canvas().clickAt(start[0], start[1]);
        page().canvas().clickAt(end[0], end[1]);
    }

    @When("I mark {string} as a goal")
    public void iMarkAsAGoal(String node) {
        clickNodeWithTool("set-goal", node);
    }

    @When("I toggle {string} as the source")
    public void iToggleAsTheSource(String node) {
        clickNodeWithTool("set-source", node);
    }

    @When("I delete node {string}")
    public void iDeleteNode(String node) {
        clickNodeWithTool("delete-node", node);
    }
}
