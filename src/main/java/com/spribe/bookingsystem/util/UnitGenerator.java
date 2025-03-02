package com.spribe.bookingsystem.util;

import com.spribe.bookingsystem.entity.AccommodationType;
import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.entity.UserEntity;
import com.spribe.bookingsystem.repository.UnitRepository;
import com.spribe.bookingsystem.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Random;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UnitGenerator {
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;

    private static final String[] UNIT_TYPES = {"HOME", "FLAT", "APARTMENTS"};
    private static final int NUM_UNITS_TO_INSERT = 90;

    @PostConstruct
    public void generateUnits() {
        if (unitRepository.count() >= 100) return;

        Random random = new Random();
        List<UserEntity> users = userRepository.findAll();
        if (users.isEmpty()) return;

        for (int i = 0; i < NUM_UNITS_TO_INSERT; i++) {
            UnitEntity unit = new UnitEntity();
            unit.setUser(users.get(random.nextInt(users.size())));
            unit.setNumRooms(random.nextInt(5) + 1); // 1 to 5 rooms
            unit.setType(AccommodationType.valueOf(UNIT_TYPES[random.nextInt(UNIT_TYPES.length)]));
            unit.setFloor(random.nextInt(10) + 1); // Floors 1 to 10
            unit.setDescription("Randomly generated unit #" + (i + 1));

            BigDecimal baseCost = BigDecimal.valueOf(random.nextInt(500) + 200); // Cost between 200-700
            unit.setBaseCost(baseCost);
            unit.setMarkup(BigDecimal.valueOf(0.15));
            unit.setTotalCost(baseCost.multiply(BigDecimal.valueOf(1.15)));

            unitRepository.save(unit);
        }

        System.out.println("✅ Inserted 90 random units after application start.");
    }
}
