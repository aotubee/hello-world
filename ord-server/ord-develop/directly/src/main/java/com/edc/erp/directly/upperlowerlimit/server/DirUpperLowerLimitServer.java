package com.edc.erp.directly.upperlowerlimit.server;
import com.edc.erp.directly.upperlowerlimit.model.out.ReplenishmentConfig;
import com.edc.erp.directly.upperlowerlimit.model.out.ReplenishmentStoreDisabledRange;

import java.util.List;

/**
 * 上下限跑货
 * @author weichao
 */
public interface DirUpperLowerLimitServer {
    /**
     *查询生效的上限结果
     * @param bizOrgCode
     * @param storeCode
     * @return
     */
   List<ReplenishmentConfig> findReplenishmentConfigList(String bizOrgCode, String storeCode);

    /**
     * 跑货生成订货单
     *
     * @param list
     * @param bizOrgCode
     */
    void replenishmentOrderJob(List<String>list,String bizOrgCode);

    /**
     * 查询补货禁用门店
     * @param key
     * @param orgCode
     * @return
     */
    List<ReplenishmentStoreDisabledRange> isReplenishmentStoreDisabled(String key, String orgCode);
}
