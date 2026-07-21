package com.sentinela.alpr.watchlist.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentinela.alpr.watchlist.domain.WatchedVehicle;

public interface WatchedVehicleRepository extends JpaRepository<WatchedVehicle, Long> {

	Optional<WatchedVehicle> findByPlateAndActiveTrue(String plate);

	long countByActiveTrue();

	boolean existsByPlate(String plate);
}
