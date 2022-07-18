package ch.admin.bag.covidcertificate.signature.service.cms;

import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;


interface CMSSigner {
	String sign(String payloadCertificateAlias, KeyStoreSlot slot) throws CertificateEncodingException, IOException;
	
	String sign(byte[] payloadBytes) throws CertificateEncodingException, IOException;
}
