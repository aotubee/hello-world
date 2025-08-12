package com.edc.erp.presale.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.erp.presale.enumeration.OrdDisPresaleAdjustOrderTypeEnum;
import com.edc.erp.presale.model.excel.ImportPresaleAdjustOrder;
import com.edc.erp.presale.model.excel.ImportPresaleAdjustOrderOut;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.plugins.common.exception.BusinessException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PresaleAdjustOrderImportListener extends ImportListener<ImportPresaleAdjustOrder> {
    @Getter
    private final List<ImportPresaleAdjustOrderOut> presaleAdjustOrders = new ArrayList<>();
    private final OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;

    /**
     * 起始行
     */
    private final AtomicInteger atomicInteger = new AtomicInteger(2);
    private final Map<String, Boolean> importMap = new HashMap<>();

    public PresaleAdjustOrderImportListener(OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService) {
        this.ordDisPresaleAssetsDetailService = ordDisPresaleAssetsDetailService;
    }

    @Override
    public void invoke(ImportPresaleAdjustOrder data, AnalysisContext analysisContext) {
        if (StringUtils.isBlank(data.getStoreCode())) {
            throw new BusinessException("门店代码不能为空;");
        }
        if (StringUtils.isBlank(data.getPresaleActivityNo())) {
            throw new BusinessException("活动单号不能为空;");
        }
        if (StringUtils.isBlank(data.getGoodsCode())) {
            throw new BusinessException("商品代码不能为空;");
        }
        // 判断调整类型是否OrdDisPresaleAdjustOrderTypeEnum,data.getAdjustType()是否在枚举中
        if (Arrays.stream(OrdDisPresaleAdjustOrderTypeEnum.values()).map(OrdDisPresaleAdjustOrderTypeEnum::getName).noneMatch(a -> a.equals(data.getAdjustType()))) {
            throw new BusinessException("调整类型不正确;");
        } else { // 如果正确转换成code
            data.setAdjustType(OrdDisPresaleAdjustOrderTypeEnum.getCode(data.getAdjustType()));
        }
        if (data.getAdjustQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("调整数量必须大于0;");
        }
        String existKey = String.format("%s:%s:%s", data.getStoreCode(), data.getPresaleActivityNo(), data.getGoodsCode());
        if (importMap.containsKey(existKey)) {
            throw new BusinessException(existKey + "已重复;");
        } else {
            importMap.put(existKey, true);
        }
        OrdDisPresaleAssetsDetail assetsDetail = ordDisPresaleAssetsDetailService.getStoreGoodsAssetsDetail(data.getStoreCode(), data.getGoodsCode(), data.getPresaleActivityNo());
        if (Objects.isNull(assetsDetail)) {
            throw new BusinessException(String.format("门店%s资产商品%s不存在;", data.getStoreCode(), data.getGoodsCode()));
        }
        if (OrdDisPresaleAdjustOrderTypeEnum.REDUCE.getCode().equals(data.getAdjustType()) && data.getAdjustQty().compareTo(assetsDetail.getSurplusQuantity()) > 0) {
            throw new BusinessException(String.format("门店%s资产商品%s剩余订货量不足;", data.getStoreCode(), data.getGoodsCode()));
        }
        presaleAdjustOrders.add(ImportPresaleAdjustOrderOut.builder()
                .goodsCode(assetsDetail.getGoodsCode())
                .adjustType(data.getAdjustType())
                .adjustQty(data.getAdjustQty())
                .storeCode(data.getStoreCode())
                .presaleActivityNo(data.getPresaleActivityNo())
                .goodsName(assetsDetail.getGoodsName())
                .barCode(assetsDetail.getBarCode())
                .surplusQuantity(assetsDetail.getSurplusQuantity())
                .packageUnit(assetsDetail.getPackageUnit())
                .packageSpecification(assetsDetail.getPackageSpecification())
                .packageSpecificationNum(assetsDetail.getPackageSpecificationNum())
                .build());
        atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("ImportPresaleAdjustOrder解析完成");
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("PresaleAdjustOrderImportListener解析失败，发生异常:{}", exception.getMessage(), exception);
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
        message += "成功导入" + presaleAdjustOrders.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - presaleAdjustOrders.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }
}
