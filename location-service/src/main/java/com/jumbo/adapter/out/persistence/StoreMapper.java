package com.jumbo.adapter.out.persistence;

import com.jumbo.application.domain.model.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(target = "todayOpen", source = "todayOpen", qualifiedByName = "stringToLocalTime")
    @Mapping(target = "todayClose", source = "todayClose", qualifiedByName = "stringToLocalTime")
    Store toDomain(StoreEntity entity);

    List<Store> toDomainList(List<StoreEntity> entities);

    @Named("stringToLocalTime")
    default LocalTime stringToLocalTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = timeString.trim().split(":");
            if (parts.length != 2) {
                return null;
            }

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            return LocalTime.of(hour, minute);
        } catch (DateTimeParseException | NumberFormatException e) {
            return null; // Return null for invalid time formats
        }
    }
}