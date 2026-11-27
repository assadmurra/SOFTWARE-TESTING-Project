package edu.swer312.pages.components;

import edu.swer312.pages.BasePage;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Animation controls: speed slider, Start, Pause / Resume, Stop and the step buttons. */
public class PlaybackControls extends BasePage {

    // Locators
    private static final By SPEED_SLIDER = By.id("speed-slider");
    private static final By SPEED_VALUE = By.id("speed-value");
    private static final By START_BUTTON = By.id("btn-start");
    private static final By PAUSE_BUTTON = By.id("btn-pause");
    private static final By STOP_BUTTON = By.id("btn-stop");
    private static final By STEP_FORWARD_BUTTON = By.id("btn-step-forward");
    private static final By STEP_BACK_BUTTON = By.id("btn-step-back");

    /** Animation control buttons by the label used in the feature files. */
    private static final Map<String, By> CONTROL_BUTTONS = Map.of(
            "Start", START_BUTTON,
            "Pause", PAUSE_BUTTON,
            "Stop", STOP_BUTTON,
            "Step forward", STEP_FORWARD_BUTTON,
            "Step back", STEP_BACK_BUTTON);

    public PlaybackControls(WebDriver driver) {
        super(driver);
    }

    /** Moves the visible speed slider to the maximum with the keyboard (no fixed delays). */
    public void setFastestSpeed() {
        find(SPEED_SLIDER).sendKeys(Keys.END);
    }

    /** Sets the speed slider (1 = slowest, one animation step per second; 10 = fastest). */
    public void setSpeed(int speed) {
        WebElement slider = find(SPEED_SLIDER);
        slider.sendKeys(Keys.HOME);
        for (int i = 1; i < speed; i++) {
            slider.sendKeys(Keys.ARROW_RIGHT);
        }
        wait.until(d -> find(SPEED_VALUE).getText().equals(speed + "x"));
    }

    /** Clicks Start without waiting; the caller decides what to wait for. */
    public void clickStart() {
        click(START_BUTTON);
    }

    /** Clicks Start and waits until the animation is running (Pause becomes enabled). */
    public void startAnimation() {
        click(START_BUTTON);
        wait.until(d -> find(PAUSE_BUTTON).isEnabled());
    }

    public void pause() {
        click(PAUSE_BUTTON);
        wait.until(d -> pauseButtonText().contains("Resume"));
    }

    public void resume() {
        click(PAUSE_BUTTON);
        wait.until(d -> pauseButtonText().contains("Pause"));
    }

    public void stop() {
        click(STOP_BUTTON);
        wait.until(d -> isStartEnabled());
    }

    public void stepForward() {
        click(STEP_FORWARD_BUTTON);
    }

    public void stepBackward() {
        click(STEP_BACK_BUTTON);
    }

    public String pauseButtonText() {
        return find(PAUSE_BUTTON).getText();
    }

    public boolean isStartEnabled() {
        return find(START_BUTTON).isEnabled();
    }

    /** @param label "Start", "Pause", "Stop", "Step forward" or "Step back" */
    public boolean isControlEnabled(String label) {
        By button = CONTROL_BUTTONS.get(label);
        if (button == null) {
            throw new IllegalArgumentException(
                    "Unknown control '" + label + "', expected one of " + CONTROL_BUTTONS.keySet());
        }
        return find(button).isEnabled();
    }
}
