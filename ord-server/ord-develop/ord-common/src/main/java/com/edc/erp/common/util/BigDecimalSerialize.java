package com.edc.erp.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * BigDecimal保留两位小数
 * @author lx
 * @date 2023-11-34 11:34:49
 */
public class BigDecimalSerialize extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        if (Objects.nonNull(value)) {
            gen.writeString(value.setScale(0)+"");
        } else {
            gen.writeString("");
        }
    }
}