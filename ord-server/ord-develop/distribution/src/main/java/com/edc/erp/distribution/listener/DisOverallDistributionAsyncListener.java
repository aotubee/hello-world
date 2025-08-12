package com.edc.erp.distribution.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: 三维导入分货单明细
 * @Author: ZhangYao
 * @Date: 2023/9/18 11:54
 **/
@Slf4j
public class DisOverallDistributionAsyncListener extends ImportListener<Map<Integer, String>> {

    /**
     * 每隔5条存储数据库，实际使用中可以2000条，然后清理list ，方便内存回收
     */
//    private static final int BATCH_COUNT = 2000;

    /**
     * 临时存放数据
     */

    public List<Map<Integer, String>> getDataList() {
        return dataList;
    }

    private List<Map<Integer, String>> dataList = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(1);

    private Map<String, String> importMap = new HashMap<>();

    public DisOverallDistributionAsyncListener() {

    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        String rowStr = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        data.entrySet().forEach(entry -> {
            Integer column = entry.getKey();
            // 过滤 EXCEL文件A1无效数据
            if (atomicInteger.get() == NumberUtil.INTEGER_ONE && column.equals(NumberUtil.INTEGER_ZERO)) {
                return;
            }
            // 判断 第一行，商品代码非空
            if (atomicInteger.get() == NumberUtil.INTEGER_ONE
                    && !column.equals(NumberUtil.INTEGER_ZERO)
                    && StringUtils.isBlank(entry.getValue())) {
                errorJoiner.add("第" + (column + NumberUtil.INTEGER_ONE) + "列商品代码不能为空");
            }
            // 商品代码验重
            if (atomicInteger.get() == NumberUtil.INTEGER_ONE
                    && StringUtils.isNotBlank(entry.getValue())) {
                // 验重
                this.checkDataRepeat(entry, errorJoiner);
            }
            // 判断 第一列，门店代码非空
            if (column.equals(NumberUtil.INTEGER_ZERO) && StringUtils.isBlank(entry.getValue())) {
                errorJoiner.add("第1列门店代码不能为空");
            }
            // 门店代码验重
            if (atomicInteger.get() != NumberUtil.INTEGER_ONE
                    && column.equals(NumberUtil.INTEGER_ZERO)
                    && StringUtils.isNotBlank(entry.getValue())) {
                // 验重
                this.checkDataRepeat(entry, errorJoiner);
            }
            // 校验商品分货数量
            if (atomicInteger.get() != NumberUtil.INTEGER_ONE && !column.equals(NumberUtil.INTEGER_ZERO)) {
                BigDecimal distributionQuantity = StringUtils.isNotBlank(entry.getValue()) ? new BigDecimal(entry.getValue()) : BigDecimal.ZERO;
                if (StringUtils.isBlank(entry.getValue())) {
                    entry.setValue(NumberUtil.INTEGER_ZERO.toString());
                }
                if (distributionQuantity.compareTo(BigDecimal.ZERO) < NumberUtil.INTEGER_ZERO) {
                    errorJoiner.add("第" + (column + NumberUtil.INTEGER_ONE) + "列分货数量必须为正整数");
                }
            }
        });
        if (errorJoiner.length() > 0) {
            totalErrorMap.put(rowStr, errorJoiner);
        } else {
            log.info(JSONObject.toJSONString(data));
            dataList.add(data);
        }
//        // 记录异常信息
//        if (errorBuilder.length() == 0) {
////            dataMap.put(data.getStoreCode().concat("_").concat(data.getGoodsCode()), data);
//        } else {
//            errorMap.put("key", rowStr);
//            errorMap.put("value", errorBuilder.toString());
////            errorList.add(errorMap);
//        }
        atomicInteger.getAndIncrement();
    }

