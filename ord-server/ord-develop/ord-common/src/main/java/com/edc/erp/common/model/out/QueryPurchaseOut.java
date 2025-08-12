package com.edc.erp.common.model.out;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName QueryPurchaseInOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/7/22 17:12
 **/
@Data
public class QueryPurchaseOut implements Serializable {
    private static final long serialVersionUID = -5816394037158556688L;

    private String goodsCode;

    private String purchaseNo;

    private String validityCode;
}
