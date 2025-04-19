package com.pizzaria.controller;

import com.pizzaria.repository.AuditLogRepository;
import com.pizzaria.entity.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit/logs")
public class AuditLogController {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}
