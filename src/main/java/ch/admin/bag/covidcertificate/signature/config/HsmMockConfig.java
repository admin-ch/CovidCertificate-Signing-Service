package ch.admin.bag.covidcertificate.signature.config;

import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import ch.admin.bag.covidcertificate.signature.service.JksKeystoreProvider;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
@Profile(ProfileRegistry.PROFILE_HSM_MOCK)
@Slf4j
public class HsmMockConfig {
    private static final String SIGNING_ALGORITHM = "SHA512withRSA";

    private static final String KEYSTORE_RESOURCE_SLOT_0 = "keystore_slot_0.jks";
    private static final String KEYSTORE_RESOURCE_SLOT_1 = "keystore_slot_1.jks";

    private static final String KEY_STORE_PASSWORD_SLOT_0 = "secret";
    private static final String KEY_STORE_PASSWORD_SLOT_1 = "secret";

    @Bean
    public Map<KeyStoreSlot, KeyStoreEntryReader> keyStoreEntryReaderMap() {
        Map<KeyStoreSlot, KeyStoreEntryReader> map = new HashMap<>();
        map.put(KeyStoreSlot.SLOT_NUMBER_0, keyStoreEntryReaderSlot0());
        map.put(KeyStoreSlot.SLOT_NUMBER_1, keyStoreEntryReaderSlot1());
        return map;
    }

    private KeyStoreEntryReader keyStoreEntryReaderSlot0() {
        KeyStoreSlot slot = KeyStoreSlot.SLOT_NUMBER_0;
        log.info("--------------> Login MOCK HSM slot {}", slot);
        var keystoreProvider = new JksKeystoreProvider(KEYSTORE_RESOURCE_SLOT_0, KEY_STORE_PASSWORD_SLOT_0);
        var keyStore = keystoreProvider.loadKeyStore();
        return new KeyStoreEntryReader(keystoreProvider, keyStore, slot, KEY_STORE_PASSWORD_SLOT_0.toCharArray());
    }

    private KeyStoreEntryReader keyStoreEntryReaderSlot1() {
        KeyStoreSlot slot = KeyStoreSlot.SLOT_NUMBER_1;
        log.info("--------------> Login MOCK HSM slot {}", slot);
        var keystoreProvider = new JksKeystoreProvider(KEYSTORE_RESOURCE_SLOT_1, KEY_STORE_PASSWORD_SLOT_1);
        var keyStore = keystoreProvider.loadKeyStore();
        return new KeyStoreEntryReader(keystoreProvider, keyStore, slot, KEY_STORE_PASSWORD_SLOT_1.toCharArray());
    }

    @Bean
    Supplier<Signature> signatureSupplier() {
        return this::getSignatureInstance;
    }

    private Signature getSignatureInstance() {
        log.debug("Get Signature Instance with {}", SIGNING_ALGORITHM);
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
