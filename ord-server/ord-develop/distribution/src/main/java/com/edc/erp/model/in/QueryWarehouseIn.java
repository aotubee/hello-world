package com.edc.erp.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lx
 * @since 2022-10-21 16:53:56
 */
@Data
public class QueryWarehouseIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务组织 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织")
    private String bizOrgCode;

    /** 是否启用 */
    @ApiModelProperty(name = "isEnable", value = "是否启用")
    private Integer isEnable;

    /** 批发单类型(out：出货，returns：退货) */
    @ApiModelProperty(name = "wholesaleOrderType", value = "批发单类型(out：出货，returns：退货)")
    private String wholesaleOrderType ;

    /** 允许(出货和退货)条件字段 */
    @ApiModelProperty(name = "isOutReturn", value = "允许(出货和退货)条件字段")
    private String isOutReturn;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode",value = "仓位代码")
    private String stockCode;
}
