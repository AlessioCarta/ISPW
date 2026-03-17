package com.ispw.uniride.config;

/**
 * Configuration class that manages global application settings.
 * Specifically handles the active persistence mechanism for the application.
 */
public class Config {

    /**
     * Enum representing the supported types of Data Persistence.
     */
    public enum DBType {
        /**
         * Uses local in-memory data structures (e.g., HashMaps) for rapid prototyping and O(1) access.
         * Data is lost when the application is closed.
         */
        MEMORY,

        /**
         * Saves data physically in CSV format on the local filesystem.
         * Utilizes thread-safe locks and static caching for read performance.
         */
        FILESYSTEM
    }

    // Costante per switchare tra le due implementazioni del DAOFactory
    public static final DBType PERSISTENCE_TYPE = DBType.MEMORY;

    private Config() {
        // Hide implicit public constructor for utility class
    }
}
