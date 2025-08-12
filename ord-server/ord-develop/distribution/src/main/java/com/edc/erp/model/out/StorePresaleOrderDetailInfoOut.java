package com.edc.erp.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName StorePresaleOrderInfoOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/30 15:09
 **/
@Data
public class StorePresaleOrderDetailInfoOut implements Serializable {
    private static final long serialVersionUID = 2552110065425516978L;
    // /**
    //  * 主键
    //  */
    // @ApiModelProperty(name = "id", value = "主键")
    // private Long id;
    // /**
    //  * 商品代码
    //  */
    // @ApiModelProperty(name = "presaleOrderId", value = "预售订单id")
    // private Long presaleOrderId;
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
     * 商品图片
     */
    @ApiModelProperty(name = "goodsImage", value = "商品图片")
    private String goodsImage;
    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    // /**
    //  * 商品代码
    //  */
    // @ApiModelProperty(name = "presaleActivityId", value = "商品代码")
    // private Long presaleActivityId;
    /**
     * 预售活动号
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "预售活动号")
    private String presaleActivityNo;

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
     * 配销价
     */
    @ApiModelProperty(name = "distributionPrice", value = "配销价")
    private BigDecimal distributionPrice;
    /**
     * 配销金额
     */
    @ApiModelProperty(name = "distributionAmount", value = "配销金额")
    private BigDecimal distributionAmount;
    /**
     * 应付金额
     */
    @ApiModelProperty(name = "payAmount", value = "应付金额")
    private BigDecimal payAmount;
    /**
     * 包装规格数
     */
    @ApiModelProperty(name = "packageSpecificationNum", value = "包装规格数")
    private Integer packageSpecificationNum;
    /**
     * 包装数
     */
    @ApiModelProperty(name = "packageQuantity", value = "包装数")
    private BigDecimal packageQuantity;
    /**
     * 订货数量
     */
    @ApiModelProperty(name = "goodsQuantity", value = "订货数量")
    private BigDecimal goodsQuantity;
    /**
     * 是否赠品
     */
    @ApiModelProperty(name = "isGift", value = "是否赠品")
    private Integer isGift;

    @ApiModelProperty(name = "giftStr", value = "赠品Str")
    private String giftStr;
}
