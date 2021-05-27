package ch.admin.bag.covidcertificate.signature.service;

import ch.admin.bag.covidcertificate.signature.SigningApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SigningApplication.class);
	}

}
