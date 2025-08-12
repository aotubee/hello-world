package com.edc.erp.returnorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.ExpiryCheckUtil;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.returnorder.enumeration.OrdReturnGoodsColumnEnum;
import com.edc.erp.returnorder.model.in.ImportOrdReturnGoodsVO;
import com.edc.erp.returnorder.model.out.SaveReturnGoodsOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 退货单商品数量导入监听器
 *
 * @author yaojinpeng
 * @since 2022/10/26 17:08
 */
@Slf4j
@NoArgsConstructor
public class OrdReturnStoreGoodsListener extends ImportListener<ImportOrdReturnGoodsVO> {
    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 3000;

    /**
     * 临时存放数据 OrdReturnDetailOut
     */
    List<SaveReturnGoodsOut> saveReturnGoodsOutList = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(2);


    private String bizOrgCode;

    private OrderGoodsServer orderGoodsServer;

    private String storeCode;

    private WarehouseServer warehouseServer;

    private String warehouseCode;

    private String stockCode;

    private String distributionType;

    private String centerStockBizOrgCode;


    public OrdReturnStoreGoodsListener(OrderGoodsServer orderGoodsServer, String storeCode, String bizOrgCode,
                                       WarehouseServer warehouseServer, String warehouseCode, String stockCode, String distributionType, String centerStockBizOrgCode) {
        this.orderGoodsServer = orderGoodsServer;
        this.storeCode = storeCode;
        this.bizOrgCode = bizOrgCode;
        this.warehouseServer = warehouseServer;
        this.warehouseCode = warehouseCode;
        this.stockCode = stockCode;
        this.distributionType = distributionType;
        this.centerStockBizOrgCode = centerStockBizOrgCode;
    }

