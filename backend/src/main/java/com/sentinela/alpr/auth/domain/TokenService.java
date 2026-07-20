package com.sentinela.alpr.auth.domain;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.sentinela.alpr.auth.api.TokenResponse;

@Service
public class TokenService {

	static final String CLAIM_ROLE = "role";
	static final String CLAIM_TYPE = "type";
	static final String TYPE_ACCESS = "access";
	static final String TYPE_REFRESH = "refresh";

	private final JwtEncoder encoder;
	private final JwtProperties properties;

	TokenService(JwtEncoder encoder, JwtProperties properties) {
		this.encoder = encoder;
		this.properties = properties;
	}

	TokenResponse issue(String login, Role role) {
		Instant now = Instant.now();
		String access = encode(login, role, TYPE_ACCESS, now, now.plus(properties.accessTtl()));
		String refresh = encode(login, role, TYPE_REFRESH, now, now.plus(properties.refreshTtl()));
		return new TokenResponse(access, refresh, properties.accessTtl().toSeconds(), "Bearer");
	}

	private String encode(String login, Role role, String type, Instant issuedAt, Instant expiresAt) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(login)
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.claim(CLAIM_ROLE, role.name())
				.claim(CLAIM_TYPE, type)
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
