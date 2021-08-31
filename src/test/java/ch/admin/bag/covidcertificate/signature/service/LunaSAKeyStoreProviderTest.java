package ch.admin.bag.covidcertificate.signature.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LunaSAKeyStoreProviderTest {
    @InjectMocks
    private LunaSAKeyStoreProvider lunaSAKeyStoreProvider;

    @Mock
    private LunaSlotManagerWrapper lunaSlotManager;

    @Test
    void loadsKeystore() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keystoreMock = mock(KeyStore.class);
        try (MockedStatic<KeyStore> keystore = Mockito.mockStatic(KeyStore.class)) {
            keystore.when((MockedStatic.Verification) KeyStore.getInstance(anyString())).thenReturn(keystoreMock);
            lunaSAKeyStoreProvider.loadKeyStore();
            verify(keystoreMock).load(null, null);
        }
    }

    @Test
    void returnsLoadedKeystore() throws KeyStoreException {
        KeyStore keystoreMock = mock(KeyStore.class);
        try (MockedStatic<KeyStore> keystore = Mockito.mockStatic(KeyStore.class)) {
            keystore.when((MockedStatic.Verification) KeyStore.getInstance(anyString())).thenReturn(keystoreMock);
            KeyStore actual = lunaSAKeyStoreProvider.loadKeyStore();
            assertEquals(keystoreMock, actual);
        }
    }

    @Test
    void throwsIllegalStateException_whenKeyStoreExceptionIsThrown() throws KeyStoreException {
        try (MockedStatic<KeyStore> keystore = Mockito.mockStatic(KeyStore.class)) {
            keystore.when((MockedStatic.Verification) KeyStore.getInstance(anyString())).thenThrow(new KeyStoreException());
            assertThrows(IllegalStateException.class,
                    () -> lunaSAKeyStoreProvider.loadKeyStore());
        }
    }
}