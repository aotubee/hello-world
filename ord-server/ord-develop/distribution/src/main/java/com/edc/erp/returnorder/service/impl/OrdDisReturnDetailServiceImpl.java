package com.edc.erp.returnorder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.edc.erp.common.async.handel.StoreChannelHandle;
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
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import com.edc.erp.returnorder.entity.OrdDisReturnImage;
import com.edc.erp.returnorder.enumeration.OrdReturnOrderStatusEnum;
import com.edc.erp.returnorder.enumeration.OrdReturnOrderTypeEnum;
import com.edc.erp.returnorder.mapper.OrdDisReturnDetailMapper;
import com.edc.erp.returnorder.mapper.OrdDisReturnImageMapper;
import com.edc.erp.returnorder.mapper.OrdDisReturnMapper;
import com.edc.erp.returnorder.model.in.AppSaveOrdDisReturnDetailIn;
import com.edc.erp.returnorder.model.in.ImportOrdReturnOrderVO;
import com.edc.erp.returnorder.model.in.OrdDisReturnDetailIn;
import com.edc.erp.returnorder.model.in.OrdSaveReturnOrderIn;
import com.edc.erp.returnorder.model.out.DisReturnOrderDtlPrintOut;
import com.edc.erp.returnorder.model.out.ExportOrdDisReturnDetail;
import com.edc.erp.returnorder.model.out.OrdDisReturnDetailOut;
import com.edc.erp.returnorder.model.out.OrdReturnDetailOut;
import com.edc.erp.returnorder.service.OrdDisReturnDetailService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dictionary.service.SystemDictService;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
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
 * 退货单详情表(DisReturnDetail)表服务实现类
 *
 * @author yaojinpeng
 * @since 2022-10-21 18:26:20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisReturnDetailServiceImpl extends BaseServiceImpl<OrdDisReturnDetail> implements OrdDisReturnDetailService {

    private final OrdDisReturnDetailMapper ordDisReturnDetailMapper;

    private final FileService fileService;

    private final WarehouseServer warehouseServer;

    private final OrdDisReturnImageMapper ordDisReturnImageMapper;
    private final StockServer stockServer;

    private final OrdDisReturnMapper ordDisReturnMapper;

    private final OrderGoodsServer orderGoodsServer;

    private final SystemDictService systemDictService;

    private final AsyncLogService asyncLogService;

    private final UniqueUtils uniqueUtils;

    private final StoreChannelHandle storeChannelHandle;

    /**
     * 统计申请退货商品总数量
     *
     * @param returnOrderId
     * @return
     */
    @Override
    public BigDecimal sumApplyReturnQuantity(Integer returnOrderId) {
        return ordDisReturnDetailMapper.sumApplyReturnQuantity(returnOrderId);
    }

    /**
     *  统计申请退货商品品项数
     *
     * @param returnOrderId
     * @return
     */
    @Override
    public Integer countApplyReturnSkuQuantity(Integer returnOrderId) {
        return ordDisReturnDetailMapper.countApplyReturnSkuQuantity(returnOrderId);
    }

    /**
     * 统计实际退货商品总数量
     *
     * @param returnOrderId
     * @return
     */
    @Override
    public BigDecimal sumActualReturnQuantity(Integer returnOrderId) {
        return ordDisReturnDetailMapper.sumActualReturnQuantity(returnOrderId);
    }

    /**
     * 查询实际品项数
     *
     * @param returnOrderId
     * @return
     */
    @Override
    public Integer countActualReturnSkuQuantity(Integer returnOrderId) {
        return ordDisReturnDetailMapper.countActualReturnSkuQuantity(returnOrderId);
    }


    @Override
    public List<OrdReturnDetailOut> finaOrdReturnDetailList(OrdDisReturnDetailIn ordDisReturnDetailIn) {
        List<OrdReturnDetailOut> detailOutList = ordDisReturnDetailMapper.finaOrdReturnDetailList(ordDisReturnDetailIn);
        return detailOutList;
    }

    /**
     * 批量添加
     *
     * @param returnDetails
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdDisReturnDetail> returnDetails) {
        ordDisReturnDetailMapper.batchSave(returnDetails);
    }

    @Override
    public void deleteByReturnOrderId(OrdDisReturn ordDisReturn) {
        OrdDisReturnDetail ordDisReturnDetail = new OrdDisReturnDetail();
        ordDisReturnDetail.setReturnOrderId(ordDisReturn.getId());
        ordDisReturnDetailMapper.delete(ordDisReturnDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdate(List<OrdDisReturnDetail> ordDisReturnDetailList) {
        return ordDisReturnDetailMapper.batchUpdate(ordDisReturnDetailList);
    }

    /**
     * 初始化退货单明细map
     *
     * @param returnOrderDetailMap
     * @param storeOut
     * @param returnGoodsInfoIn
     * @param orderGoodsOut
     * @param ordDisReturn
     */
    @Override
    public void initReturnOrderDetailMap(Map<String, List<AppSaveOrdDisReturnDetailIn>> returnOrderDetailMap, StoreOut storeOut,
                                         AppSaveOrdDisReturnDetailIn returnGoodsInfoIn, OrderGoodsOut orderGoodsOut, OrdDisReturn ordDisReturn) {
        AppSaveOrdDisReturnDetailIn returnOrderDetail = this.initReturnOrderDetail(storeOut, returnGoodsInfoIn, orderGoodsOut, ordDisReturn);
        String key = ordDisReturn.getStockCode() + SystemConstant.SHORT_LINE + orderGoodsOut.getDistributionWay();
        List<AppSaveOrdDisReturnDetailIn> ordDisReturnDetailList = returnOrderDetailMap.get(key);
        if (CollectionUtils.isEmpty(ordDisReturnDetailList)) {
            ordDisReturnDetailList = new ArrayList<>();
        }
        ordDisReturnDetailList.add(returnOrderDetail);
        returnOrderDetailMap.put(key, ordDisReturnDetailList);
    }

    @Override
    public void saveReturnOrderDetail(OrdDisReturnDetail returnOrderDetail) {
        ordDisReturnDetailMapper.insert(returnOrderDetail);
    }

    /**
     * 初始化退货单明细
     *
     * @param storeOut
     * @param returnGoodsInfoIn
     * @param orderGoodsOut
     * @param ordDisReturn
     * @return
     */
    @Override
    public AppSaveOrdDisReturnDetailIn initReturnOrderDetail(StoreOut storeOut, AppSaveOrdDisReturnDetailIn returnGoodsInfoIn, OrderGoodsOut orderGoodsOut, OrdDisReturn ordDisReturn) {
        AppSaveOrdDisReturnDetailIn ordDisReturnDetail = new AppSaveOrdDisReturnDetailIn();
        ordDisReturnDetail.setGoodsCode(orderGoodsOut.getGoodsCode());
        ordDisReturnDetail.setOrgGoodsId(orderGoodsOut.getOrgGoodsId());
        ordDisReturnDetail.setBarCode(orderGoodsOut.getBarCode());
        ordDisReturnDetail.setApplyReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
//        ordDisReturnDetail.setActualReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
//        ordDisReturnDetail.setAuditReturnQuantity(returnGoodsInfoIn.getApplyReturnQuantity());
        ordDisReturnDetail.setGoodsName(orderGoodsOut.getGoodsName());
        ordDisReturnDetail.setGoodsType(orderGoodsOut.getGoodsType());
        ordDisReturnDetail.setVendorCode(orderGoodsOut.getVendorCode());
        ordDisReturnDetail.setSellTax(orderGoodsOut.getOutTax());


        if (null != (orderGoodsOut.getDistributionSpecification())) {
            ordDisReturnDetail.setDistributionSpecificationNum(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()));
            ordDisReturnDetail.setDistributionSpecification(orderGoodsOut.getDistributionSpecification().getQpcStr());
            ordDisReturnDetail.setDistributionSpecificationUnit(orderGoodsOut.getDistributionSpecification().getUnitName());
            ordDisReturnDetail.setApplyPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
//            ordDisReturnDetail.setActualPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
//            ordDisReturnDetail.setAuditPackageQuantity(returnGoodsInfoIn.getApplyReturnQuantity().divide(new BigDecimal(orderGoodsOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
            ordDisReturnDetail.setDistributionSpecificationNum(Objects.isNull(orderGoodsOut.getDistributionSpecification().getQpc()) ? BigDecimal.ZERO : BigDecimal.valueOf(orderGoodsOut.getDistributionSpecification().getQpc()));
        }
        ordDisReturnDetail.setGoodsImage(orderGoodsOut.getImgUrl());
        ordDisReturnDetail.setReturnReason(returnGoodsInfoIn.getReturnReason());
        ordDisReturnDetail.setIsGiftReturn(null);

        BigDecimal storeStockPrice = warehouseServer.getStockPrice(storeOut.getStoreCode(), orderGoodsOut.getGoodsCode(), ordDisReturn.getBizOrgCode());
        if (Objects.isNull(storeStockPrice)) {
            log.error("配销退货单{}商品{}门店库存价为空", ordDisReturn.getReturnOrderNo(), ordDisReturnDetail.getGoodsCode());
            throw new BusinessException("配销退货单" + ordDisReturn.getReturnOrderNo() + "商品" + ordDisReturnDetail.getGoodsCode() + "门店库存价为空");
        }
        ordDisReturnDetail.setReturnUnitPrice(storeStockPrice);
        ordDisReturnDetail.setApplyReturnAmount(ordDisReturnDetail.getReturnUnitPrice().multiply(ordDisReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        if (null != orderGoodsOut.getDistributionUnitPrice()) {
//            ordDisReturnDetail.setReturnUnitPrice(orderGoodsOut.getDistributionUnitPrice());
            ordDisReturnDetail.setDistributionPrice(orderGoodsOut.getDistributionUnitPrice());
//            ordDisReturnDetail.setActualReturnAmount(orderGoodsOut.getDistributionUnitPrice().multiply(ordDisReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//            ordDisReturnDetail.setAuditReturnAmount(orderGoodsOut.getDistributionUnitPrice().multiply(ordDisReturnDetail.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        }
        BigDecimal sellTax = ordDisReturnDetail.getSellTax() == null ? BigDecimal.ZERO : ordDisReturnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
        //1+税率
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        //最新门店库存价
        ordDisReturnDetail.setStoreStockPrice(Objects.isNull(storeStockPrice) ? BigDecimal.ZERO : storeStockPrice);
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDisReturn.getStockCode());
        BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(ordDisReturn.getWrhCode(), ordDisReturn.getStockCode(),
                returnGoodsInfoIn.getGoodsCode(), stockInfoOut.getBizOrgCode(), storeOut.getStoreCode(), ordDisReturn.getBizOrgCode());
        if (Objects.isNull(warehousePrice)) {
            log.error("配销退货单{}商品{}仓储库存价为空", ordDisReturn.getReturnOrderNo(), ordDisReturnDetail.getGoodsCode());
            throw new BusinessException("配销退货单" + ordDisReturn.getReturnOrderNo() + "商品" + ordDisReturnDetail.getGoodsCode() + "仓储库存价为空");
        }
        //最新仓储库存价
        ordDisReturnDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);
        //退货去税金额
        ordDisReturnDetail.setReturnExceptTaxAmount(ordDisReturnDetail.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //退货税额
        ordDisReturnDetail.setReturnTaxAmount(ordDisReturnDetail.getApplyReturnAmount().subtract(ordDisReturnDetail.getReturnExceptTaxAmount()));
        //仓储成本金额
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
        ordDisReturnDetail.setCreator(storeOut.getStoreCode());
        ordDisReturnDetail.setCreateTime(LocalDateTime.now());
        ordDisReturnDetail.setUpdater(storeOut.getStoreCode());
        ordDisReturnDetail.setUpdateTime(LocalDateTime.now());
        ordDisReturnDetail.setIsDelete(ModelConst.DELETE.NO);
        ordDisReturnDetail.setInvoiceType(orderGoodsOut.getInvoiceType());
        ordDisReturnDetail.setImageUrlList(returnGoodsInfoIn.getImageUrlList());
//        if (NumberUtil.INTEGER_ONE.equals(orderGoodsOut.getIsManageValidityPeriod()) && StringUtils.isBlank(returnGoodsInfoIn.getExpiry())) {
//            throw new BusinessException("配销退货单" + ordDisReturn.getReturnOrderNo() + "效期商品" + ordDisReturnDetail.getGoodsCode() + "效期码为空");
//        }
        ordDisReturnDetail.setExpiry(returnGoodsInfoIn.getExpiry());
        // 天岁接入ERP，不再对接中科接口
//        if (OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(ordDisReturn.getBizOrgCode())) {
//            ZkGoodsOut zkGoods = zkServer.getZkGoodsByGoodsCodeAndBizOrgCode(ordDisReturnDetail.getGoodsCode(), ordDisReturn.getBizOrgCode());
//            if (Objects.nonNull(zkGoods)) {
//                ordDisReturnDetail.setOtherGoodsCode(zkGoods.getZkSkuCode());
//            }
//        }
        return ordDisReturnDetail;

    }

    @Override
    public OrdDisReturnDetail getReturnOrderDetailById(Integer returnOrderDetailId) {
        return ordDisReturnDetailMapper.selectByPrimaryKey(returnOrderDetailId);
    }

    /**
     * 保存或者更新退货单明细
     *
     * @param ordDisReturnDetail
     */
    @Override
    public void saveOrUpdateReturnOrderDetail(OrdDisReturnDetail ordDisReturnDetail) {
        if (null == ordDisReturnDetail.getId()) {
            ordDisReturnDetailMapper.insert(ordDisReturnDetail);
        } else {
            ordDisReturnDetailMapper.updateByPrimaryKey(ordDisReturnDetail);
        }
    }

    @Override
    public List<OrdDisReturnDetailOut> finaOrdReturnDetail(OrdDisReturnDetailIn pageIn) {
        List<OrdDisReturnDetailOut> ordDisReturnDetails = ordDisReturnDetailMapper.finaOrdReturnDetail(pageIn);
        return ordDisReturnDetails;
    }

    @Override
    public List<OrdDisReturnDetail> findByReturnOrderId(Integer item) {
        OrdDisReturnDetail ordDisReturnDetail = new OrdDisReturnDetail();
        ordDisReturnDetail.setReturnOrderId(item);
        ordDisReturnDetail.setIsDelete(NumberUtil.INTEGER_ZERO);
        return ordDisReturnDetailMapper.select(ordDisReturnDetail);
    }

    /**
     * App根据退货单明细主键删除一个明细
     *
     * @param returnOrderDetailId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReturnOrderDetailById(Integer returnOrderDetailId) {
        ordDisReturnDetailMapper.deleteByPrimaryKey(returnOrderDetailId);
        OrdDisReturnImage ordDisReturnImage = new OrdDisReturnImage();
        ordDisReturnImage.setReturnDetailId(returnOrderDetailId);
        ordDisReturnImageMapper.delete(ordDisReturnImage);
    }

    @Override
    public List<DisReturnOrderDtlPrintOut> findPrintDtlByReturnId(Long returnOrderId) {
        return ordDisReturnDetailMapper.findPrintDtlByReturnId(returnOrderId);
    }


    @Override
    public String export(OrdDisReturnDetailIn pageIn) {
        List<OrdReturnDetailOut> ordReturnDetailOutList = this.finaOrdReturnDetailList(pageIn);
        List<ExportOrdDisReturnDetail> exportOrdDisReturnDetails = parseDataToExcel(ordReturnDetailOutList);

        String title = "退货单明细信息";
        byte[] byts = FileExportUtil.getFileBytesByData(exportOrdDisReturnDetails,
                title, title, ExportOrdDisReturnDetail.class, true);
        return fileService.uploadFile(title + ".xlsx", byts, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    /**
     * 明细保存
     *
     * @param ordDisReturn
     * @param saveReturnOrderIn
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDetail(OrdDisReturn ordDisReturn, OrdSaveReturnOrderIn saveReturnOrderIn) {
        List<OrdDisReturnDetail> returnGoodsInfoInList = saveReturnOrderIn.getReturnGoodsInfoInList();
//        List<OrdDisReturnDetail> list = new ArrayList<>();
        BigDecimal applyReturnQuantity = BigDecimal.ZERO;
        BigDecimal applyReturnAmount = BigDecimal.ZERO;
        BigDecimal auditReturnQuantity = BigDecimal.ZERO;
        BigDecimal auditReturnAmount = BigDecimal.ZERO;
        BigDecimal actualReturnQuantity = BigDecimal.ZERO;
        BigDecimal actualReturnAmount = BigDecimal.ZERO;
        OrdDisReturnImage ordDisReturnImage = new OrdDisReturnImage();
        ordDisReturnImage.setReturnOrderId(ordDisReturn.getId());
        List<OrdDisReturnImage> imageList = ordDisReturnImageMapper.select(ordDisReturnImage);
        Map<Integer, List<OrdDisReturnImage>> imageMap = null;
        if (CollectionUtils.isNotEmpty(imageList)) {
            ordDisReturnImageMapper.delete(ordDisReturnImage);
            imageMap = imageList.stream().collect(Collectors.groupingBy(OrdDisReturnImage::getReturnDetailId));
        }
        int line = 1;
        for (OrdDisReturnDetail returnGoodsInfoIn : returnGoodsInfoInList) {
            OrdReturnDetailOut returnDetailOut = this.compute(returnGoodsInfoIn, ordDisReturn.getStoreCode(),
                    ordDisReturn.getBizOrgCode(), ordDisReturn.getWrhCode(), ordDisReturn.getStockCode(), saveReturnOrderIn.getCenterStockBizOrgCode());
            if (StringUtils.isBlank(saveReturnOrderIn.getDeliveryOrderNo()) && StringUtils.isNotBlank(returnGoodsInfoIn.getExpiry())) {
//                if (NumberUtil.INTEGER_ONE.equals(returnDetailOut.getIsManageValidityPeriod()) && StringUtils.isBlank(returnGoodsInfoIn.getExpiry())) {
//                    throw new BusinessException("配销退货单" + ordDisReturn.getReturnOrderNo() + "效期商品" + returnGoodsInfoIn.getGoodsCode() + "效期码为空");
//                }
                Response<LocalDateTime> expiryResponse = ExpiryCheckUtil.checkExpiry(returnGoodsInfoIn.getExpiry());
                if (!expiryResponse.isSuccess()) {
                    throw new BusinessException("配销退货单" + ordDisReturn.getReturnOrderNo() + "效期商品" + returnGoodsInfoIn.getGoodsCode() + expiryResponse.getMessage());
                }
            }
            OrdDisReturnDetail ordDisReturnDetail = new OrdDisReturnDetail();
            BeanUtils.copy(returnDetailOut, ordDisReturnDetail);
            ordDisReturnDetail.setId(null);
            ordDisReturnDetail.setReturnOrderId(ordDisReturn.getId());
            ordDisReturnDetail.setLine(line);
            ordDisReturnDetail.setIsDelete(NumberUtil.INTEGER_ZERO);
            ordDisReturnDetail.setCreator(ordDisReturn.getCreator());
            ordDisReturnDetail.setUpdater(ordDisReturn.getCreator());
            ordDisReturnDetail.setCreateTime(LocalDateTime.now());
            ordDisReturnDetail.setUpdateTime(LocalDateTime.now());
            // 天岁接入ERP，不再对接中科接口
//            if (OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(ordDisReturn.getBizOrgCode())) {
//                ZkGoodsOut zkGoods = zkServer.getZkGoodsByGoodsCodeAndBizOrgCode(ordDisReturnDetail.getGoodsCode(), ordDisReturn.getBizOrgCode());
//                if (Objects.nonNull(zkGoods)) {
//                    ordDisReturnDetail.setOtherGoodsCode(zkGoods.getZkSkuCode());
//                }
//            }
            BigDecimal stockPrice = warehouseServer.getStockPrice(ordDisReturn.getStoreCode(), returnGoodsInfoIn.getGoodsCode(), ordDisReturn.getBizOrgCode());
            if (Objects.isNull(stockPrice)) {
                log.error("配销退货单{}商品{}门店库存价为空", ordDisReturn.getReturnOrderNo(), ordDisReturnDetail.getGoodsCode());
                throw new BusinessException("配销退货单" + ordDisReturn.getReturnOrderNo() + "商品" + ordDisReturnDetail.getGoodsCode() + "门店库存价为空");
            }
            //最新门店库存价
            ordDisReturnDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
            BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(ordDisReturn.getWrhCode(), ordDisReturn.getStockCode(),
                    returnGoodsInfoIn.getGoodsCode(), saveReturnOrderIn.getCenterStockBizOrgCode(), ordDisReturn.getStoreCode(), ordDisReturn.getBizOrgCode());
            if (Objects.isNull(warehousePrice)) {
                log.error("配销退货单{}商品{}仓储库存价为空", ordDisReturn.getReturnOrderNo(), ordDisReturnDetail.getGoodsCode());
                throw new BusinessException("配销退货单" + ordDisReturn.getReturnOrderNo() + "商品" + ordDisReturnDetail.getGoodsCode() + "仓储库存价为空");
            }
            //最新仓储库存价
            ordDisReturnDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);
            //最新门店库存价
            ordDisReturnDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
            //退货去税金额
//            ordDisReturnDetail.setReturnExceptTaxAmount(ordDisReturnDetail.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //退货税额
//            ordDisReturnDetail.setReturnTaxAmount(ordDisReturnDetail.getApplyReturnAmount().subtract(ordDisReturnDetail.getReturnExceptTaxAmount()));
            applyReturnQuantity = applyReturnQuantity.add(Objects.isNull(returnGoodsInfoIn.getApplyReturnQuantity()) ? BigDecimal.ZERO : returnGoodsInfoIn.getApplyReturnQuantity());
            applyReturnAmount = applyReturnAmount.add(Objects.isNull(returnGoodsInfoIn.getApplyReturnAmount()) ? BigDecimal.ZERO : returnGoodsInfoIn.getApplyReturnAmount());
            auditReturnQuantity = auditReturnQuantity.add(Objects.isNull(returnGoodsInfoIn.getAuditReturnQuantity()) ? BigDecimal.ZERO : returnGoodsInfoIn.getAuditReturnQuantity());
            auditReturnAmount = auditReturnAmount.add(Objects.isNull(returnGoodsInfoIn.getAuditReturnAmount()) ? BigDecimal.ZERO : returnGoodsInfoIn.getAuditReturnAmount());
            actualReturnQuantity = actualReturnQuantity.add(Objects.isNull(returnGoodsInfoIn.getActualReturnQuantity()) ? BigDecimal.ZERO : returnGoodsInfoIn.getActualReturnQuantity());
            actualReturnAmount = actualReturnAmount.add(Objects.isNull(returnGoodsInfoIn.getActualReturnAmount()) ? BigDecimal.ZERO : returnGoodsInfoIn.getActualReturnAmount());
            ordDisReturnDetailMapper.insert(ordDisReturnDetail);
            //  处理上传图片
            if (null != imageMap && imageMap.size() > 0) {
                List<OrdDisReturnImage> ordDisReturnImageList = imageMap.get(returnGoodsInfoIn.getId());
                if (CollectionUtils.isNotEmpty(ordDisReturnImageList)) {
                    ordDisReturnImageList.forEach(updateDisReturnImage -> {
                        updateDisReturnImage.setReturnDetailId(ordDisReturnDetail.getId());
                        updateDisReturnImage.setId(null);
                        updateDisReturnImage.setCreateTime(null);
                        ordDisReturnImageMapper.insert(updateDisReturnImage);
                    });
                }
            }
//            list.add(ordDisReturnDetail);
            line++;
        }

//        ordDisReturnDetailMapper.batchSave(list);
        ordDisReturn.setSkuCount(returnGoodsInfoInList.size());
        ordDisReturn.setApplyReturnQuantity(applyReturnQuantity);
        ordDisReturn.setApplyReturnAmount(applyReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDisReturn.setAuditReturnQuantity(auditReturnQuantity);
        ordDisReturn.setAuditReturnAmount(auditReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDisReturn.setActualReturnQuantity(actualReturnQuantity);
        ordDisReturn.setActualReturnAmount(actualReturnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDisReturn.setDeliveryOrderNo(saveReturnOrderIn.getDeliveryOrderNo());
        ordDisReturnMapper.updateByPrimaryKeySelective(ordDisReturn);
    }


    /**
     * 计算数据
     *
     * @param ordDisReturnDetail
     * @return
     */
    @Override
    public OrdReturnDetailOut compute(OrdDisReturnDetail ordDisReturnDetail, String storeCode, String bizOrgCode, String wrhCode, String stockCode, String centerStockBizOrgCode) {
        if (Objects.isNull(ordDisReturnDetail.getApplyReturnQuantity())) {
            throw new BusinessException("请完善数据");
        }
        String channelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(storeCode, bizOrgCode);
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(ordDisReturnDetail.getGoodsCode());
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setBizOrgCode(channelBizOrgCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoodsForBackReturn(orderGoodsIn);
        if (Objects.isNull(orderGoodsOut)) {
            throw new BusinessException("门店或者商品状态不允许做加盟配销退货;");
        }
        BigDecimal sellTax = orderGoodsOut.getOutTax() == null ? BigDecimal.ZERO : orderGoodsOut.getOutTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
        //1+税率
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        OrdReturnDetailOut ordReturnDetailOut = new OrdReturnDetailOut();
        BeanUtils.copy(ordDisReturnDetail, ordReturnDetailOut);
        ordReturnDetailOut.setSellTax(orderGoodsOut.getOutTax() == null ? BigDecimal.ZERO : orderGoodsOut.getOutTax());
        //申请
        ordReturnDetailOut.setApplyReturnQuantity(ordDisReturnDetail.getApplyReturnQuantity());
        ordReturnDetailOut.setApplyPackageQuantity(ordDisReturnDetail.getApplyPackageQuantity());
        ordReturnDetailOut.setApplyReturnAmount(ordDisReturnDetail.getReturnUnitPrice().multiply(ordReturnDetailOut.getApplyReturnQuantity()).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));

        //审核
        ordReturnDetailOut.setAuditReturnQuantity(ordDisReturnDetail.getAuditReturnQuantity());
        ordReturnDetailOut.setAuditPackageQuantity(ordDisReturnDetail.getAuditPackageQuantity());

        if (Objects.nonNull(ordDisReturnDetail.getAuditReturnQuantity())) {
            ordReturnDetailOut.setAuditReturnAmount(ordDisReturnDetail.getReturnUnitPrice().multiply(ordReturnDetailOut.getAuditReturnQuantity()).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        }

        //实退
        ordReturnDetailOut.setActualReturnQuantity(ordDisReturnDetail.getActualReturnQuantity());
        ordReturnDetailOut.setActualPackageQuantity(ordDisReturnDetail.getActualPackageQuantity());
        if (Objects.nonNull(ordDisReturnDetail.getActualReturnQuantity())) {
            ordReturnDetailOut.setActualReturnAmount(ordDisReturnDetail.getReturnUnitPrice().multiply(ordDisReturnDetail.getActualReturnQuantity()).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        }

        BigDecimal qty = Objects.isNull(ordDisReturnDetail.getActualReturnQuantity()) ? (Objects.isNull(ordDisReturnDetail.getAuditReturnQuantity()) ? ordDisReturnDetail.getApplyReturnQuantity() : ordDisReturnDetail.getAuditReturnQuantity()) : ordDisReturnDetail.getActualReturnQuantity();
        //退货去税金额
//        ordReturnDetailOut.setReturnExceptTaxAmount(ordReturnDetailOut.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        ordReturnDetailOut.setReturnExceptTaxAmount(ordReturnDetailOut.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //退货税额
        ordReturnDetailOut.setReturnTaxAmount(ordReturnDetailOut.getApplyReturnAmount().subtract(ordReturnDetailOut.getReturnExceptTaxAmount()));
        //仓储成本金额
        BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(wrhCode, stockCode, ordDisReturnDetail.getGoodsCode(), centerStockBizOrgCode, storeCode, channelBizOrgCode);
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
        ordReturnDetailOut.setStoreCostAmount(ordDisReturnDetail.getStoreStockPrice().multiply(qty));
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

    @Override
    public OrdDisReturnDetail initDetail(ImportOrdReturnOrderVO importOrdReturnOrderVO, String bizOrgCode, OrderGoodsOut orderGoods) {
        OrdDisReturnDetail ordDisReturnDetail = new OrdDisReturnDetail();
        ordDisReturnDetail.setGoodsCode(importOrdReturnOrderVO.getGoodsCode());
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(importOrdReturnOrderVO.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setStoreCode(importOrdReturnOrderVO.getStoreCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.RETURN_GOODS.getType());
        if (Objects.isNull(orderGoods)) {
            throw new BusinessException("商品不存在或者不允许配货退货");
        }
        ordDisReturnDetail.setGoodsName(orderGoods.getGoodsName());
        ordDisReturnDetail.setBarCode(orderGoods.getBarCode());
        ordDisReturnDetail.setOrgGoodsId(orderGoods.getOrgGoodsId());
        ordDisReturnDetail.setGoodsType(orderGoods.getGoodsType());
        ordDisReturnDetail.setRemark(importOrdReturnOrderVO.getRemark());
        ordDisReturnDetail.setVendorCode(orderGoods.getVendorCode());
        ordDisReturnDetail.setDistributionSpecification(Objects.isNull(orderGoods.getDistributionSpecification()) ? "" : orderGoods.getDistributionSpecification().getQpcStr());
        ordDisReturnDetail.setDistributionSpecificationUnit(Objects.isNull(orderGoods.getDistributionSpecification()) ? "" : orderGoods.getDistributionSpecification().getUnitName());
        ordDisReturnDetail.setDistributionSpecificationNum(Objects.isNull(orderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : BigDecimal.valueOf(orderGoods.getDistributionSpecification().getQpc()));
        BigDecimal stockPrice = warehouseServer.getStockPrice(importOrdReturnOrderVO.getStoreCode(), importOrdReturnOrderVO.getGoodsCode(), bizOrgCode);
        ordDisReturnDetail.setReturnUnitPrice(Objects.isNull(importOrdReturnOrderVO.getReturnUnitPrice()) ? stockPrice : importOrdReturnOrderVO.getReturnUnitPrice());
        ordDisReturnDetail.setDistributionPrice(orderGoods.getDistributionUnitPrice());
        //申请
        ordDisReturnDetail.setApplyReturnQuantity(importOrdReturnOrderVO.getApplyReturnQuantity());
        ordDisReturnDetail.setApplyPackageQuantity(ordDisReturnDetail.getApplyReturnQuantity().divide(new BigDecimal(orderGoods.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        ordDisReturnDetail.setApplyReturnAmount(ordDisReturnDetail.getReturnUnitPrice().multiply(importOrdReturnOrderVO.getApplyReturnQuantity()));
//        //审核
//        ordDisReturnDetail.setAuditReturnQuantity(ordDisReturnDetail.getApplyReturnQuantity());
//        ordDisReturnDetail.setAuditPackageQuantity(ordDisReturnDetail.getApplyPackageQuantity());
//        ordDisReturnDetail.setAuditReturnAmount(ordDisReturnDetail.getApplyReturnAmount());
//        //实际
//        ordDisReturnDetail.setActualReturnQuantity(ordDisReturnDetail.getApplyReturnQuantity());
//        ordDisReturnDetail.setActualPackageQuantity(ordDisReturnDetail.getApplyPackageQuantity());
//        ordDisReturnDetail.setActualReturnAmount(ordDisReturnDetail.getApplyReturnAmount());
        ordDisReturnDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
        BigDecimal warehousePrice = warehouseServer.getStockPriceForBusiness(importOrdReturnOrderVO.getWarehouseCode(), importOrdReturnOrderVO.getStockCode(),
                importOrdReturnOrderVO.getGoodsCode(), importOrdReturnOrderVO.getCenterStockBizOrgCode(), importOrdReturnOrderVO.getStoreCode(), bizOrgCode);
        if (Objects.isNull(warehousePrice)) {
            log.error("配销退货单导入{}商品{}仓储库存价为空", ordDisReturnDetail.getGoodsCode());
            throw new BusinessException("配销退货单导入" + ordDisReturnDetail.getGoodsCode() + "仓储库存价为空");
        }
        ordDisReturnDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);
        ordDisReturnDetail.setSellTax(orderGoods.getOutTax());
        BigDecimal sellTax = ordDisReturnDetail.getSellTax() == null ? BigDecimal.ZERO : ordDisReturnDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
        //1+税率
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        //退货去税金额
        ordDisReturnDetail.setReturnExceptTaxAmount(ordDisReturnDetail.getApplyReturnAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        //退货税额
        ordDisReturnDetail.setReturnTaxAmount(ordDisReturnDetail.getApplyReturnAmount().subtract(ordDisReturnDetail.getReturnExceptTaxAmount()));
        //仓储成本金额
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
        ordDisReturnDetail.setInvoiceType(orderGoods.getInvoiceType());
        return ordDisReturnDetail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAsyncImportReturn(List<OrdSaveReturnOrderIn> returnOrderInList) {
        returnOrderInList.forEach(saveReturnOrderIn -> {
            //保存退货单
            OrdDisReturn ordDisReturn = new OrdDisReturn();
            BeanUtil.copyProperties(saveReturnOrderIn, ordDisReturn, CopyOptions.create().setIgnoreNullValue(true));
            String no = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.XT.getCode(), saveReturnOrderIn.getBizOrgCode(), uniqueUtils, 4);
            ordDisReturn.setReturnOrderNo(no);
            ordDisReturn.setReturnType(OrdReturnOrderTypeEnum.NORMAL_RETURN.getKey());
            ordDisReturn.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(saveReturnOrderIn.getBizOrgCode()));
            ordDisReturn.setSubmitTime(LocalDateTime.now());
            ordDisReturn.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
            ordDisReturn.setIsReversal(NumberUtil.INTEGER_ZERO);
            ordDisReturn.setReturnStatus(OrdReturnOrderStatusEnum.SUBMITTED.getKey());
            ordDisReturn.setIsDelete(NumberUtil.INTEGER_ZERO);
            ordDisReturn.setBizOrgCode(saveReturnOrderIn.getBizOrgCode());
            ordDisReturn.setWrhCode(saveReturnOrderIn.getWarehouseCode());
            ordDisReturn.setSkuCount(saveReturnOrderIn.getReturnGoodsInfoInList().size());
            ordDisReturn.setDistributionType(saveReturnOrderIn.getDistributionType());
            ordDisReturn.setCreator(saveReturnOrderIn.getLoginUsername());
            ordDisReturn.setUpdater(saveReturnOrderIn.getLoginUsername());
            int count = ordDisReturnMapper.insertSelective(ordDisReturn);
            //保存明细及
            if (count > 0) {
                this.handleDetail(ordDisReturn, saveReturnOrderIn);
            }
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
                    OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), OrdLogTypeEnum.ORD_DIS_RETURN_SAVE.getName(), new Date(), ordDisReturn.getCreator());

            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        });
    }


    /**
     * 将查询到的列表集合转换为导出集合
     */
    private List<ExportOrdDisReturnDetail> parseDataToExcel(List<OrdReturnDetailOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> converExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 将 OrdReturnDetailOut 转换为导出 ExportOrdDisReturnDetail
     */
    private ExportOrdDisReturnDetail converExcel(OrdReturnDetailOut detailOut, int index) {
        ExportOrdDisReturnDetail detail = new ExportOrdDisReturnDetail();
        com.edc.plugins.utils.bean.BeanUtils.copy(detailOut, detail);
        detail.setIndex(index + 1);
        detail.setGoodsType(GoodsTypeEnum.getNameByCode(detailOut.getGoodsType()));
        return detail;
    }
}
