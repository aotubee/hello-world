package com.edc.erp.common.service.impl;

import com.edc.erp.common.model.out.fl.DeliveryNumberVO;
import com.edc.erp.common.rpc.WmsClient;
import com.edc.erp.common.service.WmsService;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lishaobo
 */
@Service
@Slf4j
public class WmsServiceImpl implements WmsService {

    @Resource
    private WmsClient wmsClient;

    @Override
    public Response<DeliveryNumberVO> getDeliveryNumberByDeliveryNo(String deliveryNo) {
        Response<DeliveryNumberVO> listResponse = wmsClient.getDeliveryNumberByNo(deliveryNo);
        if (!listResponse.isSuccess()) {
            log.error("获取物流箱数信息出错，错误信息是--" + listResponse.getMessage());
            return Response.error("获取物流箱数失败");
        }
        return Response.data(listResponse.getData());
    }
}
