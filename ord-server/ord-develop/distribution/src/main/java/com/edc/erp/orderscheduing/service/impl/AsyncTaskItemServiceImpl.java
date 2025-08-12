package com.edc.erp.orderscheduing.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.FirstOrderStatusEnum;
import com.edc.erp.common.model.in.zk.ZKDeliveryOrderIn;
import com.edc.erp.common.model.in.zk.ZKReturnOrderDetailIn;
import com.edc.erp.common.model.out.goods.StandardGoodsInfoOut;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.handle.ZKDeliveryOrderHandle;
import com.edc.erp.disdeliveryorder.model.in.DisDeliveryTaskIn;
import com.edc.erp.disdeliveryorder.model.in.OrdDisDeliveryIn;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleReturnSaveIn;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleShipmentSaveIn;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDataFileService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryPayService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.disdifferenceorder.service.OrdDisDelivDifferenceService;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDelivery;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDeliveryService;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstService;
import com.edc.erp.disrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.service.OrdDisOrderDistributionService;
import com.edc.erp.distribution.service.OrdDisOrderTrackService;
import com.edc.erp.enumeration.OrderStatusEnum;
import com.edc.erp.enumeration.OrderTrackLogTemplateEnum;
import com.edc.erp.enumeration.OrderTrackStatusEnum;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.handle.DisDeliveryOrderHandle;
import com.edc.erp.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.orderscheduing.handle.OrderProcessSchedulingHandle;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.handle.ZKReturnOrderHandle;
import com.edc.erp.returnorder.service.OrdDisReturnService;
import com.edc.erp.wholesale.enumeration.PushPurProgressEnum;
import com.edc.erp.wholesale.handle.ZKWholesaleBusinessHandle;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.model.order.in.*;
import com.edc.sdk.dts.model.order.vo.*;
import com.edc.sdk.dts.service.DtsOrdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fxw
 * @description: 异步任务接口实现类
 * @since 2022/11/1 11:31
 */
@Service
@Slf4j
public class AsyncTaskItemServiceImpl implements AsyncTaskItemService {

    @Autowired
    private OrderProcessSchedulingHandle orderProcessSchedulingHandle;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrdDisOrderFirstService ordDisOrderFirstService;

    @Autowired
    private DtsOrdService dtsOrdService;

    @Autowired
    private OrdDisDelivDifferenceService ordDisDelivDifferenceService;

    @Autowired
    private OrdDisOrderDistributionService ordDisOrderDistributionService;

    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;

    @Autowired
    private WholesaleReturnsService wholesaleReturnsService;

    @Autowired
    private OrdDisReturnService ordDisReturnService;

    @Autowired
    private WholesaleShipmentService wholesaleShipmentService;

    @Autowired
    private OrdDisOrderTrackService ordDisOrderTrackService;

    @Autowired
    private DisDeliveryOrderHandle disDeliveryOrderHandle;

    @Autowired
    private OrdDisDeliveryDataFileService ordDisDeliveryDataFileService;

    @Autowired
    private OrdDisOrderFirstDeliveryService ordDisOrderFirstDeliveryService;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private StockServer stockServer;

    @Autowired
    private ZKDeliveryOrderHandle zkDeliveryOrderHandle;

    @Autowired
    private ZKReturnOrderHandle zkReturnOrderHandle;
//
//    @Autowired
//    private ZKBusinessCallBackHandle zkBusinessCallBackHandle;

    @Autowired
    private OrdDisDeliveryPayService ordDisDeliveryPayService;

    @Autowired
    private ZKWholesaleBusinessHandle zkWholesaleBusinessHandle;

    @Autowired
    private WholesaleShipmentDetailService wholesaleShipmentDetailService;

    @Autowired
    private OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private DisOrderHandle orderHandle;


    /**
     * 任务接口关联
     *
     * @throws Exception
     */
    @Override
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_DISTRIBUTION_TO_REQUEST, this::disDistributionToRequest);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_REQUEST_TO_DELIVERY, this::disRequestToDelivery);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_FIRST_TO_DELIVERY, this::disFirstToDelivery);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_DISTRIBUTION_CREATE_ORDER, this::disDistributionCreateOrder);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_DELIVERY_TO_DIFFERENCE, this::disDeliveryToDifference);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_PURCHASE_ORDER_TO_ERP, this::disPurchaseOrderToErp);

