package com.edc.erp.common.rpc;

import com.edc.erp.common.entity.OrderProcessConfigItem;
import com.edc.erp.common.model.out.OrderProcessOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *订单类型流程选项配置
 *
 * @author w
 */
@FeignClient(name ="mdm",contextId ="orderProcessConfigItemClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface OrderProcessConfigItemClient {
    /**
     * 根据入参查询订单类型流程选项
     * @param orderTypeConfigId
     * @param processCode
     * @param processCodeConfigCode
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "根据入参查询订单类型流程选项")
    @GetMapping("/opt/orderProcessConfigItem/findTheOrderProcessConfigOut")
     Response<List<OrderProcessConfigItem>> findTheOrderProcessConfigOut(@RequestParam("orderTypeConfigId") Long orderTypeConfigId,
                                                                         @RequestParam("processCode")String processCode,
                                                                         @RequestParam("processCodeConfigCode")String processCodeConfigCode,
                                                                         @RequestParam("bizOrgCode")String bizOrgCode);

    /**
     * 根据订单类型ID查询订单类型流程选项
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "根据订单类型ID查询订单类型流程选项")
    @GetMapping("/opt/orderProcess/findProcessOutListByTypeConfigIdAndOrg")
    Response<List<OrderProcessOut>> findProcessOutListByTypeConfigIdAndOrg(@RequestParam("orderTypeConfigId") Long orderTypeConfigId, @RequestParam("bizOrgCode") String bizOrgCode);
}
