package com.sentinela.alpr.watchlist.api;

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

import com.sentinela.alpr.shared.error.NotFoundException;
import com.sentinela.alpr.watchlist.domain.WatchlistService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/watchlist")
class WatchlistController {

	private final WatchlistService service;

	WatchlistController(WatchlistService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<WatchlistResponse> add(@Valid @RequestBody WatchlistRequest request) {
		WatchlistResponse created = service.add(request);
		return ResponseEntity.created(URI.create("/api/v1/watchlist/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	WatchlistResponse update(@PathVariable Long id, @Valid @RequestBody WatchlistUpdateRequest request) {
		return service.update(id, request);
	}

	@GetMapping("/{id}")
	WatchlistResponse get(@PathVariable Long id) {
		return service.findById(id)
				.orElseThrow(() -> new NotFoundException("Watchlist item not found: " + id));
	}

	@GetMapping
	Page<WatchlistResponse> list(@PageableDefault(size = 20) Pageable pageable) {
		return service.list(pageable);
	}

	@GetMapping("/summary")
	WatchlistSummaryResponse summary() {
		return service.summary();
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> deactivate(@PathVariable Long id) {
		service.deactivate(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/activate")
	ResponseEntity<Void> activate(@PathVariable Long id) {
		service.activate(id);
		return ResponseEntity.noContent().build();
	}
}
