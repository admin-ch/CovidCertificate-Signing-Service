package ch.admin.bag.covidcertificate.signature.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.HexEncoder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KidService {
    private final KeyStoreEntryReader keyStoreEntryReader;

    public String getKid(String certificateAlias) throws CertificateException, IOException, NoSuchAlgorithmException {
        var certificate = keyStoreEntryReader.getCertificate(certificateAlias);
        var outputStream = new ByteArrayOutputStream();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(certificate.getEncoded());
        new HexEncoder().encode(encodedHash, 0, 8, outputStream);
        return outputStream.toString();
    }
}
