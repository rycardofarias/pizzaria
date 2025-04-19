package com.pizzaria.components;

import com.pizzaria.interfaces.Auditable;
import com.pizzaria.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuditAspect {
    @Autowired
    private AuditService auditService;

    @Pointcut("@annotation(com.pizzaria.audit.Auditable)")
    public void auditableMethods() {}

    @AfterReturning(pointcut = "auditableMethods()", returning = "result")
    public void afterAuditableMethod(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);
        if (auditable == null) {
            auditable = joinPoint.getTarget().getClass().getAnnotation(Auditable.class);
        }
        String action = auditable != null ? auditable.action() : "";
        String entity = auditable != null ? auditable.entity() : "";
        String username = getCurrentUsername();
        String userId = username; // Supondo que username é único. Se tiver ID real, substituir aqui.
        String ipAddress = getClientIp();
        String module = joinPoint.getTarget().getClass().getSimpleName();
        String operationResult = "SUCESSO";
        String oldValue = null;
        String newValue = null;
        String details = "Método auditado: " + signature.toShortString();
        auditService.logEvent(
                username,
                userId,
                action,
                entity,
                null, // entityId
                oldValue,
                newValue,
                ipAddress,
                details,
                operationResult,
                module
        );
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            return req.getRemoteAddr();
        }
        return "unknown";
    }
}
