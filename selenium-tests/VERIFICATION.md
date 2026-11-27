# Test Results

Final verification of the Selenium + Cucumber suite described in [README.md](README.md).

## Environment

| Item | Value |
|---|---|
| Date | 27 November 2026 |
| Operating system | Windows 11 Pro |
| Java / Maven | Temurin JDK 21.0.7 (compiled for Java 17), Apache Maven 3.9.12 |
| Selenium / Cucumber | Selenium 4.38.0, Cucumber 7.30.0 (JUnit Platform) |
| Reporting | Cucumber HTML / JSON, Allure (allure-cucumber7-jvm 3.1.1, Allure report 2.46.2 via allure-maven 3.1.0) |
| Application server | `python -m http.server 8000` from the repository root |
| Browsers | Google Chrome 157.0.8103.80, Mozilla Firefox 161.0, Microsoft Edge 158.0.4370.33 |
| Drivers | Resolved automatically by Selenium Manager |
| Mode | Headless, parallel with 4 threads (`-Dparallel=true -Dthreads=4`) |
| Command | `run-all-browsers.bat` (runs the three browsers one after another) |

## Final results

| Browser | Scenarios | Passed | Failed | Errors | Skipped | Time |
|---|---:|---:|---:|---:|---:|---:|
| Chrome 157 | 103 | 99 | 0 | 0 | 4 | 458.2 s |
| Firefox 161 | 103 | 99 | 0 | 0 | 4 | 949.9 s |
| Edge 158 | 103 | 99 | 0 | 0 | 4 | 493.3 s |
| **Total** | **309** | **297** | **0** | **0** | **12** | |

The skipped scenarios are the 4 `@known-bug` scenarios. They describe correct behaviour that the
application does not have yet (see README section 5.3) and are excluded by the default tag filter
`not @known-bug`.

Running only the bug scenarios on Chrome (`mvn test -Dtags=@known-bug`) executed the 4 scenarios,
and all 4 failed as expected, confirming the bugs are still present:

| Scenario | Failure |
|---|---|
| A duplicate node name is replaced by a default name | no alert `Node "A" already exists` was shown |
| Cancelling the confirmation keeps the current graph | 4 edges instead of 5: the example was loaded although Cancel was pressed |
| A JSON file without a graph section does not replace the current graph | the search did not run: the graph had been cleared |
| The edge count includes the edges added by switching to undirected | the statistics show 1 edge instead of 2 |

Smoke runs (`run-all-browsers.bat -Dtags=@smoke` and `./run-all-browsers.sh -Dtags=@smoke`)
executed the 12 `@smoke` scenarios on each browser with 0 failures.

## Reports

Each browser run produces:

- `target/reports/<browser>/cucumber.html`: readable report with every scenario and step
- `target/reports/<browser>/allure-report.html`: single-file Allure report (opens with a double-click)
- `target/reports/<browser>/allure-results/`: raw Allure results (`allure serve <folder>`)
- `target/reports/<browser>/cucumber.json`: machine-readable results
- `target/reports/<browser>/surefire-reports/`: JUnit XML and text summary

`target/` is rebuilt by Maven, so copy these folders elsewhere if they need to be kept.

### Allure checks

| Check | Result |
|---|---|
| One Allure result per executed scenario | 99 per browser in the full run, 12 per browser in the smoke runs |
| Separate Allure results and single-file report per browser | `run-all-browsers.bat` (full suite) and `run-all-browsers.sh` (smoke): `chrome/`, `firefox/`, `edge/` each have their own `allure-results/` and `allure-report.html` |
| Environment info | Each report shows its own browser and version (chrome 157.0.8103.80, firefox 161.0, MicrosoftEdge 158.0.4370.33), plus OS, Java, headless mode and application URL |
| Grouping | Suites tab: by feature (13 feature suites in the full run). Behaviors tab: by tag (`@smoke`, `@ui`, `@algorithm`, `@data`, `@negative`, ...) |
| Screenshot on failure | `mvn test -Dtags=@known-bug`: all 4 failed scenarios have a PNG screenshot attached, and the 4 screenshots are embedded in the single-file report |
| Single-file report | Opened in Chrome from disk (no server, no Allure installed): the Firefox report shows "99 test cases, 100%", the Environment box and all suites |
| `mvn allure:serve` | Starts a local server and opens the interactive report (HTTP 200) |

## Problems found and fixed during verification

| Problem | Cause | Fix |
|---|---|---|
| Firefox only: "The first node asks for the graph type and a name" failed (*The node name dialog should be open*) | The step checked the dialog once, immediately. The app opens it a moment after the graph-type dialog closes, and Firefox under parallel load is slower than Chrome and Edge | Replaced the immediate check with an explicit wait (`waitForNodeNameDialog()` / `waitForGraphTypeDialog()`). The full Firefox suite passed afterwards |
| Edge: native drop-down options could not be selected | Edge's small default window hid part of the controls | Every browser now uses the same 1440×1100 window (`DriverFactory`) |
| Allure report generation failed on Windows (`'...\Testing' is not recognized as an internal or external command`) | The project folder was named `Testing&QA`; Windows treats `&` as a command separator, so Allure's launcher `allure.bat` broke on the path | Folder renamed to `Testing-QA`; the Allure tool is installed in the user folder (`~/.allure`), outside the project. README has a Windows note |
| Every run printed *"The classpath resource selector 'features' should not be used to select features in a package"* | The runner selected the features with `@SelectClasspathResource` | Changed to `@SelectPackages("features")`. A dry run confirmed all 103 scenarios are still found, and the warning is gone |
| Starting a page could occasionally hang under parallel load | Brython loads `.py` files with synchronous requests, and the simple Python server can drop one under load | `open()` waits up to 30 s for start-up and reloads the page once (logged) before failing |

## Application issues (not test failures)

Besides the 4 `@known-bug` scenarios, two documented application limitations are taken into
account by the tests:

- **IDS depth input:** the app hides the depth field for IDS but still reads its value. The page
  object therefore selects DLS, sets the depth, then selects IDS, using only visible controls.
- **Greedy search** commits to one neighbour and can get stuck (the app documents this). The data
  row `trap-greedy` expects exactly that, and `search.feature` only tests its heuristic choice.
