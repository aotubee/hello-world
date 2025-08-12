package com.edc.erp.presale.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.edc.erp.common.util.BigDecimalAmountConverter;
import com.edc.erp.common.util.BigDecimalConverter;
import com.edc.erp.common.util.LocalDateTimeConverter;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配销单导出
 *
 * @author wei
 */
@Data
public class ExcelPresaleAssetsInfo {

    @ExcelProperty(value = "序号", index = 0)
    private Integer index;

    /**
     * 配销单号
     */
    @ExcelProperty(value = "门店代码", index = 1)
    private String storeCode;

    /**
     * 配销单状态
     */
    @ExcelProperty(value = "门店名称", index = 2)
    private String storeName;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "presaleActivityNo", value = "活动单号")
    private String presaleActivityNo;

    /**
     * 创建人
     */
    @ApiModelProperty(name = "statusStr", value = "活动单号")
    private String statusStr;

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
     * 包装规格数
     */
    @ApiModelProperty(name = "packageSpecificationNum", value = "包装规格数")
    private BigDecimal packageSpecificationNum;

    /**
     * 包装单位
     */
    @ApiModelProperty(name = "packageUnit", value = "包装单位")
    private String packageUnit;

    /**
     * 包装数
     */
    @ApiModelProperty(name = "packageQuantity", value = "已订包装数")
    private BigDecimal packageQuantity;

    /**
     * 已订货量
     */
    @ApiModelProperty(name = "orderQuantity", value = "已订货量")
    private BigDecimal orderQuantity;

    /**
     * 剩余订货量
     */
    @ApiModelProperty(name = "surplusQuantity", value = "剩余订货量")
    private BigDecimal surplusQuantity;




    /**
     * 可订货开始时间
     */
    @ApiModelProperty(name = "beginOrderDate", value = "可订货开始时间")
    private LocalDateTime beginOrderDate;

    /**
     * 可订货结束时间
     */
    @ApiModelProperty(name = "endOrderDate", value = "可订货结束时间")
    private LocalDateTime endOrderDate;

}
