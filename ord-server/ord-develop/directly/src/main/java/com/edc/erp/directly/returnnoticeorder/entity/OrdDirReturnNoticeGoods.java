package com.edc.erp.directly.returnnoticeorder.entity;


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
 * 退货通知单与商品表(OrdDirReturnNoticeGoods)实体类
 *
 * @author
 * @since 2022-11-18 19:09:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dir_return_notice_goods")
@ApiModel(value = "OrdDirReturnNoticeGoods", description = "退货通知单与商品表")
public class OrdDirReturnNoticeGoods implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    /** 主键 */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 退货通知单主键 */
    @ApiModelProperty(name = "returnNoticeOrderId", value = "退货通知单主键")
    private Integer returnNoticeOrderId;

    /** 组织商品id */
    @ApiModelProperty(name = "orgGoodsId", value = "组织商品id")
    private Integer orgGoodsId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 商品条码 */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 商品主图 */
    @ApiModelProperty(name = "goodsImage", value = "商品主图")
    private String goodsImage;

    /** 行号 */
    @ApiModelProperty(name = "line", value = "行号")
    private String line;

    /** 品牌 */
    @ApiModelProperty(name = "brand", value = "品牌")
    private String brand;

    /** 分类代码 */
    @ApiModelProperty(name = "sort", value = "分类代码")
    private String sort;

    /** 分类名称 */
    @ApiModelProperty(name = "sortName", value = "分类名称")
    private String sortName;

    /** 配货规格 */
    @ApiModelProperty(name = "specification", value = "配货规格")
    private String specification;

    /** 配货规格 */
    @ApiModelProperty(name = "goodsType", value = "品类属性")
    private String goodsType;

    @ApiModelProperty(name = "expiry", value = "效期码")
    private String expiry;
}
