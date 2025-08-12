package com.edc.erp.directly.dirfirstorder.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.FirstOrderStatusEnum;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDetail;
import com.edc.erp.directly.dirfirstorder.mapper.OrdDirOrderFirstMapper;
import com.edc.erp.directly.dirfirstorder.model.excel.OrdDirFirstOrderImportErrorResult;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirFirstOrderAuditIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirFirstOrderDetailAuditIn;
import com.edc.erp.directly.dirfirstorder.model.out.BeforeAsyncAuditFirstOrderOut;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstDetailService;
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
public class DirFirstOrderAuditHandle {

    private final OrdDirOrderFirstDetailService ordDirOrderFirstDetailService;

    private final AsyncExportHandle asyncExportHandle;

    private final OrdDirOrderFirstMapper ordDirOrderFirstMapper;

    private final RedisService redisService;

    private final OrderGoodsServer orderGoodsServer;

    public BeforeAsyncAuditFirstOrderOut beforeAsyncAudit(OrdDirOrderFirst ordDirOrderFirst) {
        List<OrdDirOrderFirstDetail> detailList = ordDirOrderFirstDetailService.findAllByFirstOrderId(ordDirOrderFirst.getId());
        OrderGoodsIn goodsIn = new OrderGoodsIn();
        goodsIn.setStoreCode(ordDirOrderFirst.getStoreCode());
        goodsIn.setBizOrgCode(ordDirOrderFirst.getBizOrgCode());
        List<String> goodsCodeList = detailList.stream().map(OrdDirOrderFirstDetail::getGoodsCode).collect(Collectors.toList());
        goodsIn.setGoodsCodeList(goodsCodeList);
        List<OrderGoodsOut> importOrderGoodsOutList = orderGoodsServer.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
        if (CollectionUtils.isNotEmpty(importOrderGoodsOutList)) {
            Map<String, OrderGoodsOut> goodsMap = importOrderGoodsOutList.stream().collect(Collectors.toMap(OrderGoodsOut::getGoodsCode, Function.identity()));
            BeforeAsyncAuditFirstOrderOut beforeAsyncAuditFirstOrderOut = new BeforeAsyncAuditFirstOrderOut();
            beforeAsyncAuditFirstOrderOut.setOrdDirOrderFirstDetailList(detailList);
            beforeAsyncAuditFirstOrderOut.setGoodsMap(goodsMap);
            return beforeAsyncAuditFirstOrderOut;
        }
        return null;
    }

    @Async
    public void asyncCheckByAuditFirstOrder(OrdDirFirstOrderAuditIn ordDirFirstOrderAuditIn, String loginUsername, String key) {
        OrdDirOrderFirst ordDirOrderFirst = ordDirOrderFirstMapper.selectByPrimaryKey(ordDirFirstOrderAuditIn.getFirstOrderId());
        try {
            BeforeAsyncAuditFirstOrderOut beforeAsyncAuditFirstOrderOut = this.beforeAsyncAudit(ordDirOrderFirst);
            LocalDateTime effectiveTime;
            //立即生效则创建订货单任务
            if (NumberUtil.INTEGER_ONE.equals(ordDirFirstOrderAuditIn.getIsEffectiveImmediately())) {
                effectiveTime = LocalDateTime.now();
            } else {
                effectiveTime = ordDirFirstOrderAuditIn.getEffectiveTime();
            }
            Map<String, Integer> importGoodsNumMap = ordDirFirstOrderAuditIn.getDetailAuditInList().stream().collect(Collectors.toMap(OrdDirFirstOrderDetailAuditIn::getGoodsCode, OrdDirFirstOrderDetailAuditIn::getNum));
            Map<String, OrderGoodsOut> goodsMap = beforeAsyncAuditFirstOrderOut.getGoodsMap();
            List<OrdDirFirstOrderImportErrorResult> errorResultList = ordDirOrderFirstDetailService.checkDetailByAudit(ordDirOrderFirst.getStoreCode(),
                    ordDirOrderFirst.getBizOrgCode(), beforeAsyncAuditFirstOrderOut.getOrdDirOrderFirstDetailList(), goodsMap, importGoodsNumMap);
            List<OrdDirOrderFirstDetail> updateDetailList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(errorResultList)) {
                //            ordDirOrderFirst.setRemark("请下载审核问题清单查看");
                //            ordDirOrderFirstMapper.updateByPrimaryKey(ordDirOrderFirst);
                String sheetName = "直营铺货单【" + ordDirOrderFirst.getFirstOrderNo() + "】审核问题清单";
                String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
                String asyncUrl = asyncExportHandle.asyncExport(fileName, sheetName, loginUsername,
                        errorResultList, OrdDirFirstOrderImportErrorResult.class);
                log.info("铺货单审核校验异常{}", asyncUrl);
            } else {
                ordDirOrderFirst.setUpdater(loginUsername);
                ordDirOrderFirst.setUpdateTime(LocalDateTime.now());
                ordDirOrderFirst.setEffectiveTime(effectiveTime);
                ordDirOrderFirst.setIsEffectiveImmediately(ordDirFirstOrderAuditIn.getIsEffectiveImmediately());
                ordDirOrderFirst.setFirstOrderStatus(FirstOrderStatusEnum.APPROVED.getCode());
                AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<Integer> totalTotalNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
                AtomicReference<Integer> goodsNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
                ordDirFirstOrderAuditIn.getDetailAuditInList().forEach(detail -> {
                    if (Objects.isNull(detail.getId())) {
                        log.info("铺货单{}占坑明细{}，故跳过该商品", ordDirOrderFirst.getFirstOrderNo(), detail.getGoodsCode());
                        return;
                    }
                    OrdDirOrderFirstDetail dirOrderFirstDetail = ordDirOrderFirstDetailService.selectByPrimaryKey(detail.getId());
                    if (Objects.isNull(dirOrderFirstDetail)) {
                        log.info("分货单{}不存在的明细{}", ordDirOrderFirst.getFirstOrderNo(), detail.getGoodsCode());
                        return;
                    }
                    OrderGoodsOut goodsOut = goodsMap.get(detail.getGoodsCode());
                    if (Objects.isNull(goodsOut)) {
                        log.info("铺货单{}明细{}查询不存在，故跳过该商品", ordDirOrderFirst.getFirstOrderNo(), detail.getGoodsCode());
                        return;
                    }
                    dirOrderFirstDetail.setNum(detail.getNum());
                    // 铺货金额
                    BigDecimal amount = goodsOut.getDistributionUnitPrice().multiply(BigDecimal.valueOf(detail.getNum()));
                    dirOrderFirstDetail.setAmount(amount.setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    dirOrderFirstDetail.setUpdater(loginUsername);
                    dirOrderFirstDetail.setUpdateTime(LocalDateTime.now());
                    updateDetailList.add(dirOrderFirstDetail);
                    totalAmount.getAndSet(totalAmount.get().add(dirOrderFirstDetail.getAmount()));
                    totalTotalNum.set(totalTotalNum.get() + detail.getNum());
                    goodsNum.set(goodsNum.get() + NumberUtil.INTEGER_ONE);
                });
                ordDirOrderFirst.setApprovalTime(LocalDateTime.now());
                ordDirOrderFirst.setApprover(loginUsername);
                ordDirOrderFirst.setTotalAmount(totalAmount.get().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
                ordDirOrderFirst.setTotalNum(totalTotalNum.get());
                ordDirOrderFirst.setGoodsNum(goodsNum.get());
                ordDirOrderFirst.setUpdater(loginUsername);
                ordDirOrderFirst.setUpdateTime(LocalDateTime.now());
                // 审核后更新数据
                ordDirOrderFirstDetailService.updateAfterAuditSuccess(updateDetailList, ordDirOrderFirst);
                log.info("铺货单流转至创建配货单任务...");
            }
        } catch (Exception e) {
            log.error("铺货单{}审核异步校验异常", ordDirOrderFirst.getFirstOrderNo(), e);
            OrdDirFirstOrderImportErrorResult errorResult = new OrdDirFirstOrderImportErrorResult();
            errorResult.setGoodsCode(SystemConstant.SHORT_LINE);
            errorResult.setErrorMessage("审核异常，请联系管理员");
            String sheetName = "直营分货单【" + ordDirOrderFirst.getFirstOrderNo() + "】审核异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDirFirstOrderImportErrorResult.class);
        } finally {
            redisService.del(key);
        }
    }
}
