package ch.admin.bag.covidcertificate.signature.api;

import ch.admin.bag.covidcertificate.signature.service.KeyStoreSlot;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VerifyRequestDto {
    private String dataToSign;
    private String signature;
    private String certificateAlias;
    private KeyStoreSlot keyStoreSlot;
}
