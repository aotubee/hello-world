package com.edc.erp.presale.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName UpdateDisPresaleAssetsIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/2 10:21
 **/
@Data
public class UpdateDisPresaleAssetsIn implements Serializable {
    private static final long serialVersionUID = -5835654485803730957L;

    @ApiModelProperty(name = "assetsId", value = "门店预售资产ID")
    private Long assetsId;

    @ApiModelProperty(name = "bizOrgCode", value = "来源单号")
    private String bizOrgCode;

    @ApiModelProperty(name = "loginUsername", value = "来源单号")
    private String loginUsername;

    @ApiModelProperty(name = "businessType", value = "业务类型")
    private String businessType;

    @ApiModelProperty(name = "sourceNo", value = "来源单号")
    private String sourceNo;


    @ApiModelProperty(name = "assetsGoodsInList", value = "明细集合")
    private List<UpdateDisPresaleAssetsGoodsIn> assetsGoodsInList;
}
