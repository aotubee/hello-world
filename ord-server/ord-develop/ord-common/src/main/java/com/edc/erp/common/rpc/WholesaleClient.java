package com.edc.erp.common.rpc;

import com.edc.erp.common.model.in.returns.ClientWholesaleConfigIn;
import com.edc.erp.common.model.out.returns.ClientWholesaleConfigOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 批发业务远程调用接口
 *
 * @author wanglidong
 * @Since 2022/10/26 19:56
 */
@FeignClient(name = "mdm", contextId = "WholesaleClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface WholesaleClient {

   /**
     * 查询当前组织下的批发客户价格组配置信息
     * @param clientWholesaleConfigIn
     * @return
     */
    @PostMapping("/csr/clientWholesaleConfig/findByPage")
    public Response<Page<ClientWholesaleConfigOut>> findWholesalePriceGroup(@RequestBody ClientWholesaleConfigIn clientWholesaleConfigIn);

    /**
     * 查询客户状态
     *
     * @param clientCode
     * @param bizOrgCode
     * @return
     */
    @GetMapping("/csr/clientWholesaleConfig/selectClientStatus")
    public Response selectClientStatus(@RequestParam("clientCode") String clientCode,@RequestParam("bizOrgCode") String bizOrgCode);
}
