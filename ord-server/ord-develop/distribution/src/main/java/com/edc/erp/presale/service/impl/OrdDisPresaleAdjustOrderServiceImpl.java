package com.edc.erp.presale.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.out.store.StoreInfoOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.FileExportUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrder;
import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrderDetail;
import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.erp.presale.enumeration.*;
import com.edc.erp.presale.listener.PresaleAdjustOrderDetailImportListener;
import com.edc.erp.presale.listener.PresaleAdjustOrderImportListener;
import com.edc.erp.presale.mapper.OrdDisPresaleAdjustOrderMapper;
import com.edc.erp.presale.model.excel.*;
import com.edc.erp.presale.model.in.*;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailOut;
import com.edc.erp.presale.model.out.PresaleAdjustOrderDetailOut;
import com.edc.erp.presale.model.out.PresaleAdjustOrderGetOut;
import com.edc.erp.presale.model.out.PresaleAdjustOrderPageOut;
import com.edc.erp.presale.service.OrdDisPresaleAdjustOrderDetailService;
import com.edc.erp.presale.service.OrdDisPresaleAdjustOrderService;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.erp.presale.service.OrdDisPresaleAssetsService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @ClassName OrdDisPresaleAdjustOrderServiceImpl
 * @Author ZhangYao
 * @CreateTime 2024/8/21 18:37
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisPresaleAdjustOrderServiceImpl extends BaseServiceImpl<OrdDisPresaleAdjustOrder> implements OrdDisPresaleAdjustOrderService {
    private final OrdDisPresaleAdjustOrderMapper adjustOrderMapper;
    private final OrdDisPresaleAdjustOrderDetailService adjustOrderDetailService;
    private final OrdDisPresaleAssetsService ordDisPresaleAssetsService;
    private final OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;
    private final StoreCenterService storeCenterService;
    private final UniqueUtils uniqueUtils;
    private final AsyncLogService asyncLogService;
    private final FileService fileService;
    private final AsyncExportHandle asyncExportHandle;

    @Override
    public Page<PresaleAdjustOrderPageOut> findPresaleAdjustOrderByPage(PresaleAdjustOrderPageIn presaleAdjustOrderPageIn) {
        if (StringUtils.isBlank(presaleAdjustOrderPageIn.getBizOrgCode())) {
            presaleAdjustOrderPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        if (StringUtils.isNotEmpty(presaleAdjustOrderPageIn.getStoreArea())) {
            List<String> storeCodeList = new ArrayList<>();
            List<StoreInfoOut> storeList = storeCenterService.getStoreInfoByCode(new StoreInfoIn(presaleAdjustOrderPageIn.getStoreArea()));
            if (CollectionUtils.isEmpty(storeList)) {
                return new Page<>(presaleAdjustOrderPageIn);
            }
            storeList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            presaleAdjustOrderPageIn.setStoreCodeList(storeCodeList);
        }
        List<PresaleAdjustOrderPageOut> adjustOrderList = adjustOrderMapper.findPresaleAdjustOrderByPage(presaleAdjustOrderPageIn);
        adjustOrderList.forEach(item -> {
            item.setStatusDesc(OrdDisPresaleAdjustOrderStatusEnum.getName(item.getStatus()));
            item.setAdjustTypeDesc(OrdDisPresaleAdjustOrderTypeEnum.getName(item.getAdjustType()));
            // StoreInfo storeInfo = storeCenterService.getStoreByCode(item.getStoreCode(), item.getBizOrgCode());
            // if (Objects.nonNull(storeInfo)) {
            //     item.setBelongArea(storeInfo.getBelongArea());
            // }
        });
        Page<PresaleAdjustOrderPageOut> adjustOrderPage = new Page<>(presaleAdjustOrderPageIn);
        adjustOrderPage.setList(adjustOrderList);
        return adjustOrderPage;
    }

    @Override
    public PresaleAdjustOrderGetOut getPresaleAdjustOrderDetail(Long id) {
        OrdDisPresaleAdjustOrder adjustOrder = this.selectByPrimaryKey(id);
        if (Objects.isNull(adjustOrder) || ModelConst.DELETE.YES.equals(adjustOrder.getIsDelete())) {
            throw new BusinessException("预售调整单不存在");
        }
        PresaleAdjustOrderGetOut detailOut = new PresaleAdjustOrderGetOut();
        BeanUtils.copyProperties(adjustOrder, detailOut);
        List<OrdDisPresaleAdjustOrderDetail> adjustOrderDetail = adjustOrderDetailService.list(OrdDisPresaleAdjustOrderDetail.builder().adjustOrderId(id).build());
        detailOut.setStatusDesc(OrdDisPresaleAdjustOrderStatusEnum.getName(adjustOrder.getStatus()));
        detailOut.setDetails(adjustOrderDetail);
        return detailOut;
    }

    @Override
    public boolean invalidAdjustOrder(Long id) {
        OrdDisPresaleAdjustOrder adjustOrder = this.selectByPrimaryKey(id);
        // 状态判断 待审核状态允许作废
        if (Objects.isNull(adjustOrder)) {
            throw new BusinessException("预售调整单不存在");
        }
        if (!OrdDisPresaleAdjustOrderStatusEnum.PENDING.getCode().equals(adjustOrder.getStatus())) {
            throw new BusinessException("预售调整单状态不允许作废");
        }
        adjustOrder.setStatus(OrdDisPresaleAdjustOrderStatusEnum.INVALID.getCode());
        adjustOrder.setUpdateTime(LocalDateTime.now());
        adjustOrder.setUpdater(UserUtil.getUserName());
        boolean result = adjustOrderMapper.updateByPrimaryKeySelective(adjustOrder) > 0;
        if (result) {
            String content = "作废";
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ADJUST_ORDER.getName(),
                    String.valueOf(adjustOrder.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ADJUST_ORDER.getCode(),
                    content, new Date(), UserUtil.getUserName());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveAdjustOrder(Long id) {
        String loginUsername = UserUtil.getUserName();
        OrdDisPresaleAdjustOrder adjustOrder = this.selectByPrimaryKey(id);
        if (Objects.isNull(adjustOrder)) {
            throw new BusinessException("预售调整单不存在");
        }
        if (!OrdDisPresaleAdjustOrderStatusEnum.PENDING.getCode().equals(adjustOrder.getStatus())) {
            throw new BusinessException("预售调整单状态不允许审核");
        }
        List<OrdDisPresaleAdjustOrderDetail> adjustOrderDetail = adjustOrderDetailService.list(OrdDisPresaleAdjustOrderDetail.builder().adjustOrderId(id).build());
        // 1.审核时页面数据进行校验：门店代码、商品代码、活动单号在资产模块存在，且仅当调整类型为扣减时---
        //   调整数量小于等于资产余数，校验通过保存成功，校验失败--错误数据红色变红提示；
        this.checkPresaleAdjustOrderDetail(adjustOrderDetail.stream().map(item -> CheckPresaleAdjustOrderDetailIn.builder()
                .storeCode(adjustOrder.getStoreCode())
                .goodsCode(item.getGoodsCode())
                .adjustType(adjustOrder.getAdjustType())
                .adjustQty(item.getAdjustQty())
                .presaleActivityNo(item.getPresaleActivityNo())
                .build()).collect(Collectors.toList()));
        // 2.审核通过
        adjustOrder.setStatus(OrdDisPresaleAdjustOrderStatusEnum.APPROVED.getCode());
        adjustOrder.setUpdateTime(LocalDateTime.now());
        adjustOrder.setUpdater(loginUsername);
        adjustOrder.setApprover(loginUsername);
        adjustOrder.setApprovalTime(LocalDateTime.now());
        // 3.审核后（根据调整类型）增加或扣减门店预售商品数量
        if (adjustOrderMapper.updateByPrimaryKeySelective(adjustOrder) > 0) {
            UpdateDisPresaleAssetsIn updateDisPresaleAssetsIn = new UpdateDisPresaleAssetsIn();
            updateDisPresaleAssetsIn.setBizOrgCode(adjustOrder.getBizOrgCode());
            updateDisPresaleAssetsIn.setLoginUsername(loginUsername);
            updateDisPresaleAssetsIn.setBusinessType(getAdjustOrderBusinessType(adjustOrder));
            updateDisPresaleAssetsIn.setSourceNo(adjustOrder.getOrderNo());
            OrdDisPresaleAssets presaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(adjustOrder.getStoreCode());
            if (Objects.nonNull(presaleAssets)) {
                updateDisPresaleAssetsIn.setAssetsId(presaleAssets.getId());
            }
            List<UpdateDisPresaleAssetsGoodsIn> assetsGoodsInList = adjustOrderDetail.stream().map(detail -> {
                OrdDisPresaleAssetsDetail storeAssetsDetail = ordDisPresaleAssetsDetailService.getStoreGoodsAssetsDetail(adjustOrder.getStoreCode(), detail.getGoodsCode(), detail.getPresaleActivityNo());
                if (Objects.isNull(storeAssetsDetail)) {
                    throw new BusinessException("门店资产商品" + detail.getGoodsCode() + "不存在");
                }
                UpdateDisPresaleAssetsGoodsIn updateDisPresaleAssetsGoodsIn = new UpdateDisPresaleAssetsGoodsIn();
                updateDisPresaleAssetsGoodsIn.setPresaleActivityId(storeAssetsDetail.getPresaleActivityId());
                updateDisPresaleAssetsGoodsIn.setPresaleActivityNo(storeAssetsDetail.getPresaleActivityNo());
                updateDisPresaleAssetsGoodsIn.setStoreCode(presaleAssets.getStoreCode());
                updateDisPresaleAssetsGoodsIn.setStoreName(presaleAssets.getStoreName());
                updateDisPresaleAssetsGoodsIn.setGoodsCode(detail.getGoodsCode());
                updateDisPresaleAssetsGoodsIn.setUpdateSurplusQuantity(OrdDisPresaleAdjustOrderTypeEnum.REDUCE.getCode().equals(adjustOrder.getAdjustType())
                        ? detail.getAdjustQty().negate()
                        : detail.getAdjustQty());
                updateDisPresaleAssetsGoodsIn.setActualLowering(adjustOrder.getAdjustType());
                updateDisPresaleAssetsGoodsIn.setOrderQuantity(BigDecimal.ZERO);
                updateDisPresaleAssetsGoodsIn.setPackageSpecificationNum(detail.getPackageSpecificationNum());
                updateDisPresaleAssetsGoodsIn.setLoginUsername(loginUsername);
                updateDisPresaleAssetsGoodsIn.setAssetsId(storeAssetsDetail.getAssetsId());
                return updateDisPresaleAssetsGoodsIn;
            }).collect(Collectors.toList());
            updateDisPresaleAssetsIn.setAssetsGoodsInList(assetsGoodsInList);
            // 修改资产、记录流水
            ordDisPresaleAssetsService.updatePresaleAssets(updateDisPresaleAssetsIn);
            String content = "审核";
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ADJUST_ORDER.getName(),
                    String.valueOf(adjustOrder.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ADJUST_ORDER.getCode(),
                    content, new Date(), UserUtil.getUserName());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return true;
    }

    private static String getAdjustOrderBusinessType(OrdDisPresaleAdjustOrder adjustOrder) {
        String businessType;
        if (ModelConst.DELETE.YES.equals(adjustOrder.getIsChargeOrder())) {
            businessType = OrdDisPresaleAdjustOrderTypeEnum.ADD.getCode().equals(adjustOrder.getAdjustType())
                    ? OrdDisPresaleFlowBusinessTypeEnum.CHARGE_ADJUST_ORDER_ADD.getKey()
                    : OrdDisPresaleFlowBusinessTypeEnum.CHARGE_ADJUST_ORDER_REDUCE.getKey();
        } else {
            businessType = OrdDisPresaleAdjustOrderTypeEnum.ADD.getCode().equals(adjustOrder.getAdjustType())
                    ? OrdDisPresaleFlowBusinessTypeEnum.ADJUST_ORDER_ADD.getKey()
                    : OrdDisPresaleFlowBusinessTypeEnum.ADJUST_ORDER_REDUCE.getKey();
        }
        return businessType;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAdjustOrder(PresaleAdjustOrderSaveIn orderSaveIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        String orderNo = orderSaveIn.getOrderNo();
        if (StringUtils.isBlank(orderNo)) {
            orderSaveIn.setOrderNo(CreateCodeUtil.getOrgOrderNo("KYT", UserUtil.getBizOrgCode(), uniqueUtils, 4));
        }
        // 1.保存时页面数据进行校验：门店代码、商品代码、活动单号在资产模块存在，且仅当调整类型为扣减时---
        //   调整数量小于等于资产余数，校验通过保存成功，校验失败--错误数据红色变红提示；保存成功--状态为待审核
        this.checkPresaleAdjustOrderDetail(orderSaveIn.getDetails().stream().map(adjustDetail -> CheckPresaleAdjustOrderDetailIn.builder()
                .storeCode(orderSaveIn.getStoreCode())
                .adjustType(orderSaveIn.getAdjustType())
                .goodsCode(adjustDetail.getGoodsCode())
                .adjustQty(adjustDetail.getAdjustQty())
                .presaleActivityNo(adjustDetail.getPresaleActivityNo())
                .build()).collect(Collectors.toList()));
        OrdDisPresaleAdjustOrder adjustOrder = new OrdDisPresaleAdjustOrder();
        BeanUtils.copyProperties(orderSaveIn, adjustOrder);
        adjustOrder.setStatus(OrdDisPresaleAdjustOrderStatusEnum.PENDING.getCode());
        adjustOrder.setTotalSkuQty(orderSaveIn.getDetails().size());
        adjustOrder.setAdjustQty(orderSaveIn.getDetails().stream().map(PresaleAdjustOrderSaveIn.PresaleAdjustOrderGoods::getAdjustQty).reduce(BigDecimal.ZERO, BigDecimal::add));
        adjustOrder.setIsCharge(ModelConst.DELETE.NO);
        adjustOrder.setOrgCode(UserUtil.getOrgCode());
        adjustOrder.setBizOrgCode(bizOrgCode);
        adjustOrder.setCreator(UserUtil.getUserName());
        adjustOrder.setCreateTime(LocalDateTime.now());
        adjustOrder.setIsDelete(ModelConst.DELETE.NO);
        // 2.预售调整单信息保存
        this.insertOrEditPresaleAdjustOrder(adjustOrder);
        // 3.预售调整单单明细保存
        this.savePresaleAdjustOrderDetail(adjustOrder.getId(), orderSaveIn);
        return adjustOrder.getId();
    }

    private void checkPresaleAdjustOrderDetail(List<CheckPresaleAdjustOrderDetailIn> checkPresaleAdjustOrderDetails) {
        checkPresaleAdjustOrderDetails.forEach(item -> {
            // 保存时页面数据进行校验：门店代码、商品代码、活动单号在资产模块存在，且仅当调整类型为扣减时---
            // 调整数量小于等于资产余数，校验通过保存成功，校验失败--错误数据红色变红提示；
            OrdDisPresaleAssetsDetail assetsDetail = ordDisPresaleAssetsDetailService.getStoreGoodsAssetsDetail(item.getStoreCode(), item.getGoodsCode(), item.getPresaleActivityNo());
            if (Objects.isNull(assetsDetail)) {
                throw new BusinessException("门店资产商品" + item.getGoodsCode() + "不存在");
            }
            if (OrdDisPresaleAdjustOrderTypeEnum.REDUCE.getCode().equals(item.getAdjustType())) {
                if (item.getAdjustQty().compareTo(assetsDetail.getSurplusQuantity()) > 0) {
                    throw new BusinessException("门店资产商品" + item.getGoodsCode() + "剩余订货量不足");
                }
            }
        });
    }

    private void savePresaleAdjustOrderDetail(Long adjustOrderId, PresaleAdjustOrderSaveIn orderSaveIn) {
        // 1.预售调整单明细删除
        adjustOrderDetailService.deleteByAdjustOrderId(adjustOrderId);
        // 2.预售调整单明细保存
        adjustOrderDetailService.savePresaleAdjustOrderDetail(adjustOrderId, orderSaveIn);
    }

    private void insertOrEditPresaleAdjustOrder(OrdDisPresaleAdjustOrder adjustOrder) {
        OrdDisPresaleAdjustOrder query = this.selectOne(OrdDisPresaleAdjustOrder.builder().orderNo(adjustOrder.getOrderNo()).isDelete(0).build());
        String content = "";
        if (Objects.isNull(query)) {
            content = "新增";
            adjustOrder.setCreator(UserUtil.getUserName());
            adjustOrder.setCreateTime(LocalDateTime.now());
            this.insertSelective(adjustOrder);
        } else {
            content = "编辑";
            adjustOrder.setId(query.getId());
            adjustOrder.setUpdater(UserUtil.getUserName());
            adjustOrder.setUpdateTime(LocalDateTime.now());
            this.updateByPrimaryKeySelective(adjustOrder);
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ADJUST_ORDER.getName(),
                String.valueOf(adjustOrder.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ADJUST_ORDER.getCode(),
                content, new Date(), UserUtil.getUserName());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean chargeAdjustOrder(Long id) {
        // 已审核且未冲销的单据可操作（冲销标识--否，是否冲销单--否’）；
        // 增加类型单据--冲销时（门店预售商品余数减少，流水为调整单冲销-）； 减少类型单据--冲销时（门店预售商品余数增加，流水为调整单冲销+）；
        // 操作‘冲销’时，原单变为已冲销状态；新的冲销单状态为已审核且创建日期及创建人为当前 操作冲销的 日期及账号数据；
        OrdDisPresaleAdjustOrder adjustOrderOld = this.selectByPrimaryKey(id);
        if (!OrdDisPresaleAdjustOrderStatusEnum.APPROVED.getCode().equals(adjustOrderOld.getStatus()) ||
                ModelConst.DELETE.YES.equals(adjustOrderOld.getIsCharge()) || ModelConst.DELETE.YES.equals(adjustOrderOld.getIsChargeOrder())) {
            throw new BusinessException("当前单据状态不允许操作");
        }
        List<OrdDisPresaleAdjustOrderDetail> adjustOrderDetailOld = adjustOrderDetailService.list(OrdDisPresaleAdjustOrderDetail.builder().adjustOrderId(id).build());
        PresaleAdjustOrderSaveIn chargeAdjustOrder = PresaleAdjustOrderSaveIn.builder()
                .storeCode(adjustOrderOld.getStoreCode())
                .storeName(adjustOrderOld.getStoreName())
                .adjustType(OrdDisPresaleAdjustOrderTypeEnum.ADD.getCode().equals(adjustOrderOld.getAdjustType())
                        ? OrdDisPresaleAdjustOrderTypeEnum.REDUCE.getCode() : OrdDisPresaleAdjustOrderTypeEnum.ADD.getCode())  // 调整类型取反
                .sourceNo(adjustOrderOld.getOrderNo())
                .isChargeOrder(ModelConst.DELETE.YES)
                .details(adjustOrderDetailOld.stream().map(detail -> {
                    OrdDisPresaleAssetsDetail assetsDetail = ordDisPresaleAssetsDetailService.getStoreGoodsAssetsDetail(adjustOrderOld.getStoreCode(), detail.getGoodsCode(), detail.getPresaleActivityNo());
                    return PresaleAdjustOrderSaveIn.PresaleAdjustOrderGoods.builder()
                            .goodsCode(detail.getGoodsCode())
                            .goodsName(detail.getGoodsName())
                            .barCode(detail.getBarCode())
                            .adjustQty(detail.getAdjustQty()) // 调整数量取反
                            .beforeQty(assetsDetail.getSurplusQuantity())
                            .goodsType(detail.getGoodsType())
                            .packageUnit(detail.getPackageUnit())
                            .packageSpecification(detail.getPackageSpecification())
                            .packageSpecificationNum(detail.getPackageSpecificationNum())
                            .presaleActivityNo(detail.getPresaleActivityNo())
                            .build();
                }).collect(Collectors.toList())).build();
        // 保存冲销单、审核冲销单
        if (this.approveAdjustOrder(this.saveAdjustOrder(chargeAdjustOrder))) {
            // 冲销单审核通过后，修改原单为已冲销
            adjustOrderOld.setIsCharge(ModelConst.DELETE.YES);
            // adjustOrderOld.setStatus(OrdDisPresaleAdjustOrderStatusEnum.REVERSED.getCode());
            this.updateByPrimaryKeySelective(adjustOrderOld);
            // 记录原单冲销操作日志
            String content = "冲销";
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ADJUST_ORDER.getName(),
                    String.valueOf(adjustOrderOld.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ADJUST_ORDER.getCode(),
                    content, new Date(), UserUtil.getUserName());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return true;
    }

    @Override
    public String exportAdjustOrder(PresaleAdjustOrderPageIn presaleAdjustOrderPageIn) {
        if (StringUtils.isBlank(presaleAdjustOrderPageIn.getBizOrgCode())) {
            presaleAdjustOrderPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        String title = "预售调整单";
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        Page<PresaleAdjustOrderPageOut> resultPage = this.findPresaleAdjustOrderByPage(presaleAdjustOrderPageIn);
        List<ExcelPresaleAdjustOrder> exportAdjustOrderList = parsePresaleAdjustOrderListDataToExcel(resultPage.getList());
        byte[] bytes = FileExportUtil.getFileBytesByData(exportAdjustOrderList, "预售调整单", "预售调整单", ExcelPresaleAdjustOrder.class, true);
        return fileService.uploadFile(fileName, bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    public String exportAdjustOrders(PresaleAdjustOrderPageIn presaleAdjustOrderPageIn) {
        if (StringUtils.isBlank(presaleAdjustOrderPageIn.getBizOrgCode())) {
            presaleAdjustOrderPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        String title = "预售调整单明细";
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        Page<PresaleAdjustOrderPageOut> resultPage = this.findPresaleAdjustOrderByPage(presaleAdjustOrderPageIn);
        List<PresaleAdjustOrderPageOut> list = resultPage.getList();
        List<ExcelPresaleAdjustOrders> exportAdjustOrderDetailList = Lists.newArrayList();
        for (PresaleAdjustOrderPageOut adjustOrder : list) {
            List<OrdDisPresaleAdjustOrderDetail> adjustOrderDetailList = adjustOrderDetailService.list(OrdDisPresaleAdjustOrderDetail.builder().adjustOrderId(adjustOrder.getId()).build());
            if (CollectionUtils.isNotEmpty(adjustOrderDetailList)) {
                adjustOrderDetailList.forEach(detail -> {
                    ExcelPresaleAdjustOrders exportAdjustOrderDetail = new ExcelPresaleAdjustOrders();
                    BeanUtils.copyProperties(detail, exportAdjustOrderDetail);
                    exportAdjustOrderDetail.setOrderNo(adjustOrder.getOrderNo());
                    exportAdjustOrderDetail.setStatusDesc(adjustOrder.getStatusDesc());
                    exportAdjustOrderDetail.setAdjustTypeDesc(adjustOrder.getAdjustTypeDesc());
                    exportAdjustOrderDetail.setStoreCode(adjustOrder.getStoreCode());
                    exportAdjustOrderDetail.setStoreName(adjustOrder.getStoreName());
                    exportAdjustOrderDetail.setIsChargeDesc(Objects.equals(adjustOrder.getIsCharge(), ModelConst.DELETE.YES) ? "是" : "否");
                    exportAdjustOrderDetail.setIsChargeOrderDesc(Objects.equals(adjustOrder.getIsChargeOrder(), ModelConst.DELETE.YES) ? "是" : "否");
                    exportAdjustOrderDetail.setSourceNo(adjustOrder.getSourceNo());
                    exportAdjustOrderDetail.setCreator(adjustOrder.getCreator());
                    exportAdjustOrderDetail.setCreateTime(adjustOrder.getCreateTime());
                    exportAdjustOrderDetail.setApprover(adjustOrder.getApprover());
                    exportAdjustOrderDetail.setApprovalTime(adjustOrder.getApprovalTime());
                    exportAdjustOrderDetail.setRemark(adjustOrder.getRemark());
                    exportAdjustOrderDetailList.add(exportAdjustOrderDetail);
                });
            }
        }
        byte[] bytes = FileExportUtil.getFileBytesByData(exportAdjustOrderDetailList, "预售调整单明细", "预售调整单明细", ExcelPresaleAdjustOrders.class, true);
        return fileService.uploadFile(fileName, bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    private List<ExcelPresaleAdjustOrder> parsePresaleAdjustOrderListDataToExcel(List<PresaleAdjustOrderPageOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertPresaleAdjustOrderListExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    private ExcelPresaleAdjustOrder convertPresaleAdjustOrderListExcel(PresaleAdjustOrderPageOut presaleAdjustOrderPageOut, int index) {
        ExcelPresaleAdjustOrder adjustOrder = new ExcelPresaleAdjustOrder();
        BeanUtils.copyProperties(presaleAdjustOrderPageOut, adjustOrder);
        adjustOrder.setIsChargeDesc(Objects.equals(presaleAdjustOrderPageOut.getIsCharge(), ModelConst.DELETE.YES) ? "是" : "否");
        adjustOrder.setIsChargeOrderDesc(Objects.equals(presaleAdjustOrderPageOut.getIsChargeOrder(), ModelConst.DELETE.YES) ? "是" : "否");
        adjustOrder.setIndex(index + 1);
        return adjustOrder;
    }

    @Override
    public String exportAdjustOrderDetail(Long id) {
        String title = "预售调整单明细";
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        List<OrdDisPresaleAdjustOrderDetail> adjustOrderDetailList = adjustOrderDetailService.list(OrdDisPresaleAdjustOrderDetail.builder().adjustOrderId(id).build());
        List<ExcelPresaleAdjustOrderDetail> exportAdjustOrderDetailList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(adjustOrderDetailList)) {
            adjustOrderDetailList.forEach(detail -> {
                ExcelPresaleAdjustOrderDetail exportAdjustOrderDetail = new ExcelPresaleAdjustOrderDetail();
                BeanUtils.copyProperties(detail, exportAdjustOrderDetail);
                exportAdjustOrderDetailList.add(exportAdjustOrderDetail);
            });
        }
        byte[] bytes = FileExportUtil.getFileBytesByData(exportAdjustOrderDetailList, "预售调整单明细", "预售调整单明细", ExcelPresaleAdjustOrderDetail.class, true);
        return fileService.uploadFile(fileName, bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    public List<OrdDisPresaleAssetsDetailOut> getStoreGoodsAssetsDetailList(String storeCode, String goodsCode) {
        List<OrdDisPresaleAssetsDetail> assetsDetails = ordDisPresaleAssetsDetailService.getStoreGoodsAssetsDetailList(storeCode, goodsCode);
        return assetsDetails.stream().map(e -> {
            OrdDisPresaleAssetsDetailOut detail = new OrdDisPresaleAssetsDetailOut();
            BeanUtils.copyProperties(e, detail);
            detail.setStatusStr(OrdDisPresaleAssetsStatusEnum.getValueByKey(e.getStatus()));
            return detail;
        }).collect(Collectors.toList());
    }

    @Override
    public Response<String> importAdjustOrder(String fileId) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        PresaleAdjustOrderImportListener listener = new PresaleAdjustOrderImportListener(ordDisPresaleAssetsDetailService);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportPresaleAdjustOrder.class, listener).headRowNumber(NumberUtil.INTEGER_ONE).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        if (CollectionUtils.isNotEmpty(listener.getErrorDate())) {
            return Response.error(String.join("\n", listener.getErrorDate()));
        }
        this.handleAdjustOrderImport(listener.getPresaleAdjustOrders());
        return Response.success("导入成功");
    }

    @Override
    public Response<List<PresaleAdjustOrderDetailOut>> importAdjustOrderDetail(String storeCode, String adjustType, String fileId) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (null == bytes) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        PresaleAdjustOrderDetailImportListener listener = new PresaleAdjustOrderDetailImportListener(ordDisPresaleAssetsDetailService, storeCode, adjustType);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportPresaleAdjustOrderDetail.class, listener).headRowNumber(NumberUtil.INTEGER_ONE).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        return Response.data(listener.getAdjustOrderDetailOuts(), listener.message());
    }

    /**
     * 导入预售调整单
     *
     * @param presaleAdjustOrders
     */
    private void handleAdjustOrderImport(List<ImportPresaleAdjustOrderOut> presaleAdjustOrders) {
        // String loginUsername = UserUtil.getUserName();
        try {
            // 根据storeCode、adjustType分组
            Map<String, List<ImportPresaleAdjustOrderOut>> adjustOrderGroupMap = presaleAdjustOrders.stream().collect(Collectors.groupingBy(o -> o.getStoreCode() + "_" + o.getAdjustType()));
            // List<ImportPresaleAdjustOrderErrorResult> errorResultList = Lists.newArrayList();
            adjustOrderGroupMap.forEach((k, v) -> {
                String storeCode = k.split("_")[0];
                String adjustType = k.split("_")[1];
                String bizOrgCode = UserUtil.getBizOrgCode();
                StoreInfo storeInfo = storeCenterService.getStoreByCode(storeCode, bizOrgCode);
                PresaleAdjustOrderSaveIn orderSaveIn = PresaleAdjustOrderSaveIn.builder()
                        .storeCode(storeCode)
                        .storeName(storeInfo.getStoreName())
                        .adjustType(adjustType)
                        .details(Lists.newArrayList())
                        .build();
                for (ImportPresaleAdjustOrderOut item : v) {
                    orderSaveIn.getDetails().add(PresaleAdjustOrderSaveIn.PresaleAdjustOrderGoods.builder()
                            .goodsCode(item.getGoodsCode())
                            .goodsName(item.getGoodsName())
                            .barCode(item.getBarCode())
                            .adjustQty(item.getAdjustQty())
                            .beforeQty(item.getSurplusQuantity())
                            .packageUnit(item.getPackageUnit())
                            .packageSpecification(item.getPackageSpecification())
                            .packageSpecificationNum(item.getPackageSpecificationNum())
                            .presaleActivityNo(item.getPresaleActivityNo())
                            .build());
                }
                // 调用预售调整单保存接口
                this.saveAdjustOrder(orderSaveIn);
            });
            // if (CollectionUtils.isEmpty(errorResultList)) {
            //     return;
            // }
            // String sheetName = "预售调整单导入问题清单";
            // String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            // asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, errorResultList, ImportPresaleAdjustOrderErrorResult.class);
        } catch (Exception e) {
            log.error("预售调整单导入异常", e);
            // ImportPresaleAdjustOrderErrorResult errorResult = new ImportPresaleAdjustOrderErrorResult();
            // errorResult.setGoodsCode(SystemConstant.SHORT_LINE);
            // errorResult.setErrorMessage("商品配置异常，请检查商品配置");
            // String sheetName = "预售调整单导入异常";
            // String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            // asyncExportHandle.asyncExport(fileName, sheetName, loginUsername, Collections.singletonList(errorResult), ImportPresaleAdjustOrderErrorResult.class);
        }
    }
}
