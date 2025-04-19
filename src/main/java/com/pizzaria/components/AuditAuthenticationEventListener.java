package com.pizzaria.components;

import com.pizzaria.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditAuthenticationEventListener implements ApplicationListener<ApplicationEvent> {
    @Autowired
    private AuditService auditService;

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            return req.getRemoteAddr();
        }
        return "unknown";
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        String ip = getClientIp();
        if (event instanceof AuthenticationSuccessEvent success) {
            Authentication auth = success.getAuthentication();
            auditService.logEvent(auth.getName(), auth.getName(), "LOGIN_SUCCESS", "User", null, null, null, ip, "Login bem-sucedido", "SUCESSO", "Authentication");
        } else if (event instanceof LogoutSuccessEvent logout) {
            Authentication auth = logout.getAuthentication();
            if (auth != null)
                auditService.logEvent(auth.getName(), auth.getName(), "LOGOUT", "User", null, null, null, ip, "Logout realizado", "SUCESSO", "Authentication");
        } else if (event instanceof AuthenticationFailureBadCredentialsEvent fail) {
            Authentication auth = fail.getAuthentication();
            auditService.logEvent(auth.getName(), auth.getName(), "LOGIN_FAIL", "User", null, null, null, ip, "Falha de login: credenciais inválidas", "FALHA", "Authentication");
        }
    }
}
