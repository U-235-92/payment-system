package aq.project.util.entity;

import aq.project.entities.*;

import java.util.UUID;

import static aq.project.util.entity.Constants.*;

public abstract class Persons {

    public static Person getAlicePerson(Country country) {
        Address address = new Address();
        address.setCountry(country);
        address.setState(CORRECT_STATE);
        address.setCity(CORRECT_CITY);
        address.setAddress(CORRECT_ADDRESS);
        address.setZip(CORRECT_ZIP);
        address.setInstantEmbeddedData(new InstantEmbeddedData());

        Individual individual = new Individual();
        individual.setEmail(CORRECT_EMAIL);
        individual.setPhoneNumber(CORRECT_PHONE);
        individual.setPassportNumber(CORRECT_PASSPORT);
        individual.setInstantEmbeddedData(new InstantEmbeddedData());

        Person alice = new Person();
        alice.setKeycloakId(ALICE_KEYCLOAK_ID);
        alice.setAddress(address);
        alice.setIndividual(individual);
        alice.setFirstName(CORRECT_FIRST_NAME);
        alice.setLastName(CORRECT_LAST_NAME);
        alice.setActive(true);
        alice.setInstantEmbeddedData(new InstantEmbeddedData());
        return alice;
    }

    public static Person getBobPerson(Country country) {
        Address address = new Address();
        address.setCountry(country);
        address.setState(CORRECT_STATE);
        address.setCity(CORRECT_CITY);
        address.setAddress(CORRECT_ADDRESS);
        address.setZip(CORRECT_ZIP);
        address.setInstantEmbeddedData(new InstantEmbeddedData());

        Individual individual = new Individual();
        individual.setEmail("bob@post.aq");
        individual.setPhoneNumber(CORRECT_PHONE);
        individual.setPassportNumber(CORRECT_PASSPORT);
        individual.setInstantEmbeddedData(new InstantEmbeddedData());

        Person bob = new Person();
        bob.setKeycloakId(BOB_PERSON_ID);
        bob.setAddress(address);
        bob.setIndividual(individual);
        bob.setFirstName("Bob");
        bob.setLastName(CORRECT_LAST_NAME);
        bob.setActive(true);
        bob.setInstantEmbeddedData(new InstantEmbeddedData());
        return bob;
    }

    public static Person getWrongPerson() {
        Address address = new Address();
        address.setState("WRONG");
        address.setCity(null);
        address.setZip("WRONG_ZIP");
        address.setInstantEmbeddedData(new InstantEmbeddedData());

        Individual individual = new Individual();
        individual.setEmail("wrong@post.aq");
        individual.setPhoneNumber("wrong");
        individual.setPassportNumber("wrong");
        individual.setInstantEmbeddedData(new InstantEmbeddedData());

        Person wrong = new Person();
        wrong.setKeycloakId(CORRECT_PERSON_ID);
        wrong.setId(UUID.fromString(CORRECT_PERSON_ID));
        wrong.setAddress(address);
        wrong.setIndividual(individual);
        wrong.setFirstName("Wrong");
        wrong.setLastName("");
        wrong.setActive(true);
        wrong.setInstantEmbeddedData(new InstantEmbeddedData());
        return wrong;
    }

    public static Person getUnknownPerson(Country country) {
        Address address = new Address();
        address.setCountry(country);
        address.setState(CORRECT_STATE);
        address.setCity(CORRECT_CITY);
        address.setAddress(CORRECT_ADDRESS);
        address.setZip(CORRECT_ZIP);
        address.setInstantEmbeddedData(new InstantEmbeddedData());

        Individual individual = new Individual();
        individual.setEmail("unknown@post.aq");
        individual.setPhoneNumber(CORRECT_PHONE);
        individual.setPassportNumber(CORRECT_PASSPORT);
        individual.setInstantEmbeddedData(new InstantEmbeddedData());

        Person bob = new Person();
        bob.setId(UUID.fromString(UNKNOWN_PERSON_ID));
        bob.setKeycloakId(UNKNOWN_PERSON_ID);
        bob.setAddress(address);
        bob.setIndividual(individual);
        bob.setFirstName("Unknown");
        bob.setLastName(CORRECT_LAST_NAME);
        bob.setActive(true);
        bob.setInstantEmbeddedData(new InstantEmbeddedData());
        return bob;
    }

    public static Person getPersonWithIncorrectId(Country country) {
        Address address = new Address();
        address.setCountry(country);
        address.setState(CORRECT_STATE);
        address.setCity(CORRECT_CITY);
        address.setAddress(CORRECT_ADDRESS);
        address.setZip(CORRECT_ZIP);
        address.setInstantEmbeddedData(new InstantEmbeddedData());

        Individual individual = new Individual();
        individual.setEmail(CORRECT_EMAIL);
        individual.setPhoneNumber(CORRECT_PHONE);
        individual.setPassportNumber(CORRECT_PASSPORT);
        individual.setInstantEmbeddedData(new InstantEmbeddedData());

        Person alice = new Person();
        alice.setKeycloakId(INCORRECT_PERSON_ID);
        alice.setId(UUID.fromString(INCORRECT_PERSON_ID));
        alice.setAddress(address);
        alice.setIndividual(individual);
        alice.setFirstName(CORRECT_FIRST_NAME);
        alice.setLastName(CORRECT_LAST_NAME);
        alice.setActive(true);
        alice.setInstantEmbeddedData(new InstantEmbeddedData());
        return alice;
    }
}
