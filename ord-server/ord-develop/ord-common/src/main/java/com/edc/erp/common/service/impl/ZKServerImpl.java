package com.edc.erp.common.service.impl;

import com.edc.erp.common.model.in.zk.ZKWholesaleReturnBackIn;
import com.edc.erp.common.model.in.zk.ZKWholesaleShipmentBackIn;
import com.edc.erp.common.model.in.zk.ZKDeliveryOrderIn;
import com.edc.erp.common.model.in.zk.ZKReturnOrderIn;
import com.edc.erp.common.model.out.zk.ZkGoodsOut;
import com.edc.erp.common.rpc.OrderGoodsClient;
import com.edc.erp.common.rpc.ZkClient;
import com.edc.erp.common.service.ZKServer;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName ZKServerImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/10 11:19
 **/
@Service
@Slf4j
public class ZKServerImpl implements ZKServer {

    @Resource
    private OrderGoodsClient orderGoodsClient;

    @Resource
    private ZkClient zkClient;

    @Override
    public ZkGoodsOut getZkGoodsByGoodsCodeAndBizOrgCode(String goodsCode, String bizOrgCode) {
        Response<ZkGoodsOut> response = orderGoodsClient.getZkGoodsByGoodsCodeAndBizOrgCode(goodsCode, bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public Response<String> sendDeliveryOrderToZk(ZKDeliveryOrderIn zkDeliveryOrderIn) {
        return zkClient.sendUniOrderToZk(zkDeliveryOrderIn);
    }

    @Override
    public Response<String> sendUniReOrderToZk(ZKReturnOrderIn zkReturnOrderIn) {
        return zkClient.sendUniReOrderToZk(zkReturnOrderIn);
    }

    @Override
    public Response<String> sendWholesaleShipmentBack(ZKWholesaleShipmentBackIn ZKWholesaleShipmentBackIn) {
        return zkClient.sendWholesaleShipmentBack(ZKWholesaleShipmentBackIn);
    }

    @Override
    public Response<String> sendWholesaleReturnBack(ZKWholesaleReturnBackIn zkWholesaleReturnBackIn) {
        return zkClient.sendWholesaleReturnBack(zkWholesaleReturnBackIn);
    }
}
