package com.pizzaria.controller;

import com.pizzaria.entity.IpLoginAttempt;
import com.pizzaria.repository.IpLoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/ip-blocks")
@RequiredArgsConstructor
public class IpBlockAdminController extends BaseController {
    private final IpLoginAttemptRepository ipLoginAttemptRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getIpBlocks(HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<IpLoginAttempt> ipLoginAttempts = ipLoginAttemptRepository.findAll().stream()
                .filter(ip -> ip.getLockoutTime() != null && ip.getLockoutTime().plusMinutes(30).isAfter(now))
                .toList();
            return ResponseEntity.ok(ipLoginAttempts);
        } catch (Exception e) {
            return buildErrorResponse(500, "login.error.unexpected", "/api/admin/ip-blocks", request.getLocale());
        }
    }

    @DeleteMapping("/{ip}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unblockIp(@PathVariable("ip") String ip) {
        ipLoginAttemptRepository.deleteByIpAddress(ip);
        return ResponseEntity.noContent().build();
    }
}
