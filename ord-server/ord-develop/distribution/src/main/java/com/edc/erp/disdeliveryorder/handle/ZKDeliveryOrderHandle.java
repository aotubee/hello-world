package com.edc.erp.disdeliveryorder.handle;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.DeliveryOrderLogEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.model.in.zk.ZKDeliveryOrderDetailIn;
import com.edc.erp.common.model.in.zk.ZKDeliveryOrderIn;
import com.edc.erp.common.model.out.zk.ZkGoodsOut;
import com.edc.erp.common.service.ZKServer;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.enumeration.ZKPositionEnum;
import com.edc.erp.disdeliveryorder.enumeration.ZKReturnStockTypeEnum;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.plugins.common.response.Response;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-02-25 10:33
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ZKDeliveryOrderHandle {

    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    private final AsyncLogService asyncLogService;

    private final ZKServer zkServer;

    private final OrdDisDeliveryService ordDisDeliveryService;


    /**
     * @param ordDisDelivery:
     * @param loginUsername:
     * @Description: 处理推送中科前
     * @Author: ZhangYao
     * @Date: 2023/8/10 13:39
     * @return: com.edc.erp.common.model.in.zk.ZKDeliveryOrderIn
     **/
    @Transactional(rollbackFor = Exception.class)
    public Response<ZKDeliveryOrderIn> handleBeforeSendDeliveryOrderToZK(OrdDisDelivery ordDisDelivery, String loginUsername) {
        ZKDeliveryOrderIn zkDeliveryOrderIn = new ZKDeliveryOrderIn();
        zkDeliveryOrderIn.setSource_order_no(ordDisDelivery.getDeliveryOrderNo());
        zkDeliveryOrderIn.setStock_type(ZKReturnStockTypeEnum.ZK_TEMPERATURE.getZkStockType());
        String branchNo;
        String zkMemo = ordDisDelivery.getStockCode() + "-" + ZKPositionEnum.getNameByCode(ordDisDelivery.getStockCode()) + "订货仓";
        if (ZKPositionEnum.ZK_FREEZE_LOW.getCode().equals(ordDisDelivery.getStockCode())) {
            branchNo = ZKPositionEnum.ZK_ROOM_TEMPERATURE.getCode();
        } else {
            branchNo = ordDisDelivery.getStockCode();
        }
        zkDeliveryOrderIn.setBranch_no(branchNo);
        zkDeliveryOrderIn.setD_branch_no(ordDisDelivery.getStoreCode());
        zkDeliveryOrderIn.setOper_id(ordDisDelivery.getCreator());
        zkDeliveryOrderIn.setDelivery_type("0");
        zkDeliveryOrderIn.setMemo(zkMemo + ":加盟" + DistributionWaysEnum.getNameByType(ordDisDelivery.getDistributionType()));
        List<ZKDeliveryOrderDetailIn> items = Lists.newArrayList();
        List<OrdDisDeliveryDetail> deliveryOrderDetailsList = ordDisDeliveryDetailService.findDeliveryOrderDetails(ordDisDelivery.getId());
        List<OrdDisDeliveryDetail> updateOtherSkuCodeList = Lists.newArrayList();
        StringJoiner emptyGoodsJoiner = new StringJoiner(SystemConstant.COMMA);
        ordDisDelivery.setDistributionQuantity(BigDecimal.ZERO);
        ordDisDelivery.setDistributionAmount(BigDecimal.ZERO);
        deliveryOrderDetailsList.forEach(deliveryOrderDetails -> {
            ZKDeliveryOrderDetailIn zkDeliveryOrderDetailIn = new ZKDeliveryOrderDetailIn();
            ZkGoodsOut zkGoods = zkServer.getZkGoodsByGoodsCodeAndBizOrgCode(deliveryOrderDetails.getGoodsCode(), ordDisDelivery.getBizOrgCode());
            if (Objects.isNull(zkGoods)) {
                emptyGoodsJoiner.add(deliveryOrderDetails.getGoodsCode());
                return;
            }
            String otherSkuCode = zkGoods.getZkSkuCode();
            zkDeliveryOrderDetailIn.setSource_order_no(ordDisDelivery.getDeliveryOrderNo());
            zkDeliveryOrderDetailIn.setItem_no(otherSkuCode);
            zkDeliveryOrderDetailIn.setReal_qty(deliveryOrderDetails.getOrderQuantity());
            zkDeliveryOrderDetailIn.setValid_price(deliveryOrderDetails.getOrderUnitPrice());
            zkDeliveryOrderDetailIn.setSub_amt(zkDeliveryOrderDetailIn.getValid_price().multiply(zkDeliveryOrderDetailIn.getReal_qty()).setScale(4, RoundingMode.DOWN));
            zkDeliveryOrderDetailIn.setLine(deliveryOrderDetails.getLine());
            items.add(zkDeliveryOrderDetailIn);

            OrdDisDeliveryDetail updateDirectDeliveryOrderDetails = new OrdDisDeliveryDetail();
            updateDirectDeliveryOrderDetails.setId(deliveryOrderDetails.getId());
            updateDirectDeliveryOrderDetails.setOtherGoodsCode(otherSkuCode);
            updateDirectDeliveryOrderDetails.setUpdateTime(LocalDateTime.now());
            updateDirectDeliveryOrderDetails.setUpdater(loginUsername);
            updateDirectDeliveryOrderDetails.setDistributionQuantity(deliveryOrderDetails.getOrderQuantity());
            updateDirectDeliveryOrderDetails.setDistributionPackageQuantity(deliveryOrderDetails.getOrderPackageQuantity());
            updateDirectDeliveryOrderDetails.setDistributionAmount(deliveryOrderDetails.getOrderAmount());
            updateOtherSkuCodeList.add(updateDirectDeliveryOrderDetails);
            ordDisDelivery.setDistributionQuantity(ordDisDelivery.getDistributionQuantity().add(updateDirectDeliveryOrderDetails.getDistributionQuantity()));
            ordDisDelivery.setDistributionAmount(ordDisDelivery.getDistributionAmount().add(updateDirectDeliveryOrderDetails.getDistributionAmount()));
        });
        zkDeliveryOrderIn.setDetail_list(items);
        String beforeStatusCode = ordDisDelivery.getDeliveryStatusCode();
        ordDisDelivery.setDeliveryStatusCode(DeliveryOrderEnum.APPROVED.getKey());
        ordDisDelivery.setUpdater(loginUsername);
        ordDisDelivery.setUpdateTime(LocalDateTime.now());
        ordDisDeliveryService.updateByPrimaryKeySelective(ordDisDelivery);
        String content;
        if (emptyGoodsJoiner.length() > 0) {
            content = MessageFormat.format(DeliveryOrderLogEnum.CUT_DELIVERY_ORDER_ZK_EMPTY_GOODS.getKey(),
                    zkDeliveryOrderIn.getSource_order_no(), DeliveryOrderEnum.getValueByKey(beforeStatusCode),
                    DeliveryOrderEnum.getValueByKey(ordDisDelivery.getDeliveryStatusCode()), emptyGoodsJoiner.toString());
        } else {
            content = MessageFormat.format(DeliveryOrderLogEnum.CUT_DELIVERY_BEFORE_SEND_ZK.getKey(), ordDisDelivery.getDeliveryOrderNo(),
                    DeliveryOrderEnum.getValueByKey(beforeStatusCode), DeliveryOrderEnum.getValueByKey(ordDisDelivery.getDeliveryStatusCode()));
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(ordDisDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), loginUsername);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        if (CollectionUtils.isEmpty(updateOtherSkuCodeList)) {
            log.info("截单推送中科配货单{}没有商品映射，故不推中科,配销单作废处理", ordDisDelivery.getDeliveryOrderNo());
            ordDisDeliveryService.invalidDisDeliverOrder(ordDisDelivery.getId(), loginUsername, true);
            content = DeliveryOrderLogEnum.ORD_DIS_DELIVERY_EMPTY_OTHER_GOODS.getKey();
            businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(ordDisDelivery.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), loginUsername);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.error("截单推送中科配销单没有商品映射,作废不推中科");
        }
        // 同步三方商品代码
        ordDisDeliveryDetailService.batchUpdateForSendZk(updateOtherSkuCodeList);
        return Response.data(zkDeliveryOrderIn);
    }

    /**
     * @param zkDeliveryOrderIn:
     * @param deliveryOrderId:
     * @param loginUsername:
     * @Description: 推送中科
     * @Author: ZhangYao
     * @Date: 2023/8/10 13:40
     * @return: boolean
     **/
    public String sendDeliveryOrderToZK(ZKDeliveryOrderIn zkDeliveryOrderIn, Long deliveryOrderId, String loginUsername) {
        log.info("截单推送中科入参---------{}", JSONObject.toJSONString(zkDeliveryOrderIn));
        Response<String> response = zkServer.sendDeliveryOrderToZk(zkDeliveryOrderIn);
        log.info("配货单{}中科要货单请求返回---------->{}", zkDeliveryOrderIn.getSource_order_no(), JSONObject.toJSONString(response));
        if (Objects.nonNull(response) && response.isSuccess()) {
            String content = MessageFormat.format(DeliveryOrderLogEnum.CUT_DELIVERY_ORDER_ZK.getKey(), zkDeliveryOrderIn.getSource_order_no());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(deliveryOrderId),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), loginUsername);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        // 209为中科返回创建单据失败码
        if (Objects.nonNull(response) && "209".equals(response.getResultCode())) {
            log.error("配销单{}推送中科失败{}", zkDeliveryOrderIn.getSource_order_no(), response.getMessage());
            return response.getMessage();
        }
        return "success";
    }
}
