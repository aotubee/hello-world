package com.edc.erp.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lx
 * @since 2022-10-21 18:10:16
 */
@Data
public class StockInfoOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 仓位ID */
    @ApiModelProperty(name = "stockId", value = "仓位Id")
    private Long stockId;

    /** 仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /** 仓位名称 */
    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;


//    /** 仓位代码 + 仓位名称 */
//    @ApiModelProperty(name = "stockCodeStr",value = "仓位代码 + 仓位名称")
//    private String stockCodeStr;

    /** 仓储ID */
    @ApiModelProperty(name = "warehouseId", value = "仓储ID")
    private Long warehouseId;

    /** 业务组织 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织")
    private String bizOrgCode;
}
