package com.edc.erp.presale.model.out;

import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 预售调整单分页查询列表实体
 */
@Data
public class PresaleAdjustOrderPageOut extends OrdDisPresaleAdjustOrder {
    private static final long serialVersionUID = -3445885532945970181L;
    /**
     * 状态描述
     */
    @ApiModelProperty(name = "statusDesc", value = "状态描述")
    private String statusDesc;
    /**
     * 调整类型
     */
    @ApiModelProperty(name = "adjustTypeDesc", value = "调整类型")
    private String adjustTypeDesc;
    // @ApiModelProperty(value = "所属区域")
    // private String belongArea;
}