//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_DELIVERY_TO_DTS, this::disDeliveryToDts);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_DIFFERENCE_ORDER_TO_DTS, this::disDifferenceOrderToDts);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_RETURN_TO_DTS, this::disReturnToDts);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_TO_DTS, this::wholesaleShipmentToDts);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.WHOLESALE_RETURN_TO_DTS, this::wholesaleReturnToDts);


//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_DISTRIBUTION_TO_ORDER, this::disDistributionToOrder);


//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_DELIVERY_OCCUPY_INV, this::handleDirDeliveryOrderOccupyInventory);


        // DTS回传
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_DELIVERY_DTS_TO_ERP, this::unificationOrderCallBack);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_RETURN_DTS_TO_ERP, this::unificationReOrderCallBack);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_DIFFERENCE_DTS_TO_ERP, this::differenceOrderCallBack);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_WHOLESALE_DTS_TO_ERP, this::wholesaleOrderCallBack);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIS_WHOLESALE_RE_DTS_TO_ERP, this::wholesaleReOrderCallBack);


        //配销数据文件
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.ORDER_DIS_DELIVERY_DATA_FILE, this::execOrderDeliveryDataFile);

        // 配销单推送中科
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DELIVERY_SEND_ZK, this::sendDeliveryOrderToZk);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.RETURN_SEND_ZK, this::sendReturnOrderToZk);
        // 中科审核回传
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.ZK_AUDIT_CALL_BACK, this::zKAuditCallBack);
        // 中科确认回传（发货/收货回传）
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.ZK_CONFIRM_CALL_BACK, this::zKConfirmCallBack);
        // 中科创建批发处
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.ZK_SAVE_WHOLESALE_SHIPMENT, this::zKCreateWholesaleShipment);
        // 中科创建批发退
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.ZK_SAVE_WHOLESALE_RETURN, this::zKCreateWholesaleReturn);
        // 中科创建批发处回传
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.ZK_WHOLESALE_SHIPMENT_BACK, this::zKWholesaleShipmentBack);
        // 中科创建批发退回传
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.ZK_WHOLESALE_RETURN_BACK, this::zKWholesaleReturnBack);
        // 批发中转商品发采购回传采购单号
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_PURCHASE_BACK, this::handleWholesaleShipmentPurchaseBack);
    }


    /**
     * 订货单生成集货单
     *
     * @param sendBeforeCreateRequestOrderMqIn
     * @return
     */
    @Override
    public boolean disDistributionToRequest(SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("订货单状态更新至可创建要货单前入参----------------->" + jsonObject.toJSONString());
//
//        SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = JSON.parseObject(jsonObject.toJSONString(), SendBeforeCreateRequestOrderMqIn.class);
//        if (Objects.isNull(sendBeforeCreateRequestOrderMqIn)) {
//            return Response.error("订货单状态更新至可创建要货单入参为空").toString();
//        }
        List<OrdDisOrder> orderList = sendBeforeCreateRequestOrderMqIn.getOrderList();
        if (CollectionUtils.isEmpty(orderList)) {
            log.info("要货单{}下订货单为空,无法创建要货单", sendBeforeCreateRequestOrderMqIn.getOrderList());
            return true;
        }
        try {
            Long orderId = sendBeforeCreateRequestOrderMqIn.getOrderList().get(0).getId();
            if (Objects.isNull(orderId)) {
                return true;
            }
            OrdDisOrder order = orderHandle.selectByPrimaryKey(orderId);
            if (!OrderStatusEnum.PAID.getKey().equals(order.getOrderStatusCode())) {
                return false;
            }
//            String beforeCreateRequestOrderConsumerKey = "beforeCreateRequestOrderConsumerKey:" + sendBeforeCreateRequestOrderMqIn.getBizOrgCode() + ":" + orderId;
//            String redisBeforeCreateRequestOrderConsumerKey = redisService.get(beforeCreateRequestOrderConsumerKey);
//            if (StringUtils.isNotBlank(redisBeforeCreateRequestOrderConsumerKey)) {
//                log.error("重复消费-消费订货单状态更新至可创建要货单{}", orderId);
//                return true;
//            }
            orderProcessSchedulingHandle.handleBeforeCreateRequestOrder(sendBeforeCreateRequestOrderMqIn);
//            redisService.set(beforeCreateRequestOrderConsumerKey, orderId, 20, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            log.error("创建要货单前的任务异常:截单周期id---{}", sendBeforeCreateRequestOrderMqIn.getOrderCycleId(), e);
            throw e;
        }
//        return "false";
    }

    /**
     * 集货单转配销单
     *
     * @param requestOrderCreateMqIn
     * @return
     */
    @Override
    public boolean disRequestToDelivery(RequestOrderCreateMqIn requestOrderCreateMqIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("集货单创建成功后生成配销单入参----------------->" + jsonObject.toJSONString());
//        RequestOrderCreateMqIn requestOrderCreateMqIn = JSON.parseObject(jsonObject.toJSONString(), RequestOrderCreateMqIn.class);
//        if (Objects.isNull(requestOrderCreateMqIn)) {
//            return Response.error("集货单更新至可创建配销单入参为空").toString();
//        }
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(requestOrderCreateMqIn.getStoreCode());
        if (Objects.isNull(storeOut)) {
            log.error("门店{}不存在", requestOrderCreateMqIn.getStoreCode());
            return false;
        }
        StoreLogisticsOut logistics = storeCenterService.getStoreLogisticsByStoreCode(requestOrderCreateMqIn.getStoreCode(), requestOrderCreateMqIn.getBizOrgCode());
        Map<String, StockInfoOut> stockMap = stockServer.findByAuthOrg(requestOrderCreateMqIn.getBizOrgCode());
        Response response;
        try {
            response = orderProcessSchedulingHandle.handleAfterRequestOrderCreated(requestOrderCreateMqIn, storeOut, logistics, stockMap);
            return response.isSuccess();
        } catch (BusinessException be) {
            log.error("加盟集货单{}拆单异常", requestOrderCreateMqIn.getRequestOrderNo(), be);
            throw be;
        } catch (Exception e) {
//            String requestOrderSplitKey = DisSystemConstant.CHECK_DIS_REQUEST_ORDER_SPLIT_KEY + requestOrderCreateMqIn.getBizOrgCode() +
//                    SystemConstant.COLON + requestOrderCreateMqIn.getStoreCode() + SystemConstant.COLON + requestOrderCreateMqIn.getRequestOrderNo();
//            redisService.del(requestOrderSplitKey);
            log.error("加盟集货单{}拆单异常,删除验重redis", requestOrderCreateMqIn.getRequestOrderNo(), e);
            throw e;
        }
//        return JSONObject.toJSONString(response);
    }

    /**
     * 铺货单转配销单
     *
     * @param ordDisOrderFirst
     * @return
     */
    @Override
    public boolean disFirstToDelivery(OrdDisOrderFirst ordDisOrderFirst) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("铺货单审核成功后生成配销单入参----------------->" + jsonObject.toJSONString());
