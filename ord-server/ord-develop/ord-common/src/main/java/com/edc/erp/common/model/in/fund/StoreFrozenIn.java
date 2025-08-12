package com.edc.erp.common.model.in.fund;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName StoreFrozenIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/7/20 11:10
 **/
@Data
public class StoreFrozenIn implements Serializable {
    private static final long serialVersionUID = 8873235542034939629L;

    @ApiModelProperty(name = "principalCode", value = "主体编码")
    private String principalCode;

    @ApiModelProperty(name = "principalType", value = "主体类型")
    private String principalType;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "frozenOrders", value = "冻结单对象集合")
    private List<FrozenOrderIn> frozenOrders;
}
