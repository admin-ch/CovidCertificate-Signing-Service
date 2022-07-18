package ch.admin.bag.covidcertificate.signature.service;

import ch.admin.bag.covidcertificate.signature.config.error.CertificateNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Slf4j
public class KeyStoreEntryReader {

    /** An email alert is triggered when this message appears in the logs. */
    private static final String WARNING_KEYSTORE_LOST_WITH_MONITORING = "Connection to Keystore was lost. Reloading keystore.";

    private final KeyStoreProvider keyStoreProvider;
    private KeyStore keyStore;
    private final KeyStoreSlot slot;
    private final char[] keyPassword;

    public KeyStoreEntryReader(KeyStoreProvider keyStoreProvider, KeyStore keyStore, KeyStoreSlot slot, char[] keyPassword) {
        this.keyStoreProvider = keyStoreProvider;
        this.keyStore = keyStore;
        this.slot = slot;
        this.keyPassword = keyPassword;
    }

    public PrivateKey getPrivateKey(String alias) {
        log.debug("GET PRIVATE KEY {} from slot {}", alias, slot.getSlotNumber());
        rebuildKeystoreIfNeeded(alias);

        try {
            var privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword);
            if (privateKey != null) {
                return privateKey;
            }
        } catch (Exception e) {
            throw new SecurityException(String.format("KeyStore slot %s: Failed to retrieve the privateKey for alias %s", slot.getSlotNumber(), alias), e);
        }
        throw new SecurityException(String.format("KeyStore slot %s: Has no privateKey for alias %s", slot.getSlotNumber(), alias));
    }

    public X509Certificate getCertificate(String alias) {
        log.debug("GET Certificate {} from slot {}", alias, slot.getSlotNumber());
        rebuildKeystoreIfNeeded(alias);

        try {

            var certificate = keyStore.getCertificate(alias);
            if (certificate != null) {
                return (X509Certificate) certificate;
            }
        } catch (Exception e) {
            throw new CertificateNotFoundException(String.format("KeyStore slot %s: Failed to retrieve the certificate for alias %s", slot.getSlotNumber(), alias), e);
        }
        throw new CertificateNotFoundException(String.format("KeyStore slot %s: Has no certificate for alias %s", slot.getSlotNumber(), alias));
    }

    private void rebuildKeystoreIfNeeded(String alias) {
        try {
            if (!keyStore.containsAlias(alias)) {
                log.info("Alias {} not found. Reloading keystore for slot {}.", alias, slot);
                keyStore = keyStoreProvider.loadKeyStore();
            }
        } catch (KeyStoreException | RuntimeException e) {
            log.warn("Could not reload keystore for slot {}, trying again", slot, e);
            log.warn(WARNING_KEYSTORE_LOST_WITH_MONITORING);
            keyStore = keyStoreProvider.loadKeyStore();
        }
    }
}
