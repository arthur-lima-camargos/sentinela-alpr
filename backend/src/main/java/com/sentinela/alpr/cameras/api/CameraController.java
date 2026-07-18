package com.sentinela.alpr.cameras.api;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentinela.alpr.cameras.domain.CameraService;
import com.sentinela.alpr.shared.error.NotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cameras")
class CameraController {

	private final CameraService service;

	CameraController(CameraService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<CameraResponse> create(@Valid @RequestBody CameraRequest request) {
		CameraResponse created = service.create(request);
		return ResponseEntity.created(URI.create("/api/v1/cameras/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	CameraResponse update(@PathVariable Long id, @Valid @RequestBody CameraRequest request) {
		return service.update(id, request);
	}

	@GetMapping("/{id}")
	CameraResponse get(@PathVariable Long id) {
		return service.findById(id)
				.orElseThrow(() -> new NotFoundException("Camera not found: " + id));
	}

	@GetMapping
	Page<CameraResponse> list(@PageableDefault(size = 20) Pageable pageable) {
		return service.list(pageable);
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> deactivate(@PathVariable Long id) {
		service.deactivate(id);
		return ResponseEntity.noContent().build();
	}
}
