package aq.project.repositories;

import aq.project.entities.Webhook;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WebhookRepository extends CrudRepository<Webhook, Long> {

    @Query("SELECT w FROM Webhook w WHERE w.processed = false")
    List<Webhook> findAllUnprocessed();
}
