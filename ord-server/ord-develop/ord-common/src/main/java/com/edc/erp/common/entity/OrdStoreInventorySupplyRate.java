package com.edc.erp.common.entity;


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
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 配销单商品库存满足率实体类
 *
 * @author zy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_store_cycle_inventory_supply_rate")
@ApiModel(value = "OrdDirInventorySupplyRate", description = "门店要货库存满足率")
public class OrdStoreInventorySupplyRate extends BaseEntity {

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 要货周期时间
     */
    @ApiModelProperty(name = "orderCycleTime", value = "要货周期时间")
    private LocalDateTime orderCycleTime;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 配货单主键
     */
    @ApiModelProperty(name = "deliveryOrderId", value = "配货单主键")
    private Long deliveryOrderId;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 库存满足率
     */
    @ApiModelProperty(name = "supplyRate", value = "库存满足率")
    private BigDecimal supplyRate;

    /**
     * 初始分配库存
     */
    @ApiModelProperty(name = "beginInventoryQuantity", value = "初始分配库存")
    private BigDecimal beginInventoryQuantity;

    /**
     * 库存分配量
     */
    @ApiModelProperty(name = "allocationQuantity", value = "库存分配量")
    private BigDecimal allocationQuantity;

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


}
