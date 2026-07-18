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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@GetMapping
	Page<WatchlistResponse> list(@PageableDefault(size = 20) Pageable pageable) {
		return service.list(pageable);
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> remove(@PathVariable Long id) {
		service.remove(id);
		return ResponseEntity.noContent().build();
	}
}
