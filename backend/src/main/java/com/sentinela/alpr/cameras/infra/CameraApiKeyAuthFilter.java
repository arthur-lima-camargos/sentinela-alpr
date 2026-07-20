package com.sentinela.alpr.cameras.infra;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sentinela.alpr.cameras.domain.CameraApiKeyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CameraApiKeyAuthFilter extends OncePerRequestFilter {

	static final String HEADER = "X-API-Key";
	private static final String ROLE_CAMERA = "ROLE_CAMERA";

	private final CameraApiKeyService apiKeyService;

	public CameraApiKeyAuthFilter(CameraApiKeyService apiKeyService) {
		this.apiKeyService = apiKeyService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String presentedKey = request.getHeader(HEADER);
		if (presentedKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			apiKeyService.resolveCamera(presentedKey).ifPresent(cameraId -> {
				var authentication = new UsernamePasswordAuthenticationToken(
						cameraId, null, List.of(new SimpleGrantedAuthority(ROLE_CAMERA)));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			});
		}
		chain.doFilter(request, response);
	}
}
