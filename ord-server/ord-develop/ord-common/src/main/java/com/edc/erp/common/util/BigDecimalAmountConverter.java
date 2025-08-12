package com.edc.erp.common.util;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @author fxw
 * @description: bigDecimal金额小数点保留两位格式化
 * @since 2023/3/24 9:42
 */
public class BigDecimalAmountConverter implements Converter<BigDecimal> {

    @Override
    public Class<BigDecimal> supportJavaTypeKey() {
        return BigDecimal.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public BigDecimal convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty,
                                        GlobalConfiguration globalConfiguration) {
        return new BigDecimal(cellData.getStringValue());
    }

    @Override
    public WriteCellData<String> convertToExcelData(BigDecimal value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (Objects.nonNull(value)) {
            return new WriteCellData<>(value.setScale(2, RoundingMode.HALF_UP) + "");
        } else {
            return new WriteCellData<>("");
        }
    }
}
