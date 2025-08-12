package com.edc.erp.directly.returnorder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.ExpiryCheckUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import com.edc.erp.directly.returnorder.entity.OrdDirReturnImage;
import com.edc.erp.directly.returnorder.enumeration.OrdReturnOrderStatusEnum;
import com.edc.erp.directly.returnorder.enumeration.OrdReturnOrderTypeEnum;
import com.edc.erp.directly.returnorder.mapper.OrdDirReturnDetailMapper;
import com.edc.erp.directly.returnorder.mapper.OrdDirReturnImageMapper;
import com.edc.erp.directly.returnorder.mapper.OrdDirReturnMapper;
import com.edc.erp.directly.returnorder.model.excel.ExportOrdDirReturnDetail;
import com.edc.erp.directly.returnorder.model.in.AppSaveOrdDirReturnDetailIn;
import com.edc.erp.directly.returnorder.model.in.ImportOrdReturnOrderVO;
import com.edc.erp.directly.returnorder.model.in.OrdDirReturnDetailIn;
import com.edc.erp.directly.returnorder.model.in.OrdSaveReturnOrderIn;
import com.edc.erp.directly.returnorder.model.out.DirReturnOrderDtlPrintOut;
import com.edc.erp.directly.returnorder.model.out.OrdDirReturnDetailOut;
import com.edc.erp.directly.returnorder.model.out.OrdReturnDetailOut;
import com.edc.erp.directly.returnorder.service.OrdDirReturnDetailService;
import com.edc.erp.directly.util.FileExportUtil;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dictionary.service.SystemDictService;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 退货单详情表(OrdDirReturnDetail)表服务实现类
 *
 * @author
 * @since 2022-11-18 18:50:07
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDirReturnDetailServiceImpl extends BaseServiceImpl<OrdDirReturnDetail> implements OrdDirReturnDetailService {

    private final OrdDirReturnDetailMapper ordDirReturnDetailMapper;

    private final FileService fileService;

    private final WarehouseServer warehouseServer;

    private final OrdDirReturnMapper ordDirReturnMapper;

    private final AsyncLogService asyncLogService;

    private final OrderGoodsServer orderGoodsServer;

    private final UniqueUtils uniqueUtils;

    private final SystemDictService systemDictService;

    private final StoreChannelHandle storeChannelHandle;

    private final StockServer stockServer;

    private final OrdDirReturnImageMapper ordDirReturnImageMapper;

    /**
     * 批量添加明细
     *
     * @param returnDetails
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdDirReturnDetail> returnDetails) {
        ordDirReturnDetailMapper.batchSave(returnDetails);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByReturnOrderId(OrdDirReturn ordDirReturn) {
        OrdDirReturnDetail query = new OrdDirReturnDetail();
        query.setReturnOrderId(ordDirReturn.getId());
        ordDirReturnDetailMapper.delete(query);
    }

    /**
     * 查退货通知单明细
     *
     * @param pageIn
     * @return
     */
    @Override
    public List<OrdDirReturnDetailOut> finaOrdReturnDetail(OrdDirReturnDetailIn pageIn) {

        return ordDirReturnDetailMapper.finaOrdReturnDetail(pageIn);
    }

    /**
     * 批量修改
     *
     * @param ordDirReturnDetailList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdate(List<OrdDirReturnDetail> ordDirReturnDetailList) {
        return ordDirReturnDetailMapper.batchUpdate(ordDirReturnDetailList);
    }

    /**
     * 导出退货明细
     *
     * @param pageIn
     * @return
     */
    @Override
    public String export(OrdDirReturnDetailIn pageIn) {
        List<OrdReturnDetailOut> ordReturnDetailOutList = this.finaOrdReturnDetailList(pageIn);
        List<ExportOrdDirReturnDetail> exportOrdDirReturnDetails = parseDataToExcel(ordReturnDetailOutList);
        String title = "退货单明细信息";
        byte[] byts = FileExportUtil.getFileBytesByData(exportOrdDirReturnDetails,
                title, title, ExportOrdDirReturnDetail.class, true);
        return fileService.uploadFile(title + ".xlsx", byts, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    /**
     * 根据退货单id查退货明细
     *
     * @param item
     * @return
     */
    @Override
    public List<OrdDirReturnDetail> findByReturnOrderId(Integer item) {
        OrdDirReturnDetail ordDisReturnDetail = new OrdDirReturnDetail();
        ordDisReturnDetail.setReturnOrderId(item);
        ordDisReturnDetail.setIsDelete(NumberUtil.INTEGER_ZERO);
        return ordDirReturnDetailMapper.select(ordDisReturnDetail);
    }

    @Override
    public List<OrdReturnDetailOut> finaOrdReturnDetailList(OrdDirReturnDetailIn ordDirReturnDetailInl) {
        List<OrdReturnDetailOut> detailOutList = ordDirReturnDetailMapper.finaOrdReturnDetailList(ordDirReturnDetailInl);
        return detailOutList;
    }

    @Override
    public void initReturnOrderDetailMap(Map<String, List<AppSaveOrdDirReturnDetailIn>> returnOrderDetailMap, StoreOut storeOut,
                                         AppSaveOrdDirReturnDetailIn appSaveOrdDirReturnDetailIn, OrderGoodsOut orderGoodsOut, OrdDirReturn ordDirReturn) {
        AppSaveOrdDirReturnDetailIn returnOrderDetail = this.initReturnOrderDetail(storeOut, appSaveOrdDirReturnDetailIn, orderGoodsOut, ordDirReturn);
        String key = ordDirReturn.getStockCode() + SystemConstant.SHORT_LINE + orderGoodsOut.getDistributionWay();
        List<AppSaveOrdDirReturnDetailIn> ordDirReturnDetailList = returnOrderDetailMap.get(key);
        if (CollectionUtils.isEmpty(ordDirReturnDetailList)) {
            ordDirReturnDetailList = new ArrayList<>();
        }
        ordDirReturnDetailList.add(returnOrderDetail);
        returnOrderDetailMap.put(key, ordDirReturnDetailList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReturnOrderDetail(OrdDirReturnDetail returnOrderDetail) {
        ordDirReturnDetailMapper.insert(returnOrderDetail);
    }

    @Override
    public AppSaveOrdDirReturnDetailIn initReturnOrderDetail(StoreOut storeOut, AppSaveOrdDirReturnDetailIn returnGoodsInfoIn, OrderGoodsOut orderGoodsOut, OrdDirReturn ordDirReturn) {
        AppSaveOrdDirReturnDetailIn ordDirReturnDetail = new AppSaveOrdDirReturnDetailIn();
        ordDirReturnDetail.setGoodsCode(orderGoodsOut.getGoodsCode());
        ordDirReturnDetail.setOrgGoodsId(orderGoodsOut.getOrgGoodsId());
        ordDirReturnDetail.setBarCode(orderGoodsOut.getBarCode());
        ordDirReturnDetail.setApplyReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
//          ordDirReturnDetail.setAuditReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
//          ordDirReturnDetail.setActualReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
        ordDirReturnDetail.setGoodsName(orderGoodsOut.getGoodsName());
        ordDirReturnDetail.setGoodsType(orderGoodsOut.getGoodsType());
        ordDirReturnDetail.setVendorCode(orderGoodsOut.getVendorCode());
        ordDirReturnDetail.setSellTax(orderGoodsOut.getOutTax());

        if (null != (orderGoodsOut.getDistributionSpecification())) {
            ordDirReturnDetail.setDistributionSpecificationNum(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()));
            ordDirReturnDetail.setDistributionSpecification(orderGoodsOut.getDistributionSpecification().getQpcStr());
            ordDirReturnDetail.setDistributionSpecificationUnit(orderGoodsOut.getDistributionSpecification().getUnitName());
            ordDirReturnDetail.setApplyPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
//               ordDirReturnDetail.setActualPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
//               ordDirReturnDetail.setAuditPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
            ordDirReturnDetail.setDistributionSpecificationNum(Objects.isNull(orderGoodsOut.getDistributionSpecification().getQpc()) ? BigDecimal.ZERO : BigDecimal.valueOf(orderGoodsOut.getDistributionSpecification().getQpc()));
        }
        ordDirReturnDetail.setGoodsImage(orderGoodsOut.getImgUrl());
        ordDirReturnDetail.setReturnReason(returnGoodsInfoIn.getReturnReason());
        ordDirReturnDetail.setIsGiftReturn(null);
        BigDecimal storeStockPrice = warehouseServer.getStockPrice(storeOut.getStoreCode(), orderGoodsOut.getGoodsCode(), ordDirReturn.getBizOrgCode());
        ordDirReturnDetail.setReturnUnitPrice(storeStockPrice);
        if (Objects.isNull(storeStockPrice)) {
            log.error("统配退货单{}商品{}门店库存价为空", ordDirReturn.getReturnOrderNo(), ordDirReturnDetail.getGoodsCode());
            throw new BusinessException("统配退货单" + ordDirReturn.getReturnOrderNo() + "商品" + ordDirReturnDetail.getGoodsCode() + "门店库存价为空");
        }
        ordDirReturnDetail.setApplyReturnAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        if (null != orderGoodsOut.getDistributionUnitPrice()) {
//            ordDirReturnDetail.setReturnUnitPrice(orderGoodsOut.getDistributionUnitPrice());
            ordDirReturnDetail.setDistributionPrice(orderGoodsOut.getDistributionUnitPrice());
//               ordDirReturnDetail.setActualReturnAmount(orderGoodsOut.getDistributionUnitPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//               ordDirReturnDetail.setAuditReturnAmount(orderGoodsOut.getDistributionUnitPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        }
        BigDecimal sellTax = ordDirReturnDetail.getSellTax() == null ? BigDecimal.ZERO : ordDirReturnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
        //1+税率
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        //最新门店库存价
        ordDirReturnDetail.setStoreStockPrice(Objects.isNull(storeStockPrice) ? BigDecimal.ZERO : storeStockPrice);
        //最新仓储库存价
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDirReturn.getStockCode());
        BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(ordDirReturn.getWrhCode(), ordDirReturn.getStockCode(),
                returnGoodsInfoIn.getGoodsCode(), stockInfoOut.getBizOrgCode(), storeOut.getStoreCode(), ordDirReturn.getBizOrgCode());
        if (Objects.isNull(warehousePrice)) {
            log.error("统配退货单{}商品{}仓储库存价为空", ordDirReturn.getReturnOrderNo(), ordDirReturnDetail.getGoodsCode());
            throw new BusinessException("统配退货单" + ordDirReturn.getReturnOrderNo() + "商品" + ordDirReturnDetail.getGoodsCode() + "仓储库存价为空");
        }
        ordDirReturnDetail.setWrhPrice(warehousePrice);

        //退货去税金额
        ordDirReturnDetail.setReturnExceptTaxAmount(ordDirReturnDetail.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //退货税额
        ordDirReturnDetail.setReturnTaxAmount(ordDirReturnDetail.getApplyReturnAmount().subtract(ordDirReturnDetail.getReturnExceptTaxAmount()));
        //仓储成本金额
        ordDirReturnDetail.setWrhCostAmount(warehousePrice.multiply(ordDirReturnDetail.getApplyReturnQuantity()));
        //仓储成本去税金额
        ordDirReturnDetail.setWrhExceptTaxAmount(ordDirReturnDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //仓储成本税额
        ordDirReturnDetail.setWrhTaxAmount(ordDirReturnDetail.getWrhCostAmount().subtract(ordDirReturnDetail.getWrhExceptTaxAmount()));
        //门店成本金额
        ordDirReturnDetail.setStoreCostAmount(ordDirReturnDetail.getStoreStockPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()));
        //门店成本去税金额
        ordDirReturnDetail.setStoreExceptTaxAmount(ordDirReturnDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //门店成本税额
        ordDirReturnDetail.setStoreTaxAmount(ordDirReturnDetail.getStoreCostAmount().subtract(ordDirReturnDetail.getStoreExceptTaxAmount()));

        ordDirReturnDetail.setCreator(storeOut.getStoreCode());
        ordDirReturnDetail.setCreateTime(LocalDateTime.now());
        ordDirReturnDetail.setUpdater(storeOut.getStoreCode());
        ordDirReturnDetail.setUpdateTime(LocalDateTime.now());
        ordDirReturnDetail.setIsDelete(ModelConst.DELETE.NO);
        ordDirReturnDetail.setInvoiceType(orderGoodsOut.getInvoiceType());
        ordDirReturnDetail.setImageUrlList(returnGoodsInfoIn.getImageUrlList());
//        if (NumberUtil.INTEGER_ONE.equals(orderGoodsOut.getIsManageValidityPeriod()) && StringUtils.isBlank(returnGoodsInfoIn.getExpiry())) {
//            throw new BusinessException("统配退货单" + ordDirReturn.getReturnOrderNo() + "效期商品" + ordDirReturnDetail.getGoodsCode() + "效期码为空");
//        }
        ordDirReturnDetail.setExpiry(returnGoodsInfoIn.getExpiry());
        return ordDirReturnDetail;
    }

    @Override
    public OrdDirReturnDetail getReturnOrderDetailById(Integer returnOrderDetailId) {
        return ordDirReturnDetailMapper.selectByPrimaryKey(returnOrderDetailId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateReturnOrderDetail(OrdDirReturnDetail ordDisReturnDetail) {
        if (null == ordDisReturnDetail.getId()) {
            ordDirReturnDetailMapper.insert(ordDisReturnDetail);
        } else {
            ordDirReturnDetailMapper.updateByPrimaryKey(ordDisReturnDetail);
        }
    }

    /**
     * 根据退货单明细主键删除一个明细
     *
     * @param returnOrderDetailId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReturnOrderDetailById(Integer returnOrderDetailId) {
        ordDirReturnDetailMapper.deleteByPrimaryKey(returnOrderDetailId);
        OrdDirReturnImage delOrdDirReturnImage = new OrdDirReturnImage();
        delOrdDirReturnImage.setReturnDetailId(returnOrderDetailId);
        ordDirReturnImageMapper.delete(delOrdDirReturnImage);
    }

    /**
     * 实际品项数
     *
     * @param returnOrderId
     * @return
     */
    @Override
    public Integer countActualReturnSkuQuantity(Integer returnOrderId) {
        return ordDirReturnDetailMapper.countActualReturnSkuQuantity(returnOrderId);
    }

    @Override
    public List<DirReturnOrderDtlPrintOut> findPrintDtlByReturnId(Long returnOrderId) {
        return ordDirReturnDetailMapper.findPrintDtlByReturnId(returnOrderId);
    }

    /**
     * 申请总数量
     *
     * @param returnOrderId
     * @return
     */
    @Override
    public BigDecimal sumApplyReturnQuantity(Integer returnOrderId) {
        return ordDirReturnDetailMapper.sumApplyReturnQuantity(returnOrderId);
    }

    /**
     * 统计申请退货商品品项数
     *
     * @param returnOrderId
     * @return
     */
    @Override
    public Integer countApplyReturnSkuQuantity(Integer returnOrderId) {
        return ordDirReturnDetailMapper.countApplyReturnSkuQuantity(returnOrderId);
    }

    /**
     * 统计实际退货商品总数量
     *
     * @param returnOrderId
     * @return
     */
    @Override
    public BigDecimal sumActualReturnQuantity(Integer returnOrderId) {
        return ordDirReturnDetailMapper.sumActualReturnQuantity(returnOrderId);
    }

    @Override
    public OrdDirReturnDetail initDetail(ImportOrdReturnOrderVO importOrdReturnOrderVO, String bizOrgCode, OrderGoodsOut orderGoods) {
        OrdDirReturnDetail ordDirReturnDetail = new OrdDirReturnDetail();
        ordDirReturnDetail.setGoodsCode(importOrdReturnOrderVO.getGoodsCode());
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(importOrdReturnOrderVO.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setStoreCode(importOrdReturnOrderVO.getStoreCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
        if (Objects.isNull(orderGoods)) {
            throw new BusinessException("商品不存在或者不允许配货退货");
        }
        ordDirReturnDetail.setGoodsName(orderGoods.getGoodsName());
        ordDirReturnDetail.setBarCode(orderGoods.getBarCode());
        ordDirReturnDetail.setOrgGoodsId(orderGoods.getOrgGoodsId());
        ordDirReturnDetail.setGoodsType(orderGoods.getGoodsType());
        ordDirReturnDetail.setRemark(importOrdReturnOrderVO.getRemark());
        ordDirReturnDetail.setVendorCode(orderGoods.getVendorCode());
        ordDirReturnDetail.setDistributionSpecification(Objects.isNull(orderGoods.getDistributionSpecification()) ? "" : orderGoods.getDistributionSpecification().getQpcStr());
        ordDirReturnDetail.setDistributionSpecificationUnit(Objects.isNull(orderGoods.getDistributionSpecification()) ? "" : orderGoods.getDistributionSpecification().getUnitName());
        ordDirReturnDetail.setDistributionSpecificationNum(Objects.isNull(orderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : BigDecimal.valueOf(orderGoods.getDistributionSpecification().getQpc()));
        BigDecimal stockPrice = warehouseServer.getStockPrice(importOrdReturnOrderVO.getStoreCode(), importOrdReturnOrderVO.getGoodsCode(), bizOrgCode);
        ordDirReturnDetail.setReturnUnitPrice(Objects.isNull(importOrdReturnOrderVO.getReturnUnitPrice()) ? stockPrice : importOrdReturnOrderVO.getReturnUnitPrice());
        ordDirReturnDetail.setDistributionPrice(orderGoods.getDistributionUnitPrice());
        //申请
        ordDirReturnDetail.setApplyReturnQuantity(importOrdReturnOrderVO.getApplyReturnQuantity());
        ordDirReturnDetail.setApplyPackageQuantity(ordDirReturnDetail.getApplyReturnQuantity().divide(new BigDecimal(orderGoods.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        ordDirReturnDetail.setApplyReturnAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(importOrdReturnOrderVO.getApplyReturnQuantity()));
//        //审核
//        ordDirReturnDetail.setAuditReturnQuantity(ordDirReturnDetail.getApplyReturnQuantity());
//        ordDirReturnDetail.setAuditPackageQuantity(ordDirReturnDetail.getApplyPackageQuantity());
//        ordDirReturnDetail.setAuditReturnAmount(ordDirReturnDetail.getApplyReturnAmount());
//        //实际
//        ordDirReturnDetail.setActualReturnQuantity(ordDirReturnDetail.getApplyReturnQuantity());
//        ordDirReturnDetail.setActualPackageQuantity(ordDirReturnDetail.getApplyPackageQuantity());
//        ordDirReturnDetail.setActualReturnAmount(ordDirReturnDetail.getApplyReturnAmount());
        ordDirReturnDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
        BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(importOrdReturnOrderVO.getWarehouseCode(), importOrdReturnOrderVO.getStockCode(),
                importOrdReturnOrderVO.getGoodsCode(), importOrdReturnOrderVO.getCenterStockBizOrgCode(), importOrdReturnOrderVO.getStoreCode(), bizOrgCode);
        if (Objects.isNull(warehousePrice)) {
            log.error("统配退货单导入{}商品{}仓储库存价为空", ordDirReturnDetail.getGoodsCode());
            throw new BusinessException("统配退货单导入" + ordDirReturnDetail.getGoodsCode() + "仓储库存价为空");
        }
        ordDirReturnDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);
        ordDirReturnDetail.setSellTax(orderGoods.getOutTax());
        BigDecimal sellTax = ordDirReturnDetail.getSellTax() == null ? BigDecimal.ZERO : ordDirReturnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
        //1+税率
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        //退货去税金额
        ordDirReturnDetail.setReturnExceptTaxAmount(ordDirReturnDetail.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //退货税额
        ordDirReturnDetail.setReturnTaxAmount(ordDirReturnDetail.getApplyReturnAmount().subtract(ordDirReturnDetail.getReturnExceptTaxAmount()));
        //仓储成本金额
        ordDirReturnDetail.setWrhCostAmount(warehousePrice.multiply(ordDirReturnDetail.getApplyReturnQuantity()));
        //仓储成本去税金额
        ordDirReturnDetail.setWrhExceptTaxAmount(ordDirReturnDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //仓储成本税额
        ordDirReturnDetail.setWrhTaxAmount(ordDirReturnDetail.getWrhCostAmount().subtract(ordDirReturnDetail.getWrhExceptTaxAmount()));
        //门店成本金额
        ordDirReturnDetail.setStoreCostAmount(ordDirReturnDetail.getStoreStockPrice().multiply(ordDirReturnDetail.getApplyReturnQuantity()));
        //门店成本去税金额
        ordDirReturnDetail.setStoreExceptTaxAmount(ordDirReturnDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //门店成本税额
        ordDirReturnDetail.setStoreTaxAmount(ordDirReturnDetail.getStoreCostAmount().subtract(ordDirReturnDetail.getStoreExceptTaxAmount()));
        ordDirReturnDetail.setInvoiceType(orderGoods.getInvoiceType());
        return ordDirReturnDetail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAsyncImportReturn(List<OrdSaveReturnOrderIn> returnOrderInList) {
        returnOrderInList.forEach(saveReturnOrderIn -> {
            //保存退货单
            OrdDirReturn ordDirReturn = new OrdDirReturn();
            BeanUtil.copyProperties(saveReturnOrderIn, ordDirReturn, CopyOptions.create().setIgnoreNullValue(true));
            String no = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PT.getCode(), saveReturnOrderIn.getBizOrgCode(), uniqueUtils, 4);
            ordDirReturn.setReturnOrderNo(no);
            ordDirReturn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
            ordDirReturn.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(saveReturnOrderIn.getBizOrgCode()));
            ordDirReturn.setSubmitTime(LocalDateTime.now());
            ordDirReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
            ordDirReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
            ordDirReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
            ordDirReturn.setIsDelete(NumberUtil.INTEGER_ZERO);
            ordDirReturn.setBizOrgCode(saveReturnOrderIn.getBizOrgCode());
            ordDirReturn.setWrhCode(saveReturnOrderIn.getWarehouseCode());
            ordDirReturn.setSkuCount(saveReturnOrderIn.getReturnGoodsInfoInList().size());
            ordDirReturn.setDistributionType(saveReturnOrderIn.getDistributionType());
            ordDirReturn.setCreator(saveReturnOrderIn.getLoginUsername());
            ordDirReturn.setUpdater(saveReturnOrderIn.getLoginUsername());
            int count = ordDirReturnMapper.insertSelective(ordDirReturn);
            //保存明细及
            if (count > 0) {
                this.handleReturnDetail(ordDirReturn, saveReturnOrderIn);
            }
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_RETURN.getName(), String.valueOf(ordDirReturn.getId()),
                    OrdLogTypeEnum.ORD_DIR_RETURN.getCode(), OrdLogTypeEnum.ORD_DIR_RETURN_SAVE.getName(), new Date(), ordDirReturn.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReturnDetail(OrdDirReturn ordDirReturn, OrdSaveReturnOrderIn saveReturnOrderIn) {
        List<OrdDirReturnDetail> returnGoodsInfoInList = saveReturnOrderIn.getReturnGoodsInfoInList();
//        List<OrdDirReturnDetail> list = new ArrayList<>();
        BigDecimal applyReturnQuantity = BigDecimal.ZERO;
        BigDecimal applyReturnAmount = BigDecimal.ZERO;
        BigDecimal auditReturnQuantity = BigDecimal.ZERO;
        BigDecimal auditReturnAmount = BigDecimal.ZERO;
        BigDecimal actualReturnQuantity = BigDecimal.ZERO;
        BigDecimal actualReturnAmount = BigDecimal.ZERO;
        OrdDirReturnImage ordDirReturnImage = new OrdDirReturnImage();
        ordDirReturnImage.setReturnOrderId(ordDirReturn.getId());
        List<OrdDirReturnImage> imageList = ordDirReturnImageMapper.select(ordDirReturnImage);
        Map<Integer, List<OrdDirReturnImage>> imageMap = null;
        if (CollectionUtils.isNotEmpty(imageList)) {
            ordDirReturnImageMapper.delete(ordDirReturnImage);
            imageMap = imageList.stream().collect(Collectors.groupingBy(OrdDirReturnImage::getReturnDetailId));
        }
        int line = 1;
        for (OrdDirReturnDetail returnGoodsInfoIn : returnGoodsInfoInList) {
            if (StringUtils.isBlank(saveReturnOrderIn.getDeliveryOrderNo()) && StringUtils.isNotBlank(returnGoodsInfoIn.getExpiry())) {
//                if (NumberUtil.INTEGER_ONE.equals(returnDetailOut.getIsManageValidityPeriod()) && StringUtils.isBlank(returnGoodsInfoIn.getExpiry())) {
//                    throw new BusinessException("统配退货单" + ordDisReturn.getReturnOrderNo() + "效期商品" + returnGoodsInfoIn.getGoodsCode() + "效期码为空");
//                }
                Response<LocalDateTime> expiryResponse = ExpiryCheckUtil.checkExpiry(returnGoodsInfoIn.getExpiry());
                if (!expiryResponse.isSuccess()) {
                    throw new BusinessException("统配退货单" + ordDirReturn.getReturnOrderNo() + "效期商品" + returnGoodsInfoIn.getGoodsCode() + expiryResponse.getMessage());
                }
            }
            OrdReturnDetailOut returnDetailOut = this.compute(returnGoodsInfoIn, ordDirReturn.getStoreCode(),
                    ordDirReturn.getBizOrgCode(), ordDirReturn.getWrhCode(), ordDirReturn.getStockCode(), saveReturnOrderIn.getCenterStockBizOrgCode());
            OrdDirReturnDetail ordDirReturnDetail = new OrdDirReturnDetail();
            BeanUtils.copy(returnDetailOut, ordDirReturnDetail);
            ordDirReturnDetail.setId(null);
            ordDirReturnDetail.setReturnOrderId(ordDirReturn.getId());
            ordDirReturnDetail.setLine(line);
            ordDirReturnDetail.setIsDelete(NumberUtil.INTEGER_ZERO);
            ordDirReturnDetail.setCreator(ordDirReturn.getCreator());
            ordDirReturnDetail.setUpdater(ordDirReturn.getUpdater());
            ordDirReturnDetail.setCreateTime(LocalDateTime.now());
            ordDirReturnDetail.setUpdateTime(LocalDateTime.now());

            BigDecimal stockPrice = warehouseServer.getStockPrice(ordDirReturn.getStoreCode(), returnGoodsInfoIn.getGoodsCode(), ordDirReturn.getBizOrgCode());
            if (Objects.isNull(stockPrice)) {
                log.error("统配退货单{}商品{}门店库存价为空", ordDirReturn.getReturnOrderNo(), ordDirReturnDetail.getGoodsCode());
                throw new BusinessException("统配退货单" + ordDirReturn.getReturnOrderNo() + "商品" + ordDirReturnDetail.getGoodsCode() + "门店库存价为空");
            }
            //最新门店库存价
            ordDirReturnDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
            BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(ordDirReturn.getWrhCode(), ordDirReturn.getStockCode(),
                    returnGoodsInfoIn.getGoodsCode(), saveReturnOrderIn.getCenterStockBizOrgCode(), ordDirReturn.getStoreCode(), ordDirReturn.getBizOrgCode());
            if (Objects.isNull(warehousePrice)) {
                log.error("统配退货单{}商品{}仓储库存价为空", ordDirReturn.getReturnOrderNo(), ordDirReturnDetail.getGoodsCode());
                throw new BusinessException("统配退货单" + ordDirReturn.getReturnOrderNo() + "商品" + ordDirReturnDetail.getGoodsCode() + "仓储库存价为空");
            }
            //最新仓储库存价
            ordDirReturnDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);
            //退货税额
//            ordDirReturnDetail.setReturnTaxAmount(ordDirReturnDetail.getApplyReturnAmount().subtract(ordDirReturnDetail.getReturnExceptTaxAmount()));
            applyReturnQuantity = applyReturnQuantity.add(Objects.isNull(returnGoodsInfoIn.getApplyReturnQuantity()) ? BigDecimal.ZERO : returnGoodsInfoIn.getApplyReturnQuantity());
            applyReturnAmount = applyReturnAmount.add(Objects.isNull(returnGoodsInfoIn.getApplyReturnAmount()) ? BigDecimal.ZERO : returnGoodsInfoIn.getApplyReturnAmount());
            auditReturnQuantity = auditReturnQuantity.add(Objects.isNull(returnGoodsInfoIn.getAuditReturnQuantity()) ? BigDecimal.ZERO : returnGoodsInfoIn.getAuditReturnQuantity());
            auditReturnAmount = auditReturnAmount.add(Objects.isNull(returnGoodsInfoIn.getAuditReturnAmount()) ? BigDecimal.ZERO : returnGoodsInfoIn.getAuditReturnAmount());
            actualReturnQuantity = actualReturnQuantity.add(Objects.isNull(returnGoodsInfoIn.getActualReturnQuantity()) ? BigDecimal.ZERO : returnGoodsInfoIn.getActualReturnQuantity());
            actualReturnAmount = actualReturnAmount.add(Objects.isNull(returnGoodsInfoIn.getActualReturnAmount()) ? BigDecimal.ZERO : returnGoodsInfoIn.getActualReturnAmount());
            ordDirReturnDetailMapper.insert(ordDirReturnDetail);
            //  处理上传图片
            if (null != imageMap && imageMap.size() > 0) {
                List<OrdDirReturnImage> ordDirReturnImageList = imageMap.get(returnGoodsInfoIn.getId());
                if (CollectionUtils.isNotEmpty(ordDirReturnImageList)) {
                    ordDirReturnImageList.forEach(updateDirReturnImage -> {
                        updateDirReturnImage.setReturnDetailId(ordDirReturnDetail.getId());
                        updateDirReturnImage.setId(null);
                        updateDirReturnImage.setCreateTime(null);
                        ordDirReturnImageMapper.insert(updateDirReturnImage);
                    });
                }
            }
//            list.add(ordDirReturnDetail);
            line++;
        }

//        this.batchSave(list);
        ordDirReturn.setSkuCount(returnGoodsInfoInList.size());
        ordDirReturn.setApplyReturnQuantity(applyReturnQuantity);
        ordDirReturn.setApplyReturnAmount(applyReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDirReturn.setAuditReturnQuantity(auditReturnQuantity);
        ordDirReturn.setAuditReturnAmount(auditReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDirReturn.setActualReturnQuantity(actualReturnQuantity);
        ordDirReturn.setActualReturnAmount(actualReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDirReturn.setDeliveryOrderNo(saveReturnOrderIn.getDeliveryOrderNo());
        ordDirReturnMapper.updateByPrimaryKeySelective(ordDirReturn);
    }

    /**
     * 计算数据
     *
     * @param ordDirReturnDetail
     * @return
     */
    @Override
    public OrdReturnDetailOut compute(OrdDirReturnDetail ordDirReturnDetail, String storeCode, String bizOrgCode, String wrhCode, String stockCode, String centerStockBizOrgCode) {
        if (Objects.isNull(ordDirReturnDetail.getApplyReturnQuantity())) {
            throw new BusinessException("请完善数据");
        }
        String channelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(storeCode, bizOrgCode);
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(ordDirReturnDetail.getGoodsCode());
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setBizOrgCode(channelBizOrgCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoodsForBackReturn(orderGoodsIn);
        if (Objects.isNull(orderGoodsOut)) {
            throw new BusinessException("门店或者商品状态不允许做直营配货退货;");
        }
        BigDecimal sellTax = orderGoodsOut.getOutTax() == null ? BigDecimal.ZERO : orderGoodsOut.getOutTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
        //1+税率
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        OrdReturnDetailOut ordReturnDetailOut = new OrdReturnDetailOut();
        BeanUtils.copy(ordDirReturnDetail, ordReturnDetailOut);
        ordReturnDetailOut.setSellTax(orderGoodsOut.getOutTax() == null ? BigDecimal.ZERO : orderGoodsOut.getOutTax());
        //申请
        ordReturnDetailOut.setApplyReturnQuantity(ordDirReturnDetail.getApplyReturnQuantity());
        ordReturnDetailOut.setApplyPackageQuantity(ordDirReturnDetail.getApplyPackageQuantity());
        ordReturnDetailOut.setApplyReturnAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(ordReturnDetailOut.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        //审核
        ordReturnDetailOut.setAuditReturnQuantity(ordDirReturnDetail.getAuditReturnQuantity());
        ordReturnDetailOut.setAuditPackageQuantity(ordReturnDetailOut.getAuditPackageQuantity());
        if (Objects.nonNull(ordDirReturnDetail.getAuditReturnQuantity())) {
            ordReturnDetailOut.setAuditReturnAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(ordReturnDetailOut.getAuditReturnQuantity()).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        }
        //实际
        ordReturnDetailOut.setActualReturnQuantity(ordDirReturnDetail.getActualReturnQuantity());
        ordReturnDetailOut.setActualPackageQuantity(ordDirReturnDetail.getActualPackageQuantity());
        if (Objects.nonNull(ordDirReturnDetail.getActualReturnQuantity())) {
            ordReturnDetailOut.setActualReturnAmount(ordDirReturnDetail.getReturnUnitPrice().multiply(ordReturnDetailOut.getActualReturnQuantity()).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        }
        BigDecimal qty = Objects.isNull(ordDirReturnDetail.getActualReturnQuantity()) ? (Objects.isNull(ordDirReturnDetail.getAuditReturnQuantity()) ? ordDirReturnDetail.getApplyReturnQuantity() : ordDirReturnDetail.getAuditReturnQuantity()) : ordDirReturnDetail.getActualReturnQuantity();
        //退货去税金额
        ordReturnDetailOut.setReturnExceptTaxAmount(ordReturnDetailOut.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //退货税额
        ordReturnDetailOut.setReturnTaxAmount(ordReturnDetailOut.getApplyReturnAmount().subtract(ordReturnDetailOut.getReturnExceptTaxAmount()));
        //仓储成本金额
        BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(wrhCode, stockCode, ordDirReturnDetail.getGoodsCode(), centerStockBizOrgCode, storeCode, channelBizOrgCode);
        if (Objects.isNull(warehousePrice)) {
            log.error("计算退货商品{}仓储库存价为空", ordReturnDetailOut.getGoodsCode());
            throw new BusinessException("计算退货商品" + ordReturnDetailOut.getGoodsCode() + "仓储库存价为空");
        }
        BigDecimal wrhCostAmount = warehousePrice.multiply(qty);
        ordReturnDetailOut.setWrhCostAmount(wrhCostAmount);
        //仓储成本去税金额
        ordReturnDetailOut.setWrhExceptTaxAmount(ordReturnDetailOut.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //仓储成本税额
        ordReturnDetailOut.setWrhTaxAmount(ordReturnDetailOut.getWrhCostAmount().subtract(ordReturnDetailOut.getWrhExceptTaxAmount()));
        //门店成本金额
        ordReturnDetailOut.setStoreCostAmount(ordDirReturnDetail.getStoreStockPrice().multiply(qty));
        //门店成本去税金额
        ordReturnDetailOut.setStoreExceptTaxAmount(ordReturnDetailOut.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //门店成本税额
        ordReturnDetailOut.setStoreTaxAmount(ordReturnDetailOut.getStoreCostAmount().subtract(ordReturnDetailOut.getStoreExceptTaxAmount()));

        ordReturnDetailOut.setGoodsTypeStr(systemDictService.getSystemDictName(ordReturnDetailOut.getGoodsType()));
        ordReturnDetailOut.setInvoiceType(orderGoodsOut.getInvoiceType());
        ordReturnDetailOut.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(orderGoodsOut.getInvoiceType()));
        ordReturnDetailOut.setIsManageValidityPeriod(orderGoodsOut.getIsManageValidityPeriod());
        return ordReturnDetailOut;
    }


    private List<ExportOrdDirReturnDetail> parseDataToExcel(List<OrdReturnDetailOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> converExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    private ExportOrdDirReturnDetail converExcel(OrdReturnDetailOut detailOut, int index) {
        ExportOrdDirReturnDetail detail = new ExportOrdDirReturnDetail();
        com.edc.plugins.utils.bean.BeanUtils.copy(detailOut, detail);
        detail.setIndex(index + 1);
        detail.setGoodsType(GoodsTypeEnum.getNameByCode(detailOut.getGoodsType()));
        return detail;
    }
}
