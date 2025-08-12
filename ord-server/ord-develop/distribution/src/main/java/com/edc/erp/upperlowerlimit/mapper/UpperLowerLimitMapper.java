package com.edc.erp.upperlowerlimit.mapper;

import com.edc.erp.upperlowerlimit.model.out.InvStockStoreOut;
import com.edc.erp.upperlowerlimit.model.out.ReplenishmentConfig;
import com.edc.erp.upperlowerlimit.model.out.ReplenishmentStoreDisabledRange;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 查询生效的上限结果
 * @author wuke
 */
@Repository
public interface UpperLowerLimitMapper {
    /**
     * 查询生效的上限结果
     *
     * @param bizOrgCode
     * @param storeCode
     * @return
     */
    List<ReplenishmentConfig> findReplenishmentConfigList(@Param("bizOrgCode") String bizOrgCode,@Param("storeCode")String storeCode);

    /**
     *查询补货禁用门店
     * @param storeDisabledRange
     * @return
     */
    List<ReplenishmentStoreDisabledRange> isReplenishmentStoreDisabled(ReplenishmentStoreDisabledRange storeDisabledRange);

    /**
     * 获取门店库存
     * @param bizOrgCode
     * @param storeCode
     * @param goodsCodesList
     * @return
     */
    List<InvStockStoreOut> findStoreBizInvQtyList(@Param("bizOrgCode") String bizOrgCode,@Param("storeCode")  String storeCode,@Param("goodsCodesList")  List<String> goodsCodesList);
}
