package ch.admin.bag.covidcertificate.signature;

import ch.admin.bag.covidcertificate.signature.config.ProfileRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({ProfileRegistry.PROFILE_HSM_MOCK, ProfileRegistry.PROFILE_LOCAL})
class SigningApplicationTests {

	@Test
	@SuppressWarnings("java:S2699")
	void contextLoads() {
	}

}
