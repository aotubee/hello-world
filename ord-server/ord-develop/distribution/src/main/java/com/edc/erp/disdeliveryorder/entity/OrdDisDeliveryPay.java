package com.edc.erp.disdeliveryorder.entity;


import com.edc.plugins.common.model.BaseEntity;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_delivery_pay")
@ApiModel(value = "OrdDisDelivery", description = "配销单支付表")
public class OrdDisDeliveryPay extends BaseEntity {


    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配销单号 */
    @ApiModelProperty(name = "deliveryOrderNo", value = "配销单号")
    private String deliveryOrderNo;

    /** 支付单号 */
    @ApiModelProperty(name = "payNo", value = "支付单号")
    private String payNo;

    /** 原交易金额 */
    @ApiModelProperty(name = "originalAmount", value = "原交易金额")
    private BigDecimal originalAmount;

    /** 实际金额 */
    @ApiModelProperty(name = "actualAmount", value = "实际金额")
    private BigDecimal actualAmount;

    /** 交易状态 */
    @ApiModelProperty(name = "transactionStatus", value = "交易状态")
    private String transactionStatus;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

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
