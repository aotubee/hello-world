package com.edc.erp.wholesale.model.out;

import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.wholesale.model.in.ApiWholesaleOrderIn;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @ClassName ReadyHandleWholesaleApiOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/10 17:26
 **/
@Data
public class ReadyHandleWholesaleApiOut implements Serializable {
    private static final long serialVersionUID = 4167915261411275442L;

    private ApiWholesaleOrderIn apiWholesaleOrderIn;

    private ClientDistInfoOut clientDistInfoOut;

    private Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap;

    private String oldWholesaleShipmentNo;


}
