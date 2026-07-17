/**
 * detections module — high-volume core: recording (append-only) and keyset
 * querying. Publishes DetectionRecordedEvent.
 * Boundary: exposes a public API; entities/repositories are package-private.
 */
package com.sentinela.alpr.detections;
