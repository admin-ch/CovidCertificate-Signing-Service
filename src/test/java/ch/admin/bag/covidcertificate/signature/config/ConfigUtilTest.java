package ch.admin.bag.covidcertificate.signature.config;

import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigUtilTest {
    private final JFixture fixture = new JFixture();

    @Test
    void getResource_returnsCorrectFileSystemResource() {
        String filename = fixture.create(String.class);
        String expected = System.getProperty("ext.config.dir") + "/" + filename;
        FileSystemResource actual = ConfigUtil.getResource(filename);

        assertEquals(expected, actual.getPath());
    }
}