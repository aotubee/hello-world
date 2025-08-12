package com.edc.erp.common.model.out.store;

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
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 门店业务控制开关表
 * </p>
 *
 * @author gusiyuan
 * @since 2022-06-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sc_store_status_business_switch")
@ApiModel(value="StoreStatusBusinessSwitch对象", description="门店业务控制开关表")
public class StoreStatusBusinessSwitch implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ApiModelProperty(value = "门店生命周期")
    @NotEmpty(message = "门店生命周期代码不能为空")
    private String storeLifeCycle;

    @ApiModelProperty(value = "允许门店采购订货")
    private Integer isStorePurOrderSc;

    @ApiModelProperty(value = "允许门店采购退货")
    private Integer isStorePurReSc;

    @ApiModelProperty(value = "允许上下限跑货")
    private Integer isUpLowLimitSc;

    @ApiModelProperty(value = "允许门店订货")
    private Integer isStoreOrderSc;

    @ApiModelProperty(value = "允许要货配货")
    private Integer isDisSc;

    @ApiModelProperty(value = "允许配货退货")
    private Integer isDisReSc;

    @ApiModelProperty(value = "允许铺货")
    private Integer isFirstOrderSc;

    @ApiModelProperty(value = "允许配销上下限跑货")
    private Integer isUpLowLimitDisSc;

    @ApiModelProperty(value = "允许配销订货")
    private Integer isStoreOrderDisSc;

    @ApiModelProperty(value = "允许集货配销")
    private Integer isDisDisSc;

    @ApiModelProperty(value = "允许配销退货")
    private Integer isDisReDisSc;

    @ApiModelProperty(value = "允许配销铺货")
    private Integer isFirstOrderDisSc;

    @ApiModelProperty(value = "允许销售")
    private Integer isSalesSc;

    @ApiModelProperty(value = "允许门店库存调整")
    private Integer isStoreInvAdjSc;

    @ApiModelProperty(value = "允许门店报损")
    private Integer isStoreLossSc;

    @ApiModelProperty(value = "允许门店自主盘点")
    private Integer isStoreTakeStockSc;

    @ApiModelProperty(value = "允许盘点")
    private Integer isTakeStockSc;

    @ApiModelProperty(value = "允许门店调拨")
    private Integer isStoreTrfSc;

    @ApiModelProperty(value = "允许门店领用")
    private Integer isStoreRxSc;

    @ApiModelProperty(value = "允许门店加工-计划性")
    private Integer isStoreProcessSc;

    @ApiModelProperty(value = "公司代码")
    private String orgCode;

    @ApiModelProperty(value = "业务组织")
    private String bizOrgCode;

    @ApiModelProperty(value = "创建人")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改人")
    private String updater;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

    @ApiModelProperty(value = "允许分货")
    private Integer isAllotSc;

    @ApiModelProperty(value = "允许配销分货")
    private Integer isAllotDisSc;
}
