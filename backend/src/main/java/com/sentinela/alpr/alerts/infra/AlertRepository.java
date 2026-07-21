package com.sentinela.alpr.alerts.infra;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sentinela.alpr.alerts.domain.Alert;
import com.sentinela.alpr.alerts.domain.AlertStatus;

public interface AlertRepository extends JpaRepository<Alert, Long> {

	Page<Alert> findByStatus(AlertStatus status, Pageable pageable);

	long countByStatus(AlertStatus status);

	boolean existsByDetectionId(Long detectionId);
}
