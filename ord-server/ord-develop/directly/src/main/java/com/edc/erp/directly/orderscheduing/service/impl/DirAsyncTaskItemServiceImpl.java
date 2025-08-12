package com.edc.erp.directly.orderscheduing.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.FirstOrderStatusEnum;
import com.edc.erp.common.model.out.goods.StandardGoodsInfoOut;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.model.in.OrdDirDeliveryIn;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDataFileService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDetailService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.dirdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.directly.dirdifferenceorder.service.OrdDirDelivDifferenceService;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDelivery;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstDeliveryService;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstService;
import com.edc.erp.directly.dirrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionService;
import com.edc.erp.directly.distribution.service.OrdDirOrderTrackService;
import com.edc.erp.directly.enumeration.OrderTrackLogTemplateEnum;
import com.edc.erp.directly.enumeration.OrderTrackStatusEnum;
import com.edc.erp.directly.handle.DirDeliveryOrderHandle;
import com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.directly.orderscheduing.handle.DirOrderProcessSchedulingHandle;
import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
import com.edc.erp.directly.returnorder.service.OrdDirReturnService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.model.order.in.*;
import com.edc.sdk.dts.model.order.vo.DifferenceBillVO;
import com.edc.sdk.dts.model.order.vo.UnificationBillVO;
import com.edc.sdk.dts.model.order.vo.UnificationReBillVO;
import com.edc.sdk.dts.service.DtsOrdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fxw
 * @description: 异步任务接口实现类
 * @since 2022/11/1 11:31
 */
@Service
@Slf4j
public class DirAsyncTaskItemServiceImpl implements DirAsyncTaskItemService {

    @Autowired
    private DirOrderProcessSchedulingHandle orderProcessSchedulingHandle;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrdDirOrderFirstService ordDirOrderFirstService;

    @Autowired
    private DtsOrdService dtsOrdService;

    @Autowired
    private OrdDirDelivDifferenceService ordDirDelivDifferenceService;

    @Autowired
    private OrdDirReturnService ordDirReturnService;

    @Autowired
    private OrdDirDeliveryService ordDirDeliveryService;

    @Autowired
    private OrdDirOrderDistributionService dirHandleDistributionOrderService;

    @Autowired
    private OrdDirOrderTrackService ordDirOrderTrackService;

    @Autowired
    private DirDeliveryOrderHandle dirDeliveryOrderHandle;

    @Autowired
    private OrdDirDeliveryDataFileService ordDirDeliveryDataFileService;

    @Autowired
    private OrdDirOrderFirstDeliveryService ordDirOrderFirstDeliveryService;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private StockServer stockServer;

    @Autowired
    private OrdDirDeliveryDetailService ordDirDeliveryDetailService;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    /**
     * 任务接口关联
     *
     * @throws Exception
     */
    @Override
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_DISTRIBUTION_TO_REQUEST, this::dirDistributionToRequest);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS, this::dirDeliveryToDts);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_DIFFERENCE_ORDER_TO_DTS, this::dirDifferenceOrderToDts);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_RETURN_TO_DTS, this::dirReturnToDts);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_TO_DTS, this::wholesaleShipmentToDts);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.WHOLESALE_RETURN_TO_DTS, this::wholesaleReturnToDts);

//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_FIRST_TO_DELIVERY, this::dirFirstToDelivery);

//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_DELIVERY_TO_DIFFERENCE, this::dirDeliveryToDifference);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_PURCHASE_ORDER_TO_ERP, this::dirPurchaseOrderToErp);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_DISTRIBUTION_TO_ORDER, this::dirDistributionToOrder);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_DISTRIBUTION_CREATE_ORDER, this::dirDistributionCreateOrder);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY, this::dirRequestToDelivery);
        // DTS回传
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP, this::unificationOrderCallBack);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_RETURN_DTS_TO_ERP, this::unificationReOrderCallBack);
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.DIR_DIFFERENCE_DTS_TO_ERP, this::differenceOrderCallBack);

        //直营配货数据文件
