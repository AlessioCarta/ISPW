package com.ispw.uniride;

import com.ispw.uniride.boundary.LoginCLI;
import com.ispw.uniride.utils.LoggerCustom;

/**
 * Entry point for the CLI version of the application.
 * Designed to run in headless environments.
 */
public class CliMain {
    public static void main(String[] args) {
        LoggerCustom.info("Starting UniRide CLI Mode...");
        new LoginCLI().start();
    }
}
