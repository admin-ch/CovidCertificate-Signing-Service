package ch.admin.bag.covidcertificate.signature.service;

import lombok.extern.slf4j.Slf4j;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Slf4j
public class KeyStoreEntryReader {
    private static final String MOCK_AND_TEST_PASS = "secret";

    private final KeyStore keyStore;
    private final char[] keyPassword;

    public KeyStoreEntryReader(KeyStore keyStore, char[] keyPassword) {
        this.keyStore = keyStore;
        this.keyPassword = keyPassword;
    }

    public KeyStoreEntryReader(KeyStore keyStore) {
        this.keyStore = keyStore;
        this.keyPassword = MOCK_AND_TEST_PASS.toCharArray();
    }

    public PrivateKey getPrivateKey(String alias) {

        log.debug("GET PRIVATE KEY {}", alias);

        try {
            var privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword);
            if (privateKey != null) {
                return privateKey;
            }
        } catch (Exception e) {
            throw new SecurityException("Failed to retrieve the key for alias " + alias + " from the keystore: ", e);
        }
        throw new SecurityException("The KeyStore has no privateKey for alias: " + alias);
    }

    public X509Certificate getCertificate(String alias) {
        try {
            var certificate = keyStore.getCertificate(alias);
            if (certificate != null) {
                return (X509Certificate) certificate;
            }
        } catch (Exception e) {
            throw new SecurityException("Failed to retrieve the certificate from the keyStore for alias: " + alias, e);
        }
        throw new SecurityException("The KeyStore has no certificate for alias: " + alias);
    }

}
