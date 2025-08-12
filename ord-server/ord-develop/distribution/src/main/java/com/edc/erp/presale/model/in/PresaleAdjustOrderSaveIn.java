package com.edc.erp.presale.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 预售调整单保存入参
 */
@Data
@Builder
public class PresaleAdjustOrderSaveIn implements Serializable {
    private static final long serialVersionUID = -9098155688358447411L;
    /**
     * 预售单号
     */
    @ApiModelProperty(name = "orderNo", value = "调整单单号")
    private String orderNo;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店名称
     */
    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    /**
     * 调整类型
     */
    @ApiModelProperty(name = "adjustType", value = "调整类型 add增加 reduce扣减")
    private String adjustType;

    /**
     * 备注
     */
    @ApiModelProperty(name = "remark", value = "备注")
    private String remark;

    /**
     * 原单号
     */
    @ApiModelProperty(name = "sourceNo", value = "原单号")
    private String sourceNo;

    /**
     * 是否冲销单
     */
    @ApiModelProperty(name = "isChargeOrder", value = "是否冲销单")
    private Integer isChargeOrder = 0;

    /**
     * 调整单商品列表
     */
    @ApiModelProperty(name = "details", value = "调整单商品列表")
    private List<PresaleAdjustOrderGoods> details;

    /**
     * 调整单商品
     */
    @Data
    @Builder
    public static class PresaleAdjustOrderGoods implements Serializable {
        /**
         * 商品代码
         */
        @ApiModelProperty(name = "goodsCode", value = "商品代码")
        private String goodsCode;

        /**
         * 商品名称
         */
        @ApiModelProperty(name = "goodsName", value = "商品名称")
        private String goodsName;

        /**
         * 商品条码
         */
        @ApiModelProperty(name = "barCode", value = "商品条码")
        private String barCode;

        /**
         * 调整数量
         */
        @ApiModelProperty(name = "adjustQty", value = "调整数量")
        private BigDecimal adjustQty;

        /**
         * 当前预售数量
         */
        @ApiModelProperty(name = "beforeQty", value = "当前预售数量")
        private BigDecimal beforeQty;

        /**
         * 品类属性
         */
        @ApiModelProperty(name = "goodsType", value = "品类属性")
        private String goodsType;

        /**
         * 包装单位
         */
        @ApiModelProperty(name = "packageUnit", value = "包装单位")
        private String packageUnit;

        /**
         * 包装规格
         */
        @ApiModelProperty(name = "packageSpecification", value = "包装规格")
        private String packageSpecification;

        /**
         * 包装规格数
         */
        @ApiModelProperty(name = "packageSpecificationNum", value = "包装规格数")
        private BigDecimal packageSpecificationNum;

        /**
         * 预售活动号
         */
        @ApiModelProperty(name = "presaleActivityNo", value = "预售活动号")
        private String presaleActivityNo;
    }
}
