package ch.admin.bag.covidcertificate.signature.config;

import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import ch.admin.bag.covidcertificate.signature.service.JksKeystoreProvider;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.function.Supplier;

@Configuration
@Profile(ProfileRegistry.PROFILE_HSM_MOCK)
@Slf4j
public class HsmMockConfig {
    private static final String SIGNING_ALGORITHM = "SHA512withRSA";
    private static final String ALIAS = "mock";
    private static final String ALIAS_LIGHT = "mock-light";

    private static final String KEYSTORE_RESOURCE = "keystore.jks";

    private static final String KEY_STORE_PASSWORD = "secret";

    @Bean
    public KeyStoreEntryReader keyStoreEntryReader() {
        log.info("--------------> Login MOCK HSM");
        var keystoreProvider = new JksKeystoreProvider(KEYSTORE_RESOURCE, KEY_STORE_PASSWORD);
        var keyStore = keystoreProvider.loadKeyStore();
        return new KeyStoreEntryReader(keystoreProvider, keyStore, KEY_STORE_PASSWORD.toCharArray());
    }

    @Bean
    Supplier<Signature> euSigningSignatureSupplier(KeyStoreEntryReader keyStoreEntryReader) {
        return () -> initSignature(keyStoreEntryReader, ALIAS);
    }

    @Bean
    Supplier<Signature> lightSigningSignatureSupplier(KeyStoreEntryReader keyStoreEntryReader) {
        return () -> initSignature(keyStoreEntryReader, ALIAS_LIGHT);
    }

    private Signature initSignature(KeyStoreEntryReader keyStoreEntryReader, String privateKeyAlias){
        Signature signature;
        try {
            signature = Signature.getInstance(SIGNING_ALGORITHM);
            signature.initSign(keyStoreEntryReader.getPrivateKey(privateKeyAlias));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new SignatureCreationException(
                    String.format("Failed to initialize signature with algorithm %s and key for alias %s", SIGNING_ALGORITHM, privateKeyAlias),
                    e);
        }
        return signature;
    }
}
