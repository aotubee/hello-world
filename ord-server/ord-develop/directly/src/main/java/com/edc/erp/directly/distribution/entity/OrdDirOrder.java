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
 * 订单表(OrdDirOrder)实体类
 *
 * @author wanglidong
 * @since 2022-11-15 16:23:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_order")
@ApiModel(value = "OrdDirOrder", description = "订单表")
public class OrdDirOrder implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */     
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 提交时间
     */     
    @ApiModelProperty(name = "sumbitTime", value = "提交时间")
    private LocalDateTime sumbitTime;
    
    /**
     * 门店代码
     */     
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;
    
    /**
     * 订货单单号
     */     
    @ApiModelProperty(name = "orderNo", value = "订货单单号")
    private String orderNo;
    
    /**
     * 订单状态
     */     
    @ApiModelProperty(name = "orderStatusCode", value = "订单状态")
    private String orderStatusCode;
    
    /**
     * 订单应付金额
     */     
    @ApiModelProperty(name = "orderAmount", value = "订单应付金额")
    private BigDecimal orderAmount;
    
    /**
     * 订单优惠金额
     */     
    @ApiModelProperty(name = "preferentialAmount", value = "订单优惠金额")
    private BigDecimal preferentialAmount;
    
    /**
     * 订货类型
     */     
    @ApiModelProperty(name = "sourceCode", value = "订货类型")
    private String sourceCode;
    
    /**
     * 截单时间
     */     
    @ApiModelProperty(name = "cutOffTime", value = "截单时间")
    private LocalDateTime cutOffTime;
    
    /**
     * 要货单主键
     */     
    @ApiModelProperty(name = "requestOrderId", value = "要货单主键")
    private Long requestOrderId;
    
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
     * 提交人
     */     
    @ApiModelProperty(name = "submitter", value = "提交人")
    private String submitter;
    
    /**
     * 订货周期主键
     */     
    @ApiModelProperty(name = "orderCycleId", value = "订货周期主键")
    private Integer orderCycleId;

    /**
     * 整单优惠金额
     */
    @ApiModelProperty(name = "discountAmount", value = "整单优惠金额")
    private BigDecimal discountAmount;
}

