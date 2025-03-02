package com.spribe.bookingsystem.service;

import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.repository.UnitRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnitService {
    private final UnitRepository unitRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String AVAILABLE_UNITS_KEY = "available_units_count";

    public Page<UnitEntity> searchUnits(AccommodationType type, int numRooms, int floor, BigDecimal minCost, BigDecimal maxCost, LocalDate startDate, LocalDate endDate, int page, int size, String sortBy, boolean asc) {
        Sort sort = asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return unitRepository.findByTypeAndNumRoomsAndFloorAndTotalCostBetween(type, numRooms, floor, minCost, maxCost, pageRequest);
    }

    @Transactional
    public UnitEntity addUnit(UnitEntity unit) {
        unit.setTotalCost(unit.getBaseCost().multiply(BigDecimal.valueOf(1.15))); // Add 15% markup
        UnitEntity savedUnit = unitRepository.save(unit);
        updateAvailableUnitsCacheSafely(); // Ensure thread safety
        return savedUnit;
    }

    public void updateAvailability(int unitId) {
        updateAvailableUnitsCacheSafely();
    }

    private void updateAvailableUnitsCacheSafely() {
        redisTemplate.watch(AVAILABLE_UNITS_KEY); // Watch key for changes
        long availableUnits = unitRepository.countByEventsEmpty(); // Get latest count

        redisTemplate.multi(); // Start Redis transaction
        redisTemplate.opsForValue().set(AVAILABLE_UNITS_KEY, String.valueOf(availableUnits));
        redisTemplate.exec(); // Execute Redis transaction
    }

    public long getAvailableUnits() {
        String count = redisTemplate.opsForValue().get(AVAILABLE_UNITS_KEY);
        return count != null ? Long.parseLong(count) : 0;
    }
}


