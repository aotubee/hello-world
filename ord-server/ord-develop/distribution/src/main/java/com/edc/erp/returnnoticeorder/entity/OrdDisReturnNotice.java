package com.edc.erp.returnnoticeorder.entity;


import java.time.LocalDateTime;
import java.util.Date;

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
 * 配销退货通知单表(DisReturnNotice)实体类
 *
 * @author yaojinpeng
 * @since 2022-10-28 11:16:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_return_notice")
@ApiModel(value = "DisReturnNotice", description = "配销退货通知单表")
public class OrdDisReturnNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 配销退货通知单单号
     */
    @ApiModelProperty(name = "returnNoticeOrderNo", value = "配销退货通知单单号")
    private String returnNoticeOrderNo;

    /**
     * 配销退货类型
     */
    @ApiModelProperty(name = "returnType", value = "配销退货类型")
    private String returnType;

    /**
     * 配销退货原因
     */
    @ApiModelProperty(name = "returnWhy", value = "配销退货原因")
    private String returnWhy;

    /**
     * 品项数
     */
    @ApiModelProperty(name = "skuCount", value = "品项数")
    private Integer skuCount;

    /**
     * 退货生效时间
     */
    @ApiModelProperty(name = "takeEffectTime", value = "退货生效时间")
    private LocalDateTime takeEffectTime;

    /**
     * 退货截止时间
     */
    @ApiModelProperty(name = "returnDeadline", value = "退货截止时间")
    private LocalDateTime returnDeadline;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

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
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /**
     * 状态
     */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;


    /**
     * 是否立即生效
     */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否立即生效")
    private Integer isEffectiveImmediately;

}
