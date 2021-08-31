package ch.admin.bag.covidcertificate.signature.web.controller;

import ch.admin.bag.covidcertificate.signature.api.CMSSigningResponseDto;
import ch.admin.bag.covidcertificate.signature.service.cms.CMSSigningService;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CMSControllerTest {
    @InjectMocks
    private CMSController cmsController;
    @Mock
    private  CMSSigningService cmsSigningService;

    private final JFixture fixture = new JFixture();

    @Test
    void shouldCallServiveToSignCertificateWithCorrectAlias(){
        var alias = fixture.create(String.class);
        cmsController.sign(alias);
        verify(cmsSigningService).sign(alias);
    }

    @Test
    void shouldReturnSignedCertificate(){
        var responseDto = fixture.create(CMSSigningResponseDto.class);
        when(cmsSigningService.sign(any())).thenReturn(responseDto);
        var actual = cmsController.sign(fixture.create(String.class));
        assertEquals(responseDto, actual);
    }
}