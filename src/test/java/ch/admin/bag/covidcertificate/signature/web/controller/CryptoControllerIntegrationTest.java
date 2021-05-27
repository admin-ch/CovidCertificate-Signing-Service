package ch.admin.bag.covidcertificate.signature.web.controller;


import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import com.flextrade.jfixture.JFixture;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"hsm-mock"})
class CryptoControllerIntegrationTest {

    @Autowired
    private KeyStoreEntryReader keyStoreEntryReader;

    @LocalServerPort
    int localServerPort;

    private final JFixture fixture = new JFixture();
    private static final String CBOR_CONTENT_TYPE = "application/cbor";

    @Test
    void returnsStatusCode200_whenCorrectRequest() throws FileNotFoundException {
        request()
                .contentType(CBOR_CONTENT_TYPE)
                .body(fixture.create(byte[].class)).post("/sign")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    void returnsCorrectSignature_whenCorrectRequest() throws FileNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        byte[] input = fixture.create(byte[].class);
        byte[] body = request()
                .contentType(CBOR_CONTENT_TYPE)
                .body(input).post("/sign")
                .then()
                .extract()
                .body().asByteArray();

        assertTrue(verifySignature(input, body));
    }

    @Test
    void returnsStatusCode415_whenContentTypeIsFalse() throws FileNotFoundException {
        request()
                .contentType(ContentType.TEXT)
                .body(fixture.create(byte[].class)).post("/sign")
                .then()
                .assertThat()
                .statusCode(415);
    }


    private RequestSpecification request() throws FileNotFoundException {
        return RestAssured.given()
                .baseUri("http://localhost")
                .port(localServerPort);
    }

    private boolean verifySignature(byte[] input, byte[] signature) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature verificationSignature = Signature.getInstance("SHA512withRSA");
        verificationSignature.initVerify(keyStoreEntryReader.getCertificate("mock"));
        verificationSignature.update(input);
        return verificationSignature.verify(signature);
    }

}