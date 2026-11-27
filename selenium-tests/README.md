# Selenium WebDriver + Cucumber Test Project

**SWER312 Software Testing and Quality Assurance, Fall 2026: Selenium WebDriver Project**

**Team:**

| Name | Student ID |
|---|---|
| Assad Murrah | 202304007 |
| Jiovanni Kitlo | 202404659 |
| Miral Zeit | 202302849 |
| Ramez Michael | 202303046 |

This project tests an open-source web application, the **AI Search Algorithm Visualizer**
(`ashfaknawshad/aisearch-v2`), with Selenium WebDriver and Cucumber. In the app, the user
draws or loads a graph, picks a search algorithm (BFS, DFS, DLS, IDS, UCS, Bidirectional,
Greedy, A\*) and watches the search run step by step.

The tests behave like a user: they load or draw a graph, choose an algorithm, start the search,
wait for the animation to finish and check the path, cost and visit order shown on screen.
Every expected result was worked out by hand from the graph data. None of it was copied from
what the application displays. The application code was not changed.

| Item | Value |
|---|---|
| Language / build | Java 17+, Maven 3.9+ |
| Libraries | Selenium 4.38, Cucumber 7.30 (JUnit Platform), cucumber-picocontainer, Apache Commons CSV, Apache POI (Excel), Allure 3.1 (allure-cucumber7-jvm) |
| Reports | Cucumber HTML and Allure (interactive, or a single HTML file) |
| Design pattern | Page Object Model (POM) |
| Scenarios | **103** (53 written in Gherkin + 50 generated from CSV / Excel rows) |
| Browsers | Chrome, Firefox, Edge |
| Detailed results | [VERIFICATION.md](VERIFICATION.md) |

---

## 1. How to run

**Requirements:** JDK 17+, Maven 3.9+, Python 3, and Chrome, Firefox and/or Edge. Selenium
Manager downloads the matching browser drivers automatically.

**Step 1: start the application.** From the repository root (the folder that contains
`index.html`), leave this running:

```sh
python -m http.server 8000
```

**Step 2: run the tests** from the `selenium-tests` folder:

| Goal | Command |
|---|---|
| All scenarios on Chrome | `mvn test` |
| Another browser | `mvn test -Dbrowser=firefox` or `mvn test -Dbrowser=edge` |
| Parallel execution (4 browsers at once) | `mvn test -Dbrowser=chrome -Dparallel=true -Dthreads=4` |
| Quick smoke run (12 scenarios) | `mvn test -Dtags=@smoke` |
| **All three browsers, one command** | `run-all-browsers.bat` (Windows) or `./run-all-browsers.sh` (Mac / Linux) |
| Watch the browser | add `-Dheadless=false` |

### Reports

Every run produces two reports. A failed scenario has a screenshot attached in both.

| Report | Where | How to open |
|---|---|---|
| Cucumber HTML | `target/cucumber.html` | Double-click it |
| Allure (raw results) | `target/allure-results/` | `mvn allure:serve` (see below) |
| Allure (single file) | `target/allure-report/index.html` | Run `mvn allure:report` once, then double-click the file |

**Allure report.** Allure shows the results as a dashboard:
- **Overview:** pass rate, run time, and an **Environment** box with the browser name and version,
  the operating system and the Java version.
- **Suites:** scenarios grouped by feature.
- **Behaviors:** scenarios grouped by tag (`@smoke`, `@ui`, `@algorithm`, `@data`, ...) and then
  by feature.
- **Each scenario:** its steps, timings and, if it failed, the screenshot.

You don't have to install Allure: the Allure Maven plugin downloads the Allure tool the first
time it is needed.

| Goal | Command (run after the tests) |
|---|---|
| Open the interactive report in a browser (stop it with Ctrl+C) | `mvn allure:serve` |
| Build a **single HTML file** that opens with a double-click, without Allure installed | `mvn allure:report` → `target/allure-report/index.html` |

The single-file report contains everything (data, screenshots, styles) in one HTML file, so it
can be sent to someone or attached to a submission and opened without any tools. If you already
have the Allure command line installed, `allure serve target/allure-results` and
`allure generate --single-file target/allure-results` do the same.

**Reports for every browser.** `run-all-browsers` keeps a separate copy for each browser in
`target/reports/<browser>/`:

```
target/reports/chrome/
├── cucumber.html          Cucumber report
├── allure-report.html     single-file Allure report (double-click)
├── allure-results/        raw Allure results (allure serve target/reports/chrome/allure-results)
└── surefire-reports/      JUnit XML and text summary
```

