package com.edc.erp.presale.entity;


import com.edc.erp.reducestock.common.util.BigDecimalSerialize4;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
import java.time.LocalDateTime;


/**
 * 门店商品效期库存流水实体类
 *
 * @author wangzihang
 * @since 2022-10-20 22:05:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ord_dis_presale_goods_flow")
@ApiModel(value = "OrdDisPresaleGoodsFlow", description = "预售商品流水")
public class OrdDisPresaleGoodsFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(name = "id", value = "主键")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
     * 包装规格
     */
    @ApiModelProperty(name = "packageSpecification", value = "包装规格")
    private String packageSpecification;

    /**
     * 业务类型
     */
    @ApiModelProperty(name = "businessType", value = "业务类型")
    private String businessType;

    /**
     * 业务单号
     */
    @ApiModelProperty(name = "sourceNo", value = "业务单号")
    private String sourceNo;

    /**
     * 数量
     */
    @ApiModelProperty(name = "qty", value = "数量")
    @JsonSerialize(using = BigDecimalSerialize4.class)
    private BigDecimal qty;
    /**
     * 数量平衡
     */
    @ApiModelProperty(name = "qtyBalance", value = "数量平衡")
    @JsonSerialize(using = BigDecimalSerialize4.class)
    private BigDecimal qtyBalance;
    /**
     * 实际增/减
     */
    @ApiModelProperty(name = "actualLowering", value = "实际增/减")
    private String actualLowering;

    /**
     * 流水日期
     */
    @ApiModelProperty(name = "flowDate", value = "流水日期")
    private LocalDateTime flowDate;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    private LocalDateTime updateTime;
    /**
     * 操作人
     */
    @ApiModelProperty(name = "creator", value = "操作人")
    private String creator;

}
