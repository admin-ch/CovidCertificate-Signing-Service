package ch.admin.bag.covidcertificate.signature.service;

import java.security.KeyStore;

/**
 * Interface to key stores (e.g. HSM or P12)
 */
public interface KeyStoreProvider {

    /**
     * Loads keystore ready to be used by other component.
     */
    KeyStore loadKeyStore();
}
