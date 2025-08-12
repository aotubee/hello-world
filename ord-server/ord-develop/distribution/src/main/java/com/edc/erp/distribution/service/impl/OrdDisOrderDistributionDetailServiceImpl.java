package com.edc.erp.distribution.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrdSystemConstant;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.model.entity.GoodsStatusBusinessSwitch;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.FlashSaleCheckOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.StandardSpecTransInfoOut;
import com.edc.erp.common.model.out.store.StoreAndStatusSwitchOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.model.out.store.StoreStatusBusinessSwitch;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disordercart.service.DisShoppingCartService;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionDetail;
import com.edc.erp.distribution.mapper.OrdDisOrderDistributionDetailMapper;
import com.edc.erp.distribution.mapper.OrdDisOrderDistributionMapper;
import com.edc.erp.distribution.model.excel.OrdDisDistributionImportErrorResult;
import com.edc.erp.distribution.model.in.DeleteStoreGoodsIn;
import com.edc.erp.distribution.model.in.QueryOrderDistributionDetailIn;
import com.edc.erp.distribution.model.in.SaveDisDistributionDetailIn;
import com.edc.erp.distribution.model.out.*;
import com.edc.erp.distribution.service.OrdDisOrderDistributionDetailService;
import com.edc.erp.distribution.service.OrdDisOrderDistributionResultService;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.erp.enumeration.DistributionIdentificationEnum;
import com.edc.erp.enumeration.DistributionOrderLogEnum;
import com.edc.erp.enumeration.OrderDistributionOrderStatusEnum;
import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.erp.presale.service.OrdDisPresaleAssetsService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 配销分货门店商品关联表(OrdDisOrderDistributionDetail)表服务实现类
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisOrderDistributionDetailServiceImpl extends BaseServiceImpl<OrdDisOrderDistributionDetail> implements OrdDisOrderDistributionDetailService {

    private final OrdDisOrderDistributionDetailMapper ordDisOrderDistributionDetailMapper;

    private final FileService fileService;

    private final AsyncLogService asyncLogService;

    private final OrderGoodsServer orderGoodsServer;

    private final StoreCenterService storeCenterService;

    private final OrdDisOrderDistributionMapper ordDisOrderDistributionMapper;

    private final OrdDisOrderDistributionResultService ordDisOrderDistributionResultService;

    private final DisShoppingCartService shoppingCartService;

    private final AsyncPushTaskService asyncPushTaskService;

    private final OrdDisPresaleAssetsService ordDisPresaleAssetsService;

    private final OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;

    @Qualifier("disDistributionCreateOrderSender")
    private final MessageSender disDistributionCreateOrderSender;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrdDisOrderDistributionDetail(OrdDisOrderDistributionDetail ordDisOrderDistributionDetail) {
        //查询明细是否存在
        OrdDisOrderDistributionDetail detail = this.getDetail(ordDisOrderDistributionDetail.getId());
        if (Objects.isNull(detail)) {
            throw new BusinessException("配销分货门店商品不存在");
        }
        //校验商品
        OrderGoodsOut goodsOut = this.checkDetail(ordDisOrderDistributionDetail);
        //更新配送价
        ordDisOrderDistributionDetail.setOriginalPrice(goodsOut.getDistributionUnitPrice());
        //更新配送规格
        ordDisOrderDistributionDetail.setDistributionSpecification(goodsOut.getDistributionSpecification().getQpcStr());
        //分货金额
        ordDisOrderDistributionDetail.setDistributionAmount(goodsOut.getDistributionUnitPrice().multiply(ordDisOrderDistributionDetail.getDistributionQuantity()));
        //当前库存数量
        ordDisOrderDistributionDetail.setWrhInvQty(goodsOut.getStockQuantity());
        //更新时间
        ordDisOrderDistributionDetail.setUpdateTime(LocalDateTime.now());
        //更新明细
        int updateCount = ordDisOrderDistributionDetailMapper.updateByPrimaryKeySelective(ordDisOrderDistributionDetail);
        if (!NumberUtil.INTEGER_ZERO.equals(updateCount)) {
            //更新出货单
            this.updateOrder(ordDisOrderDistributionDetail.getDistributionOrderId());
        }
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDisOrderDistributionDetail.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_DETAIL_UPDATE.getName(), new Date(), ordDisOrderDistributionDetail.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return updateCount;
    }

    /**
     * 删除配销分货门店商品
     *
     * @param deleteStoreGoodsIn 删除门店商品关联入参类
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOrdDisOrderDistributionDetail(DeleteStoreGoodsIn deleteStoreGoodsIn) {
        //查询明细是否存在
        OrdDisOrderDistributionDetail detail = this.getDetail(deleteStoreGoodsIn.getStoreGoodsId());
        if (Objects.isNull(detail)) {
            throw new BusinessException("配销分货门店商品不存在");
        }
        //删除配销分货门店商品关联表
        OrdDisOrderDistributionDetail query = new OrdDisOrderDistributionDetail();
        query.setIsDelete(ModelConst.DELETE.YES);
        query.setId(detail.getId());
        //更新
        int updateCount = ordDisOrderDistributionDetailMapper.updateByPrimaryKeySelective(query);
        if (!NumberUtil.INTEGER_ZERO.equals(updateCount)) {
            this.updateOrder(deleteStoreGoodsIn.getDistributionOrderId());
        }
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(detail.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_DETAIL_DELETE.getName(), new Date(), detail.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return updateCount;
    }

    @Override
    public List<OrdDisOrderDistributionDetail> findOrdDisOrderDistributions(Long distributionOrderId, String goodsCode, String storeCode) {
        OrdDisOrderDistributionDetail query = new OrdDisOrderDistributionDetail();
        query.setId(distributionOrderId);
        query.setStoreCode(storeCode);
        query.setIsDelete(ModelConst.DELETE.NO);
        query.setGoodsCode(goodsCode);
        return ordDisOrderDistributionDetailMapper.select(query);
    }

    /**
     * 获取直营分货明细
     *
     * @param distributionOrderId 分货单id
     * @param goodsCode           商品code
     * @param storeCode           门店code
     * @return
     */
    @Override
    public OrdDisOrderDistributionDetail getOrdDisOrderDistribution(Long distributionOrderId, String goodsCode, String storeCode) {
        OrdDisOrderDistributionDetail query = new OrdDisOrderDistributionDetail();
        query.setDistributionOrderId(distributionOrderId);
        query.setStoreCode(storeCode);
        query.setIsDelete(ModelConst.DELETE.NO);
        query.setGoodsCode(goodsCode);
        return ordDisOrderDistributionDetailMapper.selectOne(query);
    }


    /**
     * 统计分货单总数量
     *
     * @param distributionOrderId 配销分货单主键
     * @return
     */
    @Override
    public BigDecimal sumTotalDistributionQuantity(Long distributionOrderId) {
        return ordDisOrderDistributionDetailMapper.sumTotalDistributionQuantity(distributionOrderId);
    }

    /**
     * 分页查询配销分货门店商品
     *
     * @param queryOrderDistributionDetailIn 配销分货门店商品入参类
     * @return
     */
    @Override
    public Page<OrdDisOrderDistributionDetailOut> findByPage(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn) {
        List<OrdDisOrderDistributionDetailOut> detailOuts = ordDisOrderDistributionDetailMapper.findByPage(queryOrderDistributionDetailIn);
        Page<OrdDisOrderDistributionDetailOut> detailPage = new Page<>(queryOrderDistributionDetailIn);
        detailPage.setList(detailOuts);
        return detailPage;
    }

    /**
     * 根据分货单id查询当前商品门店明细
     *
     * @param ordDistributionOrderId 分货单id
     * @return
     */
    @Override
    public List<OrdDisOrderDistributionDetail> getDetailByOrdDistributionOrderId(Long ordDistributionOrderId) {
        OrdDisOrderDistributionDetail ordDisOrderDistributionDetail = new OrdDisOrderDistributionDetail();
        ordDisOrderDistributionDetail.setDistributionOrderId(ordDistributionOrderId);
        ordDisOrderDistributionDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderDistributionDetailMapper.select(ordDisOrderDistributionDetail);
    }

    /**
     * 保存出货单明细
     *
     * @param distributionOrder              出货单
     * @param ordDisOrderDistributionDetails 出货单明细
     * @param isUpdateOrder                  是否更新分货单   0否 1是
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OrdDisOrderDistribution distributionOrder, List<OrdDisOrderDistributionDetail> ordDisOrderDistributionDetails, int isUpdateOrder) {
        //先删除明细防止重复添加
        OrdDisOrderDistributionDetail ordDisOrderDistributionDetail = new OrdDisOrderDistributionDetail();
        ordDisOrderDistributionDetail.setDistributionOrderId(distributionOrder.getId());
        ordDisOrderDistributionDetailMapper.delete(ordDisOrderDistributionDetail);
        ordDisOrderDistributionDetails.forEach(item -> {
            //校验商品
            OrderGoodsOut goodsOut = this.checkDetail(item);
            //添加分货单id
            item.setDistributionOrderId(distributionOrder.getId());
            //更新配送价
            item.setOriginalPrice(goodsOut.getDistributionUnitPrice());
            //更新配送规格
            item.setDistributionSpecification(goodsOut.getDistributionSpecification().getQpcStr());
            //分货金额
            item.setDistributionAmount(goodsOut.getDistributionUnitPrice().multiply(item.getDistributionQuantity()));
            //当前库存数量
            item.setWrhInvQty(goodsOut.getStockQuantity());
            //创建者和更新者
            item.setCreator(distributionOrder.getCreator());
            item.setUpdater(distributionOrder.getUpdater());
            //创建时间
            item.setCreateTime(LocalDateTime.now());
            //更新时间
            item.setUpdateTime(LocalDateTime.now());
            // 查询门店资产
//            Long assetsId;
//            if (DistributionIdentificationEnum.PRESALE_DISTRIBUTION.getCode().equals(distributionOrder.getDistributionIdentification())) {
//                OrdDisPresaleAssets presaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(item.getStoreCode());
//                assetsId = presaleAssets.getId();
//            } else {
//                assetsId = null;
//            }
            OrdDisPresaleAssets presaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(item.getStoreCode());
            if (Objects.nonNull(presaleAssets)) {
                boolean isExcess = ordDisPresaleAssetsDetailService.checkGoodsIsExcess(presaleAssets.getStoreCode(), goodsOut.getGoodsCode(), item.getDistributionQuantity());
                if (!isExcess) {
                    throw new BusinessException("门店" + item.getStoreCode() + "商品" + goodsOut.getGoodsCode() + "分货数大于预售商品余数");
                }
            }
        });
        //批量保存明细
        this.batchSave(ordDisOrderDistributionDetails);
        //如果为0则不更新分货单明细
        if (NumberUtil.INTEGER_ZERO.equals(isUpdateOrder)) {
            return;
        }
        //更新出货单总金额和分货数量
        this.updateOrder(distributionOrder.getId());
    }

    @Override
    public int countByDistributionOrderId(Long distributionOrderId) {
        OrdDisOrderDistributionDetail detail = new OrdDisOrderDistributionDetail();
        detail.setDistributionOrderId(distributionOrderId);
        detail.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderDistributionDetailMapper.selectCount(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDetailById(Long id, OrdDisOrderDistribution ordDisOrderDistribution) {
        List<OrdDisOrderDistributionDetail> existDetailList = this.getDetailByOrdDistributionOrderId(ordDisOrderDistribution.getId());
        AtomicReference<BigDecimal> distributionTotalAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> distributionTotalQuantity = new AtomicReference<>(BigDecimal.ZERO);
        existDetailList.forEach(detail -> {
            if (detail.getId().equals(id)) {
                return;
            }
            distributionTotalAmount.getAndSet(distributionTotalAmount.get().add(detail.getDistributionAmount()));
            distributionTotalQuantity.getAndSet(distributionTotalQuantity.get().add(detail.getDistributionQuantity()));
        });
        ordDisOrderDistribution.setDistributionTotalAmount(distributionTotalAmount.get());
        ordDisOrderDistribution.setDistributionTotalQuantity(distributionTotalQuantity.get());
        ordDisOrderDistribution.setUpdater(UserUtil.getUserName());
        ordDisOrderDistribution.setUpdateTime(LocalDateTime.now());
        ordDisOrderDistributionDetailMapper.deleteByPrimaryKey(id);
        ordDisOrderDistributionMapper.updateByPrimaryKeySelective(ordDisOrderDistribution);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveDetail(List<OrdDisOrderDistributionDetail> details) {
        ordDisOrderDistributionDetailMapper.batchSave(details);
    }


    @Override
    public DisDistributionCheckOut handleCheckBeforeSaveDistributionDetail(Map<String, OrdDisDistributionImportResultOut> importResultMap,
                                                                           OrdDisOrderDistribution ordDisOrderDistribution, String loginUsername, String distributionIdentification) {
        List<OrdDisDistributionImportErrorResult> errorResultList = Lists.newArrayList();
        List<OrdDisOrderDistributionDetail> saveDetailList = Lists.newArrayList();
        List<OrdDisOrderDistributionDetail> updateDetailList = Lists.newArrayList();
        List<OrdDisOrderDistributionDetail> deleteDetailList = Lists.newArrayList();
        // 获取已存在明细
        List<OrdDisOrderDistributionDetail> existDetailList = this.getDetailByOrdDistributionOrderId(ordDisOrderDistribution.getId());
        Map<String, List<OrdDisOrderDistributionDetail>> storeDetilsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(existDetailList)) {
            storeDetilsMap = existDetailList.stream().collect(Collectors.groupingBy(OrdDisOrderDistributionDetail::getStoreCode));
        }
        ordDisOrderDistribution.setUpdater(loginUsername);
        List<String> storeCodeList = Lists.newArrayList();
        Map<String, List<OrdDisOrderDistributionDetail>> finalStoreDetilsMap = storeDetilsMap;
        importResultMap.entrySet().forEach(entry -> {
            String storeCode = entry.getKey();
            //校验门店与业务状态开关
            StoreAndStatusSwitchOut storeAndStatusSwitch = storeCenterService.getStoreAndStatusSwitch(storeCode, ordDisOrderDistribution.getBizOrgCode());
            if (Objects.isNull(storeAndStatusSwitch)) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店不存在或不可用"));
                return;
            }
            StoreStatusBusinessSwitch storeStatusBusinessSwitch = storeAndStatusSwitch.getStoreStatusBusinessSwitch();
            if (Objects.isNull(storeStatusBusinessSwitch)) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店业务状态未配置"));
                return;
            }
            if (ModelConst.DELETE.NO.equals(storeStatusBusinessSwitch.getIsAllotDisSc())) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店不允许分货"));
                return;
            }
            //校验门店属性
            if (!StoreConstant.StoreProperty.FRANCHISE.getMytValue().equals(storeAndStatusSwitch.getStoreProperty())) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "直营门店，不可在配销订货管理分货"));
                return;
            }
            //查询允许分货商品信息
            OrdDisDistributionImportResultOut ordDisDistributionImportResultOut = entry.getValue();
            // 待处理所有商品
            List<OrdDisDistributionImportGoodsOut> importGoodsOutList = ordDisDistributionImportResultOut.getImportGoodsOutList();
            Map<String, OrdDisOrderDistributionDetail> storeExistDetailMap = new HashMap<>();
            List<OrdDisOrderDistributionDetail> storeDetailList = finalStoreDetilsMap.get(storeCode);
            if (CollectionUtils.isNotEmpty(storeDetailList)) {
                storeExistDetailMap = storeDetailList.stream().collect(Collectors.toMap(OrdDisOrderDistributionDetail::getGoodsCode, Function.identity()));
            }
            // 处理导入商品
            List<OrderGoodsOut> goodsOuts = this.processingImportGoodsData(ordDisOrderDistribution, importGoodsOutList, deleteDetailList,
                    storeExistDetailMap, errorResultList, storeAndStatusSwitch, saveDetailList, updateDetailList, existDetailList, distributionIdentification);
            if (CollectionUtils.isEmpty(goodsOuts)) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店商品均不可用"));
                return;
            }
            storeCodeList.add(storeCode);
        });
        AtomicReference<BigDecimal> distributionTotalAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> distributionTotalQuantity = new AtomicReference<>(BigDecimal.ZERO);
        existDetailList.forEach(detail -> {
            distributionTotalAmount.getAndSet(distributionTotalAmount.get().add(detail.getDistributionAmount()));
            distributionTotalQuantity.getAndSet(distributionTotalQuantity.get().add(detail.getDistributionQuantity()));
        });
        ordDisOrderDistribution.setDistributionTotalAmount(distributionTotalAmount.get());
        ordDisOrderDistribution.setDistributionTotalQuantity(distributionTotalQuantity.get());
        ordDisOrderDistribution.setUpdateTime(LocalDateTime.now());
        ordDisOrderDistribution.setDistributionIdentification(distributionIdentification);

        DisDistributionCheckOut disDistributionCheckOut = new DisDistributionCheckOut();
        disDistributionCheckOut.setStoreCodeList(storeCodeList);
        disDistributionCheckOut.setSaveDetailList(saveDetailList);
        disDistributionCheckOut.setUpdateDetailList(updateDetailList);
        disDistributionCheckOut.setDeleteDetailList(deleteDetailList);
        disDistributionCheckOut.setUpdateDirOrderDistribution(ordDisOrderDistribution);
        disDistributionCheckOut.setErrorResultList(errorResultList);
        disDistributionCheckOut.setDistributionOrderId(ordDisOrderDistribution.getId());
        return disDistributionCheckOut;
    }

    private List<OrderGoodsOut> processingImportGoodsData(OrdDisOrderDistribution ordDirOrderDistribution, List<OrdDisDistributionImportGoodsOut> importGoodsOutList,
                                                          List<OrdDisOrderDistributionDetail> deleteDetailList, Map<String, OrdDisOrderDistributionDetail> storeExistDetailMap,
                                                          List<OrdDisDistributionImportErrorResult> errorResultList, StoreAndStatusSwitchOut storeAndStatusSwitchOut,
                                                          List<OrdDisOrderDistributionDetail> saveDetailList, List<OrdDisOrderDistributionDetail> updateDetailList,
                                                          List<OrdDisOrderDistributionDetail> existDetailList, String distributionIdentification) {
        // 过滤出分货数量大于0的商品代码
        List<String> goodsCodeList = importGoodsOutList.stream()
                .filter(importGoodsOut -> Objects.nonNull(importGoodsOut.getGoodsCode()) && importGoodsOut.getDistributionQuantity().compareTo(BigDecimal.ZERO) > NumberUtil.INTEGER_ZERO)
                .map(OrdDisDistributionImportGoodsOut::getGoodsCode).collect(Collectors.toList());
        List<OrderGoodsOut> goodsOuts = null;
        Map<String, OrderGoodsOut> goodsMap = new HashMap<>();
        // 不能判断isEmpty就返回，下面有逻辑封装待处理的数据
        if (CollectionUtils.isNotEmpty(goodsCodeList)) {
            OrderGoodsIn goodsIn = new OrderGoodsIn();
            goodsIn.setStoreCode(storeAndStatusSwitchOut.getStoreCode());
            goodsIn.setBizOrgCode(ordDirOrderDistribution.getBizOrgCode());
            goodsIn.setGoodsCodeList(goodsCodeList);
            //查询允许分货商品信息
            Response<List<OrderGoodsOut>> goodsInfoResponse = orderGoodsServer.findGoodsInfoForDistributionImport(goodsIn);
//            if (!goodsInfoResponse.isSuccess() || CollectionUtils.isEmpty(goodsInfoResponse.getData())) {
//                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeOut.getStoreCode(), SystemConstant.SHORT_LINE, null, goodsInfoResponse.getMessage()));
//            } else {
//                goodsOuts = goodsInfoResponse.getData();
//                if (CollectionUtils.isNotEmpty(goodsOuts)) {
//                    goodsMap = goodsOuts.stream().collect(Collectors.toMap(OrderGoodsOut::getGoodsCode, Function.identity()));
//                }
//            }
            if (goodsInfoResponse.isSuccess() && CollectionUtils.isNotEmpty(goodsInfoResponse.getData())) {
                goodsOuts = goodsInfoResponse.getData();
                goodsMap = goodsOuts.stream().collect(Collectors.toMap(OrderGoodsOut::getGoodsCode, Function.identity()));
            }
        }
        // 查询门店资产
        OrdDisPresaleAssets presaleAssets;
        if (DistributionIdentificationEnum.PRESALE_DISTRIBUTION.getCode().equals(distributionIdentification)) {
            presaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(storeAndStatusSwitchOut.getStoreCode());
        } else {
            presaleAssets = null;
        }
        Map<String, OrderGoodsOut> finalGoodsMap = goodsMap;
        importGoodsOutList.forEach(ordDisDistributionImportGoodsOut -> {
            // 如果分货数量不大于0
            if (ordDisDistributionImportGoodsOut.getDistributionQuantity().compareTo(BigDecimal.ZERO) <= NumberUtil.INTEGER_ZERO) {
                if (Objects.isNull(ordDisDistributionImportGoodsOut.getDetailId())) {
                    return;
                }
                OrdDisOrderDistributionDetail deleteDetail = new OrdDisOrderDistributionDetail();
                deleteDetail.setId(ordDisDistributionImportGoodsOut.getDetailId());
                deleteDetailList.add(deleteDetail);
                return;
            }
            OrderGoodsOut goodsOut = finalGoodsMap.get(ordDisDistributionImportGoodsOut.getGoodsCode());
            // 错误信息
            StringJoiner errorJoiner = this.checkImportDistributionGoods(goodsOut, ordDisDistributionImportGoodsOut.getDetailId(),
                    storeExistDetailMap, presaleAssets, ordDisDistributionImportGoodsOut.getDistributionQuantity());
            if (errorJoiner.length() > 0) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeAndStatusSwitchOut.getStoreCode(), ordDisDistributionImportGoodsOut.getGoodsCode(),
                        ordDisDistributionImportGoodsOut.getDistributionQuantity(), errorJoiner.toString()));
            } else {
                OrdDisOrderDistributionDetail detail;
                if (Objects.isNull(ordDisDistributionImportGoodsOut.getDetailId())) {
                    // 封装分货单明细
                    detail = this.initOrdDisOrderDistributionDetail(ordDirOrderDistribution.getId(), ordDirOrderDistribution.getUpdater(),
                            goodsOut, ordDisDistributionImportGoodsOut.getDistributionQuantity(), storeAndStatusSwitchOut);
                    saveDetailList.add(detail);
                    existDetailList.add(detail);
                } else {
                    detail = ordDisOrderDistributionDetailMapper.selectByPrimaryKey(ordDisDistributionImportGoodsOut.getDetailId());
                    detail.setDistributionQuantity(ordDisDistributionImportGoodsOut.getDistributionQuantity());
                    detail.setPackingNumber(detail.getDistributionQuantity().divide(new BigDecimal(goodsOut.getDistributionSpecification().getQpc()), 0, RoundingMode.UP));
                    detail.setDistributionAmount(detail.getOriginalPrice().multiply(ordDisDistributionImportGoodsOut.getDistributionQuantity()));
                    detail.setUpdater(ordDirOrderDistribution.getUpdater());
                    detail.setUpdateTime(LocalDateTime.now());
                    updateDetailList.add(detail);
                    existDetailList.forEach(oldDetail -> {
                        if (!ordDisDistributionImportGoodsOut.getDetailId().equals(oldDetail.getId())) {
                            return;
                        }
                        Collections.replaceAll(existDetailList, oldDetail, detail);
                    });
                }
            }
        });
        return goodsOuts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveImportDistributionDetail(DisDistributionCheckOut disDistributionCheckOut) {
        OrdDisOrderDistribution updateDirOrderDistribution = disDistributionCheckOut.getUpdateDirOrderDistribution();
        if (OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(updateDirOrderDistribution.getDistributionOrderStatus())) {
            disDistributionCheckOut.getStoreCodeList().forEach(storeCode -> ordDisOrderDistributionResultService.saveNotExistOne(updateDirOrderDistribution, storeCode));
        }
        List<OrdDisOrderDistributionDetail> saveDetailList = disDistributionCheckOut.getSaveDetailList();
        if (CollectionUtils.isNotEmpty(saveDetailList)) {
            ordDisOrderDistributionDetailMapper.batchSave(saveDetailList);
        }
        List<OrdDisOrderDistributionDetail> updateDistributionQuantityDetailList = disDistributionCheckOut.getUpdateDetailList();
        if (CollectionUtils.isNotEmpty(updateDistributionQuantityDetailList)) {
            ordDisOrderDistributionDetailMapper.batchUpdateDistributionQuantity(updateDistributionQuantityDetailList);
        }
        List<OrdDisOrderDistributionDetail> deleteDistributionQuantityDetailList = disDistributionCheckOut.getDeleteDetailList();
        if (CollectionUtils.isNotEmpty(deleteDistributionQuantityDetailList)) {
            ordDisOrderDistributionDetailMapper.batchDelete(deleteDistributionQuantityDetailList);
        }
        ordDisOrderDistributionMapper.updateByPrimaryKeySelective(updateDirOrderDistribution);
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIS_DISTRIBUTION_ORDER_DETAIL_UPDATE.getName(),
                String.valueOf(updateDirOrderDistribution.getId()),
                OrdLogTypeEnum.DIS_DISTRIBUTION_ORDER_DETAIL_UPDATE.getCode(),
                OrdLogTypeEnum.DIS_DISTRIBUTION_ORDER_DETAIL_UPDATE.getName(),
                new Date(), updateDirOrderDistribution.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }


    private OrdDisOrderDistributionDetail initOrdDisOrderDistributionDetail(Long distributionOrderId, String loginUsername, OrderGoodsOut goodsOut,
                                                                            BigDecimal distributionQuantity, StoreAndStatusSwitchOut storeAndStatusSwitchOut) {
        OrdDisOrderDistributionDetail detail = new OrdDisOrderDistributionDetail();
        detail.setDistributionOrderId(distributionOrderId);
        detail.setDistributionQuantity(distributionQuantity);
        // 分货三维表导入有商品代码为空情况
        if (Objects.nonNull(goodsOut)) {
            //添加包装数
            detail.setPackingNumber(distributionQuantity.divide(new BigDecimal(goodsOut.getDistributionSpecification().getQpc()), 0, RoundingMode.UP));
            //商品代码
            detail.setGoodsCode(goodsOut.getGoodsCode());
            //门店代码
            detail.setStoreCode(storeAndStatusSwitchOut.getStoreCode());
            //分货金额
            detail.setDistributionAmount(goodsOut.getDistributionUnitPrice().multiply(distributionQuantity));
            //商品名称
            detail.setGoodsName(goodsOut.getGoodsName());
            //配货单价
            detail.setOriginalPrice(goodsOut.getDistributionUnitPrice());
            //包装规格
            detail.setDistributionSpecification(goodsOut.getDistributionSpecification().getQpcStr());
            //当前库存数量
            detail.setWrhInvQty(goodsOut.getStockQuantity());
        }
        //门店名称
        detail.setStoreName(storeAndStatusSwitchOut.getStoreName());
        detail.setStoreArea(storeAndStatusSwitchOut.getBelongArea());
        detail.setCreator(loginUsername);
        detail.setCreateTime(LocalDateTime.now());
        detail.setUpdater(loginUsername);
        detail.setUpdateTime(LocalDateTime.now());
        detail.setIsDelete(ModelConst.DELETE.NO);
        return detail;
    }

    @Override
    public StringJoiner checkImportDistributionGoods(OrderGoodsOut goodsOut, Long detailId, Map<String, OrdDisOrderDistributionDetail> existDetailMap,
                                                     OrdDisPresaleAssets presaleAssets, BigDecimal distributionQuantity) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        if (Objects.isNull(goodsOut)) {
            errorJoiner.add("该商品不存在");
            return errorJoiner;
        }
        if (Objects.isNull(detailId) && existDetailMap.containsKey(goodsOut.getGoodsCode())) {
            errorJoiner.add("分货单已存在该商品");
        }
        //门店商品上下架
        if (NumberUtil.INTEGER_ONE.equals(goodsOut.getIsShelves())) {
            errorJoiner.add("商品已下架");
        }
        //商品开关信息
        GoodsStatusBusinessSwitch goodsStatusBusinessSwitch = goodsOut.getGoodsStatusBusinessSwitch();
        if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsAllotDis())) {
            errorJoiner.add("不可被分货");
        }
        //校验规格信息是否存在
        StandardSpecTransInfoOut distributionSpecification = goodsOut.getDistributionSpecification();
        if (Objects.isNull(distributionSpecification)) {
            errorJoiner.add("配货规格不存在");
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
        //校验仓位是否存在
//                if (StringUtils.isBlank(goodsOut.getStockCode())) {
//                    errorJoiner.add("仓位为空");
//                }
        //校验配送价
        if (Objects.isNull(goodsOut.getDistributionUnitPrice())) {
            errorJoiner.add("配送价为空");
        }
        if (Objects.nonNull(presaleAssets)) {
            boolean isExcess = ordDisPresaleAssetsDetailService.checkGoodsIsExcess(presaleAssets.getStoreCode(), goodsOut.getGoodsCode(), distributionQuantity);
            if (!isExcess) {
                errorJoiner.add("分货数大于预售商品余数");
            }
        }
        return errorJoiner;
    }

    @Override
    public OrdDisDistributionImportErrorResult initOrdDisDistributionImportErrorResult(String storeCode, String goodsCode,
                                                                                       BigDecimal distributionQuantity, String errorMessage) {
        OrdDisDistributionImportErrorResult ordDisDistributionImportErrorResult = new OrdDisDistributionImportErrorResult();
        ordDisDistributionImportErrorResult.setStoreCode(storeCode);
        ordDisDistributionImportErrorResult.setGoodsCode(goodsCode);
        ordDisDistributionImportErrorResult.setDistributionQuantity(distributionQuantity);
        ordDisDistributionImportErrorResult.setErrorMessage(errorMessage);
        return ordDisDistributionImportErrorResult;
    }

    @Override
    public StringJoiner checkAuditDistributionGoods(OrderGoodsOut goodsOut, LocalDateTime effectiveTime, Boolean isAssetsFlag, Map<String, OrdDisPresaleAssetsDetail> storePresaleAssetsMap, BigDecimal distributionQuantity) {
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
        if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsAllotDis())) {
            errorJoiner.add("不可被分货");
        }
        //校验规格信息是否存在
        StandardSpecTransInfoOut distributionSpecification = goodsOut.getDistributionSpecification();
        if (Objects.isNull(distributionSpecification)) {
            errorJoiner.add("配货规格不存在");
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
        FlashSaleCheckOut flashSaleCheckOut = shoppingCartService.isCanBuyByDistributionOrder(goodsOut.getGoodsCode(), goodsOut.getBizOrgCode(), effectiveTime);
        if (Objects.nonNull(flashSaleCheckOut) && NumberUtil.INTEGER_ZERO.equals(flashSaleCheckOut.getIsCanBuyFlashSale())) {
            errorJoiner.add("当前时间不可订货");
        }
        // 如果本单是预售分货单
        if (isAssetsFlag) {
            OrdDisPresaleAssetsDetail ordDisPresaleAssetsDetail = storePresaleAssetsMap.get(goodsOut.getGoodsCode());
            if (Objects.isNull(ordDisPresaleAssetsDetail)) {
                errorJoiner.add("预售资产订货中不存在");
            } else {
                // 分货数量大于可订货单剩余量
                if (distributionQuantity.compareTo(ordDisPresaleAssetsDetail.getSurplusQuantity()) == NumberUtil.INTEGER_ONE) {
                    errorJoiner.add("分货数量不可大于资产剩余数量");
                }
            }
        }
        return errorJoiner;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateDistributionQuantity(List<OrdDisOrderDistributionDetail> detailList) {
        ordDisOrderDistributionDetailMapper.batchUpdateDistributionQuantity(detailList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAfterAuditSuccess(List<OrdDisOrderDistributionDetail> updateDetailList, OrdDisOrderDistribution ordDisOrderDistribution) {
        if (NumberUtil.INTEGER_ONE.equals(ordDisOrderDistribution.getIsEffectiveImmediately())) {
            OrdDisOrderDistribution taskDistribution = new OrdDisOrderDistribution();
            taskDistribution.setId(ordDisOrderDistribution.getId());
            taskDistribution.setUpdater(ordDisOrderDistribution.getUpdater());
            taskDistribution.setDistributionOrderNo(ordDisOrderDistribution.getDistributionOrderNo());
            String taskData = JSONObject.toJSONString(taskDistribution);
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_DISTRIBUTION_CREATE_ORDER, taskData, ordDisOrderDistribution.getBizOrgCode(), ordDisOrderDistribution.getDistributionOrderNo());
            SendResponse sendResponse = disDistributionCreateOrderSender.sendSync(taskData.getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
            log.info("配销分货单生成订货单{}消息ID---{}", ordDisOrderDistribution.getDistributionOrderNo(), sendResponse.getMessageId());
        }
        if (CollectionUtils.isNotEmpty(updateDetailList)) {
            ordDisOrderDistributionDetailMapper.batchUpdateDistributionQuantity(updateDetailList);
        }
        ordDisOrderDistributionMapper.updateByPrimaryKeySelective(ordDisOrderDistribution);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDisOrderDistribution.getId()), OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                DistributionOrderLogEnum.DISTRIBUTION_ASYNC_AUDIT.getKey(), new Date(), SystemConstant.SYSTEM_USER);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public Response<DisDistributionCheckOut> checkForSaveDetail(SaveDisDistributionDetailIn saveDisDistributionDetailIn, String loginUsername, String distributionIdentification) {
        OrdDisOrderDistribution orderDistribution = ordDisOrderDistributionMapper.selectByPrimaryKey(saveDisDistributionDetailIn.getDistributionOrderId());
        if (Objects.isNull(orderDistribution)) {
            return Response.error("不存在的分货单");
        }
        if (NumberUtil.INTEGER_ONE.equals(saveDisDistributionDetailIn.getRequestSource())
                && OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(orderDistribution.getDistributionOrderStatus())) {
            log.info("分货单{}状态不正确", orderDistribution.getDistributionOrderNo());
            return Response.error("分货单状态不正确");
        }
        Map<String, OrdDisDistributionImportResultOut> importResultMap = new HashMap<>();
        saveDisDistributionDetailIn.getDetailList().forEach(detail -> {
            OrdDisDistributionImportResultOut ordDisDistributionImportResultOut = importResultMap.get(detail.getStoreCode());
            if (Objects.isNull(ordDisDistributionImportResultOut)) {
                ordDisDistributionImportResultOut = new OrdDisDistributionImportResultOut();
                ordDisDistributionImportResultOut.setStoreCode(detail.getStoreCode());
                ordDisDistributionImportResultOut.setImportGoodsOutList(new ArrayList<>());
            }
            OrdDisDistributionImportGoodsOut ordDisDistributionImportGoodsOut = new OrdDisDistributionImportGoodsOut();
            ordDisDistributionImportGoodsOut.setGoodsCode(detail.getGoodsCode());
            ordDisDistributionImportGoodsOut.setDistributionQuantity(Objects.isNull(detail.getDistributionQuantity()) ? BigDecimal.ZERO : detail.getDistributionQuantity());
            ordDisDistributionImportGoodsOut.setDetailId(detail.getId());
            ordDisDistributionImportResultOut.getImportGoodsOutList().add(ordDisDistributionImportGoodsOut);
            importResultMap.put(detail.getStoreCode(), ordDisDistributionImportResultOut);
        });
        orderDistribution.setIsEffectiveImmediately(saveDisDistributionDetailIn.getIsEffectiveImmediately());
        if (NumberUtil.INTEGER_ONE.equals(saveDisDistributionDetailIn.getIsEffectiveImmediately())) {
            orderDistribution.setEffectiveTime(LocalDateTime.now());
        } else {
            orderDistribution.setEffectiveTime(saveDisDistributionDetailIn.getEffectiveTime());
        }
        // 校验商品
        DisDistributionCheckOut disDistributionCheckOut = this.handleCheckBeforeSaveDistributionDetail(importResultMap, orderDistribution, loginUsername, distributionIdentification);
        return Response.data(disDistributionCheckOut);
    }

    @Override
    public List<OrdDisDistributionImportErrorResult> checkDetailByAudit(List<OrdDisOrderDistributionDetail> detailList, String bizOrgCode, LocalDateTime effectiveTime, String distributionIdentification) {
        List<OrdDisDistributionImportErrorResult> errorResultList = Lists.newArrayList();
        Map<String, List<OrdDisOrderDistributionDetail>> storeDetailMap = detailList.stream().collect(Collectors.groupingBy(OrdDisOrderDistributionDetail::getStoreCode));
        Map<String, OrderGoodsOut> storeGoodsMap = new HashMap<>();
//        Map<String, StoreAndStatusSwitchOut> storeMap = new HashMap<>();
        storeDetailMap.entrySet().forEach(entry -> {
            String storeCode = entry.getKey();
            //校验门店与业务状态开关
            StoreAndStatusSwitchOut storeAndStatusSwitch = storeCenterService.getStoreAndStatusSwitch(storeCode, bizOrgCode);
            if (Objects.isNull(storeAndStatusSwitch)) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店不存在或不可用"));
                return;
            }
            StoreStatusBusinessSwitch storeStatusBusinessSwitch = storeAndStatusSwitch.getStoreStatusBusinessSwitch();
            if (Objects.isNull(storeStatusBusinessSwitch)) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店业务状态未配置"));
                return;
            }
            if (ModelConst.DELETE.NO.equals(storeStatusBusinessSwitch.getIsAllotDisSc())) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店不允许分货"));
                return;
            }
            //校验门店是否是配货
            if (!StoreConstant.StoreProperty.FRANCHISE.getMytValue().equals(storeAndStatusSwitch.getStoreProperty())) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode,
                        SystemConstant.SHORT_LINE, null, "直营门店，不可在配销订货管理分货"));
                return;
            }
            // 如果本单是预售分货单
            List<OrdDisPresaleAssetsDetail> storeGoodsAssetsDetailList = null;
            boolean isCheckAssetsFlag = DistributionIdentificationEnum.PRESALE_DISTRIBUTION.getCode().equals(distributionIdentification);
            if (isCheckAssetsFlag) {
                // 查询门店资产
                storeGoodsAssetsDetailList = ordDisPresaleAssetsDetailService.findStoreGoodsAssetsDetailList(storeCode);
                if (CollectionUtils.isEmpty(storeGoodsAssetsDetailList)) {
                    errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode,
                            SystemConstant.SHORT_LINE, null, "预售资产无可订货商品"));
                    return;
                }
            }
            List<String> goodsCodeList = entry.getValue().stream().map(OrdDisOrderDistributionDetail::getGoodsCode).collect(Collectors.toList());
            OrderGoodsIn goodsIn = new OrderGoodsIn();
            goodsIn.setStoreCode(storeCode);
            goodsIn.setBizOrgCode(bizOrgCode);
            goodsIn.setGoodsCodeList(goodsCodeList);
            //查询允许分货商品信息
//            List<OrderGoodsOut> goodsOuts = Lists.newArrayList();
            Response<List<OrderGoodsOut>> goodsInfoResponse = orderGoodsServer.findGoodsInfoForDistributionImport(goodsIn);
            if (!goodsInfoResponse.isSuccess() || CollectionUtils.isEmpty(goodsInfoResponse.getData())) {
                errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeAndStatusSwitch.getStoreCode(), SystemConstant.SHORT_LINE, null, goodsInfoResponse.getMessage()));
            } else {
                if (CollectionUtils.isEmpty(goodsInfoResponse.getData())) {
                    errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode,
                            SystemConstant.SHORT_LINE, null, "门店商品均不可用"));
                    return;
                }
            }
            Map<String, OrderGoodsOut> checkGoodsMap = goodsInfoResponse.getData().stream().collect(Collectors.toMap(OrderGoodsOut::getGoodsCode, Function.identity()));
            // 预售资产明细转MAP
            Map<String, OrdDisPresaleAssetsDetail> storePresaleAssetsMap;
            if (isCheckAssetsFlag) {
                storePresaleAssetsMap = storeGoodsAssetsDetailList.stream().collect(Collectors.toMap(OrdDisPresaleAssetsDetail::getGoodsCode, Function.identity()));
            } else {
                storePresaleAssetsMap = null;
            }
            entry.getValue().forEach(detail -> {
                OrderGoodsOut goodsOut = checkGoodsMap.get(detail.getGoodsCode());
                // 校验与封装错误信息
                StringJoiner errorJoiner = this.checkAuditDistributionGoods(goodsOut, effectiveTime, isCheckAssetsFlag, storePresaleAssetsMap, detail.getDistributionQuantity());
                if (errorJoiner.length() > 0) {
                    errorResultList.add(this.initOrdDisDistributionImportErrorResult(storeCode, detail.getGoodsCode(),
                            detail.getDistributionQuantity(), errorJoiner.toString()));
                } else {
                    storeGoodsMap.put(storeCode + SystemConstant.SHORT_LINE + goodsOut.getGoodsCode(), goodsOut);
                }
            });
//            storeMap.put(storeCode, storeAndStatusSwitch);
        });
        return errorResultList;
    }


    /**
     * 批量保存出货单明细
     *
     * @param details 出货单明细集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdDisOrderDistributionDetail> details) {
        int pages = details.size() % SystemConstant.PAGE_SIZE == 0 ? details.size() / SystemConstant.PAGE_SIZE : details.size() / SystemConstant.PAGE_SIZE + 1;
        for (int i = 0; i < pages; i++) {
            //批量保存
            ordDisOrderDistributionDetailMapper.batchSave(details.subList(i * SystemConstant.PAGE_SIZE, i == pages - 1 ? details.size() : (i + 1) * SystemConstant.PAGE_SIZE));
        }
    }

    /**
     * 更新分货单
     *
     * @param ordDistributionOrderId 分货单id
     */
    public void updateOrder(Long ordDistributionOrderId) {
        //删除成功后 更新当前分货单数据
        List<OrdDisOrderDistributionDetail> dbDetails = this.getDetailByOrdDistributionOrderId(ordDistributionOrderId);
        //分货总数量
        BigDecimal distributionTotalQuantity = dbDetails.stream().map(OrdDisOrderDistributionDetail::getDistributionQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        //分货总金额
        BigDecimal distributionTotalAmount = dbDetails.stream().map(OrdDisOrderDistributionDetail::getDistributionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //更新
        OrdDisOrderDistribution distribution = new OrdDisOrderDistribution();
        distribution.setDistributionTotalQuantity(distributionTotalQuantity);
        distribution.setDistributionTotalAmount(distributionTotalAmount);
        distribution.setId(ordDistributionOrderId);
        //更新时间
        distribution.setUpdateTime(LocalDateTime.now());
        ordDisOrderDistributionMapper.updateByPrimaryKeySelective(distribution);
    }


    /**
     * 校验明细
     *
     * @param ordDisOrderDistributionDetail 明细
     * @return
     */
    public OrderGoodsOut checkDetail(OrdDisOrderDistributionDetail ordDisOrderDistributionDetail) {
        //查询门店id
        StoreOut storeInfo = storeCenterService.getStoreInfoByErpStoreCode(ordDisOrderDistributionDetail.getStoreCode());
        if (Objects.isNull(storeInfo)) {
            throw new BusinessException("门店" + ordDisOrderDistributionDetail.getStoreCode() + "不存在");
        }
        //查询商品配送价
        OrderGoodsIn goodsIn = new OrderGoodsIn();
        goodsIn.setStoreCode(storeInfo.getStoreCode());
        goodsIn.setBizOrgCode(UserUtil.getBizOrgCode());
        goodsIn.setGoodsCode(ordDisOrderDistributionDetail.getGoodsCode());
        goodsIn.setStoreProperty(storeInfo.getStoreType());
        OrderGoodsOut goodsOut = orderGoodsServer.getSwitchGoodsInfo(goodsIn);
        // 商品校验
        if (Objects.isNull(goodsOut)) {
            throw new BusinessException("商品" + ordDisOrderDistributionDetail.getGoodsCode() + "不存在");
        }
        if (!NumberUtil.INTEGER_ONE.equals(goodsOut.getGoodsStatusBusinessSwitch().getIsAllotDis())) {
            throw new BusinessException("商品" + ordDisOrderDistributionDetail.getGoodsCode() + "不可被分货");
        }

        Integer qpc = goodsOut.getDistributionSpecification().getQpc();
        if (Objects.isNull(qpc)) {
            throw new BusinessException("商品" + ordDisOrderDistributionDetail.getGoodsCode() + "配货规格数量不合法");
        }

        return goodsOut;
    }

    /**
     * 查询配销分货门店商品关联表
     *
     * @param id
     * @return
     */
    public OrdDisOrderDistributionDetail getDetail(Long id) {
        return ordDisOrderDistributionDetailMapper.selectByPrimaryKey(id);
    }

    @Override
    public String export(Long distributionOrderId, String goodsCode, String storeCode) {
        OrdDisOrderDistributionDetail query = new OrdDisOrderDistributionDetail();
        query.setId(distributionOrderId);
        query.setStoreCode(storeCode);
        query.setIsDelete(ModelConst.DELETE.NO);
        query.setGoodsCode(goodsCode);
        List<OrdDisOrderDistributionDetail> ordDisOrderDistributionDetails = ordDisOrderDistributionDetailMapper.select(query);
        List<ExportOrdDisOrderDistributionDetail> exportList = new ArrayList<>();
        ordDisOrderDistributionDetails.forEach(item -> {
            ExportOrdDisOrderDistributionDetail exportOrdDisOrderDistributionDetail = new ExportOrdDisOrderDistributionDetail();
            BeanUtils.copyProperties(item, exportOrdDisOrderDistributionDetail);
            exportList.add(exportOrdDisOrderDistributionDetail);
        });
        byte[] bytes = FileExportUtil.getFileBytesByData(exportList,
                "品牌信息",
                "品牌信息",
                ExportOrdDisOrderDistributionDetail.class,
                true);
        return fileService.uploadFile("导出配销单分货明细" + ".xlsx", bytes, OrdSystemConstant.SYSTEM_CODE, OrdSystemConstant.SYSTEM_NAME);
    }

}
