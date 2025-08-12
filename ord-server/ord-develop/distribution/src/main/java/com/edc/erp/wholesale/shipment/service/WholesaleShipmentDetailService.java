package com.edc.erp.wholesale.shipment.service;

import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.wholesale.model.in.shipment.HSWholesaleDifferenceOrder;
import com.edc.erp.wholesale.model.in.shipment.QueryPushPurIn;
import com.edc.erp.wholesale.model.in.shipment.QueryShipmentDetailIn;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.model.out.shipment.TransferShipmentPushPurchaseOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleDateInfoOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleShipmentDetailOut;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 批发出货单明细(WholesaleShipmentDetail)表服务接口
 *
 * @author lx
 * @since 2022-10-18 11:59:38
 */
public interface WholesaleShipmentDetailService extends BaseService<WholesaleShipmentDetail> {

    /**
     * 保存出货单明细
     * @param wholesaleShipmentDetailList 出货单明细
     * @param wholesaleShipment 出货单
     */
    void save(List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList, WholesaleShipment wholesaleShipment);

    /**
     * 出货单明细
     * @param wholesaleShipmentDetailList 出货单明细
     */
    void batchSave(List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList);

    /**
     * 查询出货单详情
     * @param queryShipmentDetailIn 调整单明细入参类
     * @return
     */
    List<WholesaleShipmentDetailIn> findShipmentDetailList(QueryShipmentDetailIn queryShipmentDetailIn);

    /**
     * 分页查询出货单明细
     * @param queryShipmentDetailIn 出货单明细查询入参
     * @return
     */
    Page<WholesaleShipmentDetailOut> findByPage(QueryShipmentDetailIn queryShipmentDetailIn);

    /**
     * 批量更新批发出货单明细
     * @param wholesaleShipmentDetailList 批发出货单明细 DTS回传处理之后的数据
     */
    void batchUpdate(List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList);

    int batchUpdateAudit(List<WholesaleShipmentDetail> wholesaleShipmentDetailList);

    @Transactional(rollbackFor = Exception.class)
    void batchSaveList(List<WholesaleShipmentDetail> wholesaleShipmentDetailList);

    List<WholesaleShipmentDetail> findListByShipmentId(Long shipmentId);

    List<TransferShipmentPushPurchaseOut> findNeedPushPurDetailList(QueryPushPurIn queryPushPurIn);

    int updatePurchaseNoByPurBatchNumber(TransferShipmentPushPurchaseBackVO transferShipmentPushPurchaseBackVO, String updater, LocalDateTime updateTime);

    List<TransferShipmentPushPurchaseOut> findNeedPushPurDetailListByShipmentIdList(List<Long> shipmentIdList, String bizOrgCode);

    WholesaleDateInfoOut sumWholesaleDateInfoByIdList(List<Long> idList);

    HSWholesaleDifferenceOrder initHsOrderDifference(Long id);

    List<TransferShipmentPushPurchaseOut> findNeedDelayPushPurDetailList(QueryPushPurIn queryPushPurIn);
}
