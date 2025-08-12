package com.edc.erp.wholesale.model.in.shipment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName BackToHsBaseInfo
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/15 14:38
 **/
@Data
public class BackToHsBaseInfo implements Serializable {
    private static final long serialVersionUID = 7853164065547020632L;

    @ApiModelProperty(name = "erpOrderNo",value = "erp单号")
    private String erpOrderNo;

    @ApiModelProperty(name = "orderNo",value = "来源单号")
    private String orderNo;

    @ApiModelProperty(name = "bizOrgCode",value = "业务组织")
    private String bizOrgCode;
}
