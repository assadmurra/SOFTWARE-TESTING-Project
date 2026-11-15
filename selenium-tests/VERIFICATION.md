# Verification report

Assignment PDF was successfully extracted and read. Implementation uses the supplied `SelProj.zip` checkout with origin `ashfaknawshad/aisearch-v2`. No application source, styles, vendor files or startup scripts were changed: every non-Git file from the original application archive was compared byte-for-byte. The archive arrived with CRLF differences relative to its Git index; those are pre-existing and were preserved.

## Files added

- `.gitignore`, `pom.xml`, `README.md`, `VERIFICATION.md`
- `src/test/java/edu/swer312/RunCucumberTest.java`
- `src/test/java/edu/swer312/Steps.java`
- `src/test/java/edu/swer312/VisualizerPage.java`
- `src/test/resources/features/search.feature`, `ui.feature`, `data.feature`
- `src/test/resources/data/searches.csv`
- `src/test/resources/graphs/weighted.json`, `disconnected.json`

All paths are relative to `selenium-tests/`. Application changes: none.

## Setup and diagnoses

The application was started with:

```sh
python3 -m http.server 8000 --directory work/SelProj/aisearch-v2
```

Maven 3.9.11 and Temurin JDK 21.0.13 were provisioned under `work/tools/`. Maven's local repository and Java user home were kept under `work/cache/`. The initial environment had Java runtime 21 but no matching JDK compiler; the exact compile blocker was `error: release version 17 not supported`. A project-local JDK fixed it. Java/Maven also needed the managed proxy and CA store for initial dependency downloads; ordinary direct resolution failed with `Network is unreachable`. No TLS verification was disabled. The final tests ran from the downloaded dependency cache.

ChromeDriver first exercised preinstalled Chromium 156.0.8078.79. The smoke passed (1 executed, 15 filtered); the first full run passed 15 and errored in IDS because the test tried to edit its hidden depth field. The test was fixed to select DLS, set the visible depth field, then select IDS. Corrected serial and parallel Chromium runs passed all 16.

Google Chrome 157.0.8103.78 was then downloaded from Google's official Linux distribution, with matching ChromeDriver 157.0.8103.x, and independently smoke-tested before its full runs. Chrome and Chromium are reported separately below.

Firefox 160.0 with geckodriver 0.36.0 initially failed all 16 scenario setup hooks in the command sandbox with `Process unexpectedly closed with status 1`. A direct reproduction printed `Could not find profile folder`. The identical browser started successfully outside that command sandbox, and the full runs were retried there. This is an executor restriction; no workaround or sandbox change was added to the submitted Java code.

Edge 155.0.4283.62 with matching EdgeDriver initially failed UI setup because its small default window made native select options unavailable (`ElementNotInteractableException: Element is not currently visible and may not be manipulated`). Setting a common 1440×1100 browser window fixed the test setup. No application change was required.

Cucumber uses a fixed scenario pool and an explicit maximum pool size of three for the final parallel runs. Each scenario creates and closes its own WebDriver. Google Chrome and Firefox verification used isolated copies of the same Maven sources to avoid overwriting another browser's reports during simultaneous verification. Their final parallel runs use the submitted Java sources.

Selenium printed CDP-version warnings for these newer browsers. The tests use the standard WebDriver API and do not require CDP. JUnit also printed a non-fatal classpath-selector discovery warning. Neither warning prevented execution.

## Local reproduction

Install JDK 17+ (including javac), Maven 3.9+, and the browsers. Start the server from the application root, then run the commands in README.md. They do not require this executor's proxy, tool paths, or sandbox configuration. Explicit local driver and browser paths can be supplied if automatic driver resolution is unavailable.

The tested requirements and independent expected paths/costs are documented in README.md. No incorrect algorithm result was observed in the asserted cases. The greedy implementation's documented single-branch behavior and IDS's hidden depth input remain application limitations; the suite does not claim to prove algorithm completeness.

## Final outcomes

| Browser | Mode | Passed | Failed / errored | Filtered |
|---|---|---:|---:|---:|
| Google Chrome 157.0.8103.78 | Smoke | 1 | 0 / 0 | 15 |
| Google Chrome 157.0.8103.78 | Serial | 16 | 0 / 0 | 0 |
| Google Chrome 157.0.8103.78 | Parallel (3) | 16 | 0 / 0 | 0 |
| Firefox 160.0 | Serial | 16 | 0 / 0 | 0 |
| Firefox 160.0 | Parallel (3) | 16 | 0 / 0 | 0 |
| Edge 155.0.4283.62 | Serial | 16 | 0 / 0 | 0 |
| Edge 155.0.4283.62 | Parallel (3) | 16 | 0 / 0 | 0 |
| Chromium 156.0.8078.79 | Serial | 16 | 0 / 0 | 0 |
| Chromium 156.0.8078.79 | Parallel (3) | 16 | 0 / 0 | 0 |

The smoke run executed one scenario and filtered fifteen. Every full run executed all sixteen with zero skipped scenarios. All listed results were executed; none are unverified. Parallel full runs used three scenario workers. Total final full-run executions across Google Chrome, Firefox, and Edge: **96 passed, 0 failed, 0 errored, 0 skipped**. Chromium runs are additional verification.

HTML, JSON and JUnit XML reports are included in the submission archive under `verification/<run>/`. `verification/outcomes.json` contains these counts. The browser logs retain versions, outcomes and non-fatal warnings. Earlier failed attempts are explained above and retained as diagnostic logs.
