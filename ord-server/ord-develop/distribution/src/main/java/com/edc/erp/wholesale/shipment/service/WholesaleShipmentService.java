package com.edc.erp.wholesale.shipment.service;

import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.model.in.QueryWarehouseIn;
import com.edc.erp.model.out.StockInfoOut;
import com.edc.erp.model.out.WarehouseInfoOut;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.wholesale.model.excel.shipment.ImportShipmentOrderIn;
import com.edc.erp.wholesale.model.in.PushPurUpdateIn;
import com.edc.erp.wholesale.model.in.shipment.*;
import com.edc.erp.wholesale.model.listener.shipment.ImportShipmentOrderListener;
import com.edc.erp.wholesale.model.out.shipment.QueryShipmentReportOut;
import com.edc.erp.wholesale.model.out.shipment.ShipmentWithDetailOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleDateInfoOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleShipmentDetailOut;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import com.edc.sdk.dts.model.order.vo.WholesaleBillVO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 批发出货单(WholesaleShipment)表服务接口
 *
 * @author lx
 * @since 2022-10-18 11:59:38
 */
public interface WholesaleShipmentService extends BaseService<WholesaleShipment> {

    /**
     * 分页查询批发出货单
     * @param queryShipmentIn 批发出货单 查询入参类
     * @return
     */
    Page<ShipmentWithDetailOut> findShipmentByPage(QueryShipmentIn queryShipmentIn);

    /**
     * 查询允许批发的仓位信息
     * @param queryWarehouseIn 仓位信息入参
     * @return
     */
    List<StockInfoOut> findStockInfo(QueryWarehouseIn queryWarehouseIn);

    /**
     * 根据允许批发的仓位信息的仓储id查询仓储信息
     * @param bizOrgCode 业务组织code
     * @param wholesaleOrderType 批发单类型(out：出货，returns：退货)
     * @return
     */
    List<WarehouseInfoOut> findWarehouseInfo(String bizOrgCode, String wholesaleOrderType);

    /**
     * 查询仓储与仓位信息(二级联动)
     * @param bizOrgCode 业务组织code
     * @param wholesaleOrderType 批发单类型(out：出货，returns：退货)
     * @return
     */
    List<WarehouseInfoOut> findWarehouseStockInfo(String bizOrgCode, String wholesaleOrderType);

    /**
     * 保存或修改批发出货单
     * @param shipmentWithDetailIn 批发出货单和明细新增入参类
     * @return
     */
    ShipmentWithDetailOut saveUpdate(ShipmentWithDetailIn shipmentWithDetailIn);

    /**
     * 出货单审核
     * @param shipmentWithDetailIn 批发出货单和明细新增入参类
     * @return
     */
    ShipmentWithDetailOut audit(ShipmentWithDetailIn shipmentWithDetailIn);

    /**
     * 出货单作废
     * @param wholesaleShipment 出货单实体
     * @return
     */
    int invalid(WholesaleShipment wholesaleShipment);

    /**
     * 批发出货单查询
     * @param id
     * @param bizOrgCode
     * @return
     */
    ShipmentWithDetailOut getShipment(Long id, String bizOrgCode);

    /**
     * 出货单冲销
     * @param shipmentOrderWriteOffIn 出货单冲销入参类
     * @return
     */
    WholesaleShipment writeOff(ShipmentOrderWriteOffIn shipmentOrderWriteOffIn, String loginUsername);

    /**
     * 导出批发出货单
     * @param wholesaleShipmentId 出货单详情入参
     * @return
     */
    String export(Long wholesaleShipmentId);

    /**
     * 批量导入批发出货单
     * @param importShipmentOrderIn 批发出货单批量导入 入参类
     * @return
     */
    Response<List<WholesaleShipmentDetailOut>> importShipmentOrderDtl(ImportShipmentOrderIn importShipmentOrderIn);

    SaleGoodsInfoOut getSaleGoodsInfo(WholesaleShipmentDetailIn detail, String bizOrgCode, String priceGroupCode);

    /**
     * 增加或减少库存
     * @param wholesaleShipment 出货单
     * @param shipmentDetails 出货单详情
     * @param adjustType 库存增减
     * @return
     */
    List<StockFlowIn> addOrSubStock(WholesaleShipment wholesaleShipment, List<WholesaleShipmentDetailIn> shipmentDetails, String adjustType);

    /**
     * 批发出货单DTS回传入参 转换出货单明细
     * @param wholesaleBillVO 批发单返回入参类
     * @param wholesaleShipment
     * @return
     */
    ShipmentWithDetailIn wholesaleBillVOToShipmentDetail(WholesaleBillVO wholesaleBillVO, WholesaleShipment wholesaleShipment);

    /**
     * 资管调用参数转换
     * @param wholesaleShipment 原批发出单
     * @param shipmentStatus 出货单状态
     * @return
     */
    RechargeLiquidationIn chargeParamToShipmentLiquidationIn(WholesaleShipment wholesaleShipment, String newShipmentNo, String shipmentStatus);

    /**
     * 批量更新出货单和明细
     * @param shipmentWithDetailIn 批发出货单 DTS回传处理之后的数据
     * @return
     */
    int batchUpdate(ShipmentWithDetailIn shipmentWithDetailIn, String beforeStatus);

    /**
     * 批发出-发货
     * @param shipmentsIn 批发出-手动发货 入参
     * @return
     */
    Response<String> shipmentSendOut(ShipmentsIn shipmentsIn);

    /**
     * 批发出货单回传
     *
     * @param wholesaleBillVO
     * @return
     */
    String wholesaleOrderCallBack(WholesaleBillVO wholesaleBillVO, WholesaleShipment wholesaleShipment);

    /**
     * 批量导出批发出货单
     * @param queryShipmentIn 批发出货单查询入参
     * @return
     */
    String exportDetail(QueryShipmentIn queryShipmentIn);

    QueryShipmentReportOut queryShipmentReportForPage(QueryShipmentIn queryShipmentIn);

    ImportShipmentOrderListener importShipmentOrder(String fileId);

//    Response<String> batchAudit(List<Long> shipmentIdList);

    List<WholesaleShipmentDetail> recalculateNeedUpdateDetailForAudit(List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList,
                                                                      WholesaleShipment wholesaleShipment);

    int countOneBySourceNo(String sourceNo, String bizOrgCode);

    WholesaleShipment getOneByNo(String shipmentNo, String bizOrgCode);

    List<Long> findNeedPushPurWholesaleShipmentId(QueryPushPurIn queryPushPurIn);

    @Transactional(rollbackFor = Exception.class)
    int updatePurBatchNumberByIdList(PushPurUpdateIn pushPurUpdateIn);

    String exportOrder(QueryShipmentIn queryShipmentIn);

    WholesaleDateInfoOut sumWholesaleDateInfo(QueryShipmentIn queryShipmentIn);

    ShipmentWithDetailIn initShipmentWithDetailInForBatchAudit(WholesaleShipment wholesaleShipment, String bizOrgCode);

    WholesaleShipment getOneByNo(String shipmentNo);

    boolean checkIsHsShipment(String sourceNo);

    String replaceStr(String str);

    List<Long> findNeedDelayPushPurWholesaleShipmentId(QueryPushPurIn queryPushPurIn);

    int updatePushPurTime(WholesaleShipment wholesaleShipment);

    int updateShipmentPurchaseNoByPurBatchNumber(TransferShipmentPushPurchaseBackVO transferShipmentPushPurchaseBackVO,
                                                 String updater, LocalDateTime updateTime);

}
