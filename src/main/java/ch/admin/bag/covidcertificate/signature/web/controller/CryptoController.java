package ch.admin.bag.covidcertificate.signature.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.function.Supplier;

@RestController
@RequiredArgsConstructor
@Slf4j
final class CryptoController {
    private final ObjectProvider<Signature> signingSignature;
    private final @Qualifier("euSigningSignatureSupplier") Supplier<Signature> euSigningSignatureSupplier;
    private final @Qualifier("lightSigningSignatureSupplier") Supplier<Signature> lightSigningSignatureSupplier;

    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_CBOR_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] sign(@RequestBody byte[] message) throws GeneralSecurityException {
        log.info("Signing new EU Certificate");
        var signature = this.signingSignature.getIfAvailable(euSigningSignatureSupplier);
        signature.update(message);
        byte[] signedMessage = signature.sign();
        log.info("EU Certificate successfully signed.");
        return signedMessage;
    }

    @PostMapping(value = "/sign-light", consumes = MediaType.APPLICATION_CBOR_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] signLight(@RequestBody byte[] message) throws GeneralSecurityException {
        log.info("Signing new Certificate light");
        var signature = this.signingSignature.getIfAvailable(lightSigningSignatureSupplier);
        signature.update(message);
        byte[] signedMessage = signature.sign();
        log.info("Certificate light successfully signed.");
        return signedMessage;
    }
}
