package com.edc.erp.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName DisPresaleOrderInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/30 14:48
 **/
@Data
public class DisStorePresaleOrderPageForAppOut implements Serializable {
    private static final long serialVersionUID = 3283937488379042278L;

    @ApiModelProperty(name = "id", value = "id")
    private Long id;
    /**
     * 预售单号
     */
    @ApiModelProperty(name = "presaleOrderNo", value = "预售订单单号")
    private String presaleOrderNo;
    /**
     * 状态
     */
    @ApiModelProperty(name = "status", value = "状态")
    private String status;

    @ApiModelProperty(name = "statusStr", value = "状态中文")
    private String statusStr;
    /**
     * 品项数
     */
    @ApiModelProperty(name = "totalSkuQty", value = "品项数")
    private Integer totalSkuQty;
    /**
     * 商品数
     */
    @ApiModelProperty(name = "totalGoodsQty", value = "商品数")
    private BigDecimal totalGoodsQty;
    /**
     * 订单金额
     */
    @ApiModelProperty(name = "totalOrderAmount", value = "订单金额")
    private BigDecimal totalOrderAmount;
    /**
     * 优惠金额
     */
    @ApiModelProperty(name = "totalDiscountAmount", value = "优惠金额")
    private BigDecimal totalDiscountAmount;
    /**
     * 应付金额
     */
    @ApiModelProperty(name = "totalPayAmount", value = "应付金额")
    private BigDecimal totalPayAmount;
    // /**
    //  * 支付时间
    //  */
    // @ApiModelProperty(name = "payTime", value = "支付时间")
    // private LocalDateTime payTime;
    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;
    // /**
    //  * 创建人
    //  */
    // @ApiModelProperty(name = "creator", value = "创建人")
    // private String creator;
}
