package com.edc.erp.presale.model.out;


import com.edc.erp.presale.entity.OrdDisPresaleGoodsFlow;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 预售商品流水分页查询列表实体
 */
@Data
public class PresaleGoodsFlowPageOut extends OrdDisPresaleGoodsFlow {
    private static final long serialVersionUID = -6133605966893945399L;
    /**
     * 业务类型描述
     */
    @ApiModelProperty(name = "businessTypeDesc", value = "业务类型描述")
    private String businessTypeDesc;

    // @ApiModelProperty(value = "所属区域")
    // private String belongArea;
}
