package ch.admin.bag.covidcertificate.signature.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CMSSigningRequestDto {
    private String data;
}
