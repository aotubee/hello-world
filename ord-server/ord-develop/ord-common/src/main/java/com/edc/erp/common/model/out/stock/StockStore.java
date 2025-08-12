package com.edc.erp.common.model.out.stock;


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
 * 0re)实体类
 *
 * @author wangzihang
 * @since 2022-10-20 22:04:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "inv_stock_store")
@ApiModel(value = "StockStore", description = "门店实时库存")
public class StockStore implements Serializable {

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
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 财务库存
     */
    @ApiModelProperty(name = "financialQty", value = "财务库存")
    private BigDecimal financialQty;

    /**
     * 占用库存
     */
    @ApiModelProperty(name = "takeQty", value = "占用库存")
    private BigDecimal takeQty;

    /**
     * 业务库存
     */
    @ApiModelProperty(name = "businessQty", value = "业务库存")
    private BigDecimal businessQty;

    /**
     * 库存价格
     */
    @ApiModelProperty(name = "stockPrice", value = "库存价格")
    private BigDecimal stockPrice;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "creatTime", value = "创建时间")
    private LocalDateTime creatTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(name = "updateTime", value = "更新时间")
    private LocalDateTime updateTime;

}
