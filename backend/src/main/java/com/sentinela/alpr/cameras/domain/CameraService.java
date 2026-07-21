package com.sentinela.alpr.cameras.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinela.alpr.cameras.api.CameraRequest;
import com.sentinela.alpr.cameras.api.CameraResponse;
import com.sentinela.alpr.cameras.api.CameraSummaryResponse;
import com.sentinela.alpr.cameras.infra.CameraRepository;
import com.sentinela.alpr.shared.error.NotFoundException;

@Service
public class CameraService {

	private final CameraRepository repository;

	CameraService(CameraRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public CameraResponse create(CameraRequest request) {
		Camera camera = new Camera(request.name(), request.latitude(), request.longitude(), request.road());
		return toResponse(repository.save(camera));
	}

	@Transactional
	public CameraResponse update(Long id, CameraRequest request) {
		Camera camera = require(id);
		camera.setName(request.name());
		camera.setLatitude(request.latitude());
		camera.setLongitude(request.longitude());
		camera.setRoad(request.road());
		return toResponse(camera);
	}

	@Transactional(readOnly = true)
	public Optional<CameraResponse> findById(Long id) {
		return repository.findById(id).map(CameraService::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<CameraResponse> list(Pageable pageable) {
		return repository.findAll(pageable).map(CameraService::toResponse);
	}

	@Transactional(readOnly = true)
	public CameraSummaryResponse summary() {
		return new CameraSummaryResponse(repository.countByActiveTrue(), repository.countByActiveFalse());
	}

	@Transactional
	public void deactivate(Long id) {
		require(id).setActive(false);
	}

	@Transactional
	public void activate(Long id) {
		require(id).setActive(true);
	}

	private Camera require(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new NotFoundException("Camera not found: " + id));
	}

	private static CameraResponse toResponse(Camera c) {
		return new CameraResponse(c.getId(), c.getName(), c.getLatitude(), c.getLongitude(),
				c.getRoad(), c.isActive(), c.getCreatedAt());
	}
}
