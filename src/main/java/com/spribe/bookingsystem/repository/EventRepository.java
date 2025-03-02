package com.spribe.bookingsystem.repository;

import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.entity.EventStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Integer> {
    List<EventEntity> findByUnitIdAndStatus(Integer unitId, EventStatus status);
    List<EventEntity> findByUserId(Integer userId);
}
