package org.pmcsn.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private final Properties properties = new Properties();

    public ConfigurationManager() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration file", e);
        }
    }

    public boolean getBoolean(String section, String key) {
        String value = properties.getProperty(section + "." + key);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value)) {
            return false;
        } else {
            throw new IllegalArgumentException("Key not found: " + section + "." + key);
        }
    }

    public String getString(String section, String key) {
        String value = properties.getProperty(section + "." + key);
        if (value == null) {
            throw new IllegalArgumentException("Key not found: " + section + "." + key);
        }
        return value;
    }

    public int getInt(String section, String key) {
        String value = properties.getProperty(section + "." + key);
        if (value == null) {
            throw new IllegalArgumentException("Key not found: " + section + "." + key);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value for key: " + section + "." + key, e);
        }
    }

    public double getDouble(String section, String key) {
        String value = properties.getProperty(section + "." + key);
        if (value == null) {
            throw new IllegalArgumentException("Key not found: " + section + "." + key);
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid double value for key: " + section + "." + key, e);
        }
    }
}