//        OrdDisOrderFirst ordDisOrderFirst = JSON.parseObject(jsonObject.toJSONString(), OrdDisOrderFirst.class);
        OrdDisOrderFirstDelivery ordDisOrderFirstDelivery = new OrdDisOrderFirstDelivery();
        ordDisOrderFirstDelivery.setFirstOrderId(ordDisOrderFirst.getId());
        int count = ordDisOrderFirstDeliveryService.count(ordDisOrderFirstDelivery);
        if (count > 0) {
            log.error("铺货单{}重复处理", ordDisOrderFirst.getFirstOrderNo());
            return true;
        }
        ordDisOrderFirst = ordDisOrderFirstService.selectByPrimaryKey(ordDisOrderFirst.getId());
        boolean flag = FirstOrderStatusEnum.APPROVED.getCode().equals(ordDisOrderFirst.getFirstOrderStatus())
                || FirstOrderStatusEnum.EXECUTED.getCode().equals(ordDisOrderFirst.getFirstOrderStatus());
        if (!flag) {
            log.error("铺货单{}已作废", ordDisOrderFirst.getFirstOrderNo());
            return true;
        }
        Response<List<OrdDisDeliveryIn>> response = ordDisOrderFirstService.spiltFirstOrderByConfig(ordDisOrderFirst);
        if (!response.isSuccess()) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(response.getData())) {
            ordDisDeliveryService.bachAudit(ordDisOrderFirst.getBizOrgCode(), ordDisOrderFirst.getUpdater(), response.getData());
        }
        return true;
    }

    /**
     * 配销单收货后生成配销差异单
     *
     * @param saveDifferenceIn
     * @return
     */
    @Override
    public boolean disDeliveryToDifference(SaveDifferenceIn saveDifferenceIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("配销单收货后生成配销差异单入参----------------->" + jsonObject.toJSONString());
//        SaveDifferenceIn saveDifferenceIn = JSON.parseObject(jsonObject.toJSONString(), SaveDifferenceIn.class);
        Response saveDisDifference = ordDisDelivDifferenceService.saveDisDifference(saveDifferenceIn);
        return saveDisDifference.isSuccess();
    }

    /**
     * 配销单下发dts
     *
     * @param unificationBillIn
     * @return
     */
    @Override
    public boolean disDeliveryToDts(UnificationBillIn unificationBillIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("配销单下发dts入参----------------->" + jsonObject.toJSONString());
//        UnificationBillIn unificationBillIn = JSON.parseObject(jsonObject.toJSONString(), UnificationBillIn.class);
        boolean b = dtsOrdService.postUniOrder(unificationBillIn);
        if (b) {
            // 配销单下发dts推送订单追踪日志
            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.WAREHOUSE_IN_PROCESS.getTemplate(), unificationBillIn.getPlatform_bill_id());
            ordDisOrderTrackService.pushRedisOrderTrackMessage(unificationBillIn.getPlatform_bill_id(), unificationBillIn.getShop_code(),
                    OrderTrackStatusEnum.WAREHOUSE_IN_PROCESS.getName(), trackLog, unificationBillIn.getSource_organization(), unificationBillIn.getCreater(), unificationBillIn.getGenerate_time());
        }
