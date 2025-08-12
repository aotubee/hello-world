package com.edc.erp.directly.distribution.entity;


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
 * 直营分货门店商品关联表(OrdDirOrderDistributionDetail)实体类
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_order_distribution_detail")
@ApiModel(value = "OrdDirOrderDistributionDetail", description = "直营分货门店商品关联表")
public class OrdDirOrderDistributionDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 直营分货关联表主键
     */
    @ApiModelProperty(name = "id", value = "直营分货关联表主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 直营分货单主键
     */
    @ApiModelProperty(name = "distributionOrderId", value = "直营分货单主键")
    private Long distributionOrderId;

    /**
     * 直营分货数量
     */
    @ApiModelProperty(name = "distributionQuantity", value = "直营分货数量")
    private BigDecimal distributionQuantity;

    /**
     * 直营分货包装数
     */
    @ApiModelProperty(name = "packingNumber", value = "直营分货包装数")
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
     * 直营分货金额
     */
    @ApiModelProperty(name = "distributionAmount", value = "直营分货金额")
    private BigDecimal distributionAmount;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 直营价
     */
    @ApiModelProperty(name = "originalPrice", value = "直营价")
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
