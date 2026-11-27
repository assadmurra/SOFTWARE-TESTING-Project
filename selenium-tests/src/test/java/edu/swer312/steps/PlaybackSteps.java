package edu.swer312.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.swer312.context.ScenarioContext;
import edu.swer312.pages.VisualizerPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.util.List;
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
        page().algorithm().select(algorithm, 5);
        page().playback().setSpeed(1);
        page().playback().startAnimation();
    }

    @When("I pause the animation")
    public void iPauseTheAnimation() {
        page().playback().pause();
    }

    @When("I resume the animation")
    public void iResumeTheAnimation() {
        page().playback().resume();
    }

    @When("I stop the animation")
    public void iStopTheAnimation() {
        page().playback().stop();
    }

    @When("I step forward until the search finishes")
    public void iStepForwardUntilTheSearchFinishes() {
        for (int i = 0; i < MAX_STEPS && !page().results().hasResult(); i++) {
            page().playback().stepForward();
        }
        assertTrue(page().results().hasResult(), "No result after " + MAX_STEPS + " steps");
    }

    @When("I step back once")
    public void iStepBackOnce() {
        page().playback().stepBackward();
    }

    @Then("the pause button offers {string}")
    public void thePauseButtonOffers(String label) {
        assertTrue(page().playback().pauseButtonText().contains(label),
                "Pause button shows '" + page().playback().pauseButtonText() + "'");
    }

    @Then("the animation does not advance for {int} seconds")
    public void theAnimationDoesNotAdvanceFor(int seconds) {
        assertTrue(page().results().liveDataStaysUnchanged(Duration.ofSeconds(seconds)),
                "The Live Data Panel changed while the animation was paused");
    }

    @Then("the animation finishes")
    public void theAnimationFinishes() {
        context.recordAlert(page().waitForResult());
    }

    @Then("the animation controls are:")
    public void theAnimationControlsAre(Map<String, String> expectedStates) {
        expectedStates.forEach((button, state) ->
                assertEquals(state.equals("enabled"), page().playback().isControlEnabled(button),
                        button + " should be " + state));
    }

    @Then("no path is shown")
    public void noPathIsShown() {
        List<String> path = page().results().path();
        assertTrue(path.isEmpty(), "Expected no path, but the panel shows " + path);
    }
}
