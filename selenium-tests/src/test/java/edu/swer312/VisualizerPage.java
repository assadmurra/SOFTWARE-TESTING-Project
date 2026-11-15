package edu.swer312;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/** All UI selectors and browser interactions live here. No application internals. */
public class VisualizerPage {
 private final WebDriver driver;
 private final WebDriverWait wait;
 public VisualizerPage(WebDriver driver) { this.driver=driver; wait=new WebDriverWait(driver,Duration.ofSeconds(45)); }
 private WebElement el(String id) { return driver.findElement(By.id(id)); }
 private void click(String id) { WebElement e=wait.until(ExpectedConditions.elementToBeClickable(By.id(id))); ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'})",e); e.click(); }
 public void open() {
  driver.get(System.getProperty("baseUrl","http://localhost:8000"));
  // Brython writes 0.00 after initializing the application.
  new WebDriverWait(driver, Duration.ofSeconds(120))
   .withMessage("Application initialization did not finish")
   .until(ExpectedConditions.textToBe(By.id("stat-avg-degree"), "0.00"));
 }
 public void load(Path file,int nodes,int edges) {
  // sendKeys is Selenium's native file upload, including hidden file inputs.
  el("file-input").sendKeys(file.toAbsolutePath().toString());
  wait.until(d -> el("stat-nodes").getText().equals(""+nodes) && el("stat-edges").getText().equals(""+edges));
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
 public void run() {
  // Use the visible speed slider with keyboard input, without fixed delays.
  el("speed-slider").sendKeys(Keys.END);
  click("btn-start");
  wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#search-results .status-success, #search-results .status-failure")));
  wait.until(d -> el("btn-start").isEnabled());
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