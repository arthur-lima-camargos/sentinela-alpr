package com.sentinela.alpr.cameras.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinela.alpr.cameras.domain.CameraApiKeyService;

@RestController
@RequestMapping("/api/v1/cameras/{cameraId}/api-keys")
class CameraApiKeyController {

	private final CameraApiKeyService service;

	CameraApiKeyController(CameraApiKeyService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<IssuedApiKeyResponse> issue(@PathVariable Long cameraId) {
		IssuedApiKeyResponse issued = service.issue(cameraId);
		return ResponseEntity
				.created(URI.create("/api/v1/cameras/" + cameraId + "/api-keys/" + issued.id()))
				.body(issued);
	}

	@GetMapping
	List<ApiKeyResponse> list(@PathVariable Long cameraId) {
		return service.list(cameraId);
	}

	@DeleteMapping("/{keyId}")
	ResponseEntity<Void> revoke(@PathVariable Long cameraId, @PathVariable Long keyId) {
		service.revoke(cameraId, keyId);
		return ResponseEntity.noContent().build();
	}
}
