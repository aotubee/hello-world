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
import java.time.LocalDateTime;


/**
 * 直营分货单(OrdDisOrderDistribution)实体类
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_order_distribution_result")
@ApiModel(value = "OrdDisOrderDistributionResult", description = "加盟分货单结果")
public class OrdDisOrderDistributionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 直营分货主键
     */
    @ApiModelProperty(name = "id", value = "直营分货主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 直营分货单号
     */
    @ApiModelProperty(name = "distributionOrderId", value = "直营分货单主键")
    private Long distributionOrderId;

    /**
     * 门店
     */
    @ApiModelProperty(name = "storeCode", value = "门店")
    private String storeCode;

    /**
     * 是否完成订货单创建
     */
    @ApiModelProperty(name = "isDone", value = "是否完成订货单创建(0:未处理；1:已完成；2:处理失败)")
    private Integer isDone;

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

}
