package com.sentinela.alpr.cameras.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinela.alpr.cameras.api.ApiKeyResponse;
import com.sentinela.alpr.cameras.api.CameraResponse;
import com.sentinela.alpr.cameras.api.IssuedApiKeyResponse;
import com.sentinela.alpr.cameras.infra.CameraApiKeyRepository;
import com.sentinela.alpr.shared.error.NotFoundException;

@Service
public class CameraApiKeyService {

	private static final String KEY_PREFIX = "alpr_";
	private static final int SECRET_BYTES = 32;
	private static final int DISPLAY_PREFIX_LEN = KEY_PREFIX.length() + 8;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final CameraApiKeyRepository repository;
	private final CameraService cameraService;

	CameraApiKeyService(CameraApiKeyRepository repository, CameraService cameraService) {
		this.repository = repository;
		this.cameraService = cameraService;
	}

	@Transactional
	public IssuedApiKeyResponse issue(Long cameraId) {
		requireCamera(cameraId);
		String key = generateKey();
		String prefix = key.substring(0, DISPLAY_PREFIX_LEN);
		CameraApiKey saved = repository.save(new CameraApiKey(cameraId, sha256Hex(key), prefix));
		return new IssuedApiKeyResponse(saved.getId(), cameraId, key, saved.getKeyPrefix(), saved.getCreatedAt());
	}

	@Transactional(readOnly = true)
	public List<ApiKeyResponse> list(Long cameraId) {
		requireCamera(cameraId);
		return repository.findByCameraIdOrderByIdDesc(cameraId).stream()
				.map(CameraApiKeyService::toResponse)
				.toList();
	}

	@Transactional
	public void revoke(Long cameraId, Long keyId) {
		CameraApiKey key = repository.findByIdAndCameraId(keyId, cameraId)
				.orElseThrow(() -> new NotFoundException("API key not found: " + keyId));
		key.revoke(Instant.now());
	}

	@Transactional(readOnly = true)
	public Optional<Long> resolveCamera(String presentedKey) {
		if (presentedKey == null || presentedKey.isBlank()) {
			return Optional.empty();
		}
		return repository.findByKeyHashAndActiveTrue(sha256Hex(presentedKey))
				.filter(key -> cameraService.findById(key.getCameraId())
						.map(CameraResponse::active).orElse(false))
				.map(CameraApiKey::getCameraId);
	}

	private void requireCamera(Long cameraId) {
		cameraService.findById(cameraId)
				.orElseThrow(() -> new NotFoundException("Camera not found: " + cameraId));
	}

	private static String generateKey() {
		byte[] raw = new byte[SECRET_BYTES];
		RANDOM.nextBytes(raw);
		return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
	}

	private static String sha256Hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 not available", ex);
		}
	}

	private static ApiKeyResponse toResponse(CameraApiKey key) {
		return new ApiKeyResponse(key.getId(), key.getCameraId(), key.getKeyPrefix(),
				key.isActive(), key.getCreatedAt(), key.getRevokedAt());
	}
}
