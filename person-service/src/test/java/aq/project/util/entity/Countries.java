package aq.project.util.entity;

import aq.project.entities.Country;
import aq.project.entities.InstantEmbeddedData;

import static aq.project.util.entity.Constants.*;

public final class Countries {

    public static Country getUnknownCountry() {
        Country country = new Country();
        country.setId(85);
        country.setCode("UKN");
        country.setName("Unknown");
        return country;
    }

    public static Country getValidTestCountry() {
        Country country = new Country();
        country.setCode(COUNTRY_CODE);
        country.setName(COUNTRY_NAME);
        country.setInstantEmbeddedData(new InstantEmbeddedData());
        return country;
    }
}
