package ch.admin.bag.covidcertificate.signature.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SigningRequestDto {
    private final String dataToSign;
    private final String signingKeyAlias;
    private final String certificateAlias;
}