//        return JSONObject.toJSONString(b);
        return b;
    }

    /**
     * 配销差异单下发dts
     *
     * @param differenceBillIn
     * @return
     */
    @Override
    public boolean disDifferenceOrderToDts(DifferenceBillIn differenceBillIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("配销差异单下发dts入参----------------->" + jsonObject.toJSONString());
//        DifferenceBillIn differenceBillIn = JSON.parseObject(jsonObject.toJSONString(), DifferenceBillIn.class);
        boolean b = dtsOrdService.postDifferenceOrder(differenceBillIn);
        return b;
    }

    /**
     * 配销退货单下发dts
     *
     * @param unificationReBillIn
     * @return
     */
    @Override
    public boolean disReturnToDts(UnificationReBillIn unificationReBillIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("配销退货单下发dts入参----------------->" + jsonObject.toJSONString());
//        UnificationReBillIn unificationReBillIn = JSON.parseObject(jsonObject.toJSONString(), UnificationReBillIn.class);
        boolean b = dtsOrdService.postUniReOrder(unificationReBillIn);
//        return JSONObject.toJSONString(b);
        return b;
    }

    /**
     * 批发出货单下发dts
     *
     * @param wholesaleBillIn
     * @return
     */
    @Override
    public boolean wholesaleShipmentToDts(WholesaleBillIn wholesaleBillIn) {
        //转换json对象
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("批发出货单下发dts入参----------------->：{}", jsonObject.toJSONString());
//        WholesaleBillIn wholesaleBillIn = JSON.parseObject(jsonObject.toJSONString(), WholesaleBillIn.class);
        return dtsOrdService.postWholesaleOrder(wholesaleBillIn);
//        return JSONObject.toJSONString(b);
    }

    /**
     * 批发退货单下发dts
     *
     * @param wholesaleReBillIn
     * @return
     */
    @Override
    public boolean wholesaleReturnToDts(WholesaleReBillIn wholesaleReBillIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("批发退货单下发dts入参----------------->" + jsonObject.toJSONString());
//        WholesaleReBillIn wholesaleReBillIn = JSON.parseObject(jsonObject.toJSONString(), WholesaleReBillIn.class);
        boolean b = dtsOrdService.postWholesaleReOrder(wholesaleReBillIn);
        return b;
    }

    /**
     * 采购订单回传配销单
     *
     * @param transferNoticePurchaseVOList
     * @return
     */
    @Override
    public boolean disPurchaseOrderToErp(List<TransferNoticePurchaseVO> transferNoticePurchaseVOList) {
//        JSONArray jsonObject = JSONObject.parseArray(messageJson);
//        log.info("采购订单回传配销单回传信息----------------->" + jsonObject.toJSONString());
//        List<TransferNoticePurchaseVO> transferNoticePurchaseVOList = JSON.parseArray(jsonObject.toJSONString(), TransferNoticePurchaseVO.class);
        ordDisDeliveryService.disPurchaseOrderToErp(transferNoticePurchaseVOList);
        return true;
    }

