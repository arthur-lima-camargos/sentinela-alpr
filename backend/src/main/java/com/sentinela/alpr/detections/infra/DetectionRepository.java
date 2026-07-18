package com.sentinela.alpr.detections.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentinela.alpr.detections.domain.Detection;

public interface DetectionRepository extends JpaRepository<Detection, Long>, DetectionRepositoryCustom {
}
