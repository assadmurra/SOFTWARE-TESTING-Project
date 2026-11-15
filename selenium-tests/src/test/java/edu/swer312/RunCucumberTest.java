package edu.swer312;
import org.junit.platform.suite.api.*;
@Suite @IncludeEngines("cucumber") @SelectClasspathResource("features")
@ConfigurationParameter(key="cucumber.glue",value="edu.swer312")
@ConfigurationParameter(key="cucumber.plugin",value="pretty,html:target/cucumber.html,json:target/cucumber.json")
public class RunCucumberTest {}