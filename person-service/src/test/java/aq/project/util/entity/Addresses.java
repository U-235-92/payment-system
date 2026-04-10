package aq.project.util.entity;

import aq.project.entities.Address;
import aq.project.entities.Country;
import aq.project.entities.InstantEmbeddedData;

import static aq.project.util.entity.Constants.*;
import static aq.project.util.entity.Constants.CORRECT_ZIP;

public abstract class Addresses {

    public static Address getValidAddresses(Country country) {
        Address address = new Address();
        address.setCountry(country);
        address.setState(CORRECT_STATE);
        address.setCity(CORRECT_CITY);
        address.setAddress(CORRECT_ADDRESS);
        address.setZip(CORRECT_ZIP);
        address.setInstantEmbeddedData(new InstantEmbeddedData());
        return address;
    }

    public static Address getInvalidAddresses() {
        Address address = new Address();
        address.setState("WRONG");
        address.setCity(null);
        address.setZip("WRONG_ZIP");
        address.setInstantEmbeddedData(new InstantEmbeddedData());
        return address;
    }
}
