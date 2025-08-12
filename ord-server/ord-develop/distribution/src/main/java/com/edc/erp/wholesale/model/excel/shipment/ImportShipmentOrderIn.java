package com.edc.erp.wholesale.model.excel.shipment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 批发出货单批量导入 入参类
 * @author lx
 * @since 2022-11-03 18:06:37
 */
@Data
public class ImportShipmentOrderIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件id */
    @ApiModelProperty(name = "fileId",value = "文件id")
    private String fileId;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode",value = "客户代码")
    private String clientCode;

    /** 仓位id */
    @ApiModelProperty(name = "stockId",value = "仓位id")
    private Integer stockId;

    /** 仓储代码 */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    /** 仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode",value = "业务组织代码")
    private String bizOrgCode;
}
