package com.fourspicehotpot.backend.common.converter;

import com.fourspicehotpot.backend.common.enums.LoaiThanhToan;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LoaiThanhToanConverter implements AttributeConverter<LoaiThanhToan, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LoaiThanhToan attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public LoaiThanhToan convertToEntityAttribute(Integer dbData) {
        return dbData != null ? LoaiThanhToan.fromValue(dbData) : null;
    }
}
