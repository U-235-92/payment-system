package aq.project.mappers;

import aq.project.entities.Address;
import aq.project.entities.Individual;
import aq.project.entities.Person;

import java.time.Instant;

public final class PersonMapper {

    private PersonMapper() {}

    public static void map(Person from, Person to) {
        if(isPersonUpdated(from, to)) {
            to.setFirstName(from.getFirstName());
            to.setLastName(from.getLastName());
            to.setActive(from.isActive());
            to.getInstantEmbeddedData().setUpdated(Instant.now());
        }
        map(from.getAddress(), to.getAddress());
        map(from.getIndividual(), to.getIndividual());
    }

    private static void map(Address from, Address to) {
        if(isAddressUpdated(from, to)) {
            to.setState(from.getState());
            to.setCity(from.getCity());
            to.setAddress(from.getAddress());
            to.setZip(from.getZip());
            to.getInstantEmbeddedData().setUpdated(Instant.now());
        }
    }

    private static void map(Individual from, Individual to) {
        if(isIndividualUpdated(from, to)) {
            to.setEmail(from.getEmail());
            to.setPassportNumber(from.getPassportNumber());
            to.setPhoneNumber(from.getPhoneNumber());
            to.getInstantEmbeddedData().setUpdated(Instant.now());
        }
    }

    private static boolean isPersonUpdated(Person from, Person to) {
        if(from.hashCode() != to.hashCode()) {
            return !from.equals(to);
        }
        return false;
    }

    private static boolean isAddressUpdated(Address from, Address to) {
        if(from.hashCode() != to.hashCode()) {
            return !from.equals(to);
        }
        return false;
    }

    private static boolean isIndividualUpdated(Individual from, Individual to) {
        if(from.hashCode() != to.hashCode()) {
            return !from.equals(to);
        }
        return false;
    }
}
