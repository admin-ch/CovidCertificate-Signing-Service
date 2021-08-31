package ch.admin.bag.covidcertificate.signature.web.controller;

import ch.admin.bag.covidcertificate.signature.api.CMSSigningResponseDto;
import ch.admin.bag.covidcertificate.signature.service.cms.CMSSigningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;


@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/v1/cms")
final class CMSController {
    private final CMSSigningService cmsSigningService;

    @GetMapping(value = "/{alias}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CMSSigningResponseDto sign(@PathVariable String alias) {
        var aliasDecoded = UriUtils.decode(alias, "UTF-8");
        log.info("Signing certificate with alias {}", aliasDecoded);
        return cmsSigningService.sign(aliasDecoded);
    }
}


