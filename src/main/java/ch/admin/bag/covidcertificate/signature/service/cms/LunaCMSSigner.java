package ch.admin.bag.covidcertificate.signature.service.cms;

import ch.admin.bag.covidcertificate.signature.config.ProfileRegistry;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreEntryReader;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import com.safenetinc.luna.provider.LunaProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!"+ ProfileRegistry.PROFILE_HSM_MOCK)
public class LunaCMSSigner implements CMSSigner{

	@Resource(name="keyStoreEntryReaderMap")
	private final Map<KeyStoreSlot, KeyStoreEntryReader> keyStoreEntryReaderMap;

	@Value("${crs.decryption.euCertificateKeyStoreSlot}")
	private KeyStoreSlot euCertificateSlot;

	@Value("${app.signing-service.keystore.private-key-alias}")
	private String privateKeyAlias;

	@Value("${app.signing-service.keystore.signing-certificate-alias}")
	private String signingCertificateAlias;

	private PrivateKey privateKey;
	private X509Certificate signingCertificate;

	@PostConstruct
	private void init(){
		privateKey = keyStoreEntryReaderMap.get(euCertificateSlot).getPrivateKey(privateKeyAlias);
		signingCertificate = keyStoreEntryReaderMap.get(euCertificateSlot).getCertificate(signingCertificateAlias);
	}

	public String sign(String payloadCertificateAlias, KeyStoreSlot slot) throws CertificateEncodingException, IOException {
		var payloadCertificate= keyStoreEntryReaderMap.get(slot).getCertificate(payloadCertificateAlias);

		return new LunaCMSSignatureBuilder(LunaProvider.getInstance().getName())
				.withSigningCertificate(new X509CertificateHolder(signingCertificate.getEncoded()), privateKey)
				.withPayloadCertificate(new X509CertificateHolder(payloadCertificate.getEncoded())).buildAsString();
	}
	
	public String sign(byte[] data) throws CertificateEncodingException, IOException {
		return new LunaCMSSignatureBuilder(LunaProvider.getInstance().getName())
				.withSigningCertificate(new X509CertificateHolder(signingCertificate.getEncoded()), privateKey)
				.withPayloadBytes(data).buildAsString();
	}
}
