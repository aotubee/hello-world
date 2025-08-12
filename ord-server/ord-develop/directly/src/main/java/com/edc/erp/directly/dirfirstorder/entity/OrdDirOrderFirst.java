package com.edc.erp.directly.dirfirstorder.entity;


import java.math.BigDecimal;
import java.util.Date;

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
import java.time.LocalDateTime;



/**
 * 铺货单(OrdDirOrderFirst)实体类
 *
 * @author weichao
 * @since 2022-11-10 14:09:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_order_first")
@ApiModel(value = "OrdDirOrderFirst", description = "铺货单")
public class OrdDirOrderFirst implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键id */
    @ApiModelProperty(name = "id", value = "主键id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 铺货单号 */
    @ApiModelProperty(name = "firstOrderNo", value = "铺货单号")
    private String firstOrderNo;

    /** 铺货单状态 */
    @ApiModelProperty(name = "firstOrderStatus", value = "铺货单状态")
    private String firstOrderStatus;

    /** 门店代码 */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /** 门店名称 */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /** 铺货品项数 */
    @ApiModelProperty(name = "goodsNum", value = "铺货品项数")
    private Integer goodsNum;

    /** 铺货数量 */
    @ApiModelProperty(name = "totalNum", value = "铺货数量")
    private Integer totalNum;

    /** 铺货总额 */
    @ApiModelProperty(name = "totalAmount", value = "铺货总额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalAmount;

    /** 生效时间 */
    @ApiModelProperty(name = "effectiveTime", value = "生效时间")
    private LocalDateTime effectiveTime;

    /** 审核人 */
    @ApiModelProperty(name = "approver", value = "审核人")
    private String approver;

    /** 审核时间 */
    @ApiModelProperty(name = "approvalTime", value = "审核时间")
    private LocalDateTime approvalTime;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

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

    /** 是否立即生效 */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否立即生效")
    private Integer isEffectiveImmediately;

}
