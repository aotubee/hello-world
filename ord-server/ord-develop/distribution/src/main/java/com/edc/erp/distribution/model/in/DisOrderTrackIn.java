package com.edc.erp.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author fxw
 * @description: 订单追踪入参
 * @since 2022/10/18 14:50
 */
@Data
public class DisOrderTrackIn implements Serializable {
    private static final long serialVersionUID = 1691781146053557122L;

    /**
     * 单号
     */
    @ApiModelProperty(name = "orderNo", value = "单号")
    @NotEmpty
    private String orderNo;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    @NotEmpty(message = "门店代码不能为空")
    private String storeCode;

    /**
     * 业务类型
     */
    @ApiModelProperty(name = "businessType", value = "业务类型")
    @NotEmpty(message = "业务类型不能为空")
    private String businessType;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
}
