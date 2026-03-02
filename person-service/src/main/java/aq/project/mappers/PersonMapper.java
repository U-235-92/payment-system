package aq.project.mappers;

import aq.project.entities.Address;
import aq.project.entities.Country;
import aq.project.entities.Person;

import java.time.Instant;

public abstract class PersonMapper {

    private PersonMapper() {}

    public static void map(Person from, Person to) {
        to.setFirstName(from.getFirstName());
        to.setLastName(from.getLastName());
        to.getInstantEmbeddedData().setUpdated(Instant.now());
        to.setActive(from.isActive());
        to.setIndividual(from.getIndividual());
        map(from.getAddress(), to.getAddress());
    }

    private static void map(Address from, Address to) {
        to.setState(from.getState());
        to.setCity(from.getCity());
        to.setAddress(from.getAddress());
        to.setZip(from.getZip());
        to.getInstantEmbeddedData().setUpdated(Instant.now());
        map(from.getCountry(), to.getCountry());
    }

    private static void map(Country from, Country to) {
        to.setId(from.getId());
        to.setCode(from.getCode());
        to.setName(from.getName());
        to.getInstantEmbeddedData().setUpdated(Instant.now());
    }
}
