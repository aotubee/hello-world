package com.edc.erp.directly.returnorder.model.in;

import com.edc.plugins.common.model.BaseEntity;
import lombok.Data;

import javax.persistence.Transient;
import java.math.BigDecimal;

/**
 * @return: 导入接受参数VO
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
public class ImportOrdReturnOrderVO extends BaseEntity {

    /**
     * 仓储代码
     */
    private String warehouseCode;

    /**
     * 仓位代码
     */
    private String stockCode;

    /**
     * 门店代码
     */
    private String storeCode;

    /**
     * 商品代码
     */
    private String goodsCode;


    /**
     * 申请退货数量
     */
    private BigDecimal applyReturnQuantity;

    private String deliveryOrderNo;

    /**
     * 退货单价
     */
    private BigDecimal returnUnitPrice;

    private String expiry;

    /**
     * 备注
     */
    private String remark;

    @Transient
    private String centerStockBizOrgCode;


}
