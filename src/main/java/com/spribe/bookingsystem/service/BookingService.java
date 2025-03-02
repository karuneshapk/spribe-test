package com.spribe.bookingsystem.service;


import static com.spribe.bookingsystem.util.Constants.PENDING_PAYMENTS_KEY;
import static com.spribe.bookingsystem.util.Constants.REDIS_UNITS_KEY;
import static java.lang.Integer.parseInt;
import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.repository.EventRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final StringRedisTemplate redisTemplate;
    private final EventRepository eventRepository;
    private final PaymentService paymentService;
    private final UserService userService;
    private final UnitService unitService;

    @Transactional
    public EventEntity bookUnit(int userId, int unitId, LocalDate startDate, LocalDate endDate) {
        UserEntity user = userService.getUserById(userId);
        UnitEntity unit = unitService.findById(unitId);

        // Step 1: Check if unit is in Redis (already booked)
        String keyPattern = REDIS_UNITS_KEY + ":" + unitId + ":" + PENDING_PAYMENTS_KEY + ":*";
        Set<String> redisKeys = redisTemplate.keys(keyPattern);

        // Step 2: Cleanup orphaned events & payments before processing new ones (in case of crashed application)
        cleanupOrphanedPayments(unitId, redisKeys);

        // Step 3: Check cache if unit already has PENDING status for date range
        if (CollectionUtils.isNotEmpty(redisKeys)) {
            for (String redisKey : redisKeys) {
                if (isDateRangeOverlapping(redisKey, startDate, endDate)) {
                    throw new RuntimeException("Unit is already booked for overlapping dates (wait ~15 minutes and check again)");
                }
            }
        }

        // Step 4: Check DB if unit already has CONFIRMED status for current date range
        boolean isAvailable = paymentService.isUnitAvailable(unitId, startDate, endDate);

        if (!isAvailable) {
            throw new RuntimeException("Unit is already booked for overlapping dates");
        }

        // Step 5: Proceed with booking since no conflicts were found
        EventEntity booking = new EventEntity();
        booking.setUser(user);
        booking.setUnit(unit);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setTotalPrice(unit.getTotalCost());
        booking.setStatus(EventStatus.PENDING);

        booking = eventRepository.save(booking);

        // Step 5: Store booking in Redis with 15-minute expiration
        String redisKey = makeRedisKey(booking, startDate, endDate);
        redisTemplate.opsForValue().set(redisKey, "PENDING", 15, TimeUnit.MINUTES);

        paymentService.initiatePayment(booking);
        return booking;
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

    private String makeRedisKey(EventEntity booking, LocalDate startDate, LocalDate endDate) {
        return REDIS_UNITS_KEY + ":" + booking.getUnit().getId() + ":"
            + PENDING_PAYMENTS_KEY + ":" + booking.getId() + ":"
            + "startDate:" + startDate.toString() + ":"
            + "endDate:" + endDate.toString();
    }

}


