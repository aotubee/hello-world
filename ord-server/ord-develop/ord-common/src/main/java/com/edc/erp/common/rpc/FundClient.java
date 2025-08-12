package com.edc.erp.common.rpc;

import com.edc.erp.common.model.in.fund.*;
import com.edc.erp.common.model.out.fund.ForeignAccountFundOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 查询资管中心接口
 * @author weichao
 */
@FeignClient(name ="fundserver",contextId ="FundClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface FundClient {
    /**
     * 校验账户资金
     * @param foreignAccountFundIn
     * @return
     */
    @PostMapping("/pay/fund/accountFund/getAvailableAmount")
    Response<ForeignAccountFundOut> getAvailableAmount(@RequestBody ForeignAccountFundIn foreignAccountFundIn);

    /**
     * 账户资金变动
     * @param rechargeLiquidationIn
     * @return
     */
    @PostMapping("/pay/fund/liquidation/settlement")
    Response settlement(@RequestBody RechargeLiquidationIn rechargeLiquidationIn);

    /**
     * 批量解冻原单，批量冻结新单
     * @param unFrozenAndFreezeIn
     * @return
     */
    @PostMapping("/pay/fund/accountFund/unFrozenAndFrozen")
    Response unFrozenAndFrozen(@RequestBody UnFrozenAndFreezeIn unFrozenAndFreezeIn);

    @PostMapping("/pay/fund/accountFund/unFrozen")
    Response unFrozen(@RequestBody UnFrozenIn unFrozenIn);

    @PostMapping("/pay/fund/accountFund/frozen")
    Response frozen(@RequestBody StoreFrozenIn storeFrozenIn);

    @PostMapping("/pay/fund/accountFund/unFrozenAndSettlement")
    Response unFrozenAndSettlement(@RequestBody UnFrozenAndPayIn unFrozenAndPayIn);
}
