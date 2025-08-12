package com.edc.erp.common.model.out.store;

import com.edc.plugins.common.model.BaseEntity;
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
 * 门店配送信息
 * </p>
 *
 * @author gusiyuan
 * @since 2024-09-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sc_store_delivery")
public class StoreDelivery extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "配送id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deliveryId;

    @ApiModelProperty(value = "门店id")
    private Integer storeId;

    @ApiModelProperty(value = "门店代码")
    private String storeCode;

    @ApiModelProperty(value = "配送周期类型（常温统配，低温中转），字段对应之前的订单流中字段：order_period")
    private String deliveryType;

    @ApiModelProperty(value = "配送周期（日配，双日配）")
    private String deliveryCycle;

    @ApiModelProperty(value = "门店收货窗口")
    private String receivingWindow;

    @ApiModelProperty(value = "日配周期（135）")
    private String deliveryDailyCycle;

    @ApiModelProperty(value = "配货优先级")
    private String deliveryPriority;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改者")
    private String updater;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

    public StoreDelivery(Integer storeId, Integer isDelete) {
        this.storeId = storeId;
        this.isDelete = isDelete;
    }
}
