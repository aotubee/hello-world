package com.edc.erp.directly.model.in;

import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 发送创建要货单前MQ消息入参对象
 * @since 2022/10/18 20:13
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

    @ApiModelProperty(name = "addPushFlag",value = "是否加推")
    private Boolean addPushFlag;

    @ApiModelProperty(name = "auditType", value = "审核类型")
    private String auditType;
}
