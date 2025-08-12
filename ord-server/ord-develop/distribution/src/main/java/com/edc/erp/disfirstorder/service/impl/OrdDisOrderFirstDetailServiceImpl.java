package com.edc.erp.disfirstorder.service.impl;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.entity.GoodsStatusBusinessSwitch;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.goods.OrdQueryGoodsIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.store.StoreStatusInfo;
import com.edc.erp.common.model.out.goods.GoodsForOrdOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.StandardSpecTransInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDetail;
import com.edc.erp.disfirstorder.enumeration.FirstOrderFreezeEnum;
import com.edc.erp.disfirstorder.listener.FirstOrderListener;
import com.edc.erp.disfirstorder.mapper.OrdDisOrderFirstDetailMapper;
import com.edc.erp.disfirstorder.model.excel.ExportFirstOrderDetail;
import com.edc.erp.disfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.erp.disfirstorder.model.excel.OrdDisFirstOrderImportErrorResult;
import com.edc.erp.disfirstorder.model.in.OrdDisDeleteFirstOrderDetailIn;
import com.edc.erp.disfirstorder.model.in.InsertFirstOrderDetailIn;
import com.edc.erp.disfirstorder.model.in.OrdDisOrderFirstDetailIn;
import com.edc.erp.disfirstorder.model.in.SaveDisFirstDetailIn;
import com.edc.erp.disfirstorder.model.out.DisFirstOrderCheckOut;
import com.edc.erp.disfirstorder.model.out.OrdDisOrderFirstDetailOut;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDetailService;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstService;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 配销铺货单明细表(OrdDisOrderFirstDetail)表服务实现类
 *
 * @author weichao
 * @since 2022-10-10 16:18:10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDisOrderFirstDetailServiceImpl extends BaseServiceImpl<OrdDisOrderFirstDetail> implements OrdDisOrderFirstDetailService {

    private final OrdDisOrderFirstDetailMapper ordDisOrderFirstDetailMapper;
    private final OrdDisOrderFirstService ordDisOrderFirstService;
    private final OrderGoodsServer orderGoodsServer;
    private final StoreCenterService storeCenterService;
    private static final Integer MAX_NUMBER = 500;
    private final FileService fileService;

    private final AsyncPushTaskService asyncPushTaskService;

    private final AsyncLogService asyncLogService;

    private final StoreChannelHandle storeChannelHandle;

    @Qualifier("disFirstToDeliverySender")
    private final MessageSender disFirstToDeliverySender;

    @Override
    public Page<OrdDisOrderFirstDetailOut> findOrdDisOrderFirstDetailPage(OrdDisOrderFirstDetailIn ordDisOrderFirstDetailIn, String bizOrgCode) {
        List<OrdDisOrderFirstDetailOut> ordDisOrderFirstDetailOuts = ordDisOrderFirstDetailMapper.findOrdDisOrderFirstDetailByPage(ordDisOrderFirstDetailIn);
        Long firstOrderId = ordDisOrderFirstDetailIn.getFirstOrderId();
        OrdDisOrderFirst query = new OrdDisOrderFirst();
        query.setId(firstOrderId);
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        OrdDisOrderFirst ordDisOrderFirst = ordDisOrderFirstService.selectOne(query);
        Map<String, GoodsForOrdOut> goodsOutMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(ordDisOrderFirstDetailOuts)) {
            List<String> goodsCodeList = ordDisOrderFirstDetailOuts.stream().map(OrdDisOrderFirstDetail::getGoodsCode).collect(Collectors.toList());
            // 获取商品信息
            OrdQueryGoodsIn ordQueryGoodsIn = new OrdQueryGoodsIn();
            ordQueryGoodsIn.setStoreCode(ordDisOrderFirst.getStoreCode());
            ordQueryGoodsIn.setBizOrgCode(ordDisOrderFirst.getBizOrgCode());
            ordQueryGoodsIn.setGoodsCodes(goodsCodeList);
            List<GoodsForOrdOut> goodsCodesList = orderGoodsServer.findGoodsInfos(ordQueryGoodsIn);
            if (CollectionUtils.isNotEmpty(goodsCodesList)) {
                goodsOutMap = goodsCodesList.stream().collect(Collectors.toMap(GoodsForOrdOut::getGoodsCode, Function.identity()));
            }
        }
        for (OrdDisOrderFirstDetailOut ordDisOrderFirstDetailOut : ordDisOrderFirstDetailOuts) {
            GoodsForOrdOut goodsForOrdOut = goodsOutMap.get(ordDisOrderFirstDetailOut.getGoodsCode());
            if (Objects.isNull(goodsForOrdOut)) {
                continue;
            }
            ordDisOrderFirstDetailOut.setStockCode(goodsForOrdOut.getStockCode());
            ordDisOrderFirstDetailOut.setStockName(goodsForOrdOut.getStockName());
            ordDisOrderFirstDetailOut.setInvoiceType(goodsForOrdOut.getInvoiceType());
            if (goodsForOrdOut.getDistributionSpecification() != null) {
                ordDisOrderFirstDetailOut.setQpcStr(goodsForOrdOut.getDistributionSpecification().getQpcStr());
                BigDecimal specificationNum = BigDecimal.valueOf(goodsForOrdOut.getDistributionSpecification().getQpc());
                // 铺货包装数 （铺货数量/配货规格）
                BigDecimal distributionPackageNum = new BigDecimal(ordDisOrderFirstDetailOut.getNum()).divide(specificationNum, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
                ordDisOrderFirstDetailOut.setDistributionPackageNum(distributionPackageNum);
                ordDisOrderFirstDetailOut.setDistributionSpecificationNum(specificationNum);
                ordDisOrderFirstDetailOut.setDistributionSpecificationUnit(goodsForOrdOut.getDistributionSpecification().getUnitName());
            }
            ordDisOrderFirstDetailOut.setSort(goodsForOrdOut.getSort());
            ordDisOrderFirstDetailOut.setSortName(goodsForOrdOut.getSortName());
            ordDisOrderFirstDetailOut.setDistributionPrice(goodsForOrdOut.getDistributionUnitPrice());
            ordDisOrderFirstDetailOut.setBarCode(goodsForOrdOut.getBarCode());
            ordDisOrderFirstDetailOut.setDistributionType(DistributionWaysEnum.getNameByType(goodsForOrdOut.getDistributionWay()));
        }
        Page<OrdDisOrderFirstDetailOut> outPage = new Page<>(ordDisOrderFirstDetailIn);
        outPage.setList(ordDisOrderFirstDetailOuts);
        return outPage;
    }

    @Override
    public Response checkRepeatParam(InsertFirstOrderDetailIn insertFirstOrderDetailIn, String bizOrgCode) {
        List<OrdDisOrderFirstDetail> firstOrderDetails = insertFirstOrderDetailIn.getFirstOrderDetails();
        // 查找重复的goodsCode
        Map<String, Long> collect = firstOrderDetails.stream().collect(Collectors.groupingBy(OrdDisOrderFirstDetail::getGoodsCode, Collectors.counting()));
        List<String> repeatList = collect.keySet().stream().filter(key -> collect.get(key) > NumberUtil.INTEGER_ONE).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(repeatList)) {
            return Response.error("商品：" + repeatList + "重复维护");
        }
        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<OrdDisOrderFirst> saveFirstOrderDetail(InsertFirstOrderDetailIn insertFirstOrderDetailIn) {
        List<OrdDisOrderFirstDetail> firstOrderDetails = insertFirstOrderDetailIn.getFirstOrderDetails();
        String channelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(insertFirstOrderDetailIn.getStoreCode(), UserUtil.getBizOrgCode());
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrdDisOrderFirstDetail item : firstOrderDetails) {
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setBizOrgCode(channelBizOrgCode);
            orderGoodsIn.setGoodsCode(item.getGoodsCode());
            orderGoodsIn.setStoreCode(insertFirstOrderDetailIn.getStoreCode());
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
            OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
            if (Objects.nonNull(orderGoodsOut)) {
                if (Objects.isNull(orderGoodsOut.getDistributionUnitPrice()) || BigDecimal.ZERO.compareTo(orderGoodsOut.getDistributionUnitPrice()) == NumberUtil.INTEGER_ZERO) {
                    throw new BusinessException(item.getGoodsCode() + "-此商品无配销价");
                }
                item.setAmount(orderGoodsOut.getDistributionUnitPrice().multiply(new BigDecimal(item.getNum())));
            } else {
                throw new BusinessException(item.getGoodsCode() + "-此商品不可铺货");
            }
            totalAmount = totalAmount.add(item.getAmount());
        }
        Long firstOrderId = insertFirstOrderDetailIn.getFirstOrderId();
        OrdDisOrderFirst firstOrder = new OrdDisOrderFirst();
        firstOrder.setTotalAmount(totalAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        firstOrder.setIsEffectiveImmediately(insertFirstOrderDetailIn.getIsEffectiveImmediately());
        firstOrder.setEffectiveTime(insertFirstOrderDetailIn.getEffectiveTime());
        if (NumberUtil.INTEGER_ONE.equals(insertFirstOrderDetailIn.getIsEffectiveImmediately())) {
            firstOrder.setEffectiveTime(LocalDateTime.now());
        }
        if (firstOrderId == null) {
            // 无单则新增
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(insertFirstOrderDetailIn.getStoreCode());
            if (storeOut == null) {
                return Response.error("门店不存在");
            }
            firstOrder.setStoreName(storeOut.getStoreName());
            firstOrder.setStoreCode(insertFirstOrderDetailIn.getStoreCode());
            firstOrderId = ordDisOrderFirstService.insertFirstOrder(firstOrder);
        } else {
            // 有单则删除整单明细
            OrdDisOrderFirstDetail query = new OrdDisOrderFirstDetail();
            query.setFirstOrderId(firstOrderId);
            ordDisOrderFirstDetailMapper.delete(query);
        }
        // 计算分批入库的批次
        int limit = (firstOrderDetails.size() + MAX_NUMBER - 1) / MAX_NUMBER;
        Long finalFirstOrderId = firstOrderId;
        Stream.iterate(0, n -> n + 1).limit(limit).forEach(a -> {
            // 拿到这个参数的流的 （a * applyIdSelectSize）后面的数据  .limit（applyIdSelectSize）->后面数据的500条  .collect(Collectors.toList()->组成一个toList
            List<OrdDisOrderFirstDetail> insertList = firstOrderDetails.stream().skip(a * MAX_NUMBER).limit(MAX_NUMBER).collect(Collectors.toList());
            // 分批入库
            ordDisOrderFirstDetailMapper.batchInsertFirstOrderDetail(insertList, UserUtil.getNickname(), finalFirstOrderId);
        });

        // 修改铺货单

        // 计算铺货单统计数值
        firstOrder.setId(firstOrderId);
        firstOrder.setGoodsNum(firstOrderDetails.size());
        Integer distributionTotalNum = firstOrderDetails.stream().mapToInt(OrdDisOrderFirstDetail::getNum).sum();
        firstOrder.setTotalNum(distributionTotalNum);
        firstOrder.setUpdateTime(LocalDateTime.now());
        ordDisOrderFirstService.updateByPrimaryKeySelective(firstOrder);
        return Response.data(firstOrder);
    }

    @Override
    public String exportFirstOrderDetail(OrdDisOrderFirstDetailIn ordDisOrderFirstDetailIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        ordDisOrderFirstDetailIn.setPageNum(NumberUtil.INTEGER_ZERO);
        ordDisOrderFirstDetailIn.setPageSize(NumberUtil.INTEGER_ZERO);
        Page<OrdDisOrderFirstDetailOut> detailPage = this.findOrdDisOrderFirstDetailPage(ordDisOrderFirstDetailIn, bizOrgCode);
        if (Objects.isNull(detailPage)) {
            return null;
        }
        List<ExportFirstOrderDetail> excelReplenishmentOrderDetails = this.parseDataToExcel(detailPage.getList());
        String title = "首单铺货订单明细";
        byte[] bytes = FileExportUtil.getFileBytesByData(excelReplenishmentOrderDetails,
                title, title, ExportFirstOrderDetail.class, true);
        return fileService.uploadFile(title + ".xlsx", bytes, OrdSystemConstant.SYSTEM_CODE, OrdSystemConstant.SYSTEM_NAME);
    }

    @Override
    public Response<List<OrdDisOrderFirstDetailOut>> importFirstOrderDetail(String fileId, String storeCode, List<String> goodsCodes) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (null == bytes) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        FirstOrderListener listener = new FirstOrderListener(orderGoodsServer, storeCode, goodsCodes, storeChannelHandle);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportFirstOrderDetail.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        return listener.getResponse();
    }

    @Override
    public List<OrdDisOrderFirstDetail> findDetailListByFirstOrderId(Long firstOrderId) {
        OrdDisOrderFirstDetail ordDisOrderFirstDetail = new OrdDisOrderFirstDetail();
        ordDisOrderFirstDetail.setFirstOrderId(firstOrderId);
        ordDisOrderFirstDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderFirstDetailMapper.select(ordDisOrderFirstDetail);
    }

    private List<ExportFirstOrderDetail> parseDataToExcel(List<OrdDisOrderFirstDetailOut> firstOrderDetailOuts) {

        List<ExportFirstOrderDetail> exportFirstOrderDetails = new ArrayList<>();
        for (int i = 0; i < firstOrderDetailOuts.size(); i++) {
            OrdDisOrderFirstDetailOut firstOrderDetailOut = firstOrderDetailOuts.get(i);
            ExportFirstOrderDetail exportFirstOrderDetail = new ExportFirstOrderDetail();
            BeanUtils.copy(firstOrderDetailOut, exportFirstOrderDetail);
            exportFirstOrderDetail.setWarehouseStr(firstOrderDetailOut.getStockCode() + "【" + firstOrderDetailOut.getStockCode() + "】");
            exportFirstOrderDetail.setSortStr(firstOrderDetailOut.getSortName() + "【" + firstOrderDetailOut.getSort() + "】");
            exportFirstOrderDetail.setIndex(i + 1);
            exportFirstOrderDetail.setWarehouseStr(firstOrderDetailOut.getStockName() + "【" + firstOrderDetailOut.getStockCode() + "】");
            exportFirstOrderDetails.add(exportFirstOrderDetail);
        }
        return exportFirstOrderDetails;
    }

    @Override
    public List<OrdDisOrderFirstDetail> findAllByFirstOrderId(Long firstOrderId) {
        OrdDisOrderFirstDetail ordDirOrderFirstDetail = new OrdDisOrderFirstDetail();
        ordDirOrderFirstDetail.setFirstOrderId(firstOrderId);
        ordDirOrderFirstDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderFirstDetailMapper.select(ordDirOrderFirstDetail);
    }

    @Override
    public List<OrdDisFirstOrderImportErrorResult> checkDetailByAudit(String storeCode, String bizOrgCode, List<OrdDisOrderFirstDetail> detailList,
                                                                      Map<String, OrderGoodsOut> checkGoodsMap, Map<String, Integer> importGoodsNumMap) {
        List<OrdDisFirstOrderImportErrorResult> errorResultList = Lists.newArrayList();
        //校验门店是否存在
        StoreStatusInfo storeStatusInfo = new StoreStatusInfo();
        storeStatusInfo.setBizOrgCode(bizOrgCode);
        storeStatusInfo.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
        storeStatusInfo.setStoreProperty(StoreConstant.StoreProperty.FRANCHISE.getMytValue());
        storeStatusInfo.setStoreCode(storeCode);
        StoreInfo storeInfo = storeCenterService.getStatusStoreInfo(storeStatusInfo);
        if (Objects.isNull(storeInfo)) {
            errorResultList.add(new OrdDisFirstOrderImportErrorResult(storeCode, null, "门店不允许铺货"));
            return errorResultList;
        }
        if (Objects.isNull(checkGoodsMap) || checkGoodsMap.size() == NumberUtil.INTEGER_ZERO) {
            errorResultList.add(new OrdDisFirstOrderImportErrorResult(storeCode, null, "门店商品均不可用"));
            return errorResultList;
        }
        detailList.forEach(detail -> {
            OrderGoodsOut goodsOut = checkGoodsMap.get(detail.getGoodsCode());
            Integer num = importGoodsNumMap.get(detail.getGoodsCode());
            // 校验与封装错误信息
            StringJoiner errorJoiner = this.checkAuditFirstGoods(goodsOut, num);
            if (errorJoiner.length() > 0) {
                errorResultList.add(new OrdDisFirstOrderImportErrorResult(detail.getGoodsCode(), detail.getNum(), errorJoiner.toString()));
            }
        });
        return errorResultList;
    }

    @Override
    public int countByFirstOrderId(Long firstOrderId) {
        OrdDisOrderFirstDetail detail = new OrdDisOrderFirstDetail();
        detail.setFirstOrderId(firstOrderId);
        detail.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderFirstDetailMapper.selectCount(detail);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAfterAuditSuccess(List<OrdDisOrderFirstDetail> updateDetailList, OrdDisOrderFirst ordDisOrderFirst) {
        if (NumberUtil.INTEGER_ONE.equals(ordDisOrderFirst.getIsEffectiveImmediately())) {
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_FIRST_TO_DELIVERY, JSONObject.toJSONString(ordDisOrderFirst), ordDisOrderFirst.getBizOrgCode(), ordDisOrderFirst.getFirstOrderNo());
            SendResponse sendResponse = disFirstToDeliverySender.sendSync(JSONObject.toJSONString(ordDisOrderFirst).getBytes(),System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
            log.info("铺货单转配销单{}消息ID---{}", ordDisOrderFirst.getFirstOrderNo(), sendResponse.getMessageId());
        }
        if (CollectionUtils.isNotEmpty(updateDetailList)) {
            ordDisOrderFirstDetailMapper.batchUpdateNum(updateDetailList);
        }
        //扣减资金
        ordDisOrderFirstService.toFund(ordDisOrderFirst);
        ordDisOrderFirst.setFreezeStatus(FirstOrderFreezeEnum.FREEZE.getKey());
        ordDisOrderFirstService.updateByPrimaryKeySelective(ordDisOrderFirst);
        String payContent = MessageFormat.format(OrdLogTypeEnum.DIS_FIRST_ORDER_GOODS_AUDIT_FREEZE.getName(), ordDisOrderFirst.getTotalAmount());
        //审核日志
        BusinessLog payBusinessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                String.valueOf(ordDisOrderFirst.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), payContent, new Date(), ordDisOrderFirst.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(payBusinessLog);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS_AUDIT.getName(),
                String.valueOf(ordDisOrderFirst.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                OrdLogTypeEnum.FIRST_ORDER_GOODS_AUDIT.getName(), new Date(), ordDisOrderFirst.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public DisFirstOrderCheckOut handleCheckBeforeSaveFirstOrderDetail(OrdDisOrderFirst ordDisOrderFirst, List<ImportFirstOrderDetail> importFirstOrderDetailList, String loginUsername) {
        List<OrdDisOrderFirstDetail> existFirstDetailList = this.findAllByFirstOrderId(ordDisOrderFirst.getId());
        List<OrdDisFirstOrderImportErrorResult> errorResultList = Lists.newArrayList();
        List<OrdDisOrderFirstDetail> saveFirstDetailList = Lists.newArrayList();
        List<OrdDisOrderFirstDetail> deleteFirstDetailList = Lists.newArrayList();
        List<OrdDisOrderFirstDetail> updateFirstDetailList = Lists.newArrayList();
        ordDisOrderFirst.setUpdater(loginUsername);
        List<OrderGoodsOut> orderGoodsOutList = this.processingImportGoodsData(ordDisOrderFirst, importFirstOrderDetailList, existFirstDetailList,
                deleteFirstDetailList, errorResultList, saveFirstDetailList, updateFirstDetailList);
        if (CollectionUtils.isEmpty(orderGoodsOutList)) {
            errorResultList.add(new OrdDisFirstOrderImportErrorResult(ordDisOrderFirst.getStoreCode(), null, "门店商品均不可用"));
            return null;
        }
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<Integer> totalNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<Integer> goodsNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        existFirstDetailList.forEach(ordDirOrderFirstDetail -> {
            totalAmount.getAndSet(totalAmount.get().add(ordDirOrderFirstDetail.getAmount()));
            totalNum.set(totalNum.get() + ordDirOrderFirstDetail.getNum());
            goodsNum.set(goodsNum.get() + NumberUtil.INTEGER_ONE);
        });
        ordDisOrderFirst.setTotalAmount(totalAmount.get().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDisOrderFirst.setTotalNum(totalNum.get());
        ordDisOrderFirst.setGoodsNum(goodsNum.get());
        ordDisOrderFirst.setUpdateTime(LocalDateTime.now());

        DisFirstOrderCheckOut disFirstOrderCheckOut = new DisFirstOrderCheckOut();
        disFirstOrderCheckOut.setFirstOrderId(ordDisOrderFirst.getId());
        disFirstOrderCheckOut.setSaveDetailList(saveFirstDetailList);
        disFirstOrderCheckOut.setUpdateDetailList(updateFirstDetailList);
        disFirstOrderCheckOut.setDeleteDetailList(deleteFirstDetailList);
        disFirstOrderCheckOut.setUpdateOrdDirOrderFirst(ordDisOrderFirst);
        disFirstOrderCheckOut.setErrorResultList(errorResultList);
        return disFirstOrderCheckOut;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveImportFirstOrderDetail(DisFirstOrderCheckOut disFirstOrderCheckOut) {
        OrdDisOrderFirst updateOrdDirOrderFirst = disFirstOrderCheckOut.getUpdateOrdDirOrderFirst();
        if (CollectionUtils.isNotEmpty(disFirstOrderCheckOut.getDeleteDetailList())) {
            ordDisOrderFirstDetailMapper.batchDelete(disFirstOrderCheckOut.getDeleteDetailList());
        }
        if (CollectionUtils.isNotEmpty(disFirstOrderCheckOut.getUpdateDetailList())) {
            ordDisOrderFirstDetailMapper.batchUpdateNum(disFirstOrderCheckOut.getUpdateDetailList());
        }
        if (CollectionUtils.isNotEmpty(disFirstOrderCheckOut.getSaveDetailList())) {
            ordDisOrderFirstDetailMapper.batchInsertFirstOrderDetail(disFirstOrderCheckOut.getSaveDetailList(), updateOrdDirOrderFirst.getUpdater(), updateOrdDirOrderFirst.getId());
        }
        String content = OrdLogTypeEnum.FIRST_ORDER_GOODS_UPDATE.getName();
        ordDisOrderFirstService.updateByPrimaryKey(updateOrdDirOrderFirst);
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.FIRST_ORDER_GOODS.getName(),
                String.valueOf(updateOrdDirOrderFirst.getId()),
                OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                content, new Date(), updateOrdDirOrderFirst.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public Response<DisFirstOrderCheckOut> checkFirstForSaveDetail(SaveDisFirstDetailIn saveDisFirstDetailIn, String loginUsername) {
        OrdDisOrderFirst ordDisOrderFirst = ordDisOrderFirstService.selectByPrimaryKey(saveDisFirstDetailIn.getFirstOrderId());
        if (!FirstOrderStatusEnum.PENDING.getCode().equals(ordDisOrderFirst.getFirstOrderStatus())) {
            return Response.error("只有待审核铺货单可以新增明细");
        }
        List<ImportFirstOrderDetail> importFirstOrderDetailList = saveDisFirstDetailIn.getDetailList().stream().map(detail -> {
            ImportFirstOrderDetail importFirstOrderDetail = new ImportFirstOrderDetail();
            importFirstOrderDetail.setGoodsCode(detail.getGoodsCode());
            importFirstOrderDetail.setDistributionNum(Objects.isNull(detail.getNum()) ? NumberUtil.INTEGER_ZERO : detail.getNum());
            importFirstOrderDetail.setDetailId(detail.getId());
            return importFirstOrderDetail;
        }).collect(Collectors.toList());
        ordDisOrderFirst.setIsEffectiveImmediately(saveDisFirstDetailIn.getIsEffectiveImmediately());
        if (NumberUtil.INTEGER_ONE.equals(saveDisFirstDetailIn.getIsEffectiveImmediately())) {
            ordDisOrderFirst.setEffectiveTime(LocalDateTime.now());
        } else {
            ordDisOrderFirst.setEffectiveTime(saveDisFirstDetailIn.getEffectiveTime());
        }
        // 校验商品
        DisFirstOrderCheckOut disFirstOrderCheckOut = this.handleCheckBeforeSaveFirstOrderDetail(ordDisOrderFirst, importFirstOrderDetailList, loginUsername);
        return Response.data(disFirstOrderCheckOut);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> deleteDetail(OrdDisDeleteFirstOrderDetailIn ordDisDeleteFirstOrderDetailIn) {
        OrdDisOrderFirst ordDisOrderFirst = ordDisOrderFirstService.selectByPrimaryKey(ordDisDeleteFirstOrderDetailIn.getFirstOrderId());
        if (Objects.isNull(ordDisOrderFirst)) {
            return Response.error("不存在的铺货单");
        }
        if (!FirstOrderStatusEnum.PENDING.getCode().equals(ordDisOrderFirst.getFirstOrderStatus())) {
            return Response.error("只有待审核铺货单可以删除");
        }
        List<OrdDisOrderFirstDetail> ordDisOrderFirstDetailList = this.findDetailListByFirstOrderId(ordDisDeleteFirstOrderDetailIn.getFirstOrderId());
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<Integer> totalNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<Integer> goodsNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        ordDisOrderFirstDetailList.forEach(ordDirOrderFirstDetail -> {
            if (ordDirOrderFirstDetail.getId().equals(ordDisDeleteFirstOrderDetailIn.getId())) {
                return;
            }
            totalAmount.getAndSet(totalAmount.get().add(ordDirOrderFirstDetail.getAmount()));
            totalNum.set(totalNum.get() + ordDirOrderFirstDetail.getNum());
            goodsNum.set(goodsNum.get() + NumberUtil.INTEGER_ONE);
        });
        ordDisOrderFirst.setTotalAmount(totalAmount.get().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDisOrderFirst.setTotalNum(totalNum.get());
        ordDisOrderFirst.setGoodsNum(goodsNum.get());
        ordDisOrderFirst.setUpdater(UserUtil.getUserName());
        ordDisOrderFirst.setUpdateTime(LocalDateTime.now());
        ordDisOrderFirstService.updateByPrimaryKey(ordDisOrderFirst);
        ordDisOrderFirstDetailMapper.deleteByPrimaryKey(ordDisDeleteFirstOrderDetailIn.getId());
        return Response.success();
    }

    private List<OrderGoodsOut> processingImportGoodsData(OrdDisOrderFirst ordDirOrderFirst, List<ImportFirstOrderDetail> importFirstOrderDetailList,
                                                          List<OrdDisOrderFirstDetail> existFirstDetailList, List<OrdDisOrderFirstDetail> deleteFirstDetailList,
                                                          List<OrdDisFirstOrderImportErrorResult> errorResultList, List<OrdDisOrderFirstDetail> saveFirstDetailList,
                                                          List<OrdDisOrderFirstDetail> updateFirstDetailList) {
        // 过滤出分货数量大于0的商品代码
        List<String> goodsCodeList = importFirstOrderDetailList.stream()
                .filter(importGoodsOut -> importGoodsOut.getDistributionNum().compareTo(NumberUtil.INTEGER_ZERO) > NumberUtil.INTEGER_ZERO)
                .map(ImportFirstOrderDetail::getGoodsCode).collect(Collectors.toList());
        List<OrderGoodsOut> goodsOuts = null;
        Map<String, OrderGoodsOut> goodsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(goodsCodeList)) {
            OrderGoodsIn goodsIn = new OrderGoodsIn();
            goodsIn.setStoreCode(ordDirOrderFirst.getStoreCode());
            goodsIn.setBizOrgCode(ordDirOrderFirst.getBizOrgCode());
            goodsIn.setGoodsCodeList(goodsCodeList);
            List<OrderGoodsOut> importOrderGoodsOutList = orderGoodsServer.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
            if (CollectionUtils.isNotEmpty(importOrderGoodsOutList)) {
                goodsOuts = importOrderGoodsOutList;
                goodsMap = importOrderGoodsOutList.stream().collect(Collectors.toMap(OrderGoodsOut::getGoodsCode, Function.identity()));
            }
        }
        Map<String, String> existGoodsMap = existFirstDetailList.stream().collect(Collectors.toMap(OrdDisOrderFirstDetail::getGoodsCode, OrdDisOrderFirstDetail::getGoodsCode));
        Map<String, OrderGoodsOut> finalGoodsMap = goodsMap;
        importFirstOrderDetailList.forEach(importFirstOrderDetail -> {
            if (importFirstOrderDetail.getDistributionNum().compareTo(NumberUtil.INTEGER_ZERO) <= NumberUtil.INTEGER_ZERO) {
                if (Objects.isNull(importFirstOrderDetail.getDetailId())) {
                    // 过滤新导入，且数量不正确的
                    return;
                }
                OrdDisOrderFirstDetail deleteDetail = new OrdDisOrderFirstDetail();
                deleteDetail.setId(importFirstOrderDetail.getDetailId());
                deleteFirstDetailList.add(deleteDetail);
                existFirstDetailList.removeIf(ordDirOrderFirstDetail -> importFirstOrderDetail.getDetailId().equals(ordDirOrderFirstDetail.getId()));
                return;
            }
            OrderGoodsOut goodsOut = finalGoodsMap.get(importFirstOrderDetail.getGoodsCode());
            // 校验导入的铺货商品
            StringJoiner errorJoiner = this.checkImportFirstOrderGoods(goodsOut, importFirstOrderDetail.getDetailId(), importFirstOrderDetail.getDistributionNum(), existGoodsMap);
            if (errorJoiner.length() > 0) {
                errorResultList.add(new OrdDisFirstOrderImportErrorResult(importFirstOrderDetail.getGoodsCode(),
                        importFirstOrderDetail.getDistributionNum(), errorJoiner.toString()));
            } else {
                OrdDisOrderFirstDetail detail;
                if (Objects.isNull(importFirstOrderDetail.getDetailId())) {
                    detail = this.initOrdDisOrderFirstDetailByAsync(ordDirOrderFirst, importFirstOrderDetail, goodsOut);
                    saveFirstDetailList.add(detail);
                    existFirstDetailList.add(detail);
                } else {
                    detail = ordDisOrderFirstDetailMapper.selectByPrimaryKey(importFirstOrderDetail.getDetailId());
                    detail.setNum(importFirstOrderDetail.getDistributionNum());
                    BigDecimal amount = goodsOut.getDistributionUnitPrice().multiply(BigDecimal.valueOf(importFirstOrderDetail.getDistributionNum()));
                    detail.setAmount(amount.setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    detail.setUpdater(ordDirOrderFirst.getUpdater());
                    updateFirstDetailList.add(detail);
                    existFirstDetailList.stream().filter(ordDirOrderFirstDetail -> importFirstOrderDetail.getDetailId().equals(ordDirOrderFirstDetail.getId()))
                            .forEach(ordDirOrderFirstDetail -> Collections.replaceAll(existFirstDetailList, ordDirOrderFirstDetail, detail));
                }
            }
        });
        return goodsOuts;
    }

    private static OrdDisOrderFirstDetail initOrdDisOrderFirstDetailByAsync(OrdDisOrderFirst ordDirOrderFirst, ImportFirstOrderDetail importFirstOrderDetail, OrderGoodsOut goodsOut) {
        OrdDisOrderFirstDetail detail = new OrdDisOrderFirstDetail();
        detail.setFirstOrderId(ordDirOrderFirst.getId());
        detail.setGoodsCode(importFirstOrderDetail.getGoodsCode());
        detail.setGoodsName(goodsOut.getGoodsName());
        detail.setNum(importFirstOrderDetail.getDistributionNum());
        BigDecimal amount = goodsOut.getDistributionUnitPrice().multiply(BigDecimal.valueOf(importFirstOrderDetail.getDistributionNum()));
        detail.setAmount(amount.setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        detail.setCreator(ordDirOrderFirst.getUpdater());
        detail.setUpdater(ordDirOrderFirst.getUpdater());
        detail.setIsDelete(ModelConst.DELETE.NO);
        return detail;
    }


    private StringJoiner checkImportFirstOrderGoods(OrderGoodsOut goodsOut, Long detailId, Integer num, Map<String, String> existDetailMap) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        if (Objects.isNull(goodsOut)) {
            errorJoiner.add("该商品不存在");
            return errorJoiner;
        }
        if (Objects.isNull(detailId) && existDetailMap.containsKey(goodsOut.getGoodsCode())) {
            errorJoiner.add("单据已存在该商品");
        }
        //门店商品上下架
        if (NumberUtil.INTEGER_ONE.equals(goodsOut.getIsShelves())) {
            errorJoiner.add("商品已下架");
        }
        //商品开关信息
        GoodsStatusBusinessSwitch goodsStatusBusinessSwitch = goodsOut.getGoodsStatusBusinessSwitch();
        if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsFirstOrderDis())) {
            errorJoiner.add("不可被铺货");
        }
        //校验规格信息是否存在
        StandardSpecTransInfoOut distributionSpecification = goodsOut.getDistributionSpecification();
        if (Objects.isNull(distributionSpecification)) {
            errorJoiner.add("配货规格不存在");
        }
        Integer packageNum = num % distributionSpecification.getQpc();
        if (packageNum.compareTo(NumberUtil.INTEGER_ZERO) != NumberUtil.INTEGER_ZERO) {
            errorJoiner.add("铺货包装数必须为规格整数倍");
        }
        //校验配货数量是否合法
        if (Objects.isNull(goodsOut.getDistributionSpecification().getQpc())) {
            errorJoiner.add("配货规格数量不合法");
        }
        //校验商品规格是否合法
        String qpcStr = distributionSpecification.getQpcStr();
        if (StringUtils.isBlank(qpcStr)) {
            errorJoiner.add("配货规格不合法");
        }
        if (StringUtils.isBlank(goodsOut.getDistributionWay())) {
            errorJoiner.add("所属配送方案不存在该商品");
        }
        if (!DistributionWaysEnum.UNIFIEDDIS.getType().equals(goodsOut.getDistributionWay())
                && !DistributionWaysEnum.TRANSFER.getType().equals(goodsOut.getDistributionWay())) {
            errorJoiner.add("当前配送方式是" + DistributionWaysEnum.getNameByType(goodsOut.getDistributionWay()));
        }
        //校验配送价
        if (Objects.isNull(goodsOut.getDistributionUnitPrice())) {
            errorJoiner.add("配送价为空");
        }
        return errorJoiner;
    }

    private StringJoiner checkAuditFirstGoods(OrderGoodsOut goodsOut, Integer num) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        if (Objects.isNull(goodsOut)) {
            errorJoiner.add("该商品不存在");
            return errorJoiner;
        }
        //门店商品上下架
        if (NumberUtil.INTEGER_ONE.equals(goodsOut.getIsShelves())) {
            errorJoiner.add("商品已下架");
        }
        //商品开关信息
        GoodsStatusBusinessSwitch goodsStatusBusinessSwitch = goodsOut.getGoodsStatusBusinessSwitch();
        if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsFirstOrderDis())) {
            errorJoiner.add("不可被铺货");
        }
        //校验规格信息是否存在
        StandardSpecTransInfoOut distributionSpecification = goodsOut.getDistributionSpecification();
        if (Objects.isNull(distributionSpecification)) {
            errorJoiner.add("配货规格不存在");
        }
        Integer packageNum = num % distributionSpecification.getQpc();
        if (packageNum.compareTo(NumberUtil.INTEGER_ZERO) != NumberUtil.INTEGER_ZERO) {
            errorJoiner.add("铺货包装数必须为规格整数倍");
        }
        //校验配货数量是否合法
        if (Objects.isNull(goodsOut.getDistributionSpecification().getQpc())) {
            errorJoiner.add("配货规格数量不合法");
        }
        //校验商品规格是否合法
        String qpcStr = distributionSpecification.getQpcStr();
        if (StringUtils.isBlank(qpcStr)) {
            errorJoiner.add("配货规格不合法");
        }
        if (StringUtils.isBlank(goodsOut.getDistributionWay())) {
            errorJoiner.add("所属配送方案不存在该商品");
        }
        if (!DistributionWaysEnum.UNIFIEDDIS.getType().equals(goodsOut.getDistributionWay())
                && !DistributionWaysEnum.TRANSFER.getType().equals(goodsOut.getDistributionWay())) {
            errorJoiner.add("当前配送方式是" + DistributionWaysEnum.getNameByType(goodsOut.getDistributionWay()));
        }
        //校验配送价
        if (Objects.isNull(goodsOut.getDistributionUnitPrice())) {
            errorJoiner.add("配送价为空");
        }
//        FlashSaleCheckOut flashSaleCheckOut = shoppingCartService.isCanBuyByDistributionOrder(goodsOut.getGoodsCode(), goodsOut.getBizOrgCode(), targetDateTime);
//        if (Objects.nonNull(flashSaleCheckOut) && NumberUtil.INTEGER_ZERO.equals(flashSaleCheckOut.getIsCanBuyFlashSale())) {
//            errorJoiner.add("当前时间不可订货");
//        }
        return errorJoiner;
    }


    /**
     * 校验商品信息
     *
     * @param goodsCode
     * @param storeCode
     * @param bizOrgCode
     */
    public void checkOrderGoods(String goodsCode, String storeCode, String bizOrgCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setGoodsCode(goodsCode);
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
        OrderGoodsOut storeOrderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.nonNull(storeOrderGoods)) {
            if (Objects.isNull(storeOrderGoods.getDistributionPrice()) || BigDecimal.ZERO.compareTo(storeOrderGoods.getDistributionPrice()) == NumberUtil.INTEGER_ZERO) {
                throw new BusinessException(goodsCode + "-此商品无配销价");
            }
        } else {
            throw new BusinessException(goodsCode + "-此商品不可铺货");
        }
    }
}
