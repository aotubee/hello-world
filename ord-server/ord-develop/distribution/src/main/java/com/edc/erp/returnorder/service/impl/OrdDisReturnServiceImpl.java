package com.edc.erp.returnorder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.async.handel.SyncOrdDisOrderHandle;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.EquipmentBusinessConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.fund.ForeignAccountFundIn;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.purchase.FindVendorTransIn;
import com.edc.erp.common.model.out.InvBizRsnTransOut;
import com.edc.erp.common.model.out.fund.ForeignAccountFundOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.OrgGoodsTransInfo;
import com.edc.erp.common.model.out.purchase.VendorTransInfoVO;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.rpc.PurchaseOrderClient;
import com.edc.erp.common.service.*;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.model.out.OrdDisDeliveryOut;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsExpiryIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.reducestock.stock.service.StockStoreService;
import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNotice;
import com.edc.erp.returnnoticeorder.service.OrdDisReturnNoticeService;
import com.edc.erp.returnnoticeorder.service.OrdReturnNoticeStoreService;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import com.edc.erp.returnorder.entity.OrdDisReturnImage;
import com.edc.erp.returnorder.enumeration.DisOrdReturnOrderLogEnum;
import com.edc.erp.returnorder.enumeration.OrdReturnOrderStatusEnum;
import com.edc.erp.returnorder.enumeration.OrdReturnOrderTypeEnum;
import com.edc.erp.returnorder.handle.DisReturnImportHandle;
import com.edc.erp.returnorder.listener.OrdReturnOrderAsyncImportListener;
import com.edc.erp.returnorder.listener.OrdReturnStoreGoodsListener;
import com.edc.erp.returnorder.mapper.OrdDisReturnImageMapper;
import com.edc.erp.returnorder.mapper.OrdDisReturnMapper;
import com.edc.erp.returnorder.model.in.*;
import com.edc.erp.returnorder.model.out.*;
import com.edc.erp.returnorder.service.OrdDisReturnDetailService;
import com.edc.erp.returnorder.service.OrdDisReturnService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.export.AsyncExportExecutor;
import com.edc.plugins.export.ExportExcelByPage;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dictionary.service.SystemDictService;
import com.edc.sdk.dts.model.order.in.UnificationReBillDtlIn;
import com.edc.sdk.dts.model.order.in.UnificationReBillIn;
import com.edc.sdk.dts.model.order.vo.UnificationReBillDtlVO;
import com.edc.sdk.dts.model.order.vo.UnificationReBillVO;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 退货单(DisReturn)表服务实现类
 *
 * @author yaojinpeng
 * @since 2022-10-24 15:35:29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDisReturnServiceImpl extends BaseServiceImpl<OrdDisReturn> implements OrdDisReturnService {

    private final OrdDisReturnMapper ordDisReturnMapper;

    private final UniqueUtils uniqueUtils;

    private final SystemDictService systemDictService;

    private final OrdDisReturnDetailService ordDisReturnDetailService;

    private final FileService fileService;

    private final SyncOrdDisOrderHandle syncOrdDisOrderHandle;

    private final AsyncLogService asyncLogService;

    private final OrderGoodsServer orderGoodsServer;

    private final StoreCenterService storeCenterService;

    private final AsyncPushTaskService asyncPushTaskService;

    private final OrdDisReturnNoticeService ordDisReturnNoticeService;

    private final OrdReturnNoticeStoreService ordReturnNoticeStoreService;

    private final StockServer stockServer;

    private final WarehouseServer warehouseServer;

    private final FundServer fundServer;

    private final EquipmentBusinessReasonServer equipmentBusinessReasonServer;

    private final StockStoreService stockStoreService;

    private final RedisService redisService;

    private final AsyncExportExecutor asyncExportExecutor;

    private final StockFlowService stockFlowService;

    private final PurchaseOrderClient purchaseOrderClient;

    private final DisReturnImportHandle disReturnImportHandle;

    private final StoreChannelHandle storeChannelHandle;

    private final OrdDisReturnImageMapper ordDisReturnImageMapper;

    private final OrdDisDeliveryService ordDisDeliveryService;

    @Qualifier("disReturnToDtsSender")
    private final MessageSender disReturnToDtsSender;

    @Override
    public int saveOrdDisReturn(OrdDisReturn ordDisReturn) {
        ordDisReturn.setBizOrgCode(UserUtil.getBizOrgCode());
        ordDisReturn.setCreator(UserUtil.getUserName());
        ordDisReturn.setIsDelete(ModelConst.DELETE.NO);
        ordDisReturn.setReturnOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.XT.getCode(), ordDisReturn.getBizOrgCode(), uniqueUtils, 4));
        return ordDisReturnMapper.insertSelective(ordDisReturn);
    }

    @Override
    public OrdDisReturn getReturnOrderById(Integer returnOrderId) {
        return ordDisReturnMapper.selectByPrimaryKey(returnOrderId);
    }

    @Override
    public OrdDisReturn getReturnOrderByIdAndOrgCode(Integer returnOrderId, String bizOrgCode) {
        OrdDisReturn returnOrder = new OrdDisReturn();
        returnOrder.setId(returnOrderId);
        returnOrder.setBizOrgCode(bizOrgCode);
        returnOrder.setIsDelete(ModelConst.DELETE.NO);
        return ordDisReturnMapper.selectOne(returnOrder);
    }

    /**
     * 分页查询配销退货单
     *
     * @param returnOrderPageIn
     * @return
     */
    @Override
    public Page<BaseReturnOrderOut> findBaseReturnOrderForPage(OrdReturnOrderPageIn returnOrderPageIn) {
        String loginBizOrgCode = returnOrderPageIn.getBizOrgCode();
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(loginBizOrgCode);
        Optional<StockInfoOut> anyOneOptional = authOrgStockMap.values().stream().filter(stockInfoOut -> !loginBizOrgCode.equals(stockInfoOut.getBizOrgCode())).findFirst();
        if (!anyOneOptional.isPresent()) {
            returnOrderPageIn.setBizOrgCode("");
        }
        List<String> stockCodeList;
        if (StringUtils.isBlank(returnOrderPageIn.getStockCode())) {
            stockCodeList = authOrgStockMap.keySet().stream().collect(Collectors.toList());
            returnOrderPageIn.setStockCodeList(stockCodeList);
        } else {
            stockCodeList = Collections.singletonList(returnOrderPageIn.getStockCode());
            // 校验业务仓位是否允许操作
            storeChannelHandle.checkStockCodeListISAuth(stockCodeList, authOrgStockMap);
            returnOrderPageIn.setStockCode(null);
            returnOrderPageIn.setStockCodeList(stockCodeList);
        }
        List<BaseReturnOrderOut> baseReturnOrderOutList = ordDisReturnMapper.findBaseReturnOrderByPage(returnOrderPageIn);
        baseReturnOrderOutList.forEach(baseReturnOrderOut -> {
            OrdDisReturnNotice ordDisReturnNotice = ordDisReturnNoticeService.getReturnNoticeOrderById(baseReturnOrderOut.getReturnNoticeOrderId());
            baseReturnOrderOut.setReturnStatusValue(OrdReturnOrderStatusEnum.getValueByKey(baseReturnOrderOut.getReturnStatus()));
            baseReturnOrderOut.setReturnTypeValue(OrdReturnOrderTypeEnum.getValueByKey(baseReturnOrderOut.getReturnType()));
            baseReturnOrderOut.setReturnNoticeNo(Objects.isNull(ordDisReturnNotice) ? "" : ordDisReturnNotice.getReturnNoticeOrderNo());
            StockInfoOut stockInfoOut = stockServer.getTransInfo(baseReturnOrderOut.getStockCode());
            if (Objects.nonNull(stockInfoOut)) {
                baseReturnOrderOut.setWrhName(stockInfoOut.getWarehouseName());
                baseReturnOrderOut.setStockName(stockInfoOut.getStockName());
            }
            baseReturnOrderOut.setStoreAreaName(storeCenterService.getNameByCode(baseReturnOrderOut.getStoreArea(), baseReturnOrderOut.getBizOrgCode()));
            baseReturnOrderOut.setReturnOrderReasonValue(systemDictService.getSystemDictName(baseReturnOrderOut.getReturnOrderReason()));
            baseReturnOrderOut.setApplySkuCount(ordDisReturnDetailService.countApplyReturnSkuQuantity(baseReturnOrderOut.getId()));
            baseReturnOrderOut.setApplyReturnQuantity(ordDisReturnDetailService.sumApplyReturnQuantity(baseReturnOrderOut.getId()));
        });
        Page<BaseReturnOrderOut> resultPage = new Page<>(returnOrderPageIn);
        resultPage.setList(baseReturnOrderOutList);
        return resultPage;
    }

    /**
     * 审核退货单
     *
     * @param saveReturnOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response auditOrdReturn(OrdSaveReturnOrderIn saveReturnOrderIn, String centerStockBizOrgCode) {
        OrdDisReturn returnOrder = this.getReturnOrderById(saveReturnOrderIn.getReturnOrderId());
        if (null == returnOrder) {
            return Response.error("退货单不存在");
        }
        String key = DisSystemConstant.CHECK_DIS_RETURN_ORDER_AUDIT + returnOrder.getBizOrgCode() +
                SystemConstant.COLON + returnOrder.getStoreCode() + SystemConstant.WAIT + returnOrder.getReturnOrderNo();
        if (!redisService.setIfAbsent(key, returnOrder.getReturnOrderNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("短时间内重复审核" + returnOrder.getReturnOrderNo() + "，故判为无效提交！");
        }
        if (OrdReturnOrderStatusEnum.APPROVED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("退货单已审核");
        }
        if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("退货单已收货");
        }
        if (Objects.isNull(saveReturnOrderIn.getReturnNoticeOrderId())) {
            this.saveOrUpdateReturnOrder(saveReturnOrderIn, true, returnOrder.getBizOrgCode());
        }
        return this.audit(returnOrder, saveReturnOrderIn.getReturnGoodsInfoInList(), centerStockBizOrgCode);
    }

    /**
     * 作废配销退货单
     *
     * @param returnOrder
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidatedOrdReturn(OrdDisReturn returnOrder) {
        String beforeStatus = returnOrder.getReturnStatus();
        returnOrder.setReturnStatus(OrdReturnOrderStatusEnum.INVALID.getKey());
        ordDisReturnMapper.updateByPrimaryKeySelective(returnOrder);
        if (OrdReturnOrderStatusEnum.APPROVED.getKey().equals(beforeStatus)) {
            List<OrdDisReturnDetail> ordDisReturnDetails = ordDisReturnDetailService.findByReturnOrderId(returnOrder.getId());
            StockFlowIn stockFlowIn = this.initStoreStockCharge(returnOrder, ordDisReturnDetails, returnOrder.getUpdateTime());
            Response stockFlow = stockFlowService.checkStockFlow(Collections.singletonList(stockFlowIn));
            if (!stockFlow.isSuccess()) {
                throw new BusinessException(stockFlow.getMessage());
            }
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(returnOrder.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), OrdLogTypeEnum.ORD_DIS_RETURN_INVALID.getName(), new Date(), returnOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 导入配销退货单商品信息
     *
     * @param fileId
     * @param storeCode
     * @param bizOrgCode
     * @param warehouseCode
     * @param stockCode
     * @return
     */
    @Override
    public Response importReturnOrderGoods(String fileId, String storeCode, String bizOrgCode, String warehouseCode, String stockCode,
                                           String distributionType, String deliveryOrderNo, String centerStockBizOrgCode) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdReturnStoreGoodsListener listener = new OrdReturnStoreGoodsListener(orderGoodsServer, storeCode,
                bizOrgCode, warehouseServer, warehouseCode, stockCode, distributionType, centerStockBizOrgCode);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnGoodsVO.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        if (StringUtils.isBlank(deliveryOrderNo)) {
            return Response.data(listener.getSaveReturnGoodsOut(), listener.message());
        }
        return this.handleImportReturnForDeliveryOrderNo(deliveryOrderNo, listener);
    }

    private Response<List<SaveReturnGoodsOut>> handleImportReturnForDeliveryOrderNo(String deliveryOrderNo, OrdReturnStoreGoodsListener listener) {
        List<SaveReturnGoodsOut> resultReturnGoodsOutList = Lists.newArrayList();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        List<SaveReturnGoodsOut> saveReturnGoodsOutList = listener.getSaveReturnGoodsOut();
        OrdDisDeliveryOut ordDisDeliveryOut = ordDisDeliveryService.getDeliveryOrderOutByNo(deliveryOrderNo);
        String errorMessage = null;
        if (Objects.isNull(ordDisDeliveryOut) && StringUtils.isBlank(errorMessage)) {
            errorMessage = "配销单不存在此商品";
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDisDeliveryOut.getIsReversal()) && StringUtils.isBlank(errorMessage)) {
            errorMessage = "配销单已冲销";
        }
        if (!DeliveryOrderEnum.RECEIVED.getKey().equals(ordDisDeliveryOut.getDeliveryStatusCode()) && StringUtils.isBlank(errorMessage)) {
            errorMessage = "必须是已收货的配销单";
        }
        if (StringUtils.isNotBlank(errorMessage)) {
            String finalSameErrorMessage = errorMessage;
            saveReturnGoodsOutList.forEach(saveReturnGoodsOut -> {
                String mapKey = "第【" + saveReturnGoodsOut.getImportIndex() + "】行：";
                StringJoiner errorJoiner = totalErrorMap.get(mapKey);
                if (Objects.isNull(errorJoiner)) {
                    errorJoiner = new StringJoiner(SystemConstant.COMMA);
                }
                errorJoiner.add(finalSameErrorMessage);
                totalErrorMap.put("第【" + saveReturnGoodsOut.getImportIndex() + "】行：", errorJoiner);
            });
        } else {
            HashMap<String, OrdDisDeliveryDetail> detailMap = new HashMap<>();
            for (OrdDisDeliveryDetail detail : ordDisDeliveryOut.getDetailList()) {
                detailMap.put(detail.getGoodsCode(), detail);
            }
            saveReturnGoodsOutList.forEach(saveReturnGoodsOut -> {
                OrdDisDeliveryDetail detail = detailMap.get(saveReturnGoodsOut.getGoodsCode());
                String mapKey = "第【" + saveReturnGoodsOut.getImportIndex() + "】行：";
                StringJoiner errorJoiner = totalErrorMap.get(mapKey);
                if (Objects.isNull(errorJoiner)) {
                    errorJoiner = new StringJoiner(SystemConstant.COMMA);
                }
                if (Objects.isNull(detail)) {
                    errorJoiner.add("配销单不含此商品");
                    return;
                }
                if (Objects.isNull(detail.getArrivalQuantity())) {
                    errorJoiner.add("商品实收数为空");
                }
//                if (detail.getArrivalQuantity().compareTo(saveReturnGoodsOut.getApplyReturnQuantity()) == -1) {
//                    errorJoiner.add("商品实收数小于申请退货数");
//                }
                if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
                    totalErrorMap.put(mapKey, errorJoiner);
                } else {
                    saveReturnGoodsOut.setExpiry(detail.getExpiry());
                    resultReturnGoodsOutList.add(saveReturnGoodsOut);
                }
            });
        }
        return Response.data(resultReturnGoodsOutList, listener.getImportErrorMessage(totalErrorMap));
    }

    /**
     * 冲销退货单
     *
     * @param chargeReturnOrderIn
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int chargeReturnOrder(ChargeReturnOrderIn chargeReturnOrderIn) {
        OrdDisReturn ordDisReturn = ordDisReturnMapper.selectByPrimaryKey(chargeReturnOrderIn.getReturnOrderId());
        if (Objects.isNull(ordDisReturn)) {
            throw new BusinessException("退货单不存在");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDisReturn.getStockCode(), UserUtil.getBizOrgCode(), "配销退货单冲销");
        if (NumberUtil.INTEGER_ONE.equals(ordDisReturn.getIsReversal())) {
            throw new BusinessException("退货单已被红冲");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDisReturn.getIsReversalOrder())) {
            throw new BusinessException("红冲单不能被红冲");
        }
        if (!OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDisReturn.getReturnStatus())) {
            throw new BusinessException("已收货的退货单才可冲销");
        }
        //校验仓储库存
        String error = this.checkInvStock(ordDisReturn, chargeReturnOrderIn.getReturnGoodsInfoInList());
        if (StringUtils.isNotEmpty(error)) {
            throw new BusinessException(error);
        }
        //修改原退货单
        ordDisReturn.setIsReversal(NumberUtil.INTEGER_ONE);
        ordDisReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        //校验资金
        this.checkAvailableAmount(ordDisReturn);

        OrdDisReturn newOrdDisReturn = new OrdDisReturn();
        BeanUtils.copy(ordDisReturn, newOrdDisReturn);
        newOrdDisReturn.setApplyReturnQuantity(Objects.isNull(ordDisReturn.getApplyReturnQuantity()) ? BigDecimal.ZERO : ordDisReturn.getApplyReturnQuantity().negate());
        newOrdDisReturn.setApplyReturnAmount(Objects.isNull(ordDisReturn.getApplyReturnAmount()) ? BigDecimal.ZERO : ordDisReturn.getApplyReturnAmount().negate());
        newOrdDisReturn.setAuditReturnQuantity(Objects.isNull(ordDisReturn.getAuditReturnQuantity()) ? BigDecimal.ZERO : ordDisReturn.getAuditReturnQuantity().negate());
        newOrdDisReturn.setAuditReturnAmount(Objects.isNull(ordDisReturn.getAuditReturnAmount()) ? BigDecimal.ZERO : ordDisReturn.getAuditReturnAmount().negate());
        newOrdDisReturn.setActualReturnQuantity(Objects.isNull(ordDisReturn.getActualReturnQuantity()) ? BigDecimal.ZERO : ordDisReturn.getActualReturnQuantity().negate());
        newOrdDisReturn.setActualReturnAmount(Objects.isNull(ordDisReturn.getActualReturnAmount()) ? BigDecimal.ZERO : ordDisReturn.getActualReturnAmount().negate());
        newOrdDisReturn.setId(null);
        newOrdDisReturn.setCreateTime(null);
        newOrdDisReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
        newOrdDisReturn.setIsReversalOrder(NumberUtil.INTEGER_ONE);
        newOrdDisReturn.setReceiveTime(LocalDateTime.now());
        newOrdDisReturn.setSourceReturnNo(ordDisReturn.getReturnOrderNo());
        newOrdDisReturn.setReturnOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.XT.getCode(), ordDisReturn.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR));
        //新增红冲退货单
        int count = this.insertSelective(newOrdDisReturn);
        //退货冲销资金变动
        this.chargeReturnOrderToFund(newOrdDisReturn, newOrdDisReturn.getReturnOrderNo());
        //新增红冲单明细
        this.saveChargeOrdDisReturnDetail(newOrdDisReturn, chargeReturnOrderIn.getReturnGoodsInfoInList());
        //退货冲销调整库存
        this.adjustInv(newOrdDisReturn, chargeReturnOrderIn.getReturnGoodsInfoInList(), stockInfoOut, newOrdDisReturn.getReceiveTime());

        ordDisReturnMapper.updateByPrimaryKeySelective(ordDisReturn);
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_RETURN.getName(),
                String.valueOf(ordDisReturn.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN.getCode(),
                OrdLogTypeEnum.ORD_DIS_RETURN_CHARGE.getName(), new Date(), ordDisReturn.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }

    private void checkAvailableAmount(OrdDisReturn ordDisReturn) {
        ForeignAccountFundIn foreignAccountFundIn = new ForeignAccountFundIn();
        foreignAccountFundIn.setBizOrgCode(ordDisReturn.getBizOrgCode());
        foreignAccountFundIn.setPrincipalCode(ordDisReturn.getStoreCode());
        foreignAccountFundIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
        ForeignAccountFundOut foreignAccountFundOut = fundServer.getAvailableAmount(foreignAccountFundIn);
        if (Objects.isNull(foreignAccountFundOut)) {
            String error = "门店" + ordDisReturn.getStoreCode() + "资金账户不存在";
            log.info(error);
            throw new BusinessException(error);
        }
        if (foreignAccountFundOut.getAvailableAmount().add(foreignAccountFundOut.getCredit()).compareTo(ordDisReturn.getActualReturnAmount()) < NumberUtil.INTEGER_ZERO) {
            log.info("门店" + ordDisReturn.getStoreCode() + "可用资金不足");
            throw new BusinessException("门店" + ordDisReturn.getStoreCode() + "可用资金不足");
        }
    }

    /**
     * 保存退货单
     *
     * @param saveReturnOrderIn
     * @param isCheckRepeat
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response saveOrUpdateReturnOrder(OrdSaveReturnOrderIn saveReturnOrderIn, boolean isCheckRepeat, String channelBizOrgCode) {
        int returnOrderId;
        if (null != saveReturnOrderIn.getReturnOrderId()) {
            returnOrderId = this.updateDisReturnOrder(saveReturnOrderIn);
        } else {
            if (isCheckRepeat) {
                boolean checkReturnOrderRepeatSubmitFlag = this.checkDisReturnOrderSubmit(saveReturnOrderIn);
                if (checkReturnOrderRepeatSubmitFlag) {
                    log.error("门店{}发起退货单，短时间内异常重复提交，故判为无效提交！", saveReturnOrderIn.getStoreCode());
                    return Response.error("请不要重复提交");
                }
            }
            returnOrderId = this.insertDisReturnOrder(saveReturnOrderIn, channelBizOrgCode);
            this.setDisReturnOrderKey(saveReturnOrderIn);
        }
        if (returnOrderId > 0) {
            return Response.data(returnOrderId, "保存成功");
        }
        return Response.error("保存失败");
    }

    private boolean checkDisReturnOrderSubmit(OrdSaveReturnOrderIn saveReturnOrderIn) {
        Integer returnNoticeOrderId = Objects.isNull(saveReturnOrderIn.getReturnNoticeOrderId()) ? 0 : saveReturnOrderIn.getReturnNoticeOrderId();
        String key = DisSystemConstant.CHECK_DIS_RETURN_ORDER_REPEAT_SUBMIT + saveReturnOrderIn.getBizOrgCode() + SystemConstant.COLON + returnNoticeOrderId +
                SystemConstant.COLON + saveReturnOrderIn.getStoreCode() + SystemConstant.COLON + saveReturnOrderIn.getWarehouseCode();
        return redisService.hasKey(key);
    }

    private void setDisReturnOrderKey(OrdSaveReturnOrderIn saveReturnOrderIn) {
        Integer returnNoticeOrderId = Objects.isNull(saveReturnOrderIn.getReturnNoticeOrderId()) ? 0 : saveReturnOrderIn.getReturnNoticeOrderId();
        String key = DisSystemConstant.CHECK_DIS_RETURN_ORDER_REPEAT_SUBMIT + saveReturnOrderIn.getBizOrgCode() + SystemConstant.COLON + returnNoticeOrderId +
                SystemConstant.COLON + saveReturnOrderIn.getStoreCode() + SystemConstant.COLON + saveReturnOrderIn.getWarehouseCode();
        redisService.set(key, key, 1, TimeUnit.MINUTES);
    }

    @Override
    public void initReturnOrderToDts(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> returnGoodsInfoInList, String centerStockBizOrgCode) {
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDisReturn.getStoreCode());
        UnificationReBillIn unificationReBillIn = new UnificationReBillIn();
        //单号
        unificationReBillIn.setPlatform_bill_id(ordDisReturn.getReturnOrderNo());
        //仓储代码
        unificationReBillIn.setWarehouse_id(ordDisReturn.getWrhCode());
        //仓位代码
        unificationReBillIn.setSource_stock_id(ordDisReturn.getStockCode());
        //门店id
        unificationReBillIn.setShop_id(Objects.isNull(storeOut) ? "" : storeOut.getStoreId().toString());
        //门店代码
        unificationReBillIn.setShop_code(ordDisReturn.getStoreCode());
        //填单人
        unificationReBillIn.setCreater(ordDisReturn.getAuditor());
        //生成时间
        unificationReBillIn.setGenerate_time(ordDisReturn.getUpdateTime());
        //退货时间
        unificationReBillIn.setBill_create_date(LocalDateTime.now().toLocalDate());
        //配货方式
        unificationReBillIn.setAlc(DistributionWaysEnum.UNIFIEDDIS.getType());
        //明细
        List<UnificationReBillDtlIn> detail = this.initUnificationReBillDtlIn(ordDisReturn, returnGoodsInfoInList);
        if (CollectionUtils.isEmpty(detail)) {
            log.info("门店{}退货单{}无明细，不下发DTS", ordDisReturn.getStoreCode(), ordDisReturn.getReturnOrderNo());
            return;
        }
        unificationReBillIn.setDetail_list(detail);
        //来源组织
        unificationReBillIn.setSource_organization(centerStockBizOrgCode);
        //目标组织
        unificationReBillIn.setTarget_organization(centerStockBizOrgCode);
        //发送时间
        unificationReBillIn.setSend_time(LocalDateTime.now());
        //渠道
        unificationReBillIn.setChannel_id(null);
        //来源单位
        unificationReBillIn.setSource_unit(null);
//        unificationReBillIn.setMemo(OrdReturnOrderTypeEnum.getValueByKey(ordDisReturn.getReturnType()));
        unificationReBillIn.setMemo(systemDictService.getSystemDictName(ordDisReturn.getReturnOrderReason()));
        //下发dts
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_RETURN_TO_DTS, JSON.toJSONString(unificationReBillIn), ordDisReturn.getBizOrgCode(), ordDisReturn.getReturnOrderNo());
        SendResponse sendResponse = disReturnToDtsSender.sendSync(JSON.toJSONString(unificationReBillIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("DTS配销退货单数据下发{}消息ID---{}", ordDisReturn.getReturnOrderNo(), sendResponse.getMessageId());
    }

    /**
     * 查询配销退货单列表(库存盘点)
     *
     * @param ordDisReturn
     * @return
     */
    @Override
    public List<BaseReturnOrderOut> findDisReturnOrder(OrdDisReturn ordDisReturn) {
        ordDisReturn.setReturnStatus(OrdReturnOrderStatusEnum.APPROVED.getKey());
        List<BaseReturnOrderOut> returnOrderOutList = ordDisReturnMapper.findDisReturnOrder(ordDisReturn);
        returnOrderOutList.forEach(returnOrderOut -> {
            returnOrderOut.setReturnStatusValue(OrdReturnOrderStatusEnum.getValueByKey(returnOrderOut.getReturnStatus()));
            StockInfoOut stockInfoOut = stockServer.getTransInfo(returnOrderOut.getStockCode());
            returnOrderOut.setStockName(Objects.isNull(stockInfoOut) ? "" : stockInfoOut.getStockName());
            returnOrderOut.setReturnTypeValue(OrdReturnOrderTypeEnum.getValueByKey(returnOrderOut.getReturnType()));
            returnOrderOut.setReturnOrderReasonValue(systemDictService.getSystemDictName(returnOrderOut.getReturnOrderReason()));
        });
        return returnOrderOutList;
    }

    /**
     * App提交退货单
     *
     * @param saveReturnOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> submitReturnOrderForApp(OrdSaveReturnOrderIn saveReturnOrderIn) {
        OrdDisReturn ordDisReturn = this.getReturnOrderById(saveReturnOrderIn.getReturnOrderId());
        if (Objects.isNull(ordDisReturn)) {
            return Response.error("不存在的退货单");
        }
        String key = DisSystemConstant.CHECK_DIS_RETURN_ORDER_SUBMIT_APP + ordDisReturn.getBizOrgCode() +
                SystemConstant.COLON + ordDisReturn.getStoreCode() + SystemConstant.WAIT + ordDisReturn.getReturnOrderNo();
        if (!redisService.setIfAbsent(key, ordDisReturn.getReturnOrderNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("短时间内重复提交" + ordDisReturn.getReturnOrderNo());
        }
        if (Objects.nonNull(saveReturnOrderIn.getReturnNoticeOrderId())) {
            boolean overtimeForSubmitFlag = ordDisReturnNoticeService.isOvertimeForSubmit(saveReturnOrderIn.getReturnNoticeOrderId());
            if (overtimeForSubmitFlag) {
                return Response.error("该退货通知单退货截止时间已超时，不能提交退货");
            }
        }
        List<OrdDisReturnDetail> returnGoodsInfoInList = ordDisReturnDetailService.findByReturnOrderId(ordDisReturn.getId());
        ordDisReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
        ordDisReturn.setSubmitTime(LocalDateTime.now());
        if (Objects.nonNull(saveReturnOrderIn.getReturnNoticeOrderId())) {
            return this.handleAuditForSubmit(ordDisReturn, returnGoodsInfoInList);
        } else {
            // 天岁接入ERP，不再对接中科接口
//            if (OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(ordDisReturn.getBizOrgCode())) {
//                return this.handleAuditForSubmit(ordDisReturn, returnGoodsInfoInList);
//            }
            ordDisReturnMapper.updateByPrimaryKeySelective(ordDisReturn);
            String content = DisOrdReturnOrderLogEnum.ORD_DIS_APP_SUBMIT.getValue();
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
                    OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), content, new Date(), UserUtil.getUserName());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.success("提交成功");
        }
    }

    private Response<String> handleAuditForSubmit(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> returnGoodsInfoInList) {
//        BigDecimal auditReturnQuantity = BigDecimal.ZERO;
//        BigDecimal auditReturnAmount = BigDecimal.ZERO;
        for (OrdDisReturnDetail ordDisReturnDetail : returnGoodsInfoInList) {
            ordDisReturnDetail.setAuditReturnAmount(ordDisReturnDetail.getApplyReturnAmount());
            ordDisReturnDetail.setAuditReturnQuantity(ordDisReturnDetail.getApplyReturnQuantity());
            ordDisReturnDetail.setAuditPackageQuantity(ordDisReturnDetail.getApplyPackageQuantity());
            ordDisReturnDetailService.updateByPrimaryKey(ordDisReturnDetail);
//            auditReturnQuantity = auditReturnQuantity.add(Objects.isNull(ordDisReturnDetail.getAuditReturnQuantity()) ? BigDecimal.ZERO : ordDisReturnDetail.getAuditReturnQuantity());
//            auditReturnAmount = auditReturnAmount.add(Objects.isNull(ordDisReturnDetail.getAuditReturnAmount()) ? BigDecimal.ZERO : ordDisReturnDetail.getAuditReturnAmount());
        }
//        ordDisReturn.setAuditReturnQuantity(auditReturnQuantity);
//        ordDisReturn.setAuditReturnAmount(auditReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDisReturn.getStockCode());
        return this.audit(ordDisReturn, returnGoodsInfoInList, stockInfoOut.getBizOrgCode());
    }


    /**
     * 根据退货主键删除退货单
     *
     * @param returnOrderId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByReturnOrderById(Integer returnOrderId) {
        OrdDisReturn ordDisReturn = new OrdDisReturn();
        ordDisReturn.setId(returnOrderId);
        ordDisReturnDetailService.deleteByReturnOrderId(ordDisReturn);
        ordDisReturnMapper.deleteByPrimaryKey(returnOrderId);
        OrdDisReturnImage ordDisReturnImage = new OrdDisReturnImage();
        ordDisReturnImage.setReturnOrderId(returnOrderId);
        ordDisReturnImageMapper.delete(ordDisReturnImage);

    }

    /**
     * 配销退货单回传
     *
     * @param unificationReBillVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unificationReOrderCallBack(UnificationReBillVO unificationReBillVO) {
        OrdDisReturn query = new OrdDisReturn();
//        UnificationReBillVO unificationReBillVO = JSON.parseObject(messageJson, UnificationReBillVO.class);
        List<UnificationReBillDtlVO> detail = unificationReBillVO.getDetail();
        query.setReturnOrderNo(unificationReBillVO.getFsrcnum());
        OrdDisReturn ordDisReturn = this.selectOne(query);
        if (Objects.isNull(ordDisReturn)) {
//            return "此配销退货单不存在" + unificationReBillVO.getFsrcnum();
            throw new BusinessException("此配销退货单不存在" + unificationReBillVO.getFsrcnum());
        }
        if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDisReturn.getReturnStatus())) {
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_DIS_RETURN_PROCESSED_SYSTEM.getCode(),
                    String.valueOf(ordDisReturn.getId()),
                    OrdLogTypeEnum.ORD_DIS_RETURN.getCode(),
                    OrdLogTypeEnum.ORD_DIS_RETURN_PROCESSED_SYSTEM.getName(), new Date(), ordDisReturn.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            log.error("该退货单{}后台已收货", unificationReBillVO.getFsrcnum());
//            return "该退货单后台已收货";
            return true;
        }
        if (OrdReturnOrderStatusEnum.INVALID.getKey().equals(ordDisReturn.getReturnStatus())) {
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_DIS_RETURN_PROCESSED_SYSTEM.getCode(),
                    String.valueOf(ordDisReturn.getId()),
                    OrdLogTypeEnum.ORD_DIS_RETURN.getCode(),
                    OrdLogTypeEnum.ORD_DIS_RETURN_PROCESSED_SYSTEM.getName(), new Date(), ordDisReturn.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            log.error("该退货单{}后台已作废", unificationReBillVO.getFsrcnum());
//            return "该退货单后台已作废";
            return true;
        }
        ordDisReturn.setReturnStatus(OrdReturnOrderStatusEnum.PROCESSED.getKey());
        ordDisReturn.setReceiveTime(LocalDateTime.now());
        ordDisReturn.setLogisticsNo(unificationReBillVO.getNum());
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDisReturn.getStockCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("找不到仓位");
        }
        //初始化退货单
        List<OrdDisReturnDetail> ordDisReturnDetailList = this.initReturnOrder(ordDisReturn, detail, stockInfoOut.getBizOrgCode());
        //修改退货单
        this.updateByPrimaryKeySelective(ordDisReturn);
        //修改退货单明细
        ordDisReturnDetailService.batchUpdate(ordDisReturnDetailList);
        // 释放库存
        this.adjustInv(ordDisReturn, ordDisReturnDetailList, stockInfoOut, ordDisReturn.getReceiveTime());
        //资金返还
        this.receivingReturnOrderToFund(ordDisReturn);
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_RETURN.getName(),
                String.valueOf(ordDisReturn.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN.getCode(),
                MessageFormat.format(DisOrdReturnOrderLogEnum.ORD_DIS_RETURN_PROCESSED_BACK_AMOUNT.getValue(),
                        ordDisReturn.getActualReturnAmount().abs()), new Date(), ordDisReturn.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        return "成功";
        return true;
    }

    @Override
    public String exportOrdReturnDetailByOrder(OrdReturnOrderPageIn ordReturnOrderPageIn) {
        LocalDateTime now = LocalDateTime.now();
//        ordReturnOrderPageIn.setEndTime(DateUtil.formatLocalDateTime(now));
//        ordReturnOrderPageIn.setStartTime(DateUtil.formatLocalDateTime(now.plusDays(-2)));
        // 设置每次查询条数
        ordReturnOrderPageIn.setPageNum(1);
        ordReturnOrderPageIn.setPageSize(5000);
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = "配销退货单明细".concat(DateUtil.format(now, DatePattern.PURE_DATETIME_MS_PATTERN)).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配销退货单明细",
                        // 导出模板实体
                        AsyncExcelReturnOrderDetail.class,
                        // 分页查询对象
                        ordReturnOrderPageIn,
                        // 分页查询方法
                        page -> this.findListForAsyncExportPage(ordReturnOrderPageIn))
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    @Override
    public OrdDisReturn getReturnOrderByNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return null;
        }
        OrdDisReturn ordDisReturn = new OrdDisReturn();
        ordDisReturn.setReturnOrderNo(orderNo);
        return ordDisReturnMapper.selectOne(ordDisReturn);
    }

    @Override
    public Response<String> batchInvalidatedOrdReturn(List<Integer> idList) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        idList.forEach(id -> {
            OrdDisReturn returnOrder = this.getReturnOrderById(id);
            if (null == returnOrder) {
                errorJoiner.add("退货单" + id + "不存在");
                return;
            }
            if (!OrdReturnOrderStatusEnum.SUBMITTED.getKey().equals(returnOrder.getReturnStatus()) && !OrdReturnOrderStatusEnum.SAVED.getKey().equals(returnOrder.getReturnStatus())
                    && !OrdReturnOrderStatusEnum.APPROVED.getKey().equals(returnOrder.getReturnStatus())) {
                errorJoiner.add("退货单" + returnOrder.getReturnOrderNo() + "只有待审核、已保存和已审核才可作废");
                return;
            }
            String loginUsername = UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】");
            returnOrder.setUpdateTime(LocalDateTime.now());
            returnOrder.setUpdater(loginUsername);
            try {
                stockServer.getAndCheckStockInfo(returnOrder.getStockCode(), UserUtil.getBizOrgCode(), "作废配销退货单");
                this.invalidatedOrdReturn(returnOrder);
            } catch (Exception e) {
                log.error("退货单{}作废异常", returnOrder.getReturnOrderNo(), e);
                errorJoiner.add("退货单:" + returnOrder.getReturnOrderNo() + "作废失败");
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.success(errorJoiner.toString());
        }
        return Response.success();
    }

    @Override
    public Response<String> batchReceiving(List<Integer> idList, String loginUsername) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        idList.forEach(id -> {
            OrdDisReturn returnOrder = this.getReturnOrderById(id);
            if (null == returnOrder) {
                errorJoiner.add("退货单" + id + "不存在");
                return;
            }
            try {
                StockInfoOut stockInfoOut = stockInfoOutMap.get(returnOrder.getStockCode());
                if (Objects.isNull(stockInfoOut)) {
                    stockInfoOut = stockServer.getAndCheckStockInfo(returnOrder.getStockCode(), UserUtil.getBizOrgCode(), "配销退货单收货");
                    if (Objects.isNull(stockInfoOut)) {
                        return;
                    }
                }
                List<OrdDisReturnDetail> returnGoodsInfoInList = ordDisReturnDetailService.findByReturnOrderId(id);
                if (CollectionUtils.isNotEmpty(returnGoodsInfoInList)) {
                    returnGoodsInfoInList.forEach(ordDirReturnDetail -> {
                        ordDirReturnDetail.setActualReturnQuantity(ordDirReturnDetail.getAuditReturnQuantity());
                        ordDirReturnDetail.setActualPackageQuantity(ordDirReturnDetail.getAuditPackageQuantity());
                        ordDirReturnDetail.setActualReturnAmount(ordDirReturnDetail.getAuditReturnAmount());
                    });
                }
                OrdSaveReturnOrderIn saveReturnOrderIn = new OrdSaveReturnOrderIn();
                saveReturnOrderIn.setReturnOrderId(id);
                saveReturnOrderIn.setReturnGoodsInfoInList(returnGoodsInfoInList);
                saveReturnOrderIn.setLoginUsername(loginUsername);
                Response response = this.receiving(returnGoodsInfoInList, returnOrder, stockInfoOut);
                if (!response.isSuccess()) {
                    errorJoiner.add("退货单:" + returnOrder.getReturnOrderNo() + response.getMessage());
                }
            } catch (Exception e) {
                log.error("退货单{}收货异常", returnOrder.getReturnOrderNo(), e);
                errorJoiner.add("退货单:" + returnOrder.getReturnOrderNo() + "收货失败");
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.error(errorJoiner.toString());
        }
        return Response.success();
    }

    @Override
    public OrdDisReturn getReturnOrderNoByIdAndBizOrgCode(String returnOrderNo, String bizOrgCode) {
        OrdDisReturn returnOrder = new OrdDisReturn();
        returnOrder.setReturnOrderNo(returnOrderNo);
        returnOrder.setBizOrgCode(bizOrgCode);
        returnOrder.setIsDelete(ModelConst.DELETE.NO);
        return ordDisReturnMapper.selectOne(returnOrder);
    }

    @Override
    public List<DisReturnOrderPrintOut> findPrintDataByIds(List<Long> ids, String bizOrgCode) {
        // 仓位
        Map<String, StockInfoOut> stockMap = stockServer.findAll(bizOrgCode);
        List<DisReturnOrderPrintOut> printOutList = new ArrayList<>();
        for (Long id : ids) {
            DisReturnOrderPrintOut orderPrintOut = new DisReturnOrderPrintOut();
            OrdDisReturn ordDisReturn = selectByPrimaryKey(id);
            if (ordDisReturn == null) {
                throw new BusinessException("无效的退货单id！");
            }
            BeanUtils.copy(ordDisReturn, orderPrintOut);
            StockInfoOut stockInfoOut = stockMap.get(orderPrintOut.getStockCode());
            if (stockInfoOut != null) {
                orderPrintOut.setStockName(stockInfoOut.getStockName());
            }
            orderPrintOut.setReturnStatusName(systemDictService.getSystemDictName(orderPrintOut.getReturnStatus()));
            List<DisReturnOrderDtlPrintOut> dtlPrintOutList = ordDisReturnDetailService.findPrintDtlByReturnId(id);
            // 实配包装数合计
            BigDecimal deliveryPackQuantity = dtlPrintOutList.stream().map(DisReturnOrderDtlPrintOut::getApplyPackageQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            orderPrintOut.setApplyReturnPackQuantity(deliveryPackQuantity);
            orderPrintOut.setDtlPrintOuts(dtlPrintOutList);

            printOutList.add(orderPrintOut);
        }
        return printOutList;
    }

    @Override
    public Response<String> asyncImportReturn(String fileId, String loginUsername, String loginBizOrgCode) {
        String key = DisSystemConstant.CHECK_DIS_RETURN_IMPORT_ONLY_ONE + loginBizOrgCode;
        if (!redisService.setIfAbsent(key, loginUsername, 5L, TimeUnit.MINUTES)) {
            return Response.error("当前有用户正在异步上传退货单，请稍后尝试");
        }
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            redisService.del(key);
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdReturnOrderAsyncImportListener listener = new OrdReturnOrderAsyncImportListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnOrderVO.class, listener).headRowNumber(NumberUtil.INTEGER_ONE).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > NumberUtil.INTEGER_ZERO) {
            redisService.del(key);
            return Response.error(listener.getImportErrorMessage(totalErrorMap));
        }
        disReturnImportHandle.handleReturnListAsyncImport(listener.getImportOrdReturnOrderList(), loginUsername, loginBizOrgCode, key);
        return Response.success("开始导入，请耐心等待，切勿重复操作！");
    }


    private List<UnificationReBillDtlIn> initUnificationReBillDtlIn(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> returnGoodsInfoInList) {
        List<UnificationReBillDtlIn> dtlInList = new ArrayList<>();
        List<String> vendorCodeList = returnGoodsInfoInList.stream().map(OrdDisReturnDetail::getVendorCode).collect(Collectors.toList());
        FindVendorTransIn findVendorTransIn = new FindVendorTransIn();
        findVendorTransIn.setVendorCodes(vendorCodeList);
//        findVendorTransIn.setBizOrgCode(ordDisReturn.getBizOrgCode());
        Response<List<VendorTransInfoVO>> vendorResponse = purchaseOrderClient.findTransByCodes(findVendorTransIn);
        if (!vendorResponse.isSuccess()) {
            throw new BusinessException("获取订单方异常");
        }
        Map<String, VendorTransInfoVO> vendorTransInfoVOMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(vendorResponse.getData())) {
            vendorTransInfoVOMap = vendorResponse.getData().stream().collect(Collectors.toMap(VendorTransInfoVO::getVendorCode, Function.identity()));
        }
        for (OrdDisReturnDetail item : returnGoodsInfoInList) {
            if (BigDecimal.ZERO.compareTo(item.getAuditReturnQuantity()) > -1) {
                continue;
            }
            OrgGoodsTransInfo goodsOut = orderGoodsServer.getGoodsOut(item.getGoodsCode(), ordDisReturn.getBizOrgCode());
            UnificationReBillDtlIn dtlIn = new UnificationReBillDtlIn();
            dtlIn.setPlatform_bill_id(ordDisReturn.getReturnOrderNo());
            dtlIn.setSku_id(Objects.isNull(goodsOut) ? "" : goodsOut.getId().toString());
            dtlIn.setQuantity(item.getAuditReturnQuantity());
            dtlIn.setLine(item.getLine());
            dtlIn.setSku_code(Objects.isNull(goodsOut) ? "" : goodsOut.getGoodsCode());
            dtlIn.setPrice_i(item.getReturnUnitPrice());
            dtlIn.setSource_organization(ordDisReturn.getBizOrgCode());
            dtlIn.setTarget_organization(ordDisReturn.getBizOrgCode());
//            GoodsDistributionPlanGroupDetailsVendorOut planCodeByGoodsCode = goodsDistributionPlanGroupDetailsService.getPlanCodeByGoodsCode(item.getGoodsCode(), ordDisReturn.getBizOrgCode(), alcSchemeCode);
//            if (Objects.isNull(planCodeByGoodsCode) || Objects.isNull(planCodeByGoodsCode.getVendorId())) {
//                throw new BusinessException("未查询到订单方信息");
//            }
            VendorTransInfoVO vendorTransInfoVO = vendorTransInfoVOMap.get(item.getVendorCode());
            if (Objects.isNull(vendorTransInfoVO)) {
                throw new BusinessException("订单方" + item.getVendorCode() + "ID不存在");
            }
            dtlIn.setSupplier_id(vendorTransInfoVO.getVendorId().toString());
            if (StringUtils.isNotEmpty(item.getReturnReason())) {
                InvBizRsnTransOut invBizRsnTransOut = equipmentBusinessReasonServer.getStoreInvBizRsnTransByCode(item.getReturnReason(), ordDisReturn.getBizOrgCode(), NumberUtil.INTEGER_ZERO);
                dtlIn.setMemo(Objects.isNull(invBizRsnTransOut) ? item.getReturnReason() : invBizRsnTransOut.getBusinessReasonName());
            }
            dtlInList.add(dtlIn);
        }
        return dtlInList;
    }


    @Override
    public List<OrdDisReturnDetail> initReturnOrder(OrdDisReturn ordDisReturn, List<UnificationReBillDtlVO> detail, String centerBizOrgCode) {
        OrdDisReturnDetail queryDtl = new OrdDisReturnDetail();
        queryDtl.setReturnOrderId(ordDisReturn.getId());
        queryDtl.setIsDelete(ModelConst.DELETE.NO);
        List<OrdDisReturnDetail> ordDisReturnDetails = ordDisReturnDetailService.list(queryDtl);
        Map<String, OrdDisReturnDetail> collect = CollectionUtils.emptyIfNull(ordDisReturnDetails).stream().collect(Collectors.toMap(OrdDisReturnDetail::getGoodsCode, item -> item));
        // 批准退货总数量
        BigDecimal totalActualReturnQuantity = BigDecimal.ZERO;
        // 批准退货总金额
        BigDecimal totalActualReturnAmount = BigDecimal.ZERO;
        List<OrdDisReturnDetail> details = new ArrayList<>();
        for (UnificationReBillDtlVO item : detail) {
            OrdDisReturnDetail ordDisReturnDetail = collect.get(item.getFarticlecode());
            ordDisReturnDetail.setReturnOrderId(ordDisReturn.getId());
            //实际数量
            ordDisReturnDetail.setActualReturnQuantity(item.getFqty());
            //实际退货金额
//            ordDisReturnDetail.setActualReturnAmount(item.getFqty().multiply(ordDisReturnDetail.getReturnUnitPrice()));

            //最新门店商品库存价
            BigDecimal stockPrice = warehouseServer.getStockPrice(ordDisReturn.getStoreCode(), ordDisReturnDetail.getGoodsCode(), ordDisReturn.getBizOrgCode());
            if (Objects.isNull(stockPrice)) {
                throw new BusinessException("门店商品" + ordDisReturnDetail.getGoodsCode() + "库存价为空");
            }
            ordDisReturnDetail.setStoreStockPrice(stockPrice);
            //实际退货金额
            ordDisReturnDetail.setActualReturnAmount((stockPrice.multiply(item.getFqty())));
            ordDisReturnDetail.setWrhCostAmount(ordDisReturnDetail.getActualReturnAmount());

            //实际退货包装数
            ordDisReturnDetail.setActualPackageQuantity(ordDisReturnDetail.getActualReturnQuantity().divide(ordDisReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //1+税率
            BigDecimal sell = Objects.isNull(ordDisReturnDetail.getSellTax()) ? BigDecimal.ZERO : ordDisReturnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            //实际退货去税金额
            ordDisReturnDetail.setReturnExceptTaxAmount(ordDisReturnDetail.getActualReturnAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            // 实际税额
            ordDisReturnDetail.setReturnTaxAmount(ordDisReturnDetail.getActualReturnAmount().subtract(ordDisReturnDetail.getReturnExceptTaxAmount()));

//            ordDisReturnDetail.setWrhPrice(ordDisReturnDetail.getReturnUnitPrice());
            //仓储成本金额
            BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(ordDisReturn.getWrhCode(), ordDisReturn.getStockCode(),
                    ordDisReturnDetail.getGoodsCode(), centerBizOrgCode, ordDisReturn.getStoreCode(), ordDisReturn.getBizOrgCode());
            if (Objects.isNull(warehousePrice)) {
                log.error("配销退货单{}商品{}仓储库存价为空", ordDisReturn.getReturnOrderNo(), ordDisReturnDetail.getGoodsCode());
                throw new BusinessException("退货单" + ordDisReturn.getReturnOrderNo() + "商品" + ordDisReturnDetail.getGoodsCode() + "仓储库存价为空");
            }
//            BigDecimal warehousePrice = ordDisReturnDetail.getWrhPrice();
            ordDisReturnDetail.setWrhPrice(warehousePrice);
//            BigDecimal wrhCostAmount = warehousePrice.multiply(ordDisReturnDetail.getActualReturnQuantity());
//            ordDisReturnDetail.setWrhCostAmount(wrhCostAmount);

            //仓储成本去税金额
            ordDisReturnDetail.setWrhExceptTaxAmount(ordDisReturnDetail.getWrhCostAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //仓储成本税额
            ordDisReturnDetail.setWrhTaxAmount(ordDisReturnDetail.getWrhCostAmount().subtract(ordDisReturnDetail.getWrhExceptTaxAmount()));

//            BigDecimal stockPrice = ordDisReturnDetail.getStoreStockPrice();
            //门店成本金额,退货单已收货门店财务库存减少。门店相关为负值
            ordDisReturnDetail.setStoreCostAmount((Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice).multiply(ordDisReturnDetail.getActualReturnQuantity()).negate());
            //门店成本去税金额
            ordDisReturnDetail.setStoreExceptTaxAmount(ordDisReturnDetail.getStoreCostAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //门店成本税额
            ordDisReturnDetail.setStoreTaxAmount(ordDisReturnDetail.getStoreCostAmount().subtract(ordDisReturnDetail.getStoreExceptTaxAmount()));


            totalActualReturnAmount = totalActualReturnAmount.add(ordDisReturnDetail.getActualReturnAmount());

            totalActualReturnQuantity = totalActualReturnQuantity.add(ordDisReturnDetail.getActualReturnQuantity());
            details.add(ordDisReturnDetail);
        }
        ordDisReturn.setActualReturnAmount(totalActualReturnAmount);
        ordDisReturn.setActualReturnQuantity(totalActualReturnQuantity);
        return details;
    }

    /**
     * 修改配销退货单
     *
     * @param saveReturnOrderIn
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateDisReturnOrder(OrdSaveReturnOrderIn saveReturnOrderIn) {
        OrdDisReturn ordDisReturn = ordDisReturnMapper.selectByPrimaryKey(saveReturnOrderIn.getReturnOrderId());
        if (!OrdReturnOrderStatusEnum.SUBMITTED.getKey().equals(ordDisReturn.getReturnStatus())) {
            throw new BusinessException("只有未审核状态才能修改");
        }
        String s = this.checkData(saveReturnOrderIn, ordDisReturn.getBizOrgCode());
        if (StringUtils.isNotEmpty(s)) {
            throw new BusinessException(s);
        }
//        BeanUtil.copyProperties(saveReturnOrderIn, ordDisReturn, CopyOptions.create().setIgnoreNullValue(true));
//        ordDisReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
//        ordDisReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
//        ordDisReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
//        ordDisReturn.setUpdater(UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】"));
//        ordDisReturn.setUpdateTime(LocalDateTime.now());
//        ordDisReturn.setWrhCode(saveReturnOrderIn.getWarehouseCode());
//        int count = ordDisReturnMapper.updateByPrimaryKeySelective(ordDisReturn);

        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), OrdLogTypeEnum.ORD_DIS_RETURN_UPDATE.getName(), new Date(), ordDisReturn.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        //保存明细
//        if (count > 0) {
        ordDisReturnDetailService.deleteByReturnOrderId(ordDisReturn);
        ordDisReturnDetailService.handleDetail(ordDisReturn, saveReturnOrderIn);
//        }
        return ordDisReturn.getId();
    }

    /**
     * 保存配销退货单
     *
     * @param saveReturnOrderIn
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer insertDisReturnOrder(OrdSaveReturnOrderIn saveReturnOrderIn, String channelBizOrgCode) {
        //校验数据
        String s = this.checkData(saveReturnOrderIn, channelBizOrgCode);
        if (StringUtils.isNotEmpty(s)) {
            throw new BusinessException(s);
        }
        //保存退货单
        OrdDisReturn ordDisReturn = new OrdDisReturn();
        BeanUtil.copyProperties(saveReturnOrderIn, ordDisReturn, CopyOptions.create().setIgnoreNullValue(true));
        String no = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.XT.getCode(), channelBizOrgCode, uniqueUtils, 4);
        ordDisReturn.setReturnOrderNo(no);
        ordDisReturn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
        ordDisReturn.setOrgCode(UserUtil.getOrgCode());
        ordDisReturn.setSubmitTime(LocalDateTime.now());
        ordDisReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        ordDisReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
        ordDisReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
        ordDisReturn.setIsDelete(NumberUtil.INTEGER_ZERO);
        ordDisReturn.setBizOrgCode(channelBizOrgCode);
        ordDisReturn.setWrhCode(saveReturnOrderIn.getWarehouseCode());
        ordDisReturn.setSkuCount(saveReturnOrderIn.getReturnGoodsInfoInList().size());
//        ordDisReturn.setDistributionWay(DistributionWaysEnum.UNIFIEDDIS.getType());
//        ordDisReturn.setDistributionType(DistributionWaysEnum.getNameByType(saveReturnOrderIn.getDistributionType()));
        ordDisReturn.setDistributionType(saveReturnOrderIn.getDistributionType());
        int count = ordDisReturnMapper.insertSelective(ordDisReturn);
        //保存明细及
        if (count > 0) {
            ordDisReturnDetailService.handleDetail(ordDisReturn, saveReturnOrderIn);
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), OrdLogTypeEnum.ORD_DIS_RETURN_SAVE.getName(), new Date(), ordDisReturn.getCreator());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return ordDisReturn.getId();
    }

    /**
     * 数据校验
     *
     * @param saveReturnOrderIn
     * @return
     */
    private String checkData(OrdSaveReturnOrderIn saveReturnOrderIn, String channelBizOrgCode) {
        List<OrdDisReturnDetail> returnGoodsInfoInList = saveReturnOrderIn.getReturnGoodsInfoInList();
        StringJoiner sj = new StringJoiner(SystemConstant.COMMA);
        returnGoodsInfoInList.forEach(returnGoodsInfoIn -> {
            this.checkNum(sj, returnGoodsInfoIn);
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setGoodsCode(returnGoodsInfoIn.getGoodsCode());
            orderGoodsIn.setStoreCode(saveReturnOrderIn.getStoreCode());
            orderGoodsIn.setBizOrgCode(channelBizOrgCode);
            if (Objects.isNull(saveReturnOrderIn.getReturnNoticeOrderId())) {
                orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
            } else {
                orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS_FAST.getType());
            }

            OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoodsForBackReturn(orderGoodsIn);
            if (Objects.isNull(orderGoodsOut)) {
                sj.add(returnGoodsInfoIn.getGoodsCode() + "不允许配销退货");
            }
            if (Objects.nonNull(orderGoodsOut) && Objects.nonNull(orderGoodsOut.getDistributionUnitPrice())) {
//                returnGoodsInfoIn.setReturnUnitPrice(orderGoodsOut.getDistributionUnitPrice());
                returnGoodsInfoIn.setDistributionPrice(orderGoodsOut.getDistributionUnitPrice());
                returnGoodsInfoIn.setInvoiceType(orderGoodsOut.getInvoiceType());
            }
        });
        return sj.toString();
    }

    /**
     * 数量校验
     *
     * @param sj
     * @param returnGoodsInfoIn
     */
    private void checkNum(StringJoiner sj, OrdDisReturnDetail returnGoodsInfoIn) {
//        if (null == returnGoodsInfoIn.getActualReturnQuantity()) {
//            throw new BusinessException("实际退货数量不能为空");
//        }
//        if (null == returnGoodsInfoIn.getActualPackageQuantity()) {
//            throw new BusinessException("实际退货包装数不能为空");
//        }
        if (null == returnGoodsInfoIn.getApplyReturnQuantity()) {
            throw new BusinessException("申请退货数量不能为空");
        }
        if (null == returnGoodsInfoIn.getApplyPackageQuantity()) {
            throw new BusinessException("申请退货包装数不能为空");
        }
//        if (null == returnGoodsInfoIn.getAuditReturnQuantity()) {
//            throw new BusinessException("审核退货数量不能为空");
//        }
//        if (null == returnGoodsInfoIn.getAuditPackageQuantity()) {
//            throw new BusinessException("审核退货包装数不能为空");
//        }
//        if (returnGoodsInfoIn.getActualReturnQuantity().compareTo(returnGoodsInfoIn.getAuditReturnQuantity()) > 1) {
//            sj.add("实际退货数量不能大于审核退货数量");
//        }
//        if (returnGoodsInfoIn.getActualReturnQuantity().compareTo(returnGoodsInfoIn.getApplyReturnQuantity()) > 1) {
//            sj.add("实际退货数量不能大于申请退货数量");
//        }
//        if (returnGoodsInfoIn.getAuditReturnQuantity().compareTo(returnGoodsInfoIn.getApplyReturnQuantity()) > 1) {
//            sj.add("审核退货数量不能大于申请退货数量");
//        }
       /* if (!isIntegerValue(returnGoodsInfoIn.getApplyPackageQuantity())) {
            sj.add("申请退货包装数不能为小数");
        }
        if (!isIntegerValue(returnGoodsInfoIn.getAuditPackageQuantity())) {
            sj.add("审核退货包装数不能为小数");
        }
        if (!isIntegerValue(returnGoodsInfoIn.getActualPackageQuantity())) {
            sj.add("实际退货包装数不能为小数");
        }*/
        if (returnGoodsInfoIn.getApplyPackageQuantity().compareTo(new BigDecimal(NumberUtil.INTEGER_ZERO)) == -1) {
            sj.add("申请退货数量不能为负数");
        }
//        if (returnGoodsInfoIn.getAuditReturnQuantity().compareTo(new BigDecimal(NumberUtil.INTEGER_ZERO)) == -1) {
//            sj.add("审核退货数量不能为负数");
//        }
//        if (returnGoodsInfoIn.getActualReturnQuantity().compareTo(new BigDecimal(NumberUtil.INTEGER_ZERO)) == -1) {
//            sj.add("实际退货数量不能为负数");
//        }
    }


