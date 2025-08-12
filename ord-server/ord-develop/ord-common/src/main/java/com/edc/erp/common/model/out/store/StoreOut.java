/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.model.out.store;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 门店信息出参
 *
 * @author: weichao
 */
@Data
public class StoreOut implements Serializable {

    private static final long serialVersionUID = -951425897620297629L;

    private Integer storeId;
    private String storeCode;
    private String storeName;
    private String storeType;
    private String storeProperty;
    private String provinceCode;
    private String cityCode;
    private String countryCode;
    private String creatorId;
    private String updatorId;
    private String storePhone;
    private String storeOwner;
    private String businessWay;
    private BigDecimal lng;
    private BigDecimal lat;
    private String address;
    private String stateCode;
    private Integer isDelete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer distance;
    private String storeTypeStr;
    private String storePropertyStr;
    private String stateCodeStr;
    private String provinceCodeStr;
    private String cityCodeStr;
    private String countryCodeStr;
    private String organization;
    private String orgCode;
    private String area;
    private String alcSchemeCode;
    private String priceSchemeCode;
    private String saleSchemeCode;
    @ApiModelProperty(name = "belongArea", value = "所属区域代码")
    private String belongArea;
    private String bizOrgCode;
}
