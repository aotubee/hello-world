package com.edc.erp.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.model.out.OrdDirOrderTrackOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.DirOrderTrackServer;
import com.edc.erp.common.service.DisOrderTrackServer;
import com.edc.erp.common.service.OrderTrackMessageServer;
import com.edc.erp.common.service.StoreCenterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 订单追踪处理类
 *
 * @author wei
 */
@Slf4j
@Component
public class OrderTrackMessageServerImpl implements OrderTrackMessageServer {

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private DirOrderTrackServer dirOrderTrackServer;

    @Autowired
    private DisOrderTrackServer disOrderTrackServer;

    /**
     * 接收消息
     *
     * @param message
     */
    @Override
    public void receiveMessage(String message) {
        log.info("接收到订单追踪日志消息-----{}", message);
        if (StringUtils.isEmpty(message)) {
            log.info("订单追踪日志新增消息内容为空");
            return;
        }
        OrdDirOrderTrackOut orderTrackOut = JSONObject.toJavaObject(JSON.parseObject(message), OrdDirOrderTrackOut.class);
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(orderTrackOut.getStoreCode());
        if (Objects.isNull(storeOut)) {
            return;
        }
        if (StoreConstant.StoreProperty.FRANCHISE.getMytValue().equals(storeOut.getStoreProperty())) {
            disOrderTrackServer.disReceiveMessage(message);
        } else {
            dirOrderTrackServer.dirReceiveMessage(message);
        }

    }
}
