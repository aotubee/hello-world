package com.edc.erp.distribution.entity;


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
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 配销分货门店商品关联表(OrdDisOrderDistributionDetail)实体类
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_order_distribution_detail")
@ApiModel(value = "OrdDisOrderDistributionDetail", description = "配销分货门店商品关联表")
public class OrdDisOrderDistributionDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配销分货关联表主键
     */
    @ApiModelProperty(name = "id", value = "配销分货关联表主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配销分货单主键
     */
    @ApiModelProperty(name = "distributionOrderId", value = "配销分货单主键")
    private Long distributionOrderId;

    /**
     * 配销分货数量
     */
    @ApiModelProperty(name = "distributionQuantity", value = "配销分货数量")
    private BigDecimal distributionQuantity;

    /**
     * 配销分货包装数
     */
    @ApiModelProperty(name = "packingNumber", value = "配销分货包装数")
    private BigDecimal packingNumber;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 配销分货金额
     */
    @ApiModelProperty(name = "distributionAmount", value = "配销分货金额")
    private BigDecimal distributionAmount;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 配销价
     */
    @ApiModelProperty(name = "originalPrice", value = "配销价")
    private BigDecimal originalPrice;

    /**
     * 配货规格
     */
    @ApiModelProperty(name = "distributionSpecification", value = "配货规格")
    private String distributionSpecification;

    /**
     * 当前库存
     */
    @ApiModelProperty(name = "wrhInvQty", value = "当前库存")
    private BigDecimal wrhInvQty;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 门店区域
     */
    @ApiModelProperty(name = "storeArea", value = "门店区域")
    private String storeArea;

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
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

}
