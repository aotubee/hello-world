package com.edc.erp.disdeliveryorder.entity;


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
 * 配销单(OrdDisDelivery)实体类
 *
 * @author weichao
 * @since 2022-10-10 19:48:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_delivery")
@ApiModel(value = "OrdDisDelivery", description = "配销单")
public class OrdDisDelivery implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配销单号 */
    @ApiModelProperty(name = "deliveryOrderNo", value = "配销单号")
    private String deliveryOrderNo;

    /** 配销单状态 */
    @ApiModelProperty(name = "deliveryStatusCode", value = "配销单状态")
    private String deliveryStatusCode;

    /** 来源 */
    @ApiModelProperty(name = "sourceCode", value = "来源")
    private String sourceCode;

    /** 仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /** 是否红冲单 */
    @ApiModelProperty(name = "isReversalOrder", value = "是否红冲单")
    private Integer isReversalOrder;

    /** 是否被红冲 */
    @ApiModelProperty(name = "isReversal", value = "是否被红冲")
    private Integer isReversal;

    /** 配销方式 */
    @ApiModelProperty(name = "distributionType", value = "配销方式")
    private String distributionType;

    /**品类属性*/
    @ApiModelProperty(name = "goodsType",value = "品类属性")
    private String goodsType;

    /** 品项数 */
    @ApiModelProperty(name = "skuCount", value = "品项数")
    private Integer skuCount;

    /** 门店名称 */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 集货数量 */
    @ApiModelProperty(name = "orderQuantity", value = "集货数量")
    private BigDecimal orderQuantity;

    /** 集货金额 */
    @ApiModelProperty(name = "orderAmount", value = "集货金额")
    private BigDecimal orderAmount;

    /** 配销数量 */
    @ApiModelProperty(name = "distributionQuantity", value = "配销数量")
    private BigDecimal distributionQuantity;

    /** 配销金额 */
    @ApiModelProperty(name = "distributionAmount", value = "配销金额")
    private BigDecimal distributionAmount;

    /** 实配数量 */
    @ApiModelProperty(name = "deliveryQuantity", value = "实配数量")
    private BigDecimal deliveryQuantity;

    /** 实配金额 */
    @ApiModelProperty(name = "deliveryAmount", value = "实配金额")
    private BigDecimal deliveryAmount;

    /** 差异单号 */
    @ApiModelProperty(name = "differenceOrderNo", value = "差异单号")
    private String differenceOrderNo;

    /** 集货单号 */
    @ApiModelProperty(name = "requestOrderNo", value = "集货单号")
    private String requestOrderNo;

    /** 海鼎单号 */
    @ApiModelProperty(name = "hdDeliveryNo", value = "海鼎单号")
    private String hdDeliveryNo;

    /** 发货日期 */
    @ApiModelProperty(name = "deliveryTime", value = "发货日期")
    private LocalDateTime deliveryTime;

    /** 配货日期 */
    @ApiModelProperty(name = "distributionTime", value = "配货日期")
    private LocalDateTime distributionTime;

    /** 收货日期 */
    @ApiModelProperty(name = "receiveTime", value = "收货日期")
    private LocalDateTime receiveTime;

    /** 收货进度，1未开始；2收货中；3收货结束 */
    @ApiModelProperty(name = "receiveProgress", value = "收货进度，1未开始；2收货中；3收货结束")
    private Integer receiveProgress;

    /** 自动收货时间 */
    @ApiModelProperty(name = "autoTakeDeliveryTime", value = "自动收货时间")
    private LocalDateTime autoTakeDeliveryTime;

    /** 收货备注 */
    @ApiModelProperty(name = "takeRemark", value = "收货备注")
    private String takeRemark;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 来源单号 */
    @ApiModelProperty(name = "sourceNo", value = "来源单号")
    private String sourceNo;

    /** 物流单号 */
    @ApiModelProperty(name = "logisticsNo", value = "物流单号")
    private String logisticsNo;

    /** 冻结状态 */
    @ApiModelProperty(name = "freezeStatus", value = "冻结状态")
    private String freezeStatus;

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

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /** 转单优先级 */
    @ApiModelProperty(name = "orderPriority", value = "转单优先级")
    private String orderPriority;

    /** 仓储代码 */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;

    public OrdDisDelivery(Long id, String deliveryOrderNo) {

    }
}
