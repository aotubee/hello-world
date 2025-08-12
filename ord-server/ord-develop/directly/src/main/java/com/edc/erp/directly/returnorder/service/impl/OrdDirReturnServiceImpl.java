package com.edc.erp.directly.returnorder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.EquipmentBusinessConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.purchase.FindVendorTransIn;
import com.edc.erp.common.model.out.InvBizRsnTransOut;
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
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNotice;
import com.edc.erp.directly.returnnoticeorder.service.OrdDirReturnNoticeService;
import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import com.edc.erp.directly.returnorder.entity.OrdDirReturnImage;
import com.edc.erp.directly.returnorder.enumeration.DirOrdReturnOrderLogEnum;
import com.edc.erp.directly.returnorder.enumeration.OrdReturnOrderStatusEnum;
import com.edc.erp.directly.returnorder.enumeration.OrdReturnOrderTypeEnum;
import com.edc.erp.directly.returnorder.handle.DirReturnImportHandle;
import com.edc.erp.directly.returnorder.listener.OrdReturnOrderAsyncImportListener;
import com.edc.erp.directly.returnorder.listener.OrdReturnStoreGoodsListener;
import com.edc.erp.directly.returnorder.mapper.OrdDirReturnImageMapper;
import com.edc.erp.directly.returnorder.mapper.OrdDirReturnMapper;
import com.edc.erp.directly.returnorder.model.in.AppSaveOrdDirReturnDetailIn;
import com.edc.erp.directly.returnorder.model.excel.AsyncExcelReturnOrderDetail;
import com.edc.erp.directly.returnorder.model.excel.ExportOrdDirReturn;
import com.edc.erp.directly.returnorder.model.excel.ImportOrdReturnGoodsVO;
import com.edc.erp.directly.returnorder.model.in.SaveReturnGoodsOut;
import com.edc.erp.directly.returnorder.model.in.*;
import com.edc.erp.directly.returnorder.model.out.*;
import com.edc.erp.directly.returnorder.service.OrdDirReturnDetailService;
import com.edc.erp.directly.returnorder.service.OrdDirReturnService;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsExpiryIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.reducestock.stock.service.StockStoreService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 退货单(OrdDirReturn)表服务实现类
 *
 * @author
 * @since 2022-11-18 18:49:56
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDirReturnServiceImpl extends BaseServiceImpl<OrdDirReturn> implements OrdDirReturnService {

    private final OrdDirReturnMapper ordDirReturnMapper;

    private final UniqueUtils uniqueUtils;

    private final AsyncLogService asyncLogService;

    private final OrderGoodsServer orderGoodsServer;

    private final OrdDirReturnDetailService ordDirReturnDetailService;

    private final WarehouseServer warehouseServer;

    private final OrdDirReturnNoticeService ordDirReturnNoticeService;

    private final StockServer stockServer;

    private final StoreCenterService storeCenterService;

    private final AsyncPushTaskService asyncPushTaskService;

    private final OrdDirReturnImageMapper ordDirReturnImageMapper;

    private final FileService fileService;

    private final SystemDictService systemDictService;

    private final EquipmentBusinessReasonServer equipmentBusinessReasonServer;

    private final StockStoreService stockStoreService;

    private final RedisService redisService;

    private final AsyncExportExecutor asyncExportExecutor;

    private final StockFlowService stockFlowService;

    private final DirReturnImportHandle dirReturnImportHandle;

    private final PurchaseOrderClient purchaseOrderClient;

    private final StoreChannelHandle storeChannelHandle;

    private final OrdDirDeliveryService ordDirDeliveryService;

    @Qualifier("dirReturnToDtsSender")
    private final MessageSender dirReturnToDtsSender;

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
            returnOrderId = this.updateDirReturnOrder(saveReturnOrderIn);
        } else {
            if (isCheckRepeat) {
                boolean checkReturnOrderRepeatSubmitFlag = this.checkDirReturnOrderSubmit(saveReturnOrderIn);
                if (checkReturnOrderRepeatSubmitFlag) {
                    log.error("门店{}发起退货单，短时间内异常重复提交，故判为无效提交！", saveReturnOrderIn.getStoreCode());
                    return Response.error("请不要重复提交");
                }
            }
            returnOrderId = this.insertDirReturnOrder(saveReturnOrderIn, channelBizOrgCode);
            this.setDirReturnOrderKey(saveReturnOrderIn);
        }
        if (returnOrderId > 0) {
            return Response.data(returnOrderId, "保存成功");
        }
        return Response.error("保存失败");

    }

    private boolean checkDirReturnOrderSubmit(OrdSaveReturnOrderIn saveReturnOrderIn) {
        Integer returnNoticeOrderId = Objects.isNull(saveReturnOrderIn.getReturnNoticeOrderId()) ? 0 : saveReturnOrderIn.getReturnNoticeOrderId();
        String key = DirSystemConstant.CHECK_DIR_RETURN_ORDER_REPEAT_SUBMIT + saveReturnOrderIn.getBizOrgCode() + SystemConstant.COLON + returnNoticeOrderId +
                SystemConstant.COLON + saveReturnOrderIn.getStoreCode() + SystemConstant.COLON + saveReturnOrderIn.getWarehouseCode();
        return redisService.hasKey(key);
    }

    private void setDirReturnOrderKey(OrdSaveReturnOrderIn saveReturnOrderIn) {
        Integer returnNoticeOrderId = Objects.isNull(saveReturnOrderIn.getReturnNoticeOrderId()) ? 0 : saveReturnOrderIn.getReturnNoticeOrderId();
        String key = DirSystemConstant.CHECK_DIR_RETURN_ORDER_REPEAT_SUBMIT + saveReturnOrderIn.getBizOrgCode() + SystemConstant.COLON + returnNoticeOrderId +
                SystemConstant.COLON + saveReturnOrderIn.getStoreCode() + SystemConstant.COLON + saveReturnOrderIn.getWarehouseCode();
        redisService.set(key, key, 1, TimeUnit.MINUTES);
    }


