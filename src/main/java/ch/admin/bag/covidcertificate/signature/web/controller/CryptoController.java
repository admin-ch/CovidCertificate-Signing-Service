package ch.admin.bag.covidcertificate.signature.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
    private final Supplier<Signature> signingSignatureSupplier;

    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_CBOR_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] sign(@RequestBody byte[] message) throws GeneralSecurityException {
        log.info("Signing new Message");
        var signature = this.signingSignature.getIfAvailable(signingSignatureSupplier);
        signature.update(message);
        return signature.sign();
    }
}
