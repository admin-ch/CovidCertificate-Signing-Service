package ch.admin.bag.covidcertificate.signature;

import ch.admin.bag.covidcertificate.signature.api.SigningRequestDto;
import ch.admin.bag.covidcertificate.signature.api.VerifyRequestDto;
import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import ch.admin.bag.covidcertificate.signature.service.SigningService;
import com.flextrade.jfixture.JFixture;

import java.security.SignatureException;
import java.util.Base64;

public class FixtureCustomization {
    public static void customizeSigningRequestDto(JFixture fixture) {
        fixture.customise().lazyInstance(SigningRequestDto.class, () -> new SigningRequestDto(
                Base64.getEncoder().encodeToString(fixture.create(byte[].class)),
                "mock",
                KeyStoreSlot.SLOT_NUMBER_0
        ));
    }

    public static void customizeVerifyRequestDto(JFixture fixture, SigningService signingService) {
        KeyStoreSlot keyStoreSlot = KeyStoreSlot.SLOT_NUMBER_0;
        fixture.customise().lazyInstance(VerifyRequestDto.class, () -> {
            var dataToSign = Base64.getEncoder().encodeToString(fixture.create(byte[].class));
            byte[] signature = new byte[0];
            try {
                signature = signingService.sign("mock", keyStoreSlot, dataToSign);
            } catch (SignatureException e) {
                e.printStackTrace();
            }
            return new VerifyRequestDto(
                    dataToSign,
                    Base64.getEncoder().encodeToString(signature),
                    "mock",
                    keyStoreSlot
            );
        });
    }
}
