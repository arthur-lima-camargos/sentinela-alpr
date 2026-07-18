package com.sentinela.alpr.detections.infra;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.sentinela.alpr.detections.domain.Detection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

class DetectionRepositoryImpl implements DetectionRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<Detection> findPage(String plate, Long cameraId, Instant from, Instant to,
			Instant cursorTs, Long cursorId, int limit) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Detection> cq = cb.createQuery(Detection.class);
		Root<Detection> d = cq.from(Detection.class);
		Path<Instant> detectedAt = d.get("detectedAt");
		Path<Long> id = d.get("id");

		List<Predicate> where = new ArrayList<>();
		if (plate != null) {
			where.add(cb.equal(d.get("plate"), plate));
		}
		if (cameraId != null) {
			where.add(cb.equal(d.get("cameraId"), cameraId));
		}
		if (from != null) {
			where.add(cb.greaterThanOrEqualTo(detectedAt, from));
		}
		if (to != null) {
			where.add(cb.lessThanOrEqualTo(detectedAt, to));
		}
		if (cursorTs != null && cursorId != null) {
			// (detectedAt, id) < (cursorTs, cursorId) — seek estrito para a próxima página.
			where.add(cb.or(
					cb.lessThan(detectedAt, cursorTs),
					cb.and(cb.equal(detectedAt, cursorTs), cb.lessThan(id, cursorId))));
		}

		cq.where(where.toArray(Predicate[]::new));
		cq.orderBy(cb.desc(detectedAt), cb.desc(id));
		return em.createQuery(cq).setMaxResults(limit).getResultList();
	}
}
