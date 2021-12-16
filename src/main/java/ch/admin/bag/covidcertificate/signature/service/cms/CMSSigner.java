package ch.admin.bag.covidcertificate.signature.service.cms;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;


interface CMSSigner {
	String sign(String payloadCertificateAlias) throws CertificateEncodingException, IOException;
	
	String sign(byte[] payloadBytes) throws CertificateEncodingException, IOException;
}
