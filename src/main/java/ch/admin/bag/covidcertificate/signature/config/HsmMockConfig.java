package ch.admin.bag.covidcertificate.signature.config;

import ch.admin.bag.covidcertificate.signature.service.JksKeystoreProvider;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.GeneralSecurityException;
import java.security.Signature;

@Configuration
@Profile(ProfileRegistry.PROFILE_HSM_MOCK)
@Slf4j
public class HsmMockConfig {
    private static final String SIGNING_ALGORITHM = "SHA512withRSA";
    private static final String ALIAS = "mock";

    private static final String KEYSTORE_RESOURCE = "keystore.jks";

    private static final String KEY_STORE_PASSWORD = "secret";

    @Bean
    public KeyStoreEntryReader keyStoreEntryReader() {
        log.info("--------------> Login MOCK HSM");
        var keyStore = new JksKeystoreProvider(KEYSTORE_RESOURCE, KEY_STORE_PASSWORD).loadKeyStore();
        return new KeyStoreEntryReader(keyStore, KEY_STORE_PASSWORD.toCharArray());
    }


    @Bean
    Signature signingSignature(KeyStoreEntryReader keyStoreEntryReader) throws GeneralSecurityException {
        var signature = Signature.getInstance(SIGNING_ALGORITHM);
        signature.initSign(keyStoreEntryReader.getPrivateKey(ALIAS));
        return signature;
    }
}
