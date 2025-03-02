package com.spribe.bookingsystem.service;


import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.UnitRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final UnitRepository unitRepository;
    private final PaymentService paymentService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public EventEntity bookUnit(int userId, int unitId, LocalDate startDate, LocalDate endDate) {
        UserEntity user = userService.getUserById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));



//        List<String> expiredPayments = redisTemplate.opsForList().range(EXPIRED_PAYMENTS_LIST, 0, -1);
//        if (!CollectionUtils.isEmpty(expiredPayments)) {
//            for (String paymentIdStr : expiredPayments) {
//                int paymentId = Integer.parseInt(paymentIdStr);
//
//                PaymentEntity payment = paymentRepository.findById(paymentId).orElse(null);
//                if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
//                    payment.setStatus(PaymentStatus.FAILED);
//                    paymentRepository.save(payment);
//                }
//            }
//            // Remove processed payments from Redis
//            redisTemplate.delete(EXPIRED_PAYMENTS_LIST);
//        }



        Optional<UnitEntity> unit = unitRepository.findById(unitId);
        if (unit.isEmpty()) {
            throw new RuntimeException("Unit not found");
        }
        if (!isAvailable(unitId, startDate, endDate)) {
            throw new RuntimeException("Unit is not available for selected dates");
        }

        EventEntity booking = new EventEntity();
        booking.setUser(user);
        booking.setUnit(unit.get());
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setTotalPrice(unit.get().getTotalCost());
        booking.setStatus(EventStatus.PENDING);

        booking = eventRepository.save(booking);

        paymentService.initiatePayment(booking);
        return booking;
    }

    public boolean isAvailable(int unitId, LocalDate startDate, LocalDate endDate) {
        List<EventEntity> events = eventRepository.findByUnitIdAndStatus(unitId, EventStatus.CONFIRMED);
        return events.stream().noneMatch(b -> !(endDate.isBefore(b.getStartDate()) || startDate.isAfter(b.getEndDate())));
    }
}