> **Windows note:** the Allure tool's Windows launcher fails when the project folder path contains
> an `&` (for example `Testing&QA`). The tests and the Cucumber report are not affected. To generate
> Allure reports, keep the project in a folder whose path has no `&`.

---

## 2. Project structure

```
selenium-tests/
├── pom.xml                         dependencies, parallel settings, data-feature generation
├── run-all-browsers.bat / .sh      cross-browser run (Chrome, Firefox, Edge)
└── src/test/
    ├── java/edu/swer312/
    │   ├── RunCucumberTest.java    Cucumber runner (JUnit Platform Suite)
    │   ├── pages/                  PAGE OBJECTS: every locator, action and wait
    │   │   ├── BasePage.java           shared driver, waits, find / click helpers
    │   │   ├── VisualizerPage.java     the page: composes the components below
    │   │   └── components/
    │   │       ├── GraphPanel.java         load graph files, statistics, graph type, examples
    │   │       ├── CanvasComponent.java    draw nodes and edges, tools, node dialogs
    │   │       ├── AlgorithmPanel.java     algorithm drop-down, depth limit, info panel
    │   │       ├── PlaybackControls.java   speed, Start, Pause / Resume, Stop, step buttons
    │   │       ├── ResultsPanel.java       search result and Live Data Panel
    │   │       └── ExportMenu.java         save and export buttons
    │   ├── steps/                  STEP DEFINITIONS (Given / When / Then), one class per area
    │   │   ├── SearchSteps.java        open app, load graph, run search, check result
    │   │   ├── CanvasSteps.java        draw nodes and edges with the mouse
    │   │   ├── PlaybackSteps.java      pause / resume / stop / step the animation
    │   │   ├── ExportSteps.java        save and export, then check the downloaded files
    │   │   ├── UiSteps.java            theme, keyboard, examples, graph type, info panel
    │   │   └── DataDrivenSteps.java    outcome check for the CSV / Excel rows
    │   ├── hooks/Hooks.java        @Before: open browser   @After: screenshot on failure, close browser
    │   ├── driver/DriverFactory.java   creates Chrome / Firefox / Edge (cross-browser)
    │   ├── config/TestConfig.java  command-line settings (browser, headless, baseUrl)
    │   ├── context/ScenarioContext.java  state shared by the steps of one scenario
    │   ├── data/                   reads CSV + Excel, validates rows, generates feature files
    │   └── support/                download-folder and graph-fixture helpers
    └── resources/
        ├── features/               10 hand-written .feature files (Gherkin)
        ├── data/                   searches.csv, edge-cases.xlsx (external test data)
        ├── graphs/                 13 JSON graph files uploaded by the tests
        └── templates/              template for the generated data-driven features
```

---

## 3. Design pattern: Page Object Model (POM)

The application is a single page with several distinct areas, so the page object is built from
**components** (the *page component* form of POM):

```
VisualizerPage  ── has a ──>  GraphPanel        (graph files, statistics, graph type, examples)
                              CanvasComponent   (drawing, tools, node dialogs)
                              AlgorithmPanel    (algorithm, depth limit, info panel)
                              PlaybackControls  (speed, Start, Pause, Stop, step)
                              ResultsPanel      (result panel, Live Data Panel)
                              ExportMenu        (save and export)

All seven classes extend BasePage (driver, explicit waits, find / click helpers).
```

| Class | Responsibility |
|---|---|
| [`BasePage`](src/test/java/edu/swer312/pages/BasePage.java) | Holds the scenario's `WebDriver` and default `WebDriverWait`, plus the helpers every page object needs: `find`, `click` (waits until clickable), `waitUntilVisible`, the fluent `waitForAlertOr`, and the negative wait `staysUnchanged` |
| [`VisualizerPage`](src/test/java/edu/swer312/pages/VisualizerPage.java) | The page itself. It creates the six components and exposes them (`page.graph()`, `page.results()`, ...). It keeps the actions that span the whole page: `open()`, `runSearch()` (uses the playback controls and the results panel), browser dialogs, theme and keyboard shortcuts |
| [`components/`](src/test/java/edu/swer312/pages/components/) | One class per area of the screen, each with its **own locators** and actions: `GraphPanel`, `CanvasComponent`, `AlgorithmPanel`, `PlaybackControls`, `ResultsPanel`, `ExportMenu` |

- **Locators are declared once**, at the top of the component that owns them, as `By` constants:
  ```java
  public class PlaybackControls extends BasePage {
      private static final By START_BUTTON = By.id("btn-start");
      private static final By PAUSE_BUTTON = By.id("btn-pause");
      ...
  ```
