package com.edc.erp.common.rpc;

import com.edc.erp.common.model.out.InvBizRsnTransOut;
import com.edc.erp.common.model.out.equipment.EquipmentBusinessReasonOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
* @Description:库存业务原因取值
* @Author: fxw
* @Date: 2022/11/22
*/
@FeignClient(name ="mdm",contextId ="EquipmentBusinessReasonClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface EquipmentBusinessReasonClient {


    /**
     * 查库存原因
     * @param bizRsnCode
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "根据代码查询库存业务原因为翻译用", notes = "根据代码查询库存业务原因为翻译用")
    @GetMapping("/logc/equipmentBusinessReason/getInvBizRsnTransByCode")
     Response<InvBizRsnTransOut> getInvBizRsnTransByCode(@RequestParam String bizRsnCode, @RequestParam(required = false) String bizOrgCode, @RequestParam(required = false)
            Integer isDelete);

    @ApiOperation(value = "根据代码查询库存业务原因为翻译用（门店和大仓库存分离）", notes = "根据代码查询库存业务原因为翻译用（门店和大仓库存分离）")
    @GetMapping("/logc/invStoreBusinessReason/getInvBizRsnTransByCode")
    Response<InvBizRsnTransOut> invStoreBusinessReason(@RequestParam String bizRsnCode, @RequestParam(required = false) String bizOrgCode, @RequestParam(required = false)
    Integer isDelete);


    @ApiOperation(value = "分页获取库存业务原因", notes = "分页获取库存业务原因")
    @GetMapping("/logc/equipmentBusinessReason/page")
    Response<Page<EquipmentBusinessReasonOut>> page(@RequestParam("bizOrgCode")String bizOrgCode,
                                                    @RequestParam("pageNum") Integer pageNum,
                                                    @RequestParam("pageSize")Integer pageSize,
                                                    @RequestParam("businessReasonType")String businessReasonType,
                                                    @RequestParam("businessReasonName")String businessReasonName,
                                                    @RequestParam("businessReasonDimension")String businessReasonDimension
                                                    );
}
