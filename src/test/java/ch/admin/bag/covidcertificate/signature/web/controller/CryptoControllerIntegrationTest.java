package ch.admin.bag.covidcertificate.signature.web.controller;


import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import com.flextrade.jfixture.JFixture;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"hsm-mock"})
@TestPropertySource(properties = {"app.signing-service.monitor.prometheus.user=prometheus",
        "app.signing-service.monitor.prometheus.password={noop}secret",
        "app.signing-service.keystore.private-key-alias=mock",
        "app.signing-service.keystore.signing-certificate-alias=mock"
})
class CryptoControllerIntegrationTest {

    @Autowired
    private KeyStoreEntryReader keyStoreEntryReader;

    @LocalServerPort
    int localServerPort;

    private final JFixture fixture = new JFixture();
    private static final String CBOR_CONTENT_TYPE = "application/cbor";

    @Nested
    class Sign{
        private final String URL = "/sign";

        @Test
        void returnsStatusCode200_whenCorrectRequest() throws FileNotFoundException {
            request()
                    .contentType(CBOR_CONTENT_TYPE)
                    .body(fixture.create(byte[].class)).post(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void returnsCorrectSignature_whenCorrectRequest() throws FileNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
            byte[] input = fixture.create(byte[].class);
            byte[] body = request()
                    .contentType(CBOR_CONTENT_TYPE)
                    .body(input).post(URL)
                    .then()
                    .extract()
                    .body().asByteArray();

            assertTrue(verifySignature(input, body, "mock"));
        }

        @Test
        void returnsStatusCode415_whenContentTypeIsFalse() throws FileNotFoundException {
            request()
                    .contentType(ContentType.TEXT)
                    .body(fixture.create(byte[].class)).post(URL)
                    .then()
                    .assertThat()
                    .statusCode(415);
        }
    }

    @Nested
    class SignLight{
        private final String URL = "/sign-light";
        @Test
        void returnsStatusCode200_whenCorrectRequest() throws FileNotFoundException {
            request()
                    .contentType(CBOR_CONTENT_TYPE)
                    .body(fixture.create(byte[].class)).post(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void returnsCorrectSignature_whenCorrectRequest() throws FileNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
            byte[] input = fixture.create(byte[].class);
            byte[] body = request()
                    .contentType(CBOR_CONTENT_TYPE)
                    .body(input).post(URL)
                    .then()
                    .extract()
                    .body().asByteArray();

            assertTrue(verifySignature(input, body, "mock-light"));
        }

        @Test
        void returnsStatusCode415_whenContentTypeIsFalse() throws FileNotFoundException {
            request()
                    .contentType(ContentType.TEXT)
                    .body(fixture.create(byte[].class)).post(URL)
                    .then()
                    .assertThat()
                    .statusCode(415);
        }
    }


    private RequestSpecification request() throws FileNotFoundException {
        return RestAssured.given()
                .baseUri("http://localhost")
                .port(localServerPort);
    }

    private boolean verifySignature(byte[] input, byte[] signature, String certificateAlias) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature verificationSignature = Signature.getInstance("SHA512withRSA");
        verificationSignature.initVerify(keyStoreEntryReader.getCertificate(certificateAlias));
        verificationSignature.update(input);
        return verificationSignature.verify(signature);
    }

}
