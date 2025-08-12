package com.edc.erp.common.model.in.store;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 批量查询仓储库存分配配置入参明细
 *
 * @author zy
 */
@Data
public class QueryEquipmentStockAlloDetailIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

}
