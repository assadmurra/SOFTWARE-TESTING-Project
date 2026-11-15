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
import org.apache.commons.csv.*;
import static org.junit.jupiter.api.Assertions.*;

/** Cucumber creates a fresh Steps instance (and driver) for each scenario. */
public class Steps {
 private WebDriver driver;
 private VisualizerPage page;
 private String oldTheme;
 private Map<String,String> csvExpectation;
 @Before public void start() {
  boolean headless=Boolean.parseBoolean(System.getProperty("headless","true"));
  switch(System.getProperty("browser","chrome")) {
   case "chrome": var c=new ChromeOptions(); if(headless)c.addArguments("--headless=new"); c.addArguments("--no-sandbox","--disable-dev-shm-usage","--window-size=1440,1100"); if(System.getProperty("browserBinary")!=null)c.setBinary(System.getProperty("browserBinary")); driver=new ChromeDriver(c); break;
   case "firefox": var f=new FirefoxOptions(); if(System.getProperty("browserBinary")!=null)f.setBinary(System.getProperty("browserBinary")); if(headless)f.addArguments("-headless"); driver=new FirefoxDriver(f); break;
   case "edge": var e=new EdgeOptions(); if(System.getProperty("browserBinary")!=null)e.setBinary(System.getProperty("browserBinary")); if(headless)e.addArguments("--headless=new"); e.addArguments("--no-sandbox","--disable-dev-shm-usage"); driver=new EdgeDriver(e); break;
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
  // Each scenario uploads a separate temporary file, cleaned immediately after FileReader completes.
  Path tmp=Files.createTempFile("graph-", ".json");
  try(var in=getClass().getResourceAsStream("/graphs/"+name+".json")) {
   if(in==null)throw new IllegalArgumentException(name);
   Files.copy(in,tmp,StandardCopyOption.REPLACE_EXISTING);
   page.load(tmp, name.equals("disconnected")?3:5,name.equals("disconnected")?1:5);
  } finally { Files.deleteIfExists(tmp); }
 }
 @When("I run {string} with depth {int}") public void run(String algorithm,int depth) { page.select(algorithm,depth); page.run(); }
 @Then("the path is {string} with cost {int}") public void path(String path,int cost) { assertTrue(page.success(),page.status()); assertEquals(List.of(path.split(">")),page.path()); assertEquals(cost,page.cost(),0.00001); }
 @Then("no route is reported") public void failure() { assertFalse(page.success(),page.status()); assertTrue(page.path().isEmpty(),page.status()); assertTrue(page.status().toLowerCase().matches("(?s).*(no path|depth|limit|not found).*"),page.status()); }
 @Then("the traversal is {string}") public void traversal(String s) { assertEquals(List.of(s.split(">")),page.traversal()); }
 @Then("the graph has {int} nodes and {int} edges") public void stats(int n,int e) { assertEquals(""+n,page.nodes()); assertEquals(""+e,page.edges()); }
 @When("I clear the result") public void clear() { page.clear(); }
 @Then("the result is ready for another search") public void pending() { assertTrue(page.pending()); }
 @When("I select {string}") public void select(String algo) { page.select(algo,2); }
 @Then("the depth control is {word}") public void depth(String state) { assertEquals(state.equals("visible"),page.depthVisible()); }
 @When("I toggle the theme") public void theme() { oldTheme=page.theme(); page.toggleTheme(); }
 @Then("the theme changes") public void changed() { assertNotEquals(oldTheme,page.theme()); }
 @Then("the result matches the external CSV expectation") public void csvResult() {
  assertNotNull(csvExpectation);
  if(csvExpectation.get("result").equals("found"))path(csvExpectation.get("path"),Integer.parseInt(csvExpectation.get("cost"))); else failure();
 }
 @When("I execute external CSV case {string}") public void csv(String id) throws Exception {
  try(var reader=new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/data/searches.csv")),java.nio.charset.StandardCharsets.UTF_8);
      var records=CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader)) {
   for(var row:records) if(row.get("id").equals(id)) {
    load(row.get("graph")); run(row.get("algorithm"),Integer.parseInt(row.get("depth")));
    csvExpectation=row.toMap();
    return;
   }
   fail("CSV case missing: "+id);
  }
 }
}