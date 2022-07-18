package ch.admin.bag.covidcertificate.signature;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;


@SpringBootApplication
@Slf4j
public class SigningApplication {

    public static void main(String[] args) {
        Environment env = SpringApplication.run(SigningApplication.class, args).getEnvironment();

        log.info("\n----------------------------------------------------------\n\t" +
                        "cc-signing-service is running! \n\t" +
                        "\n\t" +
                        "Profile(s): \t{}" +
                        "\n----------------------------------------------------------",
                (Object) env.getActiveProfiles()
        );
    }
}
