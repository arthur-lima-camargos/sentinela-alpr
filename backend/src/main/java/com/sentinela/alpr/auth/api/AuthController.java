package com.sentinela.alpr.auth.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinela.alpr.auth.domain.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

	private final AuthService service;

	AuthController(AuthService service) {
		this.service = service;
	}

	@PostMapping("/login")
	TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return service.login(request.login(), request.password());
	}

	@PostMapping("/refresh")
	TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return service.refresh(request.refreshToken());
	}
}
