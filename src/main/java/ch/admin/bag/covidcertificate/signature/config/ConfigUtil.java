package ch.admin.bag.covidcertificate.signature.config;

import org.springframework.core.io.FileSystemResource;

public class ConfigUtil {

    public static final String EXT_CONFIG_DIR_KEY = "ext.config.dir";

    private ConfigUtil() {
    }

    public static FileSystemResource getResource(String filename) {
        return new FileSystemResource(System.getProperty(EXT_CONFIG_DIR_KEY) + "/" + filename);
    }
}
