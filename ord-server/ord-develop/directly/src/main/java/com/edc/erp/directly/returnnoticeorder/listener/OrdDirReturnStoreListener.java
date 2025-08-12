package com.edc.erp.directly.returnnoticeorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.out.store.StoreAndClientInfoOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.ImportListener;

import com.edc.erp.directly.returnnoticeorder.enumeration.OrdReturnNoticeTypeEnum;
import com.edc.erp.directly.returnnoticeorder.model.excel.ImportOrdReturnNoticeStore;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.plugins.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 退货通知单门店导入监听
 *
 * @author yaojinpeng
 * @since 2022/10/29 17:27
 */

@Slf4j
public class OrdDirReturnStoreListener extends ImportListener<ImportOrdReturnNoticeStore> {


    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;
    /**
     * 临时存放数据
     */
    List<OrdReturnNoticeStoreOut> returnNoticeStoreDetail = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(2);

    private StoreCenterService storeCenterService;

    private String returnType;

    private String bizOrgCode;


    public OrdDirReturnStoreListener(StoreCenterService storeCenterService, String bizOrgCode, String returnType) {
        this.storeCenterService = storeCenterService;
        this.bizOrgCode = bizOrgCode;
        this.returnType=returnType;
    }

    @Override
    public void invoke(ImportOrdReturnNoticeStore data, AnalysisContext context) {
        OrdReturnNoticeStoreOut detail = new OrdReturnNoticeStoreOut();
        StoreInfo storeInfo = storeCenterService.getStoreByCode(data.getStoreCode(), bizOrgCode);
        if (null == storeInfo) {
            throw new BusinessException(data.getStoreCode() + "门店不存在");
        }
        if (!StoreConstant.StoreProperty.DIRECTLY.getMytValue().equals(storeInfo.getStoreProperty())) {
            throw new BusinessException(data.getStoreCode() + "门店类型不匹配");
        }
        if(CollectionUtils.isNotEmpty(returnNoticeStoreDetail)){
            boolean result = returnNoticeStoreDetail.stream().anyMatch(a ->data.getStoreCode().equals(a.getStoreCode()));
            if(result){
                throw  new BusinessException("门店代码已重复;");
            }
        }
        if(returnType.equals(OrdReturnNoticeTypeEnum.LIMITED_RETURN.getKey())){
            if(null == data.getQty()){
                throw  new BusinessException(data.getStoreCode()+"门店限量退货可退数量不能为空;");
            }
        }
        if(null!=data.getQty()){
            if(data.getQty().equals(BigDecimal.ZERO) ){
                throw  new BusinessException(data.getStoreCode()+"可退数量不能为0;");
            }
            if(BigDecimal.ZERO.compareTo(data.getQty())>0){
                throw  new BusinessException(data.getStoreCode()+"可退数量不能为负数;");
            }
            if(!isIntegerValue(data.getQty())){
                throw  new BusinessException(data.getStoreCode()+"可退数量不能为小数;");
            }
        }


        // 门店信息
            detail.setStoreCode(data.getStoreCode());
            detail.setStoreName(storeInfo.getStoreName());
            detail.setQty(data.getQty());
            returnNoticeStoreDetail.add(detail);
            atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (returnNoticeStoreDetail.size() >= BATCH_COUNT) {
            returnNoticeStoreDetail.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
    }

    public List<OrdReturnNoticeStoreOut> getReturnNoticeStoreDetail() {
        return returnNoticeStoreDetail;
    }

    public String message(){
        String message = "";
        message += "成功导入" + returnNoticeStoreDetail.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - returnNoticeStoreDetail.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context)  {
        log.error("OrdDirReturnStoreListener解析失败，发生异常:{}", exception.getMessage(), exception);
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
