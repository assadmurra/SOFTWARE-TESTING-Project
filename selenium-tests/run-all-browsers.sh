#!/bin/sh
# ============================================================================
#  Cross-browser testing: runs the whole suite on Chrome, Firefox and Edge,
#  one browser after another. Inside each browser the scenarios run in parallel.
#
#  Before running: start the application from the repository root with
#      python3 -m http.server 8000
#
#  Usage:  ./run-all-browsers.sh                 (all scenarios)
#          ./run-all-browsers.sh -Dtags=@smoke   (only the smoke scenarios)
#
#  Reports: target/reports/<browser>/cucumber.html
# ============================================================================
cd "$(dirname "$0")" || exit 1
result=0
rm -rf target/reports

for browser in chrome firefox edge; do
    echo
    echo "===== Running on $browser ====="
    mvn -q test -Dbrowser="$browser" -Dparallel=true -Dthreads=4 "$@" || result=1

    mkdir -p "target/reports/$browser"
    cp target/cucumber.html target/cucumber.json "target/reports/$browser/"
    cp -r target/surefire-reports "target/reports/$browser/"
done

echo
echo "===== Summary ====="
for browser in chrome firefox edge; do
    echo "$browser: $(grep 'Tests run' "target/reports/$browser/surefire-reports/edu.swer312.RunCucumberTest.txt")"
done
echo "Reports are in target/reports/<browser>/cucumber.html"
exit $result
