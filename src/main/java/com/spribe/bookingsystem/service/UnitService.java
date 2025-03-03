package com.spribe.bookingsystem.service;

import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.exception.UnitNotFoundException;
import com.spribe.bookingsystem.mapper.UnitMapper;
import com.spribe.bookingsystem.payload.request.dto.UnitDto;
import com.spribe.bookingsystem.payload.response.SearchUnitResponse;
import com.spribe.bookingsystem.repository.UnitRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final PaymentService paymentService;
    private final CacheService cacheService;
    private final UserService userService;
    private final UnitMapper unitMapper;

    @Transactional
    public SearchUnitResponse searchUnits(AccommodationType type, Integer numRooms, Integer floor, BigDecimal minCost, BigDecimal maxCost, LocalDate startDate, LocalDate endDate, int page, int size, String sortBy, boolean asc) {
        Sort sort = asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        cleanupOrphanedPayments();

        Page<UnitEntity> currentPage
            = unitRepository.findAvailableUnits(type, numRooms, floor, minCost, maxCost, startDate, endDate, pageRequest);

        return SearchUnitResponse.builder()
            .total(currentPage.getTotalElements())
            .units(currentPage.getContent().stream().map(unitMapper::toUnitData).toList())
            .build();
    }

    public UnitEntity findById(Integer id) {
        return unitRepository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));
    }

    @Transactional
    public long getAvailableUnitsCount(LocalDate startDate, LocalDate endDate) {
        cleanupOrphanedPayments();

        return unitRepository.countAvailableUnits(startDate, endDate);
    }

    @Transactional
    public UnitEntity addUnit(UnitDto unitDto) {
        UserEntity user = userService.getUserById(unitDto.userId());
        UnitEntity unit = unitMapper.toEntity(unitDto, user);
        UnitEntity savedUnit = unitRepository.save(unit);


        cacheService.updateAvailableUnitsCacheSafely();
        return savedUnit;
    }

    private void cleanupOrphanedPayments() {
        Set<String> redisKeys = cacheService.getAvailabelKeys();

        // Cleanup orphaned events & payments before processing new ones (in case of crashed application)
        paymentService.cleanupOrphanedPayments(null, redisKeys);
    }

}


