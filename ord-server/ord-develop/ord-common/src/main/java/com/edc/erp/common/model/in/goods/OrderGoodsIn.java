/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.in.goods;


import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 订单商品信息出参
 *
 * @author weichao
 */
@Data
public class OrderGoodsIn extends Page implements Serializable {

    private static final long serialVersionUID = -1152465598931359056L;
    /**
     * 品类
     */
    @ApiModelProperty(name = "sortName", value = "品类")
    private String sortName;

    /**
     * 品类代码
     */
    @ApiModelProperty(name = "sort", value = "品类代码")
    private String sort;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 商品id
     */
    @ApiModelProperty(name = "goodsId", value = "商品id")
    private Integer goodsId;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    @NotBlank
    private String orgCode;

    /**
     *业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    @NotBlank
    private String bizOrgCode;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 供应商代码
     */
    @ApiModelProperty(name = "vendorCode", value = "供应商代码")
    private String vendorCode;

    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /**
     * 品牌
     */
    @ApiModelProperty(name = "brand", value = "品牌")
    private String brand;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 产品代码
     */
    @ApiModelProperty(name = "produceCode", value = "产品代码")
    private String produceCode;

    /**
     * 搜索参数
     */
    @ApiModelProperty(name = "searchParam", value = "搜索参数")
    private String searchParam;

    /**
     * 属性代码
     */
    @ApiModelProperty(name = "tagCode", value = "属性代码")
    private String tagCode;

    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    /**
     * 门店Id
     */
    @ApiModelProperty(name = "storeId", value = "门店Id")
    private Integer storeId;

    /**
     * 活动类型
     */
    @ApiModelProperty(name = "activityType", value = "活动类型")
    private String activityType;

    /**
     * sku集合
     */
    @ApiModelProperty(name = "goodsCodeList", value = "商品代码集合")
    private List<String> goodsCodeList;

    /**
     * 商品状态
     */
    @ApiModelProperty(name = "busGate", value = "商品状态")
    private Integer busGate;

    @ApiModelProperty(name = "busGate", value = "查询开始时间")
    private String beginTime;

    @ApiModelProperty(name = "endTime", value = "查询结束时间")
    private String endTime;

    /**
     * 排序类型
     */
    @ApiModelProperty(name = "orderByType", value = "排序类型(1:全连锁2:本店3:未经营)")
    private String orderByType;

    /**
     * 是否短保
     */
    @ApiModelProperty(name = "isShortWarranty", value = "是否短保")
    private Integer isShortWarranty;


    @ApiModelProperty(name = "storeProperty", value = "门店类型")
    private String storeProperty;

    /**
     * 配送方案代码
     */
    @ApiModelProperty(name = "alcSchemeCode", value = "配送方案代码")
    private String alcSchemeCode;

    /**
     * 门店商品经营方案代码
     */
    @ApiModelProperty(name = "saleSchemeCode", value = "门店商品经营方案代码")
    private String saleSchemeCode;


    @ApiModelProperty(name = "storeUnitId", value = "门店单元id")
    private Integer storeUnitId;

    /**
     * 商品属性
     */
    @ApiModelProperty(name = "goodsAttributeName", value = "商品属性")
    private String goodsAttributeName;

    /**
     * 商品属性代码集合
     */
    @ApiModelProperty(name = "goodsAttributeCodeList", value = "商品属性代码集合")
    private List<String> goodsAttributeCodeList;

    @ApiModelProperty(name = "isShelves", value = "是否下架")
    private Integer isShelves;

    @ApiModelProperty(name="businessType",value ="业务类型" )
    private  String businessType;

    @ApiModelProperty(name="whetherLimitGoodsSortShow",value ="商品是否展示" )
    private  Integer whetherLimitGoodsSortShow;

    /**
     * 退仓Id
     */
    @ApiModelProperty(name = "backId", value = "退仓id")
    private Integer backId;

    /**
     * 退仓代码
     */
    @ApiModelProperty(name = "backPositionCode", value = "退仓代码")
    private String backPositionCode;
    /**
     * 供应商ID
     */
    @ApiModelProperty(name = "vendorId", value = "供应商ID")
    private Integer vendorId;
    /**
     * 来源业务类型
     */
    @ApiModelProperty(name="sourceCode",value ="来源业务类型" )
    private  String sourceCode;

    /**
     * 推荐时间（yyyy-dd-mm）
     */
    @ApiModelProperty(name = "recommendTime", value = "推荐时间（yyyy-dd-mm）")
    private String recommendTime;
    /**
     * 开始时间
     */
    private String beginRecommendTime;
    /**
     * 结束时间
     */
    private String endRecommendTime;

    /**
     * 仓储代码
     *
     */
    private String warehouseCode;

}
