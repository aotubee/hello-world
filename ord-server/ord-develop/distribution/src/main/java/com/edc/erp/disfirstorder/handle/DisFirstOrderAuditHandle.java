package com.edc.erp.disfirstorder.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.FirstOrderStatusEnum;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDetail;
import com.edc.erp.disfirstorder.mapper.OrdDisOrderFirstMapper;
import com.edc.erp.disfirstorder.model.excel.OrdDisFirstOrderImportErrorResult;
import com.edc.erp.disfirstorder.model.in.OrdDisFirstOrderAuditIn;
import com.edc.erp.disfirstorder.model.in.OrdDisFirstOrderDetailAuditIn;
import com.edc.erp.disfirstorder.model.out.DisBeforeAsyncAuditFirstOrderOut;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDetailService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName DirDistibutionOrderExportHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/2 14:37
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class DisFirstOrderAuditHandle {

    private final OrdDisOrderFirstDetailService ordDisOrderFirstDetailService;

    private final AsyncExportHandle asyncExportHandle;

    private final OrdDisOrderFirstMapper ordDisOrderFirstMapper;

    private final RedisService redisService;

    private final OrderGoodsServer orderGoodsServer;

    public DisBeforeAsyncAuditFirstOrderOut beforeAsyncAudit(OrdDisOrderFirst ordDisOrderFirst) {
        List<OrdDisOrderFirstDetail> detailList = ordDisOrderFirstDetailService.findAllByFirstOrderId(ordDisOrderFirst.getId());
        OrderGoodsIn goodsIn = new OrderGoodsIn();
        goodsIn.setStoreCode(ordDisOrderFirst.getStoreCode());
        goodsIn.setBizOrgCode(ordDisOrderFirst.getBizOrgCode());
        List<String> goodsCodeList = detailList.stream().map(OrdDisOrderFirstDetail::getGoodsCode).collect(Collectors.toList());
        goodsIn.setGoodsCodeList(goodsCodeList);
        List<OrderGoodsOut> importOrderGoodsOutList = orderGoodsServer.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
        if (CollectionUtils.isNotEmpty(importOrderGoodsOutList)) {
            Map<String, OrderGoodsOut> goodsMap = importOrderGoodsOutList.stream().collect(Collectors.toMap(OrderGoodsOut::getGoodsCode, Function.identity()));
            DisBeforeAsyncAuditFirstOrderOut beforeAsyncAuditFirstOrderOut = new DisBeforeAsyncAuditFirstOrderOut();
            beforeAsyncAuditFirstOrderOut.setOrdDisOrderFirstDetailList(detailList);
            beforeAsyncAuditFirstOrderOut.setGoodsMap(goodsMap);
            return beforeAsyncAuditFirstOrderOut;
        }
        return null;
    }

    @Async
    public void asyncCheckByAuditFirstOrder(OrdDisFirstOrderAuditIn ordDisFirstOrderAuditIn, String loginUsername, String key) {
        OrdDisOrderFirst ordDisOrderFirst = ordDisOrderFirstMapper.selectByPrimaryKey(ordDisFirstOrderAuditIn.getFirstOrderId());
        try {
            DisBeforeAsyncAuditFirstOrderOut beforeAsyncAuditFirstOrderOut = this.beforeAsyncAudit(ordDisOrderFirst);
            LocalDateTime effectiveTime;
            //立即生效则创建订货单任务
            if (NumberUtil.INTEGER_ONE.equals(ordDisFirstOrderAuditIn.getIsEffectiveImmediately())) {
                effectiveTime = LocalDateTime.now();
            } else {
                effectiveTime = ordDisFirstOrderAuditIn.getEffectiveTime();
            }
            Map<String, Integer> importGoodsNumMap = ordDisFirstOrderAuditIn.getDetailAuditInList().stream().collect(Collectors.toMap(OrdDisFirstOrderDetailAuditIn::getGoodsCode, OrdDisFirstOrderDetailAuditIn::getNum));
            Map<String, OrderGoodsOut> goodsMap = beforeAsyncAuditFirstOrderOut.getGoodsMap();
            List<OrdDisFirstOrderImportErrorResult> errorResultList = ordDisOrderFirstDetailService.checkDetailByAudit(ordDisOrderFirst.getStoreCode(),
                    ordDisOrderFirst.getBizOrgCode(), beforeAsyncAuditFirstOrderOut.getOrdDisOrderFirstDetailList(), goodsMap, importGoodsNumMap);
            List<OrdDisOrderFirstDetail> updateDetailList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(errorResultList)) {
                //            ordDirOrderFirst.setRemark("请下载审核问题清单查看");
                //            ordDirOrderFirstMapper.updateByPrimaryKey(ordDirOrderFirst);
                String sheetName = "配销铺货单【" + ordDisOrderFirst.getFirstOrderNo() + "】审核问题清单";
                String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
                String asyncUrl = asyncExportHandle.asyncExport(fileName, sheetName, loginUsername,
                        errorResultList, OrdDisFirstOrderImportErrorResult.class);
                log.info("铺货单审核校验异常{}", asyncUrl);
            } else {
                ordDisOrderFirst.setUpdater(loginUsername);
                ordDisOrderFirst.setUpdateTime(LocalDateTime.now());
                ordDisOrderFirst.setEffectiveTime(effectiveTime);
                ordDisOrderFirst.setIsEffectiveImmediately(ordDisFirstOrderAuditIn.getIsEffectiveImmediately());
                ordDisOrderFirst.setFirstOrderStatus(FirstOrderStatusEnum.APPROVED.getCode());
                AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<Integer> totalTotalNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
                AtomicReference<Integer> goodsNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
                ordDisFirstOrderAuditIn.getDetailAuditInList().forEach(detail -> {
                    if (Objects.isNull(detail.getId())) {
                        log.info("铺货单{}占坑明细{}，故跳过该商品", ordDisOrderFirst.getFirstOrderNo(), detail.getGoodsCode());
                        return;
                    }
                    OrdDisOrderFirstDetail disOrderFirstDetail = ordDisOrderFirstDetailService.selectByPrimaryKey(detail.getId());
                    if (Objects.isNull(disOrderFirstDetail)) {
                        log.info("分货单{}不存在的明细{}", ordDisOrderFirst.getFirstOrderNo(), detail.getGoodsCode());
                        return;
                    }
                    OrderGoodsOut goodsOut = goodsMap.get(detail.getGoodsCode());
                    if (Objects.isNull(goodsOut)) {
                        log.info("铺货单{}明细{}查询不存在，故跳过该商品", ordDisOrderFirst.getFirstOrderNo(), detail.getGoodsCode());
                        return;
                    }
                    disOrderFirstDetail.setNum(detail.getNum());
                    // 铺货金额
                    BigDecimal amount = goodsOut.getDistributionUnitPrice().multiply(BigDecimal.valueOf(detail.getNum()));
                    disOrderFirstDetail.setAmount(amount.setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    disOrderFirstDetail.setUpdater(loginUsername);
                    disOrderFirstDetail.setUpdateTime(LocalDateTime.now());
                    updateDetailList.add(disOrderFirstDetail);
                    totalAmount.getAndSet(totalAmount.get().add(disOrderFirstDetail.getAmount()));
                    totalTotalNum.set(totalTotalNum.get() + detail.getNum());
                    goodsNum.set(goodsNum.get() + NumberUtil.INTEGER_ONE);
                });
                ordDisOrderFirst.setApprovalTime(LocalDateTime.now());
                ordDisOrderFirst.setApprover(loginUsername);
                ordDisOrderFirst.setTotalAmount(totalAmount.get().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
                ordDisOrderFirst.setTotalNum(totalTotalNum.get());
                ordDisOrderFirst.setGoodsNum(goodsNum.get());
                ordDisOrderFirst.setUpdater(loginUsername);
                ordDisOrderFirst.setUpdateTime(LocalDateTime.now());
                // 审核后更新数据
                ordDisOrderFirstDetailService.updateAfterAuditSuccess(updateDetailList, ordDisOrderFirst);
                log.info("铺货单流转至创建配销单任务...");
            }
        } catch (Exception e) {
            log.error("铺货单{}审核异步校验异常", ordDisOrderFirst.getFirstOrderNo(), e);
            OrdDisFirstOrderImportErrorResult errorResult = new OrdDisFirstOrderImportErrorResult();
            errorResult.setGoodsCode(SystemConstant.SHORT_LINE);
            errorResult.setErrorMessage("审核异常，请联系管理员");
            String sheetName = "配销分货单【" + ordDisOrderFirst.getFirstOrderNo() + "】审核异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDisFirstOrderImportErrorResult.class);
        } finally {
            redisService.del(key);
        }
    }
}
