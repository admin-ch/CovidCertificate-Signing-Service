package ch.admin.bag.covidcertificate.signature.web.controller;

import com.flextrade.jfixture.JFixture;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.FileNotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"hsm-mock"})
@TestPropertySource(properties = {"app.signing-service.monitor.prometheus.user=prometheus",
        "app.signing-service.monitor.prometheus.password={noop}secret",
        "app.signing-service.keystore.private-key-alias=mock",
        "app.signing-service.keystore.signing-certificate-alias=mock"
})
class CMSControllerIntegrationTest {
    @LocalServerPort
    int localServerPort;

    private final JFixture fixture = new JFixture();

    @Nested
    class Sign{
        private final String URL = "/v1/cms/";

        @Test
        void returnsStatusCode200_whenCorrectRequest() throws FileNotFoundException {
            request().get(URL+"mock")
                    .then()
                    .assertThat()
                    .statusCode(200);
        }

        @Test
        void returnsStatusCode404_whenAliasDoesNotExist() throws FileNotFoundException {
            request().get(URL+fixture.create(String.class))
                    .then()
                    .assertThat()
                    .statusCode(404);
        }
    }

    private RequestSpecification request() throws FileNotFoundException {
        return RestAssured.given()
                .baseUri("http://localhost")
                .port(localServerPort);
    }

}