//    /**
//     * 分货单生成订货单
//     *
//     * @param messageJson
//     * @return
//     */
//    @Override
//    public String disDistributionToOrder(String messageJson) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("配销分货单生成订货单----------------->" + jsonObject.toJSONString());
//        //转换入参
//        DisDistributionInitOrderIn disDistributionInitOrderIn = JSON.parseObject(jsonObject.toJSONString(), DisDistributionInitOrderIn.class);
//        if (Objects.isNull(disDistributionInitOrderIn)) {
//            log.info("当前无分货单生成订货单任务----------------------------------");
//            return "";
//        }
//        //生成订货单
//        String message = ordDisOrderDistributionService.handlePurchaseListForDistributionOrder(
//                disDistributionInitOrderIn.getDistributionOrderId(),
//                disDistributionInitOrderIn.getEffectiveTime(),
//                disDistributionInitOrderIn.getLoginUsername(),
//                disDistributionInitOrderIn.getBizOrgCode());
//
//        if (StringUtils.isNotBlank(message)) {
//            log.info("配销分货单生成订货单任务失败：----------------------------------");
//            return message.concat("分货失败");
//        }
//        return message;
//    }

    @Override
    public boolean disDistributionCreateOrder(OrdDisOrderDistribution orderDistribution) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("配销分货单生成订货单----------------->" + jsonObject.toJSONString());
