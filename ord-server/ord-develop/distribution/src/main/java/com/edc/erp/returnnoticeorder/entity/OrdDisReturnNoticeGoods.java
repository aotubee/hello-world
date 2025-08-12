package com.edc.erp.returnnoticeorder.entity;


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
import java.math.BigDecimal;


/**
 * 配销退货通知单与商品表(DisReturnNoticeGoods)实体类
 *
 * @author yaojinpeng
 * @since 2022-10-28 11:16:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_return_notice_goods")
@ApiModel(value = "DisReturnNoticeGoods", description = "配销退货通知单与商品表")
public class OrdDisReturnNoticeGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 配销退货通知单主键
     */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "配销退货通知单主键")
    private Integer returnNoticeOrderId;

    /**
     * 组织商品id
     */
    @ApiModelProperty(name = "orgGoodsId", value = "组织商品id")
    private Integer orgGoodsId;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 商品主图
     */
    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;

    /**
     * 行号
     */
    @ApiModelProperty(name = "line", value = "行号")
    private BigDecimal line;

    @ApiModelProperty(name = "specification", value = "规格")
    private String specification;

    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    @ApiModelProperty(name = "sort", value = "品类代码")
    private String sort;

    @ApiModelProperty(name = "sortName", value = "品类名称")
    private String sortName;

    @ApiModelProperty(name = "brand", value = "品牌")
    private String brand;

    @ApiModelProperty(name = "expiry", value = "效期码")
    private String expiry;

}
