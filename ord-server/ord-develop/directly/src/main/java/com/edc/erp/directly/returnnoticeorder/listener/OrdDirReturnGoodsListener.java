package com.edc.erp.directly.returnnoticeorder.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.directly.returnnoticeorder.model.excel.ImportOrdReturnNoticeGoods;
import com.edc.erp.directly.returnnoticeorder.model.out.OrdReturnNoticeGoodsOut;
import com.edc.plugins.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 退货通知单商品导入监听
 *
 * @author yaojinpeng
 * @since 2022/10/29 17:26
 */
@Slf4j
public class OrdDirReturnGoodsListener extends ImportListener<ImportOrdReturnNoticeGoods> {

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;
    /**
     * 临时存放数据
     */
    List<OrdReturnNoticeGoodsOut> returnNoticeGoodsDetail = new ArrayList<>();

    private AtomicInteger atomicInteger = new AtomicInteger(2);

    private OrderGoodsServer orderGoodsServer;

    List<String> goodsCodes = new ArrayList<>();

    private String bizOrgCode;


    public OrdDirReturnGoodsListener(OrderGoodsServer orderGoodsServer, List<String> goodsCodes, String bizOrgCode) {
        this.orderGoodsServer = orderGoodsServer;
        this.goodsCodes = goodsCodes;
        this.bizOrgCode = bizOrgCode;
    }

    public OrdDirReturnGoodsListener() {
    }

    @Override
    public void invoke(ImportOrdReturnNoticeGoods importReturnNoticeGoods, AnalysisContext context) {
        OrdReturnNoticeGoodsOut ordReturnNoticeGoodsOut = new OrdReturnNoticeGoodsOut();
        // 校验第一列 goodsCode
        if (StringUtils.isEmpty(importReturnNoticeGoods.getGoodsCode())) {
            throw new BusinessException("商品代码不能为空;");
        }
        if (CollectionUtils.isNotEmpty(returnNoticeGoodsDetail)) {
            boolean result = returnNoticeGoodsDetail.stream().anyMatch(a -> importReturnNoticeGoods.getGoodsCode().equals(a.getGoodsCode()));
            if (result) {
                throw new BusinessException(importReturnNoticeGoods.getGoodsCode() + "导入文件中商品代码重复;");
            }
        }

        if (CollectionUtils.isNotEmpty(goodsCodes)) {
            boolean result = goodsCodes.stream().anyMatch(a -> importReturnNoticeGoods.getGoodsCode().equals(a));
            if (result) {
                throw new BusinessException(importReturnNoticeGoods.getGoodsCode() + "商品代码明细列表已存在;");
            }
        }

        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(importReturnNoticeGoods.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        OrderGoodsOut goodsOut = orderGoodsServer.getOrderGoods(orderGoodsIn);
        if (goodsOut == null) {
            throw new BusinessException(importReturnNoticeGoods.getGoodsCode() + "商品不存在");
        }
        //商品信息
        ordReturnNoticeGoodsOut.setGoodsCode(importReturnNoticeGoods.getGoodsCode());
        ordReturnNoticeGoodsOut.setGoodsName(goodsOut.getGoodsName());
        ordReturnNoticeGoodsOut.setSort(goodsOut.getSort());
        ordReturnNoticeGoodsOut.setBarCode(goodsOut.getBarCode());
        ordReturnNoticeGoodsOut.setSortName(goodsOut.getSortName());
        ordReturnNoticeGoodsOut.setBrandName(goodsOut.getBrandName());
        ordReturnNoticeGoodsOut.setOrgGoodsId(goodsOut.getOrgGoodsId());
        ordReturnNoticeGoodsOut.setGoodsType(goodsOut.getGoodsType());
        ordReturnNoticeGoodsOut.setSpecification(goodsOut.getDistributionSpecification().getQpcStr());
        returnNoticeGoodsDetail.add(ordReturnNoticeGoodsOut);
        atomicInteger.getAndIncrement();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (returnNoticeGoodsDetail.size() >= BATCH_COUNT) {
            returnNoticeGoodsDetail.clear();
            throw new BusinessException("最大支持导入" + BATCH_COUNT + "条");
        }
    }

    public List<OrdReturnNoticeGoodsOut> getReturnNoticeGoodsDetail() {
        return returnNoticeGoodsDetail;
    }

    public String message() {
        String message = "";
        message += "成功导入" + returnNoticeGoodsDetail.size() + "条数据。";
        int num = Math.max(0, atomicInteger.get() - returnNoticeGoodsDetail.size() - 2);
        message += "失败" + num + "条数据。;";
        message += String.join("\n", this.getErrorDate());
        return message;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("OrdDirReturnGoodsListener解析失败，发生异常:{}", exception.getMessage(), exception);
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
