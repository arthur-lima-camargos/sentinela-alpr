package com.sentinela.alpr.alerts.api;

import com.sentinela.alpr.alerts.domain.AlertStatus;

import jakarta.validation.constraints.NotNull;

public record AlertStatusRequest(@NotNull AlertStatus status) {
}
