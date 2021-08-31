package ch.admin.bag.covidcertificate.signature.web.controller;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"hsm-mock"})
@TestPropertySource("file:src/test/resources/application-test.properties")
class CmsControllerIntegrationLocalTest {

    @LocalServerPort
    int localServerPort;

    @Nested
    class Sign {
        private final String URL = "/v1/cms/mock";

        @Test
        void returnsStatusCode200_whenClientUsesValidCertificateAndTrustsServerCertificate() throws FileNotFoundException {
            request("client-keystore.jks")
                    .get(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void fails_whenClientUsesCertificateNotTrustedByTheServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-certificate-not-trusted-by-the-server.jks")
                            .get(URL));
        }

        @Test
        void fails_whenClientUsesCertificateDoesNotTrustTheCertificateOfServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-missing-server-certificate-from-truststore.jks")
                            .get(URL));
        }
    }

    private RequestSpecification request(String keystore) throws FileNotFoundException {
        return RestAssured.given()
                .baseUri("https://localhost")
                .port(localServerPort)
                .keyStore(getFile(keystore), "secret")
                .trustStore(getFile(keystore), "secret");
    }

    private File getFile(String keystoreFilename) throws FileNotFoundException {
        return ResourceUtils.getFile("src/test/resources/"+keystoreFilename);
    }
}