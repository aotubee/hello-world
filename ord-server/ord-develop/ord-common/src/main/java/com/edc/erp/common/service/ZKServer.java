package com.edc.erp.common.service;

import com.edc.erp.common.model.in.zk.ZKWholesaleReturnBackIn;
import com.edc.erp.common.model.in.zk.ZKWholesaleShipmentBackIn;
import com.edc.erp.common.model.in.zk.ZKDeliveryOrderIn;
import com.edc.erp.common.model.in.zk.ZKReturnOrderIn;
import com.edc.erp.common.model.out.zk.ZkGoodsOut;
import com.edc.plugins.common.response.Response;

public interface ZKServer {

    ZkGoodsOut getZkGoodsByGoodsCodeAndBizOrgCode(String goodsCode, String bizOrgCode);


    Response<String> sendDeliveryOrderToZk(ZKDeliveryOrderIn zkDeliveryOrderIn);

    Response<String> sendUniReOrderToZk(ZKReturnOrderIn zkReturnOrderIn);

    Response<String> sendWholesaleShipmentBack(ZKWholesaleShipmentBackIn ZKWholesaleShipmentBackIn);

    Response<String> sendWholesaleReturnBack(ZKWholesaleReturnBackIn zkWholesaleReturnBackIn);

}
