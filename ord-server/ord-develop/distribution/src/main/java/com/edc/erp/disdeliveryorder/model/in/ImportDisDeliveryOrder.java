package com.edc.erp.disdeliveryorder.model.in;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 配销单导入实体
 * @since 2022/11/17 15:18
 */
@Data
public class ImportDisDeliveryOrder implements Serializable {
    private static final long serialVersionUID = -608503324008071350L;

    /**
     * 仓储代码
     */
    @ExcelProperty(value = "仓储代码",index = 0)
    private String wrhCode;


    /**
     * 仓位代码
     */
    @ExcelProperty(value = "仓位代码",index = 1)
    private String stockCode;

    /**
     * 门店代码
     */
    @ExcelProperty(value = "门店代码",index = 2)
    private String storeCode;

    /**
     * 配销方式
     */
    @ExcelProperty(value = "配送方式",index = 3)
    private String distributionType;

    /**
     * 商品代码
     */
    @ExcelProperty(value = "商品代码",index = 4)
    private String goodsCode;

    /**
     * 配销数量
     */
    @ExcelProperty(value = "配销数量",index = 5)
    private Integer deliveryQuantity;

    @ExcelProperty(value = "备注",index = 6)
    private String purchaseNo;


    @ExcelProperty(value = "备注",index = 7)
    private String remark;

    /**
     * 备注
     */

    @Transient
    private String centerStockBizOrgCode;


    @Transient
    private String expiry;
}
