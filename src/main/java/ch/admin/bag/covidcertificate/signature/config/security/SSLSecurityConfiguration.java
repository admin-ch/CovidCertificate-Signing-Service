package ch.admin.bag.covidcertificate.signature.config.security;

import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static ch.admin.bag.covidcertificate.signature.config.ProfileRegistry.PROFILE_TLS;

@EnableWebSecurity
@Profile("!"+ PROFILE_TLS)
public class SSLSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest()
                .permitAll();

        // Turn off CSRF for this REST service.
        http.antMatcher("/sign/**")
                .antMatcher("/v1/cms/**")
                .csrf()
                .disable();
    }
}