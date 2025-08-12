package com.edc.erp.common.model.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 商品配送方案明细表(GoodsDistributionPlanGroupDetails)实体类
 *
 * @author lx
 * @since 2022-07-21 15:13:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "gc_goods_distribution_plan_group_details")
@ApiModel(value = "GoodsDistributionPlanGroupDetails", description = "商品配送方案明细表")
public class GoodsDistributionPlanGroupDetails implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 方案代码 */
    @ApiModelProperty(name = "distributionPlanCode", value = "方案代码")
    private String distributionPlanCode;

    /** 组织商品ID */
    @ApiModelProperty(name = "orgGoodsId", value = "组织商品ID")
    private Long orgGoodsId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 配送仓储代码 */
    @ApiModelProperty(name = "warehouseCode", value = "配送仓储代码")
    private String warehouseCode;

    /** 配送仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "配送仓位代码")
    private String stockCode;

    /** 配货方式 */
    @ApiModelProperty(name = "distributionWay", value = "配货方式")
    private String distributionWay;

    /** 配送订单方代码 */
    @ApiModelProperty(name = "distributionOrderCode", value = "配送订单方代码")
    private String distributionOrderCode;

    /** 退货属性 */
    @ApiModelProperty(name = "returnProperty", value = "退货属性")
    private String returnProperty;

    /** 退货原则 */
    @ApiModelProperty(name = "returnPrinciple", value = "退货原则")
    private String returnPrinciple;

    /** 退货仓储代码 */
    @ApiModelProperty(name = "returnWarehouseCode", value = "退货仓储代码")
    private String returnWarehouseCode;

    /** 退货仓位代码 */
    @ApiModelProperty(name = "returnStockCode", value = "退货仓位代码")
    private String returnStockCode;

    /** 退货方式 */
    @ApiModelProperty(name = "salesReturnMethod", value = "退货方式")
    private String salesReturnMethod;

    /** 门店订货下限 */
    @ApiModelProperty(name = "storeOrderLower", value = "门店订货下限")
    private Integer storeOrderLower;

    /** 门店订货上限 */
    @ApiModelProperty(name = "storeOrderUpper", value = "门店订货上限")
    private Integer storeOrderUpper;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 修改人 */
    @ApiModelProperty(name = "updater", value = "修改人")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除(0：否，1：是；默认0) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

}
