package com.spribe.bookingsystem.service;

import static com.spribe.bookingsystem.util.Constants.PAYMENT_EXPIRATION_TIME;
import static com.spribe.bookingsystem.util.Constants.PENDING_PAYMENTS_KEY;
import static com.spribe.bookingsystem.util.Constants.REDIS_UNITS_KEY;
import static java.lang.Integer.parseInt;
import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final StringRedisTemplate redisTemplate;
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final EventRepository bookingRepository;
    private final CacheService cacheService;

    @Transactional
    public void initiatePayment(EventEntity event) {
        PaymentEntity payment = new PaymentEntity();
        payment.setEvent(event);
        payment.setUser(event.getUser());
        payment.setAmount(event.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);

        PaymentEntity savedPayment = paymentRepository.save(payment);

        // Store payment in Redis with a 15-minute TTL
        cacheService.putEventToTheCacheWithExpirationTime(payment.getEvent());
    }

    public void cleanupOrphanedPayments(Integer unitId, Set<String> redisKeys) {
        List<Integer> paymentIds = redisKeys.isEmpty() ? null : redisKeys.stream().map(this::getPaymentId).toList();

        List<PaymentEntity> orphanedPayments = paymentRepository.findOrphanedPaymentsNotInIds(unitId, paymentIds);

        for (PaymentEntity payment : orphanedPayments) {
            payment.getEvent().setStatus(EventStatus.CANCELLED);
            eventRepository.save(payment.getEvent());

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    public PaymentEntity save(PaymentEntity payment) {
        return paymentRepository.save(payment);
    }

    public List<PaymentEntity> findOrphanedPayments(Integer unitId, List<Integer> paymentIds) {
        return paymentRepository.findOrphanedPaymentsNotInIds(unitId, paymentIds);
    }

    public boolean isUnitAvailable(Integer unitId, LocalDate startDate, LocalDate endDate) {
        return paymentRepository.isUnitAvailableForBooking(unitId, startDate, endDate);
    }

    @Transactional
    public void processPayment(int paymentId, boolean success) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

        String keyPattern = cacheService.makeRedisKey(payment);
        Set<String> redisKeys = cacheService.getKeysForUnitIdAndPaymentId(keyPattern);

        if (CollectionUtils.isNotEmpty(redisKeys)) {
            if (success) {
                payment.setStatus(PaymentStatus.PAID);
                payment.getEvent().setStatus(EventStatus.CONFIRMED);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.getEvent().setStatus(EventStatus.CANCELLED);
            }
        }

        redisTemplate.delete(keyPattern);
        paymentRepository.save(payment);
        bookingRepository.save(payment.getEvent());
    }


    private Integer getPaymentId(String key) {
        return parseInt(key.split(":")[3]);
    }

}
