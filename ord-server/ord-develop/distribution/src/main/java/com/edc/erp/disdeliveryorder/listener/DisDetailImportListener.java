/**
 * Copyright © 2010-2021 Everyday Chain. All rights reserved.
 */
package com.edc.erp.disdeliveryorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.model.in.DeliveryIn;
import com.edc.erp.disdeliveryorder.model.in.ImportDeliveryIn;
import com.edc.erp.disdeliveryorder.model.in.ImportDisDeliveryDetailsOrder;
import com.edc.erp.disdeliveryorder.model.out.DisDeliveryOrderDetailsOut;
import com.edc.erp.disdeliveryorder.model.out.OrdDisDeliveryDetailOut;
import com.edc.erp.disdeliveryorder.service.impl.OrdDisDeliveryServiceImpl;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 配销单明细导入监听
 *
 * @author weichao
 */
@Slf4j
public class DisDetailImportListener extends ImportListener<ImportDisDeliveryDetailsOrder> {

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;
    /**
     * 临时存放数据
     */
    List<DisDeliveryOrderDetailsOut> deliveryOrderDetailsOuts = new ArrayList<>();

    private final AtomicInteger atomicInteger = new AtomicInteger(2);

    private final OrdDisDeliveryServiceImpl disDeliveryService;

    private final ImportDeliveryIn importDeliveryIn;

    private final String centerStockBizOrgCode;


    Map<String, DisDeliveryOrderDetailsOut> dataMap = new HashMap<>();

    public DisDetailImportListener(OrdDisDeliveryServiceImpl disDeliveryService, ImportDeliveryIn importDeliveryIn, String centerStockBizOrgCode) {
        this.disDeliveryService = disDeliveryService;
        this.importDeliveryIn = importDeliveryIn;
        this.centerStockBizOrgCode = centerStockBizOrgCode;
    }

