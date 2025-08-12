package com.edc.erp.common.entity;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-01-28 12:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "gc_store_sku_monthly_sales")
public class StoreSkuMonthlySales extends BaseEntity {

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店商品月销量
     */
    @ApiModelProperty(name = "monthlySales", value = "门店商品月销量")
    private BigDecimal monthlySales;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "skuCode", value = "商品代码")
    private String skuCode;

    /**
     * 组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;

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
}
