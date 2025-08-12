package com.edc.erp.directly.distribution.service.impl;

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
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirordercart.service.ShoppingCartService;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDistributionDetailMapper;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDistributionMapper;
import com.edc.erp.directly.distribution.model.excel.OrdDirDistributionImportErrorResult;
import com.edc.erp.directly.distribution.model.in.DeleteStoreGoodsIn;
import com.edc.erp.directly.distribution.model.in.QueryOrderDistributionDetailIn;
import com.edc.erp.directly.distribution.model.in.SaveDirDistributionDetailIn;
import com.edc.erp.directly.distribution.model.out.DirDistributionCheckOut;
import com.edc.erp.directly.distribution.model.out.OrdDirDistributionImportGoodsOut;
import com.edc.erp.directly.distribution.model.out.OrdDirDistributionImportResultOut;
import com.edc.erp.directly.distribution.model.out.OrdDirOrderDistributionDetailOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionDetailService;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionResultService;
import com.edc.erp.directly.enumeration.DistributionOrderLogEnum;
import com.edc.erp.directly.enumeration.OrderDistributionOrderStatusEnum;
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
 * 直营分货门店商品关联表(OrdDirOrderDistributionDetail)表服务实现类
 *
 * @author lixuejun
 * @since 2022-09-26 11:51:59
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDirOrderDistributionDetailServiceImpl extends BaseServiceImpl<OrdDirOrderDistributionDetail> implements OrdDirOrderDistributionDetailService {

    private final OrdDirOrderDistributionDetailMapper ordDirOrderDistributionDetailMapper;

    private final AsyncLogService asyncLogService;

    private final OrderGoodsServer orderGoodsServer;

    private final StoreCenterService storeCenterService;

    private final OrdDirOrderDistributionMapper ordDirOrderDistributionMapper;

    private final OrdDirOrderDistributionResultService ordDirOrderDistributionResultService;

    private final ShoppingCartService shoppingCartService;

    private final AsyncPushTaskService asyncPushTaskService;

    @Qualifier("dirDistributionCreateOrderSender")
    private final MessageSender dirDistributionCreateOrderSender;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrdDirOrderDistributionDetail(OrdDirOrderDistributionDetail ordDirOrderDistributionDetail) {
        //查询明细是否存在
        OrdDirOrderDistributionDetail detail = this.getDetail(ordDirOrderDistributionDetail.getId());
        if (Objects.isNull(detail)) {
            throw new BusinessException("直营分货门店商品不存在");
        }
        //校验商品
        OrderGoodsOut goodsOut = this.checkDetail(ordDirOrderDistributionDetail);
        //更新配送价
        ordDirOrderDistributionDetail.setOriginalPrice(goodsOut.getDistributionUnitPrice());
        //更新配送规格
        ordDirOrderDistributionDetail.setDistributionSpecification(goodsOut.getDistributionSpecification().getQpcStr());
        //分货金额
        ordDirOrderDistributionDetail.setDistributionAmount(goodsOut.getDistributionUnitPrice().multiply(ordDirOrderDistributionDetail.getDistributionQuantity()));
        //当前库存数量
        ordDirOrderDistributionDetail.setWrhInvQty(goodsOut.getStockQuantity());
        //更新时间
        ordDirOrderDistributionDetail.setUpdateTime(LocalDateTime.now());
        //更新明细
        int updateCount = ordDirOrderDistributionDetailMapper.updateByPrimaryKeySelective(ordDirOrderDistributionDetail);
        if (!NumberUtil.INTEGER_ZERO.equals(updateCount)) {
            //更新出货单
            this.updateOrder(ordDirOrderDistributionDetail.getDistributionOrderId());
        }
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDirOrderDistributionDetail.getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_DETAIL_UPDATE.getName(), new Date(), ordDirOrderDistributionDetail.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return updateCount;
    }

    /**
     * 删除直营分货门店商品
     *
     * @param deleteStoreGoodsIn 删除门店商品关联入参类
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOrdDirOrderDistributionDetail(DeleteStoreGoodsIn deleteStoreGoodsIn) {
        //查询明细是否存在
        OrdDirOrderDistributionDetail detail = this.getDetail(deleteStoreGoodsIn.getStoreGoodsId());
        if (Objects.isNull(detail)) {
            throw new BusinessException("直营分货门店商品不存在");
        }
        //删除直营分货门店商品关联表
        OrdDirOrderDistributionDetail query = new OrdDirOrderDistributionDetail();
        query.setIsDelete(ModelConst.DELETE.YES);
        query.setId(detail.getId());
        //更新
        int updateCount = ordDirOrderDistributionDetailMapper.updateByPrimaryKeySelective(query);
        if (!NumberUtil.INTEGER_ZERO.equals(updateCount)) {
            this.updateOrder(deleteStoreGoodsIn.getDistributionOrderId());
        }
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                String.valueOf(detail.getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_DETAIL_DELETE.getName(), new Date(), detail.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return updateCount;
    }

    @Override
    public List<OrdDirOrderDistributionDetail> findOrdDirOrderDistributions(Long distributionOrderId, String goodsCode, String storeCode) {
        OrdDirOrderDistributionDetail query = new OrdDirOrderDistributionDetail();
        query.setId(distributionOrderId);
        query.setStoreCode(storeCode);
        query.setIsDelete(ModelConst.DELETE.NO);
        query.setGoodsCode(goodsCode);
        return ordDirOrderDistributionDetailMapper.select(query);
    }

    @Override
    public OrdDirOrderDistributionDetail getOrdDirOrderDistribution(Long distributionOrderId, String goodsCode, String storeCode) {
        OrdDirOrderDistributionDetail query = new OrdDirOrderDistributionDetail();
        query.setDistributionOrderId(distributionOrderId);
        query.setStoreCode(storeCode);
        query.setIsDelete(ModelConst.DELETE.NO);
        query.setGoodsCode(goodsCode);
        return ordDirOrderDistributionDetailMapper.selectOne(query);
    }


    /**
     * 统计分货单总数量
     *
     * @param distributionOrderId 直营分货单主键
     * @return
     */
    @Override
    public BigDecimal sumTotalDistributionQuantity(Long distributionOrderId) {
        return ordDirOrderDistributionDetailMapper.sumTotalDistributionQuantity(distributionOrderId);
    }

    /**
     * 分页查询直营分货门店商品
     *
     * @param queryOrderDistributionDetailIn 直营分货门店商品入参类
     * @return
     */
    @Override
    public Page<OrdDirOrderDistributionDetailOut> findByPage(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn) {
        List<OrdDirOrderDistributionDetailOut> detailOuts = ordDirOrderDistributionDetailMapper.findByPage(queryOrderDistributionDetailIn);
        Page<OrdDirOrderDistributionDetailOut> detailPage = new Page<>(queryOrderDistributionDetailIn);
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
    public List<OrdDirOrderDistributionDetail> getDetailByOrdDistributionOrderId(Long ordDistributionOrderId) {
        OrdDirOrderDistributionDetail ordDirOrderDistributionDetail = new OrdDirOrderDistributionDetail();
        ordDirOrderDistributionDetail.setDistributionOrderId(ordDistributionOrderId);
        ordDirOrderDistributionDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDirOrderDistributionDetailMapper.select(ordDirOrderDistributionDetail);
    }

    /**
     * 保存出货单明细
     *
     * @param distributionOrder              出货单
     * @param ordDirOrderDistributionDetails 出货单明细
     * @param isUpdateOrder                  是否更新分货单 0否 1是
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OrdDirOrderDistribution distributionOrder, List<OrdDirOrderDistributionDetail> ordDirOrderDistributionDetails, int isUpdateOrder) {
        //先删除明细
        OrdDirOrderDistributionDetail ordDirOrderDistributionDetail = new OrdDirOrderDistributionDetail();
        ordDirOrderDistributionDetail.setDistributionOrderId(distributionOrder.getId());
        ordDirOrderDistributionDetailMapper.delete(ordDirOrderDistributionDetail);
        ordDirOrderDistributionDetails.forEach(item -> {
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
        });
        //批量保存明细
        this.batchSave(ordDirOrderDistributionDetails);
        //如果为0则不更新分货单明细
        if (NumberUtil.INTEGER_ZERO.equals(isUpdateOrder)) {
            return;
        }
        //更新出货单总金额和分货数量
        this.updateOrder(distributionOrder.getId());
    }

    /**
     * 获取分货明细
     *
     * @param ordDistributionOrderId 直营分货单
     * @param goodsCode              商品code
     * @param storeCode              门店代码
     * @return
     */
    @Override
    public OrdDirOrderDistributionDetail getOrdDisOrderDistribution(Long ordDistributionOrderId, String goodsCode, String storeCode) {
        OrdDirOrderDistributionDetail query = new OrdDirOrderDistributionDetail();
        query.setDistributionOrderId(ordDistributionOrderId);
        query.setStoreCode(storeCode);
        query.setIsDelete(ModelConst.DELETE.NO);
        query.setGoodsCode(goodsCode);
        return ordDirOrderDistributionDetailMapper.selectOne(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveDetail(List<OrdDirOrderDistributionDetail> details) {
        ordDirOrderDistributionDetailMapper.batchSave(details);
    }


    @Override
    public DirDistributionCheckOut handleCheckBeforeSaveDistributionDetail(Map<String, OrdDirDistributionImportResultOut> importResultMap,
                                                                           OrdDirOrderDistribution ordDirOrderDistribution, String loginUsername) {
        List<OrdDirDistributionImportErrorResult> errorResultList = Lists.newArrayList();
        List<OrdDirOrderDistributionDetail> saveDetailList = Lists.newArrayList();
        List<OrdDirOrderDistributionDetail> updateDetailList = Lists.newArrayList();
        List<OrdDirOrderDistributionDetail> deleteDetailList = Lists.newArrayList();
        // 获取已存在明细
        List<OrdDirOrderDistributionDetail> existDetailList = this.getDetailByOrdDistributionOrderId(ordDirOrderDistribution.getId());
        Map<String, List<OrdDirOrderDistributionDetail>> storeDetilsMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(existDetailList)) {
            storeDetilsMap = existDetailList.stream().collect(Collectors.groupingBy(OrdDirOrderDistributionDetail::getStoreCode));
        }
        ordDirOrderDistribution.setUpdater(loginUsername);
        List<String> storeCodeList = Lists.newArrayList();
        Map<String, List<OrdDirOrderDistributionDetail>> finalStoreDetilsMap = storeDetilsMap;
        importResultMap.entrySet().forEach(entry -> {
            String storeCode = entry.getKey();
            //校验门店与业务状态
            StoreAndStatusSwitchOut storeAndStatusSwitch = storeCenterService.getStoreAndStatusSwitch(storeCode, ordDirOrderDistribution.getBizOrgCode());
            if (Objects.isNull(storeAndStatusSwitch)) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店不存在或不可用"));
                return;
            }
            StoreStatusBusinessSwitch storeStatusBusinessSwitch = storeAndStatusSwitch.getStoreStatusBusinessSwitch();
            if (Objects.isNull(storeStatusBusinessSwitch)) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店业务状态未配置"));
                return;
            }
            if (ModelConst.DELETE.NO.equals(storeStatusBusinessSwitch.getIsAllotSc())) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店不允许分货"));
                return;
            }
            //校验门店属性
            if (!StoreConstant.StoreProperty.DIRECTLY.getMytValue().equals(storeAndStatusSwitch.getStoreProperty())) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "加盟门店，不可在直营订货管理分货"));
                return;
            }
            //查询允许分货商品信息
            OrdDirDistributionImportResultOut ordDirDistributionImportResultOut = entry.getValue();
            // 待处理所有商品
            List<OrdDirDistributionImportGoodsOut> importGoodsOutList = ordDirDistributionImportResultOut.getImportGoodsOutList();
            Map<String, OrdDirOrderDistributionDetail> storeExistDetailMap = new HashMap<>();
            List<OrdDirOrderDistributionDetail> storeDetailList = finalStoreDetilsMap.get(storeCode);
            if (CollectionUtils.isNotEmpty(storeDetailList)) {
                storeExistDetailMap = storeDetailList.stream().collect(Collectors.toMap(OrdDirOrderDistributionDetail::getGoodsCode, Function.identity()));
            }
            // 处理导入商品
            List<OrderGoodsOut> goodsOuts = this.processingImportGoodsData(ordDirOrderDistribution, importGoodsOutList, deleteDetailList, storeExistDetailMap,
                    errorResultList, storeAndStatusSwitch, saveDetailList, updateDetailList, existDetailList);
            if (CollectionUtils.isEmpty(goodsOuts)) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店商品均不可用"));
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
        ordDirOrderDistribution.setDistributionTotalAmount(distributionTotalAmount.get());
        ordDirOrderDistribution.setDistributionTotalQuantity(distributionTotalQuantity.get());
        ordDirOrderDistribution.setUpdateTime(LocalDateTime.now());

        DirDistributionCheckOut dirDistributionCheckOut = new DirDistributionCheckOut();
        dirDistributionCheckOut.setStoreCodeList(storeCodeList);
        dirDistributionCheckOut.setSaveDetailList(saveDetailList);
        dirDistributionCheckOut.setUpdateDetailList(updateDetailList);
        dirDistributionCheckOut.setDeleteDetailList(deleteDetailList);
        dirDistributionCheckOut.setUpdateDirOrderDistribution(ordDirOrderDistribution);
        dirDistributionCheckOut.setErrorResultList(errorResultList);
        dirDistributionCheckOut.setDistributionOrderId(ordDirOrderDistribution.getId());
        return dirDistributionCheckOut;
    }

    private List<OrderGoodsOut> processingImportGoodsData(OrdDirOrderDistribution ordDirOrderDistribution, List<OrdDirDistributionImportGoodsOut> importGoodsOutList,
                                                          List<OrdDirOrderDistributionDetail> deleteDistributionQuantityDetailList, Map<String, OrdDirOrderDistributionDetail> storeExistDetailMap,
                                                          List<OrdDirDistributionImportErrorResult> errorResultList, StoreAndStatusSwitchOut storeAndStatusSwitchOut,
                                                          List<OrdDirOrderDistributionDetail> saveDetailList, List<OrdDirOrderDistributionDetail> updateDetailList,
                                                          List<OrdDirOrderDistributionDetail> existDetailList) {
        // 过滤出分货数量大于0的商品代码
        List<String> goodsCodeList = importGoodsOutList.stream()
                .filter(importGoodsOut -> importGoodsOut.getDistributionQuantity().compareTo(BigDecimal.ZERO) > NumberUtil.INTEGER_ZERO)
                .map(OrdDirDistributionImportGoodsOut::getGoodsCode).collect(Collectors.toList());
        List<OrderGoodsOut> goodsOuts = null;
        Map<String, OrderGoodsOut> goodsMap = new HashMap<>();
        // 不能判断isEmpty就返回，下面有逻辑封装待处理的数据
        if (CollectionUtils.isNotEmpty(goodsCodeList)) {
            OrderGoodsIn goodsIn = new OrderGoodsIn();
            goodsIn.setStoreCode(storeAndStatusSwitchOut.getStoreCode());
            goodsIn.setBizOrgCode(ordDirOrderDistribution.getBizOrgCode());
            goodsIn.setGoodsCodeList(goodsCodeList);
            //查询允许分货商品信息
//            goodsOuts = orderGoodsServer.findGoodsInfoByStoreAndGoodsCodes(goodsIn);
//            if (CollectionUtils.isNotEmpty(goodsOuts)) {
//                goodsMap = goodsOuts.stream().collect(Collectors.toMap(OrderGoodsOut::getGoodsCode, Function.identity()));
//            }
            Response<List<OrderGoodsOut>> goodsInfoResponse = orderGoodsServer.findGoodsInfoForDistributionImport(goodsIn);
//            if (!goodsInfoResponse.isSuccess() || CollectionUtils.isEmpty(goodsInfoResponse.getData())) {
//                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeOut.getStoreCode(), SystemConstant.SHORT_LINE, null, goodsInfoResponse.getMessage()));
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
        Map<String, OrderGoodsOut> finalGoodsMap = goodsMap;
        importGoodsOutList.forEach(ordDirDistributionImportGoodsOut -> {
            // 如果分货数量不大于0
            if (ordDirDistributionImportGoodsOut.getDistributionQuantity().compareTo(BigDecimal.ZERO) <= NumberUtil.INTEGER_ZERO) {
                if (Objects.isNull(ordDirDistributionImportGoodsOut.getDetailId())) {
                    return;
                }
                OrdDirOrderDistributionDetail deleteDetail = new OrdDirOrderDistributionDetail();
                deleteDetail.setId(ordDirDistributionImportGoodsOut.getDetailId());
                deleteDistributionQuantityDetailList.add(deleteDetail);
                existDetailList.removeIf(ordDirOrderDistributionDetail -> ordDirDistributionImportGoodsOut.getDetailId().equals(ordDirOrderDistributionDetail.getId()));
                return;
            }
            OrderGoodsOut goodsOut = finalGoodsMap.get(ordDirDistributionImportGoodsOut.getGoodsCode());
            // 错误信息
            StringJoiner errorJoiner = this.checkImportDistributionGoods(goodsOut, ordDirDistributionImportGoodsOut.getDetailId(), storeExistDetailMap);
            if (errorJoiner.length() > 0) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeAndStatusSwitchOut.getStoreCode(), ordDirDistributionImportGoodsOut.getGoodsCode(),
                        ordDirDistributionImportGoodsOut.getDistributionQuantity(), errorJoiner.toString()));
            } else {
                OrdDirOrderDistributionDetail detail;
                if (Objects.isNull(ordDirDistributionImportGoodsOut.getDetailId())) {
                    // 封装分货单明细
                    detail = this.initOrdDirOrderDistributionDetail(ordDirOrderDistribution.getId(), ordDirOrderDistribution.getUpdater(),
                            goodsOut, ordDirDistributionImportGoodsOut.getDistributionQuantity(), storeAndStatusSwitchOut);
                    saveDetailList.add(detail);
                    existDetailList.add(detail);
                } else {
                    detail = ordDirOrderDistributionDetailMapper.selectByPrimaryKey(ordDirDistributionImportGoodsOut.getDetailId());
                    detail.setDistributionQuantity(ordDirDistributionImportGoodsOut.getDistributionQuantity());
                    detail.setPackingNumber(detail.getDistributionQuantity().divide(new BigDecimal(goodsOut.getDistributionSpecification().getQpc()), 0, RoundingMode.UP));
                    detail.setDistributionAmount(detail.getOriginalPrice().multiply(ordDirDistributionImportGoodsOut.getDistributionQuantity()));
                    detail.setUpdater(ordDirOrderDistribution.getUpdater());
                    detail.setUpdateTime(LocalDateTime.now());
                    updateDetailList.add(detail);
                    existDetailList.forEach(oldDetail -> {
                        if (!ordDirDistributionImportGoodsOut.getDetailId().equals(oldDetail.getId())) {
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
    public void saveImportDistributionDetail(DirDistributionCheckOut dirDistributionCheckOut) {
        OrdDirOrderDistribution updateDirOrderDistribution = dirDistributionCheckOut.getUpdateDirOrderDistribution();
        if (OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(updateDirOrderDistribution.getDistributionOrderStatus())) {
            dirDistributionCheckOut.getStoreCodeList().forEach(storeCode -> ordDirOrderDistributionResultService.saveNotExistOne(updateDirOrderDistribution, storeCode));
        }
        List<OrdDirOrderDistributionDetail> saveDetailList = dirDistributionCheckOut.getSaveDetailList();
        if (CollectionUtils.isNotEmpty(saveDetailList)) {
            ordDirOrderDistributionDetailMapper.batchSave(saveDetailList);
        }
        List<OrdDirOrderDistributionDetail> updateDistributionQuantityDetailList = dirDistributionCheckOut.getUpdateDetailList();
        if (CollectionUtils.isNotEmpty(updateDistributionQuantityDetailList)) {
            ordDirOrderDistributionDetailMapper.batchUpdateDistributionQuantity(updateDistributionQuantityDetailList);
        }
        List<OrdDirOrderDistributionDetail> deleteDistributionQuantityDetailList = dirDistributionCheckOut.getDeleteDetailList();
        if (CollectionUtils.isNotEmpty(deleteDistributionQuantityDetailList)) {
            ordDirOrderDistributionDetailMapper.batchDelete(deleteDistributionQuantityDetailList);
        }
        ordDirOrderDistributionMapper.updateByPrimaryKeySelective(updateDirOrderDistribution);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIR_DISTRIBUTION_ORDER_DETAIL_UPDATE.getName(),
                String.valueOf(updateDirOrderDistribution.getId()),
                OrdLogTypeEnum.DIR_DISTRIBUTION_ORDER_DETAIL_UPDATE.getCode(),
                OrdLogTypeEnum.DIR_DISTRIBUTION_ORDER_DETAIL_UPDATE.getName(),
                new Date(), updateDirOrderDistribution.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    private OrdDirOrderDistributionDetail initOrdDirOrderDistributionDetail(Long distributionOrderId, String loginUsername, OrderGoodsOut goodsOut,
                                                                            BigDecimal distributionQuantity, StoreAndStatusSwitchOut storeAndStatusSwitchOut) {
        OrdDirOrderDistributionDetail detail = new OrdDirOrderDistributionDetail();
        detail.setDistributionOrderId(distributionOrderId);
        detail.setDistributionQuantity(distributionQuantity);
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
    public StringJoiner checkImportDistributionGoods(OrderGoodsOut goodsOut, Long detailId, Map<String, OrdDirOrderDistributionDetail> existDetailMap) {
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
        if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsAllot())) {
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
        return errorJoiner;
    }

    @Override
    public OrdDirDistributionImportErrorResult initOrdDirDistributionImportErrorResult(String storeCode, String goodsCode,
                                                                                       BigDecimal distributionQuantity, String errorMessage) {
        OrdDirDistributionImportErrorResult ordDirDistributionImportErrorResult = new OrdDirDistributionImportErrorResult();
        ordDirDistributionImportErrorResult.setStoreCode(storeCode);
        ordDirDistributionImportErrorResult.setGoodsCode(goodsCode);
        ordDirDistributionImportErrorResult.setDistributionQuantity(distributionQuantity);
        ordDirDistributionImportErrorResult.setErrorMessage(errorMessage);
        return ordDirDistributionImportErrorResult;
    }

    @Override
    public StringJoiner checkAuditDistributionGoods(OrderGoodsOut goodsOut, LocalDateTime targetDateTime) {
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
        if (Objects.isNull(goodsStatusBusinessSwitch) || NumberUtil.INTEGER_ZERO.equals(goodsStatusBusinessSwitch.getIsAllot())) {
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
        FlashSaleCheckOut flashSaleCheckOut = shoppingCartService.isCanBuyByDistributionOrder(goodsOut.getGoodsCode(), goodsOut.getBizOrgCode(), targetDateTime);
        if (Objects.nonNull(flashSaleCheckOut) && NumberUtil.INTEGER_ZERO.equals(flashSaleCheckOut.getIsCanBuyFlashSale())) {
            errorJoiner.add("当前时间不可订货");
        }
        return errorJoiner;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateDistributionQuantity(List<OrdDirOrderDistributionDetail> detailList) {
        ordDirOrderDistributionDetailMapper.batchUpdateDistributionQuantity(detailList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAfterAuditSuccess(List<OrdDirOrderDistributionDetail> updateDetailList, OrdDirOrderDistribution ordDirOrderDistribution) {
        if (NumberUtil.INTEGER_ONE.equals(ordDirOrderDistribution.getIsEffectiveImmediately())) {
            OrdDirOrderDistribution taskDistribution = new OrdDirOrderDistribution();
            taskDistribution.setId(ordDirOrderDistribution.getId());
            taskDistribution.setUpdater(ordDirOrderDistribution.getUpdater());
            taskDistribution.setDistributionOrderNo(ordDirOrderDistribution.getDistributionOrderNo());
            String taskData = JSONObject.toJSONString(taskDistribution);
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_DISTRIBUTION_CREATE_ORDER, taskData, ordDirOrderDistribution.getBizOrgCode(), ordDirOrderDistribution.getDistributionOrderNo());
            SendResponse sendResponse = dirDistributionCreateOrderSender.sendSync(taskData.getBytes(), System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_TIME);
            log.info("直营分货单生成订货单{}消息ID---{}", ordDirOrderDistribution.getDistributionOrderNo(), sendResponse.getMessageId());
        }
        if (CollectionUtils.isNotEmpty(updateDetailList)) {
            ordDirOrderDistributionDetailMapper.batchUpdateDistributionQuantity(updateDetailList);
        }
        ordDirOrderDistributionMapper.updateByPrimaryKeySelective(ordDirOrderDistribution);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDirOrderDistribution.getId()), OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                DistributionOrderLogEnum.DISTRIBUTION_ASYNC_AUDIT.getKey(), new Date(), SystemConstant.SYSTEM_USER);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public Response<DirDistributionCheckOut> checkForSaveDetail(SaveDirDistributionDetailIn saveDirDistributionDetailIn, String loginUsername) {
        OrdDirOrderDistribution orderDistribution = ordDirOrderDistributionMapper.selectByPrimaryKey(saveDirDistributionDetailIn.getDistributionOrderId());
        if (Objects.isNull(orderDistribution)) {
            return Response.error("不存在的分货单");
        }
        if (NumberUtil.INTEGER_ONE.equals(saveDirDistributionDetailIn.getRequestSource())
                && OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(orderDistribution.getDistributionOrderStatus())) {
            log.info("分货单{}状态不正确", orderDistribution.getDistributionOrderNo());
            return Response.error("分货单状态不正确");
        }
        Map<String, OrdDirDistributionImportResultOut> importResultMap = new HashMap<>();
        saveDirDistributionDetailIn.getDetailList().forEach(detail -> {
            OrdDirDistributionImportResultOut ordDirDistributionImportResultOut = importResultMap.get(detail.getStoreCode());
            if (Objects.isNull(ordDirDistributionImportResultOut)) {
                ordDirDistributionImportResultOut = new OrdDirDistributionImportResultOut();
                ordDirDistributionImportResultOut.setStoreCode(detail.getStoreCode());
                ordDirDistributionImportResultOut.setImportGoodsOutList(new ArrayList<>());
            }
            OrdDirDistributionImportGoodsOut ordDirDistributionImportGoodsOut = new OrdDirDistributionImportGoodsOut();
            ordDirDistributionImportGoodsOut.setGoodsCode(detail.getGoodsCode());
            ordDirDistributionImportGoodsOut.setDistributionQuantity(Objects.isNull(detail.getDistributionQuantity()) ? BigDecimal.ZERO : detail.getDistributionQuantity());
            ordDirDistributionImportGoodsOut.setDetailId(detail.getId());
            ordDirDistributionImportResultOut.getImportGoodsOutList().add(ordDirDistributionImportGoodsOut);
            importResultMap.put(detail.getStoreCode(), ordDirDistributionImportResultOut);
        });
        orderDistribution.setIsEffectiveImmediately(saveDirDistributionDetailIn.getIsEffectiveImmediately());
        if (NumberUtil.INTEGER_ONE.equals(saveDirDistributionDetailIn.getIsEffectiveImmediately())) {
            orderDistribution.setEffectiveTime(LocalDateTime.now());
        } else {
            orderDistribution.setEffectiveTime(saveDirDistributionDetailIn.getEffectiveTime());
        }
        // 校验商品
        DirDistributionCheckOut dirDistributionCheckOut = this.handleCheckBeforeSaveDistributionDetail(importResultMap, orderDistribution, loginUsername);
        return Response.data(dirDistributionCheckOut);
    }

    @Override
    public List<OrdDirDistributionImportErrorResult> checkDetailByAudit(List<OrdDirOrderDistributionDetail> detailList, String bizOrgCode, LocalDateTime effectiveTime) {
        List<OrdDirDistributionImportErrorResult> errorResultList = Lists.newArrayList();
        Map<String, List<OrdDirOrderDistributionDetail>> storeDetailMap = detailList.stream().collect(Collectors.groupingBy(OrdDirOrderDistributionDetail::getStoreCode));
        Map<String, OrderGoodsOut> storeGoodsMap = new HashMap<>();
//        Map<String, StoreAndStatusSwitchOut> storeMap = new HashMap<>();
        storeDetailMap.entrySet().forEach(entry -> {
            String storeCode = entry.getKey();
            //校验门店与业务状态开关
            StoreAndStatusSwitchOut storeAndStatusSwitch = storeCenterService.getStoreAndStatusSwitch(storeCode, bizOrgCode);
            if (Objects.isNull(storeAndStatusSwitch)) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店不存在或不可用"));
                return;
            }
            StoreStatusBusinessSwitch storeStatusBusinessSwitch = storeAndStatusSwitch.getStoreStatusBusinessSwitch();
            if (Objects.isNull(storeStatusBusinessSwitch)) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店业务状态未配置"));
                return;
            }
            if (ModelConst.DELETE.NO.equals(storeStatusBusinessSwitch.getIsAllotSc())) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode, SystemConstant.SHORT_LINE, null, "门店不允许分货"));
                return;
            }
            //校验门店是否是配货
            if (!StoreConstant.StoreProperty.DIRECTLY.getMytValue().equals(storeAndStatusSwitch.getStoreProperty())) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode,
                        SystemConstant.SHORT_LINE, null, "加盟门店，不可在直营订货管理分货"));
                return;
            }
            List<String> goodsCodeList = entry.getValue().stream().map(OrdDirOrderDistributionDetail::getGoodsCode).collect(Collectors.toList());
            OrderGoodsIn goodsIn = new OrderGoodsIn();
            goodsIn.setStoreCode(storeCode);
            goodsIn.setBizOrgCode(bizOrgCode);
            goodsIn.setGoodsCodeList(goodsCodeList);
            //查询允许分货商品信息
//            List<OrderGoodsOut> goodsOuts = Lists.newArrayList();
            Response<List<OrderGoodsOut>> goodsInfoResponse = orderGoodsServer.findGoodsInfoForDistributionImport(goodsIn);
            if (!goodsInfoResponse.isSuccess() || CollectionUtils.isEmpty(goodsInfoResponse.getData())) {
                errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeAndStatusSwitch.getStoreCode(), SystemConstant.SHORT_LINE, null, goodsInfoResponse.getMessage()));
            } else {
                if (CollectionUtils.isEmpty(goodsInfoResponse.getData())) {
                    errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode,
                            SystemConstant.SHORT_LINE, null, "门店商品均不可用"));
                    return;
                }
            }
            Map<String, OrderGoodsOut> checkGoodsMap = goodsInfoResponse.getData().stream().collect(Collectors.toMap(OrderGoodsOut::getGoodsCode, Function.identity()));
            entry.getValue().forEach(detail -> {
                OrderGoodsOut goodsOut = checkGoodsMap.get(detail.getGoodsCode());
                // 校验与封装错误信息
                StringJoiner errorJoiner = this.checkAuditDistributionGoods(goodsOut, effectiveTime);
                if (errorJoiner.length() > 0) {
                    errorResultList.add(this.initOrdDirDistributionImportErrorResult(storeCode, detail.getGoodsCode(),
                            detail.getDistributionQuantity(), errorJoiner.toString()));
                } else {
                    storeGoodsMap.put(storeCode + SystemConstant.SHORT_LINE + goodsOut.getGoodsCode(), goodsOut);
                }
            });
