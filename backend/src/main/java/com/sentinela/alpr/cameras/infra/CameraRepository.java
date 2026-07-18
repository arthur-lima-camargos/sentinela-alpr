package com.sentinela.alpr.cameras.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentinela.alpr.cameras.domain.Camera;

public interface CameraRepository extends JpaRepository<Camera, Long> {
}
