package com.pizzaria.controller;

import com.pizzaria.dto.request.UserCreateRequest;
import com.pizzaria.dto.request.UserUpdateRequest;
import com.pizzaria.dto.response.UserResponse;
import com.pizzaria.entity.User;
import com.pizzaria.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest request, HttpServletRequest httpRequest) {
        try {
            User user = userService.createUser(request);
            return ResponseEntity.ok(UserResponse.fromEntity(user));
        } catch (Exception e) {
            return buildErrorResponse(500, "user.error.create", "/api/users", httpRequest.getLocale());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isOwner(#id) or hasRole('ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(UserResponse.fromEntity(user));
        } catch (Exception e) {
            return buildErrorResponse(404, "user.error.not_found", "/api/users/" + id, httpRequest.getLocale());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isOwner(#id)")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            User user = userService.updateUser(id, request);
            return ResponseEntity.ok(UserResponse.fromEntity(user));
        } catch (Exception e) {
            return buildErrorResponse(400, "user.error.update", "/api/users/" + id, httpRequest.getLocale());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return buildErrorResponse(400, "user.error.delete", "/api/users/" + id, httpRequest.getLocale());
        }
    }
} 