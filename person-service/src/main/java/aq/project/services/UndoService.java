package aq.project.services;

import aq.project.entities.Person;
import aq.project.entities.UndoEvent;
import aq.project.exceptions.NotFoundRevisionException;
import aq.project.repositories.PersonRepository;
import aq.project.repositories.UndoEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UndoService {

    private final PersonRepository personRepository;
    private final UndoEventRepository undoEventRepository;

    @Transactional
    public void undoOperation(String personKeycloakId, UUID undoEventId) throws NotFoundRevisionException {
        Person revision = personRepository.findUndoRevisionByKeycloakId(personKeycloakId);
        Person restored = new Person(revision);
        personRepository.delete(revision);
        personRepository.save(restored);
        undoEventRepository.deleteById(undoEventId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID saveUndoEvent(UndoEvent undoEvent) {
        return undoEventRepository.save(undoEvent).getId();
    }
}
