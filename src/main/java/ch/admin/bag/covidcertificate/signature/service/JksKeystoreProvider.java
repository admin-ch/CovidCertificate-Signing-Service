package ch.admin.bag.covidcertificate.signature.service;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Created by Matthias Schwarz on 3/13/18.
 */
public class JksKeystoreProvider implements KeyStoreProvider {
    private static final String KEYSTORE_TYPE = "JKS";

    private final String keystoreResource;
    private final String keystorePassword;

    public JksKeystoreProvider(String keystoreResource, String keystorePassword) {
        this.keystoreResource = keystoreResource;
        this.keystorePassword = keystorePassword;
    }

    public KeyStore loadKeyStore() {
        try (var keystoreIs = new ClassPathResource(keystoreResource).getInputStream()){
            var keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(keystoreIs, keystorePassword.toCharArray());

            return keyStore;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException("Could not open the " + keystoreResource + " keystore: " + e.getMessage(), e);
        }
    }

}
