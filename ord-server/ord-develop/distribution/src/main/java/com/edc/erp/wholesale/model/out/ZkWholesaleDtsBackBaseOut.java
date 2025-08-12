package com.edc.erp.wholesale.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName ZkWholesaleDtsBackBaseOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/12 10:47
 **/
@Data
public class ZkWholesaleDtsBackBaseOut implements Serializable {
    private static final long serialVersionUID = -5222715605871663574L;

    @ApiModelProperty(name = "message", value = "处理信息")
    private String message;

    @ApiModelProperty(name = "isSendZk", value = "是否发送中科")
    private Integer isSendZk;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织")
    private String bizOrgCode;
}
