package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPondDetail;
import com.edc.erp.directly.dirdeliveryorder.model.in.UpdateStockDirDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirSalvageDelivPondDetailOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.reducestock.model.out.stock.OperationStockOut;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrdDirSalvageDelivPondDetailService extends BaseService<OrdDirSalvageDelivPondDetail> {

    @Transactional(rollbackFor = Exception.class)
    void saveOrdDirSalvageDelivPondDetails(List<OrdDirDelivery> ordDirDeliveryList, OrdDirOrderCycle orderCycle, String loginUsername,
                                           String auditType, StoreLogisticsOut logistics);

    List<OrdDirSalvageDelivPondDetailOut> findListBySalvagePondId(Long salvagePondId);

    /**
     * 处理捞单占用库存
     *
     * @param bizOrgCode
     * @param loginUsername
     * @param ordDirSalvageDelivPondDetailOutList
     * @return
     */
    List<OperationStockOut> handleSalvage(String channelBizOrgCode, String loginUsername,Boolean isRecalculateOccupancyQty, List<OrdDirSalvageDelivPondDetailOut> ordDirSalvageDelivPondDetailOutList);

    /**
     * 封装配货单占库存后数据
     *
     * @param operationStockOutList
     * @param loginUsername
     * @param bizOrgCode
     * @return
     */
    List<UpdateStockDirDeliveryOrderIn> initUpdateStockDeliveryOrderInList(List<OperationStockOut> operationStockOutList, String loginUsername, String bizOrgCode);


    /**
     * 手动创建配销单保存捞单池明细
     *
     * @param ordDirDelivery
     */
    void saveForManualCreateDeliveryOrder(OrdDirDelivery ordDirDelivery, String auditType);

    OrdDirSalvageDelivPondDetail getOneByDeliveryOrderId(Long deliveryOrderId);

    List<OrdDirSalvageDelivPondDetailOut> findByDeliveryOrderIdList(List<Long> deliveryOrderIdList);

    List<Long> findDeliveryOrderIdListBySalvagePondId(Long salvagePondId);
}
