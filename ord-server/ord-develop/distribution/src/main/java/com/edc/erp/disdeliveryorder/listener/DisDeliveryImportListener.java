//package com.edc.erp.disdeliveryorder.listener;
//
//import com.alibaba.excel.context.AnalysisContext;
//import com.alibaba.excel.exception.ExcelDataConvertException;
//import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
//import com.edc.erp.common.enumeration.DistributionWaysEnum;
//import com.edc.erp.common.enumeration.StoreConstant;
//import com.edc.erp.common.model.entity.StoreInfo;
//import com.edc.erp.common.model.in.goods.OrderGoodsIn;
//import com.edc.erp.common.model.in.store.StoreStatusInfo;
//import com.edc.erp.common.model.out.goods.OrderGoodsOut;
//import com.edc.erp.common.model.out.stock.StockInfoOut;
//import com.edc.erp.common.service.OrderGoodsServer;
//import com.edc.erp.common.service.StockServer;
//import com.edc.erp.common.service.StoreCenterService;
//import com.edc.erp.common.util.ImportListener;
//import com.edc.erp.common.util.NumberUtil;
//import com.edc.erp.disdeliveryorder.model.in.ImportDisDeliveryOrder;
//import com.edc.erp.disdeliveryorder.model.out.OrdDisDeliveryOut;
//import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
//import com.edc.plugins.common.exception.BusinessException;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import tk.mybatis.mapper.util.StringUtil;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * @author fxw
// * @description: 配销单批量导入监听器
// * @since 2022/11/17 15:16
// */
//@Slf4j
//public class DisDeliveryImportListener extends ImportListener<ImportDisDeliveryOrder> {
//
//    /**
//     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
//     */
//    private static final int BATCH_COUNT = 5000;
//    /**
//     * 临时存放数据
//     */
//    List<OrdDisDeliveryOut> ordDisDeliveryOuts = new ArrayList<>();
//
//    List<ImportDisDeliveryOrder> deliveryOrders = new ArrayList<>();
//
//    private AtomicInteger atomicInteger = new AtomicInteger(2);
//
//    public List<ImportDisDeliveryOrder> getDeliveryOrders() {
//        return deliveryOrders;
//    }
//
//    private OrderGoodsServer orderGoodsServer;
//
//    private StoreCenterService storeCenterService;
//
//    private OrdDisDeliveryDetailService ordDisDeliveryDetailService;
//
//    private String bizOrgCode;
//
//    private StockServer stockServer;
//
//    public DisDeliveryImportListener(OrderGoodsServer orderGoodsServer,
//                                     StoreCenterService storeCenterService,
//                                     OrdDisDeliveryDetailService ordDisDeliveryDetailService,
//                                     StockServer stockServer,
//                                     String bizOrgCode) {
//        this.orderGoodsServer = orderGoodsServer;
//        this.storeCenterService = storeCenterService;
//        this.ordDisDeliveryDetailService = ordDisDeliveryDetailService;
//        this.stockServer = stockServer;
//        this.bizOrgCode = bizOrgCode;
//    }
//
//    public DisDeliveryImportListener() {
//
//    }
//
//    Map<String, ImportDisDeliveryOrder> dataMap = new HashMap<>();
//
//
//    @Override
//    public void invoke(ImportDisDeliveryOrder data, AnalysisContext context) {
//        Map<String, String> errorMap = new LinkedHashMap<>();
//        String mapKey = "第【" + atomicInteger + "】行：";
//        StringBuilder errorBuilder = new StringBuilder();
//        OrdDisDeliveryOut ordDisDeliveryOut = new OrdDisDeliveryOut();
//
//        // 校验第一列 goodsCode
//        if (StringUtils.isEmpty(data.getGoodsCode())) {
//            errorBuilder.append("商品代码不能为空;");
//        }
//
//        if (StringUtils.isEmpty(data.getStoreCode())) {
//            errorBuilder.append("门店代码不能为空;");
//        }
//
//        if (StringUtils.isEmpty(data.getWrhCode())) {
//            errorBuilder.append("仓储代码不能为空;");
//        }
//
//        if (StringUtils.isEmpty(data.getStockCode())) {
//            errorBuilder.append("仓位代码不能为空;");
//        }
//
//        if (Objects.isNull(data.getDeliveryQuantity())) {
//            errorBuilder.append("配销数量不能为空;");
//        }
//        if (Objects.isNull(data.getDistributionType())) {
//            errorBuilder.append("配送方式不能为空;");
//        }
//
//        String goodsCode = data.getGoodsCode();
//        String storeCode = data.getStoreCode();
//        if (StringUtil.isNotEmpty(goodsCode) && StringUtil.isNotEmpty(storeCode)) {
//            if (dataMap.containsKey(storeCode.concat("_").concat(goodsCode))) {
//                errorBuilder.append("门店" + storeCode + "商品" + goodsCode + "重复;");
//            }
//
//            if (errorBuilder.length() == 0) {
//                this.verifyDetailIsExist(mapKey, errorMap, data, errorBuilder);
//            }
//        }
//
//        // 记录异常信息
//        if (errorBuilder.length() == 0) {
//            deliveryOrders.add(data);
//            dataMap.put(data.getStoreCode().concat("_").concat(data.getGoodsCode()), data);
//        } else {
//            errorMap.put("key", mapKey);
//            errorMap.put("value", errorBuilder.toString());
//            errorList.add(errorMap);
//        }
//        atomicInteger.getAndIncrement();
//    }
//
//    @Override
//    public void doAfterAllAnalysed(AnalysisContext context) {
//        if (ordDisDeliveryOuts.size() >= BATCH_COUNT) {
//            ordDisDeliveryOuts.clear();
//        }
//    }
//
//    /**
//     * 校验导入数据
//     *
//     * @param data
//     */
//    public void verifyDetailIsExist(String mapKey, Map<String, String> errorMap, ImportDisDeliveryOrder data, StringBuilder errorBuilder) {
//        String type = DistributionWaysEnum.getTypeByName(data.getDistributionType());
//        if (Objects.isNull(type)) {
//            errorBuilder.append("配送方式不存在;");
//        }
//        if (Objects.nonNull(type) && !type.equals(DistributionWaysEnum.UNIFIEDDIS.getType()) && !type.equals(DistributionWaysEnum.TRANSFER.getType())) {
//            errorBuilder.append("只能导入配送方式为统配或中转;");
//        }
//        StockInfoOut stockInfoOut = stockServer.getByCode(data.getStockCode(), bizOrgCode);
//        if (Objects.isNull(stockInfoOut)) {
//            errorBuilder.append("仓位不存在;");
//        }
//        if (Objects.nonNull(stockInfoOut)) {
//            if (!stockInfoOut.getWarehouseCode().equals(data.getWrhCode())) {
//                errorBuilder.append("仓位与仓储不匹配;");
//            }
//        }
//        com.edc.erp.model.out.StockInfoOut stockInfoByCode = ordDisDeliveryDetailService.findStockInfoByCode(data.getStockCode(), bizOrgCode);
//        if (Objects.isNull(stockInfoByCode)) {
//            errorBuilder.append("仓位不允许进行配货;");
//        }
//        StoreStatusInfo storeStatusInfo = new StoreStatusInfo();
//        storeStatusInfo.setBizOrgCode(bizOrgCode);
//        storeStatusInfo.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
//        storeStatusInfo.setStoreProperty(StoreConstant.StoreProperty.FRANCHISE.getMytValue());
//        storeStatusInfo.setStoreCode(data.getStoreCode());
//        StoreInfo storeInfo = storeCenterService.getStatusStoreInfo(storeStatusInfo);
//        if (Objects.isNull(storeInfo)) {
//            errorBuilder.append("门店不存在或者门店不允许配销;");
//        }
//        if (Objects.nonNull(storeInfo)) {
//            String goodsCode = data.getGoodsCode();
//            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
//            orderGoodsIn.setGoodsCode(goodsCode);
//            orderGoodsIn.setStoreCode(data.getStoreCode());
//            orderGoodsIn.setBizOrgCode(bizOrgCode);
//            OrderGoodsOut goodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
//            if (Objects.isNull(goodsOut)) {
//                errorBuilder.append(goodsCode).append("-不可配货;");
//            }
//            if (Objects.nonNull(goodsOut)) {
//                if (Objects.isNull(goodsOut.getDistributionPrice()) || BigDecimal.ZERO.compareTo(goodsOut.getDistributionPrice()) == NumberUtil.INTEGER_ZERO) {
//                    errorBuilder.append(goodsCode).append("配销价为空;");
//                }
//                boolean isCauPag = true;
//                if (NumberUtil.INTEGER_ZERO.equals(data.getDeliveryQuantity()) || !cn.hutool.core.util.NumberUtil.isInteger(data.getDeliveryQuantity().toString())) {
//                    errorBuilder.append("配销数量不可为小数或0;");
//                    isCauPag = false;
//                }
//                boolean isError = isCauPag && Objects.nonNull(goodsOut.getDistributionSpecification()) &&
//                        (0 == (Integer.parseInt(data.getDeliveryQuantity().toString()) / goodsOut.getDistributionSpecification().getQpc()) || 0 != (Integer.parseInt(data.getDeliveryQuantity().toString()) % goodsOut.getDistributionSpecification().getQpc()));
//                if (isError) {
//                    errorBuilder.append("配销包装数量不可为小数或0;");
//                }
//            }
//        }
//
//    }
//
//    public String message() {
//        String message = "导入成功：";
//        message += "成功导入" + deliveryOrders.size() + "条数据。";
//        int num = Math.max(0, atomicInteger.get() - deliveryOrders.size() - 2);
//        message += "失败" + num + "条数据。;";
//        message += String.join("\n", this.getErrorDate());
//        return message;
//    }
//
//    public String errorMessage() {
//        String message = "导入失败：";
//        int num = Math.max(0, atomicInteger.get() - deliveryOrders.size() - 2);
//        message += "失败" + num + "条数据。;";
//        message += String.join("\n", this.getErrorDate());
//        return message;
//    }
//
//    @Override
//    public void onException(Exception exception, AnalysisContext context) {
//        log.error("DisDeliveryImportListener，发生异常:{}", exception.getMessage(), exception);
//        if (exception instanceof ExcelDataConvertException) {
//            this.encapsulateErrorMap("数据格式错误；");
//        } else if (exception instanceof BusinessException) {
//            this.encapsulateErrorMap(exception.getMessage());
//        } else {
//            this.encapsulateErrorMap("未知错误；");
//        }
//    }
//
//    private void encapsulateErrorMap(String error) {
//        Map<String, String> map = new LinkedHashMap<>(2);
//        map.put("key", "第【" + atomicInteger.get() + "】行：");
//        map.put("value", error);
//        this.errorList.add(map);
//        atomicInteger.getAndIncrement();
//    }
//}
