package com.spribe.bookingsystem.repository;

import com.spribe.bookingsystem.entity.PaymentEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {

    @Query("SELECT P FROM PaymentEntity AS P"
        + " JOIN P.event E"
        + " WHERE (:unitId IS NULL OR E.unit.id = :unitId)"
        + "     AND (:paymentIds IS NULL OR P.id NOT IN :paymentIds) "
        + "     AND P.status = 'PENDING'"
    )
    List<PaymentEntity> findOrphanedPaymentsNotInIds(@Param("unitId") Integer unitId, @Param("paymentIds") List<Integer> paymentIds);

    @Query("SELECT COUNT(p) = 0 FROM PaymentEntity p"
        + " JOIN p.event e"
        + " WHERE e.unit.id = :unitId"
        + "   AND e.status = 'CONFIRMED'"
        + "   AND (e.startDate <= :endDate AND e.endDate >= :startDate)")
    boolean isUnitAvailableForBooking(@Param("unitId") int unitId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(p) = 0 FROM PaymentEntity p"
        + " JOIN p.event e"
        + " WHERE e.unit.id = :unitId"
        + "   AND e.status = 'CONFIRMED'"
        + "   AND (e.startDate <= :endDate AND e.endDate >= :startDate)")
    long availableUnits(@Param("unitId") int unitId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}
