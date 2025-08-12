package com.edc.erp.wholesale.handle;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.OperateLogTypeEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.model.out.goods.StandardSpecOut;
import com.edc.erp.common.model.out.stock.StockWarehouseOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.wholesale.model.in.ApiWholesaleDetailIn;
import com.edc.erp.wholesale.model.in.ApiWholesaleOrderIn;
import com.edc.erp.wholesale.model.in.shipment.BackToHsBaseInfo;
import com.edc.erp.wholesale.model.in.shipment.HSWholesaleDifferenceOrder;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.model.out.ReadyHandleWholesaleApiOut;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.enumeration.DtsOrderPatternTypeEnum;
import com.edc.sdk.dts.model.order.in.WholesaleBillDtlIn;
import com.edc.sdk.dts.model.order.in.WholesaleBillIn;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @ClassName ApiWholesaleHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/10 10:45
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiWholesaleShipmentHandle {

    private final WarehouseServer warehouseServer;
    private final WholesaleShipmentService wholesaleShipmentService;
    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;
    private final StockFlowService stockFlowService;
    private final AsyncPushTaskService asyncPushTaskService;
    private final StockServer stockServer;
    private final AsyncLogService asyncLogService;
    @Autowired
    @Qualifier("hsWholesaleDiffSender")
    private MessageSender hsWholesaleDiffSender;
    @Autowired
    @Qualifier("hsWholesaleShipmentDtsBackSender")
    private MessageSender hsWholesaleShipmentDtsBackSender;


    @Transactional(rollbackFor = Exception.class)
    public Long createWholesaleShipment(ReadyHandleWholesaleApiOut readyHandleWholesaleApiOut) {
        ApiWholesaleOrderIn apiWholesaleOrderIn = readyHandleWholesaleApiOut.getApiWholesaleOrderIn();
        List<ApiWholesaleDetailIn> detailInList = apiWholesaleOrderIn.getDetailList();
        String warehouseCode = apiWholesaleOrderIn.getWarehouseCode();
        String bizOrgCode = apiWholesaleOrderIn.getBizOrgCode();
//        String clientCode = apiWholesaleOrderIn.getClientCode();
        String sourceNo = apiWholesaleOrderIn.getSourceNo();
        Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap = readyHandleWholesaleApiOut.getSaleGoodsInfoOutMap();
        AtomicReference<Integer> totalApplyQuantity = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<BigDecimal> totalApplyAmount = new AtomicReference<>(BigDecimal.ZERO);
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailInList = Lists.newArrayList();
        //封装出货单详情
        List<WholesaleShipmentDetail> wholesaleShipmentDetailList = detailInList.stream().map(apiWholesaleDetailIn -> {
            //调用rpc获取商品信息以及包装规格、批发价、库存价
            SaleGoodsInfoOut saleGoodsInfo = saleGoodsInfoOutMap.get(apiWholesaleDetailIn.getGoodsCode());
            if (Objects.isNull(saleGoodsInfo)) {
                log.error("三方单据{}创建批发出查询商品{}不在批发价格组中", sourceNo, apiWholesaleDetailIn.getGoodsCode());
                throw new BusinessException(apiWholesaleDetailIn.getGoodsCode() + ":商品不在批发价格组中");
            }
            if (NumberUtil.INTEGER_ZERO.equals(saleGoodsInfo.getIsExecute())) {
                log.error("三方单据{}创建批发出查询商品{}状态不允许做批发出货业务", sourceNo, apiWholesaleDetailIn.getGoodsCode());
                throw new BusinessException(apiWholesaleDetailIn.getGoodsCode() + ":商品状态不允许做批发出货业务");
            }
            WholesaleShipmentDetail wholesaleShipmentDetail = new WholesaleShipmentDetailIn();
            BeanUtils.copy(apiWholesaleDetailIn, wholesaleShipmentDetail);
            wholesaleShipmentDetail.setGoodsName(saleGoodsInfo.getGoodsName());
            wholesaleShipmentDetail.setGoodsType(saleGoodsInfo.getGoodsType());
            wholesaleShipmentDetail.setBarCode(saleGoodsInfo.getBarCode());
            wholesaleShipmentDetail.setPackageSpecification(saleGoodsInfo.getPackageSpecification());
            wholesaleShipmentDetail.setPackageUnit(saleGoodsInfo.getPackageUnit());
            wholesaleShipmentDetail.setVendorCode(saleGoodsInfo.getVendorCode());
            wholesaleShipmentDetail.setInvoiceType(saleGoodsInfo.getInvoiceType());
            //计算申请包装数
            String packageSpecification = null;
            Integer qpc = null;
            //  HS来源省烟草
            if (StringUtils.isNotBlank(sourceNo) && sourceNo.startsWith("HS")) {
                Optional<StandardSpecOut> optional = saleGoodsInfo.getStandardSpecs().stream().filter(sso -> sso.getQpcStr().equals("1*1")).findFirst();
                if (!optional.isPresent()) {
                    throw new BusinessException("来源单号" + sourceNo + "商品" + apiWholesaleDetailIn.getGoodsCode() + "没有1*1规格");
                }
                StandardSpecOut standardSpecOut = optional.get();
                packageSpecification = standardSpecOut.getQpcStr();
                qpc = standardSpecOut.getQpc();
            }
            // 来源中科
            if (StringUtils.isNotBlank(sourceNo) && sourceNo.startsWith("YH")) {
                packageSpecification = saleGoodsInfo.getPackageSpecification();
                qpc = saleGoodsInfo.getQpc();
            }
            String applyPackageNum = this.getPackageNum(packageSpecification, wholesaleShipmentDetail.getApplyQuantity(), qpc);
            wholesaleShipmentDetail.setApplyPackageNum(applyPackageNum);
            wholesaleShipmentDetail.setApplyAmount(wholesaleShipmentDetail.getUnitPrice().multiply(new BigDecimal(wholesaleShipmentDetail.getApplyQuantity())));
            wholesaleShipmentDetail.setVendorCode(saleGoodsInfo.getVendorCode());
            wholesaleShipmentDetail.setCreator(apiWholesaleOrderIn.getCreator());
            wholesaleShipmentDetail.setCreateTime(apiWholesaleOrderIn.getCreateTime());
            wholesaleShipmentDetail.setUpdater(apiWholesaleOrderIn.getCreator());
            wholesaleShipmentDetail.setUpdateTime(apiWholesaleOrderIn.getCreateTime());
            wholesaleShipmentDetail.setIsDelete(ModelConst.DELETE.NO);
            totalApplyQuantity.set(totalApplyQuantity.get() + wholesaleShipmentDetail.getApplyQuantity());
            totalApplyAmount.getAndSet(totalApplyAmount.get().add(wholesaleShipmentDetail.getApplyAmount()));
            WholesaleShipmentDetailIn wholesaleShipmentDetailIn = new WholesaleShipmentDetailIn();
            BeanUtils.copy(wholesaleShipmentDetail, wholesaleShipmentDetailIn);
            wholesaleShipmentDetailInList.add(wholesaleShipmentDetailIn);
            return wholesaleShipmentDetail;
        }).collect(Collectors.toList());
        ClientDistInfoOut clientDistInfo = readyHandleWholesaleApiOut.getClientDistInfoOut();
        WholesaleShipment wholesaleShipment = this.initWholesaleShipment(warehouseCode, clientDistInfo, apiWholesaleOrderIn,
                totalApplyQuantity.get(), totalApplyAmount.get(), bizOrgCode);
        wholesaleShipmentService.insertSelective(wholesaleShipment);
        wholesaleShipmentDetailList.forEach(wholesaleShipmentDetail -> wholesaleShipmentDetail.setWholesaleShipmentId(wholesaleShipment.getId()));
        wholesaleShipmentDetailService.batchSaveList(wholesaleShipmentDetailList);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                String.valueOf(wholesaleShipment.getId()),
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                MessageFormat.format(OperateLogTypeEnum.ZK_WHOLESALE_SHIPMENT_CREATE.getName(), wholesaleShipment.getShipmentNo()),
                new Date(), wholesaleShipment.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return wholesaleShipment.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditWholesaleShipment(Long shipmentId, Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap, Integer clientId) {
        WholesaleShipment wholesaleShipment = wholesaleShipmentService.selectByPrimaryKey(shipmentId);
        List<WholesaleShipmentDetail> dbDetailList = wholesaleShipmentDetailService.findListByShipmentId(shipmentId);
        AtomicReference<Integer> totalAuditQuantity = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<BigDecimal> totalAuditAmount = new AtomicReference<>(BigDecimal.ZERO);
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailInList = Lists.newArrayList();
        List<WholesaleBillDtlIn> wholesaleBillDtlInList = Lists.newArrayList();
        List<String> goodsCodeList = dbDetailList.stream().map(WholesaleShipmentDetail::getGoodsCode).collect(Collectors.toList());
        // 查询库存
        Map<String, StockWarehouseOut> stockInvMap = warehouseServer.findStockInv(wholesaleShipment.getShipmentStockCode(), goodsCodeList, wholesaleShipment.getBizOrgCode());
        dbDetailList.forEach(detail -> {
            // 计算审核数
            BigDecimal auditQuantity;
            if (DistributionWaysEnum.TRANSFER.getType().equals(wholesaleShipment.getDistributionType())) {
                auditQuantity = new BigDecimal(detail.getApplyQuantity());
                log.info("三方单据{}商品{}申请数{}-----库存数{},计算审核数{}", wholesaleShipment.getSourceNo(),
                        detail.getGoodsCode(), detail.getApplyQuantity(), "-", auditQuantity);
            } else {
                StockWarehouseOut stockWarehouseOut = stockInvMap.get(detail.getGoodsCode());
                if (Objects.isNull(stockWarehouseOut)) {
                    log.error("三方单据{}创建批发出查询商品{}仓储仓位{}无库存", wholesaleShipment.getSourceNo(), detail.getGoodsCode(), wholesaleShipment.getShipmentStockCode());
                    throw new BusinessException("三方单据" + wholesaleShipment.getSourceNo() + "创建批发出查询商品" + detail.getGoodsCode() + "仓储仓位" + wholesaleShipment.getShipmentStockCode() + "无库存");
                }
                BigDecimal stockQty = stockWarehouseOut.getBusinessQty();
                if (Objects.isNull(stockQty)) {
                    stockQty = BigDecimal.ZERO;
                }
                // 计算审核数
                auditQuantity = stockQty.compareTo(new BigDecimal(detail.getApplyQuantity())) >= NumberUtil.INTEGER_ZERO
                        ? new BigDecimal(detail.getApplyQuantity()) : stockQty;
                log.info("三方单据{}商品{}申请数{}-----库存数{},计算审核数{}", wholesaleShipment.getSourceNo(),
                        detail.getGoodsCode(), detail.getApplyQuantity(), stockQty, auditQuantity);
            }
            detail.setAuditQuantity(auditQuantity.intValue());
            detail.setAuditAmount(detail.getUnitPrice().multiply(new BigDecimal(detail.getAuditQuantity())));
            detail.setUpdateTime(LocalDateTime.now());
            detail.setUpdater(wholesaleShipment.getCreator());
            totalAuditQuantity.set(totalAuditQuantity.get() + detail.getAuditQuantity());
            totalAuditAmount.getAndSet(totalAuditAmount.get().add(detail.getAuditAmount()));
            WholesaleShipmentDetailIn wholesaleShipmentDetailIn = new WholesaleShipmentDetailIn();
            BeanUtils.copy(detail, wholesaleShipmentDetailIn);
            wholesaleShipmentDetailInList.add(wholesaleShipmentDetailIn);
            if (auditQuantity.compareTo(BigDecimal.ZERO) > NumberUtil.INTEGER_ZERO) {
                SaleGoodsInfoOut saleGoodsInfo = saleGoodsInfoOutMap.get(detail.getGoodsCode());
                //出货单下发DTS 明细入参
                WholesaleBillDtlIn wholesaleBillDtlIn = this.initWholesaleBillDtlIn(wholesaleShipment.getShipmentNo(), detail,
                        wholesaleShipment.getBizOrgCode(), saleGoodsInfo.getOrgGoodsId(), auditQuantity, detail.getAuditAmount(),
                        wholesaleShipment.getShipmentStockCode());
                wholesaleBillDtlInList.add(wholesaleBillDtlIn);
            } else {
                log.info("三方单据{}创建批发出{}明细{}审核数量为0，故不下发DTS", wholesaleShipment.getSourceNo(), wholesaleShipment.getShipmentNo(), detail.getGoodsCode());
            }
        });
        String status;
        //  HS替换单头
        if (StringUtils.isNotBlank(wholesaleShipment.getSourceNo()) && (wholesaleShipment.getSourceNo().startsWith("YH") || wholesaleShipmentService.checkIsHsShipment(""))) {
            status = totalAuditQuantity.get().compareTo(NumberUtil.INTEGER_ZERO) == NumberUtil.INTEGER_ZERO
                    ? ShipmentStatusEnum.INVALID.getCode() : ShipmentStatusEnum.APPROVED.getCode();
        } else {
            status = ShipmentStatusEnum.APPROVED.getCode();
        }
        wholesaleShipment.setAuditTime(LocalDateTime.now());
        wholesaleShipment.setUpdateTime(LocalDateTime.now());
        wholesaleShipment.setShipmentStatus(status);
        wholesaleShipmentService.updateByPrimaryKeySelective(wholesaleShipment);
        wholesaleShipmentDetailService.batchUpdateAudit(dbDetailList);
        if (ShipmentStatusEnum.APPROVED.getCode().equals(status)) {
            //调整库存
            List<StockFlowIn> stockFlowIns = wholesaleShipmentService.addOrSubStock(wholesaleShipment, wholesaleShipmentDetailInList, ShipmentStatusEnum.APPROVED.getCode());
            if (!DistributionWaysEnum.TRANSFER.getType().equals(wholesaleShipment.getDistributionType())) {
                //调用库存rpc调整库存
                Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
                if (!stockFlow.isSuccess()) {
                    throw new BusinessException("三方单据" + wholesaleShipment.getSourceNo() + "库存调整失败");
                }
            }
            //审核之后批发出货单下发DTS
            if (stockServer.isSendWms(wholesaleShipment.getShipmentStockCode(), wholesaleShipment.getBizOrgCode())) {
                this.submitWholesaleShipmentToDtsTask(clientId, wholesaleShipment, wholesaleBillDtlInList);
            }
        } else {
            if (StringUtils.isNotBlank(wholesaleShipment.getSourceNo()) && wholesaleShipment.getSourceNo().startsWith("YH")) {
                asyncPushTaskService.submit(AsyncTaskConstant.Type.ZK_WHOLESALE_SHIPMENT_BACK, JSONObject.toJSONString(wholesaleShipment),
                        wholesaleShipment.getBizOrgCode(), wholesaleShipment.getShipmentNo());
            }
            if (StringUtils.isNotBlank(wholesaleShipment.getSourceNo()) && wholesaleShipment.getSourceNo().startsWith("HS")) {
                // 单据状态回传
                BackToHsBaseInfo backToHsBaseInfo = new BackToHsBaseInfo();
                backToHsBaseInfo.setOrderNo(wholesaleShipment.getSourceNo());
                backToHsBaseInfo.setErpOrderNo(wholesaleShipment.getShipmentNo());
                backToHsBaseInfo.setBizOrgCode(wholesaleShipment.getBizOrgCode());
                hsWholesaleShipmentDtsBackSender.sendSync(JSONObject.toJSONString(backToHsBaseInfo).getBytes(), System.currentTimeMillis() + SystemConstant.HS_DIFF_DELAY_TIME);
            }
        }
        // 获取HS差异发送延迟消息
        if (StringUtils.isNotBlank(wholesaleShipment.getSourceNo()) && wholesaleShipment.getSourceNo().startsWith("HS")) {
            HSWholesaleDifferenceOrder hsWholesaleDifferenceOrder = wholesaleShipmentDetailService.initHsOrderDifference(wholesaleShipment.getId());
            if (Objects.nonNull(hsWholesaleDifferenceOrder)) {
                hsWholesaleDiffSender.sendSync(JSONObject.toJSONString(hsWholesaleDifferenceOrder).getBytes(), System.currentTimeMillis() + SystemConstant.HS_DIFF_DELAY_TIME);
            }
        }
        //保存出货单审核日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                String.valueOf(wholesaleShipment.getId()),
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                OperateLogTypeEnum.APPROVED.getName(),
                new Date(), wholesaleShipment.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
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

    private WholesaleShipment initWholesaleShipment(String shipmentWrh, ClientDistInfoOut clientDistInfo, ApiWholesaleOrderIn apiWholesaleOrderIn,
                                                    Integer totalApplyQuantity, BigDecimal totalApplyAmount, String bizOrgCode) {
        WholesaleShipment wholesaleShipment = new WholesaleShipment();
        BeanUtils.copy(apiWholesaleOrderIn, wholesaleShipment);
        wholesaleShipment.setShipmentNo(apiWholesaleOrderIn.getErpOrderNo());
        wholesaleShipment.setShipmentWrh(shipmentWrh);
        wholesaleShipment.setShipmentStatus(ShipmentStatusEnum.PENDING.getCode());
        wholesaleShipment.setShipmentStockCode(apiWholesaleOrderIn.getStockCode());
        wholesaleShipment.setShipmentWrh(apiWholesaleOrderIn.getWarehouseCode());
        wholesaleShipment.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        wholesaleShipment.setIsReversal(NumberUtil.INTEGER_ZERO);
        wholesaleShipment.setApplicationQuantity(totalApplyQuantity);
        wholesaleShipment.setApplicationAmount(totalApplyAmount);
//        wholesaleShipment.setAuditQuantity(totalAuditQuantity);
//        wholesaleShipment.setAuditAmount(totalAuditAmount);
        wholesaleShipment.setDistributionInfoId(Objects.isNull(clientDistInfo.getId()) ? null : clientDistInfo.getId().intValue());
        String driverName = apiWholesaleOrderIn.getDriverName();
        String driverPhone = apiWholesaleOrderIn.getDriverPhone();
        String driverInfo = null;
        if (StringUtils.isNotBlank(driverName) && StringUtils.isNotBlank(driverPhone)) {
            driverInfo = driverName + SystemConstant.SHORT_LINE + driverPhone;
        }
        if (StringUtils.isBlank(driverName) && StringUtils.isNotBlank(driverPhone)) {
            driverInfo = driverPhone;
        }
        if (StringUtils.isNotBlank(driverName) && StringUtils.isBlank(driverPhone)) {
            driverInfo = driverName;
        }
        if (StringUtils.isNotBlank(driverInfo)) {
            wholesaleShipment.setDriverInfo(driverInfo);
        }
        wholesaleShipment.setPriceGroupCode(clientDistInfo.getPriceGroupCode());
        wholesaleShipment.setAddressDetail(clientDistInfo.getAddressDetail());
        wholesaleShipment.setBizOrgCode(bizOrgCode);
        wholesaleShipment.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(bizOrgCode));
        wholesaleShipment.setDistributionType(apiWholesaleOrderIn.getDistributionType());
        wholesaleShipment.setUpdater(apiWholesaleOrderIn.getCreator());
        wholesaleShipment.setCreateTime(apiWholesaleOrderIn.getCreateTime());
        wholesaleShipment.setUpdateTime(wholesaleShipment.getCreateTime());
        wholesaleShipment.setIsDelete(ModelConst.DELETE.NO);
        wholesaleShipment.setAuditTime(LocalDateTime.now());
        return wholesaleShipment;
    }

    private WholesaleBillDtlIn initWholesaleBillDtlIn(String wholesaleShipmentNo, WholesaleShipmentDetail wholesaleShipmentDetail,
                                                      String bizOrgCode, Long orgGoodsId, BigDecimal auditQuantity,
                                                      BigDecimal auditAmount, String shipmentStockCode) {
        WholesaleBillDtlIn wholesaleBillDtlIn = new WholesaleBillDtlIn();
        //单号
        wholesaleBillDtlIn.setPlatform_bill_id(wholesaleShipmentNo);
        //行号
        wholesaleBillDtlIn.setLine(wholesaleShipmentDetail.getLine());
        //组织商品id
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(wholesaleShipmentDetail.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        wholesaleBillDtlIn.setSku_id(String.valueOf(orgGoodsId));
        //商品代码
        wholesaleBillDtlIn.setSku_code(wholesaleShipmentDetail.getGoodsCode());
        //数量
        wholesaleBillDtlIn.setQuantity(auditQuantity);
        //价格
        wholesaleBillDtlIn.setPrice_i(wholesaleShipmentDetail.getUnitPrice());
        //批发价格
        wholesaleBillDtlIn.setPrice_ii(wholesaleShipmentDetail.getUnitPrice());
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
        asyncPushTaskService.submit(AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_TO_DTS, JSONObject.toJSONString(wholesaleBillIn),
                wholesaleShipment.getBizOrgCode(), wholesaleShipment.getShipmentNo());
    }




}
