package com.pizzaria.repository;

import com.pizzaria.entity.IpLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IpLoginAttemptRepository extends JpaRepository<IpLoginAttempt, Long> {
    Optional<IpLoginAttempt> findByIpAddress(String ipAddress);
    void deleteByIpAddress(String ipAddress);
}
