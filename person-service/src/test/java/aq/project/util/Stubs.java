package aq.project.util;

import aq.project.entities.*;

public abstract class Stubs {

    public static final String TEST_COUNTRY_CODE = "US";
    public static final String TEST_COUNTRY_NAME = "USA";

    public static final String TEST_CORRECT_FIRST_NAME = "Alice";
    public static final String TEST_CORRECT_LAST_NAME = "K";
    public static final String TEST_CORRECT_EMAIL = "alice@post.aq";
    public static final String TEST_CORRECT_PHONE = "123456789";
    public static final String TEST_CORRECT_PASSPORT = "123456789";
    public static final String TEST_CORRECT_ADDRESS = "Main Street";
    public static final String TEST_CORRECT_STATE = "CA";
    public static final String TEST_CORRECT_CITY = "San Francisco";
    public static final String TEST_CORRECT_ZIP = "ZIP123";

    public static final String TEST_UNKNOWN_EMAIL = "notExist@post.aq";
    public static final String TEST_UNKNOWN_ID = "31699ff9-ce23-469a-a90f-99495d696f66";

    public static final String TEST_INCORRECT_EMAIL = "incorrect";
    public static final String TEST_INCORRECT_ID = "incorrect";

    public static Person getAlicePerson(Country country) {
        Address address = new Address();
        address.setCountry(country);
        address.setState(TEST_CORRECT_STATE);
        address.setCity(TEST_CORRECT_CITY);
        address.setAddress(TEST_CORRECT_ADDRESS);
        address.setZip(TEST_CORRECT_ZIP);
        address.setInstantEmbeddedData(new InstantEmbeddedData());

        Individual individual = new Individual();
        individual.setEmail(TEST_CORRECT_EMAIL);
        individual.setPhoneNumber(TEST_CORRECT_PHONE);
        individual.setPassportNumber(TEST_CORRECT_PASSPORT);
        individual.setInstantEmbeddedData(new InstantEmbeddedData());

        Person alice = new Person();
        alice.setAddress(address);
        alice.setIndividual(individual);
        alice.setFirstName(TEST_CORRECT_FIRST_NAME);
        alice.setLastName(TEST_CORRECT_LAST_NAME);
        alice.setActive(true);
        alice.setInstantEmbeddedData(new InstantEmbeddedData());
        return alice;
    }

    public static Person getBobPerson(Country country) {
        Address address = new Address();
        address.setCountry(country);
        address.setState(TEST_CORRECT_STATE);
        address.setCity(TEST_CORRECT_CITY);
        address.setAddress(TEST_CORRECT_ADDRESS);
        address.setZip(TEST_CORRECT_ZIP);
        address.setInstantEmbeddedData(new InstantEmbeddedData());

        Individual individual = new Individual();
        individual.setEmail("bob@post.aq");
        individual.setPhoneNumber(TEST_CORRECT_PHONE);
        individual.setPassportNumber(TEST_CORRECT_PASSPORT);
        individual.setInstantEmbeddedData(new InstantEmbeddedData());

        Person bob = new Person();
        bob.setAddress(address);
        bob.setIndividual(individual);
        bob.setFirstName("Bob");
        bob.setLastName(TEST_CORRECT_LAST_NAME);
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
        address.setState(TEST_CORRECT_STATE);
        address.setCity(TEST_CORRECT_CITY);
        address.setAddress(TEST_CORRECT_ADDRESS);
        address.setZip(TEST_CORRECT_ZIP);
        address.setInstantEmbeddedData(new InstantEmbeddedData());

        Individual individual = new Individual();
        individual.setEmail("unknown@post.aq");
        individual.setPhoneNumber(TEST_CORRECT_PHONE);
        individual.setPassportNumber(TEST_CORRECT_PASSPORT);
        individual.setInstantEmbeddedData(new InstantEmbeddedData());

        Person bob = new Person();
        bob.setAddress(address);
        bob.setIndividual(individual);
        bob.setFirstName("Unknown");
        bob.setLastName(TEST_CORRECT_LAST_NAME);
        bob.setActive(true);
        bob.setInstantEmbeddedData(new InstantEmbeddedData());
        return bob;
    }

    public static Country getUnknownCountry() {
        Country country = new Country();
        country.setId(85);
        country.setCode("UKN");
        country.setName("Unknown");
        return country;
    }
}
