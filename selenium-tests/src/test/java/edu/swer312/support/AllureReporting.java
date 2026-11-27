package edu.swer312.support;

import edu.swer312.config.TestConfig;
import io.qameta.allure.Allure;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openqa.selenium.Capabilities;

/**
 * Extra information for the Allure report. Reporting only - it does not change what is tested.
 *
 * <ul>
 *   <li>Environment: browser name and version, operating system and Java version are written once
 *       per run to {@code environment.properties}, which Allure shows on its overview page.</li>
 *   <li>Grouping by tag: every Cucumber tag (except {@code @regression}, which every scenario has)
 *       becomes an Allure "epic" label, so the Behaviors tab groups scenarios by tag and then by
 *       feature. Allure already groups by feature in the Suites tab.</li>
 * </ul>
 */
public final class AllureReporting {

    private static final String RESULTS_DIRECTORY_PROPERTY = "allure.results.directory";
    private static final String TAG_ON_EVERY_SCENARIO = "@regression";

    /** Parallel scenarios all start a browser; only the first one writes the environment file. */
    private static final AtomicBoolean environmentWritten = new AtomicBoolean(false);

    private AllureReporting() {
    }

    /** Adds one Allure "epic" label per Cucumber tag of the current scenario. */
    public static void groupByTags(Collection<String> tags) {
        for (String tag : tags) {
            if (!tag.equals(TAG_ON_EVERY_SCENARIO)) {
                Allure.label("epic", tag);
            }
        }
    }

    /** Writes environment.properties into the Allure results folder (first call of the run only). */
    public static void writeEnvironmentOnce(Capabilities browser) {
        if (!environmentWritten.compareAndSet(false, true)) {
            return;
        }
        Properties environment = new Properties();
        environment.setProperty("Browser", browser.getBrowserName());
        environment.setProperty("Browser.Version", browser.getBrowserVersion());
        environment.setProperty("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        environment.setProperty("Java", System.getProperty("java.version"));
        environment.setProperty("Headless", String.valueOf(TestConfig.headless()));
        environment.setProperty("Application.URL", TestConfig.baseUrl());

        Path resultsDirectory = Path.of(System.getProperty(RESULTS_DIRECTORY_PROPERTY, "allure-results"));
        try {
            Files.createDirectories(resultsDirectory);
            try (OutputStream out = Files.newOutputStream(resultsDirectory.resolve("environment.properties"))) {
                environment.store(out, "Test environment shown in the Allure report");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the Allure environment file", e);
        }
    }
}