- **Actions are methods named after what the user does**, called through the component they
  belong to:
  ```java
  page().graph().load(file, expectedNodes);
  page().algorithm().select("ucs", 5);
  page().runSearch();
  page().results().pathCost();
  ```
  Each method contains the clicks, typing and waits it needs.
- **Step definitions contain no Selenium code and no element IDs** (not a single
  `org.openqa.selenium` import). They only call page objects and assert. If the HTML of one
  area of the app changes, only that component has to be updated.

The rest of the framework is split by responsibility:

| Layer | Class(es) | Responsibility |
|---|---|---|
| Feature files | `features/*.feature` | WHAT is tested, in plain language |
| Step definitions | `steps/*Steps.java` | Map each Gherkin sentence to page actions and assertions |
| Page objects | `pages/BasePage.java`, `pages/VisualizerPage.java`, `pages/components/*` | HOW to do it in the browser |
| Hooks | `hooks/Hooks.java` | Open a fresh browser before each scenario, close it after |
| Driver factory | `driver/DriverFactory.java` | Create the requested browser with the same settings |
| Configuration | `config/TestConfig.java` | Read the command-line settings (`-Dbrowser`, `-Dheadless`, `-DbaseUrl`) in one place |
| Scenario context | `context/ScenarioContext.java` | Share the driver, page and alerts between step classes |

**Sharing state between step classes.** Cucumber's `cucumber-picocontainer` creates a new
`ScenarioContext` for every scenario and passes it to the constructor of every step class and
of `Hooks`:

```java
public class SearchSteps {
    private final ScenarioContext context;
    public SearchSteps(ScenarioContext context) { this.context = context; }
    ...
}
```

So all steps of one scenario use the same browser, while parallel scenarios each get their own.

---

## 4. BDD with Cucumber

Each feature file starts with a user story, uses a `Background` for shared set-up and describes
behaviour in Given / When / Then form. For example,
[`search.feature`](src/test/resources/features/search.feature):

```gherkin
Feature: Search algorithms on a weighted graph
  As a student learning search algorithms
  I want each algorithm to return the path its strategy predicts
  So that the visualizer teaches the correct behaviour

  Background:
    Given the visualizer is ready
    And graph "weighted" is loaded

  @smoke
  Scenario: UCS chooses the cheapest route
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3
```

The sentence `Then the path is "S>B>C>G" with cost 3` is linked to this Java step, which uses
the page object:

```java
@Then("the path is {string} with cost {int}")
public void thePathIsWithCost(String path, int cost) {
    assertPathFound(page(), path);
    assertEquals(cost, page().pathCost(), 0.00001, "path cost");
}
```

Cucumber features used: user-story headers, `Background`, `Scenario Outline` with `Examples`,
data tables (for example `the animation controls are:`), and tags for selecting what to run:

| Tag | Meaning |
|---|---|
| `@smoke` | 12 fast, essential scenarios (`-Dtags=@smoke`) |
| `@regression` | the full suite |
| `@algorithm`, `@ui`, `@export`, `@data` | functional area |
| `@negative` | invalid input or error handling |
| `@known-bug` | documents a real application bug (see 5.3); excluded by default |

---

## 5. Test cases

### 5.1 Hand-written scenarios (10 feature files, 53 scenarios)

| Feature file | Scenarios | What is covered |
|---|---:|---|
| `search.feature` | 8 | Each algorithm returns the path its strategy predicts: BFS (fewest edges), UCS and A\* (cheapest), DFS, Greedy, Bidirectional, DLS failing at depth 1, IDS succeeding at depth 2 |
| `algorithm-comparison.feature` | 2 | A\* explores fewer nodes than UCS for the same route; Greedy is not optimal |
| `graph-loading.feature` | 6 | Loading JSON graph files; directed / undirected detection; unreachable goal; malformed files are rejected without breaking the current graph |
| `canvas-editing.feature` | 6 | Drawing nodes and edges with the mouse; graph-type and node-name dialogs; undirected edges; deleting a node; cancelling a dialog |
| `search-validation.feature` | 5 | Search is refused with a clear alert when there is no graph, no goal, no source, or missing heuristics (Greedy, A\*) |
| `playback.feature` | 5 | Pause / resume, stop, step forward, step back, Space-key shortcut |
| `export.feature` | 6 | JSON, CSV, PNG and SVG exports; save and reload a graph; export refused before a search. **The downloaded files are opened and checked** |
| `examples.feature` | 5 | The four built-in example graphs load and can be searched; cancelling the confirmation |
| `graph-type.feature` | 3 | Switching directed to undirected changes reachability; cancelling keeps it directed |
| `ui.feature` | 7 | Clearing results, depth control visibility, algorithm info panel, keyboard shortcuts, theme toggle |

