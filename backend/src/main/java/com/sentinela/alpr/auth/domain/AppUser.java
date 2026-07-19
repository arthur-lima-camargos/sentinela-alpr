package com.sentinela.alpr.auth.domain;

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
@Table(name = "app_user")
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String login;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable = false)
	private boolean active = true;

	@Version
	@Column(nullable = false)
	private long version;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected AppUser() {
	}

	AppUser(String login, String passwordHash, Role role) {
		this.login = login;
		this.passwordHash = passwordHash;
		this.role = role;
		this.active = true;
	}

	Long getId() {
		return id;
	}

	String getLogin() {
		return login;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	Role getRole() {
		return role;
	}

	boolean isActive() {
		return active;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
