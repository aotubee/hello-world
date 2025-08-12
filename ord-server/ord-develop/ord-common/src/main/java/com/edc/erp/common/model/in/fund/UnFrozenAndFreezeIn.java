package com.edc.erp.common.model.in.fund;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName UnfreezeAndFreezeIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/7/19 17:30
 **/
@Data
public class UnFrozenAndFreezeIn implements Serializable {

    @ApiModelProperty(name = "principalCode", value = "主体编码")
    private String principalCode;

    @ApiModelProperty(name = "principalType", value = "主体类型")
    private String principalType;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "unFrozenBusinessNos", value = "解冻单号集合")
    private List<String> unFrozenBusinessNos;

    @ApiModelProperty(name = "frozenOrders", value = "冻结单对象集合")
    private List<FrozenOrderIn> frozenOrders;
}
