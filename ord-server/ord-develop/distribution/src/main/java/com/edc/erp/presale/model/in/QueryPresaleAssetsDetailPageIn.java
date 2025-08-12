package com.edc.erp.presale.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName QueryPresaleAssetsDetailPageIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/3 18:23
 **/
@Data
public class QueryPresaleAssetsDetailPageIn extends Page implements Serializable {
    private static final long serialVersionUID = -1234522719036449393L;

    @ApiModelProperty(name = "assetsId",  value = "资产id")
    private Long assetsId;

    @ApiModelProperty(name = "presaleActivityNo",  value = "活动单号")
    private String presaleActivityNo;

    @ApiModelProperty(name = "goodsCode",  value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "barCode",value = "条码")
    private String barCode;

    @ApiModelProperty(name = "beginOrderTime",value = "订货开始时间")
    private String beginOrderTime;

    @ApiModelProperty(name = "endOrderTime",  value = "订货结束时间")
    private String endOrderTime;

    @ApiModelProperty(name = "status", value = "活动单状态")
    private String status;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
}
