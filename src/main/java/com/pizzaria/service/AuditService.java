package com.pizzaria.service;

import com.pizzaria.repository.AuditLogRepository;

import com.pizzaria.entity.AuditLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


@Service
public class AuditService {


    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logEvent(String username, String userId, String action, String entity, String entityId, String oldValue, String newValue, String ipAddress, String details, String result, String module) {
        AuditLog log = new AuditLog();
        log.setTimestamp(LocalDateTime.now());
        log.setUsername(username);
        log.setUserId(userId);
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        log.setResult(result);
        log.setModule(module);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
