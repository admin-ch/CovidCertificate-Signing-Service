package ch.admin.bag.covidcertificate.signature.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.GeneralSecurityException;
import java.security.Signature;

@RestController
@RequiredArgsConstructor
@Slf4j
final class CryptoController {

    private final Signature signingSignature;

    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_CBOR_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] sign(@RequestBody byte[] message) throws GeneralSecurityException {
        log.info("Signing new Message");
        this.signingSignature.update(message);
        return this.signingSignature.sign();
    }
}
