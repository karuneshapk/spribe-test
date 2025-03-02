package com.spribe.bookingsystem.service;

import static com.spribe.bookingsystem.util.Constants.PAYMENT_EXPIRATION_TIME;
import static com.spribe.bookingsystem.util.Constants.PENDING_PAYMENTS_KEY;
import static com.spribe.bookingsystem.util.Constants.REDIS_UNITS_KEY;
import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
    private final PaymentRepository paymentRepository;
    private final EventRepository bookingRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void initiatePayment(EventEntity event) {
        PaymentEntity payment = new PaymentEntity();
        payment.setEvent(event);
        payment.setUser(event.getUser());
        payment.setAmount(event.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);

        PaymentEntity savedPayment = paymentRepository.save(payment);

        String redisKey = makeRedisKey(savedPayment, event.getStartDate(), event.getEndDate());

        // Store payment in Redis with a 15-minute TTL
        redisTemplate.opsForValue().set(redisKey, "PENDING", PAYMENT_EXPIRATION_TIME, TimeUnit.MINUTES);

        // Schedule a callback to move expired payments to the expired list when TTL reaches 0
        redisTemplate.expire(redisKey, PAYMENT_EXPIRATION_TIME, TimeUnit.MINUTES);
    }

    @Transactional
    public void processPayment(int paymentId, boolean success) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

        String redisKey = makeRedisKey(payment, payment.getEvent().getStartDate(), payment.getEvent().getEndDate());
        Set<String> redisKeys = redisTemplate.keys(redisKey);

        if (CollectionUtils.isNotEmpty(redisKeys)) {
            if (success) {
                payment.setStatus(PaymentStatus.PAID);
                payment.getEvent().setStatus(EventStatus.CONFIRMED);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.getEvent().setStatus(EventStatus.CANCELLED);
            }
        }

        redisTemplate.delete(redisKey);
        paymentRepository.save(payment);
        bookingRepository.save(payment.getEvent());
    }

    private String makeRedisKey(PaymentEntity payment, LocalDate startDate, LocalDate endDate) {
        String redisKey = REDIS_UNITS_KEY + ":" + payment.getEvent().getUnit().getId() + ":"
            + PENDING_PAYMENTS_KEY + ":" + payment.getId() + ":"
            + "startDate:" + startDate.toString() + ":"
            + "endDate:" + endDate.toString();

        log.debug("redisKey: {}", redisKey);

        return redisKey;
    }

}
