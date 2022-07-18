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

import java.io.FileNotFoundException;
import java.util.Base64;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"hsm-mock"})
@TestPropertySource("file:src/test/resources/application-test.properties")
class CMSControllerIntegrationTest {
    @LocalServerPort
    int localServerPort;

    private final JFixture fixture = new JFixture();

    @Nested
    class Sign {
        private final String URL = "/v1/cms/slots/{slotNumber}/alias/{alias}";

        @Test
        void returnsStatusCode200_whenCorrectRequest() throws FileNotFoundException {
            String validUrl = URL
                    .replace("{slotNumber}", "0")
                    .replace("{alias}", "mock");

            request().get(validUrl)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void returnsStatusCode404_whenAliasDoesNotExist() throws FileNotFoundException {
            String invalidUrl = URL
                    .replace("{slotNumber}", "0")
                    .replace("{alias}", fixture.create(String.class));

            request().get(invalidUrl)
                    .then()
                    .assertThat()
                    .statusCode(404);
        }
    }

    @Nested
    class SignBytes {
        private final String URL = "/v1/cms/";

        @Test
        void returnsStatusCode200_whenCorrectRequest() throws FileNotFoundException {
            var byteArrayData = fixture.create(byte[].class);
            var requestDto = new CMSSigningRequestDto(Base64.getEncoder().encodeToString(byteArrayData));

            request().body(requestDto)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .post(URL)
                    .then()
                    .assertThat()
                    .statusCode(200);
        }
    }

    private RequestSpecification request() throws FileNotFoundException {
        return RestAssured.given()
                .baseUri("http://localhost")
                .port(localServerPort);
    }

}
