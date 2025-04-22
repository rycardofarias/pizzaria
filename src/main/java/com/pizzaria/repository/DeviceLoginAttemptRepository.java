package com.pizzaria.repository;

import com.pizzaria.entity.DeviceLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceLoginAttemptRepository extends JpaRepository<DeviceLoginAttempt, Long> {
    Optional<DeviceLoginAttempt> findByDeviceFingerprint(String deviceFingerprint);
    void deleteByDeviceFingerprint(String deviceFingerprint);
}
