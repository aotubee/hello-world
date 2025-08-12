package com.edc.erp.directly.dirdeliveryorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdeliveryorder.model.in.ImportDirDeliveryOrder;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut;
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
public class DirDeliveryAsyncImportListener extends ImportListener<ImportDirDeliveryOrder> {

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;
    /**
     * 临时存放数据
     */
    List<OrdDirDeliveryOut> ordDirDeliveryOuts = new ArrayList<>();

    List<ImportDirDeliveryOrder> deliveryOrders = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(2);

    public List<ImportDirDeliveryOrder> getDeliveryOrders() {
        return deliveryOrders;
    }

    public DirDeliveryAsyncImportListener() {

    }

    Map<String, ImportDirDeliveryOrder> dataMap = new HashMap<>();


    @Override
    public void invoke(ImportDirDeliveryOrder data, AnalysisContext context) {
        String mapKey = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);

        // 校验第一列 goodsCode
        if (StringUtils.isEmpty(data.getGoodsCode())) {
            errorJoiner.add("商品代码不能为空;");
        }
        if (StringUtils.isEmpty(data.getStoreCode())) {
            errorJoiner.add("门店代码不能为空;");
        }
        if (StringUtils.isEmpty(data.getWrhCode())) {
            errorJoiner.add("仓储代码不能为空;");
        }
        if (StringUtils.isEmpty(data.getStockCode())) {
            errorJoiner.add("仓位代码不能为空;");
        }
        if (Objects.isNull(data.getDeliveryQuantity())) {
            errorJoiner.add("配货数量不能为空;");
        } else {
            if (NumberUtil.INTEGER_ZERO >= data.getDeliveryQuantity() || !cn.hutool.core.util.NumberUtil.isInteger(data.getDeliveryQuantity().toString())) {
                errorJoiner.add("列配货数量必须为大于0的正整数");
            }
        }
        if (Objects.isNull(data.getDistributionType())) {
            errorJoiner.add("配送方式不能为空;");
        }
        String type = DistributionWaysEnum.getTypeByName(data.getDistributionType());
        if (Objects.isNull(type)) {
            errorJoiner.add("配送方式不存在;");
        }else {
            data.setDistributionType(type);
        }
        if (Objects.nonNull(type) && !type.equals(DistributionWaysEnum.UNIFIEDDIS.getType())
                && !type.equals(DistributionWaysEnum.TRANSFER.getType())) {
            errorJoiner.add("只能导入配送方式为统配或中转;");
        }
        if (StringUtils.isNotBlank(data.getStoreCode()) && StringUtils.isNotBlank(data.getGoodsCode())) {
            String existKey = data.getStoreCode() + SystemConstant.WAIT + data.getGoodsCode();
            if (dataMap.containsKey(existKey)) {
                errorJoiner.add(data.getStoreCode() + "&" + data.getGoodsCode() + "已存在");
            } else {
                dataMap.put(existKey, data);
            }
        }
        // 记录异常信息
        if (errorJoiner.length() > 0) {
            totalErrorMap.put(mapKey, errorJoiner);
        } else {
            deliveryOrders.add(data);
        }
        atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (ordDirDeliveryOuts.size() >= BATCH_COUNT) {
            ordDirDeliveryOuts.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
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
