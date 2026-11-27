package edu.swer312;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Runs every feature file under src/test/resources/features (plus the features generated from
 * the CSV / Excel data) with the step definitions, hooks and page objects in package edu.swer312.
 *
 * <p>Browser, parallel execution and tag filtering are set from the command line, see pom.xml.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = "cucumber.glue", value = "edu.swer312")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty,html:target/cucumber.html,json:target/cucumber.json,"
        + "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
public class RunCucumberTest {
}
