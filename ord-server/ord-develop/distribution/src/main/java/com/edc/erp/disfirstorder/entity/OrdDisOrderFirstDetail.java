package com.edc.erp.disfirstorder.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
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


/**
 * 配销铺货单明细表(OrdDisOrderFirstDetail)实体类
 *
 * @author weichao
 * @since 2022-10-10 16:18:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_order_first_detail")
@ApiModel(value = "OrdDisOrderFirstDetail", description = "配销铺货单明细表")
public class OrdDisOrderFirstDetail implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键id */
    @ApiModelProperty(name = "id", value = "主键id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配销铺货订单主键 */
    @ApiModelProperty(name = "firstOrderId", value = "配销铺货订单主键")
    private Long firstOrderId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 配销铺货数量 */
    @ApiModelProperty(name = "num", value = "配销铺货数量")
    private Integer num;

    /** 配销铺货金额 */
    @ApiModelProperty(name = "amount", value = "配销铺货金额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;

    /** 创建者 */
    @ApiModelProperty(name = "creator", value = "创建者")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /** 修改者 */
    @ApiModelProperty(name = "updater", value = "修改者")
    private String updater;

    /** 修改时间 */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @ApiModelProperty(name = "isDelete", value = "是否删除")
    private Integer isDelete;

}
