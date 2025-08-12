package com.edc.erp.directly.distribution.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 直营分货单导入类
 *
 * @author lixuejun
 */
@Data
public class OrdDirOrderDistributionIn extends Page implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 直营分货状态
     */
    @ApiModelProperty(name = "distributionOrderStatus", value = "直营分货状态")
    private String distributionOrderStatus;

    /**
     * 直营分货单号
     */
    @ApiModelProperty(name = "distributionOrderNo", value = "直营分货单号")
    private String distributionOrderNo;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 创建时间开始
     */
    @ApiModelProperty(name = "createTimeStart", value = "创建时间")
    private String createTimeStart;

    /**
     * 创建时间结束
     */
    @ApiModelProperty(name = "createTimeEnd", value = "创建时间")
    private String createTimeEnd;

    /**
     * 生效开始时间
     */
    @ApiModelProperty(name = "effectiveStateTime",value = "生效开始时间")
    private String effectiveStateTime;

    /**
     * 生效结束时间
     */
    @ApiModelProperty(name = "effectiveEndTime",value = "生效结束时间")
    private String effectiveEndTime;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "creator", value = "创建人")
    private String creator;

    /**
     * 是否删除(0：否，1：是；默认0)
     */
    @ApiModelProperty(name = "isDelete", value = "是否删除(0：否，1：是；默认0)")
    private Integer isDelete;

    /**
     * 门店主键
     */
    @ApiModelProperty(name = "storeId", value = "门店主键")
    private Integer storeId;

    /**
     * 业务组织
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织")
    private String bizOrgCode;
}
