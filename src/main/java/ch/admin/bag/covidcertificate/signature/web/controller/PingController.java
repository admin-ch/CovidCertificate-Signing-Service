package ch.admin.bag.covidcertificate.signature.web.controller;

import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import ch.admin.bag.covidcertificate.signature.service.SigningService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.GeneralSecurityException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
final class PingController {
    private final SigningService signingService;

    @Value("${crs.decryption.defaultKeyStoreSlot}")
    private KeyStoreSlot defaultKeyStoreSlot;

    @Value("${app.signing-service.keystore.monitoring.liveness-test-private-key}")
    private String livenessTestKeyAlias;

    @Operation(
            summary = "Executes a health check: Signs a random messages to verify we can get connection to the KeyStore."
    )
    @GetMapping(value = "/ping")
    public boolean ping() throws GeneralSecurityException {
        log.debug("Calling health endpoint");
        var message = UUID.randomUUID().toString().getBytes();
        try {
            signingService.sign(livenessTestKeyAlias, defaultKeyStoreSlot, message);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
