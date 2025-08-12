package com.edc.erp.common.model.in;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName QueryPurchaseIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/7/22 17:09
 **/
@Data
public class QueryPurchaseIn implements Serializable {

    private String goodsCode;

    private String purchaseNo;
}
