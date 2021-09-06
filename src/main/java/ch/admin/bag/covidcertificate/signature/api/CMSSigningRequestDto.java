package ch.admin.bag.covidcertificate.signature.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CMSSigningRequestDto {
    private String data;
}
