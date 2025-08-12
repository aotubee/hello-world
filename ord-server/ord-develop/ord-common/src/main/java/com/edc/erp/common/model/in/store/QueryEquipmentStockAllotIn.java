package com.edc.erp.common.model.in.store;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量查询仓储库存分配配置入参
 *
 * @author zy
 */
@Data
public class QueryEquipmentStockAllotIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "stockAlloDetailInList", value = "批量查询仓储库存分配配置入参明细集合")
    private List<QueryEquipmentStockAlloDetailIn> stockAlloDetailInList;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
}
