package com.edc.erp.directly.distribution.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.distribution.model.out.OrdDirDistributionImportResultOut;
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
public class DirOverallDistributionAsyncListener extends ImportListener<Map<Integer, String>> {

    /**
     * 每隔5条存储数据库，实际使用中可以2000条，然后清理list ，方便内存回收
     */
//    private static final int BATCH_COUNT = 2000;

    /**
     * 临时存放数据
     */
//    List<OrdDirDeliveryOut> ordDisDeliveryOuts = new ArrayList<>();
    public List<Map<Integer, String>> getDataList() {
        return dataList;
    }

    private List<Map<Integer, String>> dataList = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(1);

    private Map<String, String> importMap = new HashMap<>();

    private Map<String, OrdDirDistributionImportResultOut> importResultMap = new HashMap<>();

    public DirOverallDistributionAsyncListener() {

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
            dataList.add(data);
        }
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
//        if (ordDisDeliveryOuts.size() >= BATCH_COUNT) {
//            ordDisDeliveryOuts.clear();
//        }
        log.info("DirOverallDistributionAsyncListener解析结束");
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

}
