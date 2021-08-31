package ch.admin.bag.covidcertificate.signature.service.cms;

import ch.admin.bag.covidcertificate.signature.api.CMSSigningResponseDto;
import ch.admin.bag.covidcertificate.signature.config.error.SignatureCreationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CMSSigningService {
	private final CMSSigner cmsSigner;

	public CMSSigningResponseDto sign(String alias) {
		String signature;
		try {
			signature = cmsSigner.sign(alias);
		} catch (CertificateEncodingException | IOException e) {
			throw new SignatureCreationException("Failed to sing certificate with alias " + alias, e);
		}

		return new CMSSigningResponseDto(
				signature
		);

	}

}
