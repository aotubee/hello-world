package com.edc.erp.wholesale.model.out.returns;

import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:批发退货单列表出参
 * @author wld
 * @since 2022/10/26 18:25
 */
@Data
public class WholesaleReturnsListOut extends WholesaleReturns implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户信息
     */
    @ApiModelProperty(name = "clientMessage", value = "客户信息")
    private String clientMessage;

    /**
     * 客户名称
     */
    @ApiModelProperty(name = "clientName", value = "客户名称")
    private String clientName;

    /**
     * 批发价格组
     */
    @ApiModelProperty(name = "priceGroup", value = "批发价格组")
    private String priceGroup;

    /**
     * 批发价格组名称
     */
    @ApiModelProperty(name = "priceGroupName", value = "批发价格组名称")
    private String priceGroupName;

    /**
     * 退货单状态中文
     */
    @ApiModelProperty(name = "returnStatusStr", value = "退货单状态中文")
    private String returnStatusStr;

    /**
     * 入库仓储中文
     */
    @ApiModelProperty(name = "storageWrh", value = "入库仓储")
    private String storageWrhStr;

    /**
     * 入库仓位中文
     */
    @ApiModelProperty(name = "storageStockCode", value = "入库仓位")
    private String storageStockCodeStr;
}
