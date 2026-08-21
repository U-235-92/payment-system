package aq.project.repositories;

import aq.project.entities.Webhook;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookRepository extends CrudRepository<Webhook, Long> {

    List<Webhook> findByTransactionId(UUID transactionId);

    List<Webhook> findByEventType(String eventType);
}