### 5.2 Data-driven scenarios (50, generated from external files)

| Data file | Rows | What is covered |
|---|---:|---|
| [`searches.csv`](src/test/resources/data/searches.csv) | 31 | Every algorithm on 6 graphs (weighted, tree, multi-goal, undirected, greedy trap, dangling edge), with expected path, cost and visit order |
| [`edge-cases.xlsx`](src/test/resources/data/edge-cases.xlsx), sheet `boundaries` | 7 | Depth-limit boundaries for DLS and IDS (1, 2, 3 and the maximum 20) |
| `edge-cases.xlsx`, sheet `negative` | 12 | Unreachable goals, missing heuristics, the greedy dead end, no source, no goal, empty graph, malformed JSON |

### 5.3 Application bugs found

Four scenarios, tagged `@known-bug`, describe the **correct** behaviour and fail because of real
bugs in the application. They are kept as documentation and excluded from normal runs. Run them
with `mvn test -Dtags=@known-bug`.

| Scenario | Bug |
|---|---|
| A duplicate node name is replaced by a default name | The app compares the new name with internal ids instead of visible names, so a duplicate name is accepted |
| Cancelling the confirmation keeps the current graph | Pressing "Cancel" on the example-graph dialog is ignored and the example is loaded anyway |
| A JSON file without a graph section does not replace the current graph | The app clears the graph before it notices the file is invalid |
| The edge count includes the edges added by switching to undirected | Reverse edges are added, but the statistics panel is not refreshed |

---

## 6. Testing techniques

### 6.1 Cross-browser testing

[`DriverFactory`](src/test/java/edu/swer312/driver/DriverFactory.java) creates the browser chosen
with `-Dbrowser=chrome|firefox|edge`:

```java
WebDriver driver = switch (browser.toLowerCase()) {
    case "chrome"  -> new ChromeDriver(chromeOptions(downloadFolder, headless));
    case "firefox" -> new FirefoxDriver(firefoxOptions(downloadFolder, headless));
    case "edge"    -> new EdgeDriver(edgeOptions(downloadFolder, headless));
    default -> throw new IllegalArgumentException("Unknown browser '" + browser + "'");
};
```

All browsers get the same window size (1440×1100), save downloads to the scenario's own folder
without asking, and leave JavaScript alerts open so the tests can read them.
`run-all-browsers.bat` / `.sh` runs the whole suite on all three browsers with one command.
The full suite passes on Chrome, Firefox and Edge (see section 7).

### 6.2 Parallel testing

Cucumber's JUnit Platform engine runs scenarios in parallel. It is configured in `pom.xml` and
switched on from the command line:

```xml
<cucumber.execution.parallel.enabled>${parallel}</cucumber.execution.parallel.enabled>
<cucumber.execution.parallel.config.strategy>fixed</cucumber.execution.parallel.config.strategy>
<cucumber.execution.parallel.config.fixed.parallelism>${threads}</cucumber.execution.parallel.config.fixed.parallelism>
```

Parallel scenarios cannot interfere with each other, because each scenario has:
- its own browser (created in `@Before`, closed in `@After`),
- its own `ScenarioContext` (created by picocontainer),
- its own download folder, and its own temporary copy of every uploaded graph file.

With 4 threads, the full suite (103 scenarios) runs on Chrome in about 6 minutes.

### 6.3 Data-driven testing

Test data lives in **external files**: a CSV file and an Excel workbook. Each row is one test
case: graph, algorithm, depth limit, expected outcome (`found`, `no_path` or `alert`), expected
path, cost, visit order and message.

```csv
id,description,graph,algorithm,depth,outcome,path,cost,traversal,message,tags
weighted-ucs,UCS finds the cheapest route,weighted,ucs,,found,S>B>C>G,3,S>B>C>G,,@algorithm @smoke
```

How the rows become Cucumber scenarios:

1. `CsvTestDataReader` (Apache Commons CSV) and `ExcelTestDataReader` (Apache POI) read the
   rows. Each Excel sheet is its own data set.
2. `TestDataValidator` checks every row before any browser starts: unknown algorithm, missing
   graph file, duplicate id, bad depth, and so on. A typo fails the build with a clear message.
3. During the Maven build, `DataFeatureGenerator` fills the
   [template](src/test/resources/templates/data-driven.feature.template), a normal
   `Scenario Outline`, with one `Examples` row per data row.
