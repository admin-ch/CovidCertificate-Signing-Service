package ch.admin.bag.covidcertificate.signature.web.controller;

import ch.admin.bag.covidcertificate.signature.api.SigningRequestDto;
import ch.admin.bag.covidcertificate.signature.api.VerifyRequestDto;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import ch.admin.bag.covidcertificate.signature.service.KidService;
import ch.admin.bag.covidcertificate.signature.service.SignatureVerificationService;
import ch.admin.bag.covidcertificate.signature.service.SigningService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * The CryptoController uses "Signing Keys" (DSC = Document Signer Certificates) which are stored in separate an HSM (Hardware Security Module)
 * to sign Covid Certificates (DCC = EU Digital COVID Certificate)
 */
@RestController
@RequiredArgsConstructor
@Slf4j
final class CryptoController {
    private final SigningService signingService;
    private final KidService kidService;
    private final SignatureVerificationService signatureVerificationService;

    @Value("${crs.decryption.defaultKeyStoreSlot}")
    private KeyStoreSlot defaultKeyStoreSlot;

    @Operation(
            summary = "Signs the dataToSign (typically a DCC) with the requested signing key"
    )
    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] sign(@RequestBody SigningRequestDto signingRequestDto) throws GeneralSecurityException {
        KeyStoreSlot slot = signingRequestDto.getKeyStoreSlot() != null ? signingRequestDto.getKeyStoreSlot() : defaultKeyStoreSlot;
        log.info("Signing certificate using slot {}", slot);
        var signature = signingService.sign(signingRequestDto.getSigningKeyAlias(), slot, signingRequestDto.getDataToSign());
        log.info("Certificate successfully signed.");
        return signature;
    }

    @Operation(
            summary = "Signs the provided message (typically a light-certificate) with the light-certificate signing key"
    )
    @PostMapping(value = "/sign-light", consumes = MediaType.APPLICATION_CBOR_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] signLight(@RequestBody byte[] message) throws GeneralSecurityException {
        log.info("Signing light certificate");
        var signature = signingService.signLight(message);
        log.info("Certificate light successfully signed.");
        return signature;
    }

    @Deprecated(since = "signing-service 4.3 (Replaced with /sign/configuration/kid/{slot}/{alias})")
    @GetMapping(value = "/sign/configuration/kid/{alias}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getKid(@PathVariable String alias) throws CertificateException, IOException, NoSuchAlgorithmException {
        var aliasDecoded = UriUtils.decode(alias, "UTF-8");
        log.info("Reading kid from certificate with alias {}", aliasDecoded);
        return kidService.getKid(aliasDecoded, defaultKeyStoreSlot);
    }

    @Operation(
            summary = "Returns the key identifier which is calculated using the first 8 bytes of the signing key"
    )
    @GetMapping(value = "/sign/configuration/kid/{slotNumber}/{alias}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getKidFromSlot(@PathVariable Integer slotNumber, @PathVariable String alias) throws CertificateException, IOException, NoSuchAlgorithmException {
        var aliasDecoded = UriUtils.decode(alias, "UTF-8");
        var keyStoreSlot = KeyStoreSlot.fromSlotNumber(slotNumber);
        log.info("Reading kid from certificate with alias {} from slot {}", aliasDecoded, slotNumber);
        return kidService.getKid(aliasDecoded, keyStoreSlot);
    }

    @Operation(
            summary = "Verifies a signature (typically only used for testing)"
    )
    @PostMapping(value = "/sign/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean verify(@RequestBody VerifyRequestDto verifyRequestDto) {
        KeyStoreSlot slot = verifyRequestDto.getKeyStoreSlot() != null ? verifyRequestDto.getKeyStoreSlot() : defaultKeyStoreSlot;
        log.info("Verifying signature with {} on slot {}.", verifyRequestDto.getCertificateAlias(), slot);
        var message = Base64.getDecoder().decode(verifyRequestDto.getDataToSign());
        var signature = Base64.getDecoder().decode(verifyRequestDto.getSignature());
        var signatureVerified = signatureVerificationService.verifySignature(message, signature, verifyRequestDto.getCertificateAlias(), slot);
        if (!signatureVerified) {
            log.info("Signature could not be verified with {} on slot {}.", verifyRequestDto.getCertificateAlias(), slot);
        } else {
            log.info("Signature successfully verified with {} on slot {}.", verifyRequestDto.getCertificateAlias(), slot);
        }
        return signatureVerified;
    }
}
