package com.edc.erp.common.model.out.warehouse;

import com.edc.erp.common.model.out.store.StockInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 仓储出参类
 * @author lee
 */
@Data
public class WarehouseInfoOut extends WarehouseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "物流商代码")
    private String logisticsProviderCode;

    @ApiModelProperty(value = "物流商名称")
    private String logisticsProviderName;

    @ApiModelProperty(value = "配送中心代码")
    private String dcCode;

    @ApiModelProperty(value = "配送中心名称")
    private String dcName;

    @ApiModelProperty(value = "仓位集合")
    private List<StockInfo> stockInfoList;
}
