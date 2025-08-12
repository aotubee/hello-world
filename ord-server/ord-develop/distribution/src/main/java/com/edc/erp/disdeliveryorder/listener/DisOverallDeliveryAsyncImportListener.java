package com.edc.erp.disdeliveryorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.model.in.ImportDisDeliveryOrder;
import com.edc.erp.disdeliveryorder.model.out.OrdDisDeliveryOut;
import com.edc.plugins.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zy
 * @description: 配销单批量导入监听器
 * @since 2023/11/21 15:16
 */
@Slf4j
public class DisOverallDeliveryAsyncImportListener extends ImportListener<Map<Integer, String>> {

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;
    /**
     * 临时存放数据
     */
    List<OrdDisDeliveryOut> ordDisDeliveryOuts = new ArrayList<>();

    List<ImportDisDeliveryOrder> deliveryOrders = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(1);


    /**
     * 临时存放数据
     */

    public List<Map<Integer, String>> getDataList() {
        return dataList;
    }

    private List<Map<Integer, String>> dataList = new ArrayList<>();

    private Map<String, String> importMap = new HashMap<>();


    public List<ImportDisDeliveryOrder> getDeliveryOrders() {
        return deliveryOrders;
    }

    public DisOverallDeliveryAsyncImportListener() {

    }

    Map<String, ImportDisDeliveryOrder> dataMap = new HashMap<>();


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
                String deliveryQuantityStr = entry.getValue();
                if (StringUtils.isBlank(deliveryQuantityStr) || Integer.parseInt(deliveryQuantityStr) == NumberUtil.INTEGER_ZERO) {
                    log.info("第" + (column + NumberUtil.INTEGER_ONE) + "列配货数量为空或者等于0,故跳过");
//                    errorJoiner.add("第" + (column + NumberUtil.INTEGER_ONE) + "列配销数量不能为空");
                } else {
                    Integer deliveryQuantity = Integer.parseInt(deliveryQuantityStr);
                    if (NumberUtil.INTEGER_ZERO > deliveryQuantity || !cn.hutool.core.util.NumberUtil.isInteger(deliveryQuantityStr)) {
                        errorJoiner.add("第" + (column + NumberUtil.INTEGER_ONE) + "列配销数量必须为大于0的正整数");
                    }
                }
//                Integer deliveryQuantity = StringUtils.isNotBlank(entry.getValue()) ? Integer.parseInt(entry.getValue()) : NumberUtil.INTEGER_ZERO;
//                if (StringUtils.isBlank(entry.getValue())) {
//                    entry.setValue(NumberUtil.INTEGER_ZERO.toString());
//                }
//                if (deliveryQuantity.compareTo(NumberUtil.INTEGER_ZERO) < NumberUtil.INTEGER_ZERO || !cn.hutool.core.util.NumberUtil.isInteger(deliveryQuantity.toString())) {
//                    errorJoiner.add("第" + (column + NumberUtil.INTEGER_ONE) + "列配销数量必须为正整数");
//                }
            }
        });
        if (errorJoiner.length() > 0) {
            totalErrorMap.put(rowStr, errorJoiner);
        } else {
            log.info(JSONObject.toJSONString(data));
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

    }

    public String message() {
        String message = "导入成功：";
        message += "成功导入" + deliveryOrders.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - deliveryOrders.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }

    public String errorMessage() {
        String message = "导入失败：";
        int num = Math.max(0, atomicInteger.get() - deliveryOrders.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("DisDeliveryImportListener，发生异常:{}", exception.getMessage(), exception);
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
}
