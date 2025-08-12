package com.edc.erp.presale.model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName SubmitPresaleOrderIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/29 15:08
 **/
@Data
@ApiModel(value = "SubmitPresaleOrderIn", description = "提交预售订单入参")
public class SubmitPresaleOrderIn implements Serializable {
    private static final long serialVersionUID = 967236739858237157L;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织")
    private String bizOrgCode;

    @ApiModelProperty(name = "orgCode", value = "组织代码")
    private String orgCode;

    @ApiModelProperty(name = "loginUsername", value = "登录人")
    private String loginUsername;

    @ApiModelProperty(name = "presaleOrderActivityInList", value = "预售活动集合")
    private List<SubmitPresaleOrderActivityIn> presaleOrderActivityInList;

}
