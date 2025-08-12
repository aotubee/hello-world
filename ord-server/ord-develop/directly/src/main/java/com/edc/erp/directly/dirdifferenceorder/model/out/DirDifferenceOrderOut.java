/**
 * com.edc.
 * Copyright (c) 2019-2020 All Rights Reserved.
 */
package com.edc.erp.directly.dirdifferenceorder.model.out;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifferenceDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * PpDifferenceOrderOut返回类
 *
 * @author liwenqiang
 */
@Data
public class DirDifferenceOrderOut extends OrdDirDelivDifference implements Serializable {

    /**
     * 差异单状态中文
     */
    @ApiModelProperty(name = "differenceStatusName", value = "差异单状态中文")
    private String differenceStatusName;

    /**
     * 仓位名称
     */
    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;


    @ApiModelProperty(value = "差异明细集合")
    private List<OrdDirDelivDifferenceDetailOut> differenceOrderDetailList;

    /** 差异类型中文 */
    @ApiModelProperty(name = "differenceTypeName", value = "直营配货差异类型中文")
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
