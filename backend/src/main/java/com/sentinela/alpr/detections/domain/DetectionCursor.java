package com.sentinela.alpr.detections.domain;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import com.sentinela.alpr.shared.error.BusinessRuleException;

record DetectionCursor(Instant detectedAt, Long id) {

	String encode() {
		String raw = detectedAt.toString() + "|" + id;
		return Base64.getUrlEncoder().withoutPadding()
				.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
	}

	static DetectionCursor decode(String token) {
		try {
			String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
			int sep = raw.lastIndexOf('|');
			Instant ts = Instant.parse(raw.substring(0, sep));
			Long id = Long.valueOf(raw.substring(sep + 1));
			return new DetectionCursor(ts, id);
		}
		catch (RuntimeException ex) {
			throw new BusinessRuleException("Cursor de paginação inválido.");
		}
	}
}