    @Override
    public void invoke(ImportDisDeliveryDetailsOrder importDisDeliveryDetailsOrder, AnalysisContext analysisContext) {
        Map<String, String> errorMap = new LinkedHashMap<>();
        String mapKey = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        DisDeliveryOrderDetailsOut ordDisDeliveryDetail = new DisDeliveryOrderDetailsOut();
        ordDisDeliveryDetail.setImportIndex(atomicInteger.get());
        // 校验第二列：集货数量
        BigDecimal orderQuantity = importDisDeliveryDetailsOrder.getOrderQuantity();
        if (Objects.isNull(orderQuantity)) {
            errorJoiner.add("集货数量不能为空;");
        }
        if (Objects.nonNull(orderQuantity) && orderQuantity.compareTo(BigDecimal.ZERO) < 0) {
            errorJoiner.add("集货数量必须大于0;");
        }
        // 集货数量合规再判断商品
        // 校验第一列：商品code代码
        String goodsCode = importDisDeliveryDetailsOrder.getGoodsCode();

        if (dataMap.containsKey(goodsCode)) {
            return;
        }
        if (errorJoiner.length() == 0) {
            if (StringUtils.isEmpty(goodsCode)) {
                errorJoiner.add("商品代码不能为空;");
            } else {
                DeliveryIn deliveryIn = new DeliveryIn();
                deliveryIn.setWrhCode(importDeliveryIn.getWrhCode());
                deliveryIn.setGoodsCode(goodsCode);
                deliveryIn.setStoreCode(importDeliveryIn.getStoreCode());
                deliveryIn.setBizOrgCode(importDeliveryIn.getBizOrgCode());
                deliveryIn.setStockCode(importDeliveryIn.getStockCode());
                deliveryIn.setDistributionType(DistributionWaysEnum.getNameByType(importDeliveryIn.getDistributionType()));
                OrdDisDeliveryDetailOut deliveryDetailOut = disDeliveryService.checkOrderGoods(deliveryIn, centerStockBizOrgCode);
                if (Objects.isNull(deliveryDetailOut)) {
                    errorJoiner.add("-不可配销出货;");
                }
                if (Objects.nonNull(deliveryDetailOut)) {
                    BeanUtils.copy(deliveryDetailOut, ordDisDeliveryDetail);
                    ordDisDeliveryDetail.setGoodsCode(goodsCode);
//                    ordDisDeliveryDetail.setDistributionQuantity(orderQuantity);
//                    ordDisDeliveryDetail.setDistributionPackageQuantity(ordDisDeliveryDetail.getDistributionQuantity().divide(ordDisDeliveryDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDisDeliveryDetail.setOrderUnitPrice(deliveryDetailOut.getDistributionUnitPrice());
//                    ordDisDeliveryDetail.setDistributionAmount(ordDisDeliveryDetail.getOrderUnitPrice().multiply(ordDisDeliveryDetail.getDistributionQuantity()));
                    ordDisDeliveryDetail.setOrderQuantity(orderQuantity);
                    ordDisDeliveryDetail.setOrderPackageQuantity(ordDisDeliveryDetail.getOrderQuantity().divide(ordDisDeliveryDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDisDeliveryDetail.setOrderAmount(ordDisDeliveryDetail.getOrderUnitPrice().multiply(orderQuantity));
//                    ordDisDeliveryDetail.setDeliveryQuantity(orderQuantity);
//                    ordDisDeliveryDetail.setDeliveryPackageQuantity(ordDisDeliveryDetail.getDistributionPackageQuantity());
//                    ordDisDeliveryDetail.setDeliveryAmount(ordDisDeliveryDetail.getDistributionAmount());
//                    ordDisDeliveryDetail.setArrivalQuantity(orderQuantity);
//                    ordDisDeliveryDetail.setArrivalPackageQuantity(ordDisDeliveryDetail.getDistributionPackageQuantity());
//                    ordDisDeliveryDetail.setArrivalAmount(ordDisDeliveryDetail.getDistributionAmount());
                    BigDecimal sellTax = null == ordDisDeliveryDetail.getSellTax() ? BigDecimal.ZERO : ordDisDeliveryDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
                    BigDecimal tax = sellTax.add(BigDecimal.ONE);
//                    ordDisDeliveryDetail.setDistributionExceptTaxAmount(ordDisDeliveryDetail.getDistributionAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//                    ordDisDeliveryDetail.setDistributionTaxAmount(ordDisDeliveryDetail.getDistributionAmount().subtract(ordDisDeliveryDetail.getDistributionExceptTaxAmount()));
                    ordDisDeliveryDetail.setWrhCostAmount(ordDisDeliveryDetail.getWrhPrice().multiply(orderQuantity));
                    ordDisDeliveryDetail.setWrhExceptTaxAmount(ordDisDeliveryDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDisDeliveryDetail.setWrhTaxAmount(ordDisDeliveryDetail.getWrhCostAmount().subtract(ordDisDeliveryDetail.getWrhExceptTaxAmount()));
                    ordDisDeliveryDetail.setStoreCostAmount(ordDisDeliveryDetail.getStoreStockPrice().multiply(orderQuantity));
                    ordDisDeliveryDetail.setStoreExceptTaxAmount(ordDisDeliveryDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDisDeliveryDetail.setStoreTaxAmount(ordDisDeliveryDetail.getStoreCostAmount().subtract(ordDisDeliveryDetail.getStoreExceptTaxAmount()));
                    ordDisDeliveryDetail.setPurchaseNo(importDisDeliveryDetailsOrder.getPurchaseNo());
                }
            }
        }
        // 记录异常信息
        if (errorJoiner.length() == 0) {
            ordDisDeliveryDetail.setGoodsCode(goodsCode);
            ordDisDeliveryDetail.setOrderQuantity(orderQuantity);
            deliveryOrderDetailsOuts.add(ordDisDeliveryDetail);
            dataMap.put(goodsCode, ordDisDeliveryDetail);
        } else {
//            errorMap.put("key", mapKey);
//            errorMap.put("value", errorJoiner.toString());
//            errorList.add(errorMap);
            totalErrorMap.put(mapKey, errorJoiner);
        }
        atomicInteger.getAndIncrement();
    }

    public String message() {
        String message = "";
        message += "成功导入" + deliveryOrderDetailsOuts.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - deliveryOrderDetailsOuts.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("DisDetailImportListener解析失败，发生异常:{}", exception.getMessage(), exception);
        if (exception instanceof ExcelDataConvertException) {
            this.encapsulateErrorMap("数据格式错误；");
        } else if (exception instanceof BusinessException) {
            this.encapsulateErrorMap(exception.getMessage());
        } else {
            this.encapsulateErrorMap("未知错误；");
        }
    }

    private void encapsulateErrorMap(String error) {
        Map<String, String> map = new LinkedHashMap<>(2);
        map.put("key", "第【" + atomicInteger.get() + "】行：");
        map.put("value", error);
        this.errorList.add(map);
        atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (deliveryOrderDetailsOuts.size() >= BATCH_COUNT) {
            deliveryOrderDetailsOuts.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
    }

    public List<DisDeliveryOrderDetailsOut> getDeliveryOrderDetailsOuts() {
        return deliveryOrderDetailsOuts;
    }

}
