package com.edc.erp.directly.distribution.service;

import com.edc.erp.common.model.out.goods.GoodsSortNodeOut;
import com.edc.erp.common.model.out.store.StoreDelivery;
import com.edc.erp.directly.distribution.entity.OrdDirOrderAllocationPool;
import com.edc.erp.directly.distribution.model.in.QueryAllocationPoolPageIn;
import com.edc.erp.directly.distribution.model.in.UpdateAllocationPoolIn;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderAllocationPoolPageOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrdDirOrderAllocationPoolService extends BaseService<OrdDirOrderAllocationPool> {
    List<String> findStoreCodeList();

    List<OrdDirOrderAllocationPool> findEmptyTruncationDateTimeListByStoreCode(String storeCode);

    @Transactional(rollbackFor = Exception.class)
    void handleSupplementQuantity(OrdDirOrderAllocationPool ordDirOrderAllocationPool, Map<String, StoreDelivery> deliveryTypeMap, Map<String, Map<String, BigDecimal>> truncationTimeGoodsQuantityMap);

    List<OrdDirOrderAllocationPool> findStoreTruncationDateTimeList();

    List<OrdDirOrderAllocationPool> findListByStoreCodeAndTruncationDateTime(String storeCode, LocalDateTime truncationDateTime);

    @Transactional(rollbackFor = Exception.class)
    void toHistory(List<OrdDirOrderAllocationPool> ordDirOrderAllocationPoolList, String storeCode, LocalDateTime truncationDateTime);

    @Transactional(rollbackFor = Exception.class)
    Response<String> importAllocationPoolDetail(String fileId, String loginUsername);

    void batchUpdate(List<UpdateAllocationPoolIn> updateAllocationPoolInList, String loginUsername);

    Page<OrdDirOrderAllocationPoolPageOut> findListForPage(QueryAllocationPoolPageIn queryAllocationPoolPageIn);

    String exportAllocationPool(QueryAllocationPoolPageIn queryAllocationPoolPageIn);

    GoodsSortNodeOut findAllocationPoolSortTree(String bizOrgCode);
}
