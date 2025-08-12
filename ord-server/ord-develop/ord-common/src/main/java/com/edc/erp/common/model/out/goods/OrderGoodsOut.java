/**
 * com.edc.
 * Copyright (c) 2019-2020 All Rights Reserved.
 */
package com.edc.erp.common.model.out.goods;

import com.edc.erp.common.model.entity.GoodsStatusBusinessSwitch;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @return: 订单商品信息返回类
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
public class OrderGoodsOut implements Serializable {

    private static final long serialVersionUID = 4946854852779114336L;
    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    private Integer id;

    /**
     * 配货规格Id
     */
    @ApiModelProperty(name = "distributionSpecId", value = "配货规格Id")
    private Long distributionSpecId;

    /**
     * 零售规格Id
     */
    @ApiModelProperty(name = "sellSpecId", value = "零售规格Id")
    private Long sellSpecId;

    /**
     * 组织商品id
     */
    @ApiModelProperty(name = "orgGoodsId", value = "组织商品id")
    private Integer orgGoodsId;
    /**
     * 商品GID
     */
    @ApiModelProperty(name = "goodsId", value = "商品GID")
    private Integer goodsId;
    /**
     * 商品名称
     */
    @ApiModelProperty(name = "orgGoodsName", value = "商品名称")
    private String goodsName;


    /**
     * 条码代码
     */
    @ApiModelProperty(name = "barCode", value = "条码代码")
    private String barCode;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 价格类型
     */
    @ApiModelProperty(name = "priceType", value = "价格类型")
    private String priceType;


    /**
     * 配货规格
     */
    @ApiModelProperty(name = "distributionSpecification", value = "配货规格")
    private StandardSpecTransInfoOut distributionSpecification;

    /**
     * 零售规格
     */
    @ApiModelProperty(name = "retailSpecification", value = "零售规格")
    private StandardSpecTransInfoOut retailSpecification;

    /**
     * 品类
     */
    @ApiModelProperty(name = "sortName", value = "品类名称")
    private String sortName;

    /**
     * 品类id
     */
    @ApiModelProperty(name = "sortId", value = "品类id")
    private Integer sortId;

    /**
     * 品类代码
     */
    @ApiModelProperty(name = "sort", value = "品类代码")
    private String sort;

    /**
     * 仓位
     */
    @ApiModelProperty(name = "stockName", value = "仓位")
    private String stockName;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 仓位
     */
    @ApiModelProperty(name = "stockId", value = "仓位id")
    private Integer stockId;

    /**
     * 仓位字典代码
     */
    @ApiModelProperty(name = "temperatureLayer", value = "温层")
    private String temperatureLayer;


    /**
     * 退货仓位ID
     */
    @ApiModelProperty(name = "backStockId", value = "退货仓位ID")
    private Integer backStockId;

    /**
     * 退货仓位code
     */
    @ApiModelProperty(name = "backStockCode", value = "退货仓位code")
    private String backStockCode;

    /**
     * 供应商
     */
    @ApiModelProperty(name = "vendorName", value = "订单方名称")
    private String vendorName;

    /**
     * 供应商(订单方)
     */
    @ApiModelProperty(name = "vendorCode", value = "供应商代码(订单方代码)")
    private String vendorCode;

    /**
     * 品牌
     */
    @ApiModelProperty(name = "brandCode", value = "品牌code")
    private String brand;

    /**
     * 品牌
     */
    @ApiModelProperty(name = "brandCode", value = "品牌id")
    private String brandId;

    /**
     * 品牌名称
     */
    @ApiModelProperty(name = "brandName", value = "品牌名称")
    private String brandName;

    /**
     * 产地
     */
    @ApiModelProperty(name = "origin", value = "产地")
    private String origin;

    /**
     * 保质期
     */
    @ApiModelProperty(name = "expirationDate", value = "保质期")
    private String expirationDate;

    /**
     * 商品状态
     */
    @ApiModelProperty(name = "status", value = "商品状态")
    private String status;

    /**
     * 商品状态id
     */
    @ApiModelProperty(name = "statusId", value = "商品状态Id")
    private Integer statusId;

    /**
     * 商品状态
     */
    @ApiModelProperty(name = "statusName", value = "商品名称")
    private String statusName;


    /**
     * 产品代码
     */
    @ApiModelProperty(name = "produceCode", value = "产品代码")
    private String produceCode;

    /**
     * 配货价
     */
    @ApiModelProperty(name = "distributionPrice", value = "配货价")
    private BigDecimal distributionPrice;

    /**
     * 建议零售价
     */
    @ApiModelProperty(name = "adviceSalePrice", value = "建议零售价")
    private BigDecimal adviceSalePrice;

    /**
     * 月销量
     */
    @ApiModelProperty(name = "monthlySales", value = "月销量")
    private Integer monthlySales;

    /**
     * 是否下架
     */
    @ApiModelProperty(name = "isShelves", value = "是否下架")
    private Integer isShelves;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /**
     * 数量
     */
    @ApiModelProperty(name = "goodsNum", value = "数量")
    private BigDecimal goodsNum;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 商品主图
     */
    @ApiModelProperty(name = "imgUrl", value = "商品主图")
    private String imgUrl;


