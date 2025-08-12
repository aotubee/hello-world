package com.edc.erp.presale.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.AdjustTypeEnum;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.SourceTypeEnum;
import com.edc.erp.presale.entity.*;
import com.edc.erp.presale.enumeration.OrdDisPresaleAssetsStatusEnum;
import com.edc.erp.presale.mapper.OrdDisPresaleActivityMapper;
import com.edc.erp.presale.mapper.OrdDisPresaleAssetsDetailMapper;
import com.edc.erp.presale.mapper.OrdDisPresaleGoodsFlowMapper;
import com.edc.erp.presale.model.in.QueryPresaleAssetsDetailPageIn;
import com.edc.erp.presale.model.in.UpdateDisPresaleAssetsGoodsIn;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailExtOut;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailOut;
import com.edc.erp.presale.model.out.PresaleAssetsDetailForAppOut;
import com.edc.erp.presale.model.out.PresaleAssetsForAppOut;
import com.edc.erp.presale.service.OrdDisPresaleActivityGoodsService;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName OrdDisPresaleAssetsDetailServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/21 18:39
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisPresaleAssetsDetailServiceImpl extends BaseServiceImpl<OrdDisPresaleAssetsDetail> implements OrdDisPresaleAssetsDetailService {
    private final OrdDisPresaleAssetsDetailMapper ordDisPresaleAssetsDetailMapper;
    private final OrdDisPresaleActivityMapper ordDisPresaleActivityMapper;
    ;
    private final OrdDisPresaleActivityGoodsService ordDisPresaleActivityGoodsService;
    private final OrdDisPresaleGoodsFlowMapper ordDisPresaleGoodsFlowMapper;
    private final OrderGoodsServer orderGoodsServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePresaleAssetsDetail(UpdateDisPresaleAssetsGoodsIn updateDisPresaleAssetsGoodsIn, Long assetsId,
                                          String loginUsername, String businessType, String sourceNo) {
        OrdDisPresaleActivity presaleActivity = ordDisPresaleActivityMapper.selectByPrimaryKey(updateDisPresaleAssetsGoodsIn.getPresaleActivityId());
        if (Objects.isNull(presaleActivity)) {
            throw new BusinessException("预售活动不存在");
        }
        List<OrdDisPresaleActivityGoods> presaleActivityGoodsList = ordDisPresaleActivityGoodsService.findPresaleActivityGoodsByActivityIdAndGoodsCode(updateDisPresaleAssetsGoodsIn.getPresaleActivityId(), updateDisPresaleAssetsGoodsIn.getGoodsCode());
        if (CollectionUtils.isEmpty(presaleActivityGoodsList)) {
            throw new BusinessException("商品与预售活动不符");
        }
        OrdDisPresaleActivityGoods ordDisPresaleActivityGoods = presaleActivityGoodsList.get(NumberUtil.INTEGER_ZERO);
        OrdDisPresaleAssetsDetail ordDisPresaleAssetsDetail = new OrdDisPresaleAssetsDetail();
        ordDisPresaleAssetsDetail.setAssetsId(assetsId);
        ordDisPresaleAssetsDetail.setGoodsCode(updateDisPresaleAssetsGoodsIn.getGoodsCode());
        ordDisPresaleAssetsDetail.setPresaleActivityNo(updateDisPresaleAssetsGoodsIn.getPresaleActivityNo());
        ordDisPresaleAssetsDetail.setIsDelete(ModelConst.DELETE.NO);
        int existCount = ordDisPresaleAssetsDetailMapper.selectCount(ordDisPresaleAssetsDetail);
        if (existCount == NumberUtil.INTEGER_ZERO) {
            OrdDisPresaleAssetsDetail saveOrdDisPresaleAssetsDetail = new OrdDisPresaleAssetsDetail();
            BeanUtils.copy(ordDisPresaleActivityGoods, saveOrdDisPresaleAssetsDetail);
            saveOrdDisPresaleAssetsDetail.setId(null);
            saveOrdDisPresaleAssetsDetail.setAssetsId(assetsId);
            String status;
            if (LocalDateTime.now().isBefore(presaleActivity.getBeginOrderDate())) {
                status = OrdDisPresaleAssetsStatusEnum.NOT_STARTED.getKey();
            } else if (LocalDateTime.now().isAfter(presaleActivity.getEndOrderDate())) {
                status = OrdDisPresaleAssetsStatusEnum.EXPIRED.getKey();
            } else {
                status = OrdDisPresaleAssetsStatusEnum.ORDERING.getKey();
            }
            saveOrdDisPresaleAssetsDetail.setStatus(status);
            saveOrdDisPresaleAssetsDetail.setPackageQuantity(BigDecimal.ZERO);
            saveOrdDisPresaleAssetsDetail.setOrderQuantity(BigDecimal.ZERO);
            saveOrdDisPresaleAssetsDetail.setSurplusQuantity(updateDisPresaleAssetsGoodsIn.getUpdateSurplusQuantity());
            String surplusPackageQuantity;
            Integer quantity = updateDisPresaleAssetsGoodsIn.getUpdateSurplusQuantity().intValue();
            Integer qpc = updateDisPresaleAssetsGoodsIn.getPackageSpecificationNum().intValue();
            if (quantity % qpc != NumberUtil.INTEGER_ZERO) {
                surplusPackageQuantity = quantity / qpc + "+" + quantity % qpc;
            } else {
                surplusPackageQuantity = String.valueOf(quantity / qpc);
            }
            saveOrdDisPresaleAssetsDetail.setSurplusPackageQuantity(surplusPackageQuantity);
            saveOrdDisPresaleAssetsDetail.setPresaleActivityNo(updateDisPresaleAssetsGoodsIn.getPresaleActivityNo());
            saveOrdDisPresaleAssetsDetail.setBeginOrderDate(presaleActivity.getBeginOrderDate());
            saveOrdDisPresaleAssetsDetail.setEndOrderDate(presaleActivity.getEndOrderDate());
            saveOrdDisPresaleAssetsDetail.setCreator(loginUsername);
            saveOrdDisPresaleAssetsDetail.setUpdater(loginUsername);
            saveOrdDisPresaleAssetsDetail.setIsDelete(ModelConst.DELETE.NO);
            ordDisPresaleAssetsDetailMapper.insert(saveOrdDisPresaleAssetsDetail);
        } else {
            int count = ordDisPresaleAssetsDetailMapper.updateSurplusAndOrderQuantity(updateDisPresaleAssetsGoodsIn);
            if (count < 1) {
                log.error("门店{}单号{}资产扣减异常，资产不足", updateDisPresaleAssetsGoodsIn.getStoreCode(), sourceNo);
                throw new BusinessException("门店" + updateDisPresaleAssetsGoodsIn.getStoreCode() + "单号" + sourceNo + "资产扣减异常");
            }
        }
        OrdDisPresaleAssetsDetail afterOrdDisPresaleAssetsDetail = ordDisPresaleAssetsDetailMapper.selectOne(ordDisPresaleAssetsDetail);
        OrdDisPresaleGoodsFlow ordDisPresaleGoodsFlow = new OrdDisPresaleGoodsFlow();
        BeanUtils.copy(ordDisPresaleActivityGoods, ordDisPresaleGoodsFlow);
        ordDisPresaleGoodsFlow.setStoreCode(updateDisPresaleAssetsGoodsIn.getStoreCode());
        ordDisPresaleGoodsFlow.setStoreName(updateDisPresaleAssetsGoodsIn.getStoreName());
        ordDisPresaleGoodsFlow.setBusinessType(businessType);
        ordDisPresaleGoodsFlow.setSourceNo(sourceNo);
        BigDecimal qty = updateDisPresaleAssetsGoodsIn.getUpdateSurplusQuantity().abs();
        ordDisPresaleGoodsFlow.setQty(AdjustTypeEnum.REDUCE.getCode().equals(updateDisPresaleAssetsGoodsIn.getActualLowering()) ? qty.negate() : qty);
        ordDisPresaleGoodsFlow.setQtyBalance(afterOrdDisPresaleAssetsDetail.getSurplusQuantity());
//        String actualLowering = updateDisPresaleAssetsGoodsIn.getGoodsQuantity().compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ONE ? AdjustTypeEnum.ADD.getCode() : AdjustTypeEnum.REDUCE.getCode();
        ordDisPresaleGoodsFlow.setActualLowering(updateDisPresaleAssetsGoodsIn.getActualLowering());
        ordDisPresaleGoodsFlow.setFlowDate(LocalDateTime.now());
        ordDisPresaleGoodsFlow.setBizOrgCode(presaleActivity.getBizOrgCode());
        ordDisPresaleGoodsFlow.setOrgCode(presaleActivity.getOrgCode());
        ordDisPresaleGoodsFlow.setCreator(loginUsername);
        ordDisPresaleGoodsFlow.setUpdateTime(LocalDateTime.now());
        ordDisPresaleGoodsFlowMapper.insert(ordDisPresaleGoodsFlow);
    }

    @Override
    public Page<OrdDisPresaleAssetsDetailOut> findPresaleAssetsDetailForPage(QueryPresaleAssetsDetailPageIn queryPresaleAssetsDetailPageIn) {
        List<OrdDisPresaleAssetsDetailOut> list = ordDisPresaleAssetsDetailMapper.findPresaleAssetsDetailByPage(queryPresaleAssetsDetailPageIn);
        list.forEach(ordDisPresaleAssetsDetailOut -> ordDisPresaleAssetsDetailOut.setStatusStr(OrdDisPresaleAssetsStatusEnum.getValueByKey(ordDisPresaleAssetsDetailOut.getStatus())));
        Page<OrdDisPresaleAssetsDetailOut> page = new Page<>(queryPresaleAssetsDetailPageIn);
        page.setList(list);
        return page;
    }

    // @Override
    // public String exportPresaleAssetsDetail(QueryPresaleAssetsDetailPageIn queryPresaleAssetsDetailPageIn) {
    //     String title = "预售商品资产详情";
    //     String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
    //     Page<OrdDisPresaleAssetsDetailOut> resultPage = this.findPresaleAssetsDetailForPage(queryPresaleAssetsDetailPageIn);
    //     List<ExcelOrdDisPresaleAssetsDetail> exportPresaleAssetsDetailList = Lists.newArrayList();
    //     if (CollectionUtils.isNotEmpty(resultPage.getList())) {
    //         resultPage.getList().forEach(assetsDetail -> {
    //             ExcelOrdDisPresaleAssetsDetail exportPresaleAssetsDetail = new ExcelOrdDisPresaleAssetsDetail();
    //             org.springframework.beans.BeanUtils.copyProperties(assetsDetail, exportPresaleAssetsDetail);
    //             exportPresaleAssetsDetailList.add(exportPresaleAssetsDetail);
    //         });
    //     }
    //     byte[] bytes = FileExportUtil.getFileBytesByData(exportPresaleAssetsDetailList, "预售商品资产详情", "预售商品资产详情", ExcelOrdDisPresaleAssetsDetail.class, true);
    //     return fileService.uploadFile(fileName, bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    // }

    @Override
    public List<OrdDisPresaleAssetsDetail> findStoreGoodsAssetsDetailList(String storeCode) {
        return ordDisPresaleAssetsDetailMapper.findStoreGoodsAssetsDetailList(storeCode);
    }

    @Override
    public Boolean checkGoodsIsExcess(String storeCode, String goodsCode, BigDecimal quantity) {
        OrdDisPresaleAssetsDetail ordDisPresaleAssetsDetail = ordDisPresaleAssetsDetailMapper.getStoreAssetsDetail(storeCode, goodsCode, LocalDateTime.now());
        if (Objects.isNull(ordDisPresaleAssetsDetail)) {
            return true;
        } else {
            BigDecimal surplusQuantity = ordDisPresaleAssetsDetail.getSurplusQuantity();
            return surplusQuantity.compareTo(quantity) == -1 ? false : true;
        }
    }

    @Override
    public Response<String> checkGoodsIsExcessAndInOrderTime(OrdDisPresaleAssets ordDisPresaleAssets, String goodsCode, BigDecimal quantity) {
        OrdDisPresaleAssetsDetail ordDisPresaleAssetsDetail = ordDisPresaleAssetsDetailMapper.getStoreAssetsDetail(ordDisPresaleAssets.getStoreCode(), goodsCode, LocalDateTime.now());
        if (Objects.isNull(ordDisPresaleAssetsDetail)) {
            return Response.error("预售商品" + goodsCode + "不存在");
        } else {
            LocalDateTime beginOrderDate = ordDisPresaleAssetsDetail.getBeginOrderDate();
            LocalDateTime endOrderDate = ordDisPresaleAssetsDetail.getEndOrderDate();
            LocalDateTime nowTime = LocalDateTime.now();
            if (nowTime.isBefore(beginOrderDate) || nowTime.isAfter(endOrderDate)) {
                return Response.error("预售商品" + goodsCode + "不在可订货时间内");
            }
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setStoreCode(ordDisPresaleAssets.getStoreCode());
            orderGoodsIn.setGoodsCode(goodsCode);
            orderGoodsIn.setBizOrgCode(ordDisPresaleAssets.getBizOrgCode());
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(SourceTypeEnum.INITIATIVE.getKey()));
            OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoodsByCache(orderGoodsIn);
            if (Objects.isNull(orderGoodsOut)) {
                return Response.error("商品" + goodsCode + "不存在");
            }
            BigDecimal surplusQuantity = ordDisPresaleAssetsDetail.getSurplusQuantity();
            BigDecimal surplusPackageQuantity = surplusQuantity.divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN);
            if (surplusPackageQuantity.compareTo(quantity) == -1) {
                return Response.error("预售商品" + goodsCode + "超过剩余订货量");
            }
            return Response.success();
        }
    }

    @Override
    public OrdDisPresaleAssetsDetail getStoreAssetsDetail(String storeCode, String goodsCode, LocalDateTime orderCreateTime) {
        return ordDisPresaleAssetsDetailMapper.getStoreAssetsDetail(storeCode, goodsCode, orderCreateTime);
    }

    @Override
    public OrdDisPresaleAssetsDetail getStoreGoodsAssetsDetail(String storeCode, String goodsCode, String presaleActivityNo) {
        return ordDisPresaleAssetsDetailMapper.getStoreGoodsAssetsDetail(storeCode, goodsCode, presaleActivityNo);
    }

    @Override
    public List<OrdDisPresaleAssetsDetail> getStoreGoodsAssetsDetailList(String storeCode, String goodsCode) {
        return ordDisPresaleAssetsDetailMapper.getStoreGoodsAssetsDetailList(storeCode, goodsCode);
    }

    @Override
    public List<PresaleAssetsForAppOut> findStorePresaleAssetsInfo(String storeCode) {
        List<OrdDisPresaleAssetsDetailExtOut> presaleAssetsForAppOutList = ordDisPresaleAssetsDetailMapper.findStorePresaleAssetsInfo(storeCode);
        Map<String, List<OrdDisPresaleAssetsDetailExtOut>> presaleActivityNoMap = presaleAssetsForAppOutList.stream()
                .collect(Collectors.groupingBy(assetsDetail -> assetsDetail.getPresaleActivityNo() + SystemConstant.WAIT
                        + DateUtils.format(assetsDetail.getBeginOrderDate()) + SystemConstant.WAIT + DateUtils.format(assetsDetail.getEndOrderDate())));
        List<PresaleAssetsForAppOut> resultList = presaleActivityNoMap.entrySet().stream().map(entry -> {
                    String[] keyArrays = entry.getKey().split(SystemConstant.WAIT);
                    String presaleActivityNo = keyArrays[0];
                    String beginOrderDateStr = keyArrays[1];
                    String endOrderDateStr = keyArrays[2];
                    PresaleAssetsForAppOut presaleAssetsForAppOut = new PresaleAssetsForAppOut();
                    presaleAssetsForAppOut.setPresaleActivityNo(presaleActivityNo);
                    presaleAssetsForAppOut.setBeginOrderDateStr(beginOrderDateStr);
                    presaleAssetsForAppOut.setEndOrderDateStr(endOrderDateStr);
                    List<PresaleAssetsDetailForAppOut> list = entry.getValue().stream().map(item -> {
                        PresaleAssetsDetailForAppOut presaleAssetsDetailForAppOut = new PresaleAssetsDetailForAppOut();
                        BeanUtils.copy(item, presaleAssetsDetailForAppOut);
                        return presaleAssetsDetailForAppOut;
                    }).collect(Collectors.toList());
                    presaleAssetsForAppOut.setPresaleAssetsDetailList(list);
                    return presaleAssetsForAppOut;
                })
                .sorted((o1, o2) -> o2.getEndOrderDateStr().compareTo(o1.getEndOrderDateStr())).sorted((o1, o2) -> o2.getPresaleActivityNo().compareTo(o1.getPresaleActivityNo()))
                .collect(Collectors.toList());
        return resultList;
    }

    @Override
    public List<Long> findNeedUpdateStatusAssetsDetailList() {
        return ordDisPresaleAssetsDetailMapper.findNeedUpdateStatusAssetsDetailList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String updateStatus, String nowStatus, String loginUsername) {
        ordDisPresaleAssetsDetailMapper.updateStatus(id, updateStatus, nowStatus, loginUsername);
    }

    @Override
    public OrdDisPresaleAssetsDetail getOneById(Long id) {
        return ordDisPresaleAssetsDetailMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetAssetsDetailList(OrdDisPresaleActivity presaleActivity) {
        OrdDisPresaleAssetsDetail ordDisPresaleAssetsDetail = new OrdDisPresaleAssetsDetail();
        ordDisPresaleAssetsDetail.setPresaleActivityId(presaleActivity.getId());
        List<OrdDisPresaleAssetsDetail> assetsDetailList = ordDisPresaleAssetsDetailMapper.select(ordDisPresaleAssetsDetail);
        if (CollectionUtils.isEmpty(assetsDetailList)) {
            return;
        }
        assetsDetailList.forEach(detail -> {
            String status;
            if (LocalDateTime.now().isBefore(presaleActivity.getBeginOrderDate())) {
                status = OrdDisPresaleAssetsStatusEnum.NOT_STARTED.getKey();
            } else if (LocalDateTime.now().isAfter(presaleActivity.getEndOrderDate())) {
                status = OrdDisPresaleAssetsStatusEnum.EXPIRED.getKey();
            } else {
                status = OrdDisPresaleAssetsStatusEnum.ORDERING.getKey();
            }
            detail.setStatus(status);
            detail.setEndOrderDate(presaleActivity.getEndOrderDate());
            detail.setUpdater(presaleActivity.getUpdater());
            detail.setUpdateTime(LocalDateTime.now());
            ordDisPresaleAssetsDetailMapper.updateByPrimaryKeySelective(detail);
        });
    }
}
