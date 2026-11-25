package edu.swer312;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/** All UI selectors and browser interactions live here. No application internals. */
public class VisualizerPage {
 private final WebDriver driver;
 private final WebDriverWait wait;
 public VisualizerPage(WebDriver driver) { this.driver=driver; wait=new WebDriverWait(driver,Duration.ofSeconds(45)); }
 private WebElement el(String id) { return driver.findElement(By.id(id)); }
 private void click(String id) { WebElement e=wait.until(ExpectedConditions.elementToBeClickable(By.id(id))); ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'})",e); e.click(); }
 /** Normal start-up takes 2-7 s; 30 s means a request was lost, not that the app is slow. */
 private static final Duration APP_START_TIMEOUT = Duration.ofSeconds(30);
 private static final int APP_START_ATTEMPTS = 2;

 /**
  * Opens the application and waits until Brython has initialised it (it then writes 0.00
  * into the average-degree statistic). Brython loads its .py files with synchronous requests
  * that never time out, so if the simple test web server drops one connection under
  * parallel load the page hangs forever. In that case the page is reloaded once (logged);
  * a second failure fails the test.
  */
 public void open() {
  String url = System.getProperty("baseUrl","http://localhost:8000");
  for (int attempt = 1; ; attempt++) {
   driver.get(url);
   try {
    new WebDriverWait(driver, APP_START_TIMEOUT)
     .withMessage("Application initialization did not finish (attempt " + attempt + ")")
     .until(ExpectedConditions.textToBe(By.id("stat-avg-degree"), "0.00"));
    return;
   } catch (TimeoutException e) {
    if (attempt >= APP_START_ATTEMPTS) throw e;
    System.err.println("[open] Application did not start within " + APP_START_TIMEOUT.toSeconds() + " s, reloading once");
   }
  }
 }
 /**
  * Uploads a graph file and waits until it has loaded OR the application shows an alert
  * (malformed file). expectedNodes is empty for files that are not valid graphs.
  * @return the alert text if the application rejected the file
  */
 public Optional<String> load(Path file, OptionalInt expectedNodes) {
  // sendKeys is Selenium's native file upload, including hidden file inputs.
  el("file-input").sendKeys(file.toAbsolutePath().toString());
  return waitForAlertOr(d -> {
   String graphType = el("graph-type-text").getText();
   boolean typeShown = graphType.equals("Directed") || graphType.equals("Undirected");
   return typeShown && expectedNodes.isPresent() && nodes().equals(String.valueOf(expectedNodes.getAsInt()));
  }, "graph upload");
 }
 public String nodes() { return el("stat-nodes").getText(); }
 public String edges() { return el("stat-edges").getText(); }
 public void select(String algorithm,int depth) {
  // IDS reads the same depth field but the app only exposes it for DLS.
  if (algorithm.equals("ids")) new Select(el("algorithm-select")).selectByValue("dls");
  else new Select(el("algorithm-select")).selectByValue(algorithm);
  if (algorithm.equals("dls") || algorithm.equals("ids")) {
   wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("depth-limit")));
   el("depth-limit").clear(); el("depth-limit").sendKeys(""+depth);
  }
  if (algorithm.equals("ids")) new Select(el("algorithm-select")).selectByValue("ids");
 }
 /**
  * Starts the search and waits until a result is shown OR the application shows a
  * validation alert (no source, no goal, missing heuristics, ...).
  * @return the alert text if the search was refused
  */
 public Optional<String> run() {
  // Use the visible speed slider with keyboard input, without fixed delays.
  el("speed-slider").sendKeys(Keys.END);
  click("btn-start");
  return waitForAlertOr(d -> !driver.findElements(By.cssSelector("#search-results .status-success, #search-results .status-failure")).isEmpty()
    && el("btn-start").isEnabled(), "search result");
 }

 /**
  * Polls until either a JavaScript alert is open (accepted and returned) or the given
  * condition holds (empty result). The driver is configured to leave unexpected alerts
  * open, so a DOM lookup can throw UnhandledAlertException while one appears; that is ignored
  * and the next poll reads the alert.
  */
 private Optional<String> waitForAlertOr(java.util.function.Function<WebDriver, Boolean> condition, String what) {
  return new FluentWait<>(driver)
   .withTimeout(Duration.ofSeconds(45))
   .pollingEvery(Duration.ofMillis(200))
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
 public boolean success() { return !driver.findElements(By.cssSelector("#search-results .status-success")).isEmpty(); }
 public String status() { return el("search-results").getText(); }
 public List<String> path() { return driver.findElements(By.cssSelector("#path-list .array-item")).stream().map(WebElement::getText).toList(); }
 public List<String> traversal() { return driver.findElements(By.cssSelector("#traversal-list .array-item")).stream().map(WebElement::getText).toList(); }
 public double cost() { return Double.parseDouble(driver.findElement(By.xpath("//*[@id='search-results']//*[contains(@class,'result-metric')][span[1]='Path Cost:']/span[2]")).getText()); }
 public void clear() { click("btn-clear-path"); wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#search-results .status-pending"))); }
 public boolean pending() { return el("search-results").getText().contains("Ready to start search") && path().isEmpty(); }
 public boolean depthVisible() { return el("depth-limit").isDisplayed(); }
 public String theme() { return driver.findElement(By.tagName("body")).getAttribute("class"); }
 public void toggleTheme() { click("theme-toggle"); }
}