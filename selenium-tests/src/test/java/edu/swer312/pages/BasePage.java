package edu.swer312.pages;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base class of every page object and page component: holds the scenario's driver, the
 * default explicit wait and the helpers they all share (find, click, wait for visibility,
 * wait for a result-or-alert, negative wait).
 *
 * <p>Waiting strategy: only explicit waits ({@link WebDriverWait}, {@link FluentWait} and
 * {@link ExpectedConditions}); no implicit waits and no {@code Thread.sleep}.
 */
public abstract class BasePage {

    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(45);
    protected static final Duration POLLING_INTERVAL = Duration.ofMillis(200);

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    protected WebElement find(By locator) {
        return driver.findElement(locator);
    }

    protected List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    /** Waits until the element is clickable, scrolls it into view and clicks it. */
    protected void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        executeScript("arguments[0].scrollIntoView({block:'center'})", element);
        element.click();
    }

    protected Object executeScript(String script, Object... arguments) {
        return ((JavascriptExecutor) driver).executeScript(script, arguments);
    }

    /** Explicit wait for an element to become visible. @return true if it did within the timeout */
    protected boolean waitUntilVisible(By locator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException notShown) {
            return false;
        }
    }

    /**
     * Fluent wait that polls every 200 ms until either a JavaScript alert is open (accepted and
     * returned) or the given condition holds (empty result). A DOM lookup can throw
     * UnhandledAlertException at the moment an alert appears; that is ignored and the next poll
     * reads the alert.
     */
    protected Optional<String> waitForAlertOr(Function<WebDriver, Boolean> condition, String what) {
        return new FluentWait<>(driver)
                .withTimeout(DEFAULT_TIMEOUT)
                .pollingEvery(POLLING_INTERVAL)
                .ignoring(UnhandledAlertException.class)
                .ignoring(NoSuchElementException.class)
                .withMessage("Timed out waiting for " + what + " or an alert")
                .until(d -> {
                    try {
                        Alert alert = d.switchTo().alert();
                        String text = alert.getText();
                        alert.accept();
                        return Optional.of(text);
                    } catch (NoAlertPresentException noAlert) {
                        return condition.apply(d) ? Optional.<String>empty() : null;
                    }
                });
    }

    /**
     * Negative wait: returns true if the value does NOT change during the whole period.
     * Implemented as an explicit wait for a change that is expected to time out (no sleep).
     */
    protected boolean staysUnchanged(Supplier<String> value, Duration period) {
        String before = value.get();
        try {
            new WebDriverWait(driver, period, POLLING_INTERVAL).until(d -> !value.get().equals(before));
            return false;
        } catch (TimeoutException unchanged) {
            return true;
        }
    }
}
