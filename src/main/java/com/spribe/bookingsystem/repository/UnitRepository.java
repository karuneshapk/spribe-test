package com.spribe.bookingsystem.repository;

import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.UnitEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<UnitEntity, Integer> {
    @Query("SELECT COUNT(u) FROM UnitEntity u WHERE u.events IS EMPTY")
    long countByEventsEmpty();

    @Query("SELECT unit FROM UnitEntity AS unit"
        + " WHERE (:type IS NULL OR unit.type = :type)"
        + " AND (:numRooms IS NULL OR unit.numRooms = :numRooms)"
        + " AND (:floor IS NULL OR unit.floor = :floor)"
        + " AND ("
        + "      (:minCost IS NULL AND :maxCost IS NULL) OR"
        + "      (:minCost IS NOT NULL AND :maxCost IS NOT NULL AND unit.totalCost BETWEEN :minCost AND :maxCost) OR"
        + "      (:minCost IS NULL AND :maxCost IS NOT NULL AND unit.totalCost <= :maxCost) OR"
        + "      (:minCost IS NOT NULL AND :maxCost IS NULL AND unit.totalCost >= :minCost)"
        + " )"
        + " AND NOT EXISTS ("
        + "     SELECT 1 "
        + "     FROM EventEntity event"
        + "     WHERE event.unit.id = unit.id"
        + "         AND event.startDate < :endDate"
        + "         AND event.endDate > :startDate"
        + " )"
    )
    Page<UnitEntity> findAvailableUnits(
        @Param("type") AccommodationType type,
        @Param("numRooms") Integer numRooms,
        @Param("floor") Integer floor,
        @Param("minCost") BigDecimal minCost,
        @Param("maxCost") BigDecimal maxCost,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable);

    @Query("SELECT COUNT(u) FROM UnitEntity u"
        + " WHERE NOT EXISTS ("
        + "     SELECT 1 FROM EventEntity e"
        + "     WHERE e.unit.id = u.id"
        + "     AND ("
        + "         (:startDate IS NULL AND :endDate IS NULL AND e.endDate > CURRENT_DATE) OR"
        + "         (:startDate IS NOT NULL AND :endDate IS NULL AND e.endDate > :startDate) OR"
        + "         (:startDate IS NULL AND :endDate IS NOT NULL AND e.startDate < :endDate) OR"
        + "         (:startDate IS NOT NULL AND :endDate IS NOT NULL AND e.startDate < :endDate AND e.endDate > :startDate)"
        + "     )"
        + "     AND e.status IN ('CONFIRMED', 'PENDING')"
        + " )")
    long countAvailableUnits(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
