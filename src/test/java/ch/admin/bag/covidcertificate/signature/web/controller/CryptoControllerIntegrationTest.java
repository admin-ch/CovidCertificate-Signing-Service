package ch.admin.bag.covidcertificate.signature.web.controller;


import ch.admin.bag.covidcertificate.signature.api.SigningRequestDto;
import ch.admin.bag.covidcertificate.signature.api.VerifyRequestDto;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import ch.admin.bag.covidcertificate.signature.service.KidService;
import ch.admin.bag.covidcertificate.signature.service.SigningService;
import com.flextrade.jfixture.JFixture;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Map;

import static ch.admin.bag.covidcertificate.signature.FixtureCustomization.customizeSigningRequestDto;
import static ch.admin.bag.covidcertificate.signature.FixtureCustomization.customizeVerifyRequestDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"hsm-mock"})
@TestPropertySource("file:src/test/resources/application-test.properties")
class CryptoControllerIntegrationTest {

    @Resource(name="keyStoreEntryReaderMap")
    private Map<KeyStoreSlot, KeyStoreEntryReader> keyStoreEntryReaderMap;

    @Autowired
    private SigningService signingService;
    @MockBean
    private KidService kidService;

    @MockBean
    private KeyStoreEntryReader keyStoreEntryReader;

    @LocalServerPort
    int localServerPort;

    private static final JFixture fixture = new JFixture();
    private static final String CBOR_CONTENT_TYPE = "application/cbor";

    @BeforeEach
    private void init() {
        keyStoreEntryReader = keyStoreEntryReaderMap.get(KeyStoreSlot.SLOT_NUMBER_0);
        customizeSigningRequestDto(fixture);
        customizeVerifyRequestDto(fixture, signingService);
    }


    @Nested
    class Sign {
        private final String URL = "/sign";

        @Test
        void returnsStatusCode200_whenCorrectRequest() throws FileNotFoundException {
            request()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(fixture.create(SigningRequestDto.class)).post(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void returnsCorrectSignature_whenCorrectRequest() throws FileNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
            var input = fixture.create(SigningRequestDto.class);
            var message = Base64.getDecoder().decode(input.getDataToSign());
            byte[] body = request()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(input).post(URL)
                    .then()
                    .extract()
                    .body().asByteArray();

            assertTrue(verifySignature(message, body, "mock"));
        }
    }

    @Nested
    class SignLight {
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

    @Nested
    class Verify {
        private final String URL = "/sign/verify";

        @Test
        void returnsStatusCode200_whenCorrectRequest() throws FileNotFoundException {
            request()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(fixture.create(VerifyRequestDto.class)).post(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void returnsTrue_ifCorrectSignature() throws FileNotFoundException {
            var input = fixture.create(VerifyRequestDto.class);
            var body = request()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(input).post(URL)
                    .then()
                    .extract()
                    .body().asString();

            assertEquals("true", body);
        }
    }

    @Nested
    class GetKid {
        private final String URL = "/sign/configuration/kid/mock";

        @BeforeEach
        void setup() throws CertificateException, IOException, NoSuchAlgorithmException {
            when(kidService.getKid(any(), any())).thenReturn(fixture.create(String.class));
        }

        @Test
        void returnsStatusCode200_whenCorrectRequest() throws FileNotFoundException {
            request()
                    .contentType(TEXT_PLAIN_VALUE)
                    .get(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void returnsCorrectKid_whenCorrectRequest() throws IOException, CertificateException, NoSuchAlgorithmException {
            var kid = fixture.create(String.class);
            when(kidService.getKid(any(), any())).thenReturn(kid);

            var body = request()
                    .contentType(TEXT_PLAIN_VALUE)
                    .get(URL)
                    .then()
                    .extract()
                    .body().asString();

            assertEquals(kid, body);
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