    /**
     * 标签集合
     */
    @ApiModelProperty(name = "storeRecommendOut", value = "标签集合")
    private List<StoreRecommendOut> storeRecommendOutList;


    /**
     * 是否允许退货
     */
    @ApiModelProperty(name = "allowDistributionReturn", value = "是否允许退货")
    private String allowDistributionReturn;

    /**
     * 配货方式
     */
    @ApiModelProperty(name = "distributionWay", value = "配货方式")
    private String distributionWay;

    /**
     * 退货原则
     */
    @ApiModelProperty(name = "returnPrinciple", value = "退货原则")
    private String returnPrinciple;


    /**
     * 是否散货
     */
    @ApiModelProperty(name = "isDisp", value = "是否散货")
    private Integer isDisp;

    /**
     * 配货单价
     */
    @ApiModelProperty(name = "distributionUnitPrice", value = "配货单价")
    private BigDecimal distributionUnitPrice;

    /**
     * 存储类型
     */
    @ApiModelProperty(name = "storageArea", value = "存储类型")
    private String storageArea;


    /**
     * 生效的下限
     */
    @ApiModelProperty(name = "validRecommendLowerLimit", value = "生效的下限")
    private BigDecimal validRecommendLowerLimit;

    /**
     * 推荐下限
     */
    @ApiModelProperty(name = "recommendLowerLimit", value = "推荐下限")
    private BigDecimal recommendLowerLimit;

    /**
     * 生效的上限
     */
    @ApiModelProperty(name = "validRecommendUpperLimit", value = "生效的上限")
    private BigDecimal validRecommendUpperLimit;

    /**
     * 推荐上限
     */
    @ApiModelProperty(name = "recommendUpperLimit", value = "推荐上限")
    private BigDecimal recommendUpperLimit;

    /**
     * 申请的上限
     */
    @ApiModelProperty(name = "applyRecommendUpperLimit", value = "申请的上限")
    private BigDecimal applyRecommendUpperLimit;

    /**
     * 申请的下限
     */
    @ApiModelProperty(name = "applyRecommendLowerLimit", value = "申请的下限")
    private BigDecimal applyRecommendLowerLimit;

    /**
     * 推荐上下限主键
     */
    @ApiModelProperty(name = "recommendUpperLowerLimitId", value = "推荐上下限主键")
    private Integer recommendUpperLowerLimitId;
    /**
     * 生效中是否自动补货
     */
    @ApiModelProperty(name = "validIsAutoReplenishment", value = "生效中是否自动补货,1=是；0=否")
    private Integer validIsAutoReplenishment;

    /**
     * 申请中是否自动补货
     */
    @ApiModelProperty(name = "applyIsAutoReplenishment", value = "申请中是否自动补货,1=是；0=否")
    private Integer applyIsAutoReplenishment;


    /**
     * 是否配置上下限
     */
    @ApiModelProperty(name = "isUpperAndLower", value = "是否配置上下限")
    private Integer isUpperAndLower;

    /**
     * 活动
     */
    @ApiModelProperty(name = "activity", value = "活动")
    private DisActivityOut activity;
    /**
     * 商品属性
     */
    @ApiModelProperty(name = "goodsType", value = "商品属性")
    private String goodsType;

    /**
     * 商品属性
     */
    @ApiModelProperty(name = "goodsTypeStr", value = "商品属性中文")
    private String goodsTypeStr;

    /**
     * 进项税
     */
    @ApiModelProperty(name = "inTaxId", value = "进项税id")
    private Integer inTaxId;

    /**
     * 销项税
     */
    @ApiModelProperty(name = "outTaxId", value = "销项税id")
    private Integer outTaxId;

    /**
     * 进税率
     */
    @ApiModelProperty(name = "inTax", value = "进税率")
    private BigDecimal inTax;

    /**
     * 销项税率
     */
    @ApiModelProperty(name = "outTax", value = "销项税率")
    private BigDecimal outTax;

    /**
     * 商品业务开关信息
     */
    @ApiModelProperty(name = "goodsStatusBusinessSwitch", value = "商品业务开关信息")
    private GoodsStatusBusinessSwitch goodsStatusBusinessSwitch;

    /**
     * 财务库存数
     */
    @ApiModelProperty(name = "stockQuantity", value = "财务库存数")
    private BigDecimal stockQuantity;

    /**
     * 仓储代码
     */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    /**
     * 退货仓位code
     */
    @ApiModelProperty(name = "returnWarehouseCode", value = "退货仓储code")
    private String returnWarehouseCode;

    /** 商品经营方案明细id */
    @ApiModelProperty(name = "businessPlanDetailsId", value = "商品经营方案明细id")
    private Integer businessPlanDetailsId;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;

    @ApiModelProperty(name = "isUpDownLimit", value = "商品是否允许上下限跑货")
    private Integer isUpDownLimit;

    @ApiModelProperty(name = "isUpDownLimitLogc", value = "仓位是否允许上下限跑货")
    private Integer isUpDownLimitLogc;

    @ApiModelProperty(name = "validityCode", value = "效期码")
    private String validityCode;

    @ApiModelProperty(value = "商品是否管理效期")
    private Integer isManageValidityPeriod;

}
