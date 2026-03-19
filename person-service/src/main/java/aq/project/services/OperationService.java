package aq.project.services;

import aq.project.entities.ServiceOperation;
import aq.project.repositories.ServiceOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OperationService {

    private final ServiceOperationRepository serviceOperationRepository;

    public ServiceOperation createServiceOperation(String operation, String status, String description) {
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setDescription(description);
        serviceOperation.setOperation(operation);
        serviceOperation.setStatus(status);
        return serviceOperation;
    }

    @Transactional
    public void saveServiceOperation(ServiceOperation serviceOperation) {
        serviceOperationRepository.save(serviceOperation);
    }
}
