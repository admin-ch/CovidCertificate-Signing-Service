package ch.admin.bag.covidcertificate.signature.service;

import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignatureVerificationService {

    @Resource(name = "keyStoreEntryReaderMap")
    private final Map<KeyStoreSlot, KeyStoreEntryReader> keyStoreEntryReaderMap;

    private final ObjectProvider<Signature> signatureProvider;
    private final Supplier<Signature> signatureSupplier;


    public boolean verifySignature(byte[] dataToSign, byte[] signature, String certificateAlias, KeyStoreSlot slot) {
        var verifySignature = this.signatureProvider.getIfAvailable(signatureSupplier);
        var certificate = keyStoreEntryReaderMap.get(slot).getCertificate(certificateAlias);

        try {
            verifySignature.initVerify(certificate);
            verifySignature.update(dataToSign);
            return verifySignature.verify(signature);

        } catch (InvalidKeyException e) {
            throw new SignatureCreationException(String.format("Failed to initialize verification for alias %s", certificateAlias), e);
        } catch (SignatureException e) {
            throw new SignatureCreationException(String.format("Unable to verify signature for alias %s", certificateAlias), e);
        }
    }

}
