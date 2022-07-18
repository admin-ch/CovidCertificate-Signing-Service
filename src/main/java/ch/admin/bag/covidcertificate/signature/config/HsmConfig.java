package ch.admin.bag.covidcertificate.signature.config;

import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import ch.admin.bag.covidcertificate.signature.service.LunaKeyStoreProvider;
import com.safenetinc.luna.provider.LunaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
@Profile("!" + ProfileRegistry.PROFILE_HSM_MOCK)
@Slf4j
public class HsmConfig {
    private static final String SIGNING_ALGORITHM = "SHA256/RSA/PSS";

    public static final String LUNA_PROVIDER;

    @Value("${crs.decryption.keyStorePasswordSlot0}")
    private String keyStorePasswordSlot0;

    @Value("${crs.decryption.keyStorePasswordSlot1}")
    private String keyStorePasswordSlot1;

    static {
        try {
            var lunaProvider = new LunaProvider();
            Security.addProvider(lunaProvider);
            LUNA_PROVIDER = lunaProvider.getName();
        } catch (Exception | ExceptionInInitializerError e) {
            throw new IllegalStateException("Failed to register the Luna Provider", e);
        }
    }

    @Bean
    public Map<KeyStoreSlot, KeyStoreEntryReader> keyStoreEntryReaderMap() {
        Map<KeyStoreSlot, KeyStoreEntryReader> map = new HashMap<>();
        map.put(KeyStoreSlot.SLOT_NUMBER_0, keyStoreEntryReaderSlot0());
        map.put(KeyStoreSlot.SLOT_NUMBER_1, keyStoreEntryReaderSlot1());
        return map;
    }

    private KeyStoreEntryReader keyStoreEntryReaderSlot0() {
        KeyStoreSlot slot = KeyStoreSlot.SLOT_NUMBER_0;
        LunaKeyStoreProvider lunaKeyStoreProvider = new LunaKeyStoreProvider(slot, keyStorePasswordSlot0);
        var keyStore = lunaKeyStoreProvider.loadKeyStore();
        return new KeyStoreEntryReader(lunaKeyStoreProvider, keyStore, slot, keyStorePasswordSlot0.toCharArray());
    }

    private KeyStoreEntryReader keyStoreEntryReaderSlot1() {
        KeyStoreSlot slot = KeyStoreSlot.SLOT_NUMBER_1;
        LunaKeyStoreProvider lunaKeyStoreProvider = new LunaKeyStoreProvider(slot, keyStorePasswordSlot1);
        var keyStore = lunaKeyStoreProvider.loadKeyStore();
        return new KeyStoreEntryReader(lunaKeyStoreProvider, keyStore, slot, keyStorePasswordSlot1.toCharArray());
    }

    @Bean
    Supplier<Signature> signatureSupplier() {
        return this::getSignatureInstance;
    }

    private Signature getSignatureInstance() {
        log.debug("Get Signature Instance with {} and {}", SIGNING_ALGORITHM, LUNA_PROVIDER);
        Signature signature;

        try {
            signature = Signature.getInstance(SIGNING_ALGORITHM, LUNA_PROVIDER);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new SignatureCreationException(
                    String.format("Failed to get signature instance for algorithm %s and provider %s", SIGNING_ALGORITHM, LUNA_PROVIDER),
                    e);
        }

        return signature;
    }
}
