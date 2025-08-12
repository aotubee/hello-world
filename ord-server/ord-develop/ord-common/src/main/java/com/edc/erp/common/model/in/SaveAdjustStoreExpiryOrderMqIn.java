package com.edc.erp.common.model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @ClassName SaveAdjustStoreExpiryOrderIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/7/26 15:49
 **/
@Data
@ApiModel(value = "SaveAdjustStoreExpiryOrderMqIn", description = "门店调整单保存MQ入参")
public class SaveAdjustStoreExpiryOrderMqIn implements Serializable {
    private static final long serialVersionUID = -3391963726135642616L;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    @ApiModelProperty(name = "adjustStoreExpiryMqInList", value = "明细")
    @NotNull(message = "请填写明细信息")
    List<AdjustStoreExpiryMqIn> adjustStoreExpiryMqInList;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "orgCode", value = "业务组织代码")
    private String orgCode;

    @ApiModelProperty(name = "loginUsername", value = "操作人")
    private String loginUsername;
}
