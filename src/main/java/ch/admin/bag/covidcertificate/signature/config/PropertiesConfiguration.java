package ch.admin.bag.covidcertificate.signature.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static ch.admin.bag.covidcertificate.signature.config.ProfileRegistry.PROFILE_HSM_MOCK;

@Configuration
@Profile("!"+PROFILE_HSM_MOCK)
public class PropertiesConfiguration {

    @Bean
    public PropertySourcesPlaceholderConfigurer properties() {
        final var propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocations(ConfigUtil.getResource("cc-signing-service-external-config.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

}
