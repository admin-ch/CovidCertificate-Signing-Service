package ch.admin.bag.covidcertificate.signature.service;

import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class SigningService {
    private final KeyStoreEntryReader keyStoreEntryReader;
    private final ObjectProvider<Signature> signatureProvider;
    private final Supplier<Signature> signatureSupplier;

    @Value("${crs.decryption.aliasSignLight}")
    private String aliasSignLight;


    public byte[] sign(String privateKeyAlias, String base64Message) throws SignatureException {
        var signature = initSignature(privateKeyAlias);
        var message = Base64.getDecoder().decode(base64Message);
        signature.update(message);
        return signature.sign();
    }

    public byte[] sign(String privateKeyAlias, byte[] message) throws SignatureException {
        var signature = initSignature(privateKeyAlias);
        signature.update(message);
        return signature.sign();
    }

    private Signature initSignature(String privateKeyAlias)  {
        var signature = this.signatureProvider.getIfAvailable(signatureSupplier);

        try {
            signature.initSign(keyStoreEntryReader.getPrivateKey(privateKeyAlias));
        } catch (InvalidKeyException e) {
            throw new SignatureCreationException(String.format("Failed to initialize signature for alias %s", privateKeyAlias), e);
        }

        return signature;
    }

    private Signature initSignatureLight(){
        return initSignature(aliasSignLight);
    }

    public byte[] signLight(byte[] message) throws SignatureException {
        var signature = initSignatureLight();
        signature.update(message);
        return signature.sign();
    }
}
