package com.edc.erp.entity;


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
import java.util.Date;



/**
 * 配销活动表(DisActivity)实体类
 *
 * @author fxw
 * @since 2022-10-19 18:43:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "opt_dis_activity")
@ApiModel(value = "DisActivity", description = "配销活动表")
public class DisActivity implements Serializable{
    
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

    /** 特价促销单号 */
    @ApiModelProperty(name = "specialPromotionOrderNo", value = "特价促销单号")
    private String specialPromotionOrderNo;

    /** 状态 */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    /** 商品数量 */
    @ApiModelProperty(name = "goodsNum", value = "商品数量")
    private Integer goodsNum;

    /** 门店数量 */
    @ApiModelProperty(name = "storeNum", value = "门店数量")
    private Integer storeNum;

    /** 活动开始时间 */
    @ApiModelProperty(name = "activityStartTime", value = "活动开始时间")
    private Date activityStartTime;

    /** 活动结束时间 */
    @ApiModelProperty(name = "activityEndTime", value = "活动结束时间")
    private Date activityEndTime;

    /** 备注 */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /** app促销位置 */
    @ApiModelProperty(name = "appPromotionLocation", value = "app促销位置")
    private String appPromotionLocation;

    /** 创建人 */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /** 创建时间 */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private Date createTime;

    /** 最后修改人 */
    @ApiModelProperty(name = "updater", value = "最后修改人")
    private String updater;

    /** 最后修改时间 */
    @ApiModelProperty(name = "updateTime", value = "最后修改时间")
    private Date updateTime;

    /** 是否删除(0：否，1：是；默认0) */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /** 活动类型(特价，赠品) */
    @ApiModelProperty(name = "activityType", value = "活动类型(特价，赠品)")
    private String activityType;

}