//        AsynPushTask.regPusher(AsyncTaskConstant.Type.ORDER_DIR_DELIVERY_DATA_FILE, this::execOrderDeliveryDataFile);
    }

    /**
     * 订货单生成要货单
     *
     * @param sendBeforeCreateRequestOrderMqIn
     * @return
     */
    @Override
    public boolean dirDistributionToRequest(SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
        List<OrdDirOrder> orderList = sendBeforeCreateRequestOrderMqIn.getOrderList();
        if (CollectionUtils.isEmpty(orderList)) {
            log.info("要货单{}下订货单为空,无法创建要货单", sendBeforeCreateRequestOrderMqIn.getOrderList());
            return true;
        }
        try {
            Long orderId = sendBeforeCreateRequestOrderMqIn.getOrderList().get(0).getId();
            if (Objects.isNull(orderId)) {
                return true;
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
     * 要货单转直营配货单
     *
     * @param requestOrderCreateMqIn
     * @return
     */
    @Override
    public boolean dirRequestToDelivery(RequestOrderCreateMqIn requestOrderCreateMqIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("要货单创建成功后生成直营配货单入参----------------->" + jsonObject.toJSONString());
//        RequestOrderCreateMqIn requestOrderCreateMqIn = JSON.parseObject(jsonObject.toJSONString(), RequestOrderCreateMqIn.class);
//        if (Objects.isNull(requestOrderCreateMqIn)) {
//            return Response.error("要货单更新至可创建直营配货单入参为空").toString();
//        }
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(requestOrderCreateMqIn.getStoreCode());
        if (Objects.isNull(storeOut)) {
            log.error("门店{}不存在", requestOrderCreateMqIn.getStoreCode());
            return false;
        }
        StoreLogisticsOut logistics = storeCenterService.getStoreLogisticsByStoreCode(requestOrderCreateMqIn.getStoreCode(), requestOrderCreateMqIn.getBizOrgCode());
        Map<String, StockInfoOut> stockMap = stockServer.findByAuthOrg(requestOrderCreateMqIn.getBizOrgCode());
        try {
            Response<String> response = orderProcessSchedulingHandle.handleAfterRequestOrderCreated(requestOrderCreateMqIn, storeOut, logistics, stockMap);
            return response.isSuccess();
        } catch (BusinessException be) {
            log.error("直营集货单{}拆单异常", requestOrderCreateMqIn.getRequestOrderNo(), be);
            throw be;
        } catch (Exception e) {
//            log.error("直营集货单{}拆单异常,删除验重redis", requestOrderCreateMqIn.getRequestOrderNo(), e);
//            String requestOrderSplitKey = DirSystemConstant.CHECK_DIR_REQUEST_ORDER_SPLIT_KEY + requestOrderCreateMqIn.getBizOrgCode() +
//                    SystemConstant.COLON + requestOrderCreateMqIn.getStoreCode() + SystemConstant.COLON + requestOrderCreateMqIn.getRequestOrderNo();
//            redisService.del(requestOrderSplitKey);
            throw e;
        }
    }

    /**
     * 铺货单转直营配货单
     *
     * @param ordDirOrderFirst
     * @return
     */
    @Override
    public boolean dirFirstToDelivery(OrdDirOrderFirst ordDirOrderFirst) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("铺货单审核成功后生成直营配货单入参----------------->" + jsonObject.toJSONString());
//
        OrdDirOrderFirstDelivery ordDirOrderFirstDelivery = new OrdDirOrderFirstDelivery();
        ordDirOrderFirstDelivery.setFirstOrderId(ordDirOrderFirst.getId());
        int count = ordDirOrderFirstDeliveryService.count(ordDirOrderFirstDelivery);
        if (count > 0) {
            log.error("铺货单{}重复处理", ordDirOrderFirst.getFirstOrderNo());
            return true;
        }
        ordDirOrderFirst = ordDirOrderFirstService.selectByPrimaryKey(ordDirOrderFirst.getId());
        boolean flag = FirstOrderStatusEnum.APPROVED.getCode().equals(ordDirOrderFirst.getFirstOrderStatus())
                || FirstOrderStatusEnum.EXECUTED.getCode().equals(ordDirOrderFirst.getFirstOrderStatus());
        if (!flag) {
            log.error("铺货单{}已作废", ordDirOrderFirst.getFirstOrderNo());
            return true;
        }
        Response<List<OrdDirDeliveryIn>> response = ordDirOrderFirstService.spiltFirstOrderByConfig(ordDirOrderFirst);
        if (!response.isSuccess()) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(response.getData())) {
            ordDirDeliveryService.bachAudit(ordDirOrderFirst.getBizOrgCode(), ordDirOrderFirst.getUpdater(), response.getData());
        }
        return true;
    }

    /**
     * 直营配货单收货后生成直营配货差异单
     *
     * @param saveDifferenceIn
     * @return
     */
    @Override
    public boolean dirDeliveryToDifference(SaveDifferenceIn saveDifferenceIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("直营配货单收货后生成直营配货差异单入参----------------->" + jsonObject.toJSONString());
//        SaveDifferenceIn saveDifferenceIn = JSON.parseObject(jsonObject.toJSONString(), SaveDifferenceIn.class);
        Response saveDisDifference = ordDirDelivDifferenceService.saveDirDifference(saveDifferenceIn);
//        return JSONObject.toJSONString(saveDisDifference);
        return saveDisDifference.isSuccess();
    }

    /**
     * 直营配货单下发dts
     *
     * @param unificationBillIn
     * @return
     */
    @Override
    public boolean dirDeliveryToDts(UnificationBillIn unificationBillIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("直营配货单下发dts入参----------------->" + jsonObject.toJSONString());
//        UnificationBillIn unificationBillIn = JSON.parseObject(jsonObject.toJSONString(), UnificationBillIn.class);
        boolean b = dtsOrdService.postUniOrder(unificationBillIn);
        if (b) {
            // 配货单下发dts推送订单追踪日志
            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.WAREHOUSE_IN_PROCESS.getTemplate(), unificationBillIn.getPlatform_bill_id());
            ordDirOrderTrackService.pushRedisOrderTrackMessage(unificationBillIn.getPlatform_bill_id(), unificationBillIn.getShop_code(),
                    OrderTrackStatusEnum.WAREHOUSE_IN_PROCESS.getName(), trackLog, unificationBillIn.getSource_organization(), unificationBillIn.getCreater(), unificationBillIn.getGenerate_time());
        }
//        return JSONObject.toJSONString(b);
        return b;
    }

    /**
     * 直营配货差异单下发dts
     *
     * @param differenceBillIn
     * @return
     */
    @Override
    public boolean dirDifferenceOrderToDts(DifferenceBillIn differenceBillIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("直营配货差异单下发dts入参----------------->" + jsonObject.toJSONString());
//        DifferenceBillIn differenceBillIn = JSON.parseObject(jsonObject.toJSONString(), DifferenceBillIn.class);
        boolean b = dtsOrdService.postDifferenceOrder(differenceBillIn);
//        return JSONObject.toJSONString(b);
        return b;
    }

    /**
     * 直营配货退货单下发dts
     *
     * @param unificationReBillIn
     * @return
     */
    @Override
    public boolean dirReturnToDts(UnificationReBillIn unificationReBillIn) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("直营退货单下发dts入参----------------->" + jsonObject.toJSONString());
