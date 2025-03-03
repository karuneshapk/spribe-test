package com.spribe.bookingsystem.service;

import static java.lang.Integer.parseInt;
import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.exception.PaymentNotFoundException;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final EventRepository bookingRepository;
    private final CacheService cacheService;

    @Transactional
    public PaymentEntity initiatePayment(EventEntity event) {
        PaymentEntity payment = new PaymentEntity();
        payment.setEvent(event);
        payment.setUser(event.getUser());
        payment.setAmount(event.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);

        PaymentEntity savedPayment = paymentRepository.save(payment);

        cacheService.putEventToTheCacheWithExpirationTime(savedPayment.getEvent());

        return savedPayment;
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
        List<PaymentEntity> orphanedPayments = paymentRepository.findOrphanedPaymentsNotInIds(unitId, paymentIds);
        log.debug("Found {} orphaned payments in {}", orphanedPayments.size(), unitId);
        return orphanedPayments;
    }

    public boolean isUnitAvailable(Integer unitId, LocalDate startDate, LocalDate endDate) {
        return paymentRepository.isUnitAvailableForBooking(unitId, startDate, endDate);
    }

    @Transactional
    public void processPayment(int paymentId, boolean success) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

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

        cacheService.delete(keyPattern);
        paymentRepository.save(payment);
        bookingRepository.save(payment.getEvent());
    }


    private Integer getPaymentId(String key) {
        return parseInt(key.split(":")[3]);
    }

}
