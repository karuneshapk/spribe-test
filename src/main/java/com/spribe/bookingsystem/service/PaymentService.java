package com.spribe.bookingsystem.service;

import static com.spribe.bookingsystem.util.Constants.PAYMENT_EXPIRATION_TIME;
import static com.spribe.bookingsystem.util.Constants.PENDING_PAYMENTS_KEY;
import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
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

        String redisKey = PENDING_PAYMENTS_KEY
            + ":" + savedPayment.getId()
//            + "_unit:"
//            + savedPayment.getEvent().getUnit().getId()
            ;

        // Store payment in Redis with a 15-minute TTL
        redisTemplate.opsForValue().set(redisKey, "PENDING", PAYMENT_EXPIRATION_TIME, TimeUnit.MINUTES);

        // Schedule a callback to move expired payments to the expired list when TTL reaches 0
        redisTemplate.expire(redisKey, PAYMENT_EXPIRATION_TIME, TimeUnit.MINUTES);
    }

    @Transactional
    public void processPayment(int paymentId, boolean success) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (success) {
            payment.setStatus(PaymentStatus.PAID);
            payment.getEvent().setStatus(EventStatus.CONFIRMED);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.getEvent().setStatus(EventStatus.CANCELLED);
        }

        redisTemplate.delete(PENDING_PAYMENTS_KEY + ":" + paymentId);
        paymentRepository.save(payment);
        bookingRepository.save(payment.getEvent());
    }

}
