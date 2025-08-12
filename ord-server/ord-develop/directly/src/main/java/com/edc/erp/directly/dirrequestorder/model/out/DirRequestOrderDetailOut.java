package com.edc.erp.directly.dirrequestorder.model.out;

import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description
 *
 * @author weichao

 */
@Data
public class DirRequestOrderDetailOut extends OrdDirDelivRequestDetail {

    @ApiModelProperty(name = "sortName", value = "分类名称")
    private String sortName;

    @ApiModelProperty(name = "isReturn", value = "是否可退")
    private Integer isReturn;

    @ApiModelProperty(name = "isReturn", value = "要货单价")
    private BigDecimal price;

    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;

    /** 品类属性 */
    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性")
    private String goodsTypeStr;

    @ApiModelProperty(name = "distributionTypeValue", value = "配送方式中位值")
    private String distributionTypeValue;
}