    private void checkDataRepeat(Map.Entry<Integer, String> entry, StringJoiner errorJoiner) {
        if (importMap.containsKey(entry.getValue())) {
            errorJoiner.add(entry.getValue() + "已存在");
        } else {
            importMap.put(entry.getValue(), entry.getValue());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("DisOverallDistributionAsyncListener解析完成");
//        if (ordDisDeliveryOuts.size() >= BATCH_COUNT) {
//            ordDisDeliveryOuts.clear();
//        }
//        List<TestStoreImportIn> importInList = Lists.newArrayList();
//        List<String> goodsCodeList = null;
//        if (CollectionUtils.isNotEmpty(dataList)) {
//            // 获取商品代码集合
////            goodsCodeList = dataList.get(0).values().stream().collect(Collectors.toList());
//
//            // 获取表格中列索引商品代码Map,过滤excel文件A1格子无效数据
//            Map<Integer, String> columnIndexGoodsCodeMap = dataList.get(0).entrySet().stream()
//                    .filter(entry -> Objects.nonNull(entry.getValue()))
//                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//
//            // 获取门店和商品数量集合
//            List<Map<Integer, String>> storeGoodsQuantityList = dataList.subList(1, dataList.size());
//            // 获取表格中列索引集合
//            List<Integer> columnIndexList = storeGoodsQuantityList.get(0).keySet().stream().collect(Collectors.toList());
//
//            List<TestStoreImportIn> testStoreImportInList = Lists.newArrayList();
//
//            // {0:"9200",1:"2",2:"3"}
//            storeGoodsQuantityList.forEach(storeGoodsQuantityMap -> {
//                TestStoreImportIn testStoreImportIn = new TestStoreImportIn();
//                List<TestGoodsImportIn> goodsList = Lists.newArrayList();
//                storeGoodsQuantityMap.forEach((indexKey, typeValue) -> {
//                    // 门店代码
//                    if (indexKey.equals(NumberUtil.INTEGER_ZERO)) {
//                        testStoreImportIn.setStoreCode(typeValue);
//                        return;
//                    }
//                    // 封装商品与数量
//                    String goodsCode = columnIndexGoodsCodeMap.get(indexKey);
//                    TestGoodsImportIn testGoodsImportIn = new TestGoodsImportIn();
//                    testGoodsImportIn.setGoodsCode(goodsCode);
//                    testGoodsImportIn.setQuantity(new BigDecimal(typeValue));
//                    goodsList.add(testGoodsImportIn);
//                });
//                testStoreImportIn.setGoodsList(goodsList);
//                testStoreImportInList.add(testStoreImportIn);
//            });
//            testStoreImportInList.forEach(testStoreImportIn -> {
//                System.out.println(JSONObject.toJSONString(testStoreImportIn));
//            });
//        }
    }

//    /**
//     * 校验导入数据
//     *
//     * @param data
//     */
//    public void verifyDetailIsExist(String mapKey,Map<String, String> errorMap,ImportDisDeliveryOrder data,StringBuilder errorBuilder){
//        String type = DistributionWaysEnum.getTypeByName(data.getDistributionType());
//        if (Objects.isNull(type)) {
//            errorBuilder.append("配送方式不存在;");
//        }
//        if (Objects.nonNull(type) && !type.equals(DistributionWaysEnum.UNIFIEDDIS.getType())) {
//            errorBuilder.append("只能导入配送方式为统配;");
//        }
//        StockInfoOut stockInfoOut = stockServer.getByCode(data.getStockCode(), bizOrgCode);
//        if (Objects.isNull(stockInfoOut)){
//            errorBuilder.append("仓位不存在;");
//        }
//        if (Objects.nonNull(stockInfoOut)){
//            if(!stockInfoOut.getWarehouseCode().equals(data.getWrhCode())) {
//                errorBuilder.append("仓位与仓储不匹配;");
//            }
//        }
//        com.edc.erp.model.out.StockInfoOut stockInfoByCode = ordDisDeliveryDetailService.findStockInfoByCode(data.getStockCode(), bizOrgCode);
//        if (Objects.isNull(stockInfoByCode)){
//            errorBuilder.append("仓位不允许进行配货;");
//        }
//        StoreStatusInfo storeStatusInfo = new StoreStatusInfo();
//        storeStatusInfo.setBizOrgCode(bizOrgCode);
//        storeStatusInfo.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
//        storeStatusInfo.setStoreProperty(StoreConstant.StoreProperty.FRANCHISE.getMytValue());
//        storeStatusInfo.setStoreCode(data.getStoreCode());
//        StoreInfo storeInfo = storeCenterService.getStatusStoreInfo(storeStatusInfo);
//        if(Objects.isNull(storeInfo)){
//            errorBuilder.append("门店不存在或者门店不允许配销;");
//        }
//     if(Objects.nonNull(storeInfo)){
//        String goodsCode = data.getGoodsCode();
//        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
//        orderGoodsIn.setGoodsCode(goodsCode);
//        orderGoodsIn.setStoreCode(data.getStoreCode());
//        orderGoodsIn.setBizOrgCode(bizOrgCode);
//        OrderGoodsOut goodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
//        if (Objects.isNull(goodsOut)) {
//            errorBuilder.append(goodsCode).append("-不可配货;");
//        }
//        if (Objects.nonNull(goodsOut)) {
//            if (Objects.isNull(goodsOut.getDistributionPrice()) || BigDecimal.ZERO.compareTo(goodsOut.getDistributionPrice()) == NumberUtil.INTEGER_ZERO) {
//                 errorBuilder.append(goodsCode).append("配销价为空;");
//            }
//            boolean isCauPag = true;
//            if (NumberUtil.INTEGER_ZERO.toString().equals(data.getDeliveryQuantity()) || ! cn.hutool.core.util.NumberUtil.isInteger(data.getDeliveryQuantity())){
//                errorBuilder.append("配销数量不可为小数或0;");
//                isCauPag = false;
//            }
//            boolean isError = isCauPag && Objects.nonNull(goodsOut.getDistributionSpecification()) &&
//                    (0 == (Integer.parseInt(data.getDeliveryQuantity()) / goodsOut.getDistributionSpecification().getQpc()) || 0 != (Integer.parseInt(data.getDeliveryQuantity()) % goodsOut.getDistributionSpecification().getQpc()));
//            if (isError) {
//                errorBuilder.append("配销包装数量不可为小数或0;");
//            }
//        }
//     }
//
//    }
//    public String message(){
//        String message = "导入成功：";
//        message += "成功导入" + deliveryOrders.size() + "条数据。";
//        int num = Math.max(0, atomicInteger.get() - deliveryOrders.size() - 2);
//        message += "失败" + num + "条数据。;";
//        message += String.join("\n", this.getErrorDate());
//        return message;
//    }
//    public String errorMessage(){
//        String message = "导入失败：";
//        int num = Math.max(0, atomicInteger.get() - deliveryOrders.size() - 2);
//        message += "失败" + num + "条数据。;";
//        message += String.join("\n", this.getErrorDate());
//        return message;
//    }
//    @Override
//    public void onException(Exception exception, AnalysisContext context)  {
//        log.error("DisDeliveryImportListener，发生异常:{}", exception.getMessage(), exception);
//        if (exception instanceof ExcelDataConvertException) {
//            this.encapsulateErrorMap("数据格式错误；");
//        } else if (exception instanceof BusinessException) {
//            this.encapsulateErrorMap(exception.getMessage());
//        } else {
//            this.encapsulateErrorMap("未知错误；");
//        }
//    }
//    private void encapsulateErrorMap(String error) {
//        Map<String, String> map = new LinkedHashMap<>(2);
//        map.put("key", "第【" + atomicInteger.get() + "】行：");
//        map.put("value", error);
//        this.errorList.add(map);
//        atomicInteger.getAndIncrement();
//    }
}
