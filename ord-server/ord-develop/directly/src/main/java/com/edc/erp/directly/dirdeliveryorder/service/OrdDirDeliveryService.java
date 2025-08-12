package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.vo.InOneQtyVO;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.model.in.*;
import com.edc.erp.directly.dirdeliveryorder.model.out.*;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


/**
 * 配货单表(OrdDirDelivery)表服务接口
 *
 * @author weichao
 * @since 2022-11-10 14:45:23
 */
public interface OrdDirDeliveryService extends BaseService<OrdDirDelivery> {
    /**
     * 作废配货单
     *
     * @param id
     * @param userName
     * @return
     */
    int invalidDirDeliverOrder(Long id, String userName);

    /**
     * 作废中转配货单
     *
     * @param delivery
     * @param deliveryDetails
     * @return
     */
    int invalidTransferOrder(OrdDirDelivery delivery, List<OrdDirDeliveryDetail> deliveryDetails);

    /**
     * @Description: 审核配货单
     * @Author: ZhangYao
     * @Date: 2024/4/19 15:12
     * @param stockInfoOut:
     * @param ordDirDelivery:
     * @return: com.edc.plugins.common.response.Response<com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut>
     **/
    Response<OrdDirDeliveryOut> audit(StockInfoOut stockInfoOut, OrdDirDelivery ordDirDelivery);

    /**
     * 批量审核配货单
     *
     * @param bizOrgCode
     * @param loginUsername
     * @param ordDirDeliveryIns
     * @return
     */
    Response bachAudit(String bizOrgCode, String loginUsername, List<OrdDirDeliveryIn> ordDirDeliveryIns);
//
//    /**
//     * 收到触发发货
//     *
//     * @param ordDirDeliveryIn
//     * @return
//     */
//    Response<OrdDirDeliveryOut> shipments(OrdDirDeliveryIn ordDirDeliveryIn);

    /**
     * 发货
     *
     * @param ordDirDelivery
     * @param updateDirDeliveryDetailInList
     */
    @Transactional(rollbackFor = Exception.class)
    void updateDirDeliveryInfo(OrdDirDelivery ordDirDelivery, List<UpdateDirDeliveryDetailIn> updateDirDeliveryDetailInList, String loginBizOrgCode);

    void optInvForDeliveryInfo(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> ordDirDeliveryDetails, StockInfoOut stockInfoOut);

    List<StockFlowIn> dirDeliveryOrderOptInv(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> detailList, List<OperatorInvIn> operatorInvIns, StockInfoOut stockInfoOut);

    /**
     * 新增配货单
     *
     * @param ordDirDeliveryIn
     * @return
     */
    Long saveOrUpdate(OrdDirDeliveryIn ordDirDeliveryIn);


    /**
     * 校验商品信息
     *
     * @param deliveryIn
     * @return
     */
    OrdDirDeliveryDetailOut checkOrderGoods(DeliveryIn deliveryIn, String centerStockBizOrgCode);

    /**
     * 配货单明细导入
     *
     * @param importDeliveryIn
     * @return
     */
    Response<List<DirDeliveryOrderDetailsOut>> importDeliveryDetail(ImportDeliveryIn importDeliveryIn, String centerStockBizOrgCode);

    /**
     * 运营端分页查询配货单
     *
     * @param deliveryOrderIn
     * @return
     */
    Page<DirDeliveryOrderOut> findDeliveryOrdersByPage(DirDeliveryOrderIn deliveryOrderIn);

    /**
     * 运营端配货单详情
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    Page<DirDeliveryOrderDetailsOut> findDeliveryOrderDetailForPage(DirDeliveryOrderDetailsIn deliveryOrderDetailsIn, String loginBizOrgCode);

    /**
     * 运营端查询配货单表头
     *
     * @param deliveryOrderId
     * @return
     */
    DirDeliveryOrderOut getDeliveryOrderOutByDeliveryOrderId(Long deliveryOrderId);

    /**
     * 导出配货单明细列表
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    String exportDeliveryOrderDetails(DirDeliveryOrderDetailsIn deliveryOrderDetailsIn, String loginBizOrgCode);

    /**
     * 导出配货单列表
     *
     * @param deliveryOrderIn
     * @return
     */
    String exportDeliveryOrder(DirDeliveryOrderIn deliveryOrderIn);


