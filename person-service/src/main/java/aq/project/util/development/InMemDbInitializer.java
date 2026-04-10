package aq.project.util.development;

import aq.project.entities.*;
import aq.project.repositories.CountryRepository;
import aq.project.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class InMemDbInitializer implements ApplicationRunner {

    private final PersonRepository personRepository;
    private final CountryRepository countryRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Country country = getTestCountry();
        countryRepository.save(country);
        personRepository.save(getTestPerson(country));
    }

    private Country getTestCountry() {
        Country country = new Country();
        country.setCode("RU");
        country.setName("Russia");
        country.setInstantEmbeddedData(new InstantEmbeddedData());
        return country;
    }

    private Person getTestPerson(Country country) {
        Address address = getValidAddresses(country);

        Individual individual = getValidIndividual();

        Person alice = new Person();
        alice.setKeycloakId("c0391ed2-80b5-400c-8fd2-4d374acad407"); // Copied from Keycloak Admin-CLI (User Alice)
        alice.setAddress(address);
        alice.setIndividual(individual);
        alice.setFirstName("Alice"); // Copied from Keycloak Admin-CLI (User Alice)
        alice.setLastName("K"); // Copied from Keycloak Admin-CLI (User Alice)
        alice.setActive(true);
        alice.setInstantEmbeddedData(new InstantEmbeddedData());
        return alice;
    }

    private Address getValidAddresses(Country country) {
        Address address = new Address();
        address.setCountry(country);
        address.setState("State");
        address.setCity("City");
        address.setAddress("Address");
        address.setZip("Zip");
        address.setInstantEmbeddedData(new InstantEmbeddedData());
        return address;
    }

    private Individual getValidIndividual() {
        Individual individual = new Individual();
        individual.setEmail("alice@post.aq"); // Copied from Keycloak Admin-CLI (User Alice)
        individual.setPhoneNumber("1234567890");
        individual.setPassportNumber("1234567890");
        individual.setInstantEmbeddedData(new InstantEmbeddedData());
        return individual;
    }
}
