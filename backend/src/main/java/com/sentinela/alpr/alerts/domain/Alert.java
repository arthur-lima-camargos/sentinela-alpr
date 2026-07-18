package com.sentinela.alpr.alerts.domain;

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
@Table(name = "alert")
public class Alert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String plate;

	@Column(name = "detection_id", nullable = false)
	private Long detectionId;

	@Column(name = "watched_vehicle_id", nullable = false)
	private Long watchedVehicleId;

	@Column(name = "detected_at", nullable = false)
	private Instant detectedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AlertStatus status = AlertStatus.NEW;

	@Version
	@Column(nullable = false)
	private long version;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Alert() {
	}

	Alert(String plate, Long detectionId, Long watchedVehicleId, Instant detectedAt) {
		this.plate = plate;
		this.detectionId = detectionId;
		this.watchedVehicleId = watchedVehicleId;
		this.detectedAt = detectedAt;
		this.status = AlertStatus.NEW;
	}

	void markSeen() {
		this.status = AlertStatus.SEEN;
	}

	void setStatus(AlertStatus status) {
		this.status = status;
	}

	Long getId() {
		return id;
	}

	String getPlate() {
		return plate;
	}

	Long getDetectionId() {
		return detectionId;
	}

	Long getWatchedVehicleId() {
		return watchedVehicleId;
	}

	Instant getDetectedAt() {
		return detectedAt;
	}

	AlertStatus getStatus() {
		return status;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