//    @Transactional(rollbackFor = Exception.class)
//    public Integer saveAndUpdateDirReturnOrder(OrdSaveReturnOrderIn saveReturnOrderIn) {
//        //校验数据
//        String s = checkData(saveReturnOrderIn);
//        if (StringUtils.isNotEmpty(s)) {
//            throw new BusinessException(s);
//        }
//        Integer returnOrderId = saveReturnOrderIn.getReturnOrderId();
//        OrdDirReturn ordDirReturn;
//        int count;
//        if (Objects.isNull(returnOrderId)) {
//            ordDirReturn = new OrdDirReturn();
//            BeanUtil.copyProperties(saveReturnOrderIn, ordDirReturn, CopyOptions.create().setIgnoreNullValue(true));
//            String no = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PT.getCode(), saveReturnOrderIn.getBizOrgCode(), uniqueUtils, 4);
//            ordDirReturn.setReturnOrderNo(no);
//            ordDirReturn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
//            ordDirReturn.setOrgCode(UserUtil.getOrgCode());
//            ordDirReturn.setSubmitTime(LocalDateTime.now());
//            ordDirReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
//            ordDirReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
//            ordDirReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
//            ordDirReturn.setIsDelete(NumberUtil.INTEGER_ZERO);
//            ordDirReturn.setWrhCode(saveReturnOrderIn.getWarehouseCode());
//            ordDirReturn.setRemark(saveReturnOrderIn.getRemark());
//            ordDirReturn.setSkuCount(saveReturnOrderIn.getReturnGoodsInfoInList().size());
//            count = ordDirReturnMapper.insertSelective(ordDirReturn);
//        } else {
//            ordDirReturn = ordDirReturnMapper.selectByPrimaryKey(returnOrderId);
//            if (!OrdReturnOrderStatusEnum.SUBMITTED.getKey().equals(ordDirReturn.getReturnStatus())) {
//                throw new BusinessException("只有未审核状态才能修改");
//            }
//            BeanUtil.copyProperties(saveReturnOrderIn, ordDirReturn, CopyOptions.create().setIgnoreNullValue(true));
//            ordDirReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
//            ordDirReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
//            ordDirReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
//            ordDirReturn.setUpdater(UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】"));
//            ordDirReturn.setUpdateTime(LocalDateTime.now());
//            ordDirReturn.setWrhCode(saveReturnOrderIn.getWarehouseCode());
//            count = ordDirReturnMapper.updateByPrimaryKeySelective(ordDirReturn);
//        }
//        if (count > 0) {
//            ordDirReturnDetailService.deleteByReturnOrderId(ordDirReturn);
//            ordDirReturnDetailService.insertDetail(ordDirReturn, saveReturnOrderIn);
//            if (Objects.isNull(returnOrderId)) {
//                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN.getName(), String.valueOf(ordDirReturn.getId()),
//                        OrdLogTypeEnum.ORD_DIR_RETURN.getCode(), OrdLogTypeEnum.ORD_DIR_RETURN_SAVE.getName(), new Date(), ordDirReturn.getCreator());
//                asyncLogService.sendAsyncSaveLogByMq(businessLog);
//            }
//        }
//        return ordDirReturn.getId();
//    }

    /**
     * 分页查退货单列表
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
        List<BaseReturnOrderOut> baseReturnOrderOutList = ordDirReturnMapper.findBaseReturnOrderByPage(returnOrderPageIn);
        baseReturnOrderOutList.forEach(baseReturnOrderOut -> {
            OrdDirReturnNotice ordDirReturnNotice = ordDirReturnNoticeService.getReturnNoticeOrderById(baseReturnOrderOut.getReturnNoticeOrderId());
            baseReturnOrderOut.setReturnStatusValue(OrdReturnOrderStatusEnum.getValueByKey(baseReturnOrderOut.getReturnStatus()));
            baseReturnOrderOut.setReturnTypeValue(OrdReturnOrderTypeEnum.getValueByKey(baseReturnOrderOut.getReturnType()));
            baseReturnOrderOut.setReturnNoticeNo(Objects.isNull(ordDirReturnNotice) ? "" : ordDirReturnNotice.getReturnNoticeOrderNo());
            StockInfoOut stockInfoOut = stockServer.getTransInfo(baseReturnOrderOut.getStockCode());
            if (Objects.nonNull(stockInfoOut)) {
                baseReturnOrderOut.setWrhName(stockInfoOut.getWarehouseName());
                baseReturnOrderOut.setStockName(stockInfoOut.getStockName());
            }
            baseReturnOrderOut.setStoreAreaName(storeCenterService.getNameByCode(baseReturnOrderOut.getStoreArea(), baseReturnOrderOut.getBizOrgCode()));
            baseReturnOrderOut.setReturnOrderReasonValue(systemDictService.getSystemDictName(baseReturnOrderOut.getReturnOrderReason()));
            baseReturnOrderOut.setApplySkuCount(ordDirReturnDetailService.countApplyReturnSkuQuantity(baseReturnOrderOut.getId()));
            baseReturnOrderOut.setApplyReturnQuantity(ordDirReturnDetailService.sumApplyReturnQuantity(baseReturnOrderOut.getId()));

        });
        Page<BaseReturnOrderOut> resultPage = new Page<>(returnOrderPageIn);
        resultPage.setList(baseReturnOrderOutList);
        return resultPage;
    }

    /**
     * 查退货单明细
     *
     * @param pageIn
     * @return
     */
    @Override
    public Response<ReturnDetailOut> findOrdReturnDetails(OrdDirReturnDetailIn pageIn) {
        OrdDirReturn ordDirReturn = ordDirReturnMapper.selectByPrimaryKey(pageIn.getReturnOrderId());
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirReturn.getStockCode(), pageIn.getBizOrgCode(), "操作退货单明细");
        OrdDirReturnImage ordDirReturnImage = new OrdDirReturnImage();
        ordDirReturnImage.setReturnOrderId(pageIn.getReturnOrderId());
        List<OrdDirReturnImage> allImageList = ordDirReturnImageMapper.select(ordDirReturnImage);
        Map<Integer, List<String>> detailImagesMap = new HashMap<>();
        allImageList.forEach(returnImage -> {
            List<String> imageList = detailImagesMap.get(returnImage.getReturnDetailId());
            if (CollectionUtils.isEmpty(imageList)) {
                imageList = Lists.newArrayList();
            }
            imageList.add(returnImage.getImage_url());
            detailImagesMap.put(returnImage.getReturnDetailId(), imageList);
        });
        List<OrdDirReturnDetailOut> ordDirReturnDetailOutList = ordDirReturnDetailService.finaOrdReturnDetail(pageIn);
        ordDirReturnDetailOutList.forEach(ordDirReturnDetailOut -> {
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setStoreCode(ordDirReturn.getStoreCode());
            orderGoodsIn.setGoodsCode(ordDirReturnDetailOut.getGoodsCode());
            orderGoodsIn.setBizOrgCode(ordDirReturn.getBizOrgCode());
            OrderGoodsOut orderGoods = orderGoodsServer.getStoreGoodsNoType(orderGoodsIn);
            ordDirReturnDetailOut.setReturnPrinciple(Objects.isNull(orderGoods) ? "" : orderGoods.getReturnPrinciple());
            BigDecimal stockNum = warehouseServer.getStockNum(ordDirReturn.getStoreCode(), ordDirReturnDetailOut.getGoodsCode(), ordDirReturn.getBizOrgCode());
            ordDirReturnDetailOut.setStoreInventory(stockNum);
            if (Objects.isNull(orderGoods)) {
                throw new BusinessException("门店商品" + ordDirReturnDetailOut.getGoodsCode() + "不存在");
            } else {
                ordDirReturnDetailOut.setIsManageValidityPeriod(orderGoods.getIsManageValidityPeriod());
            }
            if (StringUtils.isNotEmpty(ordDirReturnDetailOut.getReturnReason())) {
                InvBizRsnTransOut invBizRsnTransOut = equipmentBusinessReasonServer.getStoreInvBizRsnTransByCode(ordDirReturnDetailOut.getReturnReason(), ordDirReturn.getBizOrgCode(), NumberUtil.INTEGER_ZERO);
                ordDirReturnDetailOut.setReturnReasonValue(Objects.isNull(invBizRsnTransOut) ? ordDirReturnDetailOut.getReturnReason() : invBizRsnTransOut.getBusinessReasonName());
            }
            ordDirReturnDetailOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(ordDirReturnDetailOut.getGoodsType()));
            ordDirReturnDetailOut.setImageList(detailImagesMap.get(ordDirReturnDetailOut.getId()));
            ordDirReturnDetailOut.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(ordDirReturnDetailOut.getInvoiceType()));
        });
        ReturnDetailOut returnDetailOut = new ReturnDetailOut();
        BeanUtils.copy(ordDirReturn, returnDetailOut);
        returnDetailOut.setStockName(Objects.isNull(stockInfoOut) ? "" : stockInfoOut.getStockName());
        OrdDirReturnNotice ordDirReturnNotice = ordDirReturnNoticeService.getReturnNoticeOrderById(ordDirReturn.getReturnNoticeOrderId());
        returnDetailOut.setReturnNoticeOrderNo(Objects.isNull(ordDirReturnNotice) ? "" : ordDirReturnNotice.getReturnNoticeOrderNo());
        returnDetailOut.setReturnOrderId(pageIn.getReturnOrderId());
        returnDetailOut.setReturnStatusValue(OrdReturnOrderStatusEnum.getValueByKey(ordDirReturn.getReturnStatus()));
        returnDetailOut.setReturnTypeValue(OrdReturnOrderTypeEnum.getValueByKey(ordDirReturn.getReturnType()));
        returnDetailOut.setGoodsInfo(ordDirReturnDetailOutList);
        returnDetailOut.setWarehouseCode(ordDirReturn.getWrhCode());
        if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDirReturn.getReturnStatus())) {
            returnDetailOut.setActualReturnQuantity(ordDirReturn.getActualReturnQuantity());
            returnDetailOut.setActualSkuCount(ordDirReturnDetailService.countActualReturnSkuQuantity(pageIn.getReturnOrderId()));
            returnDetailOut.setActualReturnAmount(ordDirReturn.getActualReturnAmount());
        }
        returnDetailOut.setApplyReturnQuantity(ordDirReturnDetailService.sumApplyReturnQuantity(pageIn.getReturnOrderId()));
        returnDetailOut.setApplySkuCount(ordDirReturnDetailService.countApplyReturnSkuQuantity(pageIn.getReturnOrderId()));
        returnDetailOut.setReturnOrderReasonValue(systemDictService.getSystemDictName(returnDetailOut.getReturnOrderReason()));
        return Response.data(returnDetailOut);
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
        OrdDirReturn returnOrder = this.getReturnOrderById(saveReturnOrderIn.getReturnOrderId());
        if (Objects.isNull(returnOrder)) {
            return Response.error("退货单不存在");
        }
        String key = DirSystemConstant.CHECK_DIR_RETURN_ORDER_AUDIT + returnOrder.getBizOrgCode() +
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
        //无退货通知单id
        if (Objects.isNull(saveReturnOrderIn.getReturnNoticeOrderId())) {
            this.saveOrUpdateReturnOrder(saveReturnOrderIn, true, returnOrder.getBizOrgCode());
        }
        return this.audit(returnOrder, saveReturnOrderIn.getReturnGoodsInfoInList(), centerStockBizOrgCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public Response<String> audit(OrdDirReturn returnOrder, List<OrdDirReturnDetail> returnGoodsInfoInList, String centerStockBizOrgCode) {
        String s = this.checkInvStore(returnOrder, returnGoodsInfoInList);
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
        returnOrder.setAuditReturnAmount(auditReturnAmount.get());
        returnOrder.setAuditor(UserUtil.getUserName());
        returnOrder.setAuditTime(LocalDateTime.now());
        int count = ordDirReturnMapper.updateByPrimaryKey(returnOrder);
        if (count < 1) {
            return Response.error("审核失败");
        }
        //门店库存调整
        StockFlowIn stockFlowIn = this.initStoreStockCharge(returnOrder, returnGoodsInfoInList, returnOrder.getAuditTime());
        Response response = stockFlowService.checkStockFlow(Collections.singletonList(stockFlowIn));
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
        //下发dts
        if (stockServer.isSendWms(returnOrder.getStockCode(), centerStockBizOrgCode)) {
            this.initReturnOrderToDts(returnGoodsInfoInList, returnOrder, centerStockBizOrgCode);
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN.getName(), String.valueOf(returnOrder.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN.getCode(), OrdLogTypeEnum.ORD_DIR_RETURN_APPROVED.getName(), new Date(), returnOrder.getCreator());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("审核成功");
    }

    /**
     * 作废退货单
     *
     * @param returnOrder
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response invalidatedOrdReturn(OrdDirReturn returnOrder) {
        String beforeStatus = returnOrder.getReturnStatus();
        if (!OrdReturnOrderStatusEnum.SUBMITTED.getKey().equals(beforeStatus)
                && !OrdReturnOrderStatusEnum.SAVED.getKey().equals(beforeStatus)
                && !OrdReturnOrderStatusEnum.APPROVED.getKey().equals(beforeStatus)) {
            return Response.error("退货单只有待审核、已保存和已审核才可作废");
        }
        String loginUsername = UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】");
        returnOrder.setUpdateTime(LocalDateTime.now());
        returnOrder.setUpdater(loginUsername);
        returnOrder.setReturnStatus(OrdReturnOrderStatusEnum.INVALID.getKey());
        ordDirReturnMapper.updateByPrimaryKeySelective(returnOrder);
        if (OrdReturnOrderStatusEnum.APPROVED.getKey().equals(beforeStatus)) {
            List<OrdDirReturnDetail> ordDirReturnDetails = ordDirReturnDetailService.findByReturnOrderId(returnOrder.getId());
            StockFlowIn stockFlowIn = this.initStoreStockCharge(returnOrder, ordDirReturnDetails, returnOrder.getUpdateTime());
            Response response = stockFlowService.checkStockFlow(Collections.singletonList(stockFlowIn));
            if (!response.isSuccess()) {
                throw new BusinessException(response.getMessage());
            }
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN.getName(), String.valueOf(returnOrder.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN.getCode(), OrdLogTypeEnum.ORD_DIR_RETURN_INVALID.getName(), new Date(), loginUsername);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("作废成功");
    }

    /**
     * @Description: 收货
     * @Author: ZhangYao
     * @Date: 2024/3/30 14:31
     * @param returnGoodsInfoInList:
     * @param returnOrder:
     * @param stockInfoOut:
     * @return: com.edc.plugins.common.response.Response
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response receiving(List<OrdDirReturnDetail> returnGoodsInfoInList, OrdDirReturn returnOrder, StockInfoOut stockInfoOut) {
        String key = DirSystemConstant.DIR_CHECK_RETURN_RECEIVING + returnOrder.getBizOrgCode() +
                SystemConstant.COLON + returnOrder.getStoreCode() + SystemConstant.WAIT + returnOrder.getReturnOrderNo();
        if (!redisService.setIfAbsent(key, returnOrder.getReturnOrderNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("门店" + returnOrder.getStoreCode() + "短时间内异常重复收货" + returnOrder.getReturnOrderNo() + "，故判为无效提交！");
        }
        List<OrdDirReturnDetail> list = new ArrayList<>();
        // 批准退货总数量
        BigDecimal totalApprovalReturnQuantity = BigDecimal.ZERO;
        // 批准退货总金额
        BigDecimal totalApprovalReturnAmount = BigDecimal.ZERO;
        for (OrdDirReturnDetail returnGoodsInfoIn : returnGoodsInfoInList) {
            BigDecimal tax = returnGoodsInfoIn.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE)).add(BigDecimal.ONE);
            OrdDirReturnDetail ordDirReturnDetail = ordDirReturnDetailService.selectByPrimaryKey(returnGoodsInfoIn.getId());
            ordDirReturnDetail.setActualReturnQuantity(returnGoodsInfoIn.getActualReturnQuantity());
            ordDirReturnDetail.setActualPackageQuantity(returnGoodsInfoIn.getActualReturnQuantity().divide(ordDirReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_ZERO, RoundingMode.UP));
//            ordDirReturnDetail.setActualReturnAmount(returnGoodsInfoIn.getActualReturnQuantity().multiply(ordDirReturnDetail.getReturnUnitPrice()));
            //最新门店商品库存价
            BigDecimal stockPrice = warehouseServer.getStockPrice(returnOrder.getStoreCode(), returnGoodsInfoIn.getGoodsCode(), returnOrder.getBizOrgCode());
            if (Objects.isNull(stockPrice)) {
                throw new BusinessException("门店商品" + ordDirReturnDetail.getGoodsCode() + "库存价为空");
            }
            ordDirReturnDetail.setActualReturnAmount((stockPrice.multiply(returnGoodsInfoIn.getActualReturnQuantity())));
            //退货去税金额
            ordDirReturnDetail.setReturnExceptTaxAmount(ordDirReturnDetail.getActualReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //退货税额
            ordDirReturnDetail.setReturnTaxAmount(ordDirReturnDetail.getActualReturnAmount().subtract(ordDirReturnDetail.getReturnExceptTaxAmount()));
//            //获取最新门店配货价
//            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//            orderGoodsIn.setGoodsCode(returnGoodsInfoIn.getGoodsCode());
//            orderGoodsIn.setBizOrgCode(returnOrder.getBizOrgCode());
//            orderGoodsIn.setStoreCode(returnOrder.getStoreCode());
//            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
//            OrderGoodsOut orderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
//            //退货单已收货取最新门店配货价
//            if (Objects.nonNull(orderGoods)){
//                ordDirReturnDetail.setWrhPrice(orderGoods.getDistributionUnitPrice());
//                //配送价
//                ordDirReturnDetail.setDistributionPrice(orderGoods.getDistributionUnitPrice());
//            }
            //仓储成本金额
            BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(returnOrder.getWrhCode(), returnOrder.getStockCode(),
                    ordDirReturnDetail.getGoodsCode(), stockInfoOut.getBizOrgCode(), returnOrder.getStoreCode(), returnOrder.getBizOrgCode());
            if (Objects.isNull(warehousePrice)) {
                log.error("统配退货单{}商品{}仓储库存价为空", returnOrder.getReturnOrderNo(), ordDirReturnDetail.getGoodsCode());
                throw new BusinessException("退货单" + returnOrder.getReturnOrderNo() + "商品" + ordDirReturnDetail.getGoodsCode() + "仓储库存价为空");
            }
            ordDirReturnDetail.setWrhPrice(warehousePrice);
//            BigDecimal wrhCostAmount = ordDirReturnDetail.getWrhPrice().multiply(returnGoodsInfoIn.getActualReturnQuantity());
            ordDirReturnDetail.setWrhCostAmount(ordDirReturnDetail.getActualReturnAmount());
            //仓储成本去税金额
            ordDirReturnDetail.setWrhExceptTaxAmount(ordDirReturnDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //仓储成本税额
            ordDirReturnDetail.setWrhTaxAmount(ordDirReturnDetail.getWrhCostAmount().subtract(ordDirReturnDetail.getWrhExceptTaxAmount()));

            ordDirReturnDetail.setStoreStockPrice(stockPrice);
//            BigDecimal stockPrice = ordDirReturnDetail.getStoreStockPrice();
            //门店成本金额,退货单已收货门店财务库存减少。门店相关为负值
            ordDirReturnDetail.setStoreCostAmount((Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice).multiply(returnGoodsInfoIn.getActualReturnQuantity()).negate());
            //门店成本去税金额
            ordDirReturnDetail.setStoreExceptTaxAmount(ordDirReturnDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //门店成本税额
            ordDirReturnDetail.setStoreTaxAmount(ordDirReturnDetail.getStoreCostAmount().subtract(ordDirReturnDetail.getStoreExceptTaxAmount()));


            totalApprovalReturnAmount = totalApprovalReturnAmount.add(ordDirReturnDetail.getActualReturnAmount());
            totalApprovalReturnQuantity = totalApprovalReturnQuantity.add(ordDirReturnDetail.getActualReturnQuantity());
            list.add(ordDirReturnDetail);
        }
        returnOrder.setActualReturnAmount(totalApprovalReturnAmount);
        returnOrder.setActualReturnQuantity(totalApprovalReturnQuantity);
        ordDirReturnDetailService.batchUpdate(list);
        returnOrder.setReturnStatus(OrdReturnOrderStatusEnum.PROCESSED.getKey());
        returnOrder.setReceiveTime(LocalDateTime.now());
        ordDirReturnMapper.updateByPrimaryKeySelective(returnOrder);
        //调整库存
        this.adjustInv(returnOrder, list, stockInfoOut, returnOrder.getReceiveTime());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN.getName(), String.valueOf(returnOrder.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN.getCode(), OrdLogTypeEnum.ORD_DIR_RETURN_PROCESSED.getName(), new Date(), returnOrder.getUpdater());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("收货成功");
    }

    /**
     * 校验门店可用库存
     *
     * @param returnOrder
     * @param returnGoodsInfoInList
     * @return
     */
    private String checkInvStore(OrdDirReturn returnOrder, List<OrdDirReturnDetail> returnGoodsInfoInList) {
        if (stockStoreService.checkStockIsAllowNegative(returnOrder.getStoreCode(), returnOrder.getBizOrgCode())) {
            return null;
        }
        StringJoiner sj = new StringJoiner(SystemConstant.COMMA);
        if (CollectionUtils.isNotEmpty(returnGoodsInfoInList)) {
            returnGoodsInfoInList.forEach(returnDetail -> {
                BigDecimal invNum = ordDirReturnMapper.getInvStoreNum(returnDetail.getGoodsCode(), returnOrder.getBizOrgCode(), returnOrder.getStoreCode());
                if (Objects.isNull(invNum) || NumberUtil.INTEGER_ZERO > invNum.compareTo(returnDetail.getAuditReturnQuantity())) {
                    sj.add("商品【" + returnDetail.getGoodsCode() + "】" + returnDetail.getGoodsName() + "门店可用库存不足");
                }
            });
        }
        if (sj.length() > 0) {
            return sj.toString();
        }
        return null;
    }

    /**
     * 冲销退货单
     *
     * @param chargeReturnOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int chargeReturnOrder(ChargeReturnOrderIn chargeReturnOrderIn) {
        OrdDirReturn ordDirReturn = ordDirReturnMapper.selectByPrimaryKey(chargeReturnOrderIn.getReturnOrderId());
        if (Objects.isNull(ordDirReturn)) {
            throw new BusinessException("退货单不存在");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirReturn.getStockCode(), UserUtil.getBizOrgCode(), "退货单冲销");
        if (NumberUtil.INTEGER_ONE.equals(ordDirReturn.getIsReversal())) {
            throw new BusinessException("退货单已被红冲");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDirReturn.getIsReversalOrder())) {
            throw new BusinessException("红冲单不能被红冲");
        }
        if (!OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDirReturn.getReturnStatus())) {
            throw new BusinessException("已收货的退货单才可冲销");
        }
        //校验仓储库存
        String error = this.checkInvStock(ordDirReturn, chargeReturnOrderIn.getReturnGoodsInfoInList());
        if (StringUtils.isNotEmpty(error)) {
            throw new BusinessException(error);
        }
        //修改原退货单
        ordDirReturn.setIsReversal(NumberUtil.INTEGER_ONE);
        ordDirReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);

        //红冲单
        OrdDirReturn newOrdDirReturn = new OrdDirReturn();
        BeanUtils.copy(ordDirReturn, newOrdDirReturn);
        newOrdDirReturn.setApplyReturnQuantity(Objects.isNull(ordDirReturn.getApplyReturnQuantity()) ? BigDecimal.ZERO : ordDirReturn.getApplyReturnQuantity().negate());
        newOrdDirReturn.setApplyReturnAmount(Objects.isNull(ordDirReturn.getApplyReturnAmount()) ? BigDecimal.ZERO : ordDirReturn.getApplyReturnAmount().negate());
        newOrdDirReturn.setAuditReturnQuantity(Objects.isNull(ordDirReturn.getAuditReturnQuantity()) ? BigDecimal.ZERO : ordDirReturn.getAuditReturnQuantity().negate());
        newOrdDirReturn.setAuditReturnAmount(Objects.isNull(ordDirReturn.getAuditReturnAmount()) ? BigDecimal.ZERO : ordDirReturn.getAuditReturnAmount().negate());
        newOrdDirReturn.setActualReturnQuantity(Objects.isNull(ordDirReturn.getActualReturnQuantity()) ? BigDecimal.ZERO : ordDirReturn.getActualReturnQuantity().negate());
        newOrdDirReturn.setActualReturnAmount(Objects.isNull(ordDirReturn.getActualReturnAmount()) ? BigDecimal.ZERO : ordDirReturn.getActualReturnAmount().negate());
        newOrdDirReturn.setId(null);
        newOrdDirReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
        newOrdDirReturn.setIsReversalOrder(NumberUtil.INTEGER_ONE);
        newOrdDirReturn.setReceiveTime(LocalDateTime.now());
        newOrdDirReturn.setSourceReturnNo(ordDirReturn.getReturnOrderNo());
        newOrdDirReturn.setCreateTime(LocalDateTime.now());
        newOrdDirReturn.setUpdateTime(LocalDateTime.now());
        newOrdDirReturn.setReturnOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PT.getCode(), ordDirReturn.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR));
        //新增红冲退货单
        int count = this.insertSelective(newOrdDirReturn);
        //新增红冲单明细
        this.saveChargeOrdDirReturnDetail(newOrdDirReturn, chargeReturnOrderIn.getReturnGoodsInfoInList());

        //退货冲销调整库存
        this.adjustInv(newOrdDirReturn, chargeReturnOrderIn.getReturnGoodsInfoInList(), stockInfoOut, newOrdDirReturn.getReceiveTime());

        ordDirReturnMapper.updateByPrimaryKeySelective(ordDirReturn);
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_RETURN.getName(),
                String.valueOf(ordDirReturn.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN.getCode(),
                OrdLogTypeEnum.ORD_DIR_RETURN_CHARGE.getName(), new Date(), ordDirReturn.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }

    /**
     * 导入商品明细
     *
     * @param fileId
     * @param storeCode
     * @param bizOrgCode
     * @param warehouseCode
     * @param stockCode
     * @return
     */
    @Override
    public Response importReturnOrderGoods(String fileId, String storeCode, String bizOrgCode, String warehouseCode,
                                           String stockCode, String distributionType, String deliveryOrderNo, String centerStockBizOrgCode) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdReturnStoreGoodsListener listener = new OrdReturnStoreGoodsListener(orderGoodsServer, storeCode, bizOrgCode, warehouseServer,
                warehouseCode, stockCode, distributionType, centerStockBizOrgCode);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdReturnGoodsVO.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        if (StringUtils.isBlank(deliveryOrderNo)) {
            return Response.data(listener.getSaveReturnGoodsOut(), listener.message());
        }
        return this.handleImportReturnForDeliveryOrderNo(deliveryOrderNo, listener);
    }


    private Response<List<com.edc.erp.directly.returnorder.model.out.SaveReturnGoodsOut>> handleImportReturnForDeliveryOrderNo(String deliveryOrderNo, OrdReturnStoreGoodsListener listener) {
        List<com.edc.erp.directly.returnorder.model.out.SaveReturnGoodsOut> resultReturnGoodsOutList = Lists.newArrayList();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        OrdDirDeliveryOut ordDisDeliveryOut = ordDirDeliveryService.getDeliveryOrderOutByNo(deliveryOrderNo);
        String errorMessage = null;
        if (Objects.isNull(ordDisDeliveryOut) && StringUtils.isBlank(errorMessage)) {
            errorMessage = "配货单不存在此商品";
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDisDeliveryOut.getIsReversal()) && StringUtils.isBlank(errorMessage)) {
            errorMessage = "配货单已冲销";
        }
        if (!DeliveryOrderEnum.RECEIVED.getKey().equals(ordDisDeliveryOut.getDeliveryStatusCode()) && StringUtils.isBlank(errorMessage)) {
            errorMessage = "必须是已收货的配货单";
        }
        if (StringUtils.isNotBlank(errorMessage)) {
            String finalSameErrorMessage = errorMessage;
            listener.getSaveReturnGoodsOut().forEach(saveReturnGoodsOut -> {
                String mapKey = "第【" + saveReturnGoodsOut.getImportIndex() + "】行：";
                StringJoiner errorJoiner = totalErrorMap.get(mapKey);
                if (Objects.isNull(errorJoiner)) {
                    errorJoiner = new StringJoiner(SystemConstant.COMMA);
                }
                errorJoiner.add(finalSameErrorMessage);
                totalErrorMap.put("第【" + saveReturnGoodsOut.getImportIndex() + "】行：", errorJoiner);
            });
        } else {
            HashMap<String, OrdDirDeliveryDetail> detailMap = new HashMap<>();
            for (OrdDirDeliveryDetail detail : ordDisDeliveryOut.getDetailList()) {
                detailMap.put(detail.getGoodsCode(), detail);
            }
            listener.getSaveReturnGoodsOut().forEach(saveReturnGoodsOut -> {
                OrdDirDeliveryDetail detail = detailMap.get(saveReturnGoodsOut.getGoodsCode());
                String mapKey = "第【" + saveReturnGoodsOut.getImportIndex() + "】行：";
                StringJoiner errorJoiner = totalErrorMap.get(mapKey);
                if (Objects.isNull(errorJoiner)) {
                    errorJoiner = new StringJoiner(SystemConstant.COMMA);
                }
                if (Objects.isNull(detail)) {
                    errorJoiner.add("配货单不含此商品");
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
            OrdDirReturn ordDirReturn = ordDirReturnMapper.selectByPrimaryKey(item);
            if (Objects.isNull(ordDirReturn)) {
                continue;
            }
            StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirReturn.getStockCode(), UserUtil.getBizOrgCode(), "审核退货单");
            if (OrdReturnOrderStatusEnum.APPROVED.getKey().equals(ordDirReturn.getReturnStatus())) {
                continue;
            }
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDirReturn.getReturnStatus())) {
                continue;
            }
            List<OrdDirReturnDetail> details = ordDirReturnDetailService.findByReturnOrderId(item);
            if (CollectionUtils.isEmpty(details)) {
                continue;
            }
//            BigDecimal auditReturnQuantity = BigDecimal.ZERO;
//            BigDecimal auditReturnAmount = BigDecimal.ZERO;
            for (OrdDirReturnDetail detail : details) {
                detail.setAuditReturnAmount(detail.getApplyReturnAmount());
                detail.setAuditReturnQuantity(detail.getApplyReturnQuantity());
                detail.setAuditPackageQuantity(detail.getApplyPackageQuantity());
                ordDirReturnDetailService.updateByPrimaryKeySelective(OrdDirReturnDetail.builder().id(detail.getId())
                        .auditReturnAmount(detail.getApplyReturnAmount())
                        .auditReturnQuantity(detail.getApplyReturnQuantity())
                        .auditPackageQuantity(detail.getApplyPackageQuantity())
                        .build());
//                auditReturnQuantity = auditReturnQuantity.add(Objects.isNull(detail.getAuditReturnQuantity()) ? BigDecimal.ZERO : detail.getAuditReturnQuantity());
//                auditReturnAmount = auditReturnAmount.add(Objects.isNull(detail.getAuditReturnAmount()) ? BigDecimal.ZERO : detail.getAuditReturnAmount());
            }
//            ordDirReturn.setAuditReturnQuantity(auditReturnQuantity);
//            ordDirReturn.setAuditReturnAmount(auditReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            String key = DirSystemConstant.CHECK_DIR_RETURN_ORDER_AUDIT + ordDirReturn.getBizOrgCode() +
                    SystemConstant.COLON + ordDirReturn.getStoreCode() + SystemConstant.WAIT + ordDirReturn.getReturnOrderNo();
            if (!redisService.setIfAbsent(key, ordDirReturn.getReturnOrderNo(), 1L, TimeUnit.MINUTES)) {
                log.error("门店{}短时间内重复审核{}，故判为无效提交！", ordDirReturn.getStoreCode(), ordDirReturn.getReturnOrderNo());
            }
//            StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDirReturn.getStockCode());
            this.audit(ordDirReturn, details, stockInfoOut.getBizOrgCode());
            count++;
        }
        return count;
    }

    /**
     * 导出退货单
     *
     * @param ordReturnOrderPageIn
     * @return
     */
    @Override
    public String exportOrdReturn(OrdReturnOrderPageIn ordReturnOrderPageIn) {
        // 设置每次查询条数
        ordReturnOrderPageIn.setPageSize(10000);
        String title = "配货退货单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配货退货单列表",
                        // 导出模板实体
                        ExportOrdDirReturn.class,
                        // 分页查询对象
                        ordReturnOrderPageIn,
                        // 分页查询方法
                        page -> {
                            Page<BaseReturnOrderOut> baseReturnOrderForPage = this.findBaseReturnOrderForPage(ordReturnOrderPageIn);
                            List<ExportOrdDirReturn> exportOrdDirReturns = parseDataToExcel(baseReturnOrderForPage.getList());
                            log.info("导出配货退货单列表集合大小是--{}", exportOrdDirReturns.size());
                            return exportOrdDirReturns;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

//    /**
//     * 导入退货单及明细
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

    /**
     * 初始退货单
     *
     * @param ordDirReturn
     * @param detail
     * @return
     */
    @Override
    public List<OrdDirReturnDetail> initReturnOrder(OrdDirReturn ordDirReturn, List<UnificationReBillDtlVO> detail, String centerBizOrgCode) {
        OrdDirReturnDetail queryDtl = new OrdDirReturnDetail();
        queryDtl.setReturnOrderId(ordDirReturn.getId());
        queryDtl.setIsDelete(ModelConst.DELETE.NO);
        List<OrdDirReturnDetail> ordReturnDetailOuts = ordDirReturnDetailService.list(queryDtl);
        Map<String, OrdDirReturnDetail> collect = CollectionUtils.emptyIfNull(ordReturnDetailOuts).stream().collect(Collectors.toMap(OrdDirReturnDetail::getGoodsCode, item -> item));
        // 批准退货总数量
        BigDecimal totalApprovalReturnQuantity = BigDecimal.ZERO;
        // 批准退货总金额
        BigDecimal totalApprovalReturnAmount = BigDecimal.ZERO;
        List<OrdDirReturnDetail> details = new ArrayList<>();
        for (UnificationReBillDtlVO item : detail) {
            OrdDirReturnDetail ordDirReturnDetail = collect.get(item.getFarticlecode());
            //实际数量
            ordDirReturnDetail.setActualReturnQuantity(item.getFqty());
            //实际退货金额
//            ordDirReturnDetail.setActualReturnAmount(item.getFqty().multiply(ordDirReturnDetail.getReturnUnitPrice()));

            //最新门店商品库存价
            BigDecimal stockPrice = warehouseServer.getStockPrice(ordDirReturn.getStoreCode(), ordDirReturnDetail.getGoodsCode(), ordDirReturn.getBizOrgCode());
            if (Objects.isNull(stockPrice)) {
                throw new BusinessException("门店商品" + ordDirReturnDetail.getGoodsCode() + "库存价为空");
            }
            ordDirReturnDetail.setStoreStockPrice(stockPrice);
            //实际退货金额
            ordDirReturnDetail.setActualReturnAmount((stockPrice.multiply(item.getFqty())));
            ordDirReturnDetail.setWrhCostAmount(ordDirReturnDetail.getActualReturnAmount());
            //实际退货包装数
            ordDirReturnDetail.setActualPackageQuantity(ordDirReturnDetail.getActualReturnQuantity().divide(ordDirReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //1+税率
            BigDecimal sell = Objects.isNull(ordDirReturnDetail.getSellTax()) ? BigDecimal.ZERO : ordDirReturnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            BigDecimal tar = sell.add(BigDecimal.ONE);
            //实际退货去税金额
            ordDirReturnDetail.setReturnExceptTaxAmount(ordDirReturnDetail.getActualReturnAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            // 实际税额
            ordDirReturnDetail.setReturnTaxAmount(ordDirReturnDetail.getActualReturnAmount().subtract(ordDirReturnDetail.getReturnExceptTaxAmount()));

//            //获取最新门店配货价
//            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//            orderGoodsIn.setGoodsCode(ordDirReturnDetail.getGoodsCode());
//            orderGoodsIn.setBizOrgCode(ordDirReturn.getBizOrgCode());
//            orderGoodsIn.setStoreCode(ordDirReturn.getStoreCode());
//            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
//            OrderGoodsOut orderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
//            //退货单已收货取最新门店配货价
//            if (Objects.nonNull(orderGoods)){
//                ordDirReturnDetail.setWrhPrice(orderGoods.getDistributionUnitPrice());
            //仓储成本金额
            BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(ordDirReturn.getWrhCode(), ordDirReturn.getStockCode(),
                    ordDirReturnDetail.getGoodsCode(), centerBizOrgCode, ordDirReturn.getStoreCode(), ordDirReturn.getBizOrgCode());
            if (Objects.isNull(warehousePrice)) {
                log.error("统配退货单{}商品{}仓储库存价为空", ordDirReturn.getReturnOrderNo(), ordDirReturnDetail.getGoodsCode());
                throw new BusinessException("退货单" + ordDirReturn.getReturnOrderNo() + "商品" + ordDirReturnDetail.getGoodsCode() + "仓储库存价为空");
            }
            ordDirReturnDetail.setWrhPrice(warehousePrice);
//            BigDecimal warehousePrice = ordDirReturnDetail.getWrhPrice();
//            BigDecimal wrhCostAmount = warehousePrice.multiply(ordDirReturnDetail.getActualReturnQuantity());
//            ordDirReturnDetail.setWrhCostAmount(wrhCostAmount);
            //仓储成本去税金额
            ordDirReturnDetail.setWrhExceptTaxAmount(ordDirReturnDetail.getWrhCostAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //仓储成本税额
            ordDirReturnDetail.setWrhTaxAmount(ordDirReturnDetail.getWrhCostAmount().subtract(ordDirReturnDetail.getWrhExceptTaxAmount()));
//            }
//            BigDecimal stockPrice = ordDirReturnDetail.getStoreStockPrice();
            //门店成本金额,退货单已收货门店财务库存减少。门店相关为负值
            ordDirReturnDetail.setStoreCostAmount((Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice).multiply(ordDirReturnDetail.getActualReturnQuantity()).negate());
            //门店成本去税金额
            ordDirReturnDetail.setStoreExceptTaxAmount(ordDirReturnDetail.getStoreCostAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //门店成本税额
            ordDirReturnDetail.setStoreTaxAmount(ordDirReturnDetail.getStoreCostAmount().subtract(ordDirReturnDetail.getStoreExceptTaxAmount()));

            totalApprovalReturnAmount = totalApprovalReturnAmount.add(ordDirReturnDetail.getActualReturnAmount());

            totalApprovalReturnQuantity = totalApprovalReturnQuantity.add(ordDirReturnDetail.getActualReturnQuantity());
            details.add(ordDirReturnDetail);
        }
        ordDirReturn.setActualReturnAmount(totalApprovalReturnAmount);
        ordDirReturn.setActualReturnQuantity(totalApprovalReturnQuantity);
        return details;
    }

    /**
     * 查退货商品信息
     *
     * @param orderGoodsIn
     * @return
     */
    @Override
    public Response<SaveReturnGoodsOut> getGoodsInfo(OrderGoodsIn orderGoodsIn, String centerStockBizOrgCode) {
        String channelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(orderGoodsIn.getStoreCode(), UserUtil.getBizOrgCode());
        orderGoodsIn.setBizOrgCode(channelBizOrgCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoodsForBackReturn(orderGoodsIn);
        if (Objects.isNull(orderGoodsOut)) {
            return Response.error("商品不存在或者不允许配货退货");
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
            saveReturnGoodsOut.setDistributionSpecificationNum(Objects.isNull(orderGoodsOut.getDistributionSpecification()) ? BigDecimal.ZERO : BigDecimal.valueOf(orderGoodsOut.getDistributionSpecification().getQpc()));
        }
        BigDecimal storeStockPrice = warehouseServer.getStockPrice(orderGoodsIn.getStoreCode(), orderGoodsIn.getGoodsCode(), channelBizOrgCode);
        saveReturnGoodsOut.setReturnUnitPrice(storeStockPrice);
//        saveReturnGoodsOut.setReturnUnitPrice(orderGoodsOut.getDistributionUnitPrice());
        saveReturnGoodsOut.setDistributionPrice(orderGoodsOut.getDistributionUnitPrice());
        saveReturnGoodsOut.setSellTax(orderGoodsOut.getOutTax());
        BigDecimal stockPrice = warehouseServer.getStockPrice(orderGoodsIn.getStoreCode(), orderGoodsIn.getGoodsCode(), channelBizOrgCode);
        saveReturnGoodsOut.setStoreStockPrice(null != stockPrice ? stockPrice : BigDecimal.ZERO);
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
//        String key = importOrdReturnOrderVO.getStoreCode() + SystemConstant.COMMA + importOrdReturnOrderVO.getStockCode()
//                + SystemConstant.COMMA + importOrdReturnOrderVO.getWarehouseCode() + SystemConstant.COMMA + orderGoodsOut.getDistributionWay();
//        return key;
//    }

//    /**
//     * 数据入库
//     *
//     * @param listener
//     * @param bizOrgCode
//     */
//    private void initData(OrdReturnOrderListener listener, String bizOrgCode) {
//        List<ImportOrdReturnOrderVO> importOrdReturnOrderList = listener.getImportOrdReturnOrderList();
////        Map<String, List<ImportOrdReturnOrderVO>> returnOrderInfo = importOrdReturnOrderList.stream().collect(Collectors.groupingBy(item ->
////                item.getStoreCode() + "," + item.getStockCode() + "," + item.getWarehouseCode()));
//        Map<String, List<ImportOrdReturnOrderVO>> returnOrderInfo = importOrdReturnOrderList.stream()
//                .collect(Collectors.groupingBy(item -> this.getSplitKey(item, bizOrgCode)));
//        for (Map.Entry<String, List<ImportOrdReturnOrderVO>> entry : returnOrderInfo.entrySet()) {
//            List<ImportOrdReturnOrderVO> value = entry.getValue();
//            String[] splits = entry.getKey().split(",");
//            OrdSaveReturnOrderIn ordSaveReturnOrderIn = new OrdSaveReturnOrderIn();
//            ordSaveReturnOrderIn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
//            ordSaveReturnOrderIn.setStoreCode(splits[0]);
//            ordSaveReturnOrderIn.setStockCode(splits[1]);
//            ordSaveReturnOrderIn.setWarehouseCode(splits[2]);
//            ordSaveReturnOrderIn.setDistributionType(DistributionWaysEnum.getNameByType(splits[3]));
//            ordSaveReturnOrderIn.setBizOrgCode(bizOrgCode);
//            ordSaveReturnOrderIn.setReturnOrderReason(OrdReturnOrderReasonEnum.PREVIOUS_RETURN.getDictVlueCode());
//            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(splits[0]);
//            ordSaveReturnOrderIn.setStoreName(Objects.isNull(storeOut) ? "" : storeOut.getStoreName());
//            ordSaveReturnOrderIn.setStoreArea(storeOut.getBelongArea());
//            List<OrdDirReturnDetail> ordDisReturnDetailList = new ArrayList<>();
//            //明细数据封装
//            for (ImportOrdReturnOrderVO importOrdReturnOrderVO : value) {
//                OrdDirReturnDetail ordDisReturnDetail = this.detail(importOrdReturnOrderVO, bizOrgCode);
//                ordDisReturnDetailList.add(ordDisReturnDetail);
//            }
//            ordSaveReturnOrderIn.setReturnGoodsInfoInList(ordDisReturnDetailList);
//            this.saveOrUpdateReturnOrder(ordSaveReturnOrderIn, false);
//        }
//    }

//    private OrdDirReturnDetail detail(ImportOrdReturnOrderVO importOrdReturnOrderVO, String bizOrgCode) {
//        OrdDirReturnDetail ordDirReturnDetail = new OrdDirReturnDetail();
//        ordDirReturnDetail.setGoodsCode(importOrdReturnOrderVO.getGoodsCode());
//        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//        orderGoodsIn.setGoodsCode(importOrdReturnOrderVO.getGoodsCode());
//        orderGoodsIn.setBizOrgCode(bizOrgCode);
//        orderGoodsIn.setStoreCode(importOrdReturnOrderVO.getStoreCode());
//        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
//        OrderGoodsOut orderGoods = orderGoodsServer.getStoreOrderGoodsForBackReturn(orderGoodsIn);
//        if (Objects.isNull(orderGoods)) {
//            throw new BusinessException("商品不存在或者不允许配货退货");
//        }
//        ordDirReturnDetail.setGoodsName(orderGoods.getGoodsName());
//        ordDirReturnDetail.setBarCode(orderGoods.getBarCode());
//        ordDirReturnDetail.setOrgGoodsId(orderGoods.getOrgGoodsId());
//        ordDirReturnDetail.setGoodsType(orderGoods.getGoodsType());
//        ordDirReturnDetail.setRemark(importOrdReturnOrderVO.getRemark());
//        ordDirReturnDetail.setVendorCode(orderGoods.getVendorCode());
//        ordDirReturnDetail.setDistributionSpecification(Objects.isNull(orderGoods.getDistributionSpecification()) ? "" : orderGoods.getDistributionSpecification().getQpcStr());
//        ordDirReturnDetail.setDistributionSpecificationUnit(Objects.isNull(orderGoods.getDistributionSpecification()) ? "" : orderGoods.getDistributionSpecification().getUnitName());
//        ordDirReturnDetail.setDistributionSpecificationNum(Objects.isNull(orderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : BigDecimal.valueOf(orderGoods.getDistributionSpecification().getQpc()));
//        ordDirReturnDetail.setReturnUnitPrice(orderGoods.getDistributionUnitPrice());
//        ordDirReturnDetail.setDistributionPrice(orderGoods.getDistributionUnitPrice());
//        //申请
//        ordDirReturnDetail.setApplyReturnQuantity(importOrdReturnOrderVO.getApplyReturnQuantity());
//        ordDirReturnDetail.setApplyPackageQuantity(ordDirReturnDetail.getApplyReturnQuantity().divide(new BigDecimal(orderGoods.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//        ordDirReturnDetail.setApplyReturnAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(importOrdReturnOrderVO.getApplyReturnQuantity()));
////        //审核
////        ordDirReturnDetail.setAuditReturnQuantity(ordDirReturnDetail.getApplyReturnQuantity());
////        ordDirReturnDetail.setAuditPackageQuantity(ordDirReturnDetail.getApplyPackageQuantity());
////        ordDirReturnDetail.setAuditReturnAmount(ordDirReturnDetail.getApplyReturnAmount());
////        //实际
////        ordDirReturnDetail.setActualReturnQuantity(ordDirReturnDetail.getApplyReturnQuantity());
////        ordDirReturnDetail.setActualPackageQuantity(ordDirReturnDetail.getApplyPackageQuantity());
////        ordDirReturnDetail.setActualReturnAmount(ordDirReturnDetail.getApplyReturnAmount());
//        BigDecimal stockPrice = warehouseServer.getStockPrice(importOrdReturnOrderVO.getStoreCode(), importOrdReturnOrderVO.getGoodsCode(), bizOrgCode);
//        ordDirReturnDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
//        BigDecimal warehousePrice = warehouseServer.getWarehousePrice(importOrdReturnOrderVO.getWarehouseCode(), importOrdReturnOrderVO.getStockCode(), importOrdReturnOrderVO.getGoodsCode(), bizOrgCode, ordDirReturnDetail.getVendorCode());
//        ordDirReturnDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);
//        ordDirReturnDetail.setSellTax(orderGoods.getOutTax());
//        BigDecimal sellTax = ordDirReturnDetail.getSellTax() == null ? BigDecimal.ZERO : ordDirReturnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
//        //1+税率
//        BigDecimal tax = sellTax.add(BigDecimal.ONE);
//        //退货去税金额
//        ordDirReturnDetail.setReturnExceptTaxAmount(ordDirReturnDetail.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//        //退货税额
//        ordDirReturnDetail.setReturnTaxAmount(ordDirReturnDetail.getApplyReturnAmount().subtract(ordDirReturnDetail.getReturnExceptTaxAmount()));
//        //仓储成本金额
//        ordDirReturnDetail.setWrhCostAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()));
//        //仓储成本去税金额
//        ordDirReturnDetail.setWrhExceptTaxAmount(ordDirReturnDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//        //仓储成本税额
//        ordDirReturnDetail.setWrhTaxAmount(ordDirReturnDetail.getWrhCostAmount().subtract(ordDirReturnDetail.getWrhExceptTaxAmount()));
//        //门店成本金额
//        ordDirReturnDetail.setStoreCostAmount(ordDirReturnDetail.getStoreStockPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()));
//        //门店成本去税金额
//        ordDirReturnDetail.setStoreExceptTaxAmount(ordDirReturnDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//        //门店成本税额
//        ordDirReturnDetail.setStoreTaxAmount(ordDirReturnDetail.getStoreCostAmount().subtract(ordDirReturnDetail.getStoreExceptTaxAmount()));
//
//        return ordDirReturnDetail;
//    }

    private List<ExportOrdDirReturn> parseDataToExcel(List<BaseReturnOrderOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> converExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 转换excel参数
     *
     * @param baseReturnOrderOut
     * @param i
     * @return
     */
    private ExportOrdDirReturn converExcel(BaseReturnOrderOut baseReturnOrderOut, int i) {
        ExportOrdDirReturn exportOrdDirReturn = new ExportOrdDirReturn();
        BeanUtils.copy(baseReturnOrderOut, exportOrdDirReturn);
        exportOrdDirReturn.setReturnStatus(OrdReturnOrderStatusEnum.getValueByKey(baseReturnOrderOut.getReturnStatus()));
        exportOrdDirReturn.setIsReversal(baseReturnOrderOut.getIsReversal().equals(NumberUtil.INTEGER_ONE) ? "是" : "否");
        exportOrdDirReturn.setIsReversalOrder(baseReturnOrderOut.getIsReversalOrder().equals(NumberUtil.INTEGER_ONE) ? "是" : "否");
        exportOrdDirReturn.setReturnType(OrdReturnOrderTypeEnum.getValueByKey(baseReturnOrderOut.getReturnType()));
        exportOrdDirReturn.setWrh(StringUtils.isNotBlank(baseReturnOrderOut.getWrhCode()) ? "【" + baseReturnOrderOut.getWrhCode() + "】" + baseReturnOrderOut.getWrhName() : "");
        exportOrdDirReturn.setStock(StringUtils.isNotBlank(baseReturnOrderOut.getStockCode()) ? "【" + baseReturnOrderOut.getStockCode() + "】" + baseReturnOrderOut.getStockName() : "");
        exportOrdDirReturn.setIndex(i + 1);
        exportOrdDirReturn.setStoreArea(StringUtils.isNotBlank(baseReturnOrderOut.getStoreAreaName()) ? baseReturnOrderOut.getStoreAreaName() + "【" + baseReturnOrderOut.getStoreArea() + "】" : "");
        return exportOrdDirReturn;
    }


//    /**
//     * 退货冲销资金变动
//     *
//     * @param ordDirReturn
//     */
//    private void chargeReturnOrderToFund(OrdDirReturn ordDirReturn) {
//        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
//        rechargeLiquidationIn.setBizOrgCode(ordDirReturn.getBizOrgCode());
//        rechargeLiquidationIn.setBusinessType(FundTypeEnum.DISTRIBUTION_RETURN.getCode());
//        rechargeLiquidationIn.setBusinessNo(ordDirReturn.getReturnOrderNo());
//        rechargeLiquidationIn.setLiquidationAmount(ordDirReturn.getActualReturnAmount());
//        rechargeLiquidationIn.setPayOrPrincipalCode(ordDirReturn.getStoreCode());
//        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
//        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
//        if (!response.isSuccess()) {
//            throw new BusinessException(response.getMessage());
//        }
//    }

//    /**
//     * 收货资金变动
//     *
//     * @param ordDirReturn
//     */
//    private void receivingReturnOrderToFund(OrdDirReturn ordDirReturn) {
//        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
//        rechargeLiquidationIn.setBizOrgCode(ordDirReturn.getBizOrgCode());
//        rechargeLiquidationIn.setBusinessType(FundTypeEnum.DISTRIBUTION_RETURN.getCode());
//        rechargeLiquidationIn.setBusinessNo(ordDirReturn.getReturnOrderNo());
//        rechargeLiquidationIn.setLiquidationAmount(ordDirReturn.getActualReturnAmount());
//        rechargeLiquidationIn.setRecipientPrincipalCode(ordDirReturn.getStoreCode());
//        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
//        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
//        if (!response.isSuccess()) {
//            throw new BusinessException(response.getMessage());
//        }
//    }

    @Transactional(rollbackFor = Exception.class)
    public void saveChargeOrdDirReturnDetail(OrdDirReturn newOrdDirReturn, List<OrdDirReturnDetail> returnGoodsInfoInList) {
        //退货详情单
        returnGoodsInfoInList.forEach(returnDetail -> {
            returnDetail.setReturnOrderId(newOrdDirReturn.getId());
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
            returnDetail.setCreator(newOrdDirReturn.getCreator());
            returnDetail.setCreateTime(LocalDateTime.now());
        });

        ordDirReturnDetailService.batchSave(returnGoodsInfoInList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustInv(OrdDirReturn ordDirReturn, List<OrdDirReturnDetail> returnGoodsInfoInList, StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        //异步调整门店库存
        StockFlowIn storeFlowIn = this.initStoreStockCharge(ordDirReturn, returnGoodsInfoInList, flowDate);
        //异步调仓储库存
        StockFlowIn wareFlowIn = this.initWarehouseStockCharge(ordDirReturn, returnGoodsInfoInList, stockInfoOut, flowDate);
        List<StockFlowIn> stockFlowInList = Arrays.asList(storeFlowIn, wareFlowIn);
        Response response = stockFlowService.checkStockFlow(stockFlowInList);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
    }

    /**
     * 异步调仓储库存
     *
     * @param ordDirReturn
     * @param returnGoodsInfoInList
     * @return
     */
    private StockFlowIn initWarehouseStockCharge(OrdDirReturn ordDirReturn, List<OrdDirReturnDetail> returnGoodsInfoInList, StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_RETREAT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_RETREAT.getName());
        stockFlowIn.setFlowDate(flowDate);
        stockFlowIn.setCreator(ordDirReturn.getCreator());
        stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        //库存发生位置
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        stockFlowIn.setSourceNo(ordDirReturn.getReturnOrderNo());
        List<StockFlowGoodsIn> stockFlowGoodsInList = new ArrayList<>();
        returnGoodsInfoInList.forEach(returnDetail -> {
            BigDecimal sell = Objects.isNull(returnDetail.getSellTax()) ? BigDecimal.ZERO : returnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(returnDetail, stockFlowGoodsIn);
            stockFlowGoodsIn.setWarehouseCode(ordDirReturn.getWrhCode());
            stockFlowGoodsIn.setStockCode(ordDirReturn.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setSourceNo(ordDirReturn.getReturnOrderNo());
            stockFlowGoodsIn.setPrice(returnDetail.getReturnUnitPrice());

            //已收货,实际数量增加
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDirReturn.getReturnStatus()) && NumberUtil.INTEGER_ZERO.equals(ordDirReturn.getIsReversalOrder())) {
                stockFlowIn.setOperationType(OrderTypeEnum.COLLECTED.getCode());
                //财务     ---实际数
                stockFlowGoodsIn.setActualQty(returnDetail.getActualReturnQuantity().abs());
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
                stockFlowGoodsIn.setIsBusinessQty("Y");
                //统配退已收货，仓储库存调整发生价取最新门店配货价
//                stockFlowGoodsIn.setPrice(returnDetail.getDistributionPrice());
            }
            //已收货后冲销
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDirReturn.getReturnStatus()) && NumberUtil.INTEGER_ONE.equals(ordDirReturn.getIsReversalOrder())) {
                stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
                //财务     ---实际数
                stockFlowGoodsIn.setActualQty(returnDetail.getActualReturnQuantity().abs());
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
                stockFlowGoodsIn.setIsBusinessQty("Y");
                //统配退已收货后冲销，仓储库存调整发生价取原单门店配货价
//                stockFlowGoodsIn.setPrice(returnDetail.getDistributionPrice());
            }
            //收货或冲销原单据和冲销单据正负值和调库存一致，直接取原单值
            //成本含税金额
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
     * 异步调门店库存+
     *
     * @param ordDirReturn
     * @param returnGoodsInfoInList
     * @return
     */
    private StockFlowIn initStoreStockCharge(OrdDirReturn ordDirReturn, List<OrdDirReturnDetail> returnGoodsInfoInList, LocalDateTime flowDate) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(ordDirReturn.getBizOrgCode());
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_RETREAT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_RETREAT.getName());
        stockFlowIn.setCreator(ordDirReturn.getCreator());
        stockFlowIn.setOrgCode(ordDirReturn.getOrgCode());
        stockFlowIn.setFlowDate(flowDate);
        //库存发生位置
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        stockFlowIn.setSourceNo(ordDirReturn.getReturnOrderNo());
        List<StockFlowGoodsIn> stockFlowInList = new ArrayList<>();
        returnGoodsInfoInList.forEach(returnDetail -> {
            BigDecimal sell = Objects.isNull(returnDetail.getSellTax()) ? BigDecimal.ZERO : returnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(returnDetail, stockFlowGoodsIn);
            stockFlowGoodsIn.setSourceNo(ordDirReturn.getReturnOrderNo());
            stockFlowGoodsIn.setStoreCode(ordDirReturn.getStoreCode());
            stockFlowGoodsIn.setStoreName(ordDirReturn.getStoreName());
            stockFlowGoodsIn.setStockCode(ordDirReturn.getStockCode());
            stockFlowGoodsIn.setPrice(returnDetail.getReturnUnitPrice());

            //已审核状态,批准数量减少
            if (OrdReturnOrderStatusEnum.APPROVED.getKey().equals(ordDirReturn.getReturnStatus())) {
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
                stockFlowGoodsIn.setTaxAmount(returnDetail.getActualReturnAmount());
                // 税额
                stockFlowGoodsIn.setTax(returnDetail.getReturnTaxAmount());

            }
            //已作废,批准数量增加
            if (OrdReturnOrderStatusEnum.INVALID.getKey().equals(ordDirReturn.getReturnStatus())) {
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
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDirReturn.getReturnStatus()) && NumberUtil.INTEGER_ZERO.equals(ordDirReturn.getIsReversalOrder())) {
                stockFlowIn.setOperationType(OrderTypeEnum.COLLECTED.getCode());
                //财务     ---实际数
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
            if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDirReturn.getReturnStatus()) && NumberUtil.INTEGER_ONE.equals(ordDirReturn.getIsReversalOrder())) {
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
                //退货收货后冲销门店库存调整发生价取原单门店库存价
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

    private String checkInvStock(OrdDirReturn ordDirReturn, List<OrdDirReturnDetail> returnGoodsInfoInList) {
        StringJoiner sj = new StringJoiner(SystemConstant.COMMA);
        if (CollectionUtils.isNotEmpty(returnGoodsInfoInList)) {
            returnGoodsInfoInList.forEach(returnDetail -> {
                BigDecimal invNum = ordDirReturnMapper.getInvNum(returnDetail.getGoodsCode(), ordDirReturn.getStockCode(), ordDirReturn.getWrhCode());
                if (Objects.isNull(invNum) || NumberUtil.INTEGER_ZERO > invNum.compareTo(returnDetail.getActualReturnQuantity())) {
                    sj.add("商品【" + returnDetail.getGoodsCode() + "】" + returnDetail.getGoodsName() + "仓储可用库存不足");
                }
            });
        }
        if (sj.length() > 0) {
            return sj.toString();
        }
        return null;
    }

    /**
     * 下发dts
     *
     * @param returnGoodsInfoInList
     * @param ordDirReturn
     */
    private void initReturnOrderToDts(List<OrdDirReturnDetail> returnGoodsInfoInList, OrdDirReturn ordDirReturn, String centerStockBizOrgCode) {
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDirReturn.getStoreCode());
        if (Objects.isNull(storeOut)) {
            throw new BusinessException("获取门店信息失败，请稍候重试");
        }
        UnificationReBillIn unificationReBillIn = new UnificationReBillIn();
        //单号
        unificationReBillIn.setPlatform_bill_id(ordDirReturn.getReturnOrderNo());
        //仓储代码
        unificationReBillIn.setWarehouse_id(ordDirReturn.getWrhCode());
        //仓位代码
        unificationReBillIn.setSource_stock_id(ordDirReturn.getStockCode());
        //门店id
        unificationReBillIn.setShop_id(storeOut.getStoreId().toString());
        //门店代码
        unificationReBillIn.setShop_code(ordDirReturn.getStoreCode());
        //填单人
        unificationReBillIn.setCreater(ordDirReturn.getAuditor());
        //生成时间
        unificationReBillIn.setGenerate_time(ordDirReturn.getUpdateTime());
        //配货方式
        unificationReBillIn.setAlc(DistributionWaysEnum.UNIFIEDDIS.getType());
        //退货日期
        unificationReBillIn.setBill_create_date(LocalDateTime.now().toLocalDate());
        //明细
        List<UnificationReBillDtlIn> detail = this.initUnificationReBillDtlIn(ordDirReturn, returnGoodsInfoInList);
        if (CollectionUtils.isEmpty(detail)) {
            log.info("门店{}退货单{}无明细，不下发DTS", ordDirReturn.getStoreCode(), ordDirReturn.getReturnOrderNo());
            return;
        }
        unificationReBillIn.setDetail_list(detail);
        //来源组织
        unificationReBillIn.setSource_organization(centerStockBizOrgCode);
        //目标组织
        unificationReBillIn.setTarget_organization(centerStockBizOrgCode);
        //发送时间
        unificationReBillIn.setSend_time(LocalDateTime.now());
//        unificationReBillIn.setMemo(OrdReturnOrderTypeEnum.getValueByKey(ordDirReturn.getReturnType()));
        unificationReBillIn.setMemo(systemDictService.getSystemDictName(ordDirReturn.getReturnOrderReason()));
        //下发dts
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_RETURN_TO_DTS, JSON.toJSONString(unificationReBillIn), ordDirReturn.getBizOrgCode(), ordDirReturn.getReturnOrderNo());
        SendResponse sendResponse = dirReturnToDtsSender.sendSync(JSON.toJSONString(unificationReBillIn).getBytes(), System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("统配退{}下发DTS消息ID---{}", ordDirReturn.getReturnOrderNo(), sendResponse.getMessageId());
    }

    private List<UnificationReBillDtlIn> initUnificationReBillDtlIn(OrdDirReturn ordDirReturn, List<OrdDirReturnDetail> returnGoodsInfoInList) {
        List<UnificationReBillDtlIn> dtlInList = new ArrayList<>();
        List<String> vendorCodeList = returnGoodsInfoInList.stream().map(OrdDirReturnDetail::getVendorCode).collect(Collectors.toList());
        FindVendorTransIn findVendorTransIn = new FindVendorTransIn();
        findVendorTransIn.setVendorCodes(vendorCodeList);
//        findVendorTransIn.setBizOrgCode(ordDirReturn.getBizOrgCode());
        Response<List<VendorTransInfoVO>> vendorResponse = purchaseOrderClient.findTransByCodes(findVendorTransIn);
        if (!vendorResponse.isSuccess()) {
            throw new BusinessException("获取订单方异常");
        }
        Map<String, VendorTransInfoVO> vendorTransInfoVOMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(vendorResponse.getData())) {
            vendorTransInfoVOMap = vendorResponse.getData().stream().collect(Collectors.toMap(VendorTransInfoVO::getVendorCode, Function.identity()));
        }
        for (OrdDirReturnDetail item : returnGoodsInfoInList) {
            if (BigDecimal.ZERO.compareTo(item.getAuditReturnQuantity()) > -1) {
                continue;
            }
            OrgGoodsTransInfo goodsOut = orderGoodsServer.getGoodsOut(item.getGoodsCode(), ordDirReturn.getBizOrgCode());
            UnificationReBillDtlIn dtlIn = new UnificationReBillDtlIn();
            dtlIn.setPlatform_bill_id(ordDirReturn.getReturnOrderNo());
            dtlIn.setSku_id(Objects.isNull(goodsOut) ? "" : goodsOut.getId().toString());
            dtlIn.setQuantity(item.getAuditReturnQuantity());
            dtlIn.setLine(item.getLine());
            dtlIn.setSku_code(Objects.isNull(goodsOut) ? "" : goodsOut.getGoodsCode());
            dtlIn.setPrice_i(item.getReturnUnitPrice());
            dtlIn.setSource_organization(ordDirReturn.getBizOrgCode());
            dtlIn.setTarget_organization(ordDirReturn.getBizOrgCode());
//            GoodsDistributionPlanGroupDetailsVendorOut planCodeByGoodsCode = goodsDistributionPlanGroupDetailsService.getPlanCodeByGoodsCode(item.getGoodsCode(), ordDirReturn.getBizOrgCode(), alcSchemeCode);
            VendorTransInfoVO vendorTransInfoVO = vendorTransInfoVOMap.get(item.getVendorCode());
            if (Objects.isNull(vendorTransInfoVO)) {
                throw new BusinessException("订单方" + item.getVendorCode() + "ID不存在");
            }
            dtlIn.setSupplier_id(vendorTransInfoVO.getVendorId().toString());
            if (StringUtils.isNotEmpty(item.getReturnReason())) {
                InvBizRsnTransOut invBizRsnTransOut = equipmentBusinessReasonServer.getStoreInvBizRsnTransByCode(item.getReturnReason(), ordDirReturn.getBizOrgCode(), NumberUtil.INTEGER_ZERO);
                dtlIn.setMemo(Objects.isNull(invBizRsnTransOut) ? item.getReturnReason() : invBizRsnTransOut.getBusinessReasonName());
            }
            dtlInList.add(dtlIn);
        }
        return dtlInList;

    }

    @Override
    public OrdDirReturn getReturnOrderById(Integer returnOrderId) {
        return ordDirReturnMapper.selectByPrimaryKey(returnOrderId);
    }

    /**
     * 退货通知单保存退货单
     *
     * @param ordSaveReturnOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response saveAndUpdateReturnOrderByReturnNotice(AppDirReturnOrderSaveIn ordSaveReturnOrderIn) {
        Integer returnOrderId = ordSaveReturnOrderIn.getReturnOrderId();
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordSaveReturnOrderIn.getStoreCode());
        if (null == returnOrderId) {
            //第一次
            this.saveReturnOrderData(ordSaveReturnOrderIn, storeOut);
        } else {
            //非第一次
            OrdDirReturn ordDirReturn = this.getReturnOrderByIdAndOrgCode(returnOrderId, ordSaveReturnOrderIn.getBizOrgCode());
            ordDirReturnDetailService.deleteByReturnOrderId(ordDirReturn);
            List<AppSaveOrdDirReturnDetailIn> ordDirReturnDetails = this.initReturnOrderDetailList(ordSaveReturnOrderIn, storeOut, ordDirReturn);
            ordDirReturn.setUpdater(storeOut.getStoreCode());
            ordDirReturn.setUpdateTime(LocalDateTime.now());
            ordDirReturn.setSkuCount(ordDirReturnDetails.size());
            ordDirReturn.setApplyReturnQuantity(ordDirReturnDetails.stream().filter(detail -> Objects.nonNull(detail.getApplyReturnQuantity())).map(OrdDirReturnDetail::getApplyReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            ordDirReturn.setApplyReturnAmount(ordDirReturnDetails.stream().filter(detail -> Objects.nonNull(detail.getApplyReturnAmount())).map(OrdDirReturnDetail::getApplyReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            ordDirReturn.setAuditReturnQuantity(ordDirReturnDetails.stream().filter(detail -> Objects.nonNull(detail.getAuditReturnQuantity())).map(OrdDirReturnDetail::getAuditReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            ordDirReturn.setAuditReturnAmount(ordDirReturnDetails.stream().filter(detail -> Objects.nonNull(detail.getAuditReturnAmount())).map(OrdDirReturnDetail::getAuditReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            ordDirReturn.setAppRemark(ordSaveReturnOrderIn.getAppRemark());
            ordDirReturn.setDeliveryOrderNo(ordSaveReturnOrderIn.getDeliveryOrderNo());
            ordDirReturnMapper.updateByPrimaryKey(ordDirReturn);
        }
        return Response.success("保存成功");
    }

    /**
     * 查询直营退货单列表(库存盘点)
     *
     * @param ordDirReturn
     * @return
     */
    @Override
    public List<BaseReturnOrderOut> findDirReturnOrder(OrdDirReturn ordDirReturn) {
        ordDirReturn.setReturnStatus(OrdReturnOrderStatusEnum.APPROVED.getKey());
        List<BaseReturnOrderOut> returnOrderOutList = ordDirReturnMapper.findDirReturnOrder(ordDirReturn);
        returnOrderOutList.forEach(returnOrderOut -> {
            returnOrderOut.setReturnStatusValue(OrdReturnOrderStatusEnum.getValueByKey(returnOrderOut.getReturnStatus()));
            StockInfoOut stockInfoOut = stockServer.getTransInfo(returnOrderOut.getStockCode());
            returnOrderOut.setStockName(Objects.isNull(stockInfoOut) ? "" : stockInfoOut.getStockName());
            returnOrderOut.setReturnTypeValue(OrdReturnOrderTypeEnum.getValueByKey(returnOrderOut.getReturnType()));
        });
        return returnOrderOutList;
    }

    @Override
    public OrdDirReturn getReturnOrderByIdAndOrgCode(Integer returnOrderId, String bizOrgCode) {
        OrdDirReturn returnOrder = new OrdDirReturn();
        returnOrder.setId(returnOrderId);
        returnOrder.setBizOrgCode(bizOrgCode);
        returnOrder.setIsDelete(ModelConst.DELETE.NO);
        return ordDirReturnMapper.selectOne(returnOrder);
    }

    /**
     * 退货单回传
     *
     * @param unificationReBillVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unificationReOrderCallBack(UnificationReBillVO unificationReBillVO) {
        OrdDirReturn query = new OrdDirReturn();
        List<UnificationReBillDtlVO> detail = unificationReBillVO.getDetail();
        query.setReturnOrderNo(unificationReBillVO.getFsrcnum());
        OrdDirReturn ordDirReturn = this.selectOne(query);
        if (Objects.isNull(ordDirReturn) || CollectionUtils.isEmpty(detail)) {
            throw new BusinessException("此直营退货单不存在" + unificationReBillVO.getFsrcnum());
        }
        if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(ordDirReturn.getReturnStatus())) {
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_DIR_RETURN_PROCESSED_SYSTEM.getCode(),
                    String.valueOf(ordDirReturn.getId()),
                    OrdLogTypeEnum.ORD_DIR_RETURN.getCode(),
                    OrdLogTypeEnum.ORD_DIR_RETURN_PROCESSED_SYSTEM.getName(), new Date(), ordDirReturn.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            log.error("该退货单{}后台已收货", unificationReBillVO.getFsrcnum());
//            return "该退货单后台已收货";
            return true;
        }
        if (OrdReturnOrderStatusEnum.INVALID.getKey().equals(ordDirReturn.getReturnStatus())) {
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_DIR_RETURN_INVALID_SYSTEM.getCode(),
                    String.valueOf(ordDirReturn.getId()),
                    OrdLogTypeEnum.ORD_DIR_RETURN.getCode(),
                    OrdLogTypeEnum.ORD_DIR_RETURN_INVALID_SYSTEM.getName(), new Date(), ordDirReturn.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//            return "该退货单后台已作废";
            log.error("该退货单{}后台已作废", unificationReBillVO.getFsrcnum());
            return true;
        }
        ordDirReturn.setReturnStatus(OrdReturnOrderStatusEnum.PROCESSED.getKey());
        ordDirReturn.setReceiveTime(LocalDateTime.now());
        ordDirReturn.setLogisticsNo(unificationReBillVO.getNum());
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDirReturn.getStockCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("找不到仓位");
        }
        //初始化退货单
        List<OrdDirReturnDetail> ordDisReturnDetailList = this.initReturnOrder(ordDirReturn, detail, stockInfoOut.getBizOrgCode());
        //修改退货单
        this.updateByPrimaryKeySelective(ordDirReturn);
        //修改退货单明细
        ordDirReturnDetailService.batchUpdate(ordDisReturnDetailList);

        // 释放库存
        this.adjustInv(ordDirReturn, ordDisReturnDetailList, stockInfoOut, ordDirReturn.getReceiveTime());
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_RETURN.getName(),
                String.valueOf(ordDirReturn.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN.getCode(),
                OrdLogTypeEnum.ORD_DIR_RETURN_PROCESSED.getName(), new Date(), SystemConstant.SYSTEM_USER);
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
        String fileName = "配货退货单明细".concat(DateUtil.format(now, DatePattern.PURE_DATETIME_MS_PATTERN)).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配货退货单明细",
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
    public OrdDirReturn getReturnOrderByNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return null;
        }
        OrdDirReturn ordDirReturn = new OrdDirReturn();
        ordDirReturn.setReturnOrderNo(orderNo);
        return ordDirReturnMapper.selectOne(ordDirReturn);
    }

    @Override
    public Response<String> batchInvalidatedOrdReturn(List<Integer> idList) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        idList.forEach(id -> {
            OrdDirReturn ordDirReturn = this.getReturnOrderById(id);
            if (Objects.isNull(ordDirReturn)) {
                log.error("退货单ID{}不存在", id);
                return;
            }
            try {
                stockServer.getAndCheckStockInfo(ordDirReturn.getStockCode(), UserUtil.getBizOrgCode(), "作废退货单");
                Response response = this.invalidatedOrdReturn(ordDirReturn);
                if (!response.isSuccess()) {
                    errorJoiner.add("退货单:" + ordDirReturn.getReturnOrderNo() + response.getMessage());
                }
            } catch (Exception e) {
                log.error("退货单{}作废异常", ordDirReturn.getReturnOrderNo(), e);
                errorJoiner.add("退货单:" + ordDirReturn.getReturnOrderNo() + "作废失败");
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.error(errorJoiner.toString());
        }
        return Response.success();
    }

    @Override
    public Response<String> batchReceiving(List<Integer> idList) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        idList.forEach(id -> {
            OrdDirReturn ordDirReturn = this.getReturnOrderById(id);
            if (Objects.isNull(ordDirReturn)) {
                log.error("退货单ID{}不存在", id);
                return;
            }
            try {
                StockInfoOut stockInfoOut = stockInfoOutMap.get(ordDirReturn.getStockCode());
                if (Objects.isNull(stockInfoOut)) {
                    stockInfoOut = stockServer.getAndCheckStockInfo(ordDirReturn.getStockCode(), UserUtil.getBizOrgCode(), "退货单收货");
                    if (Objects.isNull(stockInfoOut)) {
                        return;
                    }
                }
                List<OrdDirReturnDetail> returnGoodsInfoInList = ordDirReturnDetailService.findByReturnOrderId(id);
                if (CollectionUtils.isNotEmpty(returnGoodsInfoInList)) {
                    returnGoodsInfoInList.forEach(ordDirReturnDetail -> {
                        ordDirReturnDetail.setActualReturnQuantity(ordDirReturnDetail.getAuditReturnQuantity());
                        ordDirReturnDetail.setActualPackageQuantity(ordDirReturnDetail.getAuditPackageQuantity());
                        ordDirReturnDetail.setActualReturnAmount(ordDirReturnDetail.getAuditReturnAmount());
                    });
                }
                Response response = this.receiving(returnGoodsInfoInList, ordDirReturn, stockInfoOut);
                if (!response.isSuccess()) {
                    errorJoiner.add("退货单:" + ordDirReturn.getReturnOrderNo() + response.getMessage());
                }
            } catch (Exception e) {
                log.error("退货单{}收货异常", ordDirReturn.getReturnOrderNo(), e);
                errorJoiner.add("退货单:" + ordDirReturn.getReturnOrderNo() + "收货失败");
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.error(errorJoiner.toString());
        }
        return Response.success();
    }

    @Override
    public List<DirReturnOrderPrintOut> findPrintDataByIds(List<Long> ids, String bizOrgCode) {
        // 仓位
        Map<String, StockInfoOut> stockMap = stockServer.findAll(bizOrgCode);
        List<DirReturnOrderPrintOut> printOutList = new ArrayList<>();
        for (Long id : ids) {
            DirReturnOrderPrintOut orderPrintOut = new DirReturnOrderPrintOut();
            OrdDirReturn ordDirReturn = selectByPrimaryKey(id);
            if (ordDirReturn == null) {
                throw new BusinessException("无效的退货单id！");
            }
            BeanUtils.copy(ordDirReturn, orderPrintOut);
            StockInfoOut stockInfoOut = stockMap.get(orderPrintOut.getStockCode());
            if (stockInfoOut != null) {
                orderPrintOut.setStockName(stockInfoOut.getStockName());
            }
            orderPrintOut.setReturnStatusName(systemDictService.getSystemDictName(orderPrintOut.getReturnStatus()));
            List<DirReturnOrderDtlPrintOut> dtlPrintOutList = ordDirReturnDetailService.findPrintDtlByReturnId(id);
            // 实配包装数合计
            BigDecimal deliveryPackQuantity = dtlPrintOutList.stream().map(DirReturnOrderDtlPrintOut::getApplyPackageQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            orderPrintOut.setApplyReturnPackQuantity(deliveryPackQuantity);
            orderPrintOut.setDtlPrintOuts(dtlPrintOutList);

            printOutList.add(orderPrintOut);
        }
        return printOutList;
    }


    @Override
    public Response<String> asyncImportReturn(String fileId, String loginUsername, String loginBizOrgCode) {
        String key = DirSystemConstant.CHECK_DIR_RETURN_IMPORT_ONLY_ONE + loginBizOrgCode;
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
        dirReturnImportHandle.handleReturnListAsyncImport(listener.getImportOrdReturnOrderList(), loginUsername, loginBizOrgCode, key);
        return Response.success("开始导入，请耐心等待，切勿重复操作！");
    }


    /**
     * 非第一次
     *
     * @param ordSaveReturnOrderIn
     * @param storeOut
     */
    private List<AppSaveOrdDirReturnDetailIn> initReturnOrderDetailList(AppDirReturnOrderSaveIn ordSaveReturnOrderIn, StoreOut storeOut, OrdDirReturn ordDirReturn) {
        List<AppSaveOrdDirReturnDetailIn> ordDirReturnDetailList = new ArrayList<>();
        OrdDirReturnNotice ordDirReturnNotice = null;
        if (null != ordSaveReturnOrderIn.getReturnNoticeOrderId()) {
            ordDirReturnNotice = ordDirReturnNoticeService.getReturnNoticeOrderById(ordSaveReturnOrderIn.getReturnNoticeOrderId());
        }
        int i = 1;
        for (AppSaveOrdDirReturnDetailIn returnGoodsInfoIn : ordSaveReturnOrderIn.getReturnGoodsInfoInList()) {
            if (Objects.isNull(returnGoodsInfoIn.getApplyReturnQuantity()) || BigDecimal.ZERO.compareTo(returnGoodsInfoIn.getApplyReturnQuantity()) == NumberUtil.INTEGER_ZERO) {
                continue;
            }
            this.checkApplyReturnQuantity(ordDirReturnNotice, returnGoodsInfoIn.getGoodsCode(), storeOut.getStoreCode(), returnGoodsInfoIn.getApplyReturnQuantity());
            OrderGoodsOut orderGoodsOut = this.getFastReturnGoodsInfo(ordSaveReturnOrderIn.getBizOrgCode(), storeOut.getStoreCode(), returnGoodsInfoIn.getGoodsCode());
            AppSaveOrdDirReturnDetailIn appSaveOrdDirReturnDetailIn = ordDirReturnDetailService.initReturnOrderDetail(storeOut, returnGoodsInfoIn, orderGoodsOut, ordDirReturn);
            appSaveOrdDirReturnDetailIn.setReturnOrderId(ordSaveReturnOrderIn.getReturnOrderId());
            if (Objects.isNull(appSaveOrdDirReturnDetailIn.getLine())) {
                appSaveOrdDirReturnDetailIn.setLine(i++);
            }
            ordDirReturnDetailList.add(appSaveOrdDirReturnDetailIn);
        }
        OrdDirReturnImage delOrdDirReturnImage = new OrdDirReturnImage();
        delOrdDirReturnImage.setReturnOrderId(ordDirReturn.getId());
        ordDirReturnImageMapper.delete(delOrdDirReturnImage);
        this.saveOrUpdateReturnOrderDetailList(ordDirReturnDetailList, ordDirReturn);
        return ordDirReturnDetailList;
    }

    private void updateReturnOrderDetail(AppSaveOrdDirReturnDetailIn returnGoodsInfoIn, AppSaveOrdDirReturnDetailIn ordDirReturnDetail, OrdDirReturn ordDirReturn) {
        ordDirReturnDetail.setApplyPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(ordDirReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
        ordDirReturnDetail.setApplyReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
        ordDirReturnDetail.setAuditPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(ordDirReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
        ordDirReturnDetail.setAuditReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
//        ordDirReturnDetail.setActualPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(ordDirReturnDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
//        ordDirReturnDetail.setActualReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
        if (null != ordDirReturnDetail.getReturnUnitPrice()) {
            ordDirReturnDetail.setApplyReturnAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            ordDirReturnDetail.setAuditReturnAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//            ordDirReturnDetail.setActualReturnAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        }
        ordDirReturnDetail.setReturnReason(returnGoodsInfoIn.getReturnReason());
        BigDecimal sellTax = ordDirReturnDetail.getSellTax() == null ? BigDecimal.ZERO : ordDirReturnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
        //1+税率
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        BigDecimal stockPrice = warehouseServer.getStockPrice(ordDirReturn.getStoreCode(), returnGoodsInfoIn.getGoodsCode(), ordDirReturn.getBizOrgCode());
        //最新门店库存价
        ordDirReturnDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
//        BigDecimal warehousePrice = warehouseServer.getWarehousePrice(ordDirReturn.getWrhCode(), ordDirReturn.getStockCode(), returnGoodsInfoIn.getGoodsCode(), ordDirReturn.getBizOrgCode(), ordDirReturnDetail.getVendorCode());
//        //最新仓储库存价
//        ordDirReturnDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);

        //退货去税金额
        ordDirReturnDetail.setReturnExceptTaxAmount(ordDirReturnDetail.getAuditReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //退货税额
        ordDirReturnDetail.setReturnTaxAmount(ordDirReturnDetail.getAuditReturnAmount().subtract(ordDirReturnDetail.getReturnExceptTaxAmount()));
        //仓储成本金额
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDirReturn.getStockCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("找不到仓位");
        }
        BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(ordDirReturn.getWrhCode(), ordDirReturn.getStockCode(),
                returnGoodsInfoIn.getGoodsCode(), stockInfoOut.getBizOrgCode(), ordDirReturn.getStoreCode(), ordDirReturn.getBizOrgCode());
        if (Objects.isNull(warehousePrice)) {
            log.error("统配退货单{}商品{}仓储库存价为空", ordDirReturn.getReturnOrderNo(), ordDirReturnDetail.getGoodsCode());
            throw new BusinessException("统配退货单" + ordDirReturn.getReturnOrderNo() + "商品" + ordDirReturnDetail.getGoodsCode() + "仓储库存价为空");
        }
        ordDirReturnDetail.setWrhPrice(warehousePrice);
        ordDirReturnDetail.setWrhCostAmount(warehousePrice.multiply(ordDirReturnDetail.getApplyReturnQuantity()));
        //仓储成本去税金额
        ordDirReturnDetail.setWrhExceptTaxAmount(ordDirReturnDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //仓储成本税额
        ordDirReturnDetail.setWrhTaxAmount(ordDirReturnDetail.getWrhCostAmount().subtract(ordDirReturnDetail.getWrhExceptTaxAmount()));
        //门店成本金额
        ordDirReturnDetail.setStoreCostAmount(ordDirReturnDetail.getStoreStockPrice().multiply(ordDirReturnDetail.getAuditReturnQuantity()));
        //门店成本去税金额
        ordDirReturnDetail.setStoreExceptTaxAmount(ordDirReturnDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //门店成本税额
        ordDirReturnDetail.setStoreTaxAmount(ordDirReturnDetail.getStoreCostAmount().subtract(ordDirReturnDetail.getStoreExceptTaxAmount()));
        ordDirReturnDetail.setExpiry(ordDirReturnDetail.getExpiry());

    }

    private void saveOrUpdateReturnOrderDetailList(List<AppSaveOrdDirReturnDetailIn> ordDirReturnDetailList, OrdDirReturn ordDirReturn) {
        ordDirReturnDetailList.forEach(ordDisReturnDetail -> {
            ordDirReturnDetailService.saveOrUpdateReturnOrderDetail(ordDisReturnDetail);
            this.handleReturnImageUpload(ordDirReturn, ordDisReturnDetail);
        });
    }

    private void handleReturnImageUpload(OrdDirReturn ordDirReturn, AppSaveOrdDirReturnDetailIn appSaveOrdDirReturnDetailIn) {
        appSaveOrdDirReturnDetailIn.getImageUrlList().forEach(imageUrl -> {
            OrdDirReturnImage ordDirReturnImage = new OrdDirReturnImage();
            ordDirReturnImage.setReturnOrderId(ordDirReturn.getId());
            ordDirReturnImage.setReturnDetailId(appSaveOrdDirReturnDetailIn.getId());
            ordDirReturnImage.setImage_url(imageUrl);
            ordDirReturnImage.setBizOrgCode(ordDirReturn.getBizOrgCode());
            ordDirReturnImage.setCreator(ordDirReturn.getCreator());
            ordDirReturnImage.setCreateTime(LocalDateTime.now());
            ordDirReturnImageMapper.insert(ordDirReturnImage);
        });
    }

    /**
     * 第一次
     *
     * @param appDirReturnOrderSaveIn
     * @param storeOut
     */
    private void saveReturnOrderData(AppDirReturnOrderSaveIn appDirReturnOrderSaveIn, StoreOut storeOut) {
        OrdDirReturnNotice ordDisReturnNotice = null;
        if (null != appDirReturnOrderSaveIn.getReturnNoticeOrderId()) {
            synchronized (appDirReturnOrderSaveIn.getStoreCode() + "_" + appDirReturnOrderSaveIn.getReturnNoticeOrderId()) {
                if (checkIsReturn(appDirReturnOrderSaveIn.getStoreCode(), appDirReturnOrderSaveIn.getReturnNoticeOrderId())) {
                    throw new BusinessException("此退货通知单已申请退货，请勿重复提交！");
                }
            }
            ordDisReturnNotice = ordDirReturnNoticeService.getReturnNoticeOrderById(appDirReturnOrderSaveIn.getReturnNoticeOrderId());
        }
        Map<String, OrdDirReturn> returnOrderMap = new LinkedHashMap<>();
        Map<String, List<AppSaveOrdDirReturnDetailIn>> returnOrderDetailMap = new LinkedHashMap<>();
        for (AppSaveOrdDirReturnDetailIn returnGoodsInfoIn : appDirReturnOrderSaveIn.getReturnGoodsInfoInList()) {
            if (Objects.isNull(returnGoodsInfoIn.getApplyReturnQuantity()) || BigDecimal.ZERO.compareTo(returnGoodsInfoIn.getApplyReturnQuantity()) == NumberUtil.INTEGER_ZERO) {
                continue;
            }
            this.checkApplyReturnQuantity(ordDisReturnNotice, returnGoodsInfoIn.getGoodsCode(), storeOut.getStoreCode(), returnGoodsInfoIn.getApplyReturnQuantity());
            OrderGoodsOut orderGoodsOut = this.getFastReturnGoodsInfo(appDirReturnOrderSaveIn.getBizOrgCode(), storeOut.getStoreCode(), returnGoodsInfoIn.getGoodsCode());
            String stockCode = orderGoodsOut.getBackStockCode();
            String key = stockCode + SystemConstant.SHORT_LINE + orderGoodsOut.getDistributionWay();
            OrdDirReturn ordDirReturn = returnOrderMap.get(key);
            if (null == ordDirReturn) {
                ordDirReturn = this.inReturnOrder(appDirReturnOrderSaveIn, storeOut, OrdReturnOrderStatusEnum.SAVED.getKey(), orderGoodsOut.getBackStockCode(), orderGoodsOut.getDistributionWay());
                returnOrderMap.put(key, ordDirReturn);
            }
            ordDirReturnDetailService.initReturnOrderDetailMap(returnOrderDetailMap, storeOut, returnGoodsInfoIn, orderGoodsOut, ordDirReturn);
        }
        this.saveReturnOrderByMap(returnOrderMap, returnOrderDetailMap);
    }

    private void saveReturnOrderByMap(Map<String, OrdDirReturn> returnOrderMap, Map<String, List<AppSaveOrdDirReturnDetailIn>> returnOrderDetailMap) {
        for (Map.Entry<String, OrdDirReturn> returnOrderEntry : returnOrderMap.entrySet()) {
            String key = returnOrderEntry.getKey();
            OrdDirReturn ordDirReturn = returnOrderEntry.getValue();
            List<AppSaveOrdDirReturnDetailIn> returnOrderDetailList = returnOrderDetailMap.get(key);
            ordDirReturn.setSkuCount(returnOrderDetailList.size());
            ordDirReturn.setApplyReturnQuantity(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getApplyReturnQuantity())).map(OrdDirReturnDetail::getApplyReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            ordDirReturn.setApplyReturnAmount(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getApplyReturnAmount())).map(OrdDirReturnDetail::getApplyReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            ordDirReturn.setAuditReturnQuantity(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getAuditReturnQuantity())).map(OrdDirReturnDetail::getAuditReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            ordDirReturn.setAuditReturnAmount(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getAuditReturnAmount())).map(OrdDirReturnDetail::getAuditReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
//            ordDirReturn.setActualReturnQuantity(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getActualReturnQuantity())).map(OrdDirReturnDetail::getActualReturnQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
//            ordDirReturn.setActualReturnAmount(returnOrderDetailList.stream().filter(detail -> Objects.nonNull(detail.getActualReturnAmount())).map(OrdDirReturnDetail::getActualReturnAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(NumberUtil.INTEGER_TWO,RoundingMode.HALF_UP));
//            if (null == ordDirReturn.getId()) {
//            }
            this.insert(ordDirReturn);
            int i = 1;
            for (AppSaveOrdDirReturnDetailIn appSaveOrdDirReturnDetailIn : returnOrderDetailList) {
                appSaveOrdDirReturnDetailIn.setReturnOrderId(ordDirReturn.getId());
                appSaveOrdDirReturnDetailIn.setLine(i++);
                ordDirReturnDetailService.saveReturnOrderDetail(appSaveOrdDirReturnDetailIn);
                this.handleReturnImageUpload(ordDirReturn, appSaveOrdDirReturnDetailIn);
//                if (null == returnOrderDetail.getId()) {
//                    i++;
//                }
            }
            String content = DirOrdReturnOrderLogEnum.ORD_DIR_APP_SAVE.getValue();
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN.getName(), String.valueOf(ordDirReturn.getId()),
                    OrdLogTypeEnum.ORD_DIR_RETURN.getCode(), content, new Date(), ordDirReturn.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
    }

    private OrdDirReturn inReturnOrder(AppDirReturnOrderSaveIn appDirReturnOrderSaveIn, StoreOut storeOut, String returnStatus, String backStockCode, String distributionType) {
        OrdDirReturn ordDirReturn = new OrdDirReturn();
        String returnOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PT.getCode(), appDirReturnOrderSaveIn.getBizOrgCode(), uniqueUtils, 4);
        ordDirReturn.setReturnOrderNo(returnOrderNo);
        ordDirReturn.setStoreCode(storeOut.getStoreCode());
        ordDirReturn.setDistributionType(distributionType);
        ordDirReturn.setStoreName(storeOut.getStoreName());
        ordDirReturn.setStockCode(backStockCode);
        ordDirReturn.setStoreArea(storeOut.getBelongArea());
        StockInfoOut stockInfoOut = stockServer.getTransInfo(backStockCode);
        ordDirReturn.setWrhCode(Objects.isNull(stockInfoOut) ? "" : stockInfoOut.getWarehouseCode());
        if (null == appDirReturnOrderSaveIn.getReturnNoticeOrderId()) {
            ordDirReturn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
        } else {
            OrdDirReturnNotice ordDirReturnNotice = ordDirReturnNoticeService.getReturnNoticeOrderById(appDirReturnOrderSaveIn.getReturnNoticeOrderId());
            ordDirReturn.setReturnType(ordDirReturnNotice.getReturnType());
        }
        ordDirReturn.setReturnStatus(returnStatus);
        ordDirReturn.setReturnOrderReason(appDirReturnOrderSaveIn.getReturnOrderReason());
        ordDirReturn.setRemark(appDirReturnOrderSaveIn.getRemark());
        ordDirReturn.setReturnNoticeOrderId(appDirReturnOrderSaveIn.getReturnNoticeOrderId());
        ordDirReturn.setBizOrgCode(appDirReturnOrderSaveIn.getBizOrgCode());
        ordDirReturn.setOrgCode(UserUtil.getOrgCode());
        ordDirReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        ordDirReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
        ordDirReturn.setCreator(storeOut.getStoreCode());
        ordDirReturn.setCreateTime(LocalDateTime.now());
        ordDirReturn.setUpdater(storeOut.getStoreCode());
        ordDirReturn.setUpdateTime(LocalDateTime.now());
        ordDirReturn.setIsDelete(0);
        ordDirReturn.setAppRemark(appDirReturnOrderSaveIn.getAppRemark());
        ordDirReturn.setDeliveryOrderNo(appDirReturnOrderSaveIn.getDeliveryOrderNo());
        return ordDirReturn;
    }

    private OrderGoodsOut getFastReturnGoodsInfo(String bizOrgCode, String storeCode, String goodsCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(goodsCode);
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS_FAST.getType());
        OrderGoodsOut orderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(orderGoods)) {
            throw new BusinessException("此商品不允许配货退货");
        }
        return orderGoods;
    }

    private void checkApplyReturnQuantity(OrdDirReturnNotice ordDirReturnNotice, String goodsCode, String storeCode, BigDecimal applyReturnQuantity) {
        // 如果是限量退货
        if (null != ordDirReturnNotice && OrdReturnOrderTypeEnum.LIMITED_RETURN.getKey().equals(ordDirReturnNotice.getReturnType())) {
            // 限量最大值 3
            BigDecimal maxQty = ordDirReturnNoticeService.getMaxQtyByParameter(goodsCode, storeCode, ordDirReturnNotice.getId());
            if (applyReturnQuantity.compareTo(maxQty) == 1) {
                throw new BusinessException("商品" + goodsCode + "最大申请数" + maxQty.toBigInteger());
            }
        }
    }

    private boolean checkIsReturn(String storeCode, Integer returnNoticeOrderId) {
        OrdDirReturn query = new OrdDirReturn();
        query.setStoreCode(storeCode);
        query.setReturnNoticeOrderId(returnNoticeOrderId);
        return ordDirReturnMapper.selectCount(query) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer insertDirReturnOrder(OrdSaveReturnOrderIn saveReturnOrderIn, String channelBizOrgCode) {
        //校验数据
        String s = this.checkData(saveReturnOrderIn, channelBizOrgCode);
        if (StringUtils.isNotEmpty(s)) {
            throw new BusinessException(s);
        }
        //保存退货单
        OrdDirReturn ordDirReturn = new OrdDirReturn();
        BeanUtil.copyProperties(saveReturnOrderIn, ordDirReturn, CopyOptions.create().setIgnoreNullValue(true));
        String no = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PT.getCode(), channelBizOrgCode, uniqueUtils, 4);
        ordDirReturn.setReturnOrderNo(no);
        ordDirReturn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
        ordDirReturn.setBizOrgCode(channelBizOrgCode);
        ordDirReturn.setOrgCode(UserUtil.getOrgCode());
        ordDirReturn.setSubmitTime(LocalDateTime.now());
        ordDirReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        ordDirReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
        ordDirReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
        ordDirReturn.setIsDelete(NumberUtil.INTEGER_ZERO);
        ordDirReturn.setWrhCode(saveReturnOrderIn.getWarehouseCode());
        ordDirReturn.setRemark(saveReturnOrderIn.getRemark());
        ordDirReturn.setSkuCount(saveReturnOrderIn.getReturnGoodsInfoInList().size());
//        ordDirReturn.setDistributionType(DistributionWaysEnum.getNameByType(saveReturnOrderIn.getDistributionType()));
        ordDirReturn.setDistributionType(saveReturnOrderIn.getDistributionType());
        int count = ordDirReturnMapper.insertSelective(ordDirReturn);
        //保存明细及
        if (count > 0) {
            ordDirReturnDetailService.handleReturnDetail(ordDirReturn, saveReturnOrderIn);
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN.getName(), String.valueOf(ordDirReturn.getId()),
                OrdLogTypeEnum.ORD_DIR_RETURN.getCode(), OrdLogTypeEnum.ORD_DIR_RETURN_SAVE.getName(), new Date(), ordDirReturn.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return ordDirReturn.getId();
    }

    /**
     * 数据校验
     *
     * @param saveReturnOrderIn
     * @return
     */
    private String checkData(OrdSaveReturnOrderIn saveReturnOrderIn, String channelBizOrgCode) {
        List<OrdDirReturnDetail> returnGoodsInfoInList = saveReturnOrderIn.getReturnGoodsInfoInList();
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
                sj.add(returnGoodsInfoIn.getGoodsCode() + "不允许直营配货退货");
            }
            if (Objects.nonNull(orderGoodsOut) && Objects.nonNull(orderGoodsOut.getDistributionUnitPrice())) {
//                returnGoodsInfoIn.setReturnUnitPrice(orderGoodsOut.getDistributionUnitPrice());
                returnGoodsInfoIn.setDistributionPrice(orderGoodsOut.getDistributionUnitPrice());
                returnGoodsInfoIn.setInvoiceType(orderGoodsOut.getInvoiceType());
            }
        });
        return sj.toString();
    }

    private void checkNum(StringJoiner sj, OrdDirReturnDetail returnGoodsInfoIn) {
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
      /*  if (!isIntegerValue(returnGoodsInfoIn.getApplyPackageQuantity())) {
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

    @Transactional(rollbackFor = Exception.class)
    public Integer updateDirReturnOrder(OrdSaveReturnOrderIn saveReturnOrderIn) {
        OrdDirReturn ordDirReturn = ordDirReturnMapper.selectByPrimaryKey(saveReturnOrderIn.getReturnOrderId());
        if (!OrdReturnOrderStatusEnum.SUBMITTED.getKey().equals(ordDirReturn.getReturnStatus())) {
            throw new BusinessException("只有未审核状态才能修改");
        }
        //校验数据
        String s = this.checkData(saveReturnOrderIn, ordDirReturn.getBizOrgCode());
        if (StringUtils.isNotEmpty(s)) {
            throw new BusinessException(s);
        }
//        BeanUtil.copyProperties(saveReturnOrderIn, ordDirReturn, CopyOptions.create().setIgnoreNullValue(true));
//        ordDirReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
//        ordDirReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
//        ordDirReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
//        ordDirReturn.setUpdater(UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】"));
//        ordDirReturn.setUpdateTime(LocalDateTime.now());
//        ordDirReturn.setWrhCode(saveReturnOrderIn.getWarehouseCode());
//        int count = ordDirReturnMapper.updateByPrimaryKeySelective(ordDirReturn);
        //保存明细
//        if (count > 0) {
        ordDirReturnDetailService.deleteByReturnOrderId(ordDirReturn);
        ordDirReturnDetailService.handleReturnDetail(ordDirReturn, saveReturnOrderIn);
//        }
        return ordDirReturn.getId();
    }

    /**
     * app提交退货单
     *
     * @param saveReturnOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> submitReturnOrderForApp(OrdSaveReturnOrderIn saveReturnOrderIn) {
        OrdDirReturn ordDirReturn = this.getReturnOrderById(saveReturnOrderIn.getReturnOrderId());
        if (Objects.isNull(ordDirReturn)) {
            return Response.error("不存在的退货单");
        }
        String key = DirSystemConstant.CHECK_DIR_RETURN_ORDER_SUBMIT_APP + ordDirReturn.getBizOrgCode() +
                SystemConstant.COLON + ordDirReturn.getStoreCode() + SystemConstant.WAIT + ordDirReturn.getReturnOrderNo();
        if (!redisService.setIfAbsent(key, ordDirReturn.getReturnOrderNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("短时间内重复提交" + ordDirReturn.getReturnOrderNo());
        }
        if (Objects.nonNull(saveReturnOrderIn.getReturnNoticeOrderId())) {
            boolean overtimeForSubmitFlag = ordDirReturnNoticeService.isOvertimeForSubmit(saveReturnOrderIn.getReturnNoticeOrderId());
            if (overtimeForSubmitFlag) {
                return Response.error("该退货通知单退货截止时间已超时，不能提交退货");
            }
        }
        ordDirReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
        ordDirReturn.setSubmitTime(LocalDateTime.now());
        ordDirReturnMapper.updateByPrimaryKeySelective(ordDirReturn);
        String content = DirOrdReturnOrderLogEnum.ORD_DIR_APP_SUBMIT.getValue();
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDirReturn.getId()),
                OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), content, new Date(), UserUtil.getUserName());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        if (Objects.nonNull(saveReturnOrderIn.getReturnNoticeOrderId())) {
            // 自动审核
            List<OrdDirReturnDetail> returnGoodsInfoInList = ordDirReturnDetailService.findByReturnOrderId(ordDirReturn.getId());
            for (OrdDirReturnDetail ordDirReturnDetail : returnGoodsInfoInList) {
                ordDirReturnDetail.setAuditReturnAmount(ordDirReturnDetail.getApplyReturnAmount());
                ordDirReturnDetail.setAuditReturnQuantity(ordDirReturnDetail.getApplyReturnQuantity());
                ordDirReturnDetail.setAuditPackageQuantity(ordDirReturnDetail.getApplyPackageQuantity());
                ordDirReturnDetailService.updateByPrimaryKey(ordDirReturnDetail);
            }
            StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDirReturn.getStockCode());
            return this.audit(ordDirReturn, returnGoodsInfoInList, stockInfoOut.getBizOrgCode());
        } else {
            return Response.success("提交成功");
        }
    }

    /**
     * 删除退货单
     *
     * @param returnOrderId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByReturnOrderById(Integer returnOrderId) {
        OrdDirReturn ordDirReturn = new OrdDirReturn();
        ordDirReturn.setId(returnOrderId);
        ordDirReturnDetailService.deleteByReturnOrderId(ordDirReturn);
        ordDirReturnMapper.deleteByPrimaryKey(returnOrderId);
        OrdDirReturnImage delOrdDirReturnImage = new OrdDirReturnImage();
        delOrdDirReturnImage.setReturnOrderId(returnOrderId);
        ordDirReturnImageMapper.delete(delOrdDirReturnImage);
    }

    /**
     * 为异步导出查询配货退货单明细方法
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
        List<AsyncExcelReturnOrderDetail> asyncExcelDeliveryOrderDetails = ordDirReturnMapper.findListForAsyncExportByPage(ordReturnOrderPageIn);
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
