package com.edc.erp.disrequestorder.model.out;

import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description
 *
 * @author weichao

 */
@Data
public class BackRequestOrderDetailOut extends OrdDisDelivRequestDetail {

    @ApiModelProperty(name = "sortName", value = "分类名称")
    private String sortName;

    @ApiModelProperty(name = "isReturn", value = "是否可退")
    private Integer isReturn;

    @ApiModelProperty(name = "isReturn", value = "集货单价")
    private BigDecimal price;

    @ApiModelProperty(name = "positionName", value = "仓位名称")
    private String positionName;

    /** 配销方式 */
    @ApiModelProperty(name = "distributionType", value = "配销方式")
    private String distributionTypeValue;

    /** 品类属性 */
    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性")
    private String goodsTypeStr;
}
