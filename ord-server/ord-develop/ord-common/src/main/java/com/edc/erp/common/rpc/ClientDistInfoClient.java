package com.edc.erp.common.rpc;


import com.edc.erp.common.model.in.customer.QueryClientDistInfoIn;
import com.edc.erp.common.model.in.customer.QueryClientInfoIn;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 查询客户配送信息 远程调用
 * @author lx
 * @since 2022-11-02 11:03:47
 */
@FeignClient(name = "mdm", contextId = "ClientDistInfoClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface ClientDistInfoClient {
    /**
     * 查询客户配送信息
     * @param queryClientDistInfoIn 查询客户信息 入参类
     * @return
     */
    @GetMapping("/csr/clientDistributionInfo/findClientDistInfo")
    Response<List<ClientDistInfoOut>> findClientDistInfo(@SpringQueryMap QueryClientDistInfoIn queryClientDistInfoIn);

    @GetMapping("/csr/clientDistributionInfo/getOneByParameter")
    Response<ClientDistInfoOut> getOneByParameter(@SpringQueryMap QueryClientInfoIn queryClientInfoIn);
}
