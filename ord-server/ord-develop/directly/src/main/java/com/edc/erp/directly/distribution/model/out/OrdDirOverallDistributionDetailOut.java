package com.edc.erp.directly.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName OrdDirOverallDistributionDetailOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/20 14:50
 **/
@Data
public class OrdDirOverallDistributionDetailOut implements Serializable {
    private static final long serialVersionUID = 5252950018203581757L;

    /**
     * 直营分货单ID
     */
    @ApiModelProperty(name = "distributionOrderId", value = "直营分货单ID")
    private Long distributionOrderId;

    /**
     * 直营分货单号
     */
    @ApiModelProperty(name = "distributionOrderNo", value = "直营分货单号")
    private String distributionOrderNo;

    /**
     * 直营分货状态中文
     */
    @ApiModelProperty(name = "distributionOrderStatusStr", value = "直营分货状态中文")
    private String distributionOrderStatusStr;


    /**
     * 是否即时生效
     */
    @ApiModelProperty(name = "isEffectiveImmediately", value = "是否即时生效(0:否 1是)")
    private Integer isEffectiveImmediately;

    /**
     * 生效时间
     */
    @ApiModelProperty(name = "effectiveTime", value = "生效时间")
    private LocalDateTime effectiveTime;

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

    @ApiModelProperty(name = "distributionStoreOutList", value = "门店商品明细集合")
    private List<OrdDirOverallDistributionStoreOut> distributionStoreOutList;
}
