package ch.admin.bag.covidcertificate.signature.service;

import ch.admin.bag.covidcertificate.signature.config.ProfileRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!"+ProfileRegistry.PROFILE_HSM_MOCK)
public class LunaSAKeyStoreProvider implements KeyStoreProvider {
    private static final String LUNA_KEYSTORE = "Luna";
    private final LunaSlotManagerWrapper lunaSlotManager;

    @Override
    public KeyStore loadKeyStore() {
        try {
            log.info("Loading Keystore");
            loginIfNeeded();
            var keyStore = KeyStore.getInstance(LUNA_KEYSTORE);
            keyStore.load(null, null);

            return keyStore;

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalStateException("Failed to initialise the key store", e);
        }
    }

    private void loginIfNeeded(){
        if(lunaSlotManager.shouldRetryLogin()){
            lunaSlotManager.reconnectHsmServer();

        }
    }
}