4. Cucumber runs each row as its own named scenario, for example `[weighted-ucs] UCS finds the
   cheapest route`. The step `the search outcome matches` (`DataDrivenSteps`) compares the
   screen with the row.

**To add a test case, add a row to the CSV or Excel file.** No Java or Gherkin needs to change.

### 6.4 Selenium waiting strategies

The app is asynchronous: it starts with Brython (Python in the browser), animates the search,
and opens dialogs and updates statistics after short delays. The project uses **only explicit
waits**: no implicit waits (which should not be mixed with explicit ones) and no `Thread.sleep`.

| Strategy | Where | Why |
|---|---|---|
| `WebDriverWait` + `ExpectedConditions.textToBe` | `open()` | Wait until the app has finished starting (a statistic changes to `0.00`) |
| `ExpectedConditions.elementToBeClickable` | every click | Never click a button that is still disabled |
| `visibilityOfElementLocated` / `invisibilityOfElementLocated` | dialogs | Wait for a dialog to open or close |
| `ExpectedConditions.alertIsPresent` | confirm dialogs | Wait for a JavaScript confirm before accepting or cancelling it |
| **`FluentWait`** (200 ms polling, ignores `UnhandledAlertException` and `NoSuchElementException`) | `waitForAlertOr(...)` | Wait for EITHER a search result OR an error alert, whichever comes first |
| `WebDriverWait` with a custom lambda condition | stats, speed slider, pause button, node-name field | Wait for app-specific states, e.g. statistics showing the expected counts |
| `FluentWait` on the file system | `Downloads.waitForFile` | Wait until a download has finished (no `.part` / `.crdownload` file) |
| Negative wait (a wait that is expected to time out) | `staysUnchanged(...)`, `Downloads.staysEmpty(...)` | Prove that a paused animation does NOT advance for 3 seconds, or that NO file is downloaded, without sleeping |

Example: the fluent wait that handles both outcomes of a search:

```java
return new FluentWait<>(driver)
        .withTimeout(Duration.ofSeconds(45))
        .pollingEvery(Duration.ofMillis(200))
        .ignoring(UnhandledAlertException.class)
        .ignoring(NoSuchElementException.class)
        .until(d -> {
            try {
                Alert alert = d.switchTo().alert();      // the app refused the search
                String text = alert.getText();
                alert.accept();
                return Optional.of(text);
            } catch (NoAlertPresentException noAlert) {   // or a result is shown
                return condition.apply(d) ? Optional.<String>empty() : null;
            }
        });
```

Waiting mattered in practice: Firefox is slower than Chrome, and one check that read the
node-name dialog immediately failed only on Firefox. It now waits explicitly until the dialog is
visible (`waitForNodeNameDialog()`), and passes on every browser.

---

## 7. Results

The full suite was run in parallel (4 threads) on every browser. Details, versions and run
times are in [VERIFICATION.md](VERIFICATION.md).

| Browser | Version | Scenarios | Passed | Failed | Skipped (`@known-bug`) | Time |
|---|---|---:|---:|---:|---:|---:|
| Chrome | 157.0 | 103 | 99 | 0 | 4 | 7 min 38 s |
| Firefox | 161.0 | 103 | 99 | 0 | 4 | 15 min 50 s |
| Edge | 158.0 | 103 | 99 | 0 | 4 | 8 min 13 s |
| **Total** | | **309** | **297** | **0** | **12** | |

The 4 skipped scenarios per browser are the `@known-bug` scenarios from section 5.3, which are
excluded on purpose.

---

## 8. Requirement checklist

| Assignment requirement | Where it is implemented |
|---|---|
| 1. Automated testing design pattern (POM) | `pages/BasePage.java`, `pages/VisualizerPage.java`, `pages/components/*`; layers described in section 3 |
| 2. BDD with Cucumber, Gherkin feature files linked to Java steps | `features/*.feature`, `steps/*Steps.java`, `RunCucumberTest.java` |
| 3. At least 10 BDD scenarios covering different functionality | 103 scenarios across 10 feature files and 3 data sets (section 5) |
| 4a. Cross-browser testing | `driver/DriverFactory.java`, `run-all-browsers.bat/.sh`; passes on Chrome, Firefox, Edge |
| 4b. Parallel testing | Cucumber parallel settings in `pom.xml`; isolation per scenario (section 6.2) |
| 4c. Data-driven testing (Excel, CSV) | `data/searches.csv`, `data/edge-cases.xlsx`, `edu/swer312/data/*` (section 6.3) |
| 4d. Selenium waiting strategies | explicit and fluent waits in `BasePage`, the page components and `Downloads` (section 6.4) |
