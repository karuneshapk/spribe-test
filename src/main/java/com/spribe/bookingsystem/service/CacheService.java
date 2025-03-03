package com.spribe.bookingsystem.service;

import static com.spribe.bookingsystem.util.Constants.PAYMENT_EXPIRATION_TIME;
import static com.spribe.bookingsystem.util.Constants.PENDING_PAYMENTS_KEY;
import static com.spribe.bookingsystem.util.Constants.REDIS_UNITS_KEY;
import static java.lang.Integer.parseInt;
import com.spribe.bookingsystem.config.PaymentProperties;
import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import com.spribe.bookingsystem.repository.UnitRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private static final String AVAILABLE_UNITS_KEY = "available_units_count";

    private final StringRedisTemplate redisTemplate;
    private final PaymentProperties paymentProperties;
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final UnitRepository unitRepository;

    @Transactional
    public void updateAvailableUnitsCacheSafely() {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            byte[] key = redisTemplate.getStringSerializer().serialize(AVAILABLE_UNITS_KEY);

            connection.watch(key); // Watch the key

            Set<String> redisKeys = redisTemplate.keys(REDIS_UNITS_KEY + ":*");

            // Cleanup orphaned events & payments before processing new ones (in case of crashed application)
            cleanupOrphanedPayments(null, redisKeys);

            long availableUnitsCount = unitRepository.countAvailableUnits(null, null);

            connection.multi(); // Start transaction
            connection.set(key, redisTemplate.getStringSerializer().serialize(String.valueOf(availableUnitsCount)));
            List<Object> execResult = connection.exec(); // Execute transaction

            if (execResult == null) {
                throw new RuntimeException("Redis transaction failed due to concurrent modification.");
            }

            return null;
        });
    }

    public Set<String> getAvailabelKeys() {
        return redisTemplate.keys(REDIS_UNITS_KEY + ":*");
    }

    public void delete(String keyPattern) {
        redisTemplate.delete(keyPattern);
    }

    public Set<String> getKeysForUnitId(Integer unitId) {
        String keyPattern = REDIS_UNITS_KEY + ":" + unitId + ":" + PENDING_PAYMENTS_KEY + ":*";
        Set<String> redisKeys = redisTemplate.keys(keyPattern);

        log.debug("redisKeys count : {}", redisKeys.size());

        return redisKeys;
    };


    public Set<String> getKeysForUnitIdAndPaymentId(String keyPattern) {
        Set<String> redisKeys = redisTemplate.keys(keyPattern);

        log.debug("redisKeys count : {}", redisKeys.size());

        return redisKeys;
    }

    public String makeRedisKey(PaymentEntity payment) {
        String redisKey = REDIS_UNITS_KEY + ":" + payment.getEvent().getUnit().getId() + ":"
            + PENDING_PAYMENTS_KEY + ":" + payment.getId() + ":"
            + "startDate:" + payment.getEvent().getStartDate().toString() + ":"
            + "endDate:" + payment.getEvent().getEndDate().toString();

        log.debug("redisKey: {}", redisKey);

        return redisKey;
    }

    public void putEventToTheCacheWithExpirationTime(EventEntity eventEntity) {
        long expirationTime = paymentProperties.expirationTime();
        TimeUnit timeUnit = paymentProperties.timeUnit();

        String redisKey = makeRedisKey(eventEntity);
        redisTemplate.opsForValue().set(redisKey, "PENDING", expirationTime, timeUnit);

        // Schedule a callback to move expired payments to the expired list when TTL reaches 0
        redisTemplate.expire(redisKey, expirationTime, timeUnit);
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

    private String makeRedisKey(EventEntity eventEntity) {
        return REDIS_UNITS_KEY + ":" + eventEntity.getUnit().getId() + ":"
            + PENDING_PAYMENTS_KEY + ":" + eventEntity.getId() + ":"
            + "startDate:" + eventEntity.getStartDate().toString() + ":"
            + "endDate:" + eventEntity.getEndDate().toString();
    }

}
