package com.sentinela.alpr.shared.support;

import java.util.regex.Pattern;

public final class PlateNormalizer {

	public static final String PLATE_REGEX = "^[A-Z]{3}[0-9][0-9A-Z][0-9]{2}$";

	private static final Pattern PLATE_PATTERN = Pattern.compile(PLATE_REGEX);

	private PlateNormalizer() {
	}

	public static String normalize(String raw) {
		if (raw == null) {
			return null;
		}
		return raw.strip().replace("-", "").replace(" ", "").toUpperCase();
	}

	public static boolean isValid(String normalized) {
		return normalized != null && PLATE_PATTERN.matcher(normalized).matches();
	}
}
