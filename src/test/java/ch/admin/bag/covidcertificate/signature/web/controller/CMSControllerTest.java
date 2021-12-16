package ch.admin.bag.covidcertificate.signature.web.controller;

import ch.admin.bag.covidcertificate.signature.api.CMSSigningRequestDto;
import ch.admin.bag.covidcertificate.signature.api.CMSSigningResponseDto;
import ch.admin.bag.covidcertificate.signature.service.cms.CMSSigningService;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CMSControllerTest {
    @InjectMocks
    private CMSController cmsController;
    @Mock
    private  CMSSigningService cmsSigningService;

    private final JFixture fixture = new JFixture();

    @Nested
    class Sign{
        @Test
        void shouldCallServiceToSignCertificateWithCorrectAlias(){
            var alias = fixture.create(String.class);
            cmsController.sign(alias);
            verify(cmsSigningService).sign(alias);
        }

        @Test
        void shouldReturnSignedCertificate(){
            var responseDto = fixture.create(CMSSigningResponseDto.class);
            when(cmsSigningService.sign(anyString())).thenReturn(responseDto);
            var actual = cmsController.sign(fixture.create(String.class));
            assertEquals(responseDto, actual);
        }
    }

    @Nested
    class SignBytes{
        @Test
        void shouldCallServiceToSignBytes(){
            var byteArrayData = fixture.create(byte[].class);
            var requestDto = new CMSSigningRequestDto(Base64.getEncoder().encodeToString(byteArrayData));
            cmsController.signBytes(requestDto);
            verify(cmsSigningService).sign(byteArrayData);
        }

        @Test
        void shouldReturnSignature(){
            var byteArrayData = fixture.create(byte[].class);
            var requestDto = new CMSSigningRequestDto(Base64.getEncoder().encodeToString(byteArrayData));
            var responseDto = fixture.create(CMSSigningResponseDto.class);
            when(cmsSigningService.sign(any(byte[].class))).thenReturn(responseDto);
            var actual = cmsController.signBytes(requestDto);
            assertEquals(responseDto, actual);
        }
    }

}