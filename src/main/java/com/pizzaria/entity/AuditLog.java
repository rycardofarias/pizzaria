package com.pizzaria.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private String username;
    private String userId;
    private String action;
    private String entity;
    private String entityId;
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    @Column(columnDefinition = "TEXT")
    private String newValue;
    private String ipAddress;
    private String details;
    private String result;
    private String module;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
}
