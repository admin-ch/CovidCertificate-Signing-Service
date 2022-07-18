package ch.admin.bag.covidcertificate.signature.web.controller;


import ch.admin.bag.covidcertificate.signature.api.CMSSigningRequestDto;
import com.flextrade.jfixture.JFixture;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"hsm-mock"})
@TestPropertySources({
        @TestPropertySource("file:src/test/resources/application-test.properties"),
        @TestPropertySource("file:src/test/resources/application-test-mtls.properties")
})
class CmsControllerIntegrationLocalTest {

    @LocalServerPort
    int localServerPort;

    private final JFixture fixture = new JFixture();

    @Nested
    class Sign {
        private final String URL = "/v1/cms/slots/0/alias/mock";

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

    @Nested
    class SignBytes {
        private final String URL = "/v1/cms/";

        @Test
        void returnsStatusCode200_whenClientUsesValidCertificateAndTrustsServerCertificate() throws FileNotFoundException {
            var byteArrayData = fixture.create(byte[].class);
            var requestDto = new CMSSigningRequestDto(Base64.getEncoder().encodeToString(byteArrayData));

            request("client-keystore.jks")
                    .body(requestDto)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .post(URL)
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
        return ResourceUtils.getFile("src/test/resources/" + keystoreFilename);
    }
}