//        //转换入参
//        OrdDisOrderDistribution orderDistribution = JSON.parseObject(jsonObject.toJSONString(), OrdDisOrderDistribution.class);
//        if (Objects.isNull(orderDistribution)) {
//            log.info("当前无分货单生成订货单任务----------------------------------");
//            return "";
//        }
        //生成订货单
        String message = ordDisOrderDistributionService.handleDistributionCreateOrder(orderDistribution.getId(), orderDistribution.getUpdater());
        if (StringUtils.isNotBlank(message)) {
            log.info("配销分货单生成订货单任务失败：{}", message);
//            return message.concat("分货失败");
        }
        return true;
    }


    /**
     * 配销单回传
     *
     * @param unificationBillVO
     * @return
     */
    @Override
    public boolean unificationOrderCallBack(UnificationBillVO unificationBillVO) {
        OrdDisDelivery ordDisDelivery = new OrdDisDelivery();
//        //转换入参
//        UnificationBillVO unificationBillVO = JSON.parseObject(messageJson, UnificationBillVO.class);
        ordDisDelivery.setDeliveryOrderNo(unificationBillVO.getFsrcnum());
        OrdDisDelivery disDelivery = ordDisDeliveryService.selectOne(ordDisDelivery);
        if (Objects.isNull(disDelivery)) {
            log.error("此配销单不存在｛｝", unificationBillVO.getFsrcnum());
            return true;
        }
        if (DeliveryOrderEnum.SHIPPED.getKey().equals(disDelivery.getDeliveryStatusCode())) {
            log.error("此配销单后台已发货｛｝", unificationBillVO.getFsrcnum());
            return true;
        }
        if (!DeliveryOrderEnum.APPROVED.getKey().equals(disDelivery.getDeliveryStatusCode())) {
            log.error("此配销单状态不正确｛｝", unificationBillVO.getFsrcnum());
            return true;
        }
        // 查询配销单明细
        List<OrdDisDeliveryDetail> deliveryOrderDetails = ordDisDeliveryDetailService.findDeliveryOrderDetails(disDelivery.getId());
        List<String> goodsCodeList = deliveryOrderDetails.stream().map(OrdDisDeliveryDetail::getGoodsCode).collect(Collectors.toList());
        List<StandardGoodsInfoOut> standardGoodsInfoOutList = orderGoodsServer.findAllByGoodsCodeList(goodsCodeList);
        Map<String, StandardGoodsInfoOut> standardGoodsMap = standardGoodsInfoOutList.stream().collect(Collectors.toMap(StandardGoodsInfoOut::getGoodsCode, Function.identity()));
        return disDeliveryOrderHandle.unificationOrderCallBack(disDelivery, deliveryOrderDetails, unificationBillVO, standardGoodsMap);
    }

    /**
     * 配销退货单回传
     *
     * @param unificationReBillVO
     * @return
     */
    @Override
    public boolean unificationReOrderCallBack(UnificationReBillVO unificationReBillVO) {
        return ordDisReturnService.unificationReOrderCallBack(unificationReBillVO);
    }

    /**
     * 配销差异单回传
     *
     * @param differenceBillVO
     * @return
     */
    @Override
    public boolean differenceOrderCallBack(DifferenceBillVO differenceBillVO) {
        return ordDisDelivDifferenceService.differenceOrderCallBack(differenceBillVO);
    }

    /**
     * 批发退货单DTS回传
     *
     * @param wholesaleReBillVO
     * @return
     */
    @Override
    public boolean wholesaleReOrderCallBack(WholesaleReBillVO wholesaleReBillVO) {
//        String wholesaleReturnNo = wholesaleReBillVO.getFsrcnum();
//        String bizOrgCode = wholesaleReBillVO.getFdestorg();
        return wholesaleReturnsService.wholesaleReOrderCallBack(wholesaleReBillVO);
    }

    /**
     * 批发出货单DTS回传
     *
     * @param wholesaleBillVO 批发单返回入参类
     * @return
     */
    @Override
    public boolean wholesaleOrderCallBack(WholesaleBillVO wholesaleBillVO) {
//        WholesaleBillVO wholesaleBillVO = null;
        String result;
        WholesaleShipment wholesaleShipment = new WholesaleShipment();
//        try {
//            wholesaleBillVO = JSON.parseObject(messageJson, WholesaleBillVO.class);
        //回传 获取批发单号
        String shipmentNo = wholesaleBillVO.getFsrcnum();
        wholesaleShipment.setShipmentStatus(ShipmentStatusEnum.APPROVED.getCode());
        //根据批发单号 查询出货单和出货单详情
        wholesaleShipment.setShipmentNo(shipmentNo);
        wholesaleShipment.setIsDelete(ModelConst.DELETE.NO);
        //查询批发出货单
        wholesaleShipment = wholesaleShipmentService.selectOne(wholesaleShipment);
        if (Objects.isNull(wholesaleShipment)) {
            log.error("回传批发单号{}不存在", shipmentNo);
//                return "回传批发单号不存在：" + shipmentNo;
            return true;
        }
        result = wholesaleShipmentService.wholesaleOrderCallBack(wholesaleBillVO, wholesaleShipment);
        log.info("批发出货单{}DTS回传业务处理结果---{}", shipmentNo, result);
        return true;
//        } catch (Exception e) {
//            String key = SystemConstant.ORD_WHOLESALE_SHIPMENT_DTS_BACK_SHIPPED + SystemConstant.COLON + wholesaleShipment.getBizOrgCode()
//                    + SystemConstant.COLON + wholesaleBillVO.getFsrcnum();
//            redisService.del(key);
//            log.error("DTS批发出{}回传ERP任务异常", Objects.nonNull(wholesaleBillVO) ? wholesaleBillVO.getFsrcnum() : JSON.toJSON(wholesaleBillVO).toString(), e);
//            throw e;
//        }
    }


    /**
     * @param messageJson:
     * @Description: 配销单推送中科
     * @Author: ZhangYao
     * @Date: 2023/8/15 9:57
     * @return: java.lang.String
     **/
    @Override
    public String sendDeliveryOrderToZk(String messageJson) {
        JSONObject jsonObject = JSONObject.parseObject(messageJson);
        log.info("配销单推送中科任务入参---------->" + jsonObject.toJSONString());
        DisDeliveryTaskIn disDeliveryTaskIn = JSON.parseObject(jsonObject.toJSONString(), DisDeliveryTaskIn.class);
        String deliveryOrderNo = disDeliveryTaskIn.getDeliveryOrderNo();
        String loginUsername = disDeliveryTaskIn.getLoginUsername();
        OrdDisDelivery ordDisDelivery = ordDisDeliveryService.getOneByDeliveryOrderNoAndBizOrgCode(disDeliveryTaskIn.getDeliveryOrderNo(), disDeliveryTaskIn.getBizOrgCode());
        if (Objects.isNull(disDeliveryTaskIn)) {
            log.error("配销单{}不存在", deliveryOrderNo);
            return Response.error("配销单不存在").toString();
        }
        if (!DeliveryOrderEnum.PENDING.getKey().equals(ordDisDelivery.getDeliveryStatusCode())
                && !DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
            return Response.error("配销单已审核").toString();
        }
        Response<ZKDeliveryOrderIn> response = zkDeliveryOrderHandle.handleBeforeSendDeliveryOrderToZK(ordDisDelivery, loginUsername);
        if (response.isSuccess()) {
            boolean isNeedPay = disDeliveryTaskIn.getIsNeedPay();
            if (isNeedPay) {
                BigDecimal distributionAmount = ordDisDelivery.getDistributionAmount();
                // 资管余额支付
                Response<String> payResponse = ordDisDeliveryPayService.payDeliveryOrder(ordDisDelivery, distributionAmount);
                if (!payResponse.isSuccess()) {
                    log.error("中科推送配销单{}支付异常:{}", ordDisDelivery.getDeliveryOrderNo(), payResponse.getMessage());
                    throw new BusinessException(payResponse.getMessage());
                }
            }
            return zkDeliveryOrderHandle.sendDeliveryOrderToZK(response.getData(), ordDisDelivery.getId(), loginUsername);
        } else {
            log.error("配销单{}推送中科前处理异常{}", deliveryOrderNo, response.getMessage());
            return "success";
        }
    }

    /**
     * @param messageJson:
     * @Description: 退货单推送中科
     * @Author: ZhangYao
     * @Date: 2023/8/15 9:58
     * @return: java.lang.String
     **/
    @Override
    public String sendReturnOrderToZk(String messageJson) {
        JSONObject jsonObject = JSONObject.parseObject(messageJson);
        log.info("发送中科退货单审核消费者入参----------------->" + jsonObject.toJSONString());
        OrdDisReturn ordDisReturn = JSON.toJavaObject(jsonObject, OrdDisReturn.class);
        String returnOrderNo = ordDisReturn.getReturnOrderNo();
        String loginUsername = ordDisReturn.getUpdater();
        ordDisReturn = ordDisReturnService.getReturnOrderNoByIdAndBizOrgCode(ordDisReturn.getReturnOrderNo(), ordDisReturn.getBizOrgCode());
        if (Objects.isNull(ordDisReturn)) {
            log.error("退货单{}不存在", returnOrderNo);
            return Response.error("退货单不存在").toString();
        }
        Response<List<ZKReturnOrderDetailIn>> handelResponse = zkReturnOrderHandle.handleBeforeSendReturnOrderToZk(ordDisReturn, loginUsername);
        if (!handelResponse.isSuccess()) {
            return handelResponse.getMessage();
        }
        Response<String> response = zkReturnOrderHandle.sendReturnOrderToZK(ordDisReturn, handelResponse.getData());
        return JSONObject.toJSONString(response);
    }

