package ch.admin.bag.covidcertificate.signature.service.cms;

import ch.admin.bag.covidcertificate.signature.api.CMSSigningResponseDto;
import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CMSSigningService {
    private final CMSSigner cmsSigner;

    public CMSSigningResponseDto sign(String alias, KeyStoreSlot slot) {
        String signature;
        try {
            signature = cmsSigner.sign(alias, slot);
        } catch (CertificateEncodingException | IOException e) {
            throw new SignatureCreationException(String.format("Failed to sign certificate with alias %s from slot %s", alias, slot.getSlotNumber()), e);
        }

        return new CMSSigningResponseDto(
                signature
        );

    }

    public CMSSigningResponseDto sign(byte[] data) {
        String signature;
        try {
            signature = cmsSigner.sign(data);
        } catch (CertificateEncodingException | IOException e) {
            throw new SignatureCreationException("Failed to sign data", e);
        }

        return new CMSSigningResponseDto(signature);
    }
}
