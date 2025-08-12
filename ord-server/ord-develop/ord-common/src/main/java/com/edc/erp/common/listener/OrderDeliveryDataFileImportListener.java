package com.edc.erp.common.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.excel.ImportOrderDeliveryDataFile;
import com.edc.erp.common.util.ImportListener;
import com.edc.plugins.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class OrderDeliveryDataFileImportListener extends ImportListener<ImportOrderDeliveryDataFile> {


    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;


    List<ImportOrderDeliveryDataFile> orderDeliveryDataFiles = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(2);

    public List<ImportOrderDeliveryDataFile> getOrderDeliveryDataFiles() {
        return orderDeliveryDataFiles;
    }

    public OrderDeliveryDataFileImportListener(){

    }

    @Override
    public void invoke(ImportOrderDeliveryDataFile data, AnalysisContext context) {
        Map<String, String> errorMap = new LinkedHashMap<>();
        String mapKey = "第【" + atomicInteger + "】行：";
        StringBuilder errorBuilder = new StringBuilder();
        if (StringUtils.isEmpty(data.getGoodsCode())){
            errorBuilder.append("商品代码不能为空;");
        }

        if (StringUtils.isEmpty(data.getOrderNo())){
            errorBuilder.append("单号不能为空;");
        }

        if (null==data.getAmount()){
            errorBuilder.append("数量不能为空;");
        }

        if (null!=data.getAmount() && data.getAmount().compareTo(BigDecimal.ZERO)==-1){
            errorBuilder.append("数量必须大于0;");
        }

//        if (null==data.getLineNo()){
//            errorBuilder.append("行号不能为空;");
//        }
        data.setRemark("");
        // 记录异常信息
        if (errorBuilder.length() == 0) {
            data.setIsSuccessed(1);
        } else {
            errorMap.put("key", mapKey);
            errorMap.put("value", errorBuilder.toString());
            errorList.add(errorMap);
            data.setIsSuccessed(0);
            data.setRemark(errorBuilder.toString());
        }
        orderDeliveryDataFiles.add(data);
        atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (orderDeliveryDataFiles.size() >= BATCH_COUNT) {
            orderDeliveryDataFiles.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
    }


    public String message(){
        String message = "";
        message += "成功导入" + orderDeliveryDataFiles.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - orderDeliveryDataFiles.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }
    public String errorMessage(){
        String message = "";
        int num = Math.max(0, atomicInteger.get() - orderDeliveryDataFiles.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }
    @Override
    public void onException(Exception exception, AnalysisContext context)  {
        log.error("OrderDeliveryDataFileImportListener解析失败，发生异常:{}", exception.getMessage(), exception);
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
