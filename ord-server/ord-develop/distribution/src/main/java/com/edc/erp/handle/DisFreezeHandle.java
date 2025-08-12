package com.edc.erp.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstService;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.handle.DisOrderHandle;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName DisFreezeHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/5/29 16:37
 **/
@Service
@RequiredArgsConstructor
public class DisFreezeHandle {

    private final OrdDisDeliveryService ordDisDeliveryService;

    private final DisOrderHandle disOrderHandle;

    private final OrdDisOrderFirstService ordDisOrderFirstService;

    @Transactional(rollbackFor = Exception.class)
    public void handleOrderConfig(List<Long> deliveryIdList, List<OrdDisOrder> needUnfreezeOrderList, String bizOrgCode) {
        if (CollectionUtils.isNotEmpty(deliveryIdList)) {
            ordDisDeliveryService.batchUpdateFreeze(deliveryIdList, SystemConstant.SYSTEM_USER, bizOrgCode);
        }
        if (CollectionUtils.isNotEmpty(needUnfreezeOrderList)) {
            // 被释放
            disOrderHandle.batchReleaseOrderList(needUnfreezeOrderList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleFirstOrder(List<Long> deliveryIdList, OrdDisOrderFirst ordDisOrderFirst) {
        if (CollectionUtils.isNotEmpty(deliveryIdList)) {
            ordDisDeliveryService.batchUpdateFreeze(deliveryIdList, SystemConstant.SYSTEM_USER, ordDisOrderFirst.getBizOrgCode());
        }
        ordDisOrderFirstService.unFreezeDisFirstOrder(ordDisOrderFirst);
    }
}
