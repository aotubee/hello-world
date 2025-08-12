package com.edc.erp.directly.upperlowerlimit.server;

import com.edc.erp.common.model.out.DssOrderInfoOut;

import java.util.List;
import java.util.Map;

public interface DssDirUpperLowerLimitServer {
    void dssReplenishmentOrderJob(Map<String, List<DssOrderInfoOut>> storeDssOrderGoodsMap, String bizOrgCode);
}
