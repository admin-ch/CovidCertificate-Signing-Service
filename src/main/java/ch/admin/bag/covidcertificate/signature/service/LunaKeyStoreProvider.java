package ch.admin.bag.covidcertificate.signature.service;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

@Slf4j
public class LunaKeyStoreProvider implements KeyStoreProvider {
    private static final String LUNA_KEYSTORE = "Luna";

    private final Integer keyStoreSlotNumber;
    private final String keyStorePassword;

    public LunaKeyStoreProvider(KeyStoreSlot slot, String keystorePassword) {
        this.keyStoreSlotNumber = slot.getSlotNumber();
        this.keyStorePassword = keystorePassword;
    }

    @Override
    public KeyStore loadKeyStore() {
        try {
            log.info("Loading KeyStore for slot {}", keyStoreSlotNumber);

            var keyStore = KeyStore.getInstance(LUNA_KEYSTORE);
            ByteArrayInputStream slotStream = new ByteArrayInputStream(("slot:" + keyStoreSlotNumber).getBytes());
            keyStore.load(slotStream, keyStorePassword.toCharArray());

            return keyStore;
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to initialise the KeyStore for slot %s", keyStoreSlotNumber), e);
        }
    }
}
