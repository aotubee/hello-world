package com.edc.erp.presale.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.goods.OrdQueryGoodsIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.store.QueryBizOrgCodeStoreIn;
import com.edc.erp.common.model.out.GoodsDisSpecOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.presale.entity.OrdDisPresaleActivity;
import com.edc.erp.presale.entity.OrdDisPresaleActivityGoods;
import com.edc.erp.presale.entity.OrdDisPresaleActivityStore;
import com.edc.erp.presale.enumeration.OrdDisPresaleActivityStatusEnum;
import com.edc.erp.presale.listener.PresaleActivityStoreImportListener;
import com.edc.erp.presale.mapper.OrdDisPresaleActivityMapper;
import com.edc.erp.presale.model.excel.ImportPresaleActivityStore;
import com.edc.erp.presale.model.in.CheckExistPresaleActivityIn;
import com.edc.erp.presale.model.in.ExtendOrderDateIn;
import com.edc.erp.presale.model.in.PresaleActivityListPageIn;
import com.edc.erp.presale.model.in.SaveDisPresaleActivityIn;
import com.edc.erp.presale.model.out.*;
import com.edc.erp.presale.service.OrdDisPresaleActivityGoodsService;
import com.edc.erp.presale.service.OrdDisPresaleActivityService;
import com.edc.erp.presale.service.OrdDisPresaleActivityStoreService;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName OrdDisPresaleActivityServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/21 15:12
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisPresaleActivityServiceImpl implements OrdDisPresaleActivityService {
    private final OrdDisPresaleActivityMapper ordDisPresaleActivityMapper;
    private final OrdDisPresaleActivityGoodsService ordDisPresaleActivityGoodsService;
    private final OrdDisPresaleActivityStoreService ordDisPresaleActivityStoreService;
    private final UniqueUtils uniqueUtils;
    private final AsyncLogService asyncLogService;
    private final OrderGoodsServer orderGoodsServer;
    private final StoreCenterService storeCenterService;
    private final FileService fileService;
    private final OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<OrdDisPresaleActivity> savePresaleActivity(SaveDisPresaleActivityIn saveDisPresaleActivityIn) {
        OrdDisPresaleActivity ordDisPresaleActivity;
        if (Objects.nonNull(saveDisPresaleActivityIn.getId())) {
            OrdDisPresaleActivity updateOrdDisPresaleActivity = new OrdDisPresaleActivity();
            BeanUtils.copy(saveDisPresaleActivityIn, updateOrdDisPresaleActivity);
            updateOrdDisPresaleActivity.setId(saveDisPresaleActivityIn.getId());
            ordDisPresaleActivityMapper.updateByPrimaryKeySelective(updateOrdDisPresaleActivity);
            ordDisPresaleActivity = ordDisPresaleActivityMapper.selectByPrimaryKey(saveDisPresaleActivityIn.getId());
        } else {
            ordDisPresaleActivity = new OrdDisPresaleActivity();
            BeanUtils.copy(saveDisPresaleActivityIn, ordDisPresaleActivity);
            String presaleActivityNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KYS.getCode(), saveDisPresaleActivityIn.getBizOrgCode(), uniqueUtils, 4);
            ordDisPresaleActivity.setPresaleActivityNo(presaleActivityNo);
            ordDisPresaleActivity.setStatus(OrdDisPresaleActivityStatusEnum.SUBMITTED.getKey());
            ordDisPresaleActivity.setTotalGoodsQty(saveDisPresaleActivityIn.getActivityGoodsList().size());
            ordDisPresaleActivity.setTotalStoreQty(saveDisPresaleActivityIn.getActivityStoreList().size());
            ordDisPresaleActivity.setCreator(saveDisPresaleActivityIn.getLoginUsername());
            ordDisPresaleActivity.setUpdater(saveDisPresaleActivityIn.getLoginUsername());
            ordDisPresaleActivity.setIsDelete(ModelConst.DELETE.NO);
            ordDisPresaleActivityMapper.insert(ordDisPresaleActivity);
        }
        Long activityId = ordDisPresaleActivity.getId();
        ordDisPresaleActivityGoodsService.deleteByActivityId(activityId);
        saveDisPresaleActivityIn.getActivityGoodsList().forEach(ordDisPresaleActivityGoods -> {
            ordDisPresaleActivityGoods.setPresaleActivityId(activityId);
            ordDisPresaleActivityGoods.setCreator(saveDisPresaleActivityIn.getLoginUsername());
            ordDisPresaleActivityGoods.setCreateTime(LocalDateTime.now());
            ordDisPresaleActivityGoods.setUpdater(saveDisPresaleActivityIn.getLoginUsername());
            ordDisPresaleActivityGoods.setIsDelete(ModelConst.DELETE.NO);
        });
        ordDisPresaleActivityGoodsService.batchSavePresaleActivityGoods(saveDisPresaleActivityIn.getActivityGoodsList());
        ordDisPresaleActivityStoreService.deleteByActivityId(activityId);
        saveDisPresaleActivityIn.getActivityStoreList().forEach(ordDisPresaleActivityStore -> {
            ordDisPresaleActivityStore.setPresaleActivityId(activityId);
            ordDisPresaleActivityStore.setCreator(saveDisPresaleActivityIn.getLoginUsername());
            ordDisPresaleActivityStore.setCreateTime(LocalDateTime.now());
            ordDisPresaleActivityStore.setUpdater(saveDisPresaleActivityIn.getLoginUsername());
            ordDisPresaleActivityStore.setIsDelete(ModelConst.DELETE.NO);
        });
        ordDisPresaleActivityStoreService.batchSavePresaleActivityStore(saveDisPresaleActivityIn.getActivityStoreList());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY_SAVE.getName(),
                String.valueOf(ordDisPresaleActivity.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY.getCode(),
                OperateLogTypeEnum.SAVE.getName(),
                new Date(), ordDisPresaleActivity.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.data(ordDisPresaleActivity);
    }

    @Override
    public void beforeCheckSavePresaleActivityData(SaveDisPresaleActivityIn saveDisPresaleActivityIn) {
        // 1.校验预售活动
        this.checkPresaleActivity(saveDisPresaleActivityIn);
        // 2.校验商品是否重复
        List<String> goodsCodeList = this.checkPresaleActivityGoods(saveDisPresaleActivityIn);
        // 3.校验门店是否重复
        List<String> storeCodeList = this.checkPresaleActivityStore(saveDisPresaleActivityIn);
        // 4.校验门店和商品存在的活动信息
//        Map<Integer, List<OrdDisPresaleActivityGoods>> baseAndGiftDetailsMap = saveDisPresaleActivityIn.getActivityGoodsList().stream().collect(Collectors.groupingBy(OrdDisPresaleActivityGoods::getIsGift));
//        baseAndGiftDetailsMap.forEach((isGift, ordDisPresaleActivityGoodsList) -> {
//            List<String> goodsCodeList = ordDisPresaleActivityGoodsList.stream().map(OrdDisPresaleActivityGoods::getGoodsCode).collect(Collectors.toList());
//
//        });
        this.checkStoreGoodsExistActivity(saveDisPresaleActivityIn, storeCodeList, goodsCodeList);
    }

    private void checkPresaleActivity(SaveDisPresaleActivityIn saveDisPresaleActivityIn) {
        if (Objects.nonNull(saveDisPresaleActivityIn.getId())) {
            OrdDisPresaleActivity ordDisPresaleActivity = ordDisPresaleActivityMapper.selectByPrimaryKey(saveDisPresaleActivityIn.getId());
            if (Objects.isNull(ordDisPresaleActivity)) {
                throw new BusinessException("预售活动不存在");
            }
            if (!OrdDisPresaleActivityStatusEnum.SUBMITTED.getKey().equals(ordDisPresaleActivity.getStatus())) {
                throw new BusinessException("预售活动状态不正确");
            }
        }
        LocalDateTime beginSaleDate = saveDisPresaleActivityIn.getBeginSaleDate();
        LocalDateTime endSaleDate = saveDisPresaleActivityIn.getEndSaleDate();
        if (beginSaleDate.isBefore(LocalDateTime.now()) || endSaleDate.isBefore(beginSaleDate)) {
            throw new BusinessException("预售活动开始时间必须大于当前时间，且预售活动结束时间必须大于开始时间");
        }
        LocalDateTime beginOrderDate = saveDisPresaleActivityIn.getBeginOrderDate();
        LocalDateTime endOrderDate = saveDisPresaleActivityIn.getEndOrderDate();
        if (endOrderDate.isBefore(beginOrderDate)) {
            throw new BusinessException("可订货结束时间必须大于可订货开始时间");
        }
//        if (beginOrderDate.isBefore(beginSaleDate) || endOrderDate.isAfter(endSaleDate)) {
//            throw new BusinessException("可订货开始时间必须在预售活动结束时间内");
//        }
        if (beginOrderDate.isBefore(beginSaleDate)) {
            throw new BusinessException("可订货开始时间必须在预售活动开始时间之后");
        }
    }

    private List<String> checkPresaleActivityGoods(SaveDisPresaleActivityIn saveDisPresaleActivityIn) {
        List<OrdDisPresaleActivityGoods> activityGoodsList = saveDisPresaleActivityIn.getActivityGoodsList();
        for (int i = NumberUtil.INTEGER_ZERO; i < NumberUtil.INTEGER_TWO; i++) {
            Integer isGift = i;
            String duplicateGoodsCodes = activityGoodsList.stream().filter(ordDisPresaleActivityGoods -> ordDisPresaleActivityGoods.getIsGift().equals(isGift))
                    .collect(Collectors.groupingBy(OrdDisPresaleActivityGoods::getGoodsCode)) // 按id分组
                    .entrySet().stream()
                    .filter(entry -> entry.getValue().size() > NumberUtil.INTEGER_ONE) // 过滤出有重复的组
                    .flatMap(entry -> entry.getValue().stream()) // 获取重复的Person对象
                    .map(OrdDisPresaleActivityGoods::getGoodsCode).collect(Collectors.joining(SystemConstant.COMMA));
            if (StringUtils.isNotBlank(duplicateGoodsCodes)) {
                throw new BusinessException(isGift.equals(NumberUtil.INTEGER_ZERO) ? "主商品" : "赠品" + duplicateGoodsCodes + "重复");
            }
        }
        List<String> goodsCodeList = activityGoodsList.stream().distinct().map(OrdDisPresaleActivityGoods::getGoodsCode).collect(Collectors.toList());
        // 校验存在组织商品中
        OrdQueryGoodsIn ordQueryGoodsIn = new OrdQueryGoodsIn();
        ordQueryGoodsIn.setGoodsCodes(goodsCodeList);
        ordQueryGoodsIn.setBizOrgCode(saveDisPresaleActivityIn.getBizOrgCode());
        List<GoodsDisSpecOut> goodsDisSpecOutList = orderGoodsServer.findGoodsDisSpecOutByOrg(ordQueryGoodsIn);
        if (CollectionUtils.isEmpty(goodsDisSpecOutList)) {
            throw new BusinessException("查询组织品为空");
        }
        Map<String, GoodsDisSpecOut> checkGoodsMap = goodsDisSpecOutList.stream().collect(Collectors.toMap(GoodsDisSpecOut::getGoodsCode, Function.identity()));
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        goodsCodeList.forEach(goodsCode -> {
            if (Objects.isNull(checkGoodsMap.get(goodsCode))) {
                errorJoiner.add("商品" + goodsCode + "不在组织商品中");
            }
        });
        if (errorJoiner.length() > NumberUtil.INTEGER_ONE) {
            throw new BusinessException(errorJoiner.toString());
        }
        return goodsCodeList;
    }

    private List<String> checkPresaleActivityStore(SaveDisPresaleActivityIn saveDisPresaleActivityIn) {
        List<OrdDisPresaleActivityStore> activityStoreList = saveDisPresaleActivityIn.getActivityStoreList();
        List<String> storeCodeList = activityStoreList.stream().map(OrdDisPresaleActivityStore::getStoreCode).collect(Collectors.toList());
        String duplicateStoreCodes = activityStoreList.stream()
                .collect(Collectors.groupingBy(OrdDisPresaleActivityStore::getStoreCode)) // 按id分组
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > NumberUtil.INTEGER_ONE) // 过滤出有重复的组
                .flatMap(entry -> entry.getValue().stream()) // 获取重复的Person对象
                .map(OrdDisPresaleActivityStore::getStoreCode).collect(Collectors.joining(SystemConstant.COMMA));
        if (StringUtils.isNotBlank(duplicateStoreCodes)) {
            throw new BusinessException("门店" + duplicateStoreCodes + "重复");
        }
        QueryBizOrgCodeStoreIn queryBizOrgCodeStoreIn = new QueryBizOrgCodeStoreIn();
        queryBizOrgCodeStoreIn.setBizOrgCode(saveDisPresaleActivityIn.getBizOrgCode());
        queryBizOrgCodeStoreIn.setStoreCodeList(storeCodeList);
        List<StoreInfo> storeBaseInfoList = storeCenterService.findSimpleStoresByCodes(queryBizOrgCodeStoreIn);
        if (CollectionUtils.isEmpty(storeBaseInfoList)) {
            throw new BusinessException("查询组织下门店为空");
        }
        Map<String, StoreInfo> checkStoreMap = storeBaseInfoList.stream().collect(Collectors.toMap(StoreInfo::getErpStoreCode, Function.identity()));
        // 校验门店存在组织中
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        storeCodeList.forEach(storeCode -> {
            StoreInfo storeInfo = checkStoreMap.get(storeCode);
            if (Objects.isNull(storeInfo)) {
                errorJoiner.add("门店" + storeCode + "不在组织中");
                return;
            }
            if (!StoreConstant.StoreProperty.FRANCHISE.getMytValue().equals(storeInfo.getStoreProperty())) {
                errorJoiner.add("门店" + storeCode + "为直营");
            }
        });
        if (errorJoiner.length() > NumberUtil.INTEGER_ONE) {
            throw new BusinessException(errorJoiner.toString());
        }
        return storeCodeList;
    }

    private void checkStoreGoodsExistActivity(SaveDisPresaleActivityIn saveDisPresaleActivityIn, List<String> storeCodeList,
                                              List<String> goodsCodeList) {
        LocalDateTime newBeginOrderDate = saveDisPresaleActivityIn.getBeginOrderDate();
        LocalDateTime newEndOrderDate = saveDisPresaleActivityIn.getEndOrderDate();
        CheckExistPresaleActivityIn checkExistPresaleActivityIn = new CheckExistPresaleActivityIn();
        List<String> statusList = Lists.list(OrdDisPresaleActivityStatusEnum.APPROVED.getKey(), OrdDisPresaleActivityStatusEnum.EXECUTED.getKey(),
                OrdDisPresaleActivityStatusEnum.STOPPED.getKey(), OrdDisPresaleActivityStatusEnum.TERMINATED.getKey());
        checkExistPresaleActivityIn.setStatusList(statusList);
        checkExistPresaleActivityIn.setStoreCodeList(storeCodeList);
        checkExistPresaleActivityIn.setGoodsCodeList(goodsCodeList);
//        checkExistPresaleActivityIn.setBeginOrderDate(newBeginOrderDate);
//        checkExistPresaleActivityIn.setEndOrderDate(newEndOrderDate);
        checkExistPresaleActivityIn.setId(saveDisPresaleActivityIn.getId());
//        checkExistPresaleActivityIn.setIsGift(isGift);
        List<CheckExistPresaleActivityOut> existDataList = ordDisPresaleActivityMapper.findExistPresaleActivityInfo(checkExistPresaleActivityIn);
        Map<String, List<CheckExistPresaleActivityOut>> presaleActivityNoMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(existDataList)) {
            presaleActivityNoMap = existDataList.stream().collect(Collectors.groupingBy(CheckExistPresaleActivityOut::getPresaleActivityNo));
        }
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        Map<String, List<CheckExistPresaleActivityOut>> finalPresaleActivityNoMap = presaleActivityNoMap;
        storeCodeList.forEach(storeCode -> goodsCodeList.forEach(goodsCode -> {
            // 循环所有活动下明细，进行对比
            finalPresaleActivityNoMap.entrySet().forEach(entry -> {
                String presaleActivityNo = entry.getKey();
                List<CheckExistPresaleActivityOut> presaleActivityNoDataList = entry.getValue();
                Map<String, CheckExistPresaleActivityOut> existActivityStoreGoodsMap = presaleActivityNoDataList.stream()
                        .collect(Collectors.toMap(detail -> detail.getStoreCode() + detail.getGoodsCode(), Function.identity(), (v1, v2) -> v1));
                CheckExistPresaleActivityOut checkExistPresaleActivityOut = existActivityStoreGoodsMap.get(storeCode + goodsCode);
                if (Objects.isNull(checkExistPresaleActivityOut)) {
                    return;
                }
                // 新的结束时间 在 老活动的 开始时间 之后，并且 新的开始时间 在 旧的 结束时间 之前，则新旧时间范围存在交叉，不允许创建
                boolean existFlag = newEndOrderDate.isAfter(checkExistPresaleActivityOut.getBeginOrderDate()) && newBeginOrderDate.isBefore(checkExistPresaleActivityOut.getEndOrderDate());
                if (existFlag) {
                    errorJoiner.add("门店" + storeCode + "下商品" + goodsCode + "已存在已审核、已生效、已中止或者已终止预售活动：" + presaleActivityNo + "中");
                }
            });
        }));
        if (errorJoiner.length() > NumberUtil.INTEGER_ONE) {
            throw new BusinessException(errorJoiner.toString());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> auditPresaleActivityData(SaveDisPresaleActivityIn saveDisPresaleActivityIn) {
        Response<OrdDisPresaleActivity> response = this.savePresaleActivity(saveDisPresaleActivityIn);
        OrdDisPresaleActivity ordDisPresaleActivity = response.getData();
        ordDisPresaleActivity.setStatus(OrdDisPresaleActivityStatusEnum.APPROVED.getKey());
        ordDisPresaleActivity.setUpdater(saveDisPresaleActivityIn.getLoginUsername());
        ordDisPresaleActivityMapper.updateByPrimaryKey(ordDisPresaleActivity);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY_AUDIT.getName(),
                String.valueOf(ordDisPresaleActivity.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY.getCode(),
                OperateLogTypeEnum.APPROVED.getName(),
                new Date(), ordDisPresaleActivity.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("审核成功");
    }

    @Override
    public List<Long> findNeedExecutePresaleActivityIdList() {
        return ordDisPresaleActivityMapper.findNeedExecutePresaleActivityIdList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executedPresaleActivity(Long id, String loginUsername) {
        OrdDisPresaleActivity ordDisPresaleActivity = new OrdDisPresaleActivity();
        ordDisPresaleActivity.setId(id);
        ordDisPresaleActivity.setStatus(OrdDisPresaleActivityStatusEnum.EXECUTED.getKey());
        ordDisPresaleActivity.setUpdater(loginUsername);
        ordDisPresaleActivity.setUpdateTime(LocalDateTime.now());
        int count = ordDisPresaleActivityMapper.updatePresaleActivityStatus(ordDisPresaleActivity, OrdDisPresaleActivityStatusEnum.APPROVED.getKey());
        if (count == NumberUtil.INTEGER_ZERO) {
            return;
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY_EXECUTED.getName(),
                String.valueOf(ordDisPresaleActivity.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY.getCode(),
                OperateLogTypeEnum.EXECUTED.getName(),
                new Date(), ordDisPresaleActivity.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public OrdDisPresaleActivity getOneById(Long id) {
        return ordDisPresaleActivityMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidPresaleActivity(OrdDisPresaleActivity ordDisPresaleActivity) {
        ordDisPresaleActivity.setStatus(OrdDisPresaleActivityStatusEnum.INVALID.getKey());
        ordDisPresaleActivity.setUpdateTime(LocalDateTime.now());
        int count = ordDisPresaleActivityMapper.updatePresaleActivityStatus(ordDisPresaleActivity, null);
        if (count == NumberUtil.INTEGER_ZERO) {
            return;
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY_INVALID.getName(),
                String.valueOf(ordDisPresaleActivity.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY.getCode(),
                OperateLogTypeEnum.INVALID.getName(),
                new Date(), ordDisPresaleActivity.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopPresaleActivity(OrdDisPresaleActivity ordDisPresaleActivity) {
        ordDisPresaleActivity.setStatus(OrdDisPresaleActivityStatusEnum.STOPPED.getKey());
        ordDisPresaleActivity.setUpdateTime(LocalDateTime.now());
        int count = ordDisPresaleActivityMapper.updatePresaleActivityStatus(ordDisPresaleActivity, OrdDisPresaleActivityStatusEnum.EXECUTED.getKey());
        if (count == NumberUtil.INTEGER_ZERO) {
            return;
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY_STOPPED.getName(),
                String.valueOf(ordDisPresaleActivity.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY.getCode(),
                OperateLogTypeEnum.ABORTED.getName(),
                new Date(), ordDisPresaleActivity.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public List<Long> findNeedStopPresaleActivityIdList() {
        return ordDisPresaleActivityMapper.findNeedStopPresaleActivityIdList();
    }

    @Override
    public List<OrdDisPresaleActivity> findExecutePresaleActivityImageList(String storeCode) {
        return ordDisPresaleActivityMapper.findExecutePresaleActivityImageList(storeCode);
    }

    @Override
    public List<PresaleActivityInfoForAppOut> findPresaleActivityInfoListForApp(String storeCode) {
        List<OrdDisPresaleActivity> disPresaleActivityInfoList = ordDisPresaleActivityMapper.findPresaleActivityInfoList(storeCode);
        // 封装活动商品
        List<PresaleActivityInfoForAppOut> presaleActivityInfoOutList = new ArrayList<>();
        for (OrdDisPresaleActivity ordDisPresaleActivity : disPresaleActivityInfoList) {
            PresaleActivityInfoForAppOut presaleActivityInfoOut = new PresaleActivityInfoForAppOut();
            BeanUtils.copy(ordDisPresaleActivity, presaleActivityInfoOut);
            List<OrdDisPresaleActivityGoods> presaleActivityGoodsList = ordDisPresaleActivityGoodsService.findGoodsListByPresaleActivityId(ordDisPresaleActivity.getId());
            // 封装活动商品
            List<PresaleActivityGoodsForAppOut> activityGoodsInfoOutList = new ArrayList<>();
            for (OrdDisPresaleActivityGoods ordDisPresaleActivityGoods : presaleActivityGoodsList) {
                PresaleActivityGoodsForAppOut presaleActivityGoodsInfoOut = new PresaleActivityGoodsForAppOut();
                BeanUtils.copy(ordDisPresaleActivityGoods, presaleActivityGoodsInfoOut);
                OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
                orderGoodsIn.setStoreCode(storeCode);
                orderGoodsIn.setGoodsCode(ordDisPresaleActivityGoods.getGoodsCode());
                orderGoodsIn.setBizOrgCode(ordDisPresaleActivity.getBizOrgCode());
                orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(SourceTypeEnum.INITIATIVE.getKey()));
                OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoodsByCache(orderGoodsIn);
                if (Objects.isNull(orderGoodsOut)) {
                    log.error("预售活动:{};商品{}不存在", ordDisPresaleActivity.getPresaleActivityNo(), ordDisPresaleActivityGoods.getGoodsCode());
                    continue;
                }
                presaleActivityGoodsInfoOut.setDistributionPrice(orderGoodsOut.getDistributionPrice());
                activityGoodsInfoOutList.add(presaleActivityGoodsInfoOut);
            }
            presaleActivityInfoOut.setPresaleActivityGoodsInfoOutList(activityGoodsInfoOutList);
            // 活动中有商品校验不通过，则整个活动不显示
            if (activityGoodsInfoOutList.size() == presaleActivityGoodsList.size()) {
                presaleActivityInfoOutList.add(presaleActivityInfoOut);
            }
        }
        return presaleActivityInfoOutList;

    }

    @Override
    public OrdDisPresaleActivity getPresaleActivityIsExecute(Long id) {
        OrdDisPresaleActivity presaleActivity = ordDisPresaleActivityMapper.selectByPrimaryKey(id);
        LocalDateTime nowTime = LocalDateTime.now();
        boolean isExecutedFlag = OrdDisPresaleActivityStatusEnum.EXECUTED.getKey().equals(presaleActivity.getStatus())
                && !(nowTime.isBefore(presaleActivity.getBeginSaleDate()) || nowTime.isAfter(presaleActivity.getEndSaleDate()));
        return isExecutedFlag ? presaleActivity : null;
    }

    @Override
    public Page<PresaleActivityListOut> findPresaleActivityListByPage(PresaleActivityListPageIn presaleActivityListIn) {
        List<PresaleActivityListOut> activityList = ordDisPresaleActivityMapper.findPresaleActivityListByPage(presaleActivityListIn);
        activityList.forEach(item -> item.setStatusDesc(OrdDisPresaleActivityStatusEnum.getValueByKey(item.getStatus())));
        Page<PresaleActivityListOut> activityPage = new Page<>(presaleActivityListIn);
        activityPage.setList(activityList);
        return activityPage;
    }

    @Override
    public PresaleActivityDetailOut findPresaleActivityById(Long id) {
        OrdDisPresaleActivity activity = ordDisPresaleActivityMapper.selectByPrimaryKey(id);
        if (Objects.isNull(activity)) {
            throw new BusinessException("预售活动不存在");
        }
        List<OrdDisPresaleActivityStore> activityStoreList = ordDisPresaleActivityStoreService.list(OrdDisPresaleActivityStore.builder().presaleActivityId(id).build());
        List<OrdDisPresaleActivityGoods> activityGoodsList = ordDisPresaleActivityGoodsService.list(OrdDisPresaleActivityGoods.builder().presaleActivityId(id).build());
        return PresaleActivityDetailOut.builder()
                .id(activity.getId())
                .presaleActivityNo(activity.getPresaleActivityNo())
                .status(activity.getStatus())
                .statusDesc(OrdDisPresaleActivityStatusEnum.getValueByKey(activity.getStatus()))
                .beginSaleDate(activity.getBeginSaleDate())
                .endSaleDate(activity.getEndSaleDate())
                .beginOrderDate(activity.getBeginOrderDate())
                .endOrderDate(activity.getEndOrderDate())
                .profile(activity.getProfile())
                .presaleImage(activity.getPresaleImage())
                .isLimitBuy(activity.getIsLimitBuy())
                .limitBuyTime(activity.getLimitBuyTime())
                .promotionalPrice(activity.getPromotionalPrice())
                .activityGoodsList(activityGoodsList)
                .activityStoreList(activityStoreList)
                .bizOrgCode(activity.getBizOrgCode())
                .creator(activity.getCreator())
                .createTime(activity.getCreateTime())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminatePresaleActivity(OrdDisPresaleActivity ordDisPresaleActivity) {
        ordDisPresaleActivity.setStatus(OrdDisPresaleActivityStatusEnum.TERMINATED.getKey());
        ordDisPresaleActivity.setUpdateTime(LocalDateTime.now());
        int count = ordDisPresaleActivityMapper.updatePresaleActivityStatus(ordDisPresaleActivity, OrdDisPresaleActivityStatusEnum.EXECUTED.getKey());
        if (count == NumberUtil.INTEGER_ZERO) {
            return;
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY_TERMINATED.getName(),
                String.valueOf(ordDisPresaleActivity.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY.getCode(),
                OperateLogTypeEnum.TERMINATED.getName(),
                new Date(), ordDisPresaleActivity.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public Response<List<ImportPresaleActivityStoreOut>> importPresaleActivityStore(String fileId) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        PresaleActivityStoreImportListener listener = new PresaleActivityStoreImportListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportPresaleActivityStore.class, listener).headRowNumber(NumberUtil.INTEGER_ONE).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > NumberUtil.INTEGER_ZERO) {
            return Response.error(listener.getImportErrorMessage(totalErrorMap));
        }
        List<String> storeCodeList = listener.getResultList().stream().map(ImportPresaleActivityStore::getStoreCode).collect(Collectors.toList());
        QueryBizOrgCodeStoreIn queryBizOrgCodeStoreIn = new QueryBizOrgCodeStoreIn();
        queryBizOrgCodeStoreIn.setBizOrgCode(UserUtil.getBizOrgCode());
        queryBizOrgCodeStoreIn.setStoreCodeList(storeCodeList);
        List<StoreInfo> storeBaseInfoList = storeCenterService.findSimpleStoresByCodes(queryBizOrgCodeStoreIn);
        if (CollectionUtils.isEmpty(storeBaseInfoList)) {
            throw new BusinessException("查询组织下门店为空");
        }
        Map<String, StoreInfo> checkStoreMap = storeBaseInfoList.stream().collect(Collectors.toMap(StoreInfo::getErpStoreCode, Function.identity()));
        // 校验门店存在组织中
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        List<ImportPresaleActivityStoreOut> list = Lists.newArrayList();
        storeCodeList.forEach(storeCode -> {
            StoreInfo storeInfo = checkStoreMap.get(storeCode);
            if (Objects.isNull(storeInfo)) {
                errorJoiner.add("门店" + storeCode + "不在组织中");
                return;
            }
            if (!StoreConstant.StoreProperty.FRANCHISE.getMytValue().equals(storeInfo.getStoreProperty())) {
                errorJoiner.add("门店" + storeCode + "为直营");
                return;
            }
            ImportPresaleActivityStoreOut importPresaleActivityStoreOut = new ImportPresaleActivityStoreOut();
            importPresaleActivityStoreOut.setStoreCode(storeCode);
            importPresaleActivityStoreOut.setStoreName(storeInfo.getStoreName());
            list.add(importPresaleActivityStoreOut);
        });
        if (errorJoiner.length() > NumberUtil.INTEGER_ONE) {
            throw new BusinessException(errorJoiner.toString());
        }
        return Response.data(list);
    }

    @Override
    public Response<CheckExtendEndOrderDateOut> checkExtendEndOrderDate(ExtendOrderDateIn extendOrderDateIn, String loginUsername) {
        OrdDisPresaleActivity activity = ordDisPresaleActivityMapper.selectByPrimaryKey(extendOrderDateIn.getId());
        if (Objects.isNull(activity)) {
            throw new BusinessException("预售活动不存在");
        }
        if (!OrdDisPresaleActivityStatusEnum.STOPPED.getKey().equals(activity.getStatus())
                && !OrdDisPresaleActivityStatusEnum.TERMINATED.getKey().equals(activity.getStatus())) {
            return Response.error("预售活动状态不允许调整订货结束时间");
        }
        LocalDateTime extendOrderEndDate = DateUtils.parseTime(extendOrderDateIn.getEndOrderDate());
        long hours = Duration.between(activity.getEndOrderDate(), extendOrderEndDate).toHours();
        if (extendOrderEndDate.isBefore(activity.getEndOrderDate()) || hours < 1l) {
            return Response.error("新的订货结束时间至少间隔1小时");
        }
        CheckExtendEndOrderDateOut checkExtendEndOrderDateOut = new CheckExtendEndOrderDateOut();
        checkExtendEndOrderDateOut.setOldEndOrderDate(activity.getEndOrderDate());
        activity.setEndOrderDate(extendOrderEndDate);
        activity.setUpdater(loginUsername);
        activity.setUpdateTime(LocalDateTime.now());
        checkExtendEndOrderDateOut.setOrdDisPresaleActivity(activity);
        List<OrdDisPresaleActivityGoods> activityGoodsList = ordDisPresaleActivityGoodsService.findGoodsListByPresaleActivityId(activity.getId());
        List<OrdDisPresaleActivityStore> activityStoreList = ordDisPresaleActivityStoreService.findStoreListByPresaleActivityId(activity.getId());
        SaveDisPresaleActivityIn saveDisPresaleActivityIn = new SaveDisPresaleActivityIn();
        BeanUtils.copy(activity, saveDisPresaleActivityIn);
        saveDisPresaleActivityIn.setLoginUsername(loginUsername);
        saveDisPresaleActivityIn.setActivityGoodsList(activityGoodsList);
        saveDisPresaleActivityIn.setActivityStoreList(activityStoreList);
        List<String> goodsCodeList = activityGoodsList.stream().distinct().map(OrdDisPresaleActivityGoods::getGoodsCode).collect(Collectors.toList());
        List<String> storeCodeList = activityStoreList.stream().map(OrdDisPresaleActivityStore::getStoreCode).collect(Collectors.toList());
        this.checkStoreGoodsExistActivity(saveDisPresaleActivityIn, storeCodeList, goodsCodeList);
        return Response.data(checkExtendEndOrderDateOut);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> extendEndOrderDate(CheckExtendEndOrderDateOut checkExtendEndOrderDateOut) {
        OrdDisPresaleActivity presaleActivity = checkExtendEndOrderDateOut.getOrdDisPresaleActivity();
        ordDisPresaleAssetsDetailService.resetAssetsDetailList(presaleActivity);
        ordDisPresaleActivityMapper.updateByPrimaryKeySelective(presaleActivity);
        String content = "原订货结束日期" + DateUtils.format(checkExtendEndOrderDateOut.getOldEndOrderDate()) + "修改为" + DateUtils.format(presaleActivity.getEndOrderDate());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY_EXTEND_END_ORDER_DATE.getName(),
                String.valueOf(presaleActivity.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ACTIVITY.getCode(),
                content, new Date(), presaleActivity.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("延长成功");
    }
}
