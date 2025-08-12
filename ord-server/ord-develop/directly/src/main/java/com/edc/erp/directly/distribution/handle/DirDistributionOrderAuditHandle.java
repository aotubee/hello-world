package com.edc.erp.directly.distribution.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDistributionMapper;
import com.edc.erp.directly.distribution.model.excel.OrdDirDistributionImportErrorResult;
import com.edc.erp.directly.distribution.model.in.OrdDirDistributionAuditIn;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionDetailService;
import com.edc.erp.directly.enumeration.OrderDistributionOrderStatusEnum;
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
public class DirDistributionOrderAuditHandle {

    private final OrdDirOrderDistributionDetailService ordDirOrderDistributionDetailService;

    private final AsyncExportHandle asyncExportHandle;

    private final OrdDirOrderDistributionMapper ordDirOrderDistributionMapper;

    private final RedisService redisService;

    @Async
    public void asyncCheckByAudit(OrdDirDistributionAuditIn ordDirDistributionAuditIn, String loginUsername, String key) {
        OrdDirOrderDistribution ordDirOrderDistribution = ordDirOrderDistributionMapper.selectByPrimaryKey(ordDirDistributionAuditIn.getDistributionOrderId());
        try {
            List<OrdDirOrderDistributionDetail> detailList = ordDirOrderDistributionDetailService.getDetailByOrdDistributionOrderId(ordDirOrderDistribution.getId());
            LocalDateTime effectiveTime;
            //立即生效则创建订货单任务
            if (NumberUtil.INTEGER_ONE.equals(ordDirDistributionAuditIn.getIsEffectiveImmediately())) {
                effectiveTime = LocalDateTime.now();
            } else {
                effectiveTime = ordDirDistributionAuditIn.getEffectiveTime();
            }
            List<OrdDirDistributionImportErrorResult> errorResultList = ordDirOrderDistributionDetailService.checkDetailByAudit(detailList, ordDirOrderDistribution.getBizOrgCode(), effectiveTime);
            ordDirOrderDistribution.setUpdater(loginUsername);
            ordDirOrderDistribution.setUpdateTime(LocalDateTime.now());
            List<OrdDirOrderDistributionDetail> updateDetailList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(errorResultList)) {
                ordDirOrderDistribution.setRemark("请下载审核问题清单查看");
                ordDirOrderDistributionMapper.updateByPrimaryKey(ordDirOrderDistribution);
                String sheetName = "直营分货单【" + ordDirOrderDistribution.getDistributionOrderNo() + "】审核问题清单";
                String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
                asyncExportHandle.asyncExport(fileName, sheetName, loginUsername,
                        errorResultList, OrdDirDistributionImportErrorResult.class);
            } else {
                ordDirOrderDistribution.setEffectiveTime(effectiveTime);
                ordDirOrderDistribution.setIsEffectiveImmediately(ordDirDistributionAuditIn.getIsEffectiveImmediately());
                ordDirOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.APPROVAL.getKey());
                AtomicReference<BigDecimal> totalDistributionAmount = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<BigDecimal> totalDistributionTotalQuantity = new AtomicReference<>(BigDecimal.ZERO);
                ordDirDistributionAuditIn.getDetailAuditInList().forEach(detail -> {
                    if (Objects.isNull(detail.getDetailId())) {
                        log.info("统筹分货单{}占坑明细{}，故跳过该商品", ordDirOrderDistribution.getDistributionOrderNo(), detail.getGoodsCode());
                        return;
                    }
                    OrdDirOrderDistributionDetail ordDirOrderDistributionDetail = ordDirOrderDistributionDetailService.selectByPrimaryKey(detail.getDetailId());
                    if (Objects.isNull(ordDirOrderDistributionDetail)) {
                        throw new BusinessException("分货单" + ordDirOrderDistribution.getDistributionOrderNo() + "不存在的明细" + detail.getGoodsCode());
                    }
                    totalDistributionAmount.getAndSet(totalDistributionAmount.get().add(ordDirOrderDistributionDetail.getOriginalPrice().multiply(detail.getDistributionQuantity())));
                    totalDistributionTotalQuantity.getAndSet(totalDistributionTotalQuantity.get().add(detail.getDistributionQuantity()));
                    ordDirOrderDistributionDetail.setDistributionQuantity(detail.getDistributionQuantity());
                    ordDirOrderDistributionDetail.setDistributionAmount(ordDirOrderDistributionDetail.getOriginalPrice().multiply(detail.getDistributionQuantity()));
                    ordDirOrderDistributionDetail.setUpdater(loginUsername);
                    ordDirOrderDistributionDetail.setUpdateTime(LocalDateTime.now());
                    updateDetailList.add(ordDirOrderDistributionDetail);
                });
                ordDirOrderDistribution.setDistributionTotalAmount(totalDistributionAmount.get());
                ordDirOrderDistribution.setDistributionTotalQuantity(totalDistributionTotalQuantity.get());
                // 审核后更新数据
                ordDirOrderDistributionDetailService.updateAfterAuditSuccess(updateDetailList, ordDirOrderDistribution);
            }
        } catch (Exception e) {
            log.error("分货单{}审核异步校验异常", ordDirOrderDistribution.getDistributionOrderNo(), e);
            OrdDirDistributionImportErrorResult errorResult = new OrdDirDistributionImportErrorResult();
            errorResult.setStoreCode(SystemConstant.SHORT_LINE);
            errorResult.setGoodsCode(SystemConstant.SHORT_LINE);
            errorResult.setErrorMessage("审核异常，请联系管理员");
            String sheetName = "直营分货单【" + ordDirOrderDistribution.getDistributionOrderNo() + "】审核异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), OrdDirDistributionImportErrorResult.class);
        } finally {
            redisService.del(key);
        }
    }
}
