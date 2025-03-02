package com.spribe.bookingsystem.listener;

import static com.spribe.bookingsystem.util.Constants.PENDING_PAYMENTS_KEY;
import com.spribe.bookingsystem.entity.EventStatus;
import com.spribe.bookingsystem.entity.PaymentEntity;
import com.spribe.bookingsystem.entity.PaymentStatus;
import com.spribe.bookingsystem.repository.EventRepository;
import com.spribe.bookingsystem.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisExpirationListener implements MessageListener {
    private final PaymentRepository paymentRepository;
    private final EventRepository bookingRepository;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (expiredKey.contains(PENDING_PAYMENTS_KEY + ":")) {
            String paymentIdStr = expiredKey.split(":")[3];
            int paymentId = Integer.parseInt(paymentIdStr);

            // Fetch payment from DB and mark it as FAILED
            PaymentEntity payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.getEvent().setStatus(EventStatus.CANCELLED);

                paymentRepository.save(payment);
                bookingRepository.save(payment.getEvent());
            }
        }
    }
}
