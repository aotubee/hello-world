/**
 * com.edc.
 * Copyright (c) 2019-2020 All Rights Reserved.
 */
package com.edc.erp.disdifferenceorder.model.out;

import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PpDifferenceOrderOut返回类
 *
 * @author liwenqiang
 */
@Data
public class DisDifferenceOrderOut extends OrdDisDelivDifference implements Serializable {

    /**
     * 差异单状态中文
     */
    @ApiModelProperty(name = "differenceStatusName", value = "差异单状态中文")
    private String differenceStatusName;

    /**
     * 仓位名称
     */
    @ApiModelProperty(name = "positionName", value = "仓位名称")
    private String positionName;


    @ApiModelProperty(value = "差异明细集合")
    private List<OrdDisDelivDifferenceDetailOut> differenceOrderDetailList;

    /** 配销差异类型中文 */
    @ApiModelProperty(name = "differenceTypeName", value = "配销差异类型中文")
    private String differenceTypeName;

    /**
     * 商品品项数
     */
    @ApiModelProperty(name = "goodsSize", value = "商品品项数")
    private Integer goodsSize;

    /** 仓储名称 */
    @ApiModelProperty(name = "wrhName", value = "仓储名称")
    private String wrhName;

}
