package ch.admin.bag.covidcertificate.signature.web.controller;

import ch.admin.bag.covidcertificate.signature.api.SigningRequestDto;
import ch.admin.bag.covidcertificate.signature.api.VerifyRequestDto;
import ch.admin.bag.covidcertificate.signature.service.KidService;
import ch.admin.bag.covidcertificate.signature.service.SignatureVerificationService;
import ch.admin.bag.covidcertificate.signature.service.SigningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;

@RestController
@RequiredArgsConstructor
@Slf4j
final class CryptoController {
    private final SigningService signingService;
    private final KidService kidService;
    private final SignatureVerificationService signatureVerificationService;

    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] sign(@RequestBody SigningRequestDto signingRequestDto) throws GeneralSecurityException {
        log.info("Signing new EU Certificate");
        var signature = signingService.sign(signingRequestDto.getSigningKeyAlias(), signingRequestDto.getDataToSign());
        log.info("EU Certificate successfully signed.");
        return signature;
    }

    @PostMapping(value = "/sign/{alias}", consumes = MediaType.APPLICATION_CBOR_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] sign(@PathVariable String alias, @RequestBody byte[] message) throws GeneralSecurityException {
        log.info("Signing new EU Certificate");
        var signature = signingService.sign(alias, message);
        log.info("EU Certificate successfully signed.");
        return signature;
    }

    @PostMapping(value = "/sign-light", consumes = MediaType.APPLICATION_CBOR_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] signLight(@RequestBody byte[] message) throws GeneralSecurityException {
        log.info("Signing new Certificate light");
        var signature = signingService.signLight(message);
        log.info("Certificate light successfully signed.");
        return signature;
    }

    @GetMapping(value = "/sign/configuration/kid/{alias}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getKid(@PathVariable String alias) throws CertificateException, IOException, NoSuchAlgorithmException {
        var aliasDecoded = UriUtils.decode(alias, "UTF-8");
        log.info("Signing certificate with alias {}", aliasDecoded);
        return kidService.getKid(aliasDecoded);
    }

    @PostMapping(value = "/sign/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean verify(@RequestBody VerifyRequestDto verifyRequestDto) {
        log.info("Verifying signature with {}.", verifyRequestDto.getCertificateAlias());
        var message = Base64.getDecoder().decode(verifyRequestDto.getDataToSign());
        var signature = Base64.getDecoder().decode(verifyRequestDto.getSignature());
        var signatureVerified = signatureVerificationService.verifySignature(message, signature, verifyRequestDto.getCertificateAlias());
        if(!signatureVerified){
            log.info("Signature could not be verified with {}.", verifyRequestDto.getCertificateAlias());
        }else{
            log.info("Signature successfully verified with {}.", verifyRequestDto.getCertificateAlias());
        }
        return signatureVerified;
    }
}
