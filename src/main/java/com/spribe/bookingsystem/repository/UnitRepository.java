package com.spribe.bookingsystem.repository;

import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.UnitEntity;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<UnitEntity, Integer> {
    List<UnitEntity> findByTypeAndNumRoomsAndFloor(AccommodationType type, int numRooms, int floor);

    Page<UnitEntity> findByTypeAndNumRoomsAndFloorAndTotalCostBetween(
        AccommodationType type, int numRooms, int floor,
        BigDecimal minCost, BigDecimal maxCost, Pageable pageable
    );

    @Query("SELECT COUNT(u) FROM UnitEntity u WHERE u.events IS EMPTY")
    long countByEventsEmpty();
}
