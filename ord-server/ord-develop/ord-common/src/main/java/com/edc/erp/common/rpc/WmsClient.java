package com.edc.erp.common.rpc;

import com.edc.erp.common.model.out.fl.DeliveryNumberVO;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zhangyao
 */
@FeignClient(name = "fl", configuration = GlobalFeignErrorDecoderConfiguration.class)
@RequestMapping(value = "/fl")
public interface WmsClient {

    /**
     * 根据单号查询配货单单据明细排序
     * @author: lishaobo
     * @date: 2021-12-16 17:26
     * @param erpDeliveryOrderNo
     * @return
     */
    @GetMapping(value = "/delivery/getDeliveryNumberByNo")
    Response<DeliveryNumberVO> getDeliveryNumberByNo(@RequestParam(value = "erpDeliveryOrderNo") String erpDeliveryOrderNo);

}
