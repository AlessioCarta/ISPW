package com.ispw.uniride.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom wrapper for java.util.logging.Logger.
 * Configured to avoid IO bottlenecks of System.out.println
 */
public class LoggerCustom {

    private static final Logger logger = Logger.getLogger(LoggerCustom.class.getName());

    static {
        // Setup base logging level and handlers if needed
        logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }

    private LoggerCustom() {
        // Private constructor for utility class
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void error(String message, Throwable thrown) {
        logger.log(Level.SEVERE, message, thrown);
    }
}
