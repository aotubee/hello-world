package com.edc.erp.directly.dirdeliveryorder.model.in;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @author fxw
 * @description: 配货单导入实体
 * @since 2022/11/17 15:18
 */
@Data
public class ImportDirDeliveryOrder implements Serializable {
    private static final long serialVersionUID = -608503324008071350L;

    /**
     * 仓储代码
     */
    private String wrhCode;


    /**
     * 仓位代码
     */
    private String stockCode;

    /**
     * 门店代码
     */
    private String storeCode;

    /**
     * 配货方式
     */
    private String distributionType;

    /**
     * 商品代码
     */
    private String goodsCode;

    /**
     * 配货数量
     */
    private Integer deliveryQuantity;

    private String purchaseNo;

    /**
     * 备注
     */
    private String remark;

    @Transient
    private String centerStockBizOrgCode;

    @Transient
    private String expiry;
}
