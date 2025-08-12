package com.edc.erp.directly.dirfirstorder.service.impl;

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
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.store.StoreStatusInfo;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.StandardSpecTransInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDetail;
import com.edc.erp.directly.dirfirstorder.listener.FirstDirOrderDetailListener;
import com.edc.erp.directly.dirfirstorder.mapper.OrdDirOrderFirstDetailMapper;
import com.edc.erp.directly.dirfirstorder.mapper.OrdDirOrderFirstMapper;
import com.edc.erp.directly.dirfirstorder.model.excel.ExportFirstOrderDetail;
import com.edc.erp.directly.dirfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.erp.directly.dirfirstorder.model.excel.OrdDirFirstOrderImportErrorResult;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirDeleteFirstOrderDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.SaveDirFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.out.DirFirstOrderCheckOut;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDirOrderFirstDetailOut;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstDetailService;
import com.edc.erp.directly.util.FileExportUtil;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 铺货单明细表(OrdDirOrderFirstDetail)表服务实现类
 *
 * @author weichao
 * @since 2022-11-10 14:09:10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDirOrderFirstDetailServiceImpl extends BaseServiceImpl<OrdDirOrderFirstDetail> implements OrdDirOrderFirstDetailService {
    private final OrdDirOrderFirstDetailMapper ordDirOrderFirstDetailMapper;
    private final FileService fileService;
    private final OrderGoodsServer orderGoodsServer;
    private final OrdDirOrderFirstMapper ordDirOrderFirstMapper;

    private final AsyncLogService asyncLogService;
    private final StoreCenterService storeCenterService;

    private final AsyncPushTaskService asyncPushTaskService;

    private final StoreChannelHandle storeChannelHandle;

    @Qualifier("dirFirstToDeliverySender")
    private final MessageSender dirFirstToDeliverySender;

    @Override
    public List<OrdDirOrderFirstDetailOut> findOrdDirOrderFirstDetail(OrdDirOrderFirstDetailIn ordDirOrderFirstDetailIn) {
        return ordDirOrderFirstDetailMapper.findOrdDisOrderFirstDetailByPage(ordDirOrderFirstDetailIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertFirstOrderDetail(List<OrdDirOrderFirstDetail> insertList, String userName, Long finalFirstOrderId) {
        ordDirOrderFirstDetailMapper.batchInsertFirstOrderDetail(insertList, userName, finalFirstOrderId);
    }

    @Override
    public Response<List<OrdDirOrderFirstDetailOut>> importFirstOrderDetail(String fileId, String storeCode, List<String> goodsCodes) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (null == bytes) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        FirstDirOrderDetailListener listener = new FirstDirOrderDetailListener(orderGoodsServer, storeCode, goodsCodes, storeChannelHandle);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportFirstOrderDetail.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        return listener.getResponse();
    }

    @Override
    public String exportFirstOrderDetail(OrdDirOrderFirstDetailIn ordDirOrderFirstDetailIn, List<OrdDirOrderFirstDetailOut> ordDirOrderFirstDetails) {
        List<ExportFirstOrderDetail> excelReplenishmentOrderDetails = this.parseDataToExcel(ordDirOrderFirstDetails);
        String title = "首单铺货订单明细";
        byte[] bytes = FileExportUtil.getFileBytesByData(excelReplenishmentOrderDetails,
                title, title, ExportFirstOrderDetail.class, true);
        return fileService.uploadFile(title + ".xlsx", bytes, OrdSystemConstant.SYSTEM_CODE, OrdSystemConstant.SYSTEM_NAME);
    }

    /**
     * 根据主键删除铺货单明细
     *
     * @param firstOrderId
     */
    @Override
    public void deleteByFirstOrderId(Long firstOrderId) {
        OrdDirOrderFirstDetail ordDirOrderFirstDetail = new OrdDirOrderFirstDetail();
        ordDirOrderFirstDetail.setFirstOrderId(firstOrderId);
        ordDirOrderFirstDetail.setIsDelete(NumberUtil.INTEGER_ZERO);
        ordDirOrderFirstDetailMapper.delete(ordDirOrderFirstDetail);
    }

    @Override
    public DirFirstOrderCheckOut handleCheckBeforeSaveFirstOrderDetail(OrdDirOrderFirst ordDirOrderFirst, List<ImportFirstOrderDetail> importFirstOrderDetailList, String loginUsername) {
        List<OrdDirOrderFirstDetail> existFirstDetailList = this.findAllByFirstOrderId(ordDirOrderFirst.getId());
        List<OrdDirFirstOrderImportErrorResult> errorResultList = Lists.newArrayList();
        List<OrdDirOrderFirstDetail> saveFirstDetailList = Lists.newArrayList();
        List<OrdDirOrderFirstDetail> deleteFirstDetailList = Lists.newArrayList();
        List<OrdDirOrderFirstDetail> updateFirstDetailList = Lists.newArrayList();
        ordDirOrderFirst.setUpdater(loginUsername);
        List<OrderGoodsOut> orderGoodsOutList = this.processingImportGoodsData(ordDirOrderFirst, importFirstOrderDetailList, existFirstDetailList, deleteFirstDetailList, errorResultList, saveFirstDetailList, updateFirstDetailList);
        if (CollectionUtils.isEmpty(orderGoodsOutList)) {
            errorResultList.add(new OrdDirFirstOrderImportErrorResult(ordDirOrderFirst.getStoreCode(), null, "门店商品均不可用"));
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
        ordDirOrderFirst.setTotalAmount(totalAmount.get().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDirOrderFirst.setTotalNum(totalNum.get());
        ordDirOrderFirst.setGoodsNum(goodsNum.get());
        ordDirOrderFirst.setUpdateTime(LocalDateTime.now());

        DirFirstOrderCheckOut dirFirstOrderCheckOut = new DirFirstOrderCheckOut();
        dirFirstOrderCheckOut.setFirstOrderId(ordDirOrderFirst.getId());
        dirFirstOrderCheckOut.setSaveDetailList(saveFirstDetailList);
        dirFirstOrderCheckOut.setUpdateDetailList(updateFirstDetailList);
        dirFirstOrderCheckOut.setDeleteDetailList(deleteFirstDetailList);
        dirFirstOrderCheckOut.setUpdateOrdDirOrderFirst(ordDirOrderFirst);
        dirFirstOrderCheckOut.setErrorResultList(errorResultList);
        return dirFirstOrderCheckOut;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveImportFirstOrderDetail(DirFirstOrderCheckOut dirFirstOrderCheckOut) {
        OrdDirOrderFirst updateOrdDirOrderFirst = dirFirstOrderCheckOut.getUpdateOrdDirOrderFirst();
        if (CollectionUtils.isNotEmpty(dirFirstOrderCheckOut.getDeleteDetailList())) {
            ordDirOrderFirstDetailMapper.batchDelete(dirFirstOrderCheckOut.getDeleteDetailList());
        }
        if (CollectionUtils.isNotEmpty(dirFirstOrderCheckOut.getUpdateDetailList())) {
            ordDirOrderFirstDetailMapper.batchUpdateNum(dirFirstOrderCheckOut.getUpdateDetailList());
        }
        if (CollectionUtils.isNotEmpty(dirFirstOrderCheckOut.getSaveDetailList())) {
            ordDirOrderFirstDetailMapper.batchInsertFirstOrderDetail(dirFirstOrderCheckOut.getSaveDetailList(), updateOrdDirOrderFirst.getUpdater(), updateOrdDirOrderFirst.getId());
        }
        String content = OrdLogTypeEnum.FIRST_ORDER_GOODS_UPDATE.getName();
        ordDirOrderFirstMapper.updateByPrimaryKey(updateOrdDirOrderFirst);
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getName(),
                String.valueOf(updateOrdDirOrderFirst.getId()),
                OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
                content, new Date(), updateOrdDirOrderFirst.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public int countByFirstOrderId(Long firstOrderId) {
        OrdDirOrderFirstDetail detail = new OrdDirOrderFirstDetail();
        detail.setFirstOrderId(firstOrderId);
        detail.setIsDelete(ModelConst.DELETE.NO);
        return ordDirOrderFirstDetailMapper.selectCount(detail);
    }

    private List<OrderGoodsOut> processingImportGoodsData(OrdDirOrderFirst ordDirOrderFirst, List<ImportFirstOrderDetail> importFirstOrderDetailList,
                                                          List<OrdDirOrderFirstDetail> existFirstDetailList, List<OrdDirOrderFirstDetail> deleteFirstDetailList,
                                                          List<OrdDirFirstOrderImportErrorResult> errorResultList, List<OrdDirOrderFirstDetail> saveFirstDetailList,
                                                          List<OrdDirOrderFirstDetail> updateFirstDetailList) {
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
        Map<String, String> existGoodsMap = existFirstDetailList.stream().collect(Collectors.toMap(OrdDirOrderFirstDetail::getGoodsCode, OrdDirOrderFirstDetail::getGoodsCode));
        Map<String, OrderGoodsOut> finalGoodsMap = goodsMap;
        importFirstOrderDetailList.forEach(importFirstOrderDetail -> {
            if (importFirstOrderDetail.getDistributionNum().compareTo(NumberUtil.INTEGER_ZERO) <= NumberUtil.INTEGER_ZERO) {
                if (Objects.isNull(importFirstOrderDetail.getDetailId())) {
                    // 过滤新导入，且数量不正确的
                    return;
                }
                OrdDirOrderFirstDetail deleteDetail = new OrdDirOrderFirstDetail();
                deleteDetail.setId(importFirstOrderDetail.getDetailId());
                deleteFirstDetailList.add(deleteDetail);
                existFirstDetailList.removeIf(ordDirOrderFirstDetail -> importFirstOrderDetail.getDetailId().equals(ordDirOrderFirstDetail.getId()));
                return;
            }
            OrderGoodsOut goodsOut = finalGoodsMap.get(importFirstOrderDetail.getGoodsCode());
            // 校验导入的铺货商品
            StringJoiner errorJoiner = this.checkImportFirstOrderGoods(goodsOut, importFirstOrderDetail.getDetailId(), importFirstOrderDetail.getDistributionNum(), existGoodsMap);
            if (errorJoiner.length() > 0) {
                errorResultList.add(new OrdDirFirstOrderImportErrorResult(importFirstOrderDetail.getGoodsCode(),
                        importFirstOrderDetail.getDistributionNum(), errorJoiner.toString()));
            } else {
                OrdDirOrderFirstDetail detail;
                if (Objects.isNull(importFirstOrderDetail.getDetailId())) {
                    detail = this.initOrdDirOrderFirstDetailByAsync(ordDirOrderFirst, importFirstOrderDetail, goodsOut);
                    saveFirstDetailList.add(detail);
                    existFirstDetailList.add(detail);
                } else {
                    detail = ordDirOrderFirstDetailMapper.selectByPrimaryKey(importFirstOrderDetail.getDetailId());
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

    private static OrdDirOrderFirstDetail initOrdDirOrderFirstDetailByAsync(OrdDirOrderFirst ordDirOrderFirst, ImportFirstOrderDetail importFirstOrderDetail, OrderGoodsOut goodsOut) {
        OrdDirOrderFirstDetail detail = new OrdDirOrderFirstDetail();
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

    @Override
    public List<OrdDirOrderFirstDetail> findAllByFirstOrderId(Long firstOrderId) {
        OrdDirOrderFirstDetail ordDirOrderFirstDetail = new OrdDirOrderFirstDetail();
        ordDirOrderFirstDetail.setFirstOrderId(firstOrderId);
        ordDirOrderFirstDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDirOrderFirstDetailMapper.select(ordDirOrderFirstDetail);
    }


    @Override
    public List<OrdDirFirstOrderImportErrorResult> checkDetailByAudit(String storeCode, String bizOrgCode, List<OrdDirOrderFirstDetail> detailList,
                                                                      Map<String, OrderGoodsOut> checkGoodsMap, Map<String, Integer> importGoodsNumMap) {
        List<OrdDirFirstOrderImportErrorResult> errorResultList = Lists.newArrayList();
        //校验门店是否存在
        StoreStatusInfo storeStatusInfo = new StoreStatusInfo();
        storeStatusInfo.setBizOrgCode(bizOrgCode);
        storeStatusInfo.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
        storeStatusInfo.setStoreProperty(StoreConstant.StoreProperty.DIRECTLY.getMytValue());
        storeStatusInfo.setStoreCode(storeCode);
        StoreInfo storeInfo = storeCenterService.getStatusStoreInfo(storeStatusInfo);
        if (Objects.isNull(storeInfo)) {
            errorResultList.add(new OrdDirFirstOrderImportErrorResult(storeCode, null, "门店不允许铺货"));
            return errorResultList;
        }
        if (Objects.isNull(checkGoodsMap) || checkGoodsMap.size() == NumberUtil.INTEGER_ZERO) {
            errorResultList.add(new OrdDirFirstOrderImportErrorResult(storeCode, null, "门店商品均不可用"));
            return errorResultList;
        }
        detailList.forEach(detail -> {
            OrderGoodsOut goodsOut = checkGoodsMap.get(detail.getGoodsCode());
            Integer num = importGoodsNumMap.get(detail.getGoodsCode());
            // 校验与封装错误信息
            StringJoiner errorJoiner = this.checkAuditFirstGoods(goodsOut, num);
            if (errorJoiner.length() > 0) {
                errorResultList.add(new OrdDirFirstOrderImportErrorResult(detail.getGoodsCode(), detail.getNum(), errorJoiner.toString()));
            }
        });
        return errorResultList;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAfterAuditSuccess(List<OrdDirOrderFirstDetail> updateDetailList, OrdDirOrderFirst ordDirOrderFirst) {
        if (NumberUtil.INTEGER_ONE.equals(ordDirOrderFirst.getIsEffectiveImmediately())) {
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_FIRST_TO_DELIVERY, JSONObject.toJSONString(ordDirOrderFirst), ordDirOrderFirst.getBizOrgCode(), ordDirOrderFirst.getFirstOrderNo());
            SendResponse sendResponse = dirFirstToDeliverySender.sendSync(JSONObject.toJSONString(ordDirOrderFirst).getBytes(),System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_TIME);
            log.info("直营铺货单转配货单{}消息ID---{}", ordDirOrderFirst.getFirstOrderNo(), sendResponse.getMessageId());
        }
        if (CollectionUtils.isNotEmpty(updateDetailList)) {
            ordDirOrderFirstDetailMapper.batchUpdateNum(updateDetailList);
        }
        ordDirOrderFirstMapper.updateByPrimaryKeySelective(ordDirOrderFirst);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS_AUDIT.getName(),
                String.valueOf(ordDirOrderFirst.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
                OrdLogTypeEnum.FIRST_ORDER_GOODS_AUDIT.getName(), new Date(), ordDirOrderFirst.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }


    @Override
    public Response<DirFirstOrderCheckOut> checkFirstForSaveDetail(SaveDirFirstDetailIn saveDirFirstDetailIn, String loginUsername) {
        OrdDirOrderFirst ordDirOrderFirst = ordDirOrderFirstMapper.selectByPrimaryKey(saveDirFirstDetailIn.getFirstOrderId());
        if (!FirstOrderStatusEnum.PENDING.getCode().equals(ordDirOrderFirst.getFirstOrderStatus())) {
            return Response.error("只有待审核铺货单可以新增明细");
        }
        List<ImportFirstOrderDetail> importFirstOrderDetailList = saveDirFirstDetailIn.getDetailList().stream().map(detail -> {
            ImportFirstOrderDetail importFirstOrderDetail = new ImportFirstOrderDetail();
            importFirstOrderDetail.setGoodsCode(detail.getGoodsCode());
            importFirstOrderDetail.setDistributionNum(Objects.isNull(detail.getNum()) ? NumberUtil.INTEGER_ZERO : detail.getNum());
            importFirstOrderDetail.setDetailId(detail.getId());
            return importFirstOrderDetail;
        }).collect(Collectors.toList());
        ordDirOrderFirst.setIsEffectiveImmediately(saveDirFirstDetailIn.getIsEffectiveImmediately());
        if (NumberUtil.INTEGER_ONE.equals(saveDirFirstDetailIn.getIsEffectiveImmediately())) {
            ordDirOrderFirst.setEffectiveTime(LocalDateTime.now());
        } else {
            ordDirOrderFirst.setEffectiveTime(saveDirFirstDetailIn.getEffectiveTime());
        }
        // 校验商品
        DirFirstOrderCheckOut dirFirstOrderCheckOut = this.handleCheckBeforeSaveFirstOrderDetail(ordDirOrderFirst, importFirstOrderDetailList, loginUsername);
        return Response.data(dirFirstOrderCheckOut);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> deleteDetail(OrdDirDeleteFirstOrderDetailIn ordDirDeleteFirstOrderDetailIn) {
        OrdDirOrderFirst ordDirOrderFirst = ordDirOrderFirstMapper.selectByPrimaryKey(ordDirDeleteFirstOrderDetailIn.getFirstOrderId());
        if (Objects.isNull(ordDirOrderFirst)) {
            return Response.error("不存在的铺货单");
        }
        if (!FirstOrderStatusEnum.PENDING.getCode().equals(ordDirOrderFirst.getFirstOrderStatus())) {
            return Response.error("只有待审核铺货单可以删除");
        }
        List<OrdDirOrderFirstDetail> ordDisOrderFirstDetailList = this.findAllByFirstOrderId(ordDirDeleteFirstOrderDetailIn.getFirstOrderId());
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<Integer> totalNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<Integer> goodsNum = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        ordDisOrderFirstDetailList.forEach(ordDirOrderFirstDetail -> {
            if (ordDirOrderFirstDetail.getId().equals(ordDirDeleteFirstOrderDetailIn.getId())) {
                return;
            }
            totalAmount.getAndSet(totalAmount.get().add(ordDirOrderFirstDetail.getAmount()));
            totalNum.set(totalNum.get() + ordDirOrderFirstDetail.getNum());
            goodsNum.set(goodsNum.get() + NumberUtil.INTEGER_ONE);
        });
        ordDirOrderFirstDetailMapper.deleteByPrimaryKey(ordDirDeleteFirstOrderDetailIn.getId());
        ordDirOrderFirst.setTotalAmount(totalAmount.get().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDirOrderFirst.setTotalNum(totalNum.get());
        ordDirOrderFirst.setGoodsNum(goodsNum.get());
        ordDirOrderFirst.setUpdater(UserUtil.getUserName());
        ordDirOrderFirst.setUpdateTime(LocalDateTime.now());
        ordDirOrderFirstMapper.updateByPrimaryKey(ordDirOrderFirst);
        return Response.success();
    }

    private List<ExportFirstOrderDetail> parseDataToExcel(List<OrdDirOrderFirstDetailOut> ordDirOrderFirstDetails) {
        List<ExportFirstOrderDetail> exportFirstOrderDetails = new ArrayList<>();
        for (int i = 0; i < ordDirOrderFirstDetails.size(); i++) {
            OrdDirOrderFirstDetailOut firstOrderDetailOut = ordDirOrderFirstDetails.get(i);
            ExportFirstOrderDetail exportFirstOrderDetail = new ExportFirstOrderDetail();
            BeanUtils.copy(firstOrderDetailOut, exportFirstOrderDetail);
            exportFirstOrderDetail.setWarehouseStr(firstOrderDetailOut.getStockName() + "【" + firstOrderDetailOut.getStockCode() + "】");
            exportFirstOrderDetail.setSortStr(firstOrderDetailOut.getSortName() + "【" + firstOrderDetailOut.getSort() + "】");
            exportFirstOrderDetail.setIndex(i + 1);
            exportFirstOrderDetail.setWarehouseStr(firstOrderDetailOut.getStockName() + "【" + firstOrderDetailOut.getStockCode() + "】");
            exportFirstOrderDetails.add(exportFirstOrderDetail);
        }
        return exportFirstOrderDetails;
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
        if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsFirstOrder())) {
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
        if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsFirstOrder())) {
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
}
