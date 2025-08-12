package com.edc.erp.wholesale.model.listener.returns;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.rpc.SaleGoodsInfoClient;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.enumeration.WholesaleOrderTypeEnum;
import com.edc.erp.wholesale.model.excel.returns.ImportWholesaleReturnDetail;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDetailAndGoodsStrOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 批发退货导入数据监听器
 *
 * @author wanglidong
 * @since 2022/11/3 16:06
 */
@Slf4j
public class ImportWholesaleReturnDetailListener extends ImportListener<ImportWholesaleReturnDetail> {

    /***
     *  定义一个原子整数
     */
    private AtomicInteger atomicInteger = new AtomicInteger(2);

    private String clientCode;

    private String stockCode;

    private String warehouseCode;

    private Integer stockId;

    private SaleGoodsInfoClient saleGoodsInfoClient;

    private boolean isCanEditPrice;

    private String priceGroupCode;

    /**
     * 定义临时存放导入数据集合对象
     */
    private final List<WholesaleReturnDetailAndGoodsStrOut> outDetails = new ArrayList<>();

    public Response<List<WholesaleReturnDetailAndGoodsStrOut>> getResponse() {
        ArrayList<WholesaleReturnDetailAndGoodsStrOut> temp = outDetails.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                new TreeSet<>(Comparator.comparing(c -> c.getGoodsCode()))), ArrayList::new));
        String message = "";
        message += "成功导入" + temp.size() + "条数据。\n";
        int num = Math.max(0, atomicInteger.get() - temp.size() - 2);
        message += "失败" + num + "条数据。;\n";
        message += String.join(";\n", this.getErrorDate());
        return Response.data(temp, message);
    }

    public ImportWholesaleReturnDetailListener(String clientCode, String stockCode, String warehouseCode, Integer stockId,
                                               SaleGoodsInfoClient saleGoodsInfoClient, boolean isCanEditPrice, String priceGroupCode) {
        this.clientCode = clientCode;
        this.stockCode = stockCode;
        this.warehouseCode = warehouseCode;
        this.stockId = stockId;
        this.saleGoodsInfoClient = saleGoodsInfoClient;
        this.isCanEditPrice = isCanEditPrice;
        this.priceGroupCode = priceGroupCode;
    }

    /**
     * 解析每一条数据
     *
     * @param importWholesaleReturnDetail 解析出来的对象
     * @param analysisContext
     */
    @Override
    public void invoke(ImportWholesaleReturnDetail importWholesaleReturnDetail, AnalysisContext analysisContext) {
        String goodsCode = importWholesaleReturnDetail.getGoodsCode();
        Integer applyQuantity = importWholesaleReturnDetail.getApplyQuantity();
        //校验商品代码
        Assert.notNull(goodsCode, () -> {
            throw new BusinessException("商品代码为空；");
        });
        //校验申请数量
        Assert.notNull(applyQuantity, () -> {
            throw new BusinessException("申请数量为空；");
        });
        //校验申请数量是否符合条件
        if (applyQuantity <= 0 || applyQuantity % 1 != 0) {
            throw new BusinessException("申请数量不能为小数、负数和0");
        }
        //校验商品代码是否存在，并根据商品代码查询商品相关信息
        WholesaleReturnDetailAndGoodsStrOut wholesaleReturnDetail = this.getWholesaleReturnDetail(importWholesaleReturnDetail);
        this.checkRepeat(outDetails, wholesaleReturnDetail);
        outDetails.add(wholesaleReturnDetail);
        this.atomicInteger.incrementAndGet();
    }

    /**
     * 根据商品代码获取批发退货详情对象
     *
     * @param importWholesaleReturnDetail
     */
    private WholesaleReturnDetailAndGoodsStrOut getWholesaleReturnDetail(ImportWholesaleReturnDetail importWholesaleReturnDetail) {
        WholesaleReturnDetail wholesaleReturnDetail = new WholesaleReturnDetail();
        wholesaleReturnDetail.setGoodsCode(importWholesaleReturnDetail.getGoodsCode());
        wholesaleReturnDetail.setApplyQuantity(importWholesaleReturnDetail.getApplyQuantity());
        wholesaleReturnDetail.setCheckQuantity(importWholesaleReturnDetail.getApplyQuantity());
        SaleGoodsInfoOut saleGoodsInfoOut = this.saleGoodsInfo(wholesaleReturnDetail, clientCode, stockCode, warehouseCode, stockId);
        //将商品信息设置进详情对象中
        wholesaleReturnDetail.setGoodsName(saleGoodsInfoOut.getGoodsName());
        wholesaleReturnDetail.setGoodsType(saleGoodsInfoOut.getGoodsType());
        wholesaleReturnDetail.setBarCode(saleGoodsInfoOut.getBarCode());
        wholesaleReturnDetail.setPackageSpecification(saleGoodsInfoOut.getPackageSpecification());
        wholesaleReturnDetail.setPackageUnit(saleGoodsInfoOut.getPackageUnit());
        wholesaleReturnDetail.setInvoiceType(saleGoodsInfoOut.getInvoiceType());
        // 客户可改价并且导入了单价则取导入的，否则取批发价格组的单价
        if (isCanEditPrice && Objects.nonNull(importWholesaleReturnDetail.getUnitPrice())) {
            wholesaleReturnDetail.setReturnsPrice(importWholesaleReturnDetail.getUnitPrice());
        } else {
            wholesaleReturnDetail.setReturnsPrice(saleGoodsInfoOut.getSalePrice());
        }
        wholesaleReturnDetail.setReturnPrinciple(saleGoodsInfoOut.getReturnPrinciple());
        WholesaleReturnDetail dataDetail = this.getDataDetail(saleGoodsInfoOut, wholesaleReturnDetail);
        WholesaleReturnDetailAndGoodsStrOut wholesaleReturnDetailAndGoodsStrOut = new WholesaleReturnDetailAndGoodsStrOut();
        BeanUtil.copyProperties(dataDetail, wholesaleReturnDetailAndGoodsStrOut);
        wholesaleReturnDetailAndGoodsStrOut.setGoodsTypeStr(saleGoodsInfoOut.getGoodsTypeStr());
        wholesaleReturnDetailAndGoodsStrOut.setBusinessQty(saleGoodsInfoOut.getBusinessQty());
        wholesaleReturnDetail.setVendorCode(saleGoodsInfoOut.getVendorCode());
        wholesaleReturnDetailAndGoodsStrOut.setVendorCode(saleGoodsInfoOut.getVendorCode());
        wholesaleReturnDetailAndGoodsStrOut.setStandardSpecs(saleGoodsInfoOut.getStandardSpecs());
        return wholesaleReturnDetailAndGoodsStrOut;
    }

    /**
     * 解析完成后执行的操作
     *
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("ImportWholesaleReturnDetailListener解析完成");
    }

    /**
     * 处理错误信息
     *
     * @param exception
     * @param context
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("ImportWholesaleReturnDetailListener解析失败，发生异常:{}", exception.getMessage(), exception);
        if (exception instanceof ExcelDataConvertException) {
            this.encapsulateErrorMap("数据格式错误；");
        } else if (exception instanceof BusinessException) {
            this.encapsulateErrorMap(exception.getMessage());
        } else {
            this.encapsulateErrorMap("未知错误；");
        }
    }

    /**
     * 保存错误提示信息
     *
     * @param error
     */
    private void encapsulateErrorMap(String error) {
        Map<String, String> map = new LinkedHashMap<>(2);
        map.put("key", "第【" + atomicInteger + "】行：");
        map.put("value", error + "\r\n");
        this.errorList.add(map);
        atomicInteger.getAndIncrement();
    }

    public SaleGoodsInfoOut saleGoodsInfo(WholesaleReturnDetail wholesaleReturnDetail, String clientCode, String stockCode, String warehouseCode, Integer stockId) {
        //查询商品入参类
        QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
        querySaleGoodsInfoIn.setGoodsCode(wholesaleReturnDetail.getGoodsCode());
        querySaleGoodsInfoIn.setBizOrgCode(UserUtil.getBizOrgCode());
        querySaleGoodsInfoIn.setClientCode(clientCode);
        querySaleGoodsInfoIn.setStockId(stockId);
        querySaleGoodsInfoIn.setStockCode(stockCode);
        querySaleGoodsInfoIn.setWarehouseCode(warehouseCode);
        querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.RETURNS.getCode());
        querySaleGoodsInfoIn.setPriceGroupCode(priceGroupCode);
        //查询商品信息
        Response<SaleGoodsInfoOut> response = saleGoodsInfoClient.getGoodsInfo(querySaleGoodsInfoIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            throw new BusinessException(response.getMessage());
        }
        return response.getData();
    }

    private WholesaleReturnDetail getDataDetail(SaleGoodsInfoOut goodsInfo, WholesaleReturnDetail wholesaleReturnDetail) {
        BigDecimal returnsPrice = wholesaleReturnDetail.getReturnsPrice();
        //计算申请包装数
        wholesaleReturnDetail.setApplyPackageNum(Objects.isNull(goodsInfo.getQpc()) ? NumberUtils.INTEGER_ZERO.toString() :
                wholesaleReturnDetail.getApplyQuantity() / goodsInfo.getQpc() + "+" + (wholesaleReturnDetail.getApplyQuantity() % goodsInfo.getQpc() != NumberUtils.INTEGER_ZERO ? "1" : "0"));
        //设置库存价
        wholesaleReturnDetail.setInventoryPrice(goodsInfo.getInventoryPrice());
        //计算申请金额
        BigDecimal applyAmount = NumberUtil.round(returnsPrice.multiply(new BigDecimal(wholesaleReturnDetail.getApplyQuantity())), com.edc.erp.common.util.NumberUtil.INTEGER_FOUR);
        wholesaleReturnDetail.setApplyAmount(applyAmount);
        //计算实际入库金额=入库数量*批发价
        Integer storageQuantity = wholesaleReturnDetail.getStorageQuantity();
        BigDecimal practicalStorageAmount = wholesaleReturnDetail.getPracticalStorageAmount();
        if (ObjectUtil.isNotEmpty(storageQuantity) && ObjectUtil.isNotEmpty(returnsPrice)) {
            practicalStorageAmount = NumberUtil.round(returnsPrice.multiply(new BigDecimal(storageQuantity)), com.edc.erp.common.util.NumberUtil.INTEGER_FOUR);
            wholesaleReturnDetail.setPracticalStorageAmount(practicalStorageAmount);
        }
        //获取销项税率
        BigDecimal outTax = goodsInfo.getOutTax();
        if (ObjectUtil.isNull(outTax)) {
            outTax = BigDecimal.ZERO;
        }
        //计算入库去税金额=入库金额/（1+税率）
        if (ObjectUtil.isNotEmpty(practicalStorageAmount) && !practicalStorageAmount.equals(BigDecimal.ZERO)) {
            BigDecimal storageNetProfit = NumberUtil.round(practicalStorageAmount.divide(BigDecimal.ONE.add(outTax)), com.edc.erp.common.util.NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setStorageNetProfit(storageNetProfit);
        }
        //计算入库税额=入库去税金额*税率
        if (ObjectUtil.isNotEmpty(practicalStorageAmount) && !practicalStorageAmount.equals(BigDecimal.ZERO)) {
            BigDecimal storageTax = NumberUtil.round(practicalStorageAmount.multiply(outTax), com.edc.erp.common.util.NumberUtil.INTEGER_FOUR);
            wholesaleReturnDetail.setStorageTax(storageTax);
        }
        //计算成本金额=仓储库存价*入库数量
        if (ObjectUtil.isNotEmpty(storageQuantity) && ObjectUtil.isNotEmpty(goodsInfo.getInventoryPrice())) {
            BigDecimal costAmount = NumberUtil.round(goodsInfo.getInventoryPrice().multiply(new BigDecimal(storageQuantity)), com.edc.erp.common.util.NumberUtil.INTEGER_FOUR);
            wholesaleReturnDetail.setCostAmount(costAmount);
        }
        //计算成本去税金额=成本金额/（1+税率）
        BigDecimal costAmount = wholesaleReturnDetail.getCostAmount();
        if (ObjectUtil.isNotEmpty(costAmount) && !costAmount.equals(BigDecimal.ZERO)) {
            BigDecimal costNetProfitAmount = NumberUtil.round(costAmount.divide(BigDecimal.ONE.add(outTax)), com.edc.erp.common.util.NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setCostNetProfitAmount(costNetProfitAmount);
        }
        //计算成本税额=成本去税金额*税率
        BigDecimal costNetProfitAmount = wholesaleReturnDetail.getCostNetProfitAmount();
        if (ObjectUtil.isNotEmpty(costNetProfitAmount) && !costNetProfitAmount.equals(BigDecimal.ZERO)) {
            BigDecimal costTax = NumberUtil.round(costNetProfitAmount.multiply(outTax), com.edc.erp.common.util.NumberUtil.INTEGER_FOUR);
            wholesaleReturnDetail.setCostTax(costTax);
        }
        wholesaleReturnDetail.setInvoiceType(goodsInfo.getInvoiceType());
        return wholesaleReturnDetail;
    }

    /**
     * 导入重复校验
     *
     * @param outDetails
     */
    private void checkRepeat(List<WholesaleReturnDetailAndGoodsStrOut> outDetails, WholesaleReturnDetailAndGoodsStrOut rawData) {
        outDetails.forEach(c -> {
            if (c.getGoodsCode().equals(rawData.getGoodsCode())) {
                throw new BusinessException(rawData.getGoodsCode() + "商品已存在,重复数据已删除");
            }
        });
    }
}