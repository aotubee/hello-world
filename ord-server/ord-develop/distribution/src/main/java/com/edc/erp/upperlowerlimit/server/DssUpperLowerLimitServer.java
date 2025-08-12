package com.edc.erp.upperlowerlimit.server;

import com.edc.erp.common.model.out.DssOrderInfoOut;

import java.util.List;
import java.util.Map;

public interface DssUpperLowerLimitServer {
    void dssReplenishmentOrderJob(Map<String, List<DssOrderInfoOut>> storeDssOrderGoodsMap, String bizOrgCode);
}
