package com.edc.erp.common.model.entity;

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
 * <p>
 * 商品业务控制开关表
 * </p>
 *
 * @author gusiyuan
 * @since 2022-06-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "gc_goods_status_business_switch")
@ApiModel(value="GoodsStatusBusinessSwitch对象", description="商品业务控制开关表")
public class GoodsStatusBusinessSwitch implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ApiModelProperty(value = "商品状态ID")
    private Integer goodsStatusId;

    @ApiModelProperty(value = "允许仓储采购订货")
    private Integer isWrhPurOrder;

    @ApiModelProperty(value = "允许仓储采购退货")
    private Integer isWrhPurRe;

    @ApiModelProperty(value = "允许门店采购订货")
    private Integer isStorePurOrder;

    @ApiModelProperty(value = "允许门店采购退货")
    private Integer isStorePurRe;

    @ApiModelProperty(value = "允许上下限跑货")
    private Integer isUpLowLimit;

    @ApiModelProperty(value = "允许分货")
    private Integer isAllot;

    @ApiModelProperty(value = "允许门店订货")
    private Integer isStoreOrder;

    @ApiModelProperty(value = "允许要货配货")
    private Integer isDis;

    @ApiModelProperty(value = "允许配货退货")
    private Integer isDisRe;

    @ApiModelProperty(value = "允许铺货")
    private Integer isFirstOrder;

    @ApiModelProperty(value = "允许配销上下限跑货")
    private Integer isUpLowLimitDis;

    @ApiModelProperty(value = "允许配销分货")
    private Integer isAllotDis;

    @ApiModelProperty(value = "允许批发订货")
    private Integer isWhslOrder;

    @ApiModelProperty(value = "允许集货配销")
    private Integer isDisDis;

    @ApiModelProperty(value = "允许配销退货")
    private Integer isDisReDis;

    @ApiModelProperty(value = "允许配销铺货")
    private Integer isFirstOrderDis;

    @ApiModelProperty(value = "允许配销订货")
    private Integer isStoreOrderDis;

    @ApiModelProperty(value = "允许销售")
    private Integer isSales;

    @ApiModelProperty(value = "允许门店库存调整")
    private Integer isStoreInvAdj;

    @ApiModelProperty(value = "允许仓储库存调整")
    private Integer isWrhInvAdj;

    @ApiModelProperty(value = "允许仓储报损")
    private Integer isWrhLoss;

    @ApiModelProperty(value = "允许门店报损")
    private Integer isStoreLoss;

    @ApiModelProperty(value = "允许门店调拨")
    private Integer isStoreTrf;

    @ApiModelProperty(value = "允许仓储移库")
    private Integer isWrhMove;

    @ApiModelProperty(value = "允许仓储调拨")
    private Integer isWrhTrf;

    @ApiModelProperty(value = "允许门店领用")
    private Integer isStoreRx;

    @ApiModelProperty(value = "允许门店加工-计划性")
    private Integer isStoreProcess;

    @ApiModelProperty(value = "允许仓储加工")
    private Integer isWrhProcess;

    @ApiModelProperty(value = "允许仓储领用")
    private Integer isWrhRx;

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

    /** 允许批发出货 */
    @ApiModelProperty(name = "isWholesaleOut", value = "允许批发出货")
    private Integer isWholesaleOut;

    /** 允许批发退货 */
    @ApiModelProperty(name = "isWholesaleReturn", value = "允许批发退货")
    private Integer isWholesaleReturn;


    public GoodsStatusBusinessSwitch(Integer goodsStatusId, int isDelete) {
        this.goodsStatusId = goodsStatusId;
        this.isDelete = isDelete;
    }
}
