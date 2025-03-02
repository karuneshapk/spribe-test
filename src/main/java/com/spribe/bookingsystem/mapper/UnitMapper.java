package com.spribe.bookingsystem.mapper;

import com.spribe.bookingsystem.entity.UnitEntity;
import com.spribe.bookingsystem.payload.response.UnitData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UnitMapper {
    UnitMapper INSTANCE = Mappers.getMapper(UnitMapper.class);

    @Mapping(target = "numRooms", source = "numRooms")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "floor", source = "floor")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "totalCost", source = "totalCost")
    UnitData toUnitData(UnitEntity unitEntity);
}
