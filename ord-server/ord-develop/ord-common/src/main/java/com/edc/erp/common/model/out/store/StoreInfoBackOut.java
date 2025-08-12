/**
 * Copyright © 2010-2021 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.out.store;

import com.edc.erp.common.model.entity.StoreInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 运营端门店基础信息出参
 *
 * @author: gusiyuan
 * @date: 2021-05-27
 */
@Data
public class StoreInfoBackOut extends StoreInfo {

    private static final long serialVersionUID = 1L;

    /**
     * 所属组织
     */
    @ApiModelProperty(name = "orgCodeStr", value = "所属组织中文")
    private String orgCodeStr;

    /**
     * 默认配送中心
     */
    @ApiModelProperty(name = "defaultDistributionCenterStr", value = "默认配送中心中文")
    private String defaultDistributionCenterStr;

    /**
     * 门店状态
     */
    @ApiModelProperty(name = "storeStatusStr", value = "门店状态中文")
    private String storeStatusStr;

    /**
     * 门店业态
     */
    @ApiModelProperty(name = "storeTypeStr", value = "门店业态中文")
    private String storeTypeStr;

    /**
     * 门店属性
     */
    @ApiModelProperty(name = "storePropertyStr", value = "门店属性中文")
    private String storePropertyStr;

    /**
     * 经营方式
     */
    @ApiModelProperty(name = "businessWayStr", value = "经营方式中文")
    private String businessWayStr;

    /**
     * 所属区域
     */
    @ApiModelProperty(name = "belongAreaStr", value = "所属区域中文")
    private String belongAreaStr;

    @ApiModelProperty(value = "距离")
    private Integer distance;

    @ApiModelProperty(value = "未维护信息")
    private String unMaintains;

    @ApiModelProperty(name = "marketCategoryStr", value = "市场类型中文")
    private String marketCategoryStr;
}
