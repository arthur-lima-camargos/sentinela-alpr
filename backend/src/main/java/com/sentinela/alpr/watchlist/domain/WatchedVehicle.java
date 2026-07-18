package com.sentinela.alpr.watchlist.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "watched_vehicle")
public class WatchedVehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String plate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WatchReason reason;

	@Column(nullable = false)
	private boolean active = true;

	@Version
	@Column(nullable = false)
	private long version;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected WatchedVehicle() {
	}

	WatchedVehicle(String plate, WatchReason reason, boolean active) {
		this.plate = plate;
		this.reason = reason;
		this.active = active;
	}

	Long getId() {
		return id;
	}

	String getPlate() {
		return plate;
	}

	WatchReason getReason() {
		return reason;
	}

	boolean isActive() {
		return active;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
