package com.edc.erp.directly.dirfirstorder.service;

import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.directly.dirdeliveryorder.model.in.OrdDirDeliveryIn;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.model.in.InsertFirstOrderDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirFirstOrderAuditIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstIn;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDerDirOrderFirstOut;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDirOrderFirstDetailOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 铺货单(OrdDirOrderFirst)表服务接口
 *
 * @author weichao
 * @since 2022-11-10 14:09:10
 */
public interface OrdDirOrderFirstService extends BaseService<OrdDirOrderFirst> {
    /**
     * 获取首单铺货订单列表
     *
     * @param ordDirOrderFirstIn
     * @return
     */
    Page<OrdDerDirOrderFirstOut> findFirstOrderPage(OrdDirOrderFirstIn ordDirOrderFirstIn);

    /**
     * 获取首单铺货订单
     * @param ordDirOrderFirstId
     * @return
     */
    OrdDerDirOrderFirstOut getFirstOrderOut(Long ordDirOrderFirstId);

    /**
     * 新增铺货单
     * @param firstOrder
     * @return
     */
    Long insertFirstOrder(OrdDirOrderFirst firstOrder);

    /**
     * 作废铺货单
     * @param firstOrder
     * @return
     */
    int invalidFirstOrder(OrdDirOrderFirst firstOrder);

//    /**
//     * 审核铺货单
//     * @param insertFirstOrderDetailIn
//     * @return
//     */
//    Response<OrdDirOrderFirst> auditFirstOrder(InsertFirstOrderDetailIn insertFirstOrderDetailIn);

    /**
     * 铺货单拆单
     * @param firstOrder
     * @return
     */
    Response<List<OrdDirDeliveryIn>> spiltFirstOrderByConfig(OrdDirOrderFirst firstOrder);

    /**
     * 获取首单铺货订单明细
     * @param ordDisOrderFirstDetailIn
     * @param bizOrgCode
     * @return
     */
    Page<OrdDirOrderFirstDetailOut> findOrdDirOrderFirstDetailPage(OrdDirOrderFirstDetailIn ordDisOrderFirstDetailIn, String bizOrgCode);

    /**
     * 校验铺货单商品
     * @param insertFirstOrderDetailIn
     * @param bizOrgCode
     * @return
     */
    Response checkRepeatParam(InsertFirstOrderDetailIn insertFirstOrderDetailIn, String bizOrgCode);

    /**
     * 新增铺货单明细
     * @param insertFirstOrderDetailIn
     * @return
     */
    Response<OrdDirOrderFirst> saveFirstOrderDetail(InsertFirstOrderDetailIn insertFirstOrderDetailIn);

    @Transactional(rollbackFor = Exception.class)
    OrdDirOrderFirst getOrdDirOrderFirstForImport(Long firstOrderId, StoreOut storeOut, String loginUsername, Integer isEffectiveImmediately, LocalDateTime effectiveTime);

    Response<Long> asyncImportFirstOrderDetail(String fileId, OrdDirOrderFirst ordDirOrderFirst, String loginUsername);

    Response<String> auditFirstOrder(OrdDirFirstOrderAuditIn ordDirFirstOrderAuditIn, String loginUsername);

    @Transactional(rollbackFor = Exception.class)
    Long createDirFirstOrder(OrdDirOrderFirst ordDirOrderFirst);
}
