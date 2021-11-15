package loaders;

import exceptions.InvalidResourcePath;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

public class ResourceLoader {
    private static final Logger LOG = LogManager.getLogger(ResourceLoader.class);
    private static final String PREFIX_PROPERTIES = "properties/";

    public Properties getPropertiesAP(String resourcePath) throws IOException {
        String path = resourcePath;
        Properties properties = new Properties();
        LOG.info("loading properties: " + path);
        properties.load(getInputStream(path));
        return properties;
    }


    public Properties getProperties(String resourcePath) throws IOException {
        String path = PREFIX_PROPERTIES + resourcePath;
        Properties properties = new Properties();
        LOG.info("loading properties: " + path);
        properties.load(getInputStream(path));
        return properties;
    }

    public Path getPath(String resourcePath) throws InvalidResourcePath {
        URL url = getClass().getClassLoader().getResource(resourcePath);

        try {
            URI uri = url.toURI();
            try {
                FileSystems.newFileSystem(uri, new HashMap<>());
            } catch (Exception ignored) {

            }
            return Paths.get(uri);
        } catch (NullPointerException | URISyntaxException e) {
            throw new InvalidResourcePath("failed to locate resource: " + resourcePath);
        }
    }

    public InputStream getInputStream(String resourcePath) {
        return getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    public InputStream getInputStream(final Path path) throws IOException {
        return Files.newInputStream(path);
    }
}
