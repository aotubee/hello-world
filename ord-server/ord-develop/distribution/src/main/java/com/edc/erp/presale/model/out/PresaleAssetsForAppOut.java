package com.edc.erp.presale.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName PresaleAssetsForAppOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/11 11:18
 **/
@Data
public class PresaleAssetsForAppOut implements Serializable {
    private static final long serialVersionUID = 3202895864584040998L;

    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动号")
    private String presaleActivityNo;

    @ApiModelProperty(name = "beginOrderDateStr", value = "可订货开始时间")
    private String beginOrderDateStr;

    /**
     * 可订货结束时间
     */
    @ApiModelProperty(name = "endOrderDateStr", value = "可订货结束时间Str")
    private String endOrderDateStr;

    @ApiModelProperty(name = "presaleAssetsDetailList", value = "资产明细")
    private List<PresaleAssetsDetailForAppOut> presaleAssetsDetailList;
}
