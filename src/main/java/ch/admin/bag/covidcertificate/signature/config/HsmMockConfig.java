package ch.admin.bag.covidcertificate.signature.config;

import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import ch.admin.bag.covidcertificate.signature.service.JksKeystoreProvider;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.function.Supplier;

@Configuration
@Profile(ProfileRegistry.PROFILE_HSM_MOCK)
@Slf4j
public class HsmMockConfig {
    private static final String SIGNING_ALGORITHM = "SHA512withRSA";

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
    Supplier<Signature> signatureSupplier() {
        return this::getSignatureInstance;
    }

    private Signature getSignatureInstance()  {
        log.debug("signingSignature with {}", SIGNING_ALGORITHM);
        Signature signature;

        try {
            signature = Signature.getInstance(SIGNING_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureCreationException(
                    String.format("Failed to get signature instance for algorithm %s", SIGNING_ALGORITHM),
                    e);
        }

        return signature;
    }
}
