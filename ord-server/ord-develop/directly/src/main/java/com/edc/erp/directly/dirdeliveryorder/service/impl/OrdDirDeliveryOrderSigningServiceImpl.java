package com.edc.erp.directly.dirdeliveryorder.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderAttachment;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderSigning;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryOrderSigningMapper;
import com.edc.erp.directly.dirdeliveryorder.model.in.CacheTakeDirDeliveryOrderGoodsIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.DirSignDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderAttachmentService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderSigningService;
import com.edc.erp.directly.enumeration.DeliveryOrderLogEnum;
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
 * 配货单签收(OrdDirDeliveryOrderSigning)表服务实现类
 *
 * @author weichao
 * @since 2022-11-21 11:15:37
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDirDeliveryOrderSigningServiceImpl extends BaseServiceImpl<OrdDirDeliveryOrderSigning> implements OrdDirDeliveryOrderSigningService {

    private final OrdDirDeliveryOrderSigningMapper ordDirDeliveryOrderSigningMapper;

    private final AsyncLogService asyncLogService;

    private final OrdDirDeliveryOrderAttachmentService ordDirDeliveryOrderAttachmentService;

    private final RedisService redisService;

    @Override
    public OrdDirDeliveryOrderSigning getDeliveryOrderSigningByDeliveryOrderId(Long dirDeliveryOrderId, String bizOrgCode) {
        OrdDirDeliveryOrderSigning ordDirDeliveryOrderSigning = new OrdDirDeliveryOrderSigning();
        ordDirDeliveryOrderSigning.setBizOrgCode(bizOrgCode);
        ordDirDeliveryOrderSigning.setDirDeliveryOrderId(dirDeliveryOrderId);
        ordDirDeliveryOrderSigning.setIsDelete(ModelConst.DELETE.NO);
        return ordDirDeliveryOrderSigningMapper.selectOne(ordDirDeliveryOrderSigning);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> signDirDeliveryOrder(DirSignDeliveryOrderIn signDeliveryOrderIn, OrdDirDelivery deliveryOrder, String loginUsername) {
        OrdDirDeliveryOrderSigning deliveryOrderSigning = this.getDeliveryOrderSigningByDeliveryOrderId(signDeliveryOrderIn.getDeliveryOrderId(), signDeliveryOrderIn.getBizOrgCode());
        if (Objects.nonNull(deliveryOrderSigning)) {
            return Response.success("该配货单已签收");
        }
        String key = DirSystemConstant.CHECK_DIR_SIGN_DIR_DELIVERY_ORDER + deliveryOrder.getBizOrgCode() +
                SystemConstant.COLON + deliveryOrder.getStoreCode() + SystemConstant.WAIT + deliveryOrder.getDeliveryOrderNo();
        if (!redisService.setIfAbsent(key, deliveryOrder.getDeliveryOrderNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("门店" + deliveryOrder.getStoreCode() + "短时间内异常重复签收配货单" + deliveryOrder.getDeliveryOrderNo() + "，故判为无效提交！");
        }
        deliveryOrderSigning = new OrdDirDeliveryOrderSigning();
        BeanUtils.copy(signDeliveryOrderIn, deliveryOrderSigning);
        deliveryOrderSigning.setDirDeliveryOrderId(signDeliveryOrderIn.getDeliveryOrderId());
        deliveryOrderSigning.setErpStoreCode(deliveryOrder.getStoreCode());
        deliveryOrderSigning.setBizOrgCode(deliveryOrder.getBizOrgCode());
        deliveryOrderSigning.setOrgCode(deliveryOrder.getOrgCode());
        deliveryOrderSigning.setCreator(loginUsername);
        deliveryOrderSigning.setUpdater(loginUsername);
        deliveryOrderSigning.setIsDelete(0);
        ordDirDeliveryOrderSigningMapper.insert(deliveryOrderSigning);
        if (CollectionUtils.isNotEmpty(signDeliveryOrderIn.getAttachmentUrlList())) {
            List<OrdDirDeliveryOrderAttachment> deliveryOrderAttachmentList = Lists.newArrayList();
            signDeliveryOrderIn.getAttachmentUrlList().forEach(url -> {
                OrdDirDeliveryOrderAttachment deliveryOrderAttachment = new OrdDirDeliveryOrderAttachment();
                deliveryOrderAttachment.setDirDeliveryOrderId(deliveryOrder.getId());
                deliveryOrderAttachment.setAttachmentType(1);
                deliveryOrderAttachment.setAttachmentUrl(url);
                deliveryOrderAttachment.setCreator(loginUsername);
                deliveryOrderAttachment.setCreateTime(LocalDateTime.now());
                deliveryOrderAttachment.setBizOrgCode(deliveryOrder.getBizOrgCode());
                deliveryOrderAttachment.setOrgCode(deliveryOrder.getOrgCode());
                deliveryOrderAttachmentList.add(deliveryOrderAttachment);
            });
            ordDirDeliveryOrderAttachmentService.batchSaveDeliveryOrderAttachment(deliveryOrderAttachmentList);
        }
        if (StringUtils.isNotBlank(deliveryOrderSigning.getDifferencesRemark())) {
            String content = MessageFormat.format(DeliveryOrderLogEnum.SIGNING_DELIVERY_ORDER.getKey(), deliveryOrderSigning.getDifferencesRemark());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(), String.valueOf(deliveryOrder.getId()),
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(), content, new Date(), loginUsername);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        this.copyBeforeTakeDeliveryData(deliveryOrder.getDeliveryOrderNo(), deliveryOrder.getBizOrgCode());
        log.info("配货单{}覆盖签收高值整件缓存至收货缓存数据成功。", deliveryOrder.getDeliveryOrderNo());
        return Response.success("签收成功");
    }

    @Override
    public Response<String> submitBeforeTakeDirDeliveryInfoToCache(OrdDirDelivery deliveryOrder, List<CacheTakeDirDeliveryOrderGoodsIn> cacheTakeDirDeliveryOrderGoodsInList) {
        cacheTakeDirDeliveryOrderGoodsInList.forEach(cacheTakeDeliveryOrderGoodsIn -> {
            String key = DirSystemConstant.DIR_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY + ":" + deliveryOrder.getBizOrgCode() + ":" + deliveryOrder.getDeliveryOrderNo();
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
        String beforeTakeDeliveryKey = DirSystemConstant.DIR_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY + ":" + bizOrgCode + ":" + deliveryOrderNo;
        Map<Object, Object> beforeTakeDeliveryCatchDataMap = redisService.hGet(beforeTakeDeliveryKey);
        if (null != beforeTakeDeliveryCatchDataMap) {
            Map<String, Object> newTakeDeliveryCatchDataMap = new HashMap<>();
            beforeTakeDeliveryCatchDataMap.forEach((k, v) -> newTakeDeliveryCatchDataMap.put(k.toString(), v));
            String key = DirSystemConstant.DIR_CACHE_TAKE_DELIVERY_ORDER_KEY + ":" + bizOrgCode + ":" + deliveryOrderNo;
            redisService.hSet(key, newTakeDeliveryCatchDataMap);
            redisService.del(DirSystemConstant.DIR_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY + ":" + bizOrgCode + ":" + deliveryOrderNo);
        }
    }
}
