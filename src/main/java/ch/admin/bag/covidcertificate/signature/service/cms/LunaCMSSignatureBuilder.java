package ch.admin.bag.covidcertificate.signature.service.cms;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultAlgorithmNameFinder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Base64;

public class LunaCMSSignatureBuilder {

    private byte[] payloadCertificate;
    private X509CertificateHolder signingCertificate;
    private PrivateKey signingCertificatePrivateKey;

    private final String lunaProvider;

    public LunaCMSSignatureBuilder(String lunaProvider) {
        this.lunaProvider = lunaProvider;
    }

    /**
     * Set signing certificate and private key.
     * @param certificate signing certificate
     * @param privateKey private key of the signing certificate
     * @return this
     */
    public LunaCMSSignatureBuilder withSigningCertificate(X509CertificateHolder certificate,
                                                          PrivateKey privateKey) {
        this.signingCertificate = certificate;
        this.signingCertificatePrivateKey = privateKey;
        return this;
    }

    /**
     * Set payload certificate.
     * @param certificate payload certificate
     * @return this
     * @throws IOException
     */
    public LunaCMSSignatureBuilder withPayloadCertificate(X509CertificateHolder certificate) throws IOException {
        this.payloadCertificate = certificate.getEncoded();
        return this;
    }

    /**
     * Set payload certificate.
     * 
     * @param data payload bytes
     * @return this
     */
    public LunaCMSSignatureBuilder withPayloadBytes(byte[] data) {
        this.payloadCertificate = data;
        return this;
    }

    /**
     * Build the CMS message.
     * @param detached detached or not
     * @return CMS message
     */
    public byte[] build(boolean detached) {
        if (this.payloadCertificate != null
                && this.signingCertificate != null && this.signingCertificatePrivateKey != null) {
            CMSSignedDataGenerator signedDataGenerator = new CMSSignedDataGenerator();

            try {
                DigestCalculatorProvider digestCalculatorProvider = (new JcaDigestCalculatorProviderBuilder())
                        .setProvider(lunaProvider)
                        .build();
                String signingAlgorithmName = (new DefaultAlgorithmNameFinder())
                        .getAlgorithmName(this.signingCertificate.getSignatureAlgorithm());
                ContentSigner contentSigner = (new JcaContentSignerBuilder(signingAlgorithmName))
                        .setProvider(lunaProvider)
                        .build(this.signingCertificatePrivateKey);
                SignerInfoGenerator signerInfoGenerator = (new JcaSignerInfoGeneratorBuilder(digestCalculatorProvider))
                        .build(contentSigner, this.signingCertificate);
                signedDataGenerator.addSignerInfoGenerator(signerInfoGenerator);
                signedDataGenerator.addCertificate(this.signingCertificate);
                CMSSignedData signedData = signedDataGenerator
                        .generate(new CMSProcessableByteArray(this.payloadCertificate), !detached);
                return signedData.getEncoded();
            } catch (CMSException | IOException | OperatorCreationException var9) {
                throw new RuntimeException("Failed to create signed message");
            }
        } else {
            throw new RuntimeException("Message Builder is not ready");
        }
    }

    public byte[] build() {
        return this.build(false);
    }

    public String buildAsString(boolean detached) {
        return Base64.getEncoder().encodeToString(this.build(detached));
    }

    public String buildAsString() {
        return Base64.getEncoder().encodeToString(this.build(false));
    }

}

