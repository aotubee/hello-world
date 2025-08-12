package com.edc.erp.common.rpc;

import com.edc.erp.common.model.in.FindGroupIn;
import com.edc.erp.common.model.out.EmpowerGroupBaseDetailOut;
import com.edc.erp.common.model.out.EmpowerGroupDetailOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 授权组
 *
 * @author: gusiyuan
 * @date: 2024-11-12
 */
@FeignClient(name = "mdm", configuration = GlobalFeignErrorDecoderConfiguration.class, contextId = "EmpowerGroupClient")
public interface EmpowerGroupClient {

    /**
     * 根据类型和组获取授权组代码集合
     *
     * @param findGroupIn
     * @return
     */
    @PostMapping(value = "/opt/empGroup/findByTypeAndGroup")
    @ResponseBody
    Response<List<String>> findByTypeAndGroup(@RequestBody FindGroupIn findGroupIn);

    /**
     * 根据组获取授权组代码集合
     *
     * @param findGroupIns
     * @return
     */
    @PostMapping(value = "/opt/empGroup/findByTypeAndGroups")
    @ResponseBody
    Response<List<EmpowerGroupDetailOut>> findByTypeAndGroups(@RequestBody List<FindGroupIn> findGroupIns);

    /**
     * 根据类型和组获取授权组代码集合
     *
     * @param findGroupIn
     * @return
     */
    @PostMapping(value = "/opt/empGroup/findAllByTypeAndGroup")
    @ResponseBody
    Response<List<EmpowerGroupBaseDetailOut>> findAllByTypeAndGroup(@RequestBody FindGroupIn findGroupIn);
}
