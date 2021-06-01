package ch.admin.bag.covidcertificate.signature.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * use this class to document your spring profile or document it in confluence. but... document it!
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileRegistry {
    /**
     * if activated, use a pkcs12 based mock otherwise use hsm-based security
     */
    public static final String PROFILE_HSM_MOCK = "hsm-mock";
    public static final String PROFILE_LOCAL = "local";
    public static final String PROFILE_TLS = "tls";
}

