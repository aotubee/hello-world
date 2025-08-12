package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderSigning;
import com.edc.erp.directly.dirdeliveryorder.model.in.CacheTakeDirDeliveryOrderGoodsIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.DirSignDeliveryOrderIn;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 配货单签收(OrdDirDeliveryOrderSigning)表服务接口
 *
 * @author weichao
 * @since 2022-11-21 11:15:37
 */
public interface OrdDirDeliveryOrderSigningService extends BaseService<OrdDirDeliveryOrderSigning> {
    /**
     * 查询配货单签收表信息
     *
     * @param dirDeliveryOrderId
     * @param bizOrgCode
     * @return
     */
    OrdDirDeliveryOrderSigning getDeliveryOrderSigningByDeliveryOrderId(Long dirDeliveryOrderId, String bizOrgCode);

    /**
     * 收货签到
     *
     * @param signDeliveryOrderIn
     * @param deliveryOrder
     * @param loginUsername
     * @return
     */
    Response<String> signDirDeliveryOrder(DirSignDeliveryOrderIn signDeliveryOrderIn, OrdDirDelivery deliveryOrder, String loginUsername);

    /**
     * 提交收货前高值整件缓存数据
     *
     * @param deliveryOrder
     * @param cacheTakeDirDeliveryOrderGoodsInList
     * @return
     */
    Response<String> submitBeforeTakeDirDeliveryInfoToCache(OrdDirDelivery deliveryOrder, List<CacheTakeDirDeliveryOrderGoodsIn> cacheTakeDirDeliveryOrderGoodsInList);
}
