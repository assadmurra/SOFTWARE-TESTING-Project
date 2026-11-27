package edu.swer312.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.swer312.context.ScenarioContext;
import edu.swer312.pages.VisualizerPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.util.Map;

/** Controlling the search animation: start, pause, resume, stop and step. */
public class PlaybackSteps {

    /** Far more animation states than any test graph produces. */
    private static final int MAX_STEPS = 200;

    private final ScenarioContext context;

    public PlaybackSteps(ScenarioContext context) {
        this.context = context;
    }

    private VisualizerPage page() {
        return context.getPage();
    }

    @When("I start {string} at the slowest speed")
    public void iStartAtTheSlowestSpeed(String algorithm) {
        page().selectAlgorithm(algorithm, 5);
        page().setSpeed(1);
        page().startAnimation();
    }

    @When("I pause the animation")
    public void iPauseTheAnimation() {
        page().pause();
    }

    @When("I resume the animation")
    public void iResumeTheAnimation() {
        page().resume();
    }

    @When("I stop the animation")
    public void iStopTheAnimation() {
        page().stop();
    }

    @When("I step forward until the search finishes")
    public void iStepForwardUntilTheSearchFinishes() {
        for (int i = 0; i < MAX_STEPS && !page().hasResult(); i++) {
            page().stepForward();
        }
        assertTrue(page().hasResult(), "No result after " + MAX_STEPS + " steps");
    }

    @When("I step back once")
    public void iStepBackOnce() {
        page().stepBackward();
    }

    @Then("the pause button offers {string}")
    public void thePauseButtonOffers(String label) {
        assertTrue(page().pauseButtonText().contains(label),
                "Pause button shows '" + page().pauseButtonText() + "'");
    }

    @Then("the animation does not advance for {int} seconds")
    public void theAnimationDoesNotAdvanceFor(int seconds) {
        assertTrue(page().staysUnchanged(page()::liveDataSnapshot, Duration.ofSeconds(seconds)),
                "The Live Data Panel changed while the animation was paused");
    }

    @Then("the animation finishes")
    public void theAnimationFinishes() {
        context.recordAlert(page().waitForResult());
    }

    @Then("the animation controls are:")
    public void theAnimationControlsAre(Map<String, String> expectedStates) {
        expectedStates.forEach((button, state) ->
                assertEquals(state.equals("enabled"), page().isControlEnabled(button), button + " should be " + state));
    }

    @Then("no path is shown")
    public void noPathIsShown() {
        assertTrue(page().path().isEmpty(), "Expected no path, but the panel shows " + page().path());
    }
}