//        UnificationReBillIn unificationReBillIn = JSON.parseObject(jsonObject.toJSONString(), UnificationReBillIn.class);
        boolean b = dtsOrdService.postUniReOrder(unificationReBillIn);
//        return JSONObject.toJSONString(b);
        return b;
    }

    /**
     * 批发出货单下发dts
     *
     * @param messageJson
     * @return
     */
    @Override
    public String wholesaleShipmentToDts(String messageJson) {
        //转换json对象
        JSONObject jsonObject = JSONObject.parseObject(messageJson);
        log.info("批发出货单下发dts入参----------------->：{}", jsonObject.toJSONString());
        WholesaleBillIn wholesaleBillIn = JSON.parseObject(jsonObject.toJSONString(), WholesaleBillIn.class);
        boolean b = dtsOrdService.postWholesaleOrder(wholesaleBillIn);
        return JSONObject.toJSONString(b);
    }

    /**
     * 批发退货单下发dts
     *
     * @param messageJson
     * @return
     */
    @Override
    public String wholesaleReturnToDts(String messageJson) {
        JSONObject jsonObject = JSONObject.parseObject(messageJson);
        log.info("批发退货单下发dts入参----------------->" + jsonObject.toJSONString());
        WholesaleReBillIn wholesaleReBillIn = JSON.parseObject(jsonObject.toJSONString(), WholesaleReBillIn.class);
        boolean b = dtsOrdService.postWholesaleReOrder(wholesaleReBillIn);
        return JSONObject.toJSONString(b);
    }

    /**
     * 采购订单回传配货单
     *
     * @param transferNoticePurchaseVOList
     * @return
     */
    @Override
    public boolean dirPurchaseOrderToErp(List<TransferNoticePurchaseVO> transferNoticePurchaseVOList) {
//        JSONArray jsonObject = JSONObject.parseArray(messageJson);
//        log.info("采购订单回传直营配货单回传信息----------------->" + jsonObject.toJSONString());
//        List<TransferNoticePurchaseVO> transferNoticePurchaseVOList = JSON.parseArray(jsonObject.toJSONString(), TransferNoticePurchaseVO.class);
        ordDirDeliveryService.dirPurchaseOrderToErp(transferNoticePurchaseVOList);
        return true;
    }

