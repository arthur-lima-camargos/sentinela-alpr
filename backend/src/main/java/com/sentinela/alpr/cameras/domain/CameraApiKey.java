package com.sentinela.alpr.cameras.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "camera_api_key")
public class CameraApiKey {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "camera_id", nullable = false)
	private Long cameraId;

	@Column(name = "key_hash", nullable = false)
	private String keyHash;

	@Column(name = "key_prefix", nullable = false)
	private String keyPrefix;

	@Column(nullable = false)
	private boolean active = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	protected CameraApiKey() {
	}

	CameraApiKey(Long cameraId, String keyHash, String keyPrefix) {
		this.cameraId = cameraId;
		this.keyHash = keyHash;
		this.keyPrefix = keyPrefix;
		this.active = true;
	}

	void revoke(Instant when) {
		this.active = false;
		this.revokedAt = when;
	}

	Long getId() {
		return id;
	}

	Long getCameraId() {
		return cameraId;
	}

	String getKeyPrefix() {
		return keyPrefix;
	}

	boolean isActive() {
		return active;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getRevokedAt() {
		return revokedAt;
	}
}
