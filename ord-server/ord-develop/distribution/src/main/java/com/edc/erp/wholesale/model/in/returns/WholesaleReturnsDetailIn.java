package com.edc.erp.wholesale.model.in.returns;

import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wld
 */
@Data
public class WholesaleReturnsDetailIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批发退货单
     */
    @ApiModelProperty(name = "wholesaleReturns", value = "批发退货单")
    private WholesaleReturns wholesaleReturns;

    /**
     * 批发退货单明细
     */
    @ApiModelProperty(name = "wholesaleReturnDetailList", value = "批发退货单明细")
    private List<WholesaleReturnDetail> wholesaleReturnDetailList;

    /**
     * 仓位id
     */
    @ApiModelProperty(name = "stockId", value = "仓位id")
    private Integer stockId;

    /**
     * 仓储代码
     */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 仓储名称
     */
    @ApiModelProperty(name = "warehouseName", value = "仓储名称")
    private String warehouseName;

    /**
     * 仓位名称
     */
    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;
}