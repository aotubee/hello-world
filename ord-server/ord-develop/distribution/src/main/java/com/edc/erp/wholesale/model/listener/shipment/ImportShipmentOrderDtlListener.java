package com.edc.erp.wholesale.model.listener.shipment;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.edc.erp.common.enumeration.InvoiceTypeEnum;
import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.rpc.SaleGoodsInfoClient;
import com.edc.erp.common.util.ImportListener;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.WholesaleOrderTypeEnum;
import com.edc.erp.wholesale.model.excel.shipment.ImportShipmentOrderDtl;
import com.edc.erp.wholesale.model.excel.shipment.ImportShipmentOrderIn;
import com.edc.erp.wholesale.model.out.shipment.WholesaleShipmentDetailOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.bean.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 批发出货单批量导入
 * @author lx
 * @since 2022-11-03 17:38:01
 */
@Slf4j
public class ImportShipmentOrderDtlListener extends ImportListener<ImportShipmentOrderDtl> {

    /** 起始行 */
    private final AtomicInteger atomicInteger = new AtomicInteger(2);

    private final List<WholesaleShipmentDetailOut> outDetails = new ArrayList<>();

    private ImportShipmentOrderIn importShipmentOrderIn;

    private SaleGoodsInfoClient saleGoodsInfoClient;

    private boolean isCanEditPrice;

    public ImportShipmentOrderDtlListener(
            ImportShipmentOrderIn importShipmentOrderIn,
            SaleGoodsInfoClient saleGoodsInfoClient, boolean isCanEditPrice) {
        this.importShipmentOrderIn = importShipmentOrderIn;
        this.saleGoodsInfoClient = saleGoodsInfoClient;
        this.isCanEditPrice = isCanEditPrice;
    }

