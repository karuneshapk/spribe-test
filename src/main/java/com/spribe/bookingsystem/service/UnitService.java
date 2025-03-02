package com.spribe.bookingsystem.service;

import static com.spribe.bookingsystem.util.Constants.REDIS_UNITS_KEY;
import static java.lang.Integer.parseInt;
import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.mapper.UnitMapper;
import com.spribe.bookingsystem.payload.request.dto.UnitDto;
import com.spribe.bookingsystem.payload.response.SearchUnitResponse;
import com.spribe.bookingsystem.payload.response.data.UnitData;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import com.spribe.bookingsystem.repository.UnitRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UserService userService;
    private final EventRepository eventRepository;
    private final UnitRepository unitRepository;
    private final StringRedisTemplate redisTemplate;
    private final PaymentRepository paymentRepository;
    private final UnitMapper unitMapper;

    private static final String AVAILABLE_UNITS_KEY = "available_units_count";

    @Transactional
    public SearchUnitResponse searchUnits(AccommodationType type, Integer numRooms, Integer floor, BigDecimal minCost, BigDecimal maxCost, LocalDate startDate, LocalDate endDate, int page, int size, String sortBy, boolean asc) {
        Sort sort = asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // Step 1: Check if unit is in Redis (already booked)
        Set<String> redisKeys = redisTemplate.keys(REDIS_UNITS_KEY + ":*");

        // Step 2: Cleanup orphaned events & payments before processing new ones (in case of crashed application)
        cleanupOrphanedPayments(null, redisKeys);
        Page<UnitEntity> currentPage = unitRepository.findAvailableUnits(type, numRooms, floor, minCost, maxCost,
            startDate, endDate, pageRequest);

        long totalElements = currentPage.getTotalElements();
        List<UnitData> units = currentPage.getContent().stream().map(unitMapper::toUnitData).toList();

        return SearchUnitResponse.builder().total(totalElements).units(units).build();
    }

    public UnitEntity findById(Integer id) {
        return unitRepository.findById(id).orElseThrow(() -> new RuntimeException("Unit not found"));
    }

    @Transactional
    public long getAvailableUnitsCount(LocalDate startDate, LocalDate endDate) {
        // Step 1: Check if unit is in Redis (already booked)
        Set<String> redisKeys = redisTemplate.keys(REDIS_UNITS_KEY + ":*");

        // Step 2: Cleanup orphaned events & payments before processing new ones (in case of crashed application)
        cleanupOrphanedPayments(null, redisKeys);

        return unitRepository.countAvailableUnits(startDate, endDate);
    }

    @Transactional
    public UnitEntity addUnit(UnitDto unitDto) {
        UserEntity user = userService.getUserById(unitDto.userId());
        UnitEntity unit = unitMapper.toEntity(unitDto, user);
        UnitEntity savedUnit = unitRepository.save(unit);


        updateAvailableUnitsCacheSafely();
        return savedUnit;
    }

//    private void updateAvailableUnitsCacheSafely() {
//        redisTemplate.watch(AVAILABLE_UNITS_KEY);
//
//        long availableUnitsCount = getAvailableUnitsCount(null, null);
//
//        redisTemplate.multi();
//        redisTemplate.opsForValue().set(AVAILABLE_UNITS_KEY, String.valueOf(availableUnitsCount));
//        redisTemplate.exec();
//    }

    private void updateAvailableUnitsCacheSafely() {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            byte[] key = redisTemplate.getStringSerializer().serialize(AVAILABLE_UNITS_KEY);

            connection.watch(key); // Watch the key

            long availableUnitsCount = getAvailableUnitsCount(null, null);

            connection.multi(); // Start transaction
            connection.set(key, redisTemplate.getStringSerializer().serialize(String.valueOf(availableUnitsCount)));
            List<Object> execResult = connection.exec(); // Execute transaction

            if (execResult == null) {
                throw new RuntimeException("Redis transaction failed due to concurrent modification.");
            }

            return null;
        });
    }

    public void updateAvailability(int unitId) {
        updateAvailableUnitsCacheSafely();
    }

    public long getAvailableUnits() {
        String count = redisTemplate.opsForValue().get(AVAILABLE_UNITS_KEY);
        return count != null ? Long.parseLong(count) : 0;
    }

    private void cleanupOrphanedPayments(Integer unitId, Set<String> redisKeys) {
        List<Integer> paymentIds = redisKeys.isEmpty() ? null : redisKeys.stream().map(this::getPaymentId).toList();

        List<PaymentEntity> orphanedPayments = paymentRepository.findOrphanedPaymentsNotInIds(unitId, paymentIds);

        for (PaymentEntity payment : orphanedPayments) {
            payment.getEvent().setStatus(EventStatus.CANCELLED);
            eventRepository.save(payment.getEvent());

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    private Integer getPaymentId(String key) {
        return parseInt(key.split(":")[3]);
    }


}


