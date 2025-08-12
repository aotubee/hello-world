package com.edc.erp.presale.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.presale.model.excel.ImportPresaleActivityStore;
import com.edc.plugins.common.exception.BusinessException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PresaleActivityStoreImportListener extends ImportListener<ImportPresaleActivityStore> {
    private static final int BATCH_COUNT = 2000;

    @Getter
    private final List<ImportPresaleActivityStore> resultList = new ArrayList<>();
    /**
     * 起始行
     */
    private final AtomicInteger atomicInteger = new AtomicInteger(2);
    Map<String, String> dataMap = new HashMap<>();

    public PresaleActivityStoreImportListener() {

    }

    @Override
    public void invoke(ImportPresaleActivityStore data, AnalysisContext analysisContext) {
        String mapKey = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);

        if (StringUtils.isBlank(data.getStoreCode())) {
            errorJoiner.add("门店代码不能为空;");
        }
        if (StringUtils.isNotBlank(data.getStoreCode())) {
            String existKey = data.getStoreCode();
            if (dataMap.containsKey(existKey)) {
                errorJoiner.add(data.getStoreCode() + "已存在");
            } else {
                dataMap.put(existKey, existKey);
            }
        }
        // 记录异常信息
        if (errorJoiner.length() > 0) {
            totalErrorMap.put(mapKey, errorJoiner);
        } else {
            resultList.add(data);
        }
        atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (resultList.size() >= BATCH_COUNT) {
            resultList.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("PresaleAdjustOrderDetailImportListener解析失败，发生异常:{}", exception.getMessage(), exception);
        if (exception instanceof ExcelDataConvertException) {
            this.encapsulateErrorMap("数据格式错误;");
        } else if (exception instanceof BusinessException) {
            this.encapsulateErrorMap(exception.getMessage());
        } else {
            this.encapsulateErrorMap("未知错误;");
        }
    }

    private void encapsulateErrorMap(String error) {
        Map<String, String> map = new LinkedHashMap<>(2);
        map.put("key", "第【" + atomicInteger.get() + "】行：");
        map.put("value", error);
        this.errorList.add(map);
        atomicInteger.getAndIncrement();
    }

    public String message() {
        String message = "";
        message += "成功导入" + resultList.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - resultList.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }
}
