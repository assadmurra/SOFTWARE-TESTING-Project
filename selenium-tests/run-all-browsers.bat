@echo off
REM ============================================================================
REM  Cross-browser testing: runs the whole suite on Chrome, Firefox and Edge,
REM  one browser after another. Inside each browser the scenarios run in parallel.
REM
REM  Before running: start the application from the repository root with
REM      python selenium-tests\tools\serve.py
REM  (like "python -m http.server 8000", but it does not drop connections when
REM   several browsers load the app at the same time)
REM
REM  Usage:  run-all-browsers.bat                 (all scenarios)
REM          run-all-browsers.bat -Dtags=@smoke   (only the smoke scenarios)
REM
REM  Reports per browser, in target\reports\<browser>\ :
REM      cucumber.html        Cucumber HTML report
REM      allure-results\      raw Allure results (open with: allure serve <folder>)
REM      allure-report.html   single-file Allure report (open with a double-click)
REM ============================================================================
setlocal
cd /d "%~dp0"
set RESULT=0
if exist target\reports rmdir /s /q target\reports

for %%B in (chrome firefox edge) do (
    echo.
    echo ===== Running on %%B =====
    call mvn -q test -Dbrowser=%%B -Dparallel=true -Dthreads=4 %*
    if errorlevel 1 set RESULT=1

    mkdir target\reports\%%B
    copy /y target\cucumber.html target\reports\%%B\ >nul
    copy /y target\cucumber.json target\reports\%%B\ >nul
    xcopy /y /q /i target\surefire-reports target\reports\%%B\surefire-reports >nul
    xcopy /y /q /i /e target\allure-results target\reports\%%B\allure-results >nul

    call mvn -q allure:report
    if errorlevel 1 (
        echo WARNING: the Allure report for %%B could not be generated
    ) else (
        copy /y target\allure-report\index.html target\reports\%%B\allure-report.html >nul
    )
)

echo.
echo ===== Summary =====
for %%B in (chrome firefox edge) do (
    echo %%B:
    findstr /c:"Tests run" target\reports\%%B\surefire-reports\edu.swer312.RunCucumberTest.txt
)
echo Reports are in target\reports\^<browser^>\ (cucumber.html, allure-report.html, allure-results\)
exit /b %RESULT%
