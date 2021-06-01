package ch.admin.bag.covidcertificate.signature.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static ch.admin.bag.covidcertificate.signature.config.ProfileRegistry.PROFILE_TLS;

@EnableWebSecurity
@Profile(PROFILE_TLS)
public class SSLSecurityConfigurationLocal extends WebSecurityConfigurerAdapter {

    @Value("${app.signing-service.allowed-user}")
    private String allowedUser;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/sign/**").authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .x509()
                .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                .userDetailsService(userDetailsService());

        // Turn off CSRF for this REST service.
        http.antMatcher("/sign/**").csrf().disable();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return (username -> {
            if (username.equals(allowedUser)) {
                return new User(username, "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
            } else {
                return null;
            }
        });
    }
}