//    @Override
//    public String zKAuditCallBack(String messageJson) {
//        log.info("中科审核回传任务----------------->" + messageJson);
//        Response<String> response = zkBusinessCallBackHandle.handleZkBusinessAuditCallBack(messageJson);
//        return JSONObject.toJSONString(response);
//    }

//    @Override
//    public String zKConfirmCallBack(String messageJson) {
//        log.info("中科确认回传任务----------------->" + messageJson);
//        Response<String> response = zkBusinessCallBackHandle.handleZkBusinessConfirmCallBack(messageJson);
//        return JSONObject.toJSONString(response);
//    }

    @Override
    public boolean zKCreateWholesaleShipment(TaskZKWholesaleShipmentSaveIn taskZKWholesaleShipmentSaveIn) {
//        log.info("中科创建批发出任务----------------->" + messageJson);
        Response<String> response = zkWholesaleBusinessHandle.handleZkWholesaleShipment(taskZKWholesaleShipmentSaveIn);
//        return JSONObject.toJSONString(response);
        log.info(response.getMessage());
        return response.isSuccess();
    }

    @Override
    public boolean zKCreateWholesaleReturn(TaskZKWholesaleReturnSaveIn taskZKWholesaleReturnSaveIn) {
//        log.info("中科创建批发退任务----------------->" + messageJson);
        Response<String> response = zkWholesaleBusinessHandle.handleZkWholesaleReturn(taskZKWholesaleReturnSaveIn);
        return response.isSuccess();
    }

    @Override
    public boolean zKWholesaleShipmentBack(WholesaleShipment wholesaleShipment) {
//        log.info("中科创建批发出回传任务----------------->" + messageJson);
//        Response<String> response;
//        WholesaleShipment wholesaleShipment = null;
        try {
//            JSONObject jsonObject = JSONObject.parseObject(messageJson);
//            if (null == jsonObject) {
//                return "中科创建批发出回传参数为空";
//            }
//            log.info("中科创建批发出回传消费者入参----------------->" + jsonObject.toJSONString());
//            wholesaleShipment = JSONObject.parseObject(jsonObject.toJSONString(), WholesaleShipment.class);
            Response<String> response = zkWholesaleBusinessHandle.handleZkWholesaleShipmentBack(wholesaleShipment);
            return response.isSuccess();
        } catch (Exception e) {
            log.error("中科创建批发出{}回传任务异常", wholesaleShipment.getShipmentNo(), e);
            throw e;
        }
//        return JSONObject.toJSONString(response);
    }

    @Override
    public boolean zKWholesaleReturnBack(WholesaleReturns wholesaleReturns) {
//        log.info("中科创建批发退回传任务----------------->" + messageJson);
        Response<String> response = zkWholesaleBusinessHandle.handleZkWholesaleReturnBack(wholesaleReturns);
//        return JSONObject.toJSONString(response);
        return response.isSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleWholesaleShipmentPurchaseBack(List<TransferShipmentPushPurchaseBackVO> shipmentPushPurchaseBackVOList) {
//        JSONArray jsonArray = JSONArray.parseArray(messageJson);
//        if (null == jsonArray) {
//             return "采购回传批发出货单参数为空";
//        }
//        log.info("采购回传批发出货单消费者入参----------------->" + jsonArray.toJSONString());
//        List<TransferShipmentPushPurchaseBackVO> shipmentPushPurchaseBackVOList = JSON.parseArray(jsonArray.toJSONString(), TransferShipmentPushPurchaseBackVO.class);
        shipmentPushPurchaseBackVOList.forEach(transferShipmentPushPurchaseBackVO -> {
            transferShipmentPushPurchaseBackVO.setPushPurProgress(PushPurProgressEnum.DONE.getProgress());
            wholesaleShipmentService.updateShipmentPurchaseNoByPurBatchNumber(transferShipmentPushPurchaseBackVO, SystemConstant.SYSTEM_USER, LocalDateTime.now());
//            wholesaleShipmentDetailService.updatePurchaseNoByPurBatchNumber(transferShipmentPushPurchaseBackVO, SystemConstant.SYSTEM_USER, LocalDateTime.now());
            log.info("批次号{}已更新采购单号{}", transferShipmentPushPurchaseBackVO.getPurBatchNumber(), transferShipmentPushPurchaseBackVO.getPurchaseOrderNo());
        });
//        return "回传更新成功";
        return true;
    }

//    @Override
//    public String handleDirDeliveryOrderOccupyInventory(String messageJson) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        DirDeliveryOccupyInventoryIn dirDeliveryOccupyInventoryIn = JSON.parseObject(jsonObject.toJSONString(), DirDeliveryOccupyInventoryIn.class);
//        List<StockFlowIn> stockFlowIns = Lists.newArrayList();
//        dirDeliveryOccupyInventoryIn.getDeliveryOrderIdList().forEach(id -> {
//            StockFlowIn stockFlowIn = disDeliveryOrderOccupyInventoryHandle.initStockFlowIn(id, dirDeliveryOccupyInventoryIn.getBizOrgCode());
//            if (Objects.isNull(stockFlowIn)) {
//                return;
//            }
//            stockFlowIns.add(stockFlowIn);
//        });
//        disDeliveryOrderOccupyInventoryHandle.handleOccupyInventory(stockFlowIns,dirDeliveryOccupyInventoryIn.getBizOrgCode(),dirDeliveryOccupyInventoryIn.getLoginUsername());
//        return "配销单占用库存成功";
//    }

    @Override
    public boolean execOrderDeliveryDataFile(OrdDeliveryDataFile ordDeliveryDataFile) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("配销单、配销退货单创建成功后入参----------------->" + jsonObject.toJSONString());
//        OrdDeliveryDataFile ordDeliveryDataFile = JSON.parseObject(jsonObject.toJSONString(), OrdDeliveryDataFile.class);
        try {
            return ordDisDeliveryDataFileService.execFile(ordDeliveryDataFile);
        } catch (Exception e) {
            log.error("执行配销数据上传文件失败", e);
            ordDeliveryDataFile.setRemark(e.getMessage());
            ordDisDeliveryDataFileService.overDataAFile(ordDeliveryDataFile);
            throw e;
        }
//        return "执行成功";
    }


}
