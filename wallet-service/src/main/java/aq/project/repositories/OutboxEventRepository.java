package aq.project.repositories;

import aq.project.entities.OutboxEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OutboxEventRepository extends PagingAndSortingRepository<OutboxEvent, String>, CrudRepository<OutboxEvent, String> {
}
