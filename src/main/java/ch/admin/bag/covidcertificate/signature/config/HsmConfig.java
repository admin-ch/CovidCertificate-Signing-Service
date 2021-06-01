package ch.admin.bag.covidcertificate.signature.config;

import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import ch.admin.bag.covidcertificate.signature.service.LunaSAKeyStoreProvider;
import com.safenetinc.luna.LunaSlotManager;
import com.safenetinc.luna.provider.LunaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import java.security.*;
import java.util.function.Supplier;

@Configuration
@Profile("!" + ProfileRegistry.PROFILE_HSM_MOCK)
@Slf4j
public class HsmConfig {
    private static final String SIGNING_ALGORITHM = "SHA256/RSA/PSS";

    private static final String LUNA_PROVIDER;

    @Value("${crs.decryption.keyStoreSlotNumber}")
    private Integer keyStoreSlotNumber;

    @Value("${crs.decryption.keyStorePassword}")
    private String keyStorePassword;

    @Value("${crs.decryption.aliasSign}")
    private String aliasSign;

    @Value("${crs.decryption.aliasVerify}")
    private String aliasVerify;

    static {
        try {
            var lunaProvider = new LunaProvider();
            Security.addProvider(lunaProvider);
            LUNA_PROVIDER = lunaProvider.getName();
        }catch (Exception | ExceptionInInitializerError e){
            throw new IllegalStateException("Failed to register the Luna Provider", e);
        }
    }

    @Bean(destroyMethod = "logout")
    LunaSlotManager slotManager() {
        log.debug("slotManager Login with {}", keyStoreSlotNumber);
        var slotManager = LunaSlotManager.getInstance();
        slotManager.login(keyStoreSlotNumber, keyStorePassword);
        return slotManager;
    }

    @Bean
    @DependsOn("slotManager")
    KeyStoreEntryReader keyStoreEntryReader() {
        var keyStore = new LunaSAKeyStoreProvider().loadKeyStore();
        return new KeyStoreEntryReader(keyStore, keyStorePassword.toCharArray());
    }

    @Bean
    Supplier<Signature> signingSignatureSupplier(KeyStoreEntryReader keyStoreEntryReader) {
        return () -> initSignature(keyStoreEntryReader);
    }

    private Signature initSignature(KeyStoreEntryReader keyStoreEntryReader)  {
        log.debug("signingSignature with {} and {}", SIGNING_ALGORITHM, LUNA_PROVIDER);
        Signature signature;

        try {
            signature = Signature.getInstance(SIGNING_ALGORITHM, LUNA_PROVIDER);
            signature.initSign(keyStoreEntryReader.getPrivateKey(aliasSign));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new SignatureCreationException(
                    String.format("Failed to initialize signature with algorithm %s and key for alias %s", SIGNING_ALGORITHM, aliasSign),
                    e);
        }

        return signature;
    }
}
