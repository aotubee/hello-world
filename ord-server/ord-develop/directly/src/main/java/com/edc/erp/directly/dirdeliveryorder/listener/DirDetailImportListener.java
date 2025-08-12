/**
 * Copyright © 2010-2021 Everyday Chain. All rights reserved.
 */
package com.edc.erp.directly.dirdeliveryorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdeliveryorder.model.in.DeliveryIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.ImportDeliveryIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.ImportDirDeliveryDetailsOrder;
import com.edc.erp.directly.dirdeliveryorder.model.out.DirDeliveryOrderDetailsOut;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryDetailOut;
import com.edc.erp.directly.dirdeliveryorder.service.impl.OrdDirDeliveryServiceImpl;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 直营配货单明细导入监听
 *
 * @author weichao
 */
@Slf4j
public class DirDetailImportListener extends ImportListener<ImportDirDeliveryDetailsOrder> {

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;
    /**
     * 临时存放数据
     */
    List<DirDeliveryOrderDetailsOut> deliveryOrderDetailsOuts = new ArrayList<>();

    private final AtomicInteger atomicInteger = new AtomicInteger(2);

    private final OrdDirDeliveryServiceImpl disDeliveryService;

    private final ImportDeliveryIn importDeliveryIn;

    private final String centerStockBizOrgCode;


    Map<String, DirDeliveryOrderDetailsOut> dataMap = new HashMap<>();

    public DirDetailImportListener(OrdDirDeliveryServiceImpl disDeliveryService, ImportDeliveryIn importDeliveryIn, String centerStockBizOrgCode) {
        this.disDeliveryService = disDeliveryService;
        this.importDeliveryIn = importDeliveryIn;
        this.centerStockBizOrgCode = centerStockBizOrgCode;
    }

    @Override
    public void invoke(ImportDirDeliveryDetailsOrder importDisDeliveryDetailsOrder, AnalysisContext analysisContext) {
        Map<String, String> errorMap = new LinkedHashMap<>();
        String mapKey = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        DirDeliveryOrderDetailsOut ordDirDeliveryDetail = new DirDeliveryOrderDetailsOut();
        ordDirDeliveryDetail.setImportIndex(atomicInteger.get());
        // 校验第二列：要货数量
        BigDecimal orderQuantity = importDisDeliveryDetailsOrder.getOrderQuantity();
        if (Objects.isNull(orderQuantity)) {
            errorJoiner.add("要货数量不能为空;");
        }
        if (Objects.nonNull(orderQuantity) && orderQuantity.compareTo(BigDecimal.ZERO) < 0) {
            errorJoiner.add("要货数量必须大于0;");
        }
        // 配货数量合规再判断商品
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
                OrdDirDeliveryDetailOut deliveryDetailOut = disDeliveryService.checkOrderGoods(deliveryIn, centerStockBizOrgCode);
                if (Objects.isNull(deliveryDetailOut)) {
                    errorJoiner.add("-不可直营配货出货");
                }
                if (!Objects.isNull(deliveryDetailOut)) {
                    BeanUtils.copy(deliveryDetailOut, ordDirDeliveryDetail);
                    ordDirDeliveryDetail.setGoodsCode(goodsCode);
//                    ordDirDeliveryDetail.setDistributionQuantity(orderQuantity);
//                    ordDirDeliveryDetail.setDistributionPackageQuantity(ordDirDeliveryDetail.getDistributionQuantity().divide(ordDirDeliveryDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDirDeliveryDetail.setOrderUnitPrice(deliveryDetailOut.getDistributionUnitPrice());
//                    ordDirDeliveryDetail.setDistributionAmount(ordDirDeliveryDetail.getOrderUnitPrice().multiply(orderQuantity));
                    ordDirDeliveryDetail.setOrderQuantity(orderQuantity);
                    ordDirDeliveryDetail.setOrderPackageQuantity(ordDirDeliveryDetail.getOrderQuantity().divide(ordDirDeliveryDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDirDeliveryDetail.setOrderAmount(ordDirDeliveryDetail.getOrderUnitPrice().multiply(orderQuantity));
//                    ordDirDeliveryDetail.setDeliveryQuantity(orderQuantity);
//                    ordDirDeliveryDetail.setDeliveryPackageQuantity(ordDirDeliveryDetail.getDistributionPackageQuantity());
//                    ordDirDeliveryDetail.setDeliveryAmount(ordDirDeliveryDetail.getDistributionAmount());
//                    ordDirDeliveryDetail.setArrivalQuantity(orderQuantity);
//                    ordDirDeliveryDetail.setArrivalPackageQuantity(ordDirDeliveryDetail.getDistributionPackageQuantity());
//                    ordDirDeliveryDetail.setArrivalAmount(ordDirDeliveryDetail.getDistributionAmount());
                    BigDecimal sellTax = null == ordDirDeliveryDetail.getSellTax() ? BigDecimal.ZERO : ordDirDeliveryDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
                    BigDecimal tax = sellTax.add(BigDecimal.ONE);
//                    ordDirDeliveryDetail.setDistributionExceptTaxAmount(ordDirDeliveryDetail.getDistributionAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//                    ordDirDeliveryDetail.setDistributionTaxAmount( ordDirDeliveryDetail.getDistributionAmount().subtract(ordDirDeliveryDetail.getDistributionExceptTaxAmount()));
                    ordDirDeliveryDetail.setWrhCostAmount(ordDirDeliveryDetail.getWrhPrice().multiply(orderQuantity));
                    ordDirDeliveryDetail.setWrhExceptTaxAmount(ordDirDeliveryDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDirDeliveryDetail.setWrhTaxAmount(ordDirDeliveryDetail.getWrhCostAmount().subtract(ordDirDeliveryDetail.getWrhExceptTaxAmount()));
                    ordDirDeliveryDetail.setStoreCostAmount(ordDirDeliveryDetail.getStoreStockPrice().multiply(orderQuantity));
                    ordDirDeliveryDetail.setStoreExceptTaxAmount(ordDirDeliveryDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDirDeliveryDetail.setStoreTaxAmount(ordDirDeliveryDetail.getStoreCostAmount().subtract(ordDirDeliveryDetail.getStoreExceptTaxAmount()));
                    ordDirDeliveryDetail.setPurchaseNo(importDisDeliveryDetailsOrder.getPurchaseNo());
                }

            }
        }
        // 记录异常信息
        if (errorJoiner.length() == 0) {
            ordDirDeliveryDetail.setGoodsCode(goodsCode);
            ordDirDeliveryDetail.setOrderQuantity(orderQuantity);
            deliveryOrderDetailsOuts.add(ordDirDeliveryDetail);
            dataMap.put(goodsCode, ordDirDeliveryDetail);
        } else {
//            errorMap.put("key", mapKey);
//            errorMap.put("value", errorBuilder.toString());
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
        log.error("DirDetailImportListener解析失败，发生异常:{}", exception.getMessage(), exception);
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

    public List<DirDeliveryOrderDetailsOut> getDeliveryOrderDetailsOuts() {
        return deliveryOrderDetailsOuts;
    }

}
