package com.edc.erp.directly.distribution.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店订货调配表实体类
 *
 * @author wanglidong
 * @since 2022-11-15 16:23:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_order_allocation_pool_history")
@ApiModel(value = "OrdDirOrderAllocationPoolHistory", description = "门店订货调配历史表")
public class OrdDirOrderAllocationPoolHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 状态
     */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 截单时间
     */
    @ApiModelProperty(name = "truncationDateTime", value = "截单时间")
    private LocalDateTime truncationDateTime;

    /**
     * 叫货周期
     */
    @ApiModelProperty(name = "deliveryCycle", value = "叫货周期")
    private String deliveryCycle;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 配送规格
     */
    @ApiModelProperty(name = "distributionSpec", value = "配送规格")
    private String distributionSpec;

    /**
     * 配送规格
     */
    @ApiModelProperty(name = "distributionSpecNum", value = "配送规格数量")
    private Integer distributionSpecNum;

    /**
     * 商品等级
     */
    @ApiModelProperty(name = "goodsLevel", value = "商品等级")
    private String goodsLevel;

    /**
     * 基础陈列量
     */
    @ApiModelProperty(name = "baseDisplayQuantity", value = "基础陈列量")
    private BigDecimal baseDisplayQuantity;

    /**
     * 单日平均销售数量
     */
    @ApiModelProperty(name = "averageDailySalesQuantity", value = "单日平均销售数量")
    private BigDecimal averageDailySalesQuantity;

    /**
     * 计算订货数量
     */
    @ApiModelProperty(name = "computeOrderQuantity", value = "计算订货数量")
    private BigDecimal computeOrderQuantity;

    /**
     * 建议订货数量
     */
    @ApiModelProperty(name = "recommendOrderQuantity", value = "建议订货数量")
    private BigDecimal recommendOrderQuantity;

    /**
     * 门店下单量
     */
    @ApiModelProperty(name = "storeOrderQuantity", value = "门店下单量")
    private BigDecimal storeOrderQuantity;

    /**
     * 补单量
     */
    @ApiModelProperty(name = "supplementQuantity", value = "补单量")
    private BigDecimal supplementQuantity;

    /**
     * 补单包装量
     */
    @ApiModelProperty(name = "supplementPackageQuantity", value = "补单包装量")
    private BigDecimal supplementPackageQuantity;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
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

    @ApiModelProperty(name = "isSuccess", value = "是否成功")
    private Integer isSuccess;
}

