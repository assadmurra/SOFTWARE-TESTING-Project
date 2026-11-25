package edu.swer312;
import io.cucumber.java.*;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.edge.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import io.cucumber.datatable.DataTable;
import edu.swer312.support.GraphFixtures;
import static org.junit.jupiter.api.Assertions.*;

/** Cucumber creates a fresh Steps instance (and driver) for each scenario. */
public class Steps {
 private WebDriver driver;
 private VisualizerPage page;
 private String oldTheme;
 /** Text of every JavaScript alert the application showed during this scenario, in order. */
 private final List<String> alerts=new ArrayList<>();
 @Before public void start() {
  boolean headless=Boolean.parseBoolean(System.getProperty("headless","true"));
  switch(System.getProperty("browser","chrome")) {
   case "chrome": var c=new ChromeOptions(); c.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE); if(headless)c.addArguments("--headless=new"); c.addArguments("--no-sandbox","--disable-dev-shm-usage","--window-size=1440,1100"); if(System.getProperty("browserBinary")!=null)c.setBinary(System.getProperty("browserBinary")); driver=new ChromeDriver(c); break;
   case "firefox": var f=new FirefoxOptions(); f.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE); if(System.getProperty("browserBinary")!=null)f.setBinary(System.getProperty("browserBinary")); if(headless)f.addArguments("-headless"); driver=new FirefoxDriver(f); break;
   case "edge": var e=new EdgeOptions(); e.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE); if(System.getProperty("browserBinary")!=null)e.setBinary(System.getProperty("browserBinary")); if(headless)e.addArguments("--headless=new"); e.addArguments("--no-sandbox","--disable-dev-shm-usage"); driver=new EdgeDriver(e); break;
   default: throw new IllegalArgumentException("browser must be chrome, firefox or edge");
  }
  driver.manage().window().setSize(new Dimension(1440,1100));
  page=new VisualizerPage(driver);
 }
 @After public void finish(Scenario scenario) {
  if(driver!=null) { try { if(scenario.isFailed())scenario.attach(((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES),"image/png","failure"); } finally { driver.quit(); } }
 }
 @Given("the visualizer is ready") public void ready() { page.open(); }
 @Given("graph {string} is loaded") public void load(String name) throws Exception {
  // Each scenario uploads a separate temporary file, deleted once the app has read it.
  Path tmp=GraphFixtures.copyToTempFile(name);
  try { page.load(tmp, GraphFixtures.expectedNodeCount(name)).ifPresent(alerts::add); }
  finally { Files.deleteIfExists(tmp); }
 }
 @When("I run {string} with depth {int}") public void run(String algorithm,int depth) { page.select(algorithm,depth); page.run().ifPresent(alerts::add); }
 @Then("the path is {string} with cost {int}") public void pathWithCost(String path,int cost) { assertPathFound(path); assertEquals(cost,page.cost(),0.00001); }
 private void assertPathFound(String path) { assertTrue(page.success(),page.status()); assertEquals(List.of(path.split(">")),page.path()); }
 @Then("no route is reported") public void failure() { assertFalse(page.success(),page.status()); assertTrue(page.path().isEmpty(),page.status()); assertTrue(page.status().toLowerCase().matches("(?s).*(no path|depth|limit|not found).*"),page.status()); }
 @Then("the traversal is {string}") public void traversal(String s) { assertEquals(List.of(s.split(">")),page.traversal()); }
 @Then("the graph has {int} nodes and {int} edges") public void stats(int n,int e) { assertEquals(""+n,page.nodes()); assertEquals(""+e,page.edges()); }
 @When("I clear the result") public void clear() { page.clear(); }
 @Then("the result is ready for another search") public void pending() { assertTrue(page.pending()); }
 @When("I select {string}") public void select(String algo) { page.select(algo,2); }
 @Then("the depth control is {word}") public void depth(String state) { assertEquals(state.equals("visible"),page.depthVisible()); }
 @When("I toggle the theme") public void theme() { oldTheme=page.theme(); page.toggleTheme(); }
 @Then("the theme changes") public void changed() { assertNotEquals(oldTheme,page.theme()); }

 // ---------------------------------------------------------------------------------
 // Data-driven outcome check, used by the features generated from CSV / Excel rows.
 // ---------------------------------------------------------------------------------

 /**
  * Compares the visible result with the expected outcome of one data row:
  * found   - goal reached with exactly this path and cost (and traversal, if given);
  * no_path - a failure result with no path (and the given status message, if any);
  * alert   - the application refused with an alert containing the given message.
  */
 @Then("the search outcome matches") public void searchOutcomeMatches(DataTable table) {
  Map<String,String> expected=table.asMap(String.class,String.class);
  String outcome=text(expected,"outcome"), message=text(expected,"message");
  switch(outcome) {
   case "found" -> {
    assertTrue(alerts.isEmpty(),"Unexpected alert(s): "+alerts);
    assertPathFound(text(expected,"path"));
    assertEquals(Double.parseDouble(text(expected,"cost")),page.cost(),0.00001,"path cost");
   }
   case "no_path" -> {
    assertTrue(alerts.isEmpty(),"Unexpected alert(s): "+alerts);
    assertFalse(page.success(),"Expected no path, but got: "+page.status());
    assertTrue(page.path().isEmpty(),"Expected an empty path, but got "+page.path());
    if(!message.isEmpty()) assertTrue(page.status().contains(message),"Status '"+page.status()+"' should contain '"+message+"'");
   }
   case "alert" -> {
    assertTrue(alerts.stream().anyMatch(a->a.contains(message)),"Expected an alert containing '"+message+"', alerts were "+alerts);
    assertFalse(page.success(),"Search should not have succeeded after an alert");
   }
   default -> fail("Unknown outcome '"+outcome+"'");
  }
  // The traversal order is optional and can be checked for successful and failed searches.
  String traversal=text(expected,"traversal");
  if(!traversal.isEmpty()) traversal(traversal);
 }

 /** Empty Gherkin table cells arrive as null. */
 private static String text(Map<String,String> map,String key) { String v=map.get(key); return v==null?"":v.trim(); }
}