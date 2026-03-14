package aq.project.util.development;

import aq.project.entities.Country;
import aq.project.entities.InstantEmbeddedData;
import aq.project.repositories.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class InMemDbInitializer implements ApplicationRunner {

    private final CountryRepository countryRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Country country = new Country();
        country.setCode("RU");
        country.setName("Russia");
        country.setInstantEmbeddedData(new InstantEmbeddedData());
        countryRepository.save(country);
    }
}
