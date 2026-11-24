package com.attendance.worker.repository;

import com.attendance.worker.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, UUID> {

    Optional<Worker> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<Worker> findByOrganizationId(UUID organizationId);

    List<Worker> findByOrganizationIdAndActiveTrue(UUID organizationId);
}