//    /**
//     * 分货单生成订货单
//     *
//     * @param messageJson
//     * @return
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public String dirDistributionToOrder(String messageJson) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("直营分货单生成订货单----------------->" + jsonObject.toJSONString());
//        //转换入参
//        DirDistributionInitOrderIn dirDistributionInitOrderIn = JSON.parseObject(jsonObject.toJSONString(), DirDistributionInitOrderIn.class);
//        if (Objects.isNull(dirDistributionInitOrderIn)) {
//            log.info("当前无分货单生成订货单任务----------------------------------");
//            return "";
//        }
//        //生成订货单
//        String message = dirHandleDistributionOrderService.handlePurchaseListForDistributionOrderOld(
//                dirDistributionInitOrderIn.getDistributionOrderId(),
//                dirDistributionInitOrderIn.getEffectiveTime(),
//                dirDistributionInitOrderIn.getLoginUsername(),
//                dirDistributionInitOrderIn.getBizOrgCode());
//
//        if (StringUtils.isNotBlank(message)) {
//            log.info("直营分货单生成订货单任务失败：----------------------------------");
//            return message.concat("分货失败");
//        }
//        return message;
//    }

    /**
     * 配货单回传
     *
     * @param unificationBillVO
     * @return
     */
    @Override
    public boolean unificationOrderCallBack(UnificationBillVO unificationBillVO) {
        //转换入参
//        UnificationBillVO unificationBillVO = JSON.parseObject(messageJson, UnificationBillVO.class);
        OrdDirDelivery ordDirDelivery = new OrdDirDelivery();
        ordDirDelivery.setDeliveryOrderNo(unificationBillVO.getFsrcnum());
        OrdDirDelivery dirDelivery = ordDirDeliveryService.selectOne(ordDirDelivery);
        if (Objects.isNull(dirDelivery)) {
            log.error("此配货单不存在｛｝", unificationBillVO.getFsrcnum());
//            return "此配货单不存在" + unificationBillVO.getFsrcnum();
            return true;
        }
        if (DeliveryOrderEnum.SHIPPED.getKey().equals(dirDelivery.getDeliveryStatusCode())) {
            log.error("此配货单后台已发货｛｝", unificationBillVO.getFsrcnum());
//            return "此配货单后台已发货" + unificationBillVO.getFsrcnum();
            return true;
        }
        if (!DeliveryOrderEnum.APPROVED.getKey().equals(dirDelivery.getDeliveryStatusCode())) {
            log.error("此配货单状态不正确｛｝", unificationBillVO.getFsrcnum());
//            return "此配货单状态不正确" + unificationBillVO.getFsrcnum();
            return true;
        }
        //查询明细
        OrdDirDeliveryDetail ordDirDeliveryDetail = new OrdDirDeliveryDetail();
        ordDirDeliveryDetail.setDeliveryOrderId(dirDelivery.getId());
        ordDirDeliveryDetail.setIsDelete(ModelConst.DELETE.NO);
        List<OrdDirDeliveryDetail> ordDirDeliveryDetails = ordDirDeliveryDetailService.list(ordDirDeliveryDetail);
        List<String> goodsCodeList = ordDirDeliveryDetails.stream().map(OrdDirDeliveryDetail::getGoodsCode).collect(Collectors.toList());
        List<StandardGoodsInfoOut> standardGoodsInfoOutList = orderGoodsServer.findAllByGoodsCodeList(goodsCodeList);
        Map<String, StandardGoodsInfoOut> standardGoodsMap = standardGoodsInfoOutList.stream().collect(Collectors.toMap(StandardGoodsInfoOut::getGoodsCode, Function.identity()));
        return dirDeliveryOrderHandle.unificationOrderCallBack(dirDelivery, ordDirDeliveryDetails, unificationBillVO, standardGoodsMap);
    }

    /**
     * 退货单回传
     *
     * @param unificationReBillVO
     * @return
     */
    @Override
    public boolean unificationReOrderCallBack(UnificationReBillVO unificationReBillVO) {
        return ordDirReturnService.unificationReOrderCallBack(unificationReBillVO);
    }

    /**
     * 差异单回传
     *
     * @param differenceBillVO
     * @return
     */
    @Override
    public boolean differenceOrderCallBack(DifferenceBillVO differenceBillVO) {
        return ordDirDelivDifferenceService.differenceOrderCallBack(differenceBillVO);
    }

