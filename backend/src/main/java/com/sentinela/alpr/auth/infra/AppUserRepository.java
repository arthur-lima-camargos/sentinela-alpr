package com.sentinela.alpr.auth.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentinela.alpr.auth.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	Optional<AppUser> findByLoginAndActiveTrue(String login);
}
