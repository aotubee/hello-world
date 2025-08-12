package com.edc.erp.wholesale.returns.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.edc.erp.common.enumeration.InvoiceTypeEnum;
import com.edc.erp.common.model.in.customer.QueryClientDistInfoIn;
import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.rpc.SaleGoodsInfoClient;
import com.edc.erp.common.service.ClientDistInfoService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.WholesaleOrderTypeEnum;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsGoodsService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * @author lee
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WholesaleReturnsGoodsServiceImpl implements WholesaleReturnsGoodsService {

    private final SaleGoodsInfoClient saleGoodsInfoClient;

    private final ClientDistInfoService clientDistInfoService;

    /**
     * 校验单条批发退货单详情数据
     *
     * @param wholesaleReturnDetail
     * @param clientCode
     * @param stockCode
     * @param warehouseCode
     * @param stockId
     * @param shipmentDetailIn
     * @param status
     * @return
     */
    @Override
    public Response checkWholesaleReturnDetail(WholesaleReturnDetail wholesaleReturnDetail, String clientCode, String stockCode,
                                               String warehouseCode, Integer stockId, WholesaleShipmentDetailIn shipmentDetailIn,
                                               String status) {
        if (StrUtil.isBlank(wholesaleReturnDetail.getGoodsCode())) {
            return Response.error("商品代码不能为空");
        }
        // 查询客户
        List<ClientDistInfoOut> clientDistInfos = clientDistInfoService.findClientDistInfo(new QueryClientDistInfoIn(clientCode, UserUtil.getBizOrgCode()));
        if (CollectionUtils.isEmpty(clientDistInfos)) {
            log.error("批发退查询客户代码{}异常", clientCode);
            throw new BusinessException("批发退查询客户" + clientCode + "校验失败");
        }
        ClientDistInfoOut clientDistInfo = clientDistInfos.get(0);
        QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
        querySaleGoodsInfoIn.setGoodsCode(wholesaleReturnDetail.getGoodsCode());
        querySaleGoodsInfoIn.setClientCode(clientCode);
        querySaleGoodsInfoIn.setStockCode(stockCode);
        querySaleGoodsInfoIn.setStockId(stockId);
        querySaleGoodsInfoIn.setBizOrgCode(UserUtil.getBizOrgCode());
        querySaleGoodsInfoIn.setWarehouseCode(warehouseCode);
        querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.RETURNS.getCode());
        querySaleGoodsInfoIn.setPriceGroupCode(clientDistInfo.getPriceGroupCode());
        //远程调用查询商品信息详情
        Response<SaleGoodsInfoOut> goodsInfoResult = saleGoodsInfoClient.getGoodsInfo(querySaleGoodsInfoIn);
        if (!goodsInfoResult.isSuccess() || Objects.isNull(goodsInfoResult.getData())) {
            throw new BusinessException(goodsInfoResult.getMessage());
        }
        //获取商品信息
        SaleGoodsInfoOut goodsInfo = goodsInfoResult.getData();
        //校验申请数量和申请包装数
        Integer applyQuantity = wholesaleReturnDetail.getApplyQuantity();
        if (ObjectUtil.isEmpty(applyQuantity) || applyQuantity <= 0 || applyQuantity % 1 != 0) {
            return Response.error("申请数量不能为空，且不能为小数、负数和0");
        }

        //获取批发出 出库数量
        Integer shipmentQuantity = Objects.nonNull(shipmentDetailIn) ? shipmentDetailIn.getShipmentQuantity() : null;
        //如果存在出货单校验申请数量不能大于出库数量
        if (Objects.nonNull(shipmentQuantity) && com.edc.erp.common.util.NumberUtil.INTEGER_ZERO > shipmentQuantity.compareTo(applyQuantity)) {
            return Response.error(goodsInfo.getGoodsCode() + "商品退货数量【" + applyQuantity + "】不能大于出货单出库数量" + "【" + shipmentQuantity + "】");
        }

        String applyPackageNum = wholesaleReturnDetail.getApplyPackageNum();
//        if (ObjectUtil.isEmpty(applyPackageNum) || applyPackageNum <= 0 || applyPackageNum % 1 != 0) {
        if (org.apache.commons.lang3.StringUtils.isBlank(applyPackageNum)) {
            return Response.error("申请包装数量不能为空，且为小数、负数和0");
        }
        //校验审核数量
        Integer checkQuantity = wholesaleReturnDetail.getCheckQuantity();
        if (ObjectUtil.isEmpty(checkQuantity) && checkQuantity > applyQuantity) {
            return Response.error("审核数量不能大于申请数量");
        }
        //处理退货单价
        BigDecimal returnsPrice = ObjectUtil.isNull(wholesaleReturnDetail.getReturnsPrice()) ? goodsInfo.getSalePrice() : wholesaleReturnDetail.getReturnsPrice();
        wholesaleReturnDetail.setReturnsPrice(returnsPrice.setScale(com.edc.erp.common.util.NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //计算申请金额
        if (ObjectUtil.isNotEmpty(applyQuantity)) {
            BigDecimal applyAmount = returnsPrice.multiply(new BigDecimal(applyQuantity)).setScale(com.edc.erp.common.util.NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setApplyAmount(applyAmount);
        }
        //获取批发出货 库存价
        BigDecimal inventoryPrice = Objects.nonNull(shipmentDetailIn) ? shipmentDetailIn.getInventoryPrice() : null;
        //审核状态和待审核(有值) 不计算税额
        if (StringUtils.isEmpty(status)) {
            getObjectResponse(wholesaleReturnDetail, goodsInfo, inventoryPrice);
        }
        // 退货原则
        wholesaleReturnDetail.setReturnPrinciple(goodsInfo.getReturnPrinciple());
        wholesaleReturnDetail.setInvoiceType(goodsInfo.getInvoiceType());
        wholesaleReturnDetail.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(wholesaleReturnDetail.getInvoiceType()));
        wholesaleReturnDetail.setVendorCode(goodsInfo.getVendorCode());
        return Response.data(wholesaleReturnDetail);
    }

    /**
     * 计算申请金额等信息
     *
     * @param wholesaleReturnDetail
     * @param goodsInfo
     * @param inventoryPrice
     * @return
     */
    @Override
    public void getObjectResponse(WholesaleReturnDetail wholesaleReturnDetail, SaleGoodsInfoOut goodsInfo, BigDecimal inventoryPrice) {
        //入库数量
        Integer storageQuantity;
        //成本金额计算值 关联出货单 则为库存价 不关联则取退货单价
        BigDecimal costPrice;
        //出货单库存价为空 则说明当前退货单未关联出货单 则取最新库存价 否则取出货单库存价
        if (Objects.isNull(inventoryPrice)) {
            //取最新库存价
            inventoryPrice = Objects.nonNull(goodsInfo) ? goodsInfo.getInventoryPrice() : BigDecimal.ZERO;
            //不关联出货单 则取入库数量
            storageQuantity = wholesaleReturnDetail.getStorageQuantity();
            //退货单价 不关联出货单 则实际入库金额和成本金额 = 退货单价 * 入库数量
            costPrice = wholesaleReturnDetail.getReturnsPrice();
        } else {
            //存在 说明关联出货单 则取申请数量   实际入库金额 = 退货单价 * 申请数量
            storageQuantity = wholesaleReturnDetail.getApplyQuantity();
            //库存价-原出货单库存价  则成本金额 = 原出货单库存价 * 申请数量
            costPrice = inventoryPrice;
        }
        //更新仓储库存价
        if (Objects.nonNull(inventoryPrice)) {
            wholesaleReturnDetail.setInventoryPrice(inventoryPrice);
        }

        //实际入库金额
        BigDecimal practicalStorageAmount = wholesaleReturnDetail.getPracticalStorageAmount();
        if (ObjectUtil.isNotEmpty(storageQuantity) && ObjectUtil.isNotEmpty(wholesaleReturnDetail.getReturnsPrice())) {
            practicalStorageAmount = wholesaleReturnDetail.getReturnsPrice().multiply(new BigDecimal(storageQuantity)).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP);
            wholesaleReturnDetail.setPracticalStorageAmount(practicalStorageAmount);
        }
        //获取销项税率
        BigDecimal outTax = Objects.nonNull(goodsInfo) ? goodsInfo.getOutTax() : BigDecimal.ZERO;
        //计算入库去税金额=入库金额/（1+税率）
        if (ObjectUtil.isNotEmpty(practicalStorageAmount) && !practicalStorageAmount.equals(BigDecimal.ZERO)) {
            BigDecimal storageNetProfit = practicalStorageAmount.divide(BigDecimal.ONE.add(outTax), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setStorageNetProfit(storageNetProfit);
        }
        //计算入库税额=实际入库金额 - 入库去税金额
        if (ObjectUtil.isNotEmpty(practicalStorageAmount) && !practicalStorageAmount.equals(BigDecimal.ZERO)) {
            BigDecimal storageTax = practicalStorageAmount.subtract(wholesaleReturnDetail.getStorageNetProfit()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setStorageTax(storageTax);
        }
        //计算成本金额=仓储库存价*入库数量
        if (ObjectUtil.isNotEmpty(storageQuantity)) {
            BigDecimal costAmount = costPrice.multiply(new BigDecimal(storageQuantity)).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setCostAmount(costAmount);
        }
        //计算成本去税金额=成本金额/（1+税率）
        BigDecimal costAmount = wholesaleReturnDetail.getCostAmount();
        if (ObjectUtil.isNotEmpty(costAmount) && !costAmount.equals(BigDecimal.ZERO)) {
            BigDecimal costNetProfitAmount = costAmount.divide(BigDecimal.ONE.add(outTax), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setCostNetProfitAmount(costNetProfitAmount);
        }
        //计算成本税额=计算成本金额 - 成本去税金额
        BigDecimal costNetProfitAmount = wholesaleReturnDetail.getCostNetProfitAmount();
        if (ObjectUtil.isNotEmpty(costNetProfitAmount) && !costNetProfitAmount.equals(BigDecimal.ZERO)) {
            BigDecimal costTax = wholesaleReturnDetail.getCostAmount().subtract(costNetProfitAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setCostTax(costTax);
        }
    }

    @Override
    public void computeWholesaleReturnTaxByDTSBack(WholesaleReturnDetail wholesaleReturnDetail, SaleGoodsInfoOut goodsInfo, BigDecimal inventoryPrice) {
        //入库数量
        Integer storageQuantity;
        //成本金额计算值 关联出货单 则为库存价 不关联则取退货单价
        BigDecimal costPrice;
        //出货单库存价为空 则说明当前退货单未关联出货单 则取最新库存价 否则取出货单库存价
        if (Objects.isNull(inventoryPrice)) {
            //取最新库存价
            inventoryPrice = Objects.nonNull(goodsInfo) ? goodsInfo.getInventoryPrice() : BigDecimal.ZERO;
            //不关联出货单 则取入库数量
            storageQuantity = wholesaleReturnDetail.getStorageQuantity();
            //退货单价 不关联出货单 则实际入库金额和成本金额 = 退货单价 * 入库数量
            costPrice = wholesaleReturnDetail.getReturnsPrice();
        } else {
            storageQuantity = wholesaleReturnDetail.getStorageQuantity();
            //库存价-原出货单库存价  则成本金额 = 原出货单库存价 * 申请数量
            costPrice = inventoryPrice;
        }
        //更新仓储库存价
        if (Objects.nonNull(inventoryPrice)) {
            wholesaleReturnDetail.setInventoryPrice(inventoryPrice);
        }

        //实际入库金额
        BigDecimal practicalStorageAmount = wholesaleReturnDetail.getPracticalStorageAmount();
        if (ObjectUtil.isNotEmpty(storageQuantity) && ObjectUtil.isNotEmpty(wholesaleReturnDetail.getReturnsPrice())) {
            practicalStorageAmount = wholesaleReturnDetail.getReturnsPrice().multiply(new BigDecimal(storageQuantity)).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP);
            wholesaleReturnDetail.setPracticalStorageAmount(practicalStorageAmount);
        }
        //获取销项税率
        BigDecimal outTax = Objects.nonNull(goodsInfo) ? goodsInfo.getOutTax() : BigDecimal.ZERO;
        //计算入库去税金额=入库金额/（1+税率）
        if (ObjectUtil.isNotEmpty(practicalStorageAmount) && !practicalStorageAmount.equals(BigDecimal.ZERO)) {
            BigDecimal storageNetProfit = practicalStorageAmount.divide(BigDecimal.ONE.add(outTax), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setStorageNetProfit(storageNetProfit);
        }
        //计算入库税额=实际入库金额 - 入库去税金额
        if (ObjectUtil.isNotEmpty(practicalStorageAmount) && !practicalStorageAmount.equals(BigDecimal.ZERO)) {
            BigDecimal storageTax = practicalStorageAmount.subtract(wholesaleReturnDetail.getStorageNetProfit()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setStorageTax(storageTax);
        }
        //计算成本金额=仓储库存价*入库数量
        if (ObjectUtil.isNotEmpty(storageQuantity)) {
            BigDecimal costAmount = costPrice.multiply(new BigDecimal(storageQuantity)).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setCostAmount(costAmount);
        }
        //计算成本去税金额=成本金额/（1+税率）
        BigDecimal costAmount = wholesaleReturnDetail.getCostAmount();
        if (ObjectUtil.isNotEmpty(costAmount) && !costAmount.equals(BigDecimal.ZERO)) {
            BigDecimal costNetProfitAmount = costAmount.divide(BigDecimal.ONE.add(outTax), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setCostNetProfitAmount(costNetProfitAmount);
        }
        //计算成本税额=计算成本金额 - 成本去税金额
        BigDecimal costNetProfitAmount = wholesaleReturnDetail.getCostNetProfitAmount();
        if (ObjectUtil.isNotEmpty(costNetProfitAmount) && !costNetProfitAmount.equals(BigDecimal.ZERO)) {
            BigDecimal costTax = wholesaleReturnDetail.getCostAmount().subtract(costNetProfitAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setCostTax(costTax);
        }
    }
}
