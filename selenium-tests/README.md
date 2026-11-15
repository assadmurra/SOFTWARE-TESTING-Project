# SWER312 Selenium assignment

Standalone Java 17+ / Maven 3.9+ Selenium 4 and Cucumber project. Application files are unchanged. The uploaded Selenium Project.pdf is the assignment source: POM, Cucumber, 10+ scenarios, cross-browser, parallel, external data, and waiting strategies. Group size and submission date are administrative requirements (up to three students; 30 November 2026).

## Run

Install Python 3, a **JDK** (17 or newer, including `javac`), Maven 3.9+, and Chrome, Firefox or Edge. Selenium Manager resolves matching drivers when not supplied explicitly. Internet is needed for initial Maven dependencies and driver resolution. From the application repository root:

```sh
python3 -m http.server 8000
```

Keep this terminal running. In another terminal:

```sh
cd selenium-tests
mvn test -Dbrowser=chrome -Dcucumber.filter.tags=@smoke
mvn test -Dbrowser=chrome
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge
mvn test -Dbrowser=chrome -Dparallel=true -Dthreads=3
mvn test -Dbrowser=firefox -Dparallel=true -Dthreads=3
mvn test -Dbrowser=edge -Dparallel=true -Dthreads=3
```

Optional: `-Dheadless=false`, `-DbaseUrl=http://localhost:8000`, `-DbrowserBinary=/path/to/browser`. To bypass driver downloads, use `-Dwebdriver.chrome.driver=/path/to/chromedriver`, `-Dwebdriver.gecko.driver=/path/to/geckodriver`, or `-Dwebdriver.edge.driver=/path/to/msedgedriver` respectively. Proxy settings belong in your Maven settings, not project code.

Reports: `target/cucumber.html`, `target/cucumber.json`, and `target/surefire-reports/`. Failed scenarios attach screenshots. Archive reports before another invocation because paths are reused.

## Application inspection and fixtures

This checkout serves `index.html`, `main.py`, `SearchAgent.py`, and local vendor libraries directly; no npm/build/backend is needed. `start-server.sh` also invokes Python's HTTP server but has Windows line endings in the supplied archive; the command above avoids that issue.

The UI supports BFS, DFS, DLS, IDS, UCS, bidirectional, greedy, and A*. It offers algorithm/depth selectors, graph type, canvas editing tools, speed/play/pause/stop/step, graph save/load, clear path, examples, theme and export controls. Tests cover seven algorithms; bidirectional is supported but outside this suite's selected coverage.

Load Graph's hidden `file-input` accepts JSON through native Selenium upload. The saved format is `metadata.is_undirected` plus `graph.nodes`, `graph.source`, and `graph.goals`. Each node supplies `name`, `x`, `y`, `heuristic`, and a `neighbors` object mapping names to numeric weights. Fixtures reproduce the exported format. A temporary file is unique to every scenario and removed after visible statistics confirm loading. Each scenario starts from a fresh browser; no shared mutable driver, static page, or application state exists.

Readiness is verified by selecting DLS and waiting for its depth control to appear, proving Brython's event handler is active. Graph readiness waits for node and edge statistics. Search completion waits for `.status-success` or `.status-failure` inside `search-results` and for Start to re-enable. No-path completion is therefore explicit, not inferred from an empty path during animation. The visible `path-list`, `traversal-list`, results cost and graph statistics provide assertions. No JavaScript reads or mutates algorithm state. JavaScript is used only to scroll controls into view; inputs and clicks use WebDriver. There is no Thread.sleep.

## Independent expected results

Weighted directed graph: S→A=8, A→G=8, S→B=1, B→C=1, C→G=1. Exactly two routes exist. BFS minimizes edges, so S→A→G wins with 2 edges and cost 16. UCS minimizes accumulated cost, so S→B→C→G wins with 3 edges and cost 3. A* has h(S)=3, h(A)=1, h(B)=2, h(C)=1, h(G)=0: these values are admissible and consistent on every edge, giving the same cost-3 route. Expected costs are sums of fixture edges, not values obtained from the application's algorithm.

DFS sorts neighbors by (x,y), then pushes them in reverse order; A is left of B, so S,A,G is the unique expected traversal and path. Greedy chooses A because h(A)=1 < h(B)=2, then G. DLS limit 1 cannot reach either goal route; IDS limit 2 reaches S,A,G. The disconnected graph has only S→A and isolated G.

The app's greedy implementation explicitly commits to only one neighbor and documents that it can get stuck. This differs from conventional frontier-based Greedy Best-First Search. The selected greedy test covers its supported heuristic choice; it does not establish completeness. No algorithm correctness failure should be inferred solely from this source observation.

IDS UI limitation: select DLS to expose and set Depth Limit, then select IDS. The app hides the field for IDS but reads its current value when starting IDS. The page object uses this supported UI sequence rather than changing a hidden input. All browsers use a 1440×1100 window to keep desktop controls visible. The worker pool maximum is also capped at the requested thread count.

## Scenarios and assignment mapping

There are 12 named scenarios plus 4 CSV example executions, totaling 16. Even without counting CSV examples, there are more than 10 distinct scenarios.

| Requirement | Evidence |
|---|---|
| Standalone Maven / Java / Selenium | `pom.xml` |
| Page Object Model | `VisualizerPage.java`: all selectors, UI interaction and waits |
| Cucumber Given/When/Then and assertions | `RunCucumberTest.java`, `Steps.java`, `src/test/resources/features/` |
| Seven algorithm scenarios | `search.feature`: BFS, UCS, A*, DFS traversal, greedy, DLS failure, IDS success |
| Multiple UI features | `ui.feature`: saved graph counts and connectivity, disconnected goal, clear/rerun, depth visibility, theme |
| External CSV | `data/searches.csv`, `data.feature`, `Steps.csv`: runtime Commons CSV parsing of graph/algorithm/depth/result/path/cost |
| Three browsers | Scenario-scoped `Steps.start` browser factory, CLI commands above |
| Parallel scenario execution | Cucumber JUnit Platform fixed pool properties in `pom.xml`; each scenario gets its own Steps and browser |
| Cleanup and diagnostics | `Steps.finish`: screenshot on failure, driver.quit in finally |
| Explicit waits | `VisualizerPage.open/load/run/clear` |
| Documentation and discussion | This README and `VERIFICATION.md` |

The Examples table chooses CSV row IDs only; expected results and graph/algorithm parameters are read from CSV at runtime. Each row becomes an independent Cucumber scenario with its own browser, including under parallel execution.

To explain the layers: `search.feature` states the expected user behavior in plain language. `Steps.run` maps its When sentence to page selection and run methods. `Steps.path` asserts the expected labels and numeric cost. `VisualizerPage` locates controls, uploads the graph, waits for completion, and reads visible output. The feature and assertions contain test intent; the page object contains browser mechanics.
