package com.edc.erp.returnorder.model.out;

import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author fxw
 * @description: 退货详情出参
 * @since 2022/12/27 19:14
 */
@Data
public class OrdDisReturnDetailOut extends OrdDisReturnDetail {

    /**
     * 退货原则
     */
    @ApiModelProperty(name = "returnPrinciple", value = "退货原则")
    private String returnPrinciple;

    /**
     * 门店库存
     */
    @ApiModelProperty(name = "storeInventory", value = "门店可用库存")
    private BigDecimal storeInventory;

    /**
     * 门店退货原因中位值
     */
    @ApiModelProperty(name = "returnReasonValue", value = "门店退货原因中位值")
    private String returnReasonValue;

    /**
     * 品类属性中文
     */
    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性中文")
    private String goodsTypeStr;

    @ApiModelProperty(name = "imageList", value = "退货图片集合")
    private List<String> imageList;

    @ApiModelProperty(value = "发票类型中文")
    private String invoiceTypeStr;

    @ApiModelProperty(value = "是否管理效期")
    private Integer isManageValidityPeriod;
}
