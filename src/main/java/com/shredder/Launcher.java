package com.shredder;

/**
 * Plain (non-Application) entry point.
 *
 * <p>When a fat jar's Main-Class extends {@link javafx.application.Application},
 * the JVM refuses to start with "JavaFX runtime components are missing" unless
 * JavaFX is on the module path. Launching through a separate class that merely
 * calls {@code Application.launch} sidesteps that check, so the shaded jar runs
 * from the classpath and the packaged .app launches on a double-click.
 */
public final class Launcher {
    public static void main(String[] args) {
        ShredderApp.main(args);
    }
}
