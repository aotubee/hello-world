package com.edc.erp.disfirstorder.service;

import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.disdeliveryorder.model.in.OrdDisDeliveryIn;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.model.in.InsertFirstOrderDetailIn;
import com.edc.erp.disfirstorder.model.in.OrdDisFirstOrderAuditIn;
import com.edc.erp.disfirstorder.model.in.OrdDisOrderFirstIn;
import com.edc.erp.disfirstorder.model.out.OrdDerDisOrderFirstOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 配销铺货单(OrdDisOrderFirst)表服务接口
 *
 * @author weichao
 * @since 2022-10-10 16:18:11
 */
public interface OrdDisOrderFirstService extends BaseService<OrdDisOrderFirst> {
    /**
     * 获取首单铺货订单列表
     * @param ordDisOrderFirstIn
     * @return
     */
    Page<OrdDerDisOrderFirstOut> findFirstOrderPage(OrdDisOrderFirstIn ordDisOrderFirstIn);

    /**
     * 获取铺货单详情
     * @param ordDisOrderFirstId
     * @return
     */
    OrdDerDisOrderFirstOut getFirstOrderOut(Long ordDisOrderFirstId);

//    /**
//     * 审核首单铺货订单
//     * @param insertFirstOrderDetailIn
//     * @return
//     */
//    Response<OrdDisOrderFirst> auditFirstOrder(InsertFirstOrderDetailIn insertFirstOrderDetailIn);

    /**
     * 作废首单铺货订单
     * @param firstOrder
     * @return
     */
    int invalidFirstOrder(OrdDisOrderFirst firstOrder);

    /**
     * 铺货单校验资金
     * @param firstOrderId
     * @return
     */
    boolean checkAvailableBalance(Long firstOrderId);

    /**
     * 新增铺货单
     * @param firstOrder
     * @return
     */
    Long insertFirstOrder(OrdDisOrderFirst firstOrder);

    /**
     * 铺货单拆单
     * @param firstOrder
     * @return
     */
    Response<List<OrdDisDeliveryIn>> spiltFirstOrderByConfig(OrdDisOrderFirst firstOrder);

    /**
     * 校验商品信息
     * @param goodsCode
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    Response<OrderGoodsOut> checkOrderGoods(String goodsCode, String storeCode, String bizOrgCode);

    /**
     * 调用资管
     * @param firstOrder
     */
    void toFund(OrdDisOrderFirst firstOrder);

    OrdDisOrderFirst getOrdDisFirstByDeliveryOrderId(Long deliveryOrderId);


    @Transactional(rollbackFor = Exception.class)
    OrdDisOrderFirst getOrdDisOrderFirstForImport(Long firstOrderId, StoreOut storeOut, String loginUsername, Integer isEffectiveImmediately, LocalDateTime effectiveTime);

    Response<Long> asyncImportFirstOrderDetail(String fileId, OrdDisOrderFirst ordDisOrderFirst, String loginUsername);

    Response<String> auditFirstOrder(OrdDisFirstOrderAuditIn ordDisFirstOrderAuditIn, String loginUsername);

    @Transactional(rollbackFor = Exception.class)
    Long createDirFirstOrder(OrdDisOrderFirst ordDisOrderFirst);

    int unFreezeDisFirstOrder(OrdDisOrderFirst ordDisOrderFirst);

    OrdDisOrderFirst getNeedUnFreezeOne(Long id);
}
