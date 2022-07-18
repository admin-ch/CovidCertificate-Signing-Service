package ch.admin.bag.covidcertificate.signature.web.controller;


import ch.admin.bag.covidcertificate.signature.api.SigningRequestDto;
import ch.admin.bag.covidcertificate.signature.api.VerifyRequestDto;
import ch.admin.bag.covidcertificate.signature.service.SigningService;
import com.flextrade.jfixture.JFixture;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.FileNotFoundException;

import static ch.admin.bag.covidcertificate.signature.FixtureCustomization.customizeSigningRequestDto;
import static ch.admin.bag.covidcertificate.signature.FixtureCustomization.customizeVerifyRequestDto;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"hsm-mock"})
@TestPropertySources({
        @TestPropertySource("file:src/test/resources/application-test.properties"),
        @TestPropertySource("file:src/test/resources/application-test-mtls.properties")
})
class CryptoControllerIntegrationLocalTest {

    @LocalServerPort
    int localServerPort;

    @Autowired
    private SigningService signingService;

    private static final JFixture fixture = new JFixture();
    private static final String CBOR_CONTENT_TYPE = "application/cbor";

    @BeforeEach
    private void init() {
        customizeSigningRequestDto(fixture);
        customizeVerifyRequestDto(fixture, signingService);
    }

    @Nested
    class Sign {
        private final String URL = "/sign";

        @Test
        void returnsStatusCode200_whenClientUsesValidCertificateAndTrustsServerCertificate() throws FileNotFoundException {
            request("client-keystore.jks")
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(fixture.create(SigningRequestDto.class)).post(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void fails_whenClientUsesCertificateNotTrustedByTheServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-certificate-not-trusted-by-the-server.jks")
                            .contentType(APPLICATION_JSON_VALUE)
                            .body(fixture.create(SigningRequestDto.class)).post(URL));
        }

        @Test
        void fails_whenClientUsesCertificateDoesNotTrustTheCertificateOfServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-missing-server-certificate-from-truststore.jks")
                            .contentType(APPLICATION_JSON_VALUE)
                            .body(fixture.create(SigningRequestDto.class)).post(URL));
        }
    }

    @Nested
    class SignLight {
        private final String URL = "/sign-light";

        @Test
        void returnsStatusCode200_whenClientUsesValidCertificateAndTrustsServerCertificate() throws FileNotFoundException {
            request("client-keystore.jks")
                    .contentType(CBOR_CONTENT_TYPE)
                    .body(fixture.create(byte[].class)).post(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void fails_whenClientUsesCertificateNotTrustedByTheServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-certificate-not-trusted-by-the-server.jks")
                            .contentType(CBOR_CONTENT_TYPE)
                            .body(fixture.create(byte[].class)).post(URL));
        }

        @Test
        void fails_whenClientUsesCertificateDoesNotTrustTheCertificateOfServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-missing-server-certificate-from-truststore.jks")
                            .contentType(CBOR_CONTENT_TYPE)
                            .body(fixture.create(byte[].class)).post(URL));
        }
    }

    @Nested
    class Verify {
        private final String URL = "/sign/verify";

        @Test
        void returnsStatusCode200_whenClientUsesValidCertificateAndTrustsServerCertificate() throws FileNotFoundException {
            request("client-keystore.jks")
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(fixture.create(VerifyRequestDto.class)).post(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void fails_whenClientUsesCertificateNotTrustedByTheServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-certificate-not-trusted-by-the-server.jks")
                            .contentType(APPLICATION_JSON_VALUE)
                            .body(fixture.create(VerifyRequestDto.class)).post(URL));
        }

        @Test
        void fails_whenClientUsesCertificateDoesNotTrustTheCertificateOfServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-missing-server-certificate-from-truststore.jks")
                            .contentType(APPLICATION_JSON_VALUE)
                            .body(fixture.create(VerifyRequestDto.class)).post(URL));
        }
    }

    @Nested
    class GetKid {
        private final String URL = "/sign/configuration/kid/mock";

        @Test
        void returnsStatusCode200_whenClientUsesValidCertificateAndTrustsServerCertificate() throws FileNotFoundException {
            request("client-keystore.jks")
                    .contentType(TEXT_PLAIN_VALUE)
                    .get(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void fails_whenClientUsesCertificateNotTrustedByTheServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-certificate-not-trusted-by-the-server.jks")
                            .contentType(TEXT_PLAIN_VALUE)
                            .get(URL));
        }

        @Test
        void fails_whenClientUsesCertificateDoesNotTrustTheCertificateOfServer() {
            assertThrows(SSLHandshakeException.class,
                    () -> request("client-keystore-with-missing-server-certificate-from-truststore.jks")
                            .contentType(TEXT_PLAIN_VALUE)
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