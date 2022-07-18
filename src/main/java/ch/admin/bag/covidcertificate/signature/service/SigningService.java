package ch.admin.bag.covidcertificate.signature.service;

import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Map;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class SigningService {

    @Resource(name = "keyStoreEntryReaderMap")
    private final Map<KeyStoreSlot, KeyStoreEntryReader> keyStoreEntryReaderMap;

    private final ObjectProvider<Signature> signatureProvider;
    private final Supplier<Signature> signatureSupplier;

    @Value("${crs.decryption.aliasSignLight}")
    private String aliasSignLight;

    @Value("${crs.decryption.lightKeyStoreSlot}")
    private KeyStoreSlot lightKeyStoreSlot;


    public byte[] sign(String privateKeyAlias, KeyStoreSlot slot, String base64Message) throws SignatureException {
        var signature = initSignature(privateKeyAlias, slot);
        var message = Base64.getDecoder().decode(base64Message);
        signature.update(message);
        return signature.sign();
    }

    public byte[] sign(String privateKeyAlias, KeyStoreSlot slot, byte[] message) throws SignatureException {
        var signature = initSignature(privateKeyAlias, slot);
        signature.update(message);
        return signature.sign();
    }

    public byte[] signLight(byte[] message) throws SignatureException {
        var signature = initSignature(aliasSignLight, lightKeyStoreSlot);
        signature.update(message);
        return signature.sign();
    }

    private Signature initSignature(String privateKeyAlias, KeyStoreSlot slot) {
        var signature = this.signatureProvider.getIfAvailable(signatureSupplier);

        try {
            signature.initSign(keyStoreEntryReaderMap.get(slot).getPrivateKey(privateKeyAlias));
        } catch (InvalidKeyException e) {
            throw new SignatureCreationException(String.format("Failed to initialize signature for alias %s", privateKeyAlias), e);
        }

        return signature;
    }
}
