package com.edc.erp.directly.dirfirstorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.directly.dirfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.plugins.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 铺货单导入
 *
 * @author weichao
 * @since 2022-11-03 17:38:01
 */
@Slf4j
public class DirFirstDirOrderAsyncListener extends ImportListener<ImportFirstOrderDetail> {

    /**
     * 每隔5条存储数据库，实际使用中可以1000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 2000;

    /**
     * 起始行
     */
    private final AtomicInteger atomicInteger = new AtomicInteger(2);

    private final List<ImportFirstOrderDetail> outDetails = new ArrayList<>();

    Map<String, Integer> importMap = new HashMap<>();

    public DirFirstDirOrderAsyncListener() {
    }

    public List<ImportFirstOrderDetail> getOutDetails() {
        return outDetails;
    }

    @Override
    public void invoke(ImportFirstOrderDetail data, AnalysisContext context) {
        String mapKey = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        if (StringUtils.isBlank(data.getGoodsCode())) {
            errorJoiner.add("商品代码不能为空");
        }
        if (Objects.isNull(data.getDistributionNum())) {
            errorJoiner.add("铺货数量不能为空");
        }
        if (Objects.nonNull(data.getDistributionNum()) && data.getDistributionNum() <= 0) {
            errorJoiner.add("铺货数量必须大于0");
        }

        String existKey = data.getGoodsCode();
        if (importMap.containsKey(existKey)) {
            errorJoiner.add(data.getGoodsCode() + "已存在");
        } else {
            importMap.put(existKey, data.getDistributionNum());
        }
        if (errorJoiner.length() > 0) {
            totalErrorMap.put(mapKey, errorJoiner);
        } else {
            outDetails.add(data);
        }
        atomicInteger.getAndIncrement();
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (outDetails.size() >= BATCH_COUNT) {
            outDetails.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
        log.info("FirstDirOrderAsyncListener解析完成");
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("FirstDirOrderDetailListener解析失败，发生异常:{}", exception.getMessage(), exception);
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
