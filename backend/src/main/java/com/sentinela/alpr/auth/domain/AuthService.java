package com.sentinela.alpr.auth.domain;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import com.sentinela.alpr.auth.api.TokenResponse;
import com.sentinela.alpr.auth.infra.AppUserRepository;

@Service
public class AuthService {

	private final AppUserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;
	private final JwtDecoder jwtDecoder;

	AuthService(AppUserRepository repository, PasswordEncoder passwordEncoder,
			TokenService tokenService, JwtDecoder jwtDecoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.jwtDecoder = jwtDecoder;
	}

	public TokenResponse login(String login, String password) {
		AppUser user = repository.findByLoginAndActiveTrue(login)
				.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid credentials");
		}
		return tokenService.issue(user.getLogin(), user.getRole());
	}

	public TokenResponse refresh(String refreshToken) {
		Jwt jwt;
		try {
			jwt = jwtDecoder.decode(refreshToken);
		} catch (JwtException ex) {
			throw new BadCredentialsException("Invalid refresh token");
		}
		if (!TokenService.TYPE_REFRESH.equals(jwt.getClaimAsString(TokenService.CLAIM_TYPE))) {
			throw new BadCredentialsException("Provided token is not a refresh token");
		}

		AppUser user = repository.findByLoginAndActiveTrue(jwt.getSubject())
				.orElseThrow(() -> new BadCredentialsException("User is no longer active"));
		return tokenService.issue(user.getLogin(), user.getRole());
	}
}
