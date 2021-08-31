package ch.admin.bag.covidcertificate.signature.service.cms;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;


abstract class CMSSigner {
	public abstract String sign(String payloadCertificateAlias) throws CertificateEncodingException, IOException;
}
