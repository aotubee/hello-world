package com.edc.erp.disdeliveryorder.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DeliveryOrderLogEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderAttachment;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderSigning;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryOrderSigningMapper;
import com.edc.erp.disdeliveryorder.model.in.CacheTakeDisDeliveryOrderGoodsIn;
import com.edc.erp.disdeliveryorder.model.in.DisSignDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderAttachmentService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderSigningService;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * 配销单签收(OrdDisDeliveryOrderSigning)表服务实现类
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisDeliveryOrderSigningServiceImpl extends BaseServiceImpl<OrdDisDeliveryOrderSigning> implements OrdDisDeliveryOrderSigningService {

    private final OrdDisDeliveryOrderSigningMapper ordDisDeliveryOrderSigningMapper;

    private final OrdDisDeliveryOrderAttachmentService ordDisDeliveryOrderAttachmentService;

    private final AsyncLogService asyncLogService;

    private final RedisService redisService;

    /**
     * 查询配销单签收表信息
     *
     * @param disDeliveryOrderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public OrdDisDeliveryOrderSigning getDeliveryOrderSigningByDeliveryOrderId(Long disDeliveryOrderId, String bizOrgCode) {
        OrdDisDeliveryOrderSigning ordDisDeliveryOrderSigning = new OrdDisDeliveryOrderSigning();
        ordDisDeliveryOrderSigning.setBizOrgCode(bizOrgCode);
        ordDisDeliveryOrderSigning.setDisDeliveryOrderId(disDeliveryOrderId);
        ordDisDeliveryOrderSigning.setIsDelete(ModelConst.DELETE.NO);
        return ordDisDeliveryOrderSigningMapper.selectOne(ordDisDeliveryOrderSigning);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> signDisDeliveryOrder(DisSignDeliveryOrderIn signDeliveryOrderIn, OrdDisDelivery deliveryOrder, String loginUsername) {
        OrdDisDeliveryOrderSigning deliveryOrderSigning = this.getDeliveryOrderSigningByDeliveryOrderId(signDeliveryOrderIn.getDeliveryOrderId(), signDeliveryOrderIn.getBizOrgCode());
        if (Objects.nonNull(deliveryOrderSigning)) {
            return Response.success("该配销单已签收");
        }
        String key = DisSystemConstant.CHECK_DIS_SIGN_DIR_DELIVERY_ORDER + deliveryOrder.getBizOrgCode() +
                SystemConstant.COLON + deliveryOrder.getStoreCode() + SystemConstant.WAIT + deliveryOrder.getDeliveryOrderNo();
        if (!redisService.setIfAbsent(key, deliveryOrder.getDeliveryOrderNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("门店" + deliveryOrder.getStoreCode() + "短时间内异常重复签收配销单" + deliveryOrder.getDeliveryOrderNo() + "，故判为无效提交！");
        }
        deliveryOrderSigning = new OrdDisDeliveryOrderSigning();
        BeanUtils.copy(signDeliveryOrderIn, deliveryOrderSigning);
        deliveryOrderSigning.setDisDeliveryOrderId(signDeliveryOrderIn.getDeliveryOrderId());
        deliveryOrderSigning.setErpStoreCode(deliveryOrder.getStoreCode());
        deliveryOrderSigning.setBizOrgCode(deliveryOrder.getBizOrgCode());
        deliveryOrderSigning.setOrgCode(deliveryOrder.getOrgCode());
        deliveryOrderSigning.setCreator(loginUsername);
        deliveryOrderSigning.setUpdater(loginUsername);
        deliveryOrderSigning.setIsDelete(0);
        ordDisDeliveryOrderSigningMapper.insert(deliveryOrderSigning);
        if (CollectionUtils.isNotEmpty(signDeliveryOrderIn.getAttachmentUrlList())) {
            List<OrdDisDeliveryOrderAttachment> deliveryOrderAttachmentList = Lists.newArrayList();
            signDeliveryOrderIn.getAttachmentUrlList().forEach(url -> {
                OrdDisDeliveryOrderAttachment deliveryOrderAttachment = new OrdDisDeliveryOrderAttachment();
                deliveryOrderAttachment.setDisDeliveryOrderId(deliveryOrder.getId());
                deliveryOrderAttachment.setAttachmentType(1);
                deliveryOrderAttachment.setAttachmentUrl(url);
                deliveryOrderAttachment.setCreator(loginUsername);
                deliveryOrderAttachment.setCreateTime(LocalDateTime.now());
                deliveryOrderAttachment.setBizOrgCode(deliveryOrder.getBizOrgCode());
                deliveryOrderAttachment.setOrgCode(deliveryOrder.getOrgCode());
                deliveryOrderAttachmentList.add(deliveryOrderAttachment);
            });
            ordDisDeliveryOrderAttachmentService.batchSaveDeliveryOrderAttachment(deliveryOrderAttachmentList);
        }
        if (StringUtils.isNotBlank(deliveryOrderSigning.getDifferencesRemark())) {
            String content = MessageFormat.format(DeliveryOrderLogEnum.SIGNING_DELIVERY_ORDER.getKey(), deliveryOrderSigning.getDifferencesRemark());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(deliveryOrder.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), loginUsername);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        this.copyBeforeTakeDeliveryData(deliveryOrder.getDeliveryOrderNo(), deliveryOrder.getBizOrgCode());
        log.info("配销单{}覆盖签收高值整件缓存至收货缓存数据成功。", deliveryOrder.getDeliveryOrderNo());
        return Response.success("签收成功");
    }

    @Override
    public Response<String> submitBeforeTakeDisDeliveryInfoToCache(OrdDisDelivery deliveryOrder, List<CacheTakeDisDeliveryOrderGoodsIn> cacheTakeDisDeliveryOrderGoodsInList) {
        cacheTakeDisDeliveryOrderGoodsInList.forEach(cacheTakeDeliveryOrderGoodsIn -> {
            String key = DisSystemConstant.DIS_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY + ":" + deliveryOrder.getBizOrgCode() + ":" + deliveryOrder.getDeliveryOrderNo();
            redisService.hSet(key, cacheTakeDeliveryOrderGoodsIn.getDeliveryOrderDetailsId().toString(), JSONObject.toJSONString(cacheTakeDeliveryOrderGoodsIn));
        });
        return Response.success();
    }

    /**
     * 复制签收时收货前高值散件缓存数据至收货缓存数据中
     *
     * @param deliveryOrderNo
     * @param bizOrgCode
     */
    private void copyBeforeTakeDeliveryData(String deliveryOrderNo, String bizOrgCode) {
        String beforeTakeDeliveryKey = DisSystemConstant.DIS_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY + ":" + bizOrgCode + ":" + deliveryOrderNo;
        Map<Object, Object> beforeTakeDeliveryCatchDataMap = redisService.hGet(beforeTakeDeliveryKey);
        if (null != beforeTakeDeliveryCatchDataMap) {
            Map<String, Object> newTakeDeliveryCatchDataMap = new HashMap<>();
            beforeTakeDeliveryCatchDataMap.forEach((k, v) -> newTakeDeliveryCatchDataMap.put(k.toString(), v));
            String key = DisSystemConstant.DIS_CACHE_TAKE_DELIVERY_ORDER_KEY + ":" + bizOrgCode + ":" + deliveryOrderNo;
            redisService.hSet(key, newTakeDeliveryCatchDataMap);
            redisService.del(DisSystemConstant.DIS_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY + ":" + bizOrgCode + ":" + deliveryOrderNo);
        }
    }
}
