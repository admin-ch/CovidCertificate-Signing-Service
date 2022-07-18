package ch.admin.bag.covidcertificate.signature.web.controller;

import ch.admin.bag.covidcertificate.signature.api.CMSSigningRequestDto;
import ch.admin.bag.covidcertificate.signature.api.CMSSigningResponseDto;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import ch.admin.bag.covidcertificate.signature.service.cms.CMSSigningService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.util.Base64;


/**
 * The CMSController (Certificate Management System) is used to provide the DSCs (Document Signer Certificates = "Signign Keys") to a caller.
 * A DSC can be requested by its alias. The DSC will be signed using a specific X509 Certificate which has been shared with the EU.
 * Additionally, a sample payload can also be signed using this specific X509 Certificate.
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/v1/cms")
final class CMSController {
    private final CMSSigningService cmsSigningService;

    @Operation(
            summary = "Loads a certificate and signs it with the certificate for Upload to the EU gateway"
    )
    @GetMapping(value = "/slots/{slotNumber}/alias/{alias}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CMSSigningResponseDto sign(@PathVariable Integer slotNumber, @PathVariable String alias) {
        var aliasDecoded = UriUtils.decode(alias, "UTF-8");
        var keyStoreSlot = KeyStoreSlot.fromSlotNumber(slotNumber);
        log.info("Signing certificate with alias {} from slot {} for upload to EU gateway", aliasDecoded, keyStoreSlot);
        return cmsSigningService.sign(aliasDecoded, keyStoreSlot);
    }

    @Operation(
            summary = "Signs the provided data with the certificate for upload to the EU gateway"
    )
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public CMSSigningResponseDto signBytes(@RequestBody CMSSigningRequestDto data) {
        log.info("Signing data for upload to EU gateway");
        var dataDecoded = Base64.getDecoder().decode(data.getData());
        return cmsSigningService.sign(dataDecoded);
    }
}


