package com.edc.erp.returnnoticeorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreAndClientInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.returnnoticeorder.enumeration.OrdReturnNoticeTypeEnum;
import com.edc.erp.returnnoticeorder.model.in.ImportOrdReturnNoticeDetail;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.erp.returnnoticeorder.model.out.OrdReturnNoticeStoreOut;
import com.edc.plugins.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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
public class OrdDisReturnListener extends ImportListener<ImportOrdReturnNoticeDetail> {


    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 3000;

    private AtomicInteger atomicInteger = new AtomicInteger(2);

    private int successCount = 0;

    private StoreCenterService storeCenterService;

    private OrderGoodsServer orderGoodsServer;

    private String returnType;

    private String bizOrgCode;

    /**
     * 防重
     */
    Map<String, Map<String, String>> preventDuplicationMap = new HashMap<>();

    /**
     * 临时存放数据商品
     */
    Map<String, OrdReturnNoticeGoodsOut> goodsDataMap = new HashMap<>();

    /**
     * 导入前此通知单的明细数据
     */
    private Map<String, Map<String, String>> detailMap;


    public OrdDisReturnListener(StoreCenterService storeCenterService, OrderGoodsServer orderGoodsServer, String bizOrgCode, String returnType, Map<String, Map<String, String>> detailMap) {
        this.storeCenterService = storeCenterService;
        this.orderGoodsServer = orderGoodsServer;
        this.bizOrgCode = bizOrgCode;
        this.returnType=returnType;
        this.detailMap = detailMap;
    }

    @Override
    public void invoke(ImportOrdReturnNoticeDetail data, AnalysisContext context) {
        Integer rowNumber = context.readSheetHolder().getApproximateTotalRowNumber();
        if (rowNumber > (BATCH_COUNT + 1)) {
            throw new ExcelAnalysisException("超出总行数限制，当前总行数为：" + rowNumber);
        }
        OrdReturnNoticeStoreOut detail = new OrdReturnNoticeStoreOut();
        if(StringUtils.isBlank(data.getGoodsCode())){
            throw new BusinessException("商品代码不能为空;");
        }
        if(StringUtils.isBlank(data.getStoreCode())){
            throw new BusinessException("门店代码不能为空;");
        }
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(data.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        OrderGoodsOut goodsOut = orderGoodsServer.getOrderGoods(orderGoodsIn);
        if (goodsOut == null){
            throw new BusinessException("商品不存在;");
        }
        StoreAndClientInfoOut storeByStoreCodeAndType = storeCenterService.getStoreByStoreCodeAndType(data.getStoreCode(), StoreConstant.StoreProperty.FRANCHISE.getMytValue(), bizOrgCode);
        if (null == storeByStoreCodeAndType){
            throw new BusinessException(data.getStoreCode()+"门店不存在或者门店类型不匹配;");
        }
        if(OrdReturnNoticeTypeEnum.LIMITED_RETURN.getKey().equals(returnType)){
            if(Objects.isNull(data.getQty())){
                throw new BusinessException(data.getStoreCode()+"限量退货时可退数量不能为空;");
            }
            if(NumberUtils.INTEGER_ZERO.equals(data.getQty())){
                throw new BusinessException(data.getStoreCode()+"可退数量不能为0;");
            }
            if(NumberUtils.INTEGER_ZERO.compareTo(data.getQty()) > 0) {
                throw new BusinessException(data.getStoreCode()+"可退数量不能为负数;");
            }
        }
        // 还需要防止重复导入
        Map<String, String> storeOutMap = new HashMap<>();
        if (preventDuplicationMap.containsKey(data.getGoodsCode())) {
            storeOutMap = preventDuplicationMap.get(data.getGoodsCode());
        } else {
            OrdReturnNoticeGoodsOut ordReturnNoticeGoodsOut = new OrdReturnNoticeGoodsOut();
            //商品信息
            ordReturnNoticeGoodsOut.setGoodsCode(data.getGoodsCode());
            ordReturnNoticeGoodsOut.setGoodsName(goodsOut.getGoodsName());
            ordReturnNoticeGoodsOut.setSort(goodsOut.getSort());
            ordReturnNoticeGoodsOut.setBarCode(goodsOut.getBarCode());
            ordReturnNoticeGoodsOut.setSortName(goodsOut.getSortName());
            ordReturnNoticeGoodsOut.setBrandName(goodsOut.getBrandName());
            ordReturnNoticeGoodsOut.setBrand(goodsOut.getBrand());
            ordReturnNoticeGoodsOut.setOrgGoodsId(goodsOut.getOrgGoodsId());
            ordReturnNoticeGoodsOut.setGoodsType(goodsOut.getGoodsType());
            ordReturnNoticeGoodsOut.setSpecification(goodsOut.getDistributionSpecification().getQpcStr());
            ordReturnNoticeGoodsOut.setStoreInfo(new ArrayList<>());
            goodsDataMap.put(data.getGoodsCode(), ordReturnNoticeGoodsOut);
        }
        if (storeOutMap.containsKey(data.getStoreCode())) {
            throw new BusinessException("商品" + data.getGoodsCode() + "门店" + data.getStoreCode() + "导入文件中重复;");
        }
        if (detailMap.containsKey(data.getGoodsCode()) && detailMap.get(data.getGoodsCode()).containsKey(data.getStoreCode())) {
            throw new BusinessException("商品" + data.getGoodsCode() + "门店" + data.getStoreCode() + "与数据库中数据重复;");
        }
        //校验此门店是否可退此商品
        orderGoodsIn.setStoreCode(data.getStoreCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS_FAST.getType());
        OrderGoodsOut storeOrderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(storeOrderGoods)) {
            throw new BusinessException("门店" + data.getStoreCode() + "商品" + data.getGoodsCode() + "不存在，请查证后添加;");
        }
        if (Objects.isNull(storeOrderGoods.getDistributionUnitPrice())) {
            throw new BusinessException("门店" + data.getStoreCode() + "商品" + data.getGoodsCode() + "无配销价，请查证后添加;");
        }
        // 门店信息
        detail.setStoreCode(data.getStoreCode());
        detail.setStoreName(storeByStoreCodeAndType.getStoreName());
        detail.setReturnNum(OrdReturnNoticeTypeEnum.LIMITED_RETURN.getKey().equals(returnType) ? BigDecimal.valueOf(data.getQty()) : null);
        detail.setClient(storeByStoreCodeAndType.getClientName()+"【"+storeByStoreCodeAndType.getStoreCode()+"】");
        storeOutMap.put(data.getStoreCode(), data.getStoreCode());
        goodsDataMap.get(data.getGoodsCode()).getStoreInfo().add(detail);
        preventDuplicationMap.put(data.getGoodsCode(), storeOutMap);
        atomicInteger.getAndIncrement();
        successCount ++;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (goodsDataMap.size() >= BATCH_COUNT) {
            goodsDataMap.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
    }

    public List<OrdReturnNoticeGoodsOut> getReturnNoticeGoodsDetail() {
        return new ArrayList<>(goodsDataMap.values());
    }


    public String message() {
        String message = "";
        message += "成功导入" + successCount + "条数据。";
        int num = Math.max(0, atomicInteger.get() - successCount - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context)  {
        log.error("OrdDisReturnStoreListener解析失败，发生异常:{}", exception.getMessage(), exception);
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
