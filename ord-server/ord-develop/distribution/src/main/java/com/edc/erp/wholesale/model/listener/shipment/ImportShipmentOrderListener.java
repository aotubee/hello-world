package com.edc.erp.wholesale.model.listener.shipment;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.wholesale.model.excel.shipment.ImportShipmentOrder;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批发出货单批量导入
 * @author gusiyuan
 * @since 2023-10-12 14:38:01
 */
@Slf4j
public class ImportShipmentOrderListener extends ImportListener<ImportShipmentOrder> {

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;
    /** 起始行 */
    private final AtomicInteger atomicInteger = new AtomicInteger(2);
    private final Map<String, List<ImportShipmentOrder>> spiltMap = new HashMap<>();
    private final List<ImportShipmentOrder> outDetails = new ArrayList<>();

    public List<ImportShipmentOrder> getOutDetails() {
        return outDetails;
    }

    @Override
    public void invoke(ImportShipmentOrder data, AnalysisContext context) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        if (StringUtils.isEmpty(data.getClientCode())) {
            errorJoiner.add("客户代码为空;");
        }
        if (StringUtils.isEmpty(data.getShipmentStockCode())) {
            errorJoiner.add("出库仓位为空;");
        }
        String type = DistributionWaysEnum.getTypeByName(data.getDistributionType());
        if (Objects.isNull(type)) {
            errorJoiner.add("配送方式不存在;");
        } else {
            data.setDistributionType(type);
        }
        if (Objects.nonNull(type) && !type.equals(DistributionWaysEnum.UNIFIEDDIS.getType())
                && !type.equals(DistributionWaysEnum.TRANSFER.getType())) {
            errorJoiner.add("只能导入配送方式为统配或中转;");
        }else {
            data.setDistributionType(type);
        }
        if (StringUtils.isEmpty(data.getGoodsCode())) {
            errorJoiner.add("商品代码为空;");
        }
        if (data.getApplyQuantity() == null) {
            errorJoiner.add("申请数量为空;");
        } else {
            if (NumberUtil.INTEGER_ZERO.equals(data.getApplyQuantity())) {
                errorJoiner.add("申请数量为0;");
            }
            if (NumberUtil.INTEGER_ZERO > data.getApplyQuantity().compareTo(NumberUtil.INTEGER_ZERO)) {
                errorJoiner.add("申请数量为负数;");
            }
        }
        if (errorJoiner.length() == 0) {
            List<ImportShipmentOrder> mapValue = new ArrayList<>();
            String spiltOrderKey = this.buildSpiltOrderKey(data);
            if (spiltMap.containsKey(spiltOrderKey)) {
                mapValue = spiltMap.get(spiltOrderKey);
                boolean repeat = mapValue.stream().anyMatch(a -> a.getGoodsCode().equals(data.getGoodsCode()));
                // 拆单的key若包含商品一致则报错
                if (repeat) {
                    this.encapsulateErrorMap("数据重复;");
                    return;
                }
            }
            // 客户代码、仓储代码、仓位代码、来源单号拆单，如果来源单号为空，就按其他拆单规格成一个单
            mapValue.add(data);
            spiltMap.put(spiltOrderKey, mapValue);

            ImportShipmentOrder importShipmentOrder = new ImportShipmentOrder();
            BeanUtils.copy(data, importShipmentOrder);
            outDetails.add(importShipmentOrder);
            atomicInteger.incrementAndGet();
        } else {
            // 记录错误
            this.encapsulateErrorMap(errorJoiner.toString());
        }
    }

    private String buildSpiltOrderKey(ImportShipmentOrder importShipmentOrder) {
        String preKey = importShipmentOrder.getClientCode() + "$" + importShipmentOrder.getShipmentStockCode() +  "$" + importShipmentOrder.getDistributionType() ;
        if (StringUtils.isNotEmpty(importShipmentOrder.getSourceNo())) {
            preKey += "$" + importShipmentOrder.getSourceNo();
        }
        return preKey;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (outDetails.size() > BATCH_COUNT) {
            outDetails.clear();
            throw new BusinessException("最多导入行数为" + BATCH_COUNT + "行，请确认！");
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("ImportShipmentOrderListener解析失败，发生异常:{}", exception.getMessage(), exception);
        // 如果是某一个单元格的转换异常 能获取到具体行号
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
            int rowNo = excelDataConvertException.getRowIndex() + 1;
            Map<String, String> errorMap = new LinkedHashMap<>();
            errorMap.put("key", "第【" + rowNo + "】行：");
            errorMap.put("value", "数据格式错误; ");
            errorList.add(errorMap);
        }
        atomicInteger.getAndIncrement();
    }

    private void encapsulateErrorMap(String error) {
        Map<String, String> map = new LinkedHashMap<>(2);
        map.put("key", "第【" + atomicInteger.get() + "】行：");
        map.put("value", error);
        this.errorList.add(map);
        atomicInteger.getAndIncrement();
    }
}
