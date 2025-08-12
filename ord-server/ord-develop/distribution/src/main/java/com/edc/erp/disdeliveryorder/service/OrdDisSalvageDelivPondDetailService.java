package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisSalvageDelivPondDetail;
import com.edc.erp.disdeliveryorder.model.in.UpdateStockDisDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.model.out.OrdDisSalvageDelivPondDetailOut;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.reducestock.model.out.stock.OperationStockOut;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrdDisSalvageDelivPondDetailService extends BaseService<OrdDisSalvageDelivPondDetail> {

    @Transactional(rollbackFor = Exception.class)
    void saveOrdDisSalvageDelivPondDetails(List<OrdDisDelivery> ordDisDeliveryList, OrdDisOrderCycle orderCycle,
                                           String loginUsername, String auditType, StoreLogisticsOut logistics);

    /**
     * 手动创建配销单保存捞单池明细
     *
     * @param ordDisDelivery
     */
    void saveForManualCreateDeliveryOrder(OrdDisDelivery ordDisDelivery, String auditType);

    List<OrdDisSalvageDelivPondDetailOut> findListBySalvagePondId(Long salvagePondId);

    /**
     * 处理捞单占用库存
     *
     * @param bizOrgCode
     * @param loginUsername
     * @param ordDisSalvageDelivPondDetailOutList
     * @return
     */
    List<OperationStockOut> handleSalvage(String bizOrgCode, String loginUsername, Boolean isRecalculateOccupancyQty, List<OrdDisSalvageDelivPondDetailOut> ordDisSalvageDelivPondDetailOutList);

    /**
     * 封装配货单占库存后数据
     *
     * @param operationStockOutList
     * @param loginUsername
     * @param bizOrgCode
     * @return
     */
    List<UpdateStockDisDeliveryOrderIn> initUpdateStockDeliveryOrderInList(List<OperationStockOut> operationStockOutList, String loginUsername, String bizOrgCode);

    OrdDisSalvageDelivPondDetail getOneByDeliveryOrderId(Long deliveryOrderId);


    List<OrdDisSalvageDelivPondDetailOut> findByDeliveryOrderIdList(List<Long> deliveryOrderIdList);

    List<Long> findDeliveryOrderIdListBySalvagePondId(Long salvagePondId);

}
