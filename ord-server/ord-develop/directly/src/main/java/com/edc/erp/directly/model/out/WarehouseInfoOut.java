package com.edc.erp.directly.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lx
 * @since 2022-10-21 19:36:30
 */
@Data
public class WarehouseInfoOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 仓储ID */
    @ApiModelProperty(name = "id", value = "仓储ID")
    private Long id;

    /** 仓储代码 */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    /** 仓储名称 */
    @ApiModelProperty(name = "warehouseName", value = "仓储名称")
    private String warehouseName;

//    /** 仓储代码 + 仓储名称 */
//    @ApiModelProperty(name = "warehouseCodeStr",value = "仓储代码 + 仓储名称")
//    private String warehouseCodeStr;

    /** 业务组织 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织")
    private String bizOrgCode;

    @ApiModelProperty(name = "stockInfoOut",value = "仓位集合")
    private List<StockInfoOut> stockInfoOuts;
}
