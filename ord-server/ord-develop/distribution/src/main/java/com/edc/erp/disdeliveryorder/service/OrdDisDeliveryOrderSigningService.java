package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderSigning;
import com.edc.erp.disdeliveryorder.model.in.CacheTakeDisDeliveryOrderGoodsIn;
import com.edc.erp.disdeliveryorder.model.in.DisSignDeliveryOrderIn;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 配销单签收(OrdDisDeliveryOrderSigning)表服务接口
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
public interface OrdDisDeliveryOrderSigningService extends BaseService<OrdDisDeliveryOrderSigning> {

    /**
     * 查询配销单签收表信息
     *
     * @param disDeliveryOrderId
     * @param bizOrgCode
     * @return
     */
    OrdDisDeliveryOrderSigning getDeliveryOrderSigningByDeliveryOrderId(Long disDeliveryOrderId, String bizOrgCode);

    /**
     * 收货签到
     *
     * @param signDeliveryOrderIn
     * @param deliveryOrder
     * @param loginUsername
     * @return
     */
    Response<String> signDisDeliveryOrder(DisSignDeliveryOrderIn signDeliveryOrderIn, OrdDisDelivery deliveryOrder, String loginUsername);

    /**
     * 提交收货前高值整件缓存数据
     *
     * @param deliveryOrder
     * @param cacheTakeDisDeliveryOrderGoodsInList
     * @return
     */
    Response<String> submitBeforeTakeDisDeliveryInfoToCache(OrdDisDelivery deliveryOrder, List<CacheTakeDisDeliveryOrderGoodsIn> cacheTakeDisDeliveryOrderGoodsInList);

}