    public Response<List<WholesaleShipmentDetailOut>> getResponse() {
        /**按商品代码对导入的数据去重*/
        List<WholesaleShipmentDetailOut> detailOuts = outDetails.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(WholesaleShipmentDetailOut::getGoodsCode))), ArrayList::new)
        );

        String message = "";
        message += "成功导入" + detailOuts.size() + "条数据。\n";
        int num = Math.max(0, atomicInteger.get() - detailOuts.size() - 2);
        message += "失败" + num + "条数据。;\n";
        message += String.join("\n", this.getErrorDate());
        return Response.data(detailOuts, message);
    }

    public List<WholesaleShipmentDetailOut> getOutDetails() {
        return outDetails;
    }

    @Override
    public void invoke(ImportShipmentOrderDtl data, AnalysisContext context) {
        Assert.notNull(data.getGoodsCode(), () -> {
            throw new BusinessException("商品代码不能为空;");
        });
        Assert.notNull(data.getApplyQuantity(), () -> {
            throw new BusinessException("申请数量不能为空;");
        });
        Assert.isTrue(!NumberUtil.INTEGER_ZERO.equals(data.getApplyQuantity()), () -> {
            throw new BusinessException("申请数量不能为0;");
        });

        Assert.isTrue(NumberUtil.INTEGER_ZERO < data.getApplyQuantity().compareTo(NumberUtil.INTEGER_ZERO), () -> {
            throw new BusinessException("申请数量不能为负数;");
        });

        //校验商品 通过则将商品信息对应出货信息字段保存
        this.verifyDetailIsExist(data);

        //判断list中是否存商品代码
        if (outDetails.stream().anyMatch(a -> a.getGoodsCode().equals(data.getGoodsCode()))) {
            throw new BusinessException(data.getGoodsCode() + "：当前集合中商品代码重复;");
        }
        //获取商品信息
        SaleGoodsInfoOut saleGoodsInfoOut = this.saleGoodsInfo(data);
        //保存明细至结果集
        this.initDetail(data, saleGoodsInfoOut);
        atomicInteger.incrementAndGet();
    }

    /**
     * 保存明细至结果集
     * @param data excel导入数据
     * @param saleGoodsInfoOut 商品信息
     */
    private void initDetail(ImportShipmentOrderDtl data, SaleGoodsInfoOut saleGoodsInfoOut) {
        //明细结果集
        WholesaleShipmentDetailOut wholesaleShipmentDetailOut = new WholesaleShipmentDetailOut();
        //商品code
        wholesaleShipmentDetailOut.setGoodsCode(data.getGoodsCode());
        //申请数量
        wholesaleShipmentDetailOut.setApplyQuantity(data.getApplyQuantity());
        //copy商品属性
        BeanUtils.copy(saleGoodsInfoOut, wholesaleShipmentDetailOut);
        //计算包装数
        String applyPackageNum;
        if (Objects.isNull(saleGoodsInfoOut.getQpc())) {
            applyPackageNum = NumberUtil.INTEGER_ZERO.toString();
        } else {
            if (data.getApplyQuantity() % saleGoodsInfoOut.getQpc() != NumberUtil.INTEGER_ZERO) {
                applyPackageNum = data.getApplyQuantity() / saleGoodsInfoOut.getQpc() + "+" + data.getApplyQuantity() % saleGoodsInfoOut.getQpc();
            } else {
                applyPackageNum = String.valueOf(data.getApplyQuantity() / saleGoodsInfoOut.getQpc());
            }
        }
        wholesaleShipmentDetailOut.setApplyPackageNum(applyPackageNum);
        // 客户可改价并且导入了单价则取导入的，否则取批发价格组的单价
        if (isCanEditPrice && Objects.nonNull(data.getUnitPrice())) {
            wholesaleShipmentDetailOut.setUnitPrice(data.getUnitPrice());
        } else {
            wholesaleShipmentDetailOut.setUnitPrice(saleGoodsInfoOut.getSalePrice());
        }
        //申请金额
        BigDecimal amount = wholesaleShipmentDetailOut.getUnitPrice().multiply(new BigDecimal(data.getApplyQuantity()));
        wholesaleShipmentDetailOut.setApplyAmount(amount);
        //审核数量
        wholesaleShipmentDetailOut.setAuditQuantity(data.getApplyQuantity());
        //审核金额
        wholesaleShipmentDetailOut.setAuditAmount(amount);
        wholesaleShipmentDetailOut.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(wholesaleShipmentDetailOut.getInvoiceType()));
        wholesaleShipmentDetailOut.setStandardSpecs(saleGoodsInfoOut.getStandardSpecs());
        //批量添加
        outDetails.add(wholesaleShipmentDetailOut);
    }

    /**
     * 校验商品是否符合规则
     * @param data excel批量导入入参类
     */
    public void verifyDetailIsExist(ImportShipmentOrderDtl data) {
        //获取商品信息
        SaleGoodsInfoOut saleGoodsInfoOut = this.saleGoodsInfo(data);
        Assert.notNull(saleGoodsInfoOut, () -> {
            throw new BusinessException(data.getGoodsCode() + ":商品状态不允许做批发出货业务;");
        });

//        if(data.getApplyQuantity() % saleGoodsInfoOut.getQpc() != NumberUtil.INTEGER_ZERO){
//            throw new BusinessException(data.getGoodsCode() + "：申请数量必须符合整批包装数");
//        }
//        //查询详情 校验是否已存在
//        WholesaleShipmentDetail dbDetail = wholesaleShipmentMapper.getWholesaleShipmentDetail(
//                data.getGoodsCode(), ShipmentStatusEnum.PENDING.getCode(), importShipmentOrderIn.getBizOrgCode());
//
//        Assert.isNull(dbDetail,() ->{
//            throw new BusinessException(data.getGoodsCode() + "：该商品在出货单中已存在;");
//        });
    }

    /**
     * 查询商品信息
     * @param data excel批量导入入参类
     * @return
     */
    public SaleGoodsInfoOut saleGoodsInfo(ImportShipmentOrderDtl data) {
        //查询商品入参类
        QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
        querySaleGoodsInfoIn.setGoodsCode(data.getGoodsCode());
        querySaleGoodsInfoIn.setBizOrgCode(importShipmentOrderIn.getBizOrgCode());
        querySaleGoodsInfoIn.setClientCode(importShipmentOrderIn.getClientCode());
        querySaleGoodsInfoIn.setStockId(importShipmentOrderIn.getStockId());
        querySaleGoodsInfoIn.setStockCode(importShipmentOrderIn.getStockCode());
        querySaleGoodsInfoIn.setWarehouseCode(importShipmentOrderIn.getWarehouseCode());
        querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.OUT.getCode());
        //查询商品信息
        Response<SaleGoodsInfoOut> response = saleGoodsInfoClient.getGoodsInfo(querySaleGoodsInfoIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            throw new BusinessException(response.getMessage());
        }
        return response.getData();
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("ImportExportShipmentOrderListener解析完成");
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("ImportShipmentOrderDtlListener解析失败，发生异常:{}", exception.getMessage(), exception);
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
