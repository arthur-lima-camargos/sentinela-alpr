package com.sentinela.alpr.detections.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "detection")
public class Detection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String plate;

	@Column(name = "camera_id", nullable = false)
	private Long cameraId;

	@Column(name = "detected_at", nullable = false)
	private Instant detectedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Detection() {
	}

	Detection(String plate, Long cameraId, Instant detectedAt) {
		this.plate = plate;
		this.cameraId = cameraId;
		this.detectedAt = detectedAt;
	}

	Long getId() {
		return id;
	}

	String getPlate() {
		return plate;
	}

	Long getCameraId() {
		return cameraId;
	}

	Instant getDetectedAt() {
		return detectedAt;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