//    @Override
//    public String wholesaleReOrderCallBack(String messageJson) {
//        return "成功";
//    }

    @Override
    public String wholesaleOrderCallBack(String messageJson) {
        return "成功";
    }

    @Override
    public boolean execOrderDeliveryDataFile(OrdDeliveryDataFile ordDeliveryDataFile) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("配货单、配货退货单创建成功后入参----------------->" + jsonObject.toJSONString());
//        OrdDeliveryDataFile ordDeliveryDataFile = JSON.parseObject(jsonObject.toJSONString(), OrdDeliveryDataFile.class);
        try {
            return ordDirDeliveryDataFileService.execFile(ordDeliveryDataFile);
        } catch (Exception e) {
            log.error("执行配货数据上传文件失败", e);
            ordDeliveryDataFile.setRemark(e.getMessage());
            ordDirDeliveryDataFileService.overDataAFile(ordDeliveryDataFile);
            throw e;
        }
//        return "执行成功";
    }

    @Override
    public boolean dirDistributionCreateOrder(OrdDirOrderDistribution orderDistribution) {
//        JSONObject jsonObject = JSONObject.parseObject(messageJson);
//        log.info("直营分货单生成订货单----------------->" + jsonObject.toJSONString());
//        //转换入参
//        OrdDirOrderDistribution orderDistribution = JSON.parseObject(jsonObject.toJSONString(), OrdDirOrderDistribution.class);
//        if (Objects.isNull(orderDistribution)) {
//            log.info("当前无分货单生成订货单任务----------------------------------");
//            return "";
//        }
        //生成订货单
        String message = dirHandleDistributionOrderService.handleDistributionCreateOrder(orderDistribution.getId(), orderDistribution.getUpdater());
        if (StringUtils.isNotBlank(message)) {
            log.info("直营分货单{}生成订货单任务失败：----------------------------------", orderDistribution.getDistributionOrderNo());
//            return message.concat("分货失败");
        }
        return true;
    }
}
