package com.sentinela.alpr.watchlist.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sentinela.alpr.shared.error.BusinessRuleException;
import com.sentinela.alpr.shared.error.ConflictException;
import com.sentinela.alpr.shared.error.NotFoundException;
import com.sentinela.alpr.shared.support.PlateNormalizer;
import com.sentinela.alpr.watchlist.api.WatchlistRequest;
import com.sentinela.alpr.watchlist.api.WatchlistResponse;
import com.sentinela.alpr.watchlist.api.WatchlistSummaryResponse;
import com.sentinela.alpr.watchlist.api.WatchlistUpdateRequest;
import com.sentinela.alpr.watchlist.infra.WatchedVehicleRepository;

@Service
public class WatchlistService {

	private final WatchedVehicleRepository repository;

	WatchlistService(WatchedVehicleRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public WatchlistResponse add(WatchlistRequest request) {
		String plate = normalizePlate(request.plate());
		if (repository.existsByPlate(plate)) {
			throw new ConflictException("Plate already in watchlist: " + plate);
		}
		boolean active = request.active() == null || request.active();
		WatchedVehicle saved = repository.save(new WatchedVehicle(plate, request.reason(), active));
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public Optional<WatchlistResponse> findById(Long id) {
		return repository.findById(id).map(WatchlistService::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<WatchlistResponse> list(Pageable pageable) {
		return repository.findAll(pageable).map(WatchlistService::toResponse);
	}

	@Transactional(readOnly = true)
	public WatchlistSummaryResponse summary() {
		return new WatchlistSummaryResponse(repository.countByActiveTrue());
	}

	@Transactional
	public WatchlistResponse update(Long id, WatchlistUpdateRequest request) {
		WatchedVehicle vehicle = require(id);
		vehicle.setReason(request.reason());
		return toResponse(vehicle);
	}

	@Transactional
	public void deactivate(Long id) {
		require(id).setActive(false);
	}

	@Transactional
	public void activate(Long id) {
		require(id).setActive(true);
	}

	@Transactional(readOnly = true)
	public Optional<WatchlistResponse> findActiveMatch(String plate) {
		return repository.findByPlateAndActiveTrue(PlateNormalizer.normalize(plate))
				.map(WatchlistService::toResponse);
	}

	private WatchedVehicle require(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new NotFoundException("Watchlist item not found: " + id));
	}

	private static String normalizePlate(String raw) {
		String plate = PlateNormalizer.normalize(raw);
		if (!PlateNormalizer.isValid(plate)) {
			throw new BusinessRuleException("Plate is invalid: " + raw);
		}
		return plate;
	}

	private static WatchlistResponse toResponse(WatchedVehicle w) {
		return new WatchlistResponse(w.getId(), w.getPlate(), w.getReason(), w.isActive(), w.getCreatedAt());
	}
}
