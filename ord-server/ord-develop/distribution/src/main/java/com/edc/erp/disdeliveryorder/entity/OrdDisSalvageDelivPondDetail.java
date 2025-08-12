package com.edc.erp.disdeliveryorder.entity;


import com.edc.plugins.common.model.BaseEntity;
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
import java.time.LocalDateTime;


/**
 * 配销捞单池明细(OrdDisSalvageDelivPondDetail)实体类
 *
 * @author zy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_salvage_deliv_pond_detail")
@ApiModel(value = "OrdDisSalvageDelivPondDetail", description = "配销捞单池明细")
public class OrdDisSalvageDelivPondDetail extends BaseEntity {

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配销捞单池主键id
     */
    @ApiModelProperty(name = "salvagePondId", value = "配销捞单池主键id")
    private Long salvagePondId;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 第一笔订单支付时间
     */
    @ApiModelProperty(name = "firstOrderTime", value = "第一笔订单支付时间")
    private LocalDateTime firstOrderTime;

    /**
     * 配货单id
     */
    @ApiModelProperty(name = "deliveryOrderId", value = "配货单id")
    private Long deliveryOrderId;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 仓储代码
     */
    @ApiModelProperty(name = "wrhCode", value = "仓储代码")
    private String wrhCode;

    /**
     * 分配规则
     */
    @ApiModelProperty(name = "allotRule", value = "分配规则")
    private String allotRule;

    /**
     * 分配规则
     */
    @ApiModelProperty(name = "auditType", value = "审核类型")
    private String auditType;

    /**
     * 常温配货优先级
     */
    @ApiModelProperty(value = "常温配货优先级")
    private Integer roomPry;

    /**
     * 冷冻配货优先级
     */
    @ApiModelProperty(value = "冷冻配货优先级")
    private Integer frozenPry;

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
