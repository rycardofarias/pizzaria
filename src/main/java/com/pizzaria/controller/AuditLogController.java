package com.pizzaria.controller;

import com.pizzaria.repository.AuditLogRepository;
import com.pizzaria.entity.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/audit/logs")
public class AuditLogController extends BaseController {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<?> getAuditLogs(Pageable pageable, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(auditLogRepository.findAll(pageable));
        } catch (Exception e) {
            return buildErrorResponse(500, "login.error.unexpected", "/api/audit/logs", request.getLocale());
        }
    }
}
