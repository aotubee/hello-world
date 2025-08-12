package com.edc.erp.wholesale.handle;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OperateLogTypeEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.InvBizRsnTransOut;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.service.EquipmentBusinessReasonServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.ReversalEnum;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.wholesale.model.in.ApiWholesaleDetailIn;
import com.edc.erp.wholesale.model.in.ApiWholesaleOrderIn;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.model.out.ReadyHandleWholesaleApiOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.returns.service.WholesaleReturnDetailService;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.model.order.in.WholesaleReBillDtlIn;
import com.edc.sdk.dts.model.order.in.WholesaleReBillIn;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @ClassName ApiWholesaleReturnsHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/11 9:01
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiWholesaleReturnsHandle {
    private final WholesaleReturnsService wholesaleReturnsService;
    private final WholesaleReturnDetailService wholesaleReturnDetailService;
    private final StockServer stockServer;
    private final AsyncLogService asyncLogService;
    private final EquipmentBusinessReasonServer equipmentBusinessReasonServer;
    private final AsyncPushTaskService asyncPushTaskService;


    @Transactional(rollbackFor = Exception.class)
    public Long createWholesaleReturns(ReadyHandleWholesaleApiOut readyHandleWholesaleApiOut) {
        ApiWholesaleOrderIn apiWholesaleOrderIn = readyHandleWholesaleApiOut.getApiWholesaleOrderIn();
        List<ApiWholesaleDetailIn> detailInList = apiWholesaleOrderIn.getDetailList();
//        String warehouseCode = apiWholesaleOrderIn.getWarehouseCode();
        String bizOrgCode = apiWholesaleOrderIn.getBizOrgCode();
//        String clientCode = apiWholesaleOrderIn.getClientCode();
        String sourceNo = apiWholesaleOrderIn.getSourceNo();
        Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap = readyHandleWholesaleApiOut.getSaleGoodsInfoOutMap();

        AtomicReference<Integer> totalApplyQuantity = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<BigDecimal> totalApplyAmount = new AtomicReference<>(BigDecimal.ZERO);
        // 下发DTS明细
//        List<WholesaleReBillDtlIn> wholesaleReBillDtlInList = Lists.newArrayList();
        // 封装批发退明细
        List<WholesaleReturnDetail> wholesaleReturnDetailList = detailInList.stream().map(apiWholesaleDetailIn -> {
            SaleGoodsInfoOut goodsInfo = saleGoodsInfoOutMap.get(apiWholesaleDetailIn.getGoodsCode());
            if (Objects.isNull(goodsInfo)) {
                log.error("三方单据{}创建批发退查询商品{}状态不允许做批发退业务", sourceNo, apiWholesaleDetailIn.getGoodsCode());
                throw new BusinessException(apiWholesaleDetailIn.getGoodsCode() + ":商品状态不允许做批发退业务");
            }
            //校验申请数量和申请包装数
            Integer applyQuantity = apiWholesaleDetailIn.getApplyQuantity();
            if (ObjectUtil.isEmpty(applyQuantity) || applyQuantity <= 0 || applyQuantity % 1 != 0) {
                log.error("三方单据{}创建批发退查询商品{}数量必须为正整数", sourceNo, apiWholesaleDetailIn.getGoodsCode());
                throw new BusinessException(apiWholesaleDetailIn.getGoodsCode() + ":商品数量必须为正整数");
            }
            //根据退货单关联的出货单单号查询出货单
            Map<String, WholesaleShipmentDetailIn> shipmentMap = wholesaleReturnsService.getShipmentByWholesaleShipmentNo(readyHandleWholesaleApiOut.getOldWholesaleShipmentNo(), bizOrgCode);
            WholesaleShipmentDetailIn shipmentDetailIn = shipmentMap.get(apiWholesaleDetailIn.getGoodsCode());
//            if (Objects.isNull(shipmentDetailIn)) {
//                log.error("中科{}创建批发退商品{}未在原批发出货单{}中存在", sourceNo, zkWholesaleReturnDetailIn.getGoodsCode(), zkWholesaleReturnIn.getWholesaleShipmentNo());
//                throw new BusinessException(zkWholesaleReturnDetailIn.getGoodsCode() + ":原批发出货单未见此商品");
//            }
            //获取批发出 出库数量
//            Integer shipmentQuantity = Objects.nonNull(shipmentDetailIn) ? shipmentDetailIn.getShipmentQuantity() : null;
            //如果存在出货单校验申请数量不能大于出库数量
//            if (Objects.nonNull(shipmentQuantity) && NumberUtil.INTEGER_ZERO > shipmentQuantity.compareTo(applyQuantity)) {
//                log.error("中科{}创建批发退商品{}退货数量{}不能大于批发出货单出库数量{}", zkWholesaleReturnIn.getSourceNo(), zkWholesaleReturnDetailIn.getGoodsCode(), applyQuantity, shipmentQuantity);
//                throw new BusinessException("商品" + zkWholesaleReturnDetailIn.getGoodsCode() + "退货数量" + zkWholesaleReturnDetailIn.getApplyQuantity() + "不能大于批发出货单出库数量" + shipmentQuantity);
//            }
            WholesaleReturnDetail wholesaleReturnDetail = new WholesaleReturnDetail();
            BeanUtils.copy(apiWholesaleDetailIn, wholesaleReturnDetail);
            wholesaleReturnDetail.setGoodsName(goodsInfo.getGoodsName());
            wholesaleReturnDetail.setGoodsType(goodsInfo.getGoodsType());
            wholesaleReturnDetail.setBarCode(goodsInfo.getBarCode());
            wholesaleReturnDetail.setPackageSpecification(goodsInfo.getPackageSpecification());
            wholesaleReturnDetail.setPackageUnit(goodsInfo.getPackageUnit());
            wholesaleReturnDetail.setInvoiceType(goodsInfo.getInvoiceType());
            wholesaleReturnDetail.setVendorCode(goodsInfo.getVendorCode());
            Integer applyPackageNum = apiWholesaleDetailIn.getApplyQuantity() / goodsInfo.getQpc();
            wholesaleReturnDetail.setApplyPackageNum(applyPackageNum.toString());
            //处理退货单价
            BigDecimal returnsPrice = apiWholesaleDetailIn.getUnitPrice();
            wholesaleReturnDetail.setReturnsPrice(returnsPrice.setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //计算申请金额
            if (ObjectUtil.isNotEmpty(applyQuantity)) {
                BigDecimal applyAmount = returnsPrice.multiply(new BigDecimal(applyQuantity)).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
                wholesaleReturnDetail.setApplyAmount(applyAmount);
            }
            //获取批发出货 库存价
            BigDecimal inventoryPrice = Objects.nonNull(shipmentDetailIn) ? shipmentDetailIn.getInventoryPrice() : null;
            wholesaleReturnDetail.setInventoryPrice(Objects.isNull(inventoryPrice) ? wholesaleReturnDetail.getReturnsPrice() : inventoryPrice);
            // 审核数x
            wholesaleReturnDetail.setCheckQuantity(wholesaleReturnDetail.getApplyQuantity());
//            //审核状态和待审核(有值) 不计算税额
//            if (StringUtils.isEmpty(status)) {
//                getObjectResponse(wholesaleReturnDetail, goodsInfo, inventoryPrice);
//            }
            wholesaleReturnDetail.setCreator(apiWholesaleOrderIn.getCreator());
            wholesaleReturnDetail.setUpdater(apiWholesaleOrderIn.getCreator());
            wholesaleReturnDetail.setCreateTime(apiWholesaleOrderIn.getCreateTime());
            wholesaleReturnDetail.setUpdateTime(apiWholesaleOrderIn.getCreateTime());
            wholesaleReturnDetail.setIsDelete(ModelConst.DELETE.NO);
            totalApplyQuantity.set(totalApplyQuantity.get() + wholesaleReturnDetail.getApplyQuantity());
            totalApplyAmount.getAndSet(totalApplyAmount.get().add(wholesaleReturnDetail.getApplyAmount()));
//            WholesaleReBillDtlIn wholesaleReBillDtlIn = this.initWholesaleReBillDtlIn(wholesaleReturnNo, wholesaleReturnDetail, bizOrgCode, goodsInfo);
//            wholesaleReBillDtlInList.add(wholesaleReBillDtlIn);
            return wholesaleReturnDetail;
        }).collect(Collectors.toList());
        WholesaleReturns wholesaleReturns = this.initWholesaleReturns(apiWholesaleOrderIn, readyHandleWholesaleApiOut.getClientDistInfoOut(),
                totalApplyQuantity.get(), totalApplyAmount.get(), readyHandleWholesaleApiOut.getOldWholesaleShipmentNo());
        wholesaleReturnsService.insert(wholesaleReturns);
        wholesaleReturnDetailList.forEach(wholesaleReturnDetail -> wholesaleReturnDetail.setWholesaleReturnId(wholesaleReturns.getId()));
        wholesaleReturnDetailService.insertWholesaleReturnDetailList(wholesaleReturnDetailList);
        //保存日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                String.valueOf(wholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(),
                MessageFormat.format(OperateLogTypeEnum.ZK_WHOLESALE_RETURN_CREATE.getName(), sourceNo, wholesaleReturns.getWholesaleReturnNo()), new Date(), wholesaleReturns.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return wholesaleReturns.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditWholesaleReturns(Long shipmentReturnId, Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap, Integer clientId) {
        WholesaleReturns wholesaleReturns = wholesaleReturnsService.getOneById(shipmentReturnId);
        wholesaleReturns.setReturnStatus(ShipmentStatusEnum.APPROVED.getCode());
        wholesaleReturns.setUpdateTime(LocalDateTime.now());
        wholesaleReturns.setAuditTime(LocalDateTime.now());
        // 下发DTS明细
        List<WholesaleReBillDtlIn> wholesaleReBillDtlInList = Lists.newArrayList();
        List<WholesaleReturnDetail> detailList = wholesaleReturnDetailService.findListByWholesaleReturnId(shipmentReturnId);
        detailList.forEach(detail -> {
            // 审核数x
            detail.setCheckQuantity(detail.getApplyQuantity());
            detail.setUpdateTime(LocalDateTime.now());
            detail.setUpdater(wholesaleReturns.getCreator());
            SaleGoodsInfoOut goodsInfo = saleGoodsInfoOutMap.get(detail.getGoodsCode());
            WholesaleReBillDtlIn wholesaleReBillDtlIn = this.initWholesaleReBillDtlIn(wholesaleReturns.getWholesaleReturnNo(), detail, wholesaleReturns.getBizOrgCode(), goodsInfo);
            wholesaleReBillDtlInList.add(wholesaleReBillDtlIn);
        });
        wholesaleReturnsService.updateByPrimaryKeySelective(wholesaleReturns);
        wholesaleReturnDetailService.batchUpdateForAudit(detailList);
        //发送消息到DTS
        if (stockServer.isSendWms(wholesaleReturns.getStorageStockCode(), wholesaleReturns.getBizOrgCode())) {
            this.submitWholesaleReturnToDtsTask(clientId, wholesaleReturns, wholesaleReBillDtlInList);
        }
        //保存日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                String.valueOf(wholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(),
                OperateLogTypeEnum.APPROVED.getName(), new Date(), wholesaleReturns.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    private WholesaleReturns initWholesaleReturns(ApiWholesaleOrderIn apiWholesaleOrderIn, ClientDistInfoOut clientDistInfo,
                                                  Integer totalApplyQuantity, BigDecimal totalApplyAmount, String wholesaleShipmentNo) {
        WholesaleReturns wholesaleReturns = new WholesaleReturns();
        BeanUtils.copy(apiWholesaleOrderIn, wholesaleReturns);
        wholesaleReturns.setWholesaleReturnNo(apiWholesaleOrderIn.getErpOrderNo());
        wholesaleReturns.setStorageWrh(apiWholesaleOrderIn.getWarehouseCode());
        wholesaleReturns.setReturnStatus(ShipmentStatusEnum.PENDING.getCode());
        wholesaleReturns.setPriceGroupCode(clientDistInfo.getPriceGroupCode());
        wholesaleReturns.setApplicationQuantity(totalApplyQuantity);
        wholesaleReturns.setApplicationAmount(totalApplyAmount);
        wholesaleReturns.setDistributionInfoId(Objects.isNull(clientDistInfo.getId()) ? null : clientDistInfo.getId().intValue());
        wholesaleReturns.setAddressDetail(clientDistInfo.getAddressDetail());
        wholesaleReturns.setWholesaleShipmentNo(wholesaleShipmentNo);
        wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
        wholesaleReturns.setIsReversal(ReversalEnum.IS_REVERSAL_FALSE.getCode());
        wholesaleReturns.setIsReversalOrder(ReversalEnum.IS_REVERSAL_ORDER_FALSE.getCode());
        wholesaleReturns.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(wholesaleReturns.getBizOrgCode()));
        wholesaleReturns.setUpdater(apiWholesaleOrderIn.getCreator());
        wholesaleReturns.setCreateTime(apiWholesaleOrderIn.getCreateTime());
        wholesaleReturns.setUpdateTime(apiWholesaleOrderIn.getCreateTime());
//        wholesaleReturns.setAuditTime(LocalDateTime.now());
        wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleReturns;
    }

    private WholesaleReBillDtlIn initWholesaleReBillDtlIn(String wholesaleReturnNo, WholesaleReturnDetail wholesaleReturnDetail,
                                                          String bizOrgCode, SaleGoodsInfoOut goodsInfo) {
        WholesaleReBillDtlIn wholesaleReBillDtlIn = new WholesaleReBillDtlIn();
        //设置单号
        wholesaleReBillDtlIn.setPlatform_bill_id(wholesaleReturnNo);
        //设置行号
        wholesaleReBillDtlIn.setLine(wholesaleReturnDetail.getLine());
        //设置组织商品id(查询数据库获取组织商品信息)
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(wholesaleReturnDetail.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        wholesaleReBillDtlIn.setSku_id(String.valueOf(goodsInfo.getOrgGoodsId()));
        //设置组织商品代码
        wholesaleReBillDtlIn.setSku_code(wholesaleReturnDetail.getGoodsCode());
        //设置数量(数据类型怎么为BigDecimal)
        wholesaleReBillDtlIn.setQuantity(BigDecimal.valueOf(wholesaleReturnDetail.getCheckQuantity()));
        //设置价格
        wholesaleReBillDtlIn.setPrice_i(wholesaleReturnDetail.getReturnsPrice());
        //设置来源组织
        wholesaleReBillDtlIn.setSource_organization(bizOrgCode);
        //设置目标组织
        wholesaleReBillDtlIn.setTarget_organization(bizOrgCode);
        return wholesaleReBillDtlIn;
    }

    private void submitWholesaleReturnToDtsTask(Integer clientId, WholesaleReturns wholesaleReturns, List<WholesaleReBillDtlIn> wholesaleReBillDtlInList) {
        //批发退货单DTS入参转换
        WholesaleReBillIn wholesaleReBillIn = new WholesaleReBillIn();
        //设置退货单号
        wholesaleReBillIn.setPlatform_bill_id(wholesaleReturns.getWholesaleReturnNo());
        //设置退货时间
        wholesaleReBillIn.setBill_create_date(LocalDate.from(wholesaleReturns.getCreateTime()));
        //设置客户id(需要远程调用)
        wholesaleReBillIn.setCustomer_id(clientId.toString());
        //设置客户代码
        wholesaleReBillIn.setCustomer_code(wholesaleReturns.getClientCode());
        //设置仓储代码
        wholesaleReBillIn.setWarehouse_id(wholesaleReturns.getStorageWrh());
        //设置退货仓位代码
        wholesaleReBillIn.setSource_stock_id(wholesaleReturns.getStorageStockCode());
        //设置退货地址
        // 收货人|电话|收货地址|remark，
        String consignee = this.replaceStr(wholesaleReturns.getConsignee());
        String consigneePhone = this.replaceStr(wholesaleReturns.getConsigneePhone());
        String addressDetail = this.replaceStr(wholesaleReturns.getAddressDetail());
        String address_i = consignee + SystemConstant.VERTICAL_BAR + consigneePhone + SystemConstant.VERTICAL_BAR + addressDetail;
        wholesaleReBillIn.setAddress_i(address_i);
        //设置填单人
        wholesaleReBillIn.setCreater(wholesaleReturns.getCreator());
        //设置生成时间(审核时间)更新时间为空时，将创建时间设置为审核时间
        wholesaleReBillIn.setGenerate_time(ObjectUtil.isEmpty(wholesaleReturns.getUpdateTime()) ? wholesaleReturns.getCreateTime() : wholesaleReturns.getUpdateTime());
        //设置备注
        InvBizRsnTransOut invBizRsnTransOut = equipmentBusinessReasonServer.getWarehouseBizRsnTransByCode(wholesaleReturns.getReturnsReason(),
                wholesaleReturns.getBizOrgCode(), NumberUtil.INTEGER_ZERO);
        wholesaleReBillIn.setMemo(Objects.isNull(invBizRsnTransOut) ? wholesaleReturns.getRemark() : wholesaleReturns.getRemark() + SystemConstant.SHORT_LINE + invBizRsnTransOut.getBusinessReasonName());
        //设置来源单号
        if (StringUtils.isNotBlank(wholesaleReturns.getSourceNo())) {
            wholesaleReBillIn.setFrom_num(wholesaleReturns.getSourceNo());
        } else if (StringUtils.isNotBlank(wholesaleReturns.getWholesaleShipmentNo())) {
            wholesaleReBillIn.setFrom_num(wholesaleReturns.getWholesaleShipmentNo());
        } else {
            log.info("批发退{}下发DTS来源单号为空", wholesaleReturns.getWholesaleReturnNo());
        }
        //设置来源组织
        wholesaleReBillIn.setSource_organization(wholesaleReturns.getBizOrgCode());
        //设置目标组织(目标组织设置为来源组织,因为流转发生于当前组织)
        wholesaleReBillIn.setTarget_organization(wholesaleReBillIn.getSource_organization());
        //设置详情
        wholesaleReBillIn.setDetail_list(wholesaleReBillDtlInList);
        //存储消息
        asyncPushTaskService.submit(AsyncTaskConstant.Type.WHOLESALE_RETURN_TO_DTS, JSONObject.toJSONString(wholesaleReBillIn),
                wholesaleReturns.getBizOrgCode(), wholesaleReturns.getWholesaleReturnNo());
    }

    private String replaceStr(String str) {
        return StringUtils.isBlank(str) ? "" : str.replace(SystemConstant.VERTICAL_BAR, "");
    }

}
