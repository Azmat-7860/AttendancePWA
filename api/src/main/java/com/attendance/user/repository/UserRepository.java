package com.attendance.user.repository;

import com.attendance.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCaseAndActiveTrue(String email);

    Optional<User> findByGoogleSubAndActiveTrue(String googleSub);

    Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<User> findByOrganizationId(UUID organizationId);

    Optional<User> findByOrganizationIdAndEmailIgnoreCase(UUID organizationId, String email);

    boolean existsByOrganizationIdAndEmailIgnoreCase(UUID organizationId, String email);
}
