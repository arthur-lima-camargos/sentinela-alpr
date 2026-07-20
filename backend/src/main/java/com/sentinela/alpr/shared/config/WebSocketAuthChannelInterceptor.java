package com.sentinela.alpr.shared.config;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtDecoder jwtDecoder;

	WebSocketAuthChannelInterceptor(JwtDecoder jwtDecoder) {
		this.jwtDecoder = jwtDecoder;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
			accessor.setUser(authenticate(accessor.getFirstNativeHeader("Authorization")));
		}
		return message;
	}

	private UsernamePasswordAuthenticationToken authenticate(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			throw new MessagingException("Missing bearer token on CONNECT");
		}
		Jwt jwt;
		try {
			jwt = jwtDecoder.decode(authorizationHeader.substring(BEARER_PREFIX.length()));
		} catch (JwtException ex) {
			throw new MessagingException("Invalid token on CONNECT");
		}
		if (!"access".equals(jwt.getClaimAsString("type"))) {
			throw new MessagingException("Not an access token");
		}
		String role = jwt.getClaimAsString("role");
		List<GrantedAuthority> authorities = role == null
				? List.of()
				: List.of(new SimpleGrantedAuthority("ROLE_" + role));
		return new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, authorities);
	}
}