//    @Override
//    public void releaseStockAndFund(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> ordDisReturnDetailList) {
//        //异步调仓储库存 -
//        StockFlowIn wareFlowIn = initWarehouseStockCharge(ordDisReturn, ordDisReturnDetailList);
//        Response stockFlow = syncOrdDisOrderHandle.checkStockFlow(Collections.singletonList(wareFlowIn));
//        if (!stockFlow.isSuccess()){
//            throw new BusinessException(stockFlow.getMessage());
//        }
//        //资金
//        toFund(ordDisReturn);
//    }
//
//
//    private void toFund(OrdDisReturn ordDisReturn) {
//        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
//        rechargeLiquidationIn.setRecipientPrincipalCode(ordDisReturn.getStoreCode());
//        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
//        rechargeLiquidationIn.setBizOrgCode(ordDisReturn.getBizOrgCode());
//        rechargeLiquidationIn.setBusinessNo(ordDisReturn.getReturnOrderNo());
//        rechargeLiquidationIn.setBusinessType(FundTypeEnum.DISTRIBUTION_RETURN.getCode());
//        rechargeLiquidationIn.setLiquidationAmount(ordDisReturn.getActualReturnAmount());
//        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
//        if (!response.isSuccess()){
//            throw new BusinessException(response.getMessage());
//        }
//    }


    /**
     * 新增红冲退货单
     *
     * @param newOrdDisReturn
     * @param returnDetails
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveChargeOrdDisReturnDetail(OrdDisReturn newOrdDisReturn, List<OrdDisReturnDetail> returnDetails) {
        //退货详情单
        returnDetails.forEach(returnDetail -> {
            returnDetail.setReturnOrderId(newOrdDisReturn.getId());
            returnDetail.setId(null);
            returnDetail.setApplyReturnQuantity(Objects.nonNull(returnDetail.getApplyReturnQuantity()) ? returnDetail.getApplyReturnQuantity().negate() : null);
            returnDetail.setApplyReturnAmount(Objects.nonNull(returnDetail.getApplyReturnAmount()) ? returnDetail.getApplyReturnAmount().negate() : null);
            returnDetail.setApplyPackageQuantity(Objects.nonNull(returnDetail.getApplyPackageQuantity()) ? returnDetail.getApplyPackageQuantity().negate() : null);
            returnDetail.setAuditReturnQuantity(Objects.nonNull(returnDetail.getAuditReturnQuantity()) ? returnDetail.getAuditReturnQuantity().negate() : null);
            returnDetail.setAuditReturnAmount(Objects.nonNull(returnDetail.getAuditReturnAmount()) ? returnDetail.getAuditReturnAmount().negate() : null);
            returnDetail.setAuditPackageQuantity(Objects.nonNull(returnDetail.getAuditPackageQuantity()) ? returnDetail.getAuditPackageQuantity().negate() : null);
            returnDetail.setActualReturnQuantity(Objects.nonNull(returnDetail.getActualReturnQuantity()) ? returnDetail.getActualReturnQuantity().negate() : null);
            returnDetail.setActualReturnAmount(Objects.nonNull(returnDetail.getActualReturnAmount()) ? returnDetail.getActualReturnAmount().negate() : null);
            returnDetail.setActualPackageQuantity(Objects.nonNull(returnDetail.getActualPackageQuantity()) ? returnDetail.getActualPackageQuantity().negate() : null);
            returnDetail.setReturnTaxAmount(Objects.nonNull(returnDetail.getReturnTaxAmount()) ? returnDetail.getReturnTaxAmount().negate() : null);
            returnDetail.setReturnExceptTaxAmount(Objects.nonNull(returnDetail.getReturnExceptTaxAmount()) ? returnDetail.getReturnExceptTaxAmount().negate() : null);
            returnDetail.setWrhCostAmount(Objects.nonNull(returnDetail.getWrhCostAmount()) ? returnDetail.getWrhCostAmount().negate() : null);
            returnDetail.setWrhExceptTaxAmount(Objects.nonNull(returnDetail.getWrhExceptTaxAmount().negate()) ? returnDetail.getWrhExceptTaxAmount().negate() : null);
            returnDetail.setWrhTaxAmount(Objects.nonNull(returnDetail.getWrhTaxAmount()) ? returnDetail.getWrhTaxAmount().negate() : null);
            returnDetail.setStoreTaxAmount(Objects.nonNull(returnDetail.getStoreTaxAmount()) ? returnDetail.getStoreTaxAmount().negate() : null);
            returnDetail.setStoreExceptTaxAmount(Objects.nonNull(returnDetail.getStoreExceptTaxAmount()) ? returnDetail.getStoreExceptTaxAmount().negate() : null);
            returnDetail.setStoreCostAmount(Objects.nonNull(returnDetail.getStoreCostAmount()) ? returnDetail.getStoreCostAmount().negate() : null);
            returnDetail.setCreator(newOrdDisReturn.getCreator());
            returnDetail.setCreateTime(LocalDateTime.now());
        });

        ordDisReturnDetailService.batchSave(returnDetails);
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_RETURN_SAVE.getCode(),
                String.valueOf(newOrdDisReturn.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN_SAVE.name(),
                OrdLogTypeEnum.ORD_DIS_RETURN_SAVE.getName(), new Date(), newOrdDisReturn.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 冲销资金变动 调用资管
     *
     * @param ordDisReturn
     */
    private void chargeReturnOrderToFund(OrdDisReturn ordDisReturn, String newReturnOrderNo) {
        if (BigDecimal.ZERO.compareTo(ordDisReturn.getActualReturnAmount()) == 0) {
            return;
        }
        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
        rechargeLiquidationIn.setBizOrgCode(ordDisReturn.getBizOrgCode());
        rechargeLiquidationIn.setBusinessType(FundTypeEnum.DIS_RETURN_ORDER_PAY.getCode());
        rechargeLiquidationIn.setBusinessNo(newReturnOrderNo);
        rechargeLiquidationIn.setLiquidationAmount(ordDisReturn.getActualReturnAmount().abs());
        rechargeLiquidationIn.setPayOrPrincipalCode(ordDisReturn.getStoreCode());
        rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.STORE.getCode());
        rechargeLiquidationIn.setRecipientPrincipalCode(ordDisReturn.getBizOrgCode());
        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
        rechargeLiquidationIn.setDirection(FundDirectionEnum.PAY.getCode());
        rechargeLiquidationIn.setOriginalBusinessNo(ordDisReturn.getSourceReturnNo());
        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), MessageFormat.format(DisOrdReturnOrderLogEnum.ORD_DIS_RETURN_PROCESSED_PAY_AMOUNT.getValue(),
                ordDisReturn.getActualReturnAmount().abs()), new Date(), ordDisReturn.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 收货资金变动 调用资管
     *
     * @param ordDisReturn
     */
    @Override
    public void receivingReturnOrderToFund(OrdDisReturn ordDisReturn) {
        if (BigDecimal.ZERO.compareTo(ordDisReturn.getActualReturnAmount()) == 0) {
            return;
        }
        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
        rechargeLiquidationIn.setBizOrgCode(ordDisReturn.getBizOrgCode());
        rechargeLiquidationIn.setBusinessType(FundTypeEnum.DIS_RETURN_ORDER_RETURN.getCode());
        rechargeLiquidationIn.setBusinessNo(ordDisReturn.getReturnOrderNo());
        rechargeLiquidationIn.setLiquidationAmount(ordDisReturn.getActualReturnAmount().abs());
        rechargeLiquidationIn.setRecipientPrincipalCode(ordDisReturn.getStoreCode());
        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
        rechargeLiquidationIn.setPayOrPrincipalCode(ordDisReturn.getBizOrgCode());
        rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
        rechargeLiquidationIn.setOriginalBusinessNo(ordDisReturn.getReturnOrderNo());
        rechargeLiquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
        rechargeLiquidationIn.setRemark(FundReturnTypeEnum.DIS_RETURN_ORDER_RECEIVE.getName());
        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
    }


    /**
     * 库存调整
     *
     * @param ordDisReturn
     * @param returnDetails
     */
    @Override
    public void adjustInv(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> returnDetails, StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        List<StockFlowIn> stockFlowIns = Lists.newArrayList();
        //异步调门店库存
        StockFlowIn storeFlowIn = this.initStoreStockCharge(ordDisReturn, returnDetails, flowDate);
        stockFlowIns.add(storeFlowIn);
        // 天岁接入ERP，不再对接中科接口
//        boolean isAbutmentWms = stockServer.isAbutmentWms(ordDisReturn.getStockCode(), ordDisReturn.getBizOrgCode());
//        if (isAbutmentWms) {
        //异步调仓储库存
        StockFlowIn wareFlowIn = this.initWarehouseStockCharge(ordDisReturn, returnDetails, stockInfoOut, flowDate);
        stockFlowIns.add(wareFlowIn);
//        }
        Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
        if (!stockFlow.isSuccess()) {
            throw new BusinessException("库存调整失败");
        }
    }

    /**
     * 异步调仓储库存
     *
     * @param ordDisReturn
     * @param returnDetails
     * @return
     */
    private StockFlowIn initWarehouseStockCharge(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> returnDetails, StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_RETREAT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_RETREAT.getName());
        stockFlowIn.setFlowDate(flowDate);
        stockFlowIn.setCreator(ordDisReturn.getCreator());
        stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        //库存发生位置
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        stockFlowIn.setSourceNo(ordDisReturn.getReturnOrderNo());
        List<StockFlowGoodsIn> stockFlowGoodsInList = new ArrayList<>();
        returnDetails.forEach(returnDetail -> {
            BigDecimal sell = Objects.isNull(returnDetail.getSellTax()) ? BigDecimal.ZERO : returnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(returnDetail, stockFlowGoodsIn);
            stockFlowGoodsIn.setWarehouseCode(ordDisReturn.getWrhCode());
            stockFlowGoodsIn.setStockCode(ordDisReturn.getStockCode());
            stockFlowGoodsIn.setSourceNo(ordDisReturn.getReturnOrderNo());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setPrice(returnDetail.getReturnUnitPrice());

            //已收货,实际数量增加
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDisReturn.getReturnStatus()) && NumberUtil.INTEGER_ZERO.equals(ordDisReturn.getIsReversalOrder())) {
                stockFlowIn.setOperationType(OrderTypeEnum.COLLECTED.getCode());
                //财务     ---实际数
                stockFlowGoodsIn.setActualQty(returnDetail.getActualReturnQuantity().abs());
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
                stockFlowGoodsIn.setIsBusinessQty("Y");
                //配销退已收货，仓储库存调整发生价取最新门店配货价
//                stockFlowGoodsIn.setPrice(returnDetail.getDistributionPrice());
            }
            //已收货后,冲销实际数量
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDisReturn.getReturnStatus()) && NumberUtil.INTEGER_ONE.equals(ordDisReturn.getIsReversalOrder())) {
                stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
                //财务     ---实际数
                stockFlowGoodsIn.setActualQty(returnDetail.getActualReturnQuantity().abs());
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
                stockFlowGoodsIn.setIsBusinessQty("Y");
                //配销退已收货后冲销，仓储库存调整发生价取原单门店配货价
//                stockFlowGoodsIn.setPrice(returnDetail.getDistributionPrice());
            }
            //收货或冲销原单据和冲销单据正负值和调库存一致，直接取原单值
            //成本含税金额,取原单门店配送价
            stockFlowGoodsIn.setCostTaxAmount(returnDetail.getActualReturnAmount());
            //成本不含税金额
            stockFlowGoodsIn.setCostNonTaxAmount(returnDetail.getWrhExceptTaxAmount());
            //成本税额
            stockFlowGoodsIn.setCostTax(returnDetail.getWrhTaxAmount());
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(returnDetail.getReturnExceptTaxAmount());
            //含税金额
