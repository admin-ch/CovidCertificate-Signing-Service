package ch.admin.bag.covidcertificate.signature.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.HexEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KidService {
    @Resource(name = "keyStoreEntryReaderMap")
    private final Map<KeyStoreSlot, KeyStoreEntryReader> keyStoreEntryReaderMap;

    public String getKid(String certificateAlias, KeyStoreSlot slot) throws CertificateException, IOException, NoSuchAlgorithmException {
        var certificate = keyStoreEntryReaderMap.get(slot).getCertificate(certificateAlias);
        var outputStream = new ByteArrayOutputStream();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(certificate.getEncoded());
        new HexEncoder().encode(encodedHash, 0, 8, outputStream);
        return outputStream.toString();
    }
}
