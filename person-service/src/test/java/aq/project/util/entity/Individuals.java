package aq.project.util.entity;

import aq.project.entities.Individual;
import aq.project.entities.InstantEmbeddedData;

import static aq.project.util.entity.Constants.*;

public abstract class Individuals {

    public static Individual getValidIndividual() {
        Individual individual = new Individual();
        individual.setEmail(CORRECT_EMAIL);
        individual.setPhoneNumber(CORRECT_PHONE);
        individual.setPassportNumber(CORRECT_PASSPORT);
        individual.setInstantEmbeddedData(new InstantEmbeddedData());
        return individual;
    }

    public static Individual getInvalidIndividual() {
        Individual individual = new Individual();
        individual.setEmail("wrong@post.aq");
        individual.setPhoneNumber("wrong");
        individual.setPassportNumber("wrong");
        individual.setInstantEmbeddedData(new InstantEmbeddedData());
        return individual;
    }
}
