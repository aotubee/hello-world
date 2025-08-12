package com.edc.erp.presale.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.erp.presale.enumeration.OrdDisPresaleAdjustOrderTypeEnum;
import com.edc.erp.presale.model.excel.ImportPresaleAdjustOrderDetail;
import com.edc.erp.presale.model.out.PresaleAdjustOrderDetailOut;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.plugins.common.exception.BusinessException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PresaleAdjustOrderDetailImportListener extends ImportListener<ImportPresaleAdjustOrderDetail> {

    @Getter
    private final List<PresaleAdjustOrderDetailOut> adjustOrderDetailOuts = new ArrayList<>();
    /**
     * 起始行
     */
    private final AtomicInteger atomicInteger = new AtomicInteger(2);
    private final Map<String, Boolean> importMap = new HashMap<>();
    private final OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;
    private final String storeCode;
    private final String adjustType;

    public PresaleAdjustOrderDetailImportListener(OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService, String storeCode, String adjustType) {
        this.ordDisPresaleAssetsDetailService = ordDisPresaleAssetsDetailService;
        this.storeCode = storeCode;
        this.adjustType = adjustType;
    }

    @Override
    public void invoke(ImportPresaleAdjustOrderDetail data, AnalysisContext analysisContext) {
        if (StringUtils.isBlank(data.getPresaleActivityNo())) {
            throw new BusinessException("活动单号不能为空;");
        }
        if (StringUtils.isBlank(data.getGoodsCode())) {
            throw new BusinessException("商品代码不能为空;");
        }
        if (data.getAdjustQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("调整数量必须大于0;");
        }
        String existKey = String.format("%s:%s", data.getPresaleActivityNo(), data.getGoodsCode());
        if (importMap.containsKey(existKey)) {
            throw new BusinessException(existKey + "已重复;");
        } else {
            importMap.put(existKey, true);
        }
        OrdDisPresaleAssetsDetail assetsDetail = ordDisPresaleAssetsDetailService.getStoreGoodsAssetsDetail(this.storeCode, data.getGoodsCode(), data.getPresaleActivityNo());
        if (Objects.isNull(assetsDetail)) {
            throw new BusinessException(String.format("门店%s资产商品%s不存在;", this.storeCode, data.getGoodsCode()));
        }
        if (OrdDisPresaleAdjustOrderTypeEnum.REDUCE.getCode().equals(this.adjustType) && data.getAdjustQty().compareTo(assetsDetail.getSurplusQuantity()) > 0) {
            throw new BusinessException(String.format("门店%s资产商品%s剩余订货量不足;", this.storeCode, data.getGoodsCode()));
        }
        adjustOrderDetailOuts.add(PresaleAdjustOrderDetailOut.builder()
                .goodsCode(data.getGoodsCode())
                .adjustQty(data.getAdjustQty())
                .presaleActivityNo(data.getPresaleActivityNo())
                .beforeQty(assetsDetail.getSurplusQuantity())
                .goodsName(assetsDetail.getGoodsName())
                .barCode(assetsDetail.getBarCode())
                .packageSpecification(assetsDetail.getPackageSpecification())
                .packageSpecificationNum(assetsDetail.getPackageSpecificationNum())
                .packageUnit(assetsDetail.getPackageUnit())
                .build()
        );
        atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("ImportPresaleAdjustOrderDetail解析完成");
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
        message += "成功导入" + adjustOrderDetailOuts.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - adjustOrderDetailOuts.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }
}
