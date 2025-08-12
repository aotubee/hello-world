package com.edc.erp.common.rpc;

import com.edc.erp.common.model.out.goodsdistributionplan.GoodsDistributionPlanGroupDetailsVendorOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 查询允许批发出货业务的商品信息 远程调用
 * @author lx
 * @since 2022-10-28 10:06:21
 */
@FeignClient(name ="mdm",contextId ="GoodsDistributionPlanGroupDetailsClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface GoodsDistributionPlanGroupDetailsClient {

    /**
     * 查询允许批发出货业务的商品信息
     *
     * @param goodsCode
     * @param bizOrgCode
     * @param alcSchemeCode
     * @return
     */
    @GetMapping("/gc/goodsDistributionPlanGroupDetails/getPlanCodeByGoodsCode")
    Response<GoodsDistributionPlanGroupDetailsVendorOut> getPlanCodeByGoodsCode(@RequestParam String goodsCode,
                                                                              @RequestParam String bizOrgCode, @RequestParam String alcSchemeCode);
}