//            storeMap.put(storeCode, storeAndStatusSwitch);
        });
        return errorResultList;
    }

    @Override
    public int countByDistributionOrderId(Long distributionOrderId) {
        OrdDirOrderDistributionDetail detail = new OrdDirOrderDistributionDetail();
        detail.setDistributionOrderId(distributionOrderId);
        detail.setIsDelete(ModelConst.DELETE.NO);
        return ordDirOrderDistributionDetailMapper.selectCount(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDetailById(Long id, OrdDirOrderDistribution ordDirOrderDistribution) {
        List<OrdDirOrderDistributionDetail> existDetailList = this.getDetailByOrdDistributionOrderId(ordDirOrderDistribution.getId());
        AtomicReference<BigDecimal> distributionTotalAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> distributionTotalQuantity = new AtomicReference<>(BigDecimal.ZERO);
        existDetailList.forEach(detail -> {
            if (detail.getId().equals(id)) {
                return;
            }
            distributionTotalAmount.getAndSet(distributionTotalAmount.get().add(detail.getDistributionAmount()));
            distributionTotalQuantity.getAndSet(distributionTotalQuantity.get().add(detail.getDistributionQuantity()));
        });
        ordDirOrderDistribution.setDistributionTotalAmount(distributionTotalAmount.get());
        ordDirOrderDistribution.setDistributionTotalQuantity(distributionTotalQuantity.get());
        ordDirOrderDistribution.setUpdater(UserUtil.getUserName());
        ordDirOrderDistribution.setUpdateTime(LocalDateTime.now());
        ordDirOrderDistributionDetailMapper.deleteByPrimaryKey(id);
        ordDirOrderDistributionMapper.updateByPrimaryKeySelective(ordDirOrderDistribution);
    }


    /**
     * 批量保存出货单明细
     *
     * @param details 出货单明细集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdDirOrderDistributionDetail> details) {
        int pages = details.size() % SystemConstant.PAGE_SIZE == 0 ? details.size() / SystemConstant.PAGE_SIZE : details.size() / SystemConstant.PAGE_SIZE + 1;
        for (int i = 0; i < pages; i++) {
            //批量保存
            ordDirOrderDistributionDetailMapper.batchSave(details.subList(i * SystemConstant.PAGE_SIZE, i == pages - 1 ? details.size() : (i + 1) * SystemConstant.PAGE_SIZE));
        }
    }

    /**
     * 更新分货单
     *
     * @param ordDistributionOrderId 分货单id
     */
    public void updateOrder(Long ordDistributionOrderId) {
        //删除成功后 更新当前分货单数据
        List<OrdDirOrderDistributionDetail> dbDetails = this.getDetailByOrdDistributionOrderId(ordDistributionOrderId);
        //分货总数量
        BigDecimal distributionTotalQuantity = dbDetails.stream().map(OrdDirOrderDistributionDetail::getDistributionQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        //分货总金额
        BigDecimal distributionTotalAmount = dbDetails.stream().map(OrdDirOrderDistributionDetail::getDistributionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //更新
        OrdDirOrderDistribution distribution = new OrdDirOrderDistribution();
        distribution.setDistributionTotalQuantity(distributionTotalQuantity);
        distribution.setDistributionTotalAmount(distributionTotalAmount);
        distribution.setId(ordDistributionOrderId);
        //更新时间
        distribution.setUpdateTime(LocalDateTime.now());
        ordDirOrderDistributionMapper.updateByPrimaryKeySelective(distribution);
    }

    /**
     * 校验明细
     *
     * @param ordDirOrderDistributionDetail 明细
     * @return
     */
    public OrderGoodsOut checkDetail(OrdDirOrderDistributionDetail ordDirOrderDistributionDetail) {
        //查询门店id
        StoreOut storeInfo = storeCenterService.getStoreInfoByErpStoreCode(ordDirOrderDistributionDetail.getStoreCode());
        if (Objects.isNull(storeInfo)) {
            throw new BusinessException("门店" + ordDirOrderDistributionDetail.getStoreCode() + "不存在");
        }
        //查询商品配送价
        OrderGoodsIn goodsIn = new OrderGoodsIn();
        goodsIn.setStoreCode(storeInfo.getStoreCode());
        goodsIn.setBizOrgCode(UserUtil.getBizOrgCode());
        goodsIn.setGoodsCode(ordDirOrderDistributionDetail.getGoodsCode());
        goodsIn.setStoreProperty(storeInfo.getStoreType());
        OrderGoodsOut goodsOut = orderGoodsServer.getSwitchGoodsInfo(goodsIn);
        // 商品校验
        if (Objects.isNull(goodsOut)) {
            throw new BusinessException("商品" + ordDirOrderDistributionDetail.getGoodsCode() + "不存在");
        }
        if (!NumberUtil.INTEGER_ONE.equals(goodsOut.getGoodsStatusBusinessSwitch().getIsAllotDis())) {
            throw new BusinessException("商品" + ordDirOrderDistributionDetail.getGoodsCode() + "不可被分货");
        }

        Integer qpc = goodsOut.getDistributionSpecification().getQpc();
        if (Objects.isNull(qpc)) {
            throw new BusinessException("商品" + ordDirOrderDistributionDetail.getGoodsCode() + "配货规格数量不合法");
        }

        return goodsOut;
    }

    /**
     * 查询直营分货门店商品关联表
     *
     * @param id
     * @return
     */
    public OrdDirOrderDistributionDetail getDetail(Long id) {
        return ordDirOrderDistributionDetailMapper.selectByPrimaryKey(id);
    }
}
