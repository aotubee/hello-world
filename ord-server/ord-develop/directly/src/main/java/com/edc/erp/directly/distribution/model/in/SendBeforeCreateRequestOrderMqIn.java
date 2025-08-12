package com.edc.erp.directly.distribution.model.in;

import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发送创建要货单前MQ消息入参对象
 *
 * @author wanglidong
 * @since 2022/11/16 17:26
 */
@Data
public class SendBeforeCreateRequestOrderMqIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "orderList", value = "待推单订货单集合")
    private List<OrdDirOrder> orderList;

    @ApiModelProperty(name = "orderCycleId", value = "订货周期id")
    private Integer orderCycleId;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "loginUsername", value = "操作人")
    private String loginUsername;
}