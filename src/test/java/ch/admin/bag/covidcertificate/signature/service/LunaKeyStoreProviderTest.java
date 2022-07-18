package ch.admin.bag.covidcertificate.signature.service;

import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class LunaKeyStoreProviderTest {
    private final KeyStoreSlot slot = fixture.create(KeyStoreSlot.class);
    private final String password = fixture.create(String.class);

    private final LunaKeyStoreProvider lunaKeyStoreProvider = new LunaKeyStoreProvider(slot, password);

    private static final JFixture fixture = new JFixture();

    @Test
    void loadsKeystoreWithCorrectPassword() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keystoreMock = mock(KeyStore.class);
        try (MockedStatic<KeyStore> keystore = Mockito.mockStatic(KeyStore.class)) {
            keystore.when((MockedStatic.Verification) KeyStore.getInstance(anyString())).thenReturn(keystoreMock);
            lunaKeyStoreProvider.loadKeyStore();
            verify(keystoreMock).load(any(), eq(password.toCharArray()));
        }
    }

    @Test
    void returnsLoadedKeystore() throws KeyStoreException {
        KeyStore keystoreMock = mock(KeyStore.class);
        try (MockedStatic<KeyStore> keystore = Mockito.mockStatic(KeyStore.class)) {
            keystore.when((MockedStatic.Verification) KeyStore.getInstance(anyString())).thenReturn(keystoreMock);
            KeyStore actual = lunaKeyStoreProvider.loadKeyStore();
            assertEquals(keystoreMock, actual);
        }
    }

    @Test
    void throwsIllegalStateException_whenKeyStoreExceptionIsThrown() throws KeyStoreException {
        try (MockedStatic<KeyStore> keystore = Mockito.mockStatic(KeyStore.class)) {
            keystore.when((MockedStatic.Verification) KeyStore.getInstance(anyString())).thenThrow(new KeyStoreException());
            assertThrows(IllegalStateException.class,
                    () -> lunaKeyStoreProvider.loadKeyStore());
        }
    }
}