package com.edc.erp.distribution.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionDetail;
import com.edc.erp.distribution.mapper.OrdDisOrderDistributionMapper;
import com.edc.erp.distribution.model.excel.OrdDisDistributionImportErrorResult;
import com.edc.erp.distribution.model.in.OrdDisDistributionAuditIn;
import com.edc.erp.distribution.service.OrdDisOrderDistributionDetailService;
import com.edc.erp.enumeration.OrderDistributionOrderStatusEnum;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @ClassName DirDistibutionOrderExportHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/9/2 14:37
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class DisDistributionOrderAuditHandle {


    private final OrdDisOrderDistributionDetailService ordDisOrderDistributionDetailService;

    private final AsyncExportHandle asyncExportHandle;

    private final OrdDisOrderDistributionMapper ordDisOrderDistributionMapper;

    private final RedisService redisService;


    @Async
    public void asyncCheckByAudit(OrdDisDistributionAuditIn ordDisDistributionAuditIn, String loginUsername, String key) {
        OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistributionMapper.selectByPrimaryKey(ordDisDistributionAuditIn.getDistributionOrderId());
        try {
            List<OrdDisOrderDistributionDetail> detailList = ordDisOrderDistributionDetailService.getDetailByOrdDistributionOrderId(ordDisOrderDistribution.getId());
            LocalDateTime effectiveTime;
            //立即生效则创建订货单任务
            if (NumberUtil.INTEGER_ONE.equals(ordDisDistributionAuditIn.getIsEffectiveImmediately())) {
                effectiveTime = LocalDateTime.now();
            } else {
                effectiveTime = ordDisDistributionAuditIn.getEffectiveTime();
            }
            List<OrdDisDistributionImportErrorResult> errorResultList = ordDisOrderDistributionDetailService.checkDetailByAudit(detailList,
                    ordDisOrderDistribution.getBizOrgCode(), effectiveTime, ordDisOrderDistribution.getDistributionIdentification());
            ordDisOrderDistribution.setUpdater(loginUsername);
            ordDisOrderDistribution.setUpdateTime(LocalDateTime.now());
            List<OrdDisOrderDistributionDetail> updateDetailList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(errorResultList)) {
                ordDisOrderDistribution.setRemark("请下载审核问题清单查看");
                ordDisOrderDistributionMapper.updateByPrimaryKey(ordDisOrderDistribution);
                String sheetName = "配销分货单【" + ordDisOrderDistribution.getDistributionOrderNo() + "】审核问题清单";
                String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
                asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, OrdDisDistributionImportErrorResult.class);
            } else {
                //立即生效则创建订货单任务
                ordDisOrderDistribution.setEffectiveTime(effectiveTime);
                ordDisOrderDistribution.setIsEffectiveImmediately(ordDisDistributionAuditIn.getIsEffectiveImmediately());
                ordDisOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.APPROVAL.getKey());
                AtomicReference<BigDecimal> totalDistributionAmount = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<BigDecimal> totalDistributionTotalQuantity = new AtomicReference<>(BigDecimal.ZERO);
                ordDisDistributionAuditIn.getDetailAuditInList().forEach(detail -> {
                    if (Objects.isNull(detail.getDetailId())) {
                        log.info("统筹分货单{}占坑明细{}，故跳过该商品", ordDisOrderDistribution.getDistributionOrderNo(), detail.getGoodsCode());
                        return;
                    }
                    OrdDisOrderDistributionDetail ordDisOrderDistributionDetail = ordDisOrderDistributionDetailService.selectByPrimaryKey(detail.getDetailId());
                    if (Objects.isNull(ordDisOrderDistributionDetail)) {
                        throw new BusinessException("分货单" + ordDisOrderDistribution.getDistributionOrderNo() + "不存在的明细" + detail.getGoodsCode());
                    }
                    totalDistributionAmount.getAndSet(totalDistributionAmount.get().add(ordDisOrderDistributionDetail.getOriginalPrice().multiply(detail.getDistributionQuantity())));
                    totalDistributionTotalQuantity.getAndSet(totalDistributionTotalQuantity.get().add(detail.getDistributionQuantity()));
                    ordDisOrderDistributionDetail.setDistributionQuantity(detail.getDistributionQuantity());
                    ordDisOrderDistributionDetail.setDistributionAmount(ordDisOrderDistributionDetail.getOriginalPrice().multiply(detail.getDistributionQuantity()));
                    ordDisOrderDistributionDetail.setUpdater(loginUsername);
                    ordDisOrderDistributionDetail.setUpdateTime(LocalDateTime.now());
                    updateDetailList.add(ordDisOrderDistributionDetail);
                });
                ordDisOrderDistribution.setDistributionTotalAmount(totalDistributionAmount.get());
                ordDisOrderDistribution.setDistributionTotalQuantity(totalDistributionTotalQuantity.get());
                // 审核后更新数据
                ordDisOrderDistributionDetailService.updateAfterAuditSuccess(updateDetailList, ordDisOrderDistribution);
            }
        } catch (Exception e) {
            log.error("分货单{}审核异步校验异常", ordDisOrderDistribution.getDistributionOrderNo(), e);
            OrdDisDistributionImportErrorResult errorResult = new OrdDisDistributionImportErrorResult();
            errorResult.setStoreCode(SystemConstant.SHORT_LINE);
            errorResult.setGoodsCode(SystemConstant.SHORT_LINE);
            errorResult.setErrorMessage("审核异常，请联系管理员");
            String sheetName = "配销分货单【" + ordDisOrderDistribution.getDistributionOrderNo() + "】审核异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDisDistributionImportErrorResult.class);
        } finally {
            redisService.del(key);
        }
    }
}