    /**
     * 运营端查询配货单收货信息
     *
     * @param dirDeliveryOrderId
     * @param bizOrgCode
     * @return
     */
    TakeDeliveryInfoOut getTakeDeliveryInfoByDeliveryOrderId(Long dirDeliveryOrderId);

    /**
     * 冲销配货单
     *
     * @param chargeDirDeliveryOrderIn
     * @return
     */
    Response chargeDeliveryOrder(ChargeDirDeliveryOrderIn chargeDirDeliveryOrderIn);

    Response<String> asyncImportDelivery(String fileId, String loginUsername, String loginBizOrgCode);

    @Transactional(rollbackFor = Exception.class)
    List<Long> saveAsyncImportDeliveryOrder(List<OrdDirDeliveryIn> ordDirDeliveryInList);

    @Transactional(rollbackFor = Exception.class)
    List<OrdDirDeliveryIn> initDeliveryOrderAndDetailForAsyncImport(List<ImportDirDeliveryOrder> deliveryOrderList,
                                                                    Map<String, StoreInfo> storeInfoMap, String loginUsername);

    /**
     * 运营端查询配货单金额数量汇总
     *
     * @param deliveryOrderIn
     * @return
     */
    DirDeliveryOrderOut getDeliveryOrderTotal(DirDeliveryOrderIn deliveryOrderIn);

    /**
     * 查询配货单列表(库存盘点)
     *
     * @param deliveryOrderIn
     * @return
     */
    List<DirDeliveryOrderOut> findDirDeliveryOrder(DirDeliveryOrderIn deliveryOrderIn);


    /**
     * 查询在单量
     *
     * @param storeCode
     * @param beginTime
     * @param endTime
     * @return
     */
    List<InOneQtyVO> findInDeliveryOrder(String storeCode, String beginTime, String endTime);

    /**
     * 重置指定收货
     *
     * @param id
     * @param bizOrgCode
     * @param loginUsername
     */
    void updateResetTakeDirDeliveryByDeliveryOrderId(Long id, String bizOrgCode, String loginUsername);

    List<OrdDirDelivery> findNeedAutoTakeDirDeliveryOrderList(@Param("nowTime") String nowTime, @Param("deliveryStatusCode") String deliveryStatusCode);

    int updateDirDeliveryTakeOngoingById(OrdDirDelivery deliveryOrder);

    /**
     * 采购订单回传直营配货单
     *
     * @param transferNoticePurchaseVOList
     * @return
     */
    String dirPurchaseOrderToErp(List<TransferNoticePurchaseVO> transferNoticePurchaseVOList);

//    @Transactional(rollbackFor = Exception.class)
//    void handleDeliveryOrderAfterStock(OrdDirDeliveryOut ordDirDelivery, List<OrdDirDeliveryDetail> dirDeliveryDetailList, Integer storeId);

    int countByStatusAndIdList(String deliveryOrderStatus, List<Long> idList);

    /**
     * 导出多个配货单明细
     *
     * @param deliveryOrderIn
     * @return
     */
    String exportDeliveryOrderDetailByOrder(DirDeliveryOrderIn deliveryOrderIn);

    List<DirNoSalvageDeliveryOrderOut> findNoSalvageDeliveryOrderList(List<Long> idList, String bizOrgCode);

    OrdDirDeliveryOut getDeliveryOrderOutByNo(String deliveryOrderNo);

    /**
     * 根据时间段查询未审核的单据
     * @param createTimeBegin
     * @param createTimeEnd
     * @param bizOrgCode
     * @return
     */
    List<String> findNotAuditDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode);

    /**
     * 校验金额是否过大并提醒
     * @param createTimeBegin
     * @param createTimeEnd
     * @param bizOrgCode
     */
    void checkDeliveryAmountSimilarity(String createTimeBegin, String createTimeEnd, String bizOrgCode);


    List<DirNoAuditDeliveryOrderInfoOut> findNoAuditDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode);

    List<DirDeliveryOrderPrintOut> findPrintDataByIds(List<Long> ids);

    Page<DirDeliveryForReturnOut> findDeliveryOrderForReturn(DirDeliveryOrderIn deliveryOrderIn);

    List<DirStoreDeliveryNoInfoForAppOut> findStoreDeliveryInfoList(String storeCode);
}
