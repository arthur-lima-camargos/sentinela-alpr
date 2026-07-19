package com.sentinela.alpr.shared.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.sentinela.alpr.auth.domain.JwtProperties;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
class SecurityConfig {

	private final JwtProperties jwtProperties;

	SecurityConfig(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// Público
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
						.requestMatchers("/actuator/health/**").permitAll()
						// Provisório: handshake do STOMP (segurança na Fase 4c)
						.requestMatchers("/ws/**").permitAll()
						// Provisório: ingestão de detecções (vira API Key na Fase 4b)
						.requestMatchers(HttpMethod.POST, "/api/v1/detections/**").permitAll()
						// Escrita de cadastros exige ADMIN
						.requestMatchers(HttpMethod.POST, "/api/v1/cameras/**", "/api/v1/watchlist/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/v1/cameras/**", "/api/v1/watchlist/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/v1/cameras/**", "/api/v1/watchlist/**").hasRole("ADMIN")
						// Demais exigem autenticação (OPERATOR ou ADMIN)
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(problemDetailEntryPoint())
						.accessDeniedHandler(problemDetailAccessDeniedHandler()));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private SecretKey secretKey() {
		byte[] bytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder() {
		return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey()));
	}

	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withSecretKey(secretKey()).macAlgorithm(MacAlgorithm.HS256).build();
	}

	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			String role = jwt.getClaimAsString("role");
			return role == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + role));
		});
		return converter;
	}

	private AuthenticationEntryPoint problemDetailEntryPoint() {
		return (request, response, ex) -> writeProblem(response, HttpServletResponse.SC_UNAUTHORIZED,
				"Unauthorized", "Authentication is required to access this resource.");
	}

	private AccessDeniedHandler problemDetailAccessDeniedHandler() {
		return (request, response, ex) -> writeProblem(response, HttpServletResponse.SC_FORBIDDEN,
				"Forbidden", "You do not have permission to access this resource.");
	}

	private static void writeProblem(HttpServletResponse response, int status, String title, String detail)
			throws java.io.IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + title
				+ "\",\"status\":" + status + ",\"detail\":\"" + detail + "\"}");
	}
}
