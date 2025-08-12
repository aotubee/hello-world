package com.edc.erp.common.model.out.store;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * description 门店信息与开关
 *
 * @author gusiyuan
 * @since 2023/11/13 09:45
 */
@Data
public class StoreAndStatusSwitchOut implements Serializable {
    private static final long serialVersionUID = -2553546956147674854L;

    @ApiModelProperty(value = "门店id")
    private Integer storeId;

    @ApiModelProperty(value = "erp门店代码")
    private String storeCode;

    @ApiModelProperty(value = "门店名称")
    private String storeName;

    @ApiModelProperty(value = "业务组织")
    private String bizOrgCode;

    @ApiModelProperty(value = "所属区域")
    private String belongArea;

    @ApiModelProperty(value = "门店状态")
    private String storeStatus;

    @ApiModelProperty(value = "门店业态")
    private String storeType;

    @ApiModelProperty(value = "门店类型")
    private String storeProperty;

    @ApiModelProperty(value = "经营方式")
    private String businessWay;

    @ApiModelProperty(value = "所属配送方案")
    private String alcSchemeCode;

    @ApiModelProperty(value = "所属价格组")
    private String priceSchemeCode;

    @ApiModelProperty(value = "所属经营方案")
    private String saleSchemeCode;

    @ApiModelProperty(value = "市场类型")
    private String marketCategory;

    @ApiModelProperty(value = "状态开关")
    private StoreStatusBusinessSwitch storeStatusBusinessSwitch;
}
