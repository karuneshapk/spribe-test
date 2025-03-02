package com.spribe.bookingsystem.mapper;

import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.payload.response.data.UnitData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface UnitMapper {
    UnitMapper INSTANCE = Mappers.getMapper(UnitMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "numRooms", source = "numRooms")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "floor", source = "floor")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "totalCost", source = "totalCost")
    UnitData toUnitData(UnitEntity unitEntity);
}
