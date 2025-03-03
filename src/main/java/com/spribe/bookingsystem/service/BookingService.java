package com.spribe.bookingsystem.service;

import static java.lang.Integer.parseInt;
import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.exception.UnitAlreadyBookedException;
import com.spribe.bookingsystem.mapper.EventMapper;
import com.spribe.bookingsystem.payload.response.data.EventData;
import com.spribe.bookingsystem.repository.EventRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final EventRepository eventRepository;
    private final PaymentService paymentService;
    private final CacheService cacheService;
    private final EventMapper eventMapper;
    private final UserService userService;
    private final UnitService unitService;

    @Transactional
    public EventData bookUnit(int userId, int unitId, LocalDate startDate, LocalDate endDate) {
        log.debug("Booking unit: {} for user: {} - startDate: {}, endDate: {}", unitId, userId, startDate, endDate);
        checkCache(unitId, startDate, endDate);
        EventData data = checkDbAndPersistUnit(userId, unitId, startDate, endDate);

        return data;
    }

    private void checkCache(Integer unitId, LocalDate startDate, LocalDate endDate) {
        log.debug("Check cache for unit: {}", unitId);
        Set<String> redisKeys = cacheService.getKeysForUnitId(unitId);

        // Cleanup orphaned events & payments before processing new ones (in case of crashed application)
        cleanupOrphanedPayments(unitId, redisKeys);

        // Check cache if unit already has PENDING status for date range
        if (CollectionUtils.isNotEmpty(redisKeys)) {
            for (String redisKey : redisKeys) {
                if (isDateRangeOverlapping(redisKey, startDate, endDate)) {
                    log.error("Unit {} is already booked for overlapping dates (wait ~15 minutes and check again)", unitId);
                    throw new UnitAlreadyBookedException("Unit is already booked for overlapping dates (wait ~15 minutes and check again)");
                }
            }
        }
    }

    private EventData checkDbAndPersistUnit(int userId, int unitId, LocalDate startDate, LocalDate endDate) {
        // Check DB if unit already has CONFIRMED status for current date range
        boolean isAvailable = paymentService.isUnitAvailable(unitId, startDate, endDate);

        if (!isAvailable) {
            log.error("Unit {} is already booked for overlapping dates: startDate: {} - endDate {}", unitId, startDate.toString(), endDate.toString());
            throw new UnitAlreadyBookedException("Unit is already booked for overlapping dates");
        }

        EventEntity eventEntity = persistEvent(userId, unitId, startDate, endDate);

        PaymentEntity paymentEntity = paymentService.initiatePayment(eventEntity);
        EventData data = eventMapper.toData(eventEntity, paymentEntity.getId());
        return data;
    }

    /**
     * Cleanup orphaned events & payments (DB contains `PENDING/PAID` but Redis doesn't)
     */
    private void cleanupOrphanedPayments(int unitId, Set<String> redisKeys) {
        List<Integer> paymentIds = redisKeys.isEmpty() ? null : redisKeys.stream().map(this::getPaymentId).toList();

        List<PaymentEntity> orphanedPayments = paymentService.findOrphanedPayments(unitId, paymentIds);

        for (PaymentEntity payment : orphanedPayments) {
            payment.getEvent().setStatus(EventStatus.CANCELLED);
            eventRepository.save(payment.getEvent());

            payment.setStatus(PaymentStatus.FAILED);
            paymentService.save(payment);
        }

        if (CollectionUtils.isNotEmpty(orphanedPayments)) {
            log.debug("Cleanup orphaned payments: {}", orphanedPayments.size());
        }
    }

    private EventEntity persistEvent(Integer userId, Integer unitId, LocalDate startDate, LocalDate endDate) {
        UserEntity user = userService.getUserById(userId);
        UnitEntity unit = unitService.findById(unitId);

        EventEntity eventEntity = EventEntity.builder()
            .user(user)
            .unit(unit)
            .startDate(startDate)
            .endDate(endDate)
            .totalPrice(unit.getTotalCost())
            .status(EventStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        eventEntity = eventRepository.save(eventEntity);

        log.debug("Persisted event {}", eventEntity);
        return eventEntity;
    }

    private Integer getPaymentId(String key) {
        return parseInt(key.split(":")[3]);
    }

    /**
     * Check if booking dates overlap with a Redis-stored event.
     */
    private boolean isDateRangeOverlapping(String redisKey, LocalDate startDate, LocalDate endDate) {
        String[] parts = redisKey.split(":startDate:|:endDate:");

        if (parts.length < 3) return false;

        LocalDate existingStartDate = LocalDate.parse(parts[1]);
        LocalDate existingEndDate = LocalDate.parse(parts[2]);

        return (startDate.isBefore(existingEndDate) && endDate.isAfter(existingStartDate));
    }

}
