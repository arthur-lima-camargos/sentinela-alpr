package com.sentinela.alpr.cameras.domain;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "camera")
public class Camera {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	private BigDecimal latitude;

	private BigDecimal longitude;

	private String road;

	@Column(nullable = false)
	private boolean active = true;

	@Version
	@Column(nullable = false)
	private long version;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Camera() {
	}

	Camera(String name, BigDecimal latitude, BigDecimal longitude, String road) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.road = road;
		this.active = true;
	}

	Long getId() {
		return id;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	BigDecimal getLatitude() {
		return latitude;
	}

	void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	BigDecimal getLongitude() {
		return longitude;
	}

	void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	String getRoad() {
		return road;
	}

	void setRoad(String road) {
		this.road = road;
	}

	boolean isActive() {
		return active;
	}

	void setActive(boolean active) {
		this.active = active;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
