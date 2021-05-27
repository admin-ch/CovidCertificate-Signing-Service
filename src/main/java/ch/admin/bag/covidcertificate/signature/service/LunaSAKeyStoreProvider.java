package ch.admin.bag.covidcertificate.signature.service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class LunaSAKeyStoreProvider implements KeyStoreProvider {
    private static final String LUNA_KEYSTORE = "Luna";

    @Override
    public KeyStore loadKeyStore() {
        try {
            var keyStore = KeyStore.getInstance(LUNA_KEYSTORE);
            keyStore.load(null, null);

            return keyStore;

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalStateException("Failed to initialise the key store", e);
        }
    }
}
