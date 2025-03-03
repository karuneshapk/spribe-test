package com.spribe.bookingsystem.mapper;

import com.spribe.bookingsystem.entity.EventEntity;
import com.spribe.bookingsystem.payload.response.data.EventData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "userId", source = "event.user.id")
    @Mapping(target = "unitId", source = "event.unit.id")
    @Mapping(target = "paymentId", source = "paymentId")
    EventData toData(EventEntity event, Integer paymentId);
}