    @Override
    public void invoke(ImportOrdReturnGoodsVO data, AnalysisContext context) {
        String mapKey = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        SaveReturnGoodsOut saveReturnGoodsOut = new SaveReturnGoodsOut();
        // 校验第二列：商品sku
        String goodsCode = data.getGoodsCode();
        if (StringUtils.isEmpty(goodsCode)) {
            errorJoiner.add(OrdReturnGoodsColumnEnum.DISTRIBUTION_SKU_CODE.getValue() + "不能为空;");
//            throw new BusinessException(OrdReturnGoodsColumnEnum.DISTRIBUTION_SKU_CODE.getValue() + "不能为空;");
        }
        // 校验第三列：申请退货数量
        BigDecimal applyReturnQuantity = data.getApplyReturnQuantity();
        if (null == applyReturnQuantity) {
            errorJoiner.add(OrdReturnGoodsColumnEnum.DISTRIBUTION_QUANTITY.getValue() + "不能为空;");
//            throw new BusinessException(OrdReturnGoodsColumnEnum.DISTRIBUTION_QUANTITY.getValue() + "不能为空;");
        }
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
        orderGoodsIn.setGoodsCode(goodsCode);
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setStockCode(stockCode);
        orderGoodsIn.setWarehouseCode(warehouseCode);
        OrderGoodsOut goodsOut = orderGoodsServer.getStoreOrderGoodsForBackReturn(orderGoodsIn);
        if (Objects.isNull(goodsOut)) {
            errorJoiner.add("商品不允许配销退货;");
//            throw new BusinessException(goodsCode + "商品不允许配销退货;");
        } else {
            if (CollectionUtils.isNotEmpty(saveReturnGoodsOutList)) {
                boolean result = saveReturnGoodsOutList.stream().anyMatch(a -> goodsCode.equals(a.getGoodsCode()));
                if (result) {
                    errorJoiner.add("商品代码已重复;");
//                throw new BusinessException("商品代码已重复;");
                }
            }
            if (!distributionType.equals(goodsOut.getDistributionWay())) {
                errorJoiner.add("商品配送方式与单头不一致;");
//            throw new BusinessException(goodsCode + "商品配送方式与单头不一致;");
            }
//        BigDecimal num = applyReturnQuantity.divide(new BigDecimal(goodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
//        if (!isIntegerValue(num)) {
//            throw new BusinessException(goodsCode + "商品包装数不是整数;");
//        }
            if (!warehouseCode.equals(goodsOut.getReturnWarehouseCode())) {
                errorJoiner.add(goodsCode + "商品退货仓储与所选退货仓储不符;");
//            throw new BusinessException(goodsCode + "商品退货仓储与所选退货仓储不符;");
            }
            if (!stockCode.equals(goodsOut.getBackStockCode())) {
                errorJoiner.add(goodsCode + "商品退货仓位与所选退货仓位不符;");
//            throw new BusinessException(goodsCode + "商品退货仓位与所选退货仓位不符;");
            }
            if (StringUtils.isNotBlank(data.getExpiry())) {
                Response<LocalDateTime> expiryResponse = ExpiryCheckUtil.checkExpiry(data.getExpiry());
                if (!expiryResponse.isSuccess()) {
                    errorJoiner.add(stockCode + "&" + data.getGoodsCode() + expiryResponse.getMessage());
                } else {
                    saveReturnGoodsOut.setExpiry(data.getExpiry());
                }
            }
            saveReturnGoodsOut.setGoodsCode(goodsCode);
            saveReturnGoodsOut.setGoodsName(goodsOut.getGoodsName());
            saveReturnGoodsOut.setOrgGoodsId(goodsOut.getOrgGoodsId());
            saveReturnGoodsOut.setBarCode(goodsOut.getBarCode());
            saveReturnGoodsOut.setGoodsType(goodsOut.getGoodsType());
            saveReturnGoodsOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(goodsOut.getGoodsType()));
            saveReturnGoodsOut.setDistributionSpecification(goodsOut.getDistributionSpecification().getQpcStr());
            saveReturnGoodsOut.setDistributionSpecificationNum(BigDecimal.valueOf(goodsOut.getDistributionSpecification().getQpc()));
            saveReturnGoodsOut.setDistributionSpecificationUnit(goodsOut.getDistributionSpecification().getUnitName());
            saveReturnGoodsOut.setVendorCode(goodsOut.getVendorCode());
            saveReturnGoodsOut.setVendorName(goodsOut.getVendorName());
            BigDecimal stockPrice = warehouseServer.getStockPrice(storeCode, goodsCode, bizOrgCode);
            saveReturnGoodsOut.setReturnUnitPrice(Objects.isNull(data.getReturnUnitPrice()) ? stockPrice : data.getReturnUnitPrice());
            saveReturnGoodsOut.setDistributionPrice(goodsOut.getDistributionUnitPrice());
            //申请
            saveReturnGoodsOut.setApplyReturnQuantity(applyReturnQuantity);
            saveReturnGoodsOut.setApplyPackageQuantity(applyReturnQuantity.divide(new BigDecimal(goodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            saveReturnGoodsOut.setApplyReturnAmount(saveReturnGoodsOut.getReturnUnitPrice().multiply(applyReturnQuantity));
            //审核
            saveReturnGoodsOut.setAuditReturnQuantity(saveReturnGoodsOut.getApplyReturnQuantity());
            saveReturnGoodsOut.setAuditPackageQuantity(saveReturnGoodsOut.getApplyPackageQuantity());
            saveReturnGoodsOut.setAuditReturnAmount(saveReturnGoodsOut.getApplyReturnAmount());
            //实际
            saveReturnGoodsOut.setActualReturnQuantity(saveReturnGoodsOut.getApplyReturnQuantity());
            saveReturnGoodsOut.setActualPackageQuantity(saveReturnGoodsOut.getApplyPackageQuantity());
            saveReturnGoodsOut.setActualReturnAmount(saveReturnGoodsOut.getApplyReturnAmount());

            saveReturnGoodsOut.setStoreStockPrice(Objects.nonNull(stockPrice) ? stockPrice : BigDecimal.ZERO);
            BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(warehouseCode, stockCode, goodsCode, centerStockBizOrgCode, storeCode, bizOrgCode);
            if (Objects.isNull(warehousePrice)) {
                log.error("配销导入退货明细商品{}仓储库存价为空", goodsCode);
                errorJoiner.add(goodsCode + "商品仓储库存价为空;");
            }
            saveReturnGoodsOut.setWrhPrice(Objects.nonNull(warehousePrice) ? warehousePrice : BigDecimal.ZERO);
            saveReturnGoodsOut.setSellTax(goodsOut.getOutTax());

            BigDecimal sellTax = saveReturnGoodsOut.getSellTax() == null ? BigDecimal.ZERO : saveReturnGoodsOut.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            //1+税率
            BigDecimal tax = sellTax.add(BigDecimal.ONE);
            //退货去税金额
            saveReturnGoodsOut.setReturnExceptTaxAmount(saveReturnGoodsOut.getActualReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //退货税额
            saveReturnGoodsOut.setReturnTaxAmount(saveReturnGoodsOut.getActualReturnAmount().subtract(saveReturnGoodsOut.getReturnExceptTaxAmount()));
            //仓储成本金额
            saveReturnGoodsOut.setWrhCostAmount(saveReturnGoodsOut.getWrhPrice().multiply(saveReturnGoodsOut.getActualReturnQuantity()));
            //仓储成本去税金额
            saveReturnGoodsOut.setWrhExceptTaxAmount(saveReturnGoodsOut.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //仓储成本税额
            saveReturnGoodsOut.setWrhTaxAmount(saveReturnGoodsOut.getWrhCostAmount().subtract(saveReturnGoodsOut.getWrhExceptTaxAmount()));
            //门店成本金额
            saveReturnGoodsOut.setStoreCostAmount(saveReturnGoodsOut.getStoreStockPrice().multiply(saveReturnGoodsOut.getActualReturnQuantity()));
            //门店成本去税金额
            saveReturnGoodsOut.setStoreExceptTaxAmount(saveReturnGoodsOut.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //门店成本税额
            saveReturnGoodsOut.setStoreTaxAmount(saveReturnGoodsOut.getStoreCostAmount().subtract(saveReturnGoodsOut.getStoreExceptTaxAmount()));
            saveReturnGoodsOut.setInvoiceType(goodsOut.getInvoiceType());
            saveReturnGoodsOut.setIsManageValidityPeriod(goodsOut.getIsManageValidityPeriod());
            saveReturnGoodsOut.setImportIndex(atomicInteger.get());

        }
        if (errorJoiner.length() == 0) {
            saveReturnGoodsOutList.add(saveReturnGoodsOut);
        } else {
            totalErrorMap.put(mapKey, errorJoiner);
        }
        atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (saveReturnGoodsOutList.size() >= BATCH_COUNT) {
            saveReturnGoodsOutList.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
    }

    public List<SaveReturnGoodsOut> getSaveReturnGoodsOut() {
        return saveReturnGoodsOutList;
    }

    public String message() {
        String message = "";
        message += "成功导入" + saveReturnGoodsOutList.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - saveReturnGoodsOutList.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getImportErrorMessage(totalErrorMap));
        return message;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("OrdReturnStoreGoodsListener解析失败，发生异常:{}", exception.getMessage(), exception);
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

    private boolean isIntegerValue(BigDecimal bd) {
        return bd.stripTrailingZeros().scale() <= 0;
    }

}