//            stockFlowGoodsIn.setTaxAmount(returnDetail.getReturnExceptTaxAmount().multiply(tar).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            stockFlowGoodsIn.setTaxAmount(returnDetail.getActualReturnAmount());
            //税额
            stockFlowGoodsIn.setTax(returnDetail.getReturnTaxAmount());
            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
            stockFlowGoodsInList.add(stockFlowGoodsIn);
        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsInList);
        return stockFlowIn;
    }

    /**
     * 异步调门店库存
     *
     * @param ordDisReturn
     * @param returnDetails
     * @return
     */
    private StockFlowIn initStoreStockCharge(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> returnDetails, LocalDateTime flowDate) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(ordDisReturn.getBizOrgCode());
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_RETREAT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_RETREAT.getName());
        stockFlowIn.setCreator(ordDisReturn.getCreator());
        stockFlowIn.setOrgCode(ordDisReturn.getOrgCode());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        stockFlowIn.setSourceNo(ordDisReturn.getReturnOrderNo());
        stockFlowIn.setFlowDate(flowDate);
        List<StockFlowGoodsIn> stockFlowInList = new ArrayList<>();
        returnDetails.forEach(returnDetail -> {
            BigDecimal sell = Objects.isNull(returnDetail.getSellTax()) ? BigDecimal.ZERO : returnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(returnDetail, stockFlowGoodsIn);
            stockFlowGoodsIn.setSourceNo(ordDisReturn.getReturnOrderNo());
            stockFlowGoodsIn.setStoreCode(ordDisReturn.getStoreCode());
            stockFlowGoodsIn.setStoreName(ordDisReturn.getStoreName());
            stockFlowGoodsIn.setStockCode(ordDisReturn.getStockCode());
            stockFlowGoodsIn.setPrice(returnDetail.getReturnUnitPrice());

            //已审核状态,批准数量减少
            if (OrdReturnOrderStatusEnum.APPROVED.getKey().equals(ordDisReturn.getReturnStatus())) {
                stockFlowIn.setOperationType(OrderTypeEnum.APPROVED.getCode());
                //占用    ---退货审核数
                stockFlowGoodsIn.setApplyQty(returnDetail.getAuditReturnQuantity().abs());
                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.ADD.getCode());

                //成本不含税金额
                stockFlowGoodsIn.setCostNonTaxAmount(returnDetail.getStoreExceptTaxAmount());
                //成本含税金额
                stockFlowGoodsIn.setCostTaxAmount(returnDetail.getStoreCostAmount());
                //成本税额
                stockFlowGoodsIn.setCostTax(returnDetail.getStoreTaxAmount());
                //不含税金额
                stockFlowGoodsIn.setNonTaxAmount(returnDetail.getReturnExceptTaxAmount());
                //含税金额
                stockFlowGoodsIn.setTaxAmount(returnDetail.getReturnExceptTaxAmount().multiply(tar).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                // 税额
                stockFlowGoodsIn.setTax(returnDetail.getReturnTaxAmount());
            }
            //已作废,批准数量增加
            if (OrdReturnOrderStatusEnum.INVALID.getKey().equals(ordDisReturn.getReturnStatus())) {
                stockFlowIn.setOperationType(OrderTypeEnum.INVALID.getCode());
                //审核增/减
                stockFlowGoodsIn.setApplyQty((Objects.nonNull(returnDetail.getAuditReturnQuantity()) ? returnDetail.getAuditReturnQuantity() : returnDetail.getApplyReturnQuantity()).abs());
                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());

                //成本不含税金额
                stockFlowGoodsIn.setCostNonTaxAmount(returnDetail.getStoreExceptTaxAmount().negate());
                //成本含税金额
                stockFlowGoodsIn.setCostTaxAmount(returnDetail.getStoreCostAmount().negate());
                //成本税额
                stockFlowGoodsIn.setCostTax(returnDetail.getStoreTaxAmount().negate());
                //不含税金额
                stockFlowGoodsIn.setNonTaxAmount(returnDetail.getReturnExceptTaxAmount().negate());
                //含税金额
                stockFlowGoodsIn.setTaxAmount(returnDetail.getReturnExceptTaxAmount().multiply(tar).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP).negate());
                // 税额
                stockFlowGoodsIn.setTax(returnDetail.getReturnTaxAmount().negate());
            }
            //已收货,实际数量减少
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDisReturn.getReturnStatus()) && NumberUtil.INTEGER_ZERO.equals(ordDisReturn.getIsReversalOrder())) {
                stockFlowIn.setOperationType(OrderTypeEnum.COLLECTED.getCode());
                //财务   ---实际数
                stockFlowGoodsIn.setActualQty(returnDetail.getActualReturnQuantity().abs());
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
                //占用   ---实际数
                stockFlowGoodsIn.setApplyQty(returnDetail.getAuditReturnQuantity().abs());
                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
                //成本含税金额
                stockFlowGoodsIn.setCostTaxAmount(returnDetail.getStoreCostAmount());
                //成本不含税金额
                stockFlowGoodsIn.setCostNonTaxAmount(returnDetail.getStoreExceptTaxAmount());
                //成本税额
                stockFlowGoodsIn.setCostTax(returnDetail.getStoreTaxAmount());
                //不含税金额
                stockFlowGoodsIn.setNonTaxAmount(returnDetail.getReturnExceptTaxAmount().negate());
                //含税金额
//                stockFlowGoodsIn.setTaxAmount(returnDetail.getReturnExceptTaxAmount().multiply(tar).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP).negate());
                stockFlowGoodsIn.setTaxAmount(returnDetail.getActualReturnAmount().negate());
                // 税额
                stockFlowGoodsIn.setTax(returnDetail.getReturnTaxAmount().negate());
                //退货收货门店库存调整发生价取最新门店库存价
                stockFlowGoodsIn.setPrice(returnDetail.getStoreStockPrice());
                if (StringUtils.isNotBlank(returnDetail.getExpiry())) {
                    stockFlowGoodsIn.setGoodsExpiryFlowList(Lists.newArrayList(new StockFlowGoodsExpiryIn(returnDetail.getExpiry(), stockFlowGoodsIn.getActualQty())));
                }
            }

            //已收货后,冲销实际数量增加
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDisReturn.getReturnStatus()) && NumberUtil.INTEGER_ONE.equals(ordDisReturn.getIsReversalOrder())) {
                stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
                //财务     ---实际数
                stockFlowGoodsIn.setActualQty(returnDetail.getActualReturnQuantity().abs());
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
                //成本不含税金额
                stockFlowGoodsIn.setCostNonTaxAmount(returnDetail.getStoreExceptTaxAmount());
                //成本含税金额
                stockFlowGoodsIn.setCostTaxAmount(returnDetail.getStoreCostAmount());
                //成本税额
                stockFlowGoodsIn.setCostTax(returnDetail.getStoreTaxAmount());
                //不含税金额
                stockFlowGoodsIn.setNonTaxAmount(returnDetail.getReturnExceptTaxAmount().negate());
                //含税金额
//                stockFlowGoodsIn.setTaxAmount(returnDetail.getReturnExceptTaxAmount().multiply(tar).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP).negate());
                stockFlowGoodsIn.setTaxAmount(returnDetail.getActualReturnAmount().negate());
                // 税额
                stockFlowGoodsIn.setTax(returnDetail.getReturnTaxAmount().negate());
                //退货收货门店库存调整发生价取原单门店库存价
                stockFlowGoodsIn.setPrice(returnDetail.getStoreStockPrice());
                if (StringUtils.isNotBlank(returnDetail.getExpiry())) {
                    stockFlowGoodsIn.setGoodsExpiryFlowList(Lists.newArrayList(new StockFlowGoodsExpiryIn(returnDetail.getExpiry(), stockFlowGoodsIn.getActualQty())));
                }
            }

            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.STORE.getCode());
            stockFlowInList.add(stockFlowGoodsIn);
        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowInList);
        return stockFlowIn;
    }

    /**
     * 校验仓储库存
     *
     * @param ordDisReturn
     * @param returnDetails
     * @return
     */
    private String checkInvStock(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> returnDetails) {
        StringJoiner sj = new StringJoiner(SystemConstant.COMMA);
        if (CollectionUtils.isNotEmpty(returnDetails)) {
            returnDetails.forEach(returnDetail -> {
                BigDecimal invNum = ordDisReturnMapper.getInvNum(returnDetail.getGoodsCode(), ordDisReturn.getStockCode(), ordDisReturn.getWrhCode());
                if (Objects.isNull(invNum) || NumberUtil.INTEGER_ZERO > invNum.compareTo(returnDetail.getActualReturnQuantity())) {
                    sj.add("商品【" + returnDetail.getGoodsCode() + "】" + returnDetail.getGoodsName() + "仓储可用库存不足");
                }
            });
        }
        if (StringUtils.isNotEmpty(sj.toString())) {
            sj.toString();
        }
        return null;
    }

    /**
     * 校验门店库存
     *
     * @param ordDisReturn
     * @param returnDetails
     * @return
     */
    private String checkInvStore(OrdDisReturn ordDisReturn, List<OrdDisReturnDetail> returnDetails) {
        if (stockStoreService.checkStockIsAllowNegative(ordDisReturn.getStoreCode(), ordDisReturn.getBizOrgCode())) {
            return null;
        }
        StringJoiner sj = new StringJoiner(SystemConstant.COMMA);
        if (CollectionUtils.isNotEmpty(returnDetails)) {
            returnDetails.forEach(returnDetail -> {
                BigDecimal invNum = ordDisReturnMapper.getInvStoreNum(returnDetail.getGoodsCode(), ordDisReturn.getBizOrgCode(), ordDisReturn.getStoreCode());
                if (Objects.isNull(invNum) || NumberUtil.INTEGER_ZERO > invNum.compareTo(returnDetail.getAuditReturnQuantity())) {
                    sj.add("商品【" + returnDetail.getGoodsCode() + "】" + returnDetail.getGoodsName() + "门店可用库存不足");
                }
            });
        }
        if (StringUtils.isNotEmpty(sj.toString())) {
            return sj.toString();
        }
        return null;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response saveReturnOrderByReturnNotice(AppDisReturnOrderSaveIn appDisReturnOrderSaveIn) {
        Integer returnOrderId = appDisReturnOrderSaveIn.getReturnOrderId();
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(appDisReturnOrderSaveIn.getStoreCode());
        if (null == returnOrderId) {
            //第一次
            this.saveReturnOrderData(appDisReturnOrderSaveIn, storeOut);
        } else {
            OrdDisReturn ordDisReturn = this.getReturnOrderByIdAndOrgCode(returnOrderId, appDisReturnOrderSaveIn.getBizOrgCode());
            ordDisReturnDetailService.deleteByReturnOrderId(ordDisReturn);
            //非第一次
            List<AppSaveOrdDisReturnDetailIn> ordDisReturnDetails = this.initReturnOrderDetailList(appDisReturnOrderSaveIn, storeOut, ordDisReturn);
            ordDisReturn.setUpdater(storeOut.getStoreCode());
            ordDisReturn.setUpdateTime(LocalDateTime.now());
            ordDisReturn.setSkuCount(ordDisReturnDetails.size());
            ordDisReturn.setApplyReturnQuantity(ordDisReturnDetails.stream().filter(detail -> Objects.nonNull(detail.getApplyReturnQuantity())).map(OrdDisReturnDetail::getApplyReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            ordDisReturn.setApplyReturnAmount(ordDisReturnDetails.stream().filter(detail -> Objects.nonNull(detail.getApplyReturnAmount())).map(OrdDisReturnDetail::getApplyReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            ordDisReturn.setAuditReturnQuantity(ordDisReturnDetails.stream().filter(detail -> Objects.nonNull(detail.getAuditReturnQuantity())).map(OrdDisReturnDetail::getAuditReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            ordDisReturn.setAuditReturnAmount(ordDisReturnDetails.stream().filter(detail -> Objects.nonNull(detail.getAuditReturnAmount())).map(OrdDisReturnDetail::getAuditReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            ordDisReturn.setAppRemark(appDisReturnOrderSaveIn.getAppRemark());
            ordDisReturn.setDeliveryOrderNo(appDisReturnOrderSaveIn.getDeliveryOrderNo());
            ordDisReturnMapper.updateByPrimaryKey(ordDisReturn);
        }
        return Response.success("保存成功");
    }

    /**
     * @Description: 收货
     * @Author: ZhangYao
     * @Date: 2024/3/30 16:55
     * @param returnGoodsInfoInList:
     * @param returnOrder:
     * @param stockInfoOut:
     * @return: com.edc.plugins.common.response.Response
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response receiving(List<OrdDisReturnDetail> returnGoodsInfoInList, OrdDisReturn returnOrder, StockInfoOut stockInfoOut) {
        String key = DisSystemConstant.DIS_CHECK_RETURN_RECEIVING + returnOrder.getBizOrgCode() +
                SystemConstant.COLON + returnOrder.getStoreCode() + SystemConstant.WAIT + returnOrder.getReturnOrderNo();
        if (!redisService.setIfAbsent(key, returnOrder.getReturnOrderNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("门店" + returnOrder.getStoreCode() + "短时间内异常重复收货" + returnOrder.getReturnOrderNo() + "，故判为无效提交！");
        }
        List<OrdDisReturnDetail> list = new ArrayList<>();
        // 批准退货总数量
        BigDecimal totalActualReturnQuantity = BigDecimal.ZERO;
        // 批准退货总金额
        BigDecimal totalActualReturnAmount = BigDecimal.ZERO;
        for (OrdDisReturnDetail returnGoodsInfoIn : returnGoodsInfoInList) {
            BigDecimal tax = returnGoodsInfoIn.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE)).add(BigDecimal.ONE);

            OrdDisReturnDetail ordDisReturnDetail = ordDisReturnDetailService.selectByPrimaryKey(returnGoodsInfoIn.getId());
            ordDisReturnDetail.setActualReturnQuantity(returnGoodsInfoIn.getActualReturnQuantity());
            ordDisReturnDetail.setActualPackageQuantity(returnGoodsInfoIn.getActualReturnQuantity().divide(ordDisReturnDetail.getDistributionSpecificationNum(),
                    NumberUtil.INTEGER_ZERO, RoundingMode.UP));
//            ordDisReturnDetail.setActualReturnAmount(ordDisReturnDetail.getReturnUnitPrice().multiply(returnGoodsInfoIn.getActualReturnQuantity()));

            //最新门店商品库存价
            BigDecimal stockPrice = warehouseServer.getStockPrice(returnOrder.getStoreCode(), returnGoodsInfoIn.getGoodsCode(), returnOrder.getBizOrgCode());
            if (Objects.isNull(stockPrice)) {
                throw new BusinessException("门店商品" + ordDisReturnDetail.getGoodsCode() + "库存价为空");
            }
            ordDisReturnDetail.setActualReturnAmount(stockPrice.multiply(returnGoodsInfoIn.getActualReturnQuantity()));
            //退货去税金额
            ordDisReturnDetail.setReturnExceptTaxAmount(ordDisReturnDetail.getActualReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //退货税额
            ordDisReturnDetail.setReturnTaxAmount(ordDisReturnDetail.getActualReturnAmount().subtract(ordDisReturnDetail.getReturnExceptTaxAmount()));
//            //获取最新门店配货价
//            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//            orderGoodsIn.setGoodsCode(returnGoodsInfoIn.getGoodsCode());
//            orderGoodsIn.setBizOrgCode(returnOrder.getBizOrgCode());
//            orderGoodsIn.setStoreCode(returnOrder.getStoreCode());
//            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
//            OrderGoodsOut orderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
//            //退货单已收货取最新门店配货价
//            if (Objects.nonNull(orderGoods) && Objects.nonNull(orderGoods.getDistributionUnitPrice())) {
//                ordDisReturnDetail.setWrhPrice(orderGoods.getDistributionUnitPrice());
//                //配送价
//                ordDisReturnDetail.setDistributionPrice(orderGoods.getDistributionUnitPrice());
//            }
            //仓储成本金额
            BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(returnOrder.getWrhCode(), returnOrder.getStockCode(),
                    ordDisReturnDetail.getGoodsCode(), stockInfoOut.getBizOrgCode(), returnOrder.getStoreCode(), returnOrder.getBizOrgCode());
            if (Objects.isNull(warehousePrice)) {
                log.error("统配退货单{}商品{}仓储库存价为空", returnOrder.getReturnOrderNo(), ordDisReturnDetail.getGoodsCode());
                throw new BusinessException("退货单" + returnOrder.getReturnOrderNo() + "商品" + ordDisReturnDetail.getGoodsCode() + "仓储库存价为空");
            }
            ordDisReturnDetail.setWrhPrice(warehousePrice);
//            BigDecimal wrhCostAmount = ordDisReturnDetail.getWrhPrice().multiply(returnGoodsInfoIn.getActualReturnQuantity());
            ordDisReturnDetail.setWrhCostAmount(ordDisReturnDetail.getActualReturnAmount());
            //仓储成本去税金额
            ordDisReturnDetail.setWrhExceptTaxAmount(ordDisReturnDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //仓储成本税额
            ordDisReturnDetail.setWrhTaxAmount(ordDisReturnDetail.getWrhCostAmount().subtract(ordDisReturnDetail.getWrhExceptTaxAmount()));
            ordDisReturnDetail.setStoreStockPrice(stockPrice);
//            BigDecimal stockPrice = ordDisReturnDetail.getStoreStockPrice();
            ordDisReturnDetail.setStoreStockPrice(stockPrice);
            //门店成本金额,退货单已收货门店财务库存减少。门店相关为负值
            ordDisReturnDetail.setStoreCostAmount((Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice).multiply(returnGoodsInfoIn.getActualReturnQuantity()).negate());
            //门店成本去税金额
            ordDisReturnDetail.setStoreExceptTaxAmount(ordDisReturnDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //门店成本税额
            ordDisReturnDetail.setStoreTaxAmount(ordDisReturnDetail.getStoreCostAmount().subtract(ordDisReturnDetail.getStoreExceptTaxAmount()));


            totalActualReturnAmount = totalActualReturnAmount.add(ordDisReturnDetail.getActualReturnAmount());
            totalActualReturnQuantity = totalActualReturnQuantity.add(ordDisReturnDetail.getActualReturnQuantity());
            list.add(ordDisReturnDetail);
        }

        returnOrder.setActualReturnAmount(totalActualReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        returnOrder.setActualReturnQuantity(totalActualReturnQuantity);
        ordDisReturnDetailService.batchUpdate(list);
        returnOrder.setReturnStatus(OrdReturnOrderStatusEnum.PROCESSED.getKey());
        returnOrder.setReceiveTime(LocalDateTime.now());
        ordDisReturnMapper.updateByPrimaryKeySelective(returnOrder);
        //库存调整
        this.adjustInv(returnOrder, list, stockInfoOut, returnOrder.getReceiveTime());
        //资金变动
        receivingReturnOrderToFund(returnOrder);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(returnOrder.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), MessageFormat.format(DisOrdReturnOrderLogEnum.ORD_DIS_RETURN_PROCESSED_BACK_AMOUNT.getValue(),
                returnOrder.getActualReturnAmount().abs()), new Date(), returnOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("收货成功");
    }

    @Override
    public Response<ReturnDetailOut> findOrdReturnDetails(OrdDisReturnDetailIn pageIn) {
        OrdDisReturn ordDisReturn = ordDisReturnMapper.selectByPrimaryKey(pageIn.getReturnOrderId());
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDisReturn.getStockCode(), pageIn.getBizOrgCode(), "操作配销退货单明细");
        OrdDisReturnImage ordDisReturnImage = new OrdDisReturnImage();
        ordDisReturnImage.setReturnOrderId(pageIn.getReturnOrderId());
        List<OrdDisReturnImage> allImageList = ordDisReturnImageMapper.select(ordDisReturnImage);
        Map<Integer, List<String>> detailImagesMap = new HashMap<>();
        allImageList.forEach(returnImage -> {
            List<String> imageList = detailImagesMap.get(returnImage.getReturnDetailId());
            if (CollectionUtils.isEmpty(imageList)) {
                imageList = Lists.newArrayList();
            }
            imageList.add(returnImage.getImage_url());
            detailImagesMap.put(returnImage.getReturnDetailId(), imageList);
        });
        List<OrdDisReturnDetailOut> ordDisReturnDetailOutList = ordDisReturnDetailService.finaOrdReturnDetail(pageIn);
        ordDisReturnDetailOutList.forEach(ordDisReturnDetailOut -> {
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setStoreCode(ordDisReturn.getStoreCode());
            orderGoodsIn.setGoodsCode(ordDisReturnDetailOut.getGoodsCode());
            orderGoodsIn.setBizOrgCode(ordDisReturn.getBizOrgCode());
            OrderGoodsOut orderGoods = orderGoodsServer.getStoreGoodsNoType(orderGoodsIn);
            ordDisReturnDetailOut.setReturnPrinciple(Objects.isNull(orderGoods) ? "" : orderGoods.getReturnPrinciple());
            BigDecimal stockNum = warehouseServer.getStockNum(ordDisReturn.getStoreCode(), ordDisReturnDetailOut.getGoodsCode(), ordDisReturn.getBizOrgCode());
            ordDisReturnDetailOut.setStoreInventory(stockNum);
            if (Objects.isNull(orderGoods)) {
                throw new BusinessException("门店商品" + ordDisReturnDetailOut.getGoodsCode() + "不存在");
            } else {
                ordDisReturnDetailOut.setIsManageValidityPeriod(orderGoods.getIsManageValidityPeriod());
            }
            if (StringUtils.isNotEmpty(ordDisReturnDetailOut.getReturnReason())) {
                InvBizRsnTransOut invBizRsnTransOut = equipmentBusinessReasonServer.getStoreInvBizRsnTransByCode(ordDisReturnDetailOut.getReturnReason(), ordDisReturn.getBizOrgCode(), NumberUtil.INTEGER_ZERO);
                ordDisReturnDetailOut.setReturnReasonValue(Objects.isNull(invBizRsnTransOut) ? ordDisReturnDetailOut.getReturnReason() : invBizRsnTransOut.getBusinessReasonName());
            }
            ordDisReturnDetailOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(ordDisReturnDetailOut.getGoodsType()));
            ordDisReturnDetailOut.setImageList(detailImagesMap.get(ordDisReturnDetailOut.getId()));
            ordDisReturnDetailOut.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(ordDisReturnDetailOut.getInvoiceType()));
        });
        ReturnDetailOut returnDetailOut = new ReturnDetailOut();
        BeanUtils.copy(ordDisReturn, returnDetailOut);
        returnDetailOut.setStockName(Objects.isNull(stockInfoOut) ? "" : stockInfoOut.getStockName());
        OrdDisReturnNotice ordDisReturnNotice = ordDisReturnNoticeService.getReturnNoticeOrderById(ordDisReturn.getReturnNoticeOrderId());
        returnDetailOut.setReturnNoticeOrderNo(Objects.isNull(ordDisReturnNotice) ? "" : ordDisReturnNotice.getReturnNoticeOrderNo());
        returnDetailOut.setReturnOrderId(pageIn.getReturnOrderId());
        returnDetailOut.setReturnStatusValue(OrdReturnOrderStatusEnum.getValueByKey(ordDisReturn.getReturnStatus()));
        returnDetailOut.setReturnTypeValue(OrdReturnOrderTypeEnum.getValueByKey(ordDisReturn.getReturnType()));
        returnDetailOut.setGoodsInfo(ordDisReturnDetailOutList);
        returnDetailOut.setWarehouseCode(ordDisReturn.getWrhCode());
        if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDisReturn.getReturnStatus())) {
            returnDetailOut.setActualReturnQuantity(ordDisReturn.getActualReturnQuantity());
            returnDetailOut.setActualSkuCount(ordDisReturnDetailService.countActualReturnSkuQuantity(pageIn.getReturnOrderId()));
            returnDetailOut.setActualReturnAmount(ordDisReturn.getActualReturnAmount());
        }
        returnDetailOut.setApplyReturnQuantity(ordDisReturnDetailService.sumApplyReturnQuantity(pageIn.getReturnOrderId()));
        returnDetailOut.setApplySkuCount(ordDisReturnDetailService.countApplyReturnSkuQuantity(pageIn.getReturnOrderId()));
        returnDetailOut.setReturnOrderReasonValue(systemDictService.getSystemDictName(returnDetailOut.getReturnOrderReason()));
        return Response.data(returnDetailOut);
    }


    /**
     * 批量审核
     *
     * @param returnOrderIds
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchApproved(List<Integer> returnOrderIds) {
        int count = NumberUtil.INTEGER_ZERO;
        for (Integer item : returnOrderIds) {
            OrdDisReturn returnOrder = ordDisReturnMapper.selectByPrimaryKey(item);
            if (null == returnOrder) {
                continue;
            }
            StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(returnOrder.getStockCode(), UserUtil.getBizOrgCode(), "审核配销退货单");
            String key = DisSystemConstant.CHECK_DIS_RETURN_ORDER_AUDIT + returnOrder.getBizOrgCode() +
                    SystemConstant.COLON + returnOrder.getStoreCode() + SystemConstant.WAIT + returnOrder.getReturnOrderNo();
            if (!redisService.setIfAbsent(key, returnOrder.getReturnOrderNo(), 1L, TimeUnit.MINUTES)) {
                log.error("门店{}短时间内重复审核{}，故判为无效提交！", returnOrder.getStoreCode(), returnOrder.getReturnOrderNo());
                continue;
            }
            if (OrdReturnOrderStatusEnum.APPROVED.getKey().equals(returnOrder.getReturnStatus())) {
                continue;
            }
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(returnOrder.getReturnStatus())) {
                continue;
            }
            List<OrdDisReturnDetail> details = ordDisReturnDetailService.findByReturnOrderId(item);
            if (CollectionUtils.isEmpty(details)) {
                continue;
            }
//            BigDecimal auditReturnQuantity = BigDecimal.ZERO;
//            BigDecimal auditReturnAmount = BigDecimal.ZERO;
            for (OrdDisReturnDetail detail : details) {
                detail.setAuditReturnAmount(detail.getApplyReturnAmount());
                detail.setAuditReturnQuantity(detail.getApplyReturnQuantity());
                detail.setAuditPackageQuantity(detail.getApplyPackageQuantity());
                ordDisReturnDetailService.updateByPrimaryKeySelective(OrdDisReturnDetail.builder().id(detail.getId())
                        .auditReturnAmount(detail.getApplyReturnAmount())
                        .auditReturnQuantity(detail.getApplyReturnQuantity())
                        .auditPackageQuantity(detail.getApplyPackageQuantity())
                        .build());
//                auditReturnQuantity = auditReturnQuantity.add(Objects.isNull(detail.getAuditReturnQuantity()) ? BigDecimal.ZERO : detail.getAuditReturnQuantity());
//                auditReturnAmount = auditReturnAmount.add(Objects.isNull(detail.getAuditReturnAmount()) ? BigDecimal.ZERO : detail.getAuditReturnAmount());
            }
//            returnOrder.setAuditReturnQuantity(auditReturnQuantity);
//            returnOrder.setAuditReturnAmount(auditReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
//            StockInfoOut stockInfoOut = stockServer.getTransInfo(returnOrder.getStockCode());
            this.audit(returnOrder, details, stockInfoOut.getBizOrgCode());
            count++;
        }
        return count;
    }

    /**
     * 导出退货列表
     *
     * @param ordReturnOrderPageIn
     * @return
     */
    @Override
    public String exportOrdReturn(OrdReturnOrderPageIn ordReturnOrderPageIn) {
        // 设置每次查询条数
        ordReturnOrderPageIn.setPageSize(10000);
        String title = "配销退货单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配销退货单列表",
                        // 导出模板实体
                        ExportOrdDisReturn.class,
                        // 分页查询对象
                        ordReturnOrderPageIn,
                        // 分页查询方法
                        page -> {
                            Page<BaseReturnOrderOut> baseReturnOrderForPage = this.findBaseReturnOrderForPage(ordReturnOrderPageIn);
                            List<ExportOrdDisReturn> exportOrdDirReturns = parseDataToExcel(baseReturnOrderForPage.getList());
                            log.info("导出配销退货单列表集合大小是--{}", exportOrdDirReturns.size());
                            return exportOrdDirReturns;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

//    /**
//     * 导入退货列表
//     *
//     * @param fileId
//     * @param bizOrgCode
//     * @return
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Response importOrdReturn(String fileId, String bizOrgCode) {
//        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
//        if (bytes == null) {
//            return Response.error("无效的Excel模板");
//        }
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
//        OrdReturnOrderListener listener = new OrdReturnOrderListener(orderGoodsServer, bizOrgCode, storeCenterService, stockServer, warehouseServer);
//        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnOrderVO.class, listener).headRowNumber(1).build();
//        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
//        excelReader.read(readSheet).finish();
//        if (!listener.errorList.isEmpty()) {
//            return Response.error(listener.errorEessage());
//        }
//        //数据入库
//        initData(listener, bizOrgCode);
//        return Response.success(listener.message());
//    }
//
//    private String getSplitKey(ImportOrdReturnOrderVO importOrdReturnOrderVO, String bizOrgCode) {
////        OrderGoodsOut orderGoodsOut = this.getFastReturnGoodsInfo(bizOrgCode, importOrdReturnOrderVO.getStoreCode(), importOrdReturnOrderVO.getGoodsCode());
//        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//        orderGoodsIn.setGoodsCode(importOrdReturnOrderVO.getGoodsCode());
//        orderGoodsIn.setStoreCode(importOrdReturnOrderVO.getStoreCode());
//        orderGoodsIn.setBizOrgCode(bizOrgCode);
//        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
//        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoodsForBackReturn(orderGoodsIn);
//        if (Objects.isNull(orderGoodsOut)) {
//            throw new BusinessException("商品不存在或者不允许配货退货");
//        }
////        OrderGoodsOut orderGoodsOut = this.getFastReturnGoodsInfo(bizOrgCode, importOrdReturnOrderVO.getStoreCode(), importOrdReturnOrderVO.getGoodsCode());
//        String key = importOrdReturnOrderVO.getStoreCode() + SystemConstant.COMMA + importOrdReturnOrderVO.getStockCode()
//                + SystemConstant.COMMA + importOrdReturnOrderVO.getWarehouseCode() + SystemConstant.COMMA + orderGoodsOut.getDistributionWay();
//        return key;
//    }
//
//    private void initData(OrdReturnOrderListener listener, String bizOrgCode) {
//        List<ImportOrdReturnOrderVO> importOrdReturnOrderList = listener.getImportOrdReturnOrderList();
////        Map<String, List<ImportOrdReturnOrderVO>> returnOrderInfo = importOrdReturnOrderList.stream().collect(Collectors.groupingBy(item ->
////                item.getStoreCode() + SystemConstant.COMMA + item.getStockCode() + SystemConstant.COMMA + item.getWarehouseCode()));
//        Map<String, List<ImportOrdReturnOrderVO>> returnOrderInfo = importOrdReturnOrderList.stream().collect(Collectors.groupingBy(item -> this.getSplitKey(item, bizOrgCode)));
//        for (Map.Entry<String, List<ImportOrdReturnOrderVO>> entry : returnOrderInfo.entrySet()) {
//            List<ImportOrdReturnOrderVO> value = entry.getValue();
//            String[] splits = entry.getKey().split(SystemConstant.COMMA);
//            OrdSaveReturnOrderIn ordSaveReturnOrderIn = new OrdSaveReturnOrderIn();
//            ordSaveReturnOrderIn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
//            ordSaveReturnOrderIn.setStoreCode(splits[0]);
//            ordSaveReturnOrderIn.setStockCode(splits[1]);
//            ordSaveReturnOrderIn.setWarehouseCode(splits[2]);
//            ordSaveReturnOrderIn.setDistributionType(DistributionWaysEnum.getNameByType(splits[3]));
//            ordSaveReturnOrderIn.setReturnOrderReason(OrdReturnOrderReasonEnum.PREVIOUS_RETURN.getDictVlueCode());
//            ordSaveReturnOrderIn.setBizOrgCode(bizOrgCode);
//            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(splits[0]);
//            ordSaveReturnOrderIn.setStoreName(Objects.isNull(storeOut) ? "" : storeOut.getStoreName());
//            ordSaveReturnOrderIn.setStoreArea(storeOut.getBelongArea());
//            List<OrdDisReturnDetail> ordDisReturnDetailList = new ArrayList<>();
//            //明细数据封装
//            for (ImportOrdReturnOrderVO importOrdReturnOrderVO : value) {
//                OrdDisReturnDetail ordDisReturnDetail = ordDisReturnDetailService.initDetail(importOrdReturnOrderVO, bizOrgCode);
//                ordDisReturnDetailList.add(ordDisReturnDetail);
//            }
//            ordSaveReturnOrderIn.setReturnGoodsInfoInList(ordDisReturnDetailList);
//            this.saveOrUpdateReturnOrder(ordSaveReturnOrderIn, false);
//        }
//    }

    /**
     * 退货商品信息
     *
     * @return
     */
    @Override
    public Response<SaveReturnGoodsOut> getGoodInfo(OrderGoodsIn orderGoodsIn, String centerStockBizOrgCode) {
        String channelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(orderGoodsIn.getStoreCode(), UserUtil.getBizOrgCode());
        orderGoodsIn.setBizOrgCode(channelBizOrgCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoodsForBackReturn(orderGoodsIn);
        if (Objects.isNull(orderGoodsOut)) {
            return Response.error("商品不存在或者不允许配销退货");
        }
        if (Objects.nonNull(orderGoodsIn.getWarehouseCode()) && Objects.nonNull(orderGoodsIn.getStockCode())) {
            if (!orderGoodsIn.getWarehouseCode().equals(orderGoodsOut.getReturnWarehouseCode())) {
                return Response.error(orderGoodsIn.getGoodsCode() + "商品退货仓储和所选退货仓储不符");
            }
            if (!orderGoodsIn.getStockCode().equals(orderGoodsOut.getBackStockCode())) {
                return Response.error(orderGoodsIn.getGoodsCode() + "商品退货仓位和所选仓位不符");
            }
        }
        SaveReturnGoodsOut saveReturnGoodsOut = new SaveReturnGoodsOut();
        saveReturnGoodsOut.setGoodsCode(orderGoodsOut.getGoodsCode());
        saveReturnGoodsOut.setGoodsName(orderGoodsOut.getGoodsName());
        saveReturnGoodsOut.setReturnPrinciple(orderGoodsOut.getReturnPrinciple());
        saveReturnGoodsOut.setGoodsType(orderGoodsOut.getGoodsType());
        saveReturnGoodsOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(orderGoodsOut.getGoodsType()));
        saveReturnGoodsOut.setBarCode(orderGoodsOut.getBarCode());
        saveReturnGoodsOut.setOrgGoodsId(orderGoodsOut.getOrgGoodsId());
        saveReturnGoodsOut.setVendorCode(orderGoodsOut.getVendorCode());
        saveReturnGoodsOut.setVendorName(orderGoodsOut.getVendorName());
        saveReturnGoodsOut.setDistributionType(orderGoodsOut.getDistributionWay());
        if (Objects.nonNull(orderGoodsOut.getDistributionSpecification())) {
            saveReturnGoodsOut.setDistributionSpecificationUnit(orderGoodsOut.getDistributionSpecification().getUnitName());
            saveReturnGoodsOut.setDistributionSpecification(orderGoodsOut.getDistributionSpecification().getQpcStr());
            saveReturnGoodsOut.setDistributionSpecificationNum(BigDecimal.valueOf(orderGoodsOut.getDistributionSpecification().getQpc()));
        }
        BigDecimal storeStockPrice = warehouseServer.getStockPrice(orderGoodsIn.getStoreCode(), orderGoodsIn.getGoodsCode(), channelBizOrgCode);
        saveReturnGoodsOut.setReturnUnitPrice(storeStockPrice);
//        saveReturnGoodsOut.setReturnUnitPrice(orderGoodsOut.getDistributionUnitPrice());
        saveReturnGoodsOut.setDistributionPrice(orderGoodsOut.getDistributionUnitPrice());
        saveReturnGoodsOut.setSellTax(orderGoodsOut.getOutTax());
        BigDecimal warehouseStockPrice = warehouseServer.getStockPrice(orderGoodsIn.getStoreCode(), orderGoodsIn.getGoodsCode(), channelBizOrgCode);
        saveReturnGoodsOut.setStoreStockPrice(null != warehouseStockPrice ? warehouseStockPrice : BigDecimal.ZERO);
        BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(orderGoodsIn.getWarehouseCode(), orderGoodsIn.getStockCode(),
                orderGoodsIn.getGoodsCode(), centerStockBizOrgCode, orderGoodsIn.getStoreCode(), channelBizOrgCode);
        if (Objects.isNull(warehousePrice)) {
            log.error("获取商品{}信息仓储库存价为空", orderGoodsOut.getGoodsCode());
            throw new BusinessException("获取商品" + orderGoodsOut.getGoodsCode() + "信息仓储库存价为空");
        }
        saveReturnGoodsOut.setWrhPrice(warehousePrice);
        saveReturnGoodsOut.setInvoiceType(orderGoodsOut.getInvoiceType());
        saveReturnGoodsOut.setIsManageValidityPeriod(orderGoodsOut.getIsManageValidityPeriod());
        return Response.data(saveReturnGoodsOut);
    }

    private List<ExportOrdDisReturn> parseDataToExcel(List<BaseReturnOrderOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 转换excel参数
     *
     * @param baseReturnOrderOut
     * @param i
     * @return
     */
    private ExportOrdDisReturn convertExcel(BaseReturnOrderOut baseReturnOrderOut, int i) {
        ExportOrdDisReturn exportOrdDisReturn = new ExportOrdDisReturn();
        BeanUtils.copy(baseReturnOrderOut, exportOrdDisReturn);
        exportOrdDisReturn.setReturnStatus(OrdReturnOrderStatusEnum.getValueByKey(baseReturnOrderOut.getReturnStatus()));
        exportOrdDisReturn.setIsReversal(NumberUtil.INTEGER_ONE.equals(baseReturnOrderOut.getIsReversal()) ? "是" : "否");
        exportOrdDisReturn.setIsReversalOrder(NumberUtil.INTEGER_ONE.equals(baseReturnOrderOut.getIsReversalOrder()) ? "是" : "否");
        exportOrdDisReturn.setReturnType(OrdReturnOrderTypeEnum.getValueByKey(baseReturnOrderOut.getReturnType()));
        exportOrdDisReturn.setWrh(StringUtils.isNotBlank(baseReturnOrderOut.getWrhCode()) ? "【" + baseReturnOrderOut.getWrhCode() + "】" + baseReturnOrderOut.getWrhName() : "");
        exportOrdDisReturn.setStock(StringUtils.isNotBlank(baseReturnOrderOut.getStockCode()) ? "【" + baseReturnOrderOut.getStockCode() + "】" + baseReturnOrderOut.getStockName() : "");
        exportOrdDisReturn.setIndex(i + 1);
        exportOrdDisReturn.setStoreArea(StringUtils.isNotBlank(baseReturnOrderOut.getStoreAreaName()) ? baseReturnOrderOut.getStoreAreaName() + "【" + baseReturnOrderOut.getStoreArea() + "】" : "");
        return exportOrdDisReturn;
    }


    /**
     * 非第一次
     *
     * @param appDisReturnOrderSaveIn
     * @param storeOut
     */
    private List<AppSaveOrdDisReturnDetailIn> initReturnOrderDetailList(AppDisReturnOrderSaveIn appDisReturnOrderSaveIn, StoreOut storeOut, OrdDisReturn ordDisReturn) {
        List<AppSaveOrdDisReturnDetailIn> ordDisReturnDetailList = new ArrayList<>();
        OrdDisReturnNotice ordDisReturnNotice = null;
        if (null != appDisReturnOrderSaveIn.getReturnNoticeOrderId()) {
            ordDisReturnNotice = ordDisReturnNoticeService.getReturnNoticeOrderById(appDisReturnOrderSaveIn.getReturnNoticeOrderId());
        }
        int i = 1;
        for (AppSaveOrdDisReturnDetailIn returnGoodsInfoIn : appDisReturnOrderSaveIn.getReturnGoodsInfoInList()) {
            if (Objects.isNull(returnGoodsInfoIn.getApplyReturnQuantity()) || BigDecimal.ZERO.compareTo(returnGoodsInfoIn.getApplyReturnQuantity()) == NumberUtil.INTEGER_ZERO) {
                continue;
            }
            this.checkApplyReturnQuantity(ordDisReturnNotice, returnGoodsInfoIn.getGoodsCode(), storeOut.getStoreCode(), returnGoodsInfoIn.getApplyReturnQuantity());
            OrderGoodsOut orderGoodsOut = this.getFastReturnGoodsInfo(appDisReturnOrderSaveIn.getBizOrgCode(), storeOut.getStoreCode(), returnGoodsInfoIn.getGoodsCode());
            AppSaveOrdDisReturnDetailIn appSaveOrdDisReturnDetailIn = ordDisReturnDetailService.initReturnOrderDetail(storeOut, returnGoodsInfoIn, orderGoodsOut, ordDisReturn);
            appSaveOrdDisReturnDetailIn.setReturnOrderId(appDisReturnOrderSaveIn.getReturnOrderId());
            if (Objects.isNull(appSaveOrdDisReturnDetailIn.getLine())) {
                appSaveOrdDisReturnDetailIn.setLine(i++);
            }
            ordDisReturnDetailList.add(appSaveOrdDisReturnDetailIn);
        }
        OrdDisReturnImage delOrdDisReturnImage = new OrdDisReturnImage();
        delOrdDisReturnImage.setReturnOrderId(ordDisReturn.getId());
        ordDisReturnImageMapper.delete(delOrdDisReturnImage);
        this.saveOrUpdateReturnOrderDetailList(ordDisReturnDetailList, ordDisReturn);
        return ordDisReturnDetailList;
    }

    private void updateReturnOrderDetail(OrdDisReturnDetail returnGoodsInfoIn, AppSaveOrdDisReturnDetailIn ordDisReturnDetail, OrdDisReturn ordDisReturn) {
        ordDisReturnDetail.setApplyPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(ordDisReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
        ordDisReturnDetail.setApplyReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
        ordDisReturnDetail.setAuditPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(ordDisReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
        ordDisReturnDetail.setAuditReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
//        ordDisReturnDetail.setActualPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(ordDisReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
//        ordDisReturnDetail.setActualReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
        if (null != ordDisReturnDetail.getReturnUnitPrice()) {
            ordDisReturnDetail.setApplyReturnAmount(ordDisReturnDetail.getReturnUnitPrice().multiply(ordDisReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            ordDisReturnDetail.setAuditReturnAmount(ordDisReturnDetail.getReturnUnitPrice().multiply(ordDisReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//            ordDisReturnDetail.setActualReturnAmount(ordDisReturnDetail.getReturnUnitPrice().multiply(ordDisReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        }
        ordDisReturnDetail.setReturnReason(returnGoodsInfoIn.getReturnReason());
        BigDecimal sellTax = ordDisReturnDetail.getSellTax() == null ? BigDecimal.ZERO : ordDisReturnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
        //1+税率
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        BigDecimal stockPrice = warehouseServer.getStockPrice(ordDisReturn.getStoreCode(), returnGoodsInfoIn.getGoodsCode(), ordDisReturn.getBizOrgCode());
        //最新门店库存价
        ordDisReturnDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
//        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDisReturn.getStockCode());
//        BigDecimal warehousePrice = warehouseServer.getWarehousePrice(ordDisReturn.getWrhCode(), ordDisReturn.getStockCode(),
//                returnGoodsInfoIn.getGoodsCode(), stockInfoOut.getBizOrgCode(), ordDisReturnDetail.getVendorCode());
//        //最新仓储库存价
//        ordDisReturnDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);

        //退货去税金额
        ordDisReturnDetail.setReturnExceptTaxAmount(ordDisReturnDetail.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //退货税额
        ordDisReturnDetail.setReturnTaxAmount(ordDisReturnDetail.getApplyReturnAmount().subtract(ordDisReturnDetail.getReturnExceptTaxAmount()));
        //仓储成本金额
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDisReturn.getStockCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("找不到仓位");
        }
        BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(ordDisReturn.getWrhCode(), ordDisReturn.getStockCode(),
                returnGoodsInfoIn.getGoodsCode(), stockInfoOut.getBizOrgCode(), ordDisReturn.getStoreCode(), ordDisReturn.getBizOrgCode());
        if (Objects.isNull(warehousePrice)) {
            log.error("配销退货单{}商品{}仓储库存价为空", ordDisReturn.getReturnOrderNo(), ordDisReturnDetail.getGoodsCode());
            throw new BusinessException("配销退货单" + ordDisReturn.getReturnOrderNo() + "商品" + ordDisReturnDetail.getGoodsCode() + "仓储库存价为空");
        }
        ordDisReturnDetail.setWrhPrice(warehousePrice);
        ordDisReturnDetail.setWrhCostAmount(warehousePrice.multiply(ordDisReturnDetail.getApplyReturnQuantity()));
        //仓储成本去税金额
        ordDisReturnDetail.setWrhExceptTaxAmount(ordDisReturnDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //仓储成本税额
        ordDisReturnDetail.setWrhTaxAmount(ordDisReturnDetail.getWrhCostAmount().subtract(ordDisReturnDetail.getWrhExceptTaxAmount()));
        //门店成本金额
        ordDisReturnDetail.setStoreCostAmount(ordDisReturnDetail.getStoreStockPrice().multiply(ordDisReturnDetail.getApplyReturnQuantity()));
        //门店成本去税金额
        ordDisReturnDetail.setStoreExceptTaxAmount(ordDisReturnDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //门店成本税额
        ordDisReturnDetail.setStoreTaxAmount(ordDisReturnDetail.getStoreCostAmount().subtract(ordDisReturnDetail.getStoreExceptTaxAmount()));
        ordDisReturnDetail.setExpiry(ordDisReturnDetail.getExpiry());
    }

    /**
     * 保存或者更新退货单明细
     *
     * @param ordDisReturnDetailList 退货单明细
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateReturnOrderDetailList(List<AppSaveOrdDisReturnDetailIn> ordDisReturnDetailList, OrdDisReturn ordDisReturn) {
        ordDisReturnDetailList.forEach(ordDisReturnDetail -> {
            ordDisReturnDetailService.saveOrUpdateReturnOrderDetail(ordDisReturnDetail);
            this.handleReturnImageUpload(ordDisReturn, ordDisReturnDetail);
        });
    }


    /**
     * 退货通知单第一次保存
     *
     * @param appDisReturnOrderSaveIn
     * @param storeOut
     */
    private void saveReturnOrderData(AppDisReturnOrderSaveIn appDisReturnOrderSaveIn, StoreOut storeOut) {
        OrdDisReturnNotice ordDisReturnNotice = null;
        if (null != appDisReturnOrderSaveIn.getReturnNoticeOrderId()) {
            synchronized (appDisReturnOrderSaveIn.getStoreCode() + "_" + appDisReturnOrderSaveIn.getReturnNoticeOrderId()) {
                if (checkIsReturn(appDisReturnOrderSaveIn.getStoreCode(), appDisReturnOrderSaveIn.getReturnNoticeOrderId())) {
                    throw new BusinessException("此退货通知单已申请退货，请勿重复提交！");
                }
            }
            ordDisReturnNotice = ordDisReturnNoticeService.getReturnNoticeOrderById(appDisReturnOrderSaveIn.getReturnNoticeOrderId());
        }
        Map<String, OrdDisReturn> returnOrderMap = new LinkedHashMap<>();
        Map<String, List<com.edc.erp.returnorder.model.in.AppSaveOrdDisReturnDetailIn>> returnOrderDetailMap = new LinkedHashMap<>();
        for (com.edc.erp.returnorder.model.in.AppSaveOrdDisReturnDetailIn appSaveOrdDisReturnDetailIn : appDisReturnOrderSaveIn.getReturnGoodsInfoInList()) {
            if (Objects.isNull(appSaveOrdDisReturnDetailIn.getApplyReturnQuantity()) || BigDecimal.ZERO.compareTo(appSaveOrdDisReturnDetailIn.getApplyReturnQuantity()) == NumberUtil.INTEGER_ZERO) {
                continue;
            }
            this.checkApplyReturnQuantity(ordDisReturnNotice, appSaveOrdDisReturnDetailIn.getGoodsCode(), storeOut.getStoreCode(), appSaveOrdDisReturnDetailIn.getApplyReturnQuantity());
            OrderGoodsOut orderGoodsOut = this.getFastReturnGoodsInfo(appDisReturnOrderSaveIn.getBizOrgCode(), storeOut.getStoreCode(), appSaveOrdDisReturnDetailIn.getGoodsCode());
            String stockCode = orderGoodsOut.getBackStockCode();
            String key = stockCode + SystemConstant.SHORT_LINE + orderGoodsOut.getDistributionWay();
            OrdDisReturn ordDisReturn = returnOrderMap.get(orderGoodsOut.getBackStockCode());
            if (null == ordDisReturn) {
                ordDisReturn = this.inReturnOrder(appDisReturnOrderSaveIn, storeOut, OrdReturnOrderStatusEnum.SAVED.getKey(),
                        orderGoodsOut.getBackStockCode(), orderGoodsOut.getDistributionWay());
                returnOrderMap.put(key, ordDisReturn);
            }
            ordDisReturnDetailService.initReturnOrderDetailMap(returnOrderDetailMap, storeOut, appSaveOrdDisReturnDetailIn, orderGoodsOut, ordDisReturn);
        }
        this.saveReturnOrderByMap(returnOrderMap, returnOrderDetailMap);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveReturnOrderByMap(Map<String, OrdDisReturn> returnOrderMap, Map<String, List<AppSaveOrdDisReturnDetailIn>> returnOrderDetailMap) {
        for (Map.Entry<String, OrdDisReturn> returnOrderEntry : returnOrderMap.entrySet()) {
            String key = returnOrderEntry.getKey();
            OrdDisReturn ordDisReturn = returnOrderEntry.getValue();
            List<AppSaveOrdDisReturnDetailIn> returnOrderDetailList = returnOrderDetailMap.get(key);
            ordDisReturn.setSkuCount(returnOrderDetailList.size());
            ordDisReturn.setApplyReturnQuantity(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getApplyReturnQuantity())).map(OrdDisReturnDetail::getApplyReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            ordDisReturn.setApplyReturnAmount(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getApplyReturnAmount())).map(OrdDisReturnDetail::getApplyReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            ordDisReturn.setAuditReturnQuantity(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getAuditReturnQuantity())).map(OrdDisReturnDetail::getAuditReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            ordDisReturn.setAuditReturnAmount(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getAuditReturnAmount())).map(OrdDisReturnDetail::getAuditReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
//            ordDisReturn.setActualReturnQuantity(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getActualReturnQuantity())).map(OrdDisReturnDetail::getActualReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
//            ordDisReturn.setActualReturnAmount(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getActualReturnAmount())).map(OrdDisReturnDetail::getActualReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            if (null == ordDisReturn.getId()) {
                this.insert(ordDisReturn);
                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
                        OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), DisOrdReturnOrderLogEnum.ORD_DIS_APP_SAVE.getValue(), new Date(), ordDisReturn.getCreator());
                asyncLogService.sendAsyncSaveLogByMq(businessLog);
            }
            int i = 1;
            for (AppSaveOrdDisReturnDetailIn appSaveOrdDisReturnDetailIn : returnOrderDetailList) {
                appSaveOrdDisReturnDetailIn.setReturnOrderId(ordDisReturn.getId());
                appSaveOrdDisReturnDetailIn.setLine(i);
                if (null == appSaveOrdDisReturnDetailIn.getId()) {
                    ordDisReturnDetailService.saveReturnOrderDetail(appSaveOrdDisReturnDetailIn);
                    this.handleReturnImageUpload(ordDisReturn, appSaveOrdDisReturnDetailIn);
                    i++;
                }
            }
        }
    }

    private void handleReturnImageUpload(OrdDisReturn ordDisReturn, AppSaveOrdDisReturnDetailIn appSaveOrdDisReturnDetailIn) {
        appSaveOrdDisReturnDetailIn.getImageUrlList().forEach(imageUrl -> {
            OrdDisReturnImage ordDisReturnImage = new OrdDisReturnImage();
            ordDisReturnImage.setReturnOrderId(ordDisReturn.getId());
            ordDisReturnImage.setReturnDetailId(appSaveOrdDisReturnDetailIn.getId());
            ordDisReturnImage.setImage_url(imageUrl);
            ordDisReturnImage.setBizOrgCode(ordDisReturn.getBizOrgCode());
            ordDisReturnImage.setCreator(ordDisReturn.getCreator());
            ordDisReturnImage.setCreateTime(LocalDateTime.now());
            ordDisReturnImageMapper.insert(ordDisReturnImage);
        });
    }

    private OrdDisReturn inReturnOrder(AppDisReturnOrderSaveIn appDisReturnOrderSaveIn, StoreOut storeOut, String returnStatus, String backStockCode, String distributionWay) {
        OrdDisReturn ordDisReturn = new OrdDisReturn();
        String returnOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.XT.getCode(), appDisReturnOrderSaveIn.getBizOrgCode(), uniqueUtils, 4);
        ordDisReturn.setReturnOrderNo(returnOrderNo);
        ordDisReturn.setStoreCode(storeOut.getStoreCode());
        ordDisReturn.setDistributionType(distributionWay);
        ordDisReturn.setStoreName(storeOut.getStoreName());
        ordDisReturn.setStockCode(backStockCode);
        ordDisReturn.setStoreArea(storeOut.getBelongArea());
        StockInfoOut stockInfoOut = stockServer.getTransInfo(backStockCode);
        ordDisReturn.setWrhCode(Objects.isNull(stockInfoOut) ? "" : stockInfoOut.getWarehouseCode());
        if (null == appDisReturnOrderSaveIn.getReturnNoticeOrderId()) {
            ordDisReturn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
        } else {
            OrdDisReturnNotice ordDisReturnNotice = ordDisReturnNoticeService.getReturnNoticeOrderById(appDisReturnOrderSaveIn.getReturnNoticeOrderId());
            ordDisReturn.setReturnType(ordDisReturnNotice.getReturnType());
        }
        ordDisReturn.setReturnStatus(returnStatus);
        ordDisReturn.setRemark(appDisReturnOrderSaveIn.getRemark());
        ordDisReturn.setReturnNoticeOrderId(appDisReturnOrderSaveIn.getReturnNoticeOrderId());
        ordDisReturn.setReturnOrderReason(appDisReturnOrderSaveIn.getReturnOrderReason());
        ordDisReturn.setBizOrgCode(appDisReturnOrderSaveIn.getBizOrgCode());
        ordDisReturn.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(ordDisReturn.getBizOrgCode()));
        ordDisReturn.setCreator(storeOut.getStoreCode());
        ordDisReturn.setCreateTime(LocalDateTime.now());
        ordDisReturn.setUpdater(storeOut.getStoreCode());
        ordDisReturn.setUpdateTime(LocalDateTime.now());
        ordDisReturn.setIsDelete(ModelConst.DELETE.NO);
        ordDisReturn.setIsReversal(ModelConst.DELETE.NO);
        ordDisReturn.setIsReversalOrder(ModelConst.DELETE.NO);
        ordDisReturn.setAppRemark(appDisReturnOrderSaveIn.getAppRemark());
        ordDisReturn.setDeliveryOrderNo(appDisReturnOrderSaveIn.getDeliveryOrderNo());
//        ordDisReturn.setDistributionWay(distributionWay);
        return ordDisReturn;
    }

    /**
     * 审核退货单
     *
     * @param returnOrder
     * @param returnGoodsInfoInList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Response<String> audit(OrdDisReturn returnOrder, List<OrdDisReturnDetail> returnGoodsInfoInList, String centerStockBizOrgCode) {
        String s = checkInvStore(returnOrder, returnGoodsInfoInList);
        if (StringUtils.isNotEmpty(s)) {
            throw new BusinessException(s);
        }
        returnOrder.setReturnStatus(OrdReturnOrderStatusEnum.APPROVED.getKey());
        returnOrder.setUpdater(UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】"));
        returnOrder.setUpdateTime(LocalDateTime.now());
        AtomicReference<BigDecimal> auditReturnQuantity = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> auditReturnAmount = new AtomicReference<>(BigDecimal.ZERO);
        returnGoodsInfoInList.forEach(ordDirReturnDetail -> {
            if (Objects.nonNull(ordDirReturnDetail.getAuditReturnQuantity())) {
                auditReturnQuantity.getAndSet(auditReturnQuantity.get().add(ordDirReturnDetail.getAuditReturnQuantity()));
            }
            if (Objects.nonNull(ordDirReturnDetail.getAuditReturnAmount())) {
                auditReturnAmount.getAndSet(auditReturnAmount.get().add(ordDirReturnDetail.getAuditReturnAmount()));
            }
        });
        returnOrder.setAuditReturnQuantity(auditReturnQuantity.get());
        returnOrder.setAuditReturnAmount(auditReturnAmount.get().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        returnOrder.setAuditor(UserUtil.getUserName());
        returnOrder.setAuditTime(LocalDateTime.now());
        int count = ordDisReturnMapper.updateByPrimaryKey(returnOrder);
        if (1 > count) {
            return Response.error("审核失败");
        }
        //门店库存调整
        StockFlowIn stockFlowIn = this.initStoreStockCharge(returnOrder, returnGoodsInfoInList, returnOrder.getAuditTime());
        Response stockFlow = stockFlowService.checkStockFlow(Collections.singletonList(stockFlowIn));
        if (!stockFlow.isSuccess()) {
            throw new BusinessException("库存调整失败");
        }
        // 天岁接入ERP，不再对接中科接口
//        if (OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(returnOrder.getBizOrgCode())) {
//            OrdDisReturn sendZKRetrun = new OrdDisReturn();
//            sendZKRetrun.setReturnOrderNo(returnOrder.getReturnOrderNo());
//            sendZKRetrun.setBizOrgCode(returnOrder.getBizOrgCode());
//            sendZKRetrun.setCreator(returnOrder.getUpdater());
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.RETURN_SEND_ZK, JSONObject.toJSONString(sendZKRetrun), returnOrder.getBizOrgCode(), returnOrder.getReturnOrderNo());
//        } else {
        //下发dts
        if (stockServer.isSendWms(returnOrder.getStockCode(), centerStockBizOrgCode)) {
            this.initReturnOrderToDts(returnOrder, returnGoodsInfoInList, centerStockBizOrgCode);
        }
//        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(returnOrder.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), OrdLogTypeEnum.ORD_DIS_RETURN_APPROVED.getName(), new Date(), returnOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("审核成功");
    }

    private OrderGoodsOut getFastReturnGoodsInfo(String bizOrgCode, String storeCode, String goodsCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(goodsCode);
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS_FAST.getType());
        OrderGoodsOut orderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(orderGoods)) {
            throw new BusinessException("此商品不允许配销退货");
        }
        return orderGoods;
    }

    private void checkApplyReturnQuantity(OrdDisReturnNotice ordDisReturnNotice, String goodsCode, String storeCode, BigDecimal applyReturnQuantity) {
        // 如果是限量退货
        if (null != ordDisReturnNotice && OrdReturnOrderTypeEnum.LIMITED_RETURN.getKey().equals(ordDisReturnNotice.getReturnType())) {
            // 限量最大值 3
            BigDecimal maxQty = ordReturnNoticeStoreService.getMaxQtyByParameter(goodsCode, storeCode, ordDisReturnNotice.getId());
            if (applyReturnQuantity.compareTo(maxQty) > 0) {
                throw new BusinessException("商品" + goodsCode + "最大申请数" + maxQty.toBigInteger());
            }
        }
    }

    private boolean checkIsReturn(String storeCode, Integer returnNoticeOrderId) {
        OrdDisReturn query = new OrdDisReturn();
        query.setStoreCode(storeCode);
        query.setReturnNoticeOrderId(returnNoticeOrderId);
        return ordDisReturnMapper.selectCount(query) > 0;
    }

    /**
     * 为异步导出查询配销退货单明细方法
     *
     * @param ordReturnOrderPageIn
     * @return
     */
    private List<AsyncExcelReturnOrderDetail> findListForAsyncExportPage(OrdReturnOrderPageIn ordReturnOrderPageIn) {
        String loginBizOrgCode = ordReturnOrderPageIn.getBizOrgCode();
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(loginBizOrgCode);
        Optional<StockInfoOut> anyOneOptional = authOrgStockMap.values().stream().filter(stockInfoOut -> !loginBizOrgCode.equals(stockInfoOut.getBizOrgCode())).findFirst();
        if (!anyOneOptional.isPresent()) {
            ordReturnOrderPageIn.setBizOrgCode("");
        }
        if (CollectionUtils.isEmpty(ordReturnOrderPageIn.getStockCodeList())) {
            List<String> stockCodeList = authOrgStockMap.keySet().stream().collect(Collectors.toList());
            ordReturnOrderPageIn.setStockCodeList(stockCodeList);
        } else {
            // 校验业务仓位是否允许操作
            storeChannelHandle.checkStockCodeListISAuth(ordReturnOrderPageIn.getStockCodeList(), authOrgStockMap);
        }
        List<AsyncExcelReturnOrderDetail> asyncExcelDeliveryOrderDetails = ordDisReturnMapper.findListForAsyncExportByPage(ordReturnOrderPageIn);
        if (CollectionUtils.isEmpty(asyncExcelDeliveryOrderDetails)) {
            return asyncExcelDeliveryOrderDetails;
        }
        String defauldReturnReasonCode = equipmentBusinessReasonServer.getDefaultReasonCodeByName(ordReturnOrderPageIn.getBizOrgCode(), EquipmentBusinessConstant.DEFAULT_BUSINESS_REASON_TYPE, EquipmentBusinessConstant.DEFAULT_BUSINESS_REASON_NAME, EquipmentBusinessConstant.BUSINESS_REASON_DIMENSION);
        asyncExcelDeliveryOrderDetails.forEach(item -> {
            item.setReturnTypeValue(OrdReturnOrderTypeEnum.getValueByKey(item.getReturnType()));
            item.setReturnStatusValue(OrdReturnOrderStatusEnum.getValueByKey(item.getReturnStatus()));
            item.setReturnOrderReason(systemDictService.getSystemDictName(item.getReturnOrderReason()));
            if (StringUtils.isBlank(item.getReturnReason())) {
                item.setReturnReason(defauldReturnReasonCode);
            }
            item.setReturnReason(systemDictService.getSystemDictName(item.getReturnOrderReason()));
        });
        return asyncExcelDeliveryOrderDetails;
    }
}