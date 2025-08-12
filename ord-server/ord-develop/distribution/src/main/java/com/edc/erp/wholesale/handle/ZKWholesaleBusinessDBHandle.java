package com.edc.erp.wholesale.handle;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.OperateLogTypeEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.InvBizRsnTransOut;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.model.out.stock.StockWarehouseOut;
import com.edc.erp.common.service.EquipmentBusinessReasonServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.model.in.zk.*;
import com.edc.erp.enumeration.ReversalEnum;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.wholesale.enumeration.PushPurProgressEnum;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.returns.service.WholesaleReturnDetailService;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.enumeration.DtsOrderPatternTypeEnum;
import com.edc.sdk.dts.model.order.in.WholesaleBillDtlIn;
import com.edc.sdk.dts.model.order.in.WholesaleBillIn;
import com.edc.sdk.dts.model.order.in.WholesaleReBillDtlIn;
import com.edc.sdk.dts.model.order.in.WholesaleReBillIn;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * @ClassName ZKWholesaleBusinessDBHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/7 18:34
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class ZKWholesaleBusinessDBHandle {

    private final WarehouseServer warehouseServer;

    private final WholesaleShipmentService wholesaleShipmentService;

    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;

    private final StockFlowService stockFlowService;

    private final AsyncPushTaskService asyncPushTaskService;

    private final StockServer stockServer;

    private final AsyncLogService asyncLogService;

    private final WholesaleReturnsService wholesaleReturnsService;

    private final WholesaleReturnDetailService wholesaleReturnDetailService;

    private final EquipmentBusinessReasonServer equipmentBusinessReasonServer;

    @Qualifier("wholesaleShipmentToDtsSender")
    private final MessageSender wholesaleShipmentToDtsSender;

    @Qualifier("wholesaleReturnToDtsSender")
    private final MessageSender wholesaleReturnToDtsSender;

    @Qualifier("zKWholesaleShipmentBackSender")
    private final MessageSender zKWholesaleShipmentBackSender;

    @Transactional(rollbackFor = Exception.class)
    public void saveZKWholesaleShipment(TaskZKWholesaleShipmentSaveIn taskZKWholesaleShipmentSaveIn, Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap,
                                        ClientDistInfoOut clientDistInfo) {
        String bizOrgCode = taskZKWholesaleShipmentSaveIn.getBizOrgCode();
        ZKWholesaleShipmentIn zkWholesaleShipmentIn = taskZKWholesaleShipmentSaveIn.getZkWholesaleShipmentIn();
        List<ZKWholesaleShipmentDetailIn> zkWholesaleShipmentDetailInList = taskZKWholesaleShipmentSaveIn.getZkWholesaleShipmentDetailInList();
        List<String> goodsCodeList = zkWholesaleShipmentDetailInList.stream().map(ZKWholesaleShipmentDetailIn::getGoodsCode).collect(Collectors.toList());
        // 查询库存
        Map<String, StockWarehouseOut> stockInvMap = warehouseServer.findStockInv(zkWholesaleShipmentIn.getShipmentStockCode(), goodsCodeList, bizOrgCode);
        AtomicReference<Integer> totalApplyQuantity = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<BigDecimal> totalApplyAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<Integer> totalAuditQuantity = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<BigDecimal> totalAuditAmount = new AtomicReference<>(BigDecimal.ZERO);
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailInList = Lists.newArrayList();
        List<WholesaleBillDtlIn> wholesaleBillDtlInList = Lists.newArrayList();
        //封装出货单详情
        List<WholesaleShipmentDetail> wholesaleShipmentDetailList = zkWholesaleShipmentDetailInList.stream().map(zkWholesaleShipmentDetailIn -> {
            //调用rpc获取商品信息以及包装规格、批发价、库存价
            SaleGoodsInfoOut saleGoodsInfo = saleGoodsInfoOutMap.get(zkWholesaleShipmentDetailIn.getGoodsCode());
            if (Objects.isNull(saleGoodsInfo)) {
                log.error("中科{}创建批发出查询商品{}不在批发价格组中", zkWholesaleShipmentIn.getSourceNo(), zkWholesaleShipmentDetailIn.getGoodsCode());
                throw new BusinessException(zkWholesaleShipmentDetailIn.getGoodsCode() + ":商品不在批发价格组中");
            }
            if (NumberUtil.INTEGER_ZERO.equals(saleGoodsInfo.getIsExecute())) {
                log.error("中科{}创建批发出查询商品{}状态不允许做批发出货业务", zkWholesaleShipmentIn.getSourceNo(), zkWholesaleShipmentDetailIn.getGoodsCode());
                throw new BusinessException(zkWholesaleShipmentDetailIn.getGoodsCode() + ":商品状态不允许做批发出货业务");
            }
            WholesaleShipmentDetail wholesaleShipmentDetail = new WholesaleShipmentDetailIn();
            BeanUtils.copy(zkWholesaleShipmentDetailIn, wholesaleShipmentDetail);
            wholesaleShipmentDetail.setGoodsName(saleGoodsInfo.getGoodsName());
            wholesaleShipmentDetail.setGoodsType(saleGoodsInfo.getGoodsType());
            wholesaleShipmentDetail.setBarCode(saleGoodsInfo.getBarCode());
            wholesaleShipmentDetail.setPackageSpecification(saleGoodsInfo.getPackageSpecification());
            wholesaleShipmentDetail.setPackageUnit(saleGoodsInfo.getPackageUnit());
            wholesaleShipmentDetail.setVendorCode(saleGoodsInfo.getVendorCode());
            wholesaleShipmentDetail.setInvoiceType(saleGoodsInfo.getInvoiceType());
            //计算申请包装数
            String applyPackageNum = this.getPackageNum(saleGoodsInfo.getPackageSpecification(), wholesaleShipmentDetail.getApplyQuantity(), saleGoodsInfo.getQpc());
            wholesaleShipmentDetail.setApplyPackageNum(applyPackageNum);
            wholesaleShipmentDetail.setApplyAmount(wholesaleShipmentDetail.getUnitPrice().multiply(new BigDecimal(wholesaleShipmentDetail.getApplyQuantity())));
            // 计算审核数
            BigDecimal auditQuantity;
            if (DistributionWaysEnum.TRANSFER.getType().equals(zkWholesaleShipmentIn.getDistributionWay())) {
                auditQuantity = new BigDecimal(wholesaleShipmentDetail.getApplyQuantity());
                log.info("中科批发单{}商品{}申请数{}-----库存数{},计算审核数{}", zkWholesaleShipmentIn.getSourceNo(),
                        zkWholesaleShipmentDetailIn.getGoodsCode(), zkWholesaleShipmentDetailIn.getApplyQuantity(), "-", auditQuantity);
            } else {
                StockWarehouseOut stockWarehouseOut = stockInvMap.get(zkWholesaleShipmentDetailIn.getGoodsCode());
                if (Objects.isNull(stockWarehouseOut)) {
                    log.error("中科{}创建批发出查询商品{}仓储仓位{}无库存", zkWholesaleShipmentIn.getSourceNo(), zkWholesaleShipmentDetailIn.getGoodsCode(), zkWholesaleShipmentIn.getShipmentStockCode());
                    throw new BusinessException("中科" + zkWholesaleShipmentIn.getSourceNo() + "创建批发出查询商品" + zkWholesaleShipmentDetailIn.getGoodsCode() + "仓储仓位" + zkWholesaleShipmentIn.getShipmentStockCode() + "无库存");
                }
                BigDecimal stockQty = stockWarehouseOut.getBusinessQty();
                if (Objects.isNull(stockQty)) {
                    stockQty = BigDecimal.ZERO;
                }
                // 计算审核数
                auditQuantity = stockQty.compareTo(new BigDecimal(wholesaleShipmentDetail.getApplyQuantity())) >= NumberUtil.INTEGER_ZERO
                        ? new BigDecimal(wholesaleShipmentDetail.getApplyQuantity()) : stockQty;
                log.info("中科批发单{}商品{}申请数{}-----库存数{},计算审核数{}", zkWholesaleShipmentIn.getSourceNo(),
                        zkWholesaleShipmentDetailIn.getGoodsCode(), zkWholesaleShipmentDetailIn.getApplyQuantity(), stockQty, auditQuantity);
            }
            wholesaleShipmentDetail.setAuditQuantity(auditQuantity.intValue());
            wholesaleShipmentDetail.setAuditAmount(wholesaleShipmentDetail.getUnitPrice().multiply(new BigDecimal(wholesaleShipmentDetail.getAuditQuantity())));
            wholesaleShipmentDetail.setVendorCode(saleGoodsInfo.getVendorCode());
            wholesaleShipmentDetail.setCreator(zkWholesaleShipmentIn.getCreator());
            wholesaleShipmentDetail.setCreateTime(zkWholesaleShipmentIn.getCreateTime());
            wholesaleShipmentDetail.setUpdater(zkWholesaleShipmentIn.getCreator());
            wholesaleShipmentDetail.setUpdateTime(zkWholesaleShipmentIn.getCreateTime());
            wholesaleShipmentDetail.setIsDelete(ModelConst.DELETE.NO);
            totalApplyQuantity.set(totalApplyQuantity.get() + wholesaleShipmentDetail.getApplyQuantity());
            totalApplyAmount.getAndSet(totalApplyAmount.get().add(wholesaleShipmentDetail.getApplyAmount()));
            totalAuditQuantity.set(totalAuditQuantity.get() + wholesaleShipmentDetail.getAuditQuantity());
            totalAuditAmount.getAndSet(totalAuditAmount.get().add(wholesaleShipmentDetail.getAuditAmount()));
            WholesaleShipmentDetailIn wholesaleShipmentDetailIn = new WholesaleShipmentDetailIn();
            BeanUtils.copy(wholesaleShipmentDetail, wholesaleShipmentDetailIn);
            wholesaleShipmentDetailInList.add(wholesaleShipmentDetailIn);
            if (auditQuantity.compareTo(BigDecimal.ZERO) > NumberUtil.INTEGER_ZERO) {
                //出货单下发DTS 明细入参
                WholesaleBillDtlIn wholesaleBillDtlIn = this.initWholesaleBillDtlIn(taskZKWholesaleShipmentSaveIn.getWholesaleShipmentNo(), zkWholesaleShipmentDetailIn,
                        bizOrgCode, saleGoodsInfo.getOrgGoodsId(), auditQuantity, wholesaleShipmentDetail.getAuditAmount(),
                        zkWholesaleShipmentIn.getShipmentStockCode());
                wholesaleBillDtlInList.add(wholesaleBillDtlIn);
            } else {
                log.info("中科创建批发出{}明细{}审核数量为0，故不下发DTS", taskZKWholesaleShipmentSaveIn.getWholesaleShipmentNo(), zkWholesaleShipmentDetailIn.getGoodsCode());
            }
            return wholesaleShipmentDetail;
        }).collect(Collectors.toList());
        String status = totalAuditQuantity.get().compareTo(NumberUtil.INTEGER_ZERO) == NumberUtil.INTEGER_ZERO
                ? ShipmentStatusEnum.INVALID.getCode() : ShipmentStatusEnum.APPROVED.getCode();
        // 封装批发出货单
        WholesaleShipment wholesaleShipment = this.initWholesaleShipment(taskZKWholesaleShipmentSaveIn.getWholesaleShipmentNo(),
                taskZKWholesaleShipmentSaveIn.getShipmentWrh(), clientDistInfo, zkWholesaleShipmentIn, status, totalApplyQuantity.get(),
                totalApplyAmount.get(), totalAuditQuantity.get(), totalAuditAmount.get(), bizOrgCode);
        wholesaleShipmentService.insertSelective(wholesaleShipment);
        wholesaleShipmentDetailList.forEach(wholesaleShipmentDetail -> wholesaleShipmentDetail.setWholesaleShipmentId(wholesaleShipment.getId()));
        wholesaleShipmentDetailService.batchSaveList(wholesaleShipmentDetailList);
        if (ShipmentStatusEnum.APPROVED.getCode().equals(status)) {
            //调整库存
            List<StockFlowIn> stockFlowIns = wholesaleShipmentService.addOrSubStock(wholesaleShipment, wholesaleShipmentDetailInList, ShipmentStatusEnum.APPROVED.getCode());
            if (!DistributionWaysEnum.TRANSFER.getType().equals(zkWholesaleShipmentIn.getDistributionWay())) {
                //调用库存rpc调整库存
                Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
                if (!stockFlow.isSuccess()) {
                    throw new BusinessException("库存调整失败");
                }
            }
            //审核之后批发出货单下发DTS
            if (stockServer.isSendWms(zkWholesaleShipmentIn.getShipmentStockCode(), bizOrgCode)) {
                this.submitWholesaleShipmentToDtsTask(clientDistInfo.getClientId(), wholesaleShipment, wholesaleBillDtlInList);
            }
        } else {
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.ZK_WHOLESALE_SHIPMENT_BACK, JSONObject.toJSONString(wholesaleShipment),
//                    wholesaleShipment.getBizOrgCode(), wholesaleShipment.getShipmentNo());
            SendResponse sendResponse = zKWholesaleShipmentBackSender.sendSync(JSONObject.toJSONString(wholesaleShipment).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
            log.info("中科请求创建批发出货单回传中科{}消息ID---{}", wholesaleShipment.getShipmentNo(), sendResponse.getMessageId());
        }
        //保存出货单审核日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                String.valueOf(wholesaleShipment.getId()),
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                MessageFormat.format(OperateLogTypeEnum.ZK_WHOLESALE_SHIPMENT_CREATE.getName(), ShipmentStatusEnum.getNameByCode(status)),
                new Date(), wholesaleShipment.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }


    @Transactional(rollbackFor = Exception.class)
    public void saveZKWholesaleReturn(TaskZKWholesaleReturnSaveIn taskZKWholesaleReturnSaveIn, Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap,
                                      Map<String, WholesaleShipmentDetailIn> shipmentMap, ClientDistInfoOut clientDistInfo) {
        String wholesaleReturnNo = taskZKWholesaleReturnSaveIn.getWholesaleReturnNo();
        String bizOrgCode = taskZKWholesaleReturnSaveIn.getBizOrgCode();
        ZKWholesaleReturnIn zkWholesaleReturnIn = taskZKWholesaleReturnSaveIn.getZkWholesaleReturnIn();
        String wholesaleShipmentNo = zkWholesaleReturnIn.getWholesaleShipmentNo();
        AtomicReference<Integer> totalApplyQuantity = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<BigDecimal> totalApplyAmount = new AtomicReference<>(BigDecimal.ZERO);
        // 下发DTS明细
        List<WholesaleReBillDtlIn> wholesaleReBillDtlInList = Lists.newArrayList();
        // 封装批发退明细
        List<WholesaleReturnDetail> wholesaleReturnDetailList = taskZKWholesaleReturnSaveIn.getZkWholesaleReturnDetailInList().stream().map(zkWholesaleReturnDetailIn -> {
            SaleGoodsInfoOut goodsInfo = saleGoodsInfoOutMap.get(zkWholesaleReturnDetailIn.getGoodsCode());
            if (Objects.isNull(goodsInfo)) {
                log.error("中科{}创建批发退查询商品{}状态不允许做批发退业务", zkWholesaleReturnIn.getSourceNo(), zkWholesaleReturnDetailIn.getGoodsCode());
                throw new BusinessException(zkWholesaleReturnDetailIn.getGoodsCode() + ":商品状态不允许做批发退业务");
            }
            //校验申请数量和申请包装数
            Integer applyQuantity = zkWholesaleReturnDetailIn.getApplyQuantity();
            if (ObjectUtil.isEmpty(applyQuantity) || applyQuantity <= 0 || applyQuantity % 1 != 0) {
                log.error("中科{}创建批发退查询商品{}数量必须为正整数", zkWholesaleReturnIn.getSourceNo(), zkWholesaleReturnDetailIn.getGoodsCode());
                throw new BusinessException(zkWholesaleReturnDetailIn.getGoodsCode() + ":商品数量必须为正整数");
            }
            WholesaleShipmentDetailIn shipmentDetailIn = shipmentMap.get(zkWholesaleReturnDetailIn.getGoodsCode());
//            if (Objects.isNull(shipmentDetailIn)) {
//                log.error("中科{}创建批发退商品{}未在原批发出货单{}中存在", zkWholesaleReturnIn.getSourceNo(), zkWholesaleReturnDetailIn.getGoodsCode(), zkWholesaleReturnIn.getWholesaleShipmentNo());
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
            BeanUtils.copy(zkWholesaleReturnDetailIn, wholesaleReturnDetail);
            wholesaleReturnDetail.setGoodsName(goodsInfo.getGoodsName());
            wholesaleReturnDetail.setGoodsType(goodsInfo.getGoodsType());
            wholesaleReturnDetail.setBarCode(goodsInfo.getBarCode());
            wholesaleReturnDetail.setPackageSpecification(goodsInfo.getPackageSpecification());
            wholesaleReturnDetail.setPackageUnit(goodsInfo.getPackageUnit());
            wholesaleReturnDetail.setInvoiceType(goodsInfo.getInvoiceType());
            wholesaleReturnDetail.setVendorCode(goodsInfo.getVendorCode());
            Integer applyPackageNum = zkWholesaleReturnDetailIn.getApplyQuantity() / goodsInfo.getQpc();
            wholesaleReturnDetail.setApplyPackageNum(applyPackageNum.toString());
            //处理退货单价
            BigDecimal returnsPrice = zkWholesaleReturnDetailIn.getReturnsPrice();
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
            wholesaleReturnDetail.setCreator(zkWholesaleReturnIn.getCreator());
            wholesaleReturnDetail.setUpdater(zkWholesaleReturnIn.getCreator());
            wholesaleReturnDetail.setCreateTime(zkWholesaleReturnIn.getCreateTime());
            wholesaleReturnDetail.setUpdateTime(zkWholesaleReturnIn.getCreateTime());
            wholesaleReturnDetail.setIsDelete(ModelConst.DELETE.NO);
            totalApplyQuantity.set(totalApplyQuantity.get() + wholesaleReturnDetail.getApplyQuantity());
            totalApplyAmount.getAndSet(totalApplyAmount.get().add(wholesaleReturnDetail.getApplyAmount()));
            WholesaleReBillDtlIn wholesaleReBillDtlIn = this.initWholesaleReBillDtlIn(wholesaleReturnNo, wholesaleReturnDetail, bizOrgCode, goodsInfo);
            wholesaleReBillDtlInList.add(wholesaleReBillDtlIn);
            return wholesaleReturnDetail;
        }).collect(Collectors.toList());
        WholesaleReturns wholesaleReturns = this.initWholesaleReturns(zkWholesaleReturnIn, taskZKWholesaleReturnSaveIn.getStorageWrh(), wholesaleReturnNo, clientDistInfo,
                totalApplyQuantity.get(), totalApplyAmount.get(), wholesaleShipmentNo);
        wholesaleReturnsService.insert(wholesaleReturns);
        wholesaleReturnDetailList.forEach(wholesaleReturnDetail -> wholesaleReturnDetail.setWholesaleReturnId(wholesaleReturns.getId()));
        wholesaleReturnDetailService.insertWholesaleReturnDetailList(wholesaleReturnDetailList);
        //发送消息到DTS
        if (stockServer.isSendWms(zkWholesaleReturnIn.getStorageStockCode(), bizOrgCode)) {
            this.submitWholesaleReturnToDtsTask(clientDistInfo, wholesaleReturns, wholesaleReBillDtlInList);
        }
        //保存日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                String.valueOf(wholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(),
                OperateLogTypeEnum.ZK_WHOLESALE_RETURN_CREATE.getName(), new Date(), wholesaleReturns.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    private void submitWholesaleShipmentToDtsTask(Integer clientId, WholesaleShipment wholesaleShipment, List<WholesaleBillDtlIn> wholesaleBillDtlInList) {
        //批发出货单下发DTS入参
        WholesaleBillIn wholesaleBillIn = new WholesaleBillIn();
        //单号
        wholesaleBillIn.setPlatform_bill_id(wholesaleShipment.getShipmentNo());
        //批发日期
        wholesaleBillIn.setBill_create_date(wholesaleShipment.getCreateTime().toLocalDate());
        //客户ID
        wholesaleBillIn.setCustomer_id(clientId.toString());
        //客户代码
        wholesaleBillIn.setCustomer_code(wholesaleShipment.getClientCode());
        //仓储代码
        wholesaleBillIn.setWarehouse_id(wholesaleShipment.getShipmentWrh());
        //出货仓位
        wholesaleBillIn.setSource_stock_id(wholesaleShipment.getShipmentStockCode());
        //送货地址
//        wholesaleBillIn.setAddress_i(wholesaleShipment.getAddressDetail());
        // 收货人|电话|收货地址|remark，
        String consignee = wholesaleShipmentService.replaceStr(wholesaleShipment.getConsignee());
        String consigneePhone = wholesaleShipmentService.replaceStr(wholesaleShipment.getConsigneePhone());
        String addressDetail = wholesaleShipmentService.replaceStr(wholesaleShipment.getAddressDetail());
        String address_i = consignee + SystemConstant.VERTICAL_BAR + consigneePhone + SystemConstant.VERTICAL_BAR + addressDetail;
        wholesaleBillIn.setAddress_i(address_i);
        //优惠价
        wholesaleBillIn.setAmount_ii(wholesaleShipment.getAuditAmount());
        //总要货金额
        wholesaleBillIn.setAmount_i(wholesaleShipment.getAuditAmount());
        //订单模式
        wholesaleBillIn.setOrder_pattern(DtsOrderPatternTypeEnum.PURCHASE_RETURN.getCode());
        //填单人
        wholesaleBillIn.setCreater(wholesaleShipment.getCreator());
        //生成时间
        wholesaleBillIn.setGenerate_time(LocalDateTime.now());
        //明细数量
        wholesaleBillIn.setCount(wholesaleBillDtlInList.size());
        //未收金额
        wholesaleBillIn.setAmount_iii(wholesaleShipment.getAuditAmount());
        wholesaleBillIn.setDetail_list(wholesaleBillDtlInList);
        //来源组织
        wholesaleBillIn.setSource_organization(wholesaleShipment.getBizOrgCode());
        //目标组织
        wholesaleBillIn.setTarget_organization(wholesaleShipment.getBizOrgCode());
        wholesaleBillIn.setMemo(wholesaleShipment.getRemark());
        wholesaleBillIn.setFrom_num(wholesaleShipment.getSourceNo());
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_TO_DTS, JSONObject.toJSONString(wholesaleBillIn),
//                wholesaleShipment.getBizOrgCode(), wholesaleShipment.getShipmentNo());
        SendResponse sendResponse = wholesaleShipmentToDtsSender.sendSync(JSONObject.toJSONString(wholesaleBillIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("DTS批发出货单数据下发DTS{}消息ID---{}", wholesaleShipment.getShipmentNo(), sendResponse.getMessageId());
    }

    private WholesaleShipment initWholesaleShipment(String wholesaleShipmentNo, String shipmentWrh, ClientDistInfoOut clientDistInfo,
                                                    ZKWholesaleShipmentIn zkWholesaleShipmentIn, String status,
                                                    Integer totalApplyQuantity, BigDecimal totalApplyAmount, Integer totalAuditQuantity,
                                                    BigDecimal totalAuditAmount, String bizOrgCode) {
        WholesaleShipment wholesaleShipment = new WholesaleShipment();
        BeanUtils.copy(zkWholesaleShipmentIn, wholesaleShipment);
        wholesaleShipment.setShipmentNo(wholesaleShipmentNo);
        wholesaleShipment.setShipmentWrh(shipmentWrh);
        wholesaleShipment.setShipmentStatus(status);
        wholesaleShipment.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        wholesaleShipment.setIsReversal(NumberUtil.INTEGER_ZERO);
        wholesaleShipment.setApplicationQuantity(totalApplyQuantity);
        wholesaleShipment.setApplicationAmount(totalApplyAmount);
        wholesaleShipment.setAuditQuantity(totalAuditQuantity);
        wholesaleShipment.setAuditAmount(totalAuditAmount);
        wholesaleShipment.setDistributionInfoId(clientDistInfo.getId().intValue());
        String driverInfo = null;
        if (StringUtils.isNotBlank(zkWholesaleShipmentIn.getDriverName()) && StringUtils.isNotBlank(zkWholesaleShipmentIn.getDriverPhone())) {
            driverInfo = zkWholesaleShipmentIn.getDriverName() + SystemConstant.SHORT_LINE + zkWholesaleShipmentIn.getDriverPhone();
        }
        if (StringUtils.isBlank(zkWholesaleShipmentIn.getDriverName()) && StringUtils.isNotBlank(zkWholesaleShipmentIn.getDriverPhone())) {
            driverInfo = zkWholesaleShipmentIn.getDriverPhone();
        }
        if (StringUtils.isNotBlank(zkWholesaleShipmentIn.getDriverName()) && StringUtils.isBlank(zkWholesaleShipmentIn.getDriverPhone())) {
            driverInfo = zkWholesaleShipmentIn.getDriverName();
        }
        if (StringUtils.isNotBlank(driverInfo)) {
            wholesaleShipment.setDriverInfo(driverInfo);
        }
        wholesaleShipment.setPriceGroupCode(clientDistInfo.getPriceGroupCode());
        wholesaleShipment.setAddressDetail(clientDistInfo.getAddressDetail());
        wholesaleShipment.setBizOrgCode(bizOrgCode);
        wholesaleShipment.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(bizOrgCode));
        wholesaleShipment.setDistributionType(zkWholesaleShipmentIn.getDistributionWay());
        wholesaleShipment.setUpdater(zkWholesaleShipmentIn.getCreator());
        wholesaleShipment.setCreateTime(zkWholesaleShipmentIn.getCreateTime());
        wholesaleShipment.setUpdateTime(wholesaleShipment.getCreateTime());
        wholesaleShipment.setIsDelete(ModelConst.DELETE.NO);
        wholesaleShipment.setAuditTime(LocalDateTime.now());
        wholesaleShipment.setPushPurProgress(PushPurProgressEnum.PENDING.getProgress());
        return wholesaleShipment;
    }

    private WholesaleBillDtlIn initWholesaleBillDtlIn(String wholesaleShipmentNo, ZKWholesaleShipmentDetailIn zkWholesaleShipmentDetailIn,
                                                      String bizOrgCode, Long orgGoodsId, BigDecimal auditQuantity,
                                                      BigDecimal auditAmount, String shipmentStockCode) {
        WholesaleBillDtlIn wholesaleBillDtlIn = new WholesaleBillDtlIn();
        //单号
        wholesaleBillDtlIn.setPlatform_bill_id(wholesaleShipmentNo);
        //行号
        wholesaleBillDtlIn.setLine(zkWholesaleShipmentDetailIn.getLine());
        //组织商品id
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(zkWholesaleShipmentDetailIn.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        wholesaleBillDtlIn.setSku_id(String.valueOf(orgGoodsId));
        //商品代码
        wholesaleBillDtlIn.setSku_code(zkWholesaleShipmentDetailIn.getGoodsCode());
        //数量
        wholesaleBillDtlIn.setQuantity(auditQuantity);
        //价格
        wholesaleBillDtlIn.setPrice_i(zkWholesaleShipmentDetailIn.getUnitPrice());
        //批发价格
        wholesaleBillDtlIn.setPrice_ii(zkWholesaleShipmentDetailIn.getUnitPrice());
        //优惠金额
        wholesaleBillDtlIn.setDiscount_amount(auditAmount);
        //退货仓位代码
        wholesaleBillDtlIn.setSource_stock_id(shipmentStockCode);
        //来源组织
        wholesaleBillDtlIn.setSource_organization(bizOrgCode);
        //目标组织
        wholesaleBillDtlIn.setTarget_organization(bizOrgCode);
        return wholesaleBillDtlIn;
    }

    private String getPackageNum(String packageSpecification, Integer quantity, Integer qpc) {
        String applyPackageNum;
        if (Objects.isNull(packageSpecification)) {
            applyPackageNum = NumberUtil.INTEGER_ZERO.toString();
        } else {
            if (quantity % qpc != NumberUtil.INTEGER_ZERO) {
                applyPackageNum = quantity / qpc + "+" + quantity % qpc;
            } else {
                applyPackageNum = String.valueOf(quantity / qpc);
            }
        }
        return applyPackageNum;
    }

    private void submitWholesaleReturnToDtsTask(ClientDistInfoOut clientDistInfo, WholesaleReturns wholesaleReturns, List<WholesaleReBillDtlIn> wholesaleReBillDtlInList) {
        //批发退货单DTS入参转换
        WholesaleReBillIn wholesaleReBillIn = new WholesaleReBillIn();
        //设置退货单号
        wholesaleReBillIn.setPlatform_bill_id(wholesaleReturns.getWholesaleReturnNo());
        //设置退货时间
        wholesaleReBillIn.setBill_create_date(LocalDate.from(wholesaleReturns.getCreateTime()));
        //设置客户id(需要远程调用)
        wholesaleReBillIn.setCustomer_id(clientDistInfo.getClientId().toString());
        //设置客户代码
        wholesaleReBillIn.setCustomer_code(wholesaleReturns.getClientCode());
        //设置仓储代码
        wholesaleReBillIn.setWarehouse_id(wholesaleReturns.getStorageWrh());
        //设置退货仓位代码
        wholesaleReBillIn.setSource_stock_id(wholesaleReturns.getStorageStockCode());
        //设置退货地址
        // 收货人|电话|收货地址|remark，
        String consignee = wholesaleShipmentService.replaceStr(wholesaleReturns.getConsignee());
        String consigneePhone = wholesaleShipmentService.replaceStr(wholesaleReturns.getConsigneePhone());
        String addressDetail = wholesaleShipmentService.replaceStr(wholesaleReturns.getAddressDetail());
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
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.WHOLESALE_RETURN_TO_DTS, JSONObject.toJSONString(wholesaleReBillIn),
//                wholesaleReturns.getBizOrgCode(), wholesaleReturns.getWholesaleReturnNo());
        SendResponse sendResponse = wholesaleReturnToDtsSender.sendSync(JSONObject.toJSONString(wholesaleReBillIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("DTS批发退单数据下发DTS{}消息ID---{}", wholesaleReturns.getWholesaleReturnNo(), sendResponse.getMessageId());
    }

    private static WholesaleReturns initWholesaleReturns(ZKWholesaleReturnIn zkWholesaleReturnIn, String storageWrh, String wholesaleReturnNo,
                                                         ClientDistInfoOut clientDistInfo, Integer totalApplyQuantity,
                                                         BigDecimal totalApplyAmount, String wholesaleShipmentNo) {
        WholesaleReturns wholesaleReturns = new WholesaleReturns();
        BeanUtils.copy(zkWholesaleReturnIn, wholesaleReturns);
        wholesaleReturns.setWholesaleReturnNo(wholesaleReturnNo);
        wholesaleReturns.setStorageWrh(storageWrh);
        wholesaleReturns.setReturnStatus(ShipmentStatusEnum.APPROVED.getCode());
        wholesaleReturns.setPriceGroupCode(clientDistInfo.getPriceGroupCode());
        wholesaleReturns.setApplicationQuantity(totalApplyQuantity);
        wholesaleReturns.setApplicationAmount(totalApplyAmount);
        wholesaleReturns.setDistributionInfoId(clientDistInfo.getId().intValue());
        wholesaleReturns.setAddressDetail(clientDistInfo.getAddressDetail());
        wholesaleReturns.setWholesaleShipmentNo(wholesaleShipmentNo);
        wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
        wholesaleReturns.setIsReversal(ReversalEnum.IS_REVERSAL_FALSE.getCode());
        wholesaleReturns.setIsReversalOrder(ReversalEnum.IS_REVERSAL_ORDER_FALSE.getCode());
        wholesaleReturns.setBizOrgCode(clientDistInfo.getBizOrgCode());
        wholesaleReturns.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(wholesaleReturns.getBizOrgCode()));
        wholesaleReturns.setUpdater(zkWholesaleReturnIn.getCreator());
        wholesaleReturns.setCreateTime(zkWholesaleReturnIn.getCreateTime());
        wholesaleReturns.setUpdateTime(wholesaleReturns.getCreateTime());
        wholesaleReturns.setAuditTime(LocalDateTime.now());
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


}
