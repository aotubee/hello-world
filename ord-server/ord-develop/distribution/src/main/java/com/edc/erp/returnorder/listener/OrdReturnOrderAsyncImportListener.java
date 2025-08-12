package com.edc.erp.returnorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.ExpiryCheckUtil;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.returnorder.model.in.ImportOrdReturnOrderVO;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 退货单导入监听器
 * @author lh
 */
@Slf4j
public class OrdReturnOrderAsyncImportListener extends ImportListener<ImportOrdReturnOrderVO> {
    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 3000;

    /**
     * 临时存放数据 OrdReturnDetailOut
     */
    //List<BaseReturnOrderOut> baseReturnOrderOutList = new ArrayList<>();


    List<ImportOrdReturnOrderVO> importOrdReturnOrderList = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(2);

    Map<String, ImportOrdReturnOrderVO> dataMap = new HashMap<>();

    Set<String> deliveryOrderNoSet = new HashSet<>();

    Map<String, String> storeDeliveryOrderNoMap = new HashMap<>();


    public OrdReturnOrderAsyncImportListener() {

    }

    @Override
    public void invoke(ImportOrdReturnOrderVO data, AnalysisContext context) {
        String mapKey = "第【" + atomicInteger + "】行：";
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        String storeCode = data.getStoreCode();
        String stockCode = data.getStockCode();
        String warehouseCode = data.getWarehouseCode();
        String goodsCode = data.getGoodsCode();
        BigDecimal applyReturnQuantity = data.getApplyReturnQuantity();
        //校验数据
        if (StringUtils.isBlank(storeCode)) {
            errorJoiner.add("门店代码不能为空;");
        }
        if (StringUtils.isBlank(stockCode)) {
            errorJoiner.add("仓位代码不能为空;");
        }
        if (StringUtils.isBlank(warehouseCode)) {
            errorJoiner.add("仓储代码不能为空;");
        }
        if (StringUtils.isBlank(goodsCode)) {
            errorJoiner.add("商品代码不能为空;");
        }
        if (applyReturnQuantity.equals(NumberUtil.INTEGER_ZERO) || applyReturnQuantity == null) {
            errorJoiner.add("申请数量不能为空或者为0;");
        }
//        deliveryOrderNoSet.add(data.getDeliveryOrderNo());
//        if (deliveryOrderNoSet.size() > 1) {
//            errorJoiner.add("配货单单号必须唯一;");
//        }
        if (StringUtils.isNotBlank(data.getStoreCode()) && StringUtils.isNotBlank(data.getDeliveryOrderNo()) && StringUtils.isNotBlank(data.getStockCode())) {
            String existKey = storeCode + SystemConstant.WAIT + data.getStockCode();
            if (storeDeliveryOrderNoMap.containsKey(existKey)) {
                String existDeliveryOrderNo = storeDeliveryOrderNoMap.get(existKey);
                if (!existDeliveryOrderNo.equals(data.getDeliveryOrderNo())) {
                    errorJoiner.add(data.getStoreCode() + "&" + data.getStockCode() + "已存在配销单号" + existDeliveryOrderNo);
                }
            } else {
                storeDeliveryOrderNoMap.put(existKey, data.getDeliveryOrderNo());
            }
        }
        if (StringUtils.isNotBlank(data.getStoreCode()) && StringUtils.isNotBlank(data.getGoodsCode())) {
            String existKey = storeCode + SystemConstant.WAIT + goodsCode;
            if (dataMap.containsKey(existKey)) {
                errorJoiner.add(data.getStoreCode() + "&" + data.getGoodsCode() + "已存在");
            } else {
                dataMap.put(existKey, data);
            }
        }
        if (StringUtils.isBlank(data.getDeliveryOrderNo()) && StringUtils.isNotBlank(data.getExpiry())) {
            Response<LocalDateTime> expiryResponse = ExpiryCheckUtil.checkExpiry(data.getExpiry());
            if (!expiryResponse.isSuccess()) {
                errorJoiner.add(data.getStoreCode() + "&" + data.getGoodsCode() + expiryResponse.getMessage());
            }
        }
        // 记录异常信息
        if (errorJoiner.length() > 0) {
            totalErrorMap.put(mapKey, errorJoiner);
        } else {
            importOrdReturnOrderList.add(data);
        }
        atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (importOrdReturnOrderList.size() >= BATCH_COUNT) {
            importOrdReturnOrderList.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
    }

    public List<ImportOrdReturnOrderVO> getImportOrdReturnOrderList() {
        return importOrdReturnOrderList;
    }

    public String message() {
        String message = "";
        message += "成功导入" + importOrdReturnOrderList.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - importOrdReturnOrderList.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }


    public String errorEessage() {
        String message = "";
        int num = Math.max(0, atomicInteger.get() - importOrdReturnOrderList.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("配销OrdReturnOrderAsyncImportListener解析失败，发生异常:{}", exception.getMessage(), exception);
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
