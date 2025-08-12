/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.directly.upperlowerlimit.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 补货配置实体类
 *
 * @author gusiyuan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplenishmentConfig {

    /**
     * 主键
     */

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 商品sku
     */
    @ApiModelProperty(name = "skuCode", value = "商品sku")
    private String skuCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 库存上限
     */
    @ApiModelProperty(name = "inventoryUpperLimit", value = "库存上限")
    private BigDecimal inventoryUpperLimit;

    /**
     * 库存下限
     */
    @ApiModelProperty(name = "inventoryLowerLimit", value = "库存下限")
    private BigDecimal inventoryLowerLimit;

    /**
     * 最低库存上限
     */
    @ApiModelProperty(name = "minInventoryUpperLimit", value = "最低库存上限")
    private BigDecimal minInventoryUpperLimit;

    /**
     * 最低库存下限
     */
    @ApiModelProperty(name = "minInventoryLowerLimit", value = "最低库存下限")
    private BigDecimal minInventoryLowerLimit;

    /**
     * 是否自动补货
     */
    @ApiModelProperty(name = "isAutoReplenishment", value = "是否自动补货")
    private Integer isAutoReplenishment;

    /**
     * 组织代码
     */
    @ApiModelProperty(name= "orgCode", value = "组织代码")
    private String orgCode;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /**
     * 修改时间
     */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "公司代码")
    private String bizOrgCode;

    public ReplenishmentConfig(String storeCode, String skuCode, Integer isDelete, String orgCode) {
        this.storeCode = storeCode;
        this.skuCode = skuCode;
        this.isDelete = isDelete;
        this.orgCode = orgCode;
    }
}
