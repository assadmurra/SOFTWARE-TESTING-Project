package edu.swer312.config;

/**
 * All settings that can be changed from the command line, in one place.
 *
 * <p>Example: {@code mvn test -Dbrowser=firefox -Dheadless=false}
 */
public final class TestConfig {

    private TestConfig() {
    }

    /** Browser to test with: "chrome" (default), "firefox" or "edge". */
    public static String browser() {
        return System.getProperty("browser", "chrome");
    }

    /** True (default) to run the browser without a visible window. */
    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", "true"));
    }

    /** Address of the application under test. */
    public static String baseUrl() {
        return System.getProperty("baseUrl", "http://localhost:8000");
    }

    /** Optional path to a browser executable that is not installed in the default location. */
    public static String browserBinary() {
        return System.getProperty("browserBinary");
    }
}
