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

@Service
@RequiredArgsConstructor
public class UndoService {

    private final PersonRepository personRepository;
    private final UndoEventRepository undoEventRepository;

    @Transactional
    public void undoOperation(UndoEvent undoEvent) throws NotFoundRevisionException {
        Person revision = personRepository.findUndoRevisionByKeycloakId(undoEvent.getPersonKeycloakId().toString());
        Person restored = new Person(revision);
        personRepository.delete(revision);
        personRepository.save(restored);
        undoEventRepository.delete(undoEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveUndoEvent(UndoEvent undoEvent) {
        undoEventRepository.save(undoEvent);
    }
}
