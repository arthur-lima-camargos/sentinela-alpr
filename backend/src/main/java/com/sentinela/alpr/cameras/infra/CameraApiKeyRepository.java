package com.sentinela.alpr.cameras.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentinela.alpr.cameras.domain.CameraApiKey;

public interface CameraApiKeyRepository extends JpaRepository<CameraApiKey, Long> {

	Optional<CameraApiKey> findByKeyHashAndActiveTrue(String keyHash);

	List<CameraApiKey> findByCameraIdOrderByIdDesc(Long cameraId);

	Optional<CameraApiKey> findByIdAndCameraId(Long id, Long cameraId);
}
