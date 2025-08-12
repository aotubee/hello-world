package com.edc.erp.distribution.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.enumeration.SourceTypeEnum;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.model.entity.GoodsStatusBusinessSwitch;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.out.FlashSaleCheckOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreAndStatusSwitchOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.model.out.store.StoreStatusBusinessSwitch;
import com.edc.erp.common.model.out.ucmanager.UserNameOut;
import com.edc.erp.common.service.AppUserService;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disordercart.service.DisShoppingCartService;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionDetail;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionResult;
import com.edc.erp.distribution.handle.DisDistributionOrderAuditHandle;
import com.edc.erp.distribution.handle.DisDistributionOrderImportHandle;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.listener.DisDistributionAsyncListener;
import com.edc.erp.distribution.listener.OrdDistributionOrderStoreGoodsListener;
import com.edc.erp.distribution.mapper.OrdDisOrderDistributionMapper;
import com.edc.erp.distribution.model.excel.ExportOrdDisOrderDetail;
import com.edc.erp.distribution.model.excel.OrdDisDistributionImportErrorResult;
import com.edc.erp.distribution.model.in.*;
import com.edc.erp.distribution.model.out.*;
import com.edc.erp.distribution.service.OrdDisOrderDistributionDetailService;
import com.edc.erp.distribution.service.OrdDisOrderDistributionResultService;
import com.edc.erp.distribution.service.OrdDisOrderDistributionService;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.erp.enumeration.*;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * 配销分货单(OrdDisOrderDistribution)表服务实现类
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDisOrderDistributionServiceImpl extends BaseServiceImpl<OrdDisOrderDistribution> implements OrdDisOrderDistributionService {

    private final OrdDisOrderDistributionMapper ordDisOrderDistributionMapper;

    private final AsyncLogService logService;

    private final UniqueUtils uniqueUtils;

    private final StoreCenterService storeCenterService;

    private final AppUserService appUserService;

    private final OrderGoodsServer orderGoodsServer;

    private final OrdDisOrderDistributionDetailService ordDisOrderDistributionDetailService;

    private final DisOrderHandle orderHandle;

    private final FileService fileService;

    private final AsyncPushTaskService asyncPushTaskService;

    private final DisShoppingCartService disShoppingCartService;

    private final RedisService redisService;

    private final OrdDisOrderDistributionResultService ordDisOrderDistributionResultService;

    private final DisDistributionOrderAuditHandle disDistributionOrderAuditHandle;

    private final AsyncLogService asyncLogService;

    private final DisDistributionOrderImportHandle disDistributionOrderImportHandle;

    private final AsyncExportHandle asyncExportHandle;


    /**
     * 逻辑删除配销分货单
     *
     * @param orderDistribution 分货单实体
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int logicDelOrdDisOrderDistribution(OrdDisOrderDistribution orderDistribution) {
        int delete = ordDisOrderDistributionMapper.logicDeleteByPrimaryKey(orderDistribution);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(orderDistribution.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_DELETE.getName(), new Date(), orderDistribution.getUpdater());
        logService.sendAsyncSaveLogByMq(businessLog);
        return delete;
    }


    /**
     * 修改配销分货单
     *
     * @param orderDistribution 分货单实体
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrdDisOrderDistribution(OrdDisOrderDistribution orderDistribution) {
        int update = ordDisOrderDistributionMapper.updateByPrimaryKeySelective(orderDistribution);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(orderDistribution.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_UPDATE.getName(), new Date(), orderDistribution.getUpdater());
        logService.sendAsyncSaveLogByMq(businessLog);
        return update;
    }


    /**
     * 查询配销分货单表头
     *
     * @param distributionOrderId 分货单主键
     * @return
     */
    @Override
    public BackHeaderOrdDistributionOrderOut getHeaderOrdDistributionOrderOutById(Long distributionOrderId) {
        OrdDisOrderDistribution ordDisOrderDistribution = this.getOrdDisOrderDistribution(distributionOrderId);
        if (Objects.isNull(ordDisOrderDistribution)) {
            return new BackHeaderOrdDistributionOrderOut();
        }
        BackHeaderOrdDistributionOrderOut ordDistributionOrderOut = new BackHeaderOrdDistributionOrderOut();
        BeanUtils.copy(ordDisOrderDistribution, ordDistributionOrderOut);
        ordDistributionOrderOut.setDistributionOrderId(distributionOrderId);
        ordDistributionOrderOut.setCreatorId(ordDisOrderDistribution.getCreator());
        ordDistributionOrderOut.setCreateTime(ordDisOrderDistribution.getCreateTime());
        //分货总数量
        ordDistributionOrderOut.setTotalDistributionQuantity(ordDisOrderDistribution.getDistributionTotalQuantity());
        //状态转中文
        ordDistributionOrderOut.setDistributionOrderStatusValue(OrderDistributionOrderStatusEnum.getValueByKey(ordDisOrderDistribution.getDistributionOrderStatus()));
        return ordDistributionOrderOut;
    }


    /**
     * 分页查询分货订单列表
     *
     * @param orderDistributionIn 分货单分页入参
     * @return
     */
    @Override
    public Page<OrdDisOrderDistributionOrderOut> findOrdDistributionOrder(OrdDisOrderDistributionIn orderDistributionIn) {
        orderDistributionIn.setIsDelete(ModelConst.DELETE.NO);
        // 根据门店代码得到门店id
        String storeCode = orderDistributionIn.getStoreCode();
        if (StringUtils.isNotEmpty(storeCode)) {
            //查询门店信息
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(storeCode);
            //门店id
            orderDistributionIn.setStoreId(Objects.nonNull(storeOut) ? storeOut.getStoreId() : NumberUtil.INTEGER_ZERO);

        }
        List<OrdDisOrderDistributionOrderOut> ordDisOrderDistributionOuts = ordDisOrderDistributionMapper.findOrdDisOrderDistributionByPage(orderDistributionIn);
        ordDisOrderDistributionOuts.forEach(disOrderDistribution -> {
            // 分货状态中文
            disOrderDistribution.setDistributionOrderStatusValue(OrderDistributionOrderStatusEnum.getValueByKey(disOrderDistribution.getDistributionOrderStatus()));
            disOrderDistribution.setDistributionIdentificationStr(DistributionIdentificationEnum.getNameByCode(disOrderDistribution.getDistributionIdentification()));
        });
        Page<OrdDisOrderDistributionOrderOut> page = new Page(orderDistributionIn);
        page.setList(ordDisOrderDistributionOuts);
        return page;
    }


    /**
     * 根据主键查询配销分货单
     *
     * @param id 主键
     * @return
     */
    @Override
    public OrdDisOrderDistribution getOrdDisOrderDistribution(Long id) {
        return ordDisOrderDistributionMapper.selectByPrimaryKey(id);
    }


    /**
     * 批量导入分货单
     *
     * @param fileId 文件id
     * @return
     */
    @Override
    public Response<List<OrdDisOrderDistributionDetailOut>> initDistributionOrderStoreGoodsListener(String fileId) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdDistributionOrderStoreGoodsListener listener = new OrdDistributionOrderStoreGoodsListener(appUserService, storeCenterService, orderGoodsServer);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdDistributionStoreGoodsVO.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(0).build();
        excelReader.read(readSheet).finish();
        return listener.getResponse();
    }


//    /**
//     * 处理导入分货更新分货单
//     *
//     * @param ordDistributionOrderId 分货单主键
//     * @param userName               登录人
//     * @param dataList               导入的商品数据集合
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void handleDistributionOrder(Long ordDistributionOrderId, String userName, List<OrdDistributionOrderStoreGoodsIn> dataList) {
//        for (OrdDistributionOrderStoreGoodsIn goodsIn : dataList) {
//            OrdDisOrderDistributionDetail dbDetail = ordDisOrderDistributionDetailService.getOrdDisOrderDistribution(ordDistributionOrderId, goodsIn.getSkuCode(), goodsIn.getStoreCode());
//            //配销分货金额
//            BigDecimal distributionAmount = goodsIn.getOriginalPrice().multiply(goodsIn.getDistributionQuantity());
//            //不存在则新增
//            if (Objects.isNull(dbDetail)) {
//                dbDetail = new OrdDisOrderDistributionDetail();
//                BeanUtils.copy(goodsIn, dbDetail);
//                dbDetail.setCreator(userName);
//                dbDetail.setUpdater(userName);
//                dbDetail.setGoodsCode(goodsIn.getSkuCode());
//                dbDetail.setDistributionAmount(distributionAmount);
//                dbDetail.setDistributionOrderId(ordDistributionOrderId);
//                dbDetail.setWrhInvQty(goodsIn.getWrhInvQty());
//                ordDisOrderDistributionDetailService.insertSelective(dbDetail);
//            } else {
//                //存在则更新
//                dbDetail.setDistributionQuantity(goodsIn.getDistributionQuantity());
//                dbDetail.setPackingNumber(goodsIn.getPackingNumber());
//                dbDetail.setDistributionAmount(distributionAmount);
//                dbDetail.setWrhInvQty(goodsIn.getWrhInvQty());
//                ordDisOrderDistributionDetailService.updateByPrimaryKeySelective(dbDetail);
//            }
//        }
//        OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistributionMapper.selectByPrimaryKey(ordDistributionOrderId);
//        if (Objects.nonNull(ordDisOrderDistribution)) {
//            //查询当前商品门店明细 重新统计
//            List<OrdDisOrderDistributionDetail> dbDetails = ordDisOrderDistributionDetailService.getDetailByOrdDistributionOrderId(ordDistributionOrderId);
//            //获取总数量和总金额
//            Map<String, BigDecimal> sumMap = this.quantityAndAmountSum(dbDetails, null, ordDisOrderDistribution.getBizOrgCode());
//            ordDisOrderDistribution.setDistributionTotalAmount(sumMap.get(OrdDisMapKeyConstant.TOTAL_AMOUNT));
//            ordDisOrderDistribution.setDistributionTotalQuantity(sumMap.get(OrdDisMapKeyConstant.TOTAL_QUANTITY));
//            ordDisOrderDistribution.setStockCode(dataList.get(0).getStorageCode());
//            ordDisOrderDistribution.setStockName(dataList.get(0).getStorageName());
//        }
//        ordDisOrderDistributionMapper.updateByPrimaryKeySelective(ordDisOrderDistribution);
//    }


    /**
     * 保存分货单门店商品信息
     *
     * @param importStoreDistributionOrderIn 导入分货入参
     * @param userName                       登录人
     * @param bizOrgCode                     业务组织
     * @return
     */
    @Override
    public Response<List<OrdDisOrderDistributionDetailOut>> importDistributionOrder(ImportStoreDistributionOrderIn importStoreDistributionOrderIn,
                                                                                    String userName, String bizOrgCode) {
        //结果集
        List<OrdDisOrderDistributionDetailOut> outs = new ArrayList<>();
        //获取商品和分货数量集合
        List<ImportStoreGoodsIn> importStoreGoodsInList = importStoreDistributionOrderIn.getImportStoreGoodsInList();
        if (CollectionUtils.isEmpty(importStoreGoodsInList)) {
            return Response.error("商品代码不能为空");
        }
        importStoreDistributionOrderIn.getStoreCodeList().forEach(storeCode -> {
            importStoreDistributionOrderIn.getImportStoreGoodsInList().forEach(importStoreGoodsIn -> {
                //明细结果实体
                OrdDisOrderDistributionDetailOut detailOut = new OrdDisOrderDistributionDetailOut();
                //校验通过 获取门店和商品信息
                CheckDisOrderOut checkDisOrderOut = this.checkDisOrder(importStoreGoodsIn.getSkuCode(), storeCode, bizOrgCode);
                //获取门店信息
                StoreOut storeOut = checkDisOrderOut.getStoreOut();
                //获取商品信息
                OrderGoodsOut goodsOut = checkDisOrderOut.getGoodsOut();
                //商品代码
                detailOut.setGoodsCode(importStoreGoodsIn.getSkuCode());
                //商品名称
                detailOut.setGoodsName(goodsOut.getGoodsName());
                //门店代码
                detailOut.setStoreCode(storeCode);
                //门店名称
                detailOut.setStoreName(storeOut.getStoreName());
                //分货数量
                detailOut.setDistributionQuantity(importStoreGoodsIn.getDistributionQuantity());
                //分货包装数
                detailOut.setPackingNumber(importStoreGoodsIn.getDistributionQuantity().divide(new BigDecimal(goodsOut.getDistributionSpecification().getQpc()), 0, RoundingMode.UP));
                //配货规格
                detailOut.setDistributionSpecification(goodsOut.getDistributionSpecification().getQpcStr());
                //当前库存数量
                detailOut.setWrhInvQty(goodsOut.getStockQuantity());
                //配销价
                detailOut.setOriginalPrice(goodsOut.getDistributionUnitPrice());
                //分货金额
                detailOut.setDistributionAmount(goodsOut.getDistributionUnitPrice().multiply(importStoreGoodsIn.getDistributionQuantity()));
                //添加至结果集
                outs.add(detailOut);
            });
        });
        return Response.data(outs, "导入门店/商品成功");
    }


    /**
     * 配销分货单作废
     *
     * @param ordDisOrderDistribution 配销分货单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidDistributionOrder(OrdDisOrderDistribution ordDisOrderDistribution) {
        OrdDisOrderDistribution dbDistribution = this.getOrdDisOrderDistribution(ordDisOrderDistribution.getId());
        if (!OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(dbDistribution.getDistributionOrderStatus())
                && !OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(dbDistribution.getDistributionOrderStatus())) {
            throw new BusinessException(dbDistribution.getDistributionOrderStatus() + "状态不可作废");
        }
        ordDisOrderDistributionMapper.updateByPrimaryKeySelective(ordDisOrderDistribution);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDisOrderDistribution.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_INVALID.getName(), new Date(), ordDisOrderDistribution.getUpdater());
        logService.sendAsyncSaveLogByMq(businessLog);
    }


    /**
     * 提交分货单
     *
     * @param submitDistributionOrderIn 审核入参
     * @param isEnd                     是否完结
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitDistributionOrder(OrdExamineDistributionOrderIn submitDistributionOrderIn, Integer isEnd) {
        OrdDisOrderDistribution distributionOrder = new OrdDisOrderDistribution();
        //审核
        distributionOrder.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.APPROVAL.getKey());
        distributionOrder.setId(submitDistributionOrderIn.getDistributionOrderId());
        distributionOrder.setUpdateTime(LocalDateTime.now());
        distributionOrder.setIsEnd(isEnd);
        distributionOrder.setIsEffectiveImmediately(submitDistributionOrderIn.getIsEffectiveImmediately());
        distributionOrder.setEffectiveTime(submitDistributionOrderIn.getEffectiveTime());
        //更新出货单
        int updateCount = ordDisOrderDistributionMapper.updateByPrimaryKeySelective(distributionOrder);
        //更新和保存明细
        // 单据审核，明细不能在变动
//        ordDisOrderDistributionDetailService.save(distributionOrder,submitDistributionOrderIn.getOrdDisOrderDistributionDetails(),NumberUtil.INTEGER_ONE);

        return updateCount;
    }


    /**
     * 初始化订单
     *
     * @param distributionOrderId 分货订单主键
     * @param loginUsername       操作人
     * @param bizOrgCode          业务组织
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, List<OrderCartOut>> initDistributionGoodsToOrderCartOut(Long distributionOrderId, String loginUsername, String bizOrgCode) {
        // 按门店提交分货单下商品到订货清单与订单中
        OrdDisOrderDistributionDetail query = new OrdDisOrderDistributionDetail();
        query.setDistributionOrderId(distributionOrderId);
        query.setIsDelete(ModelConst.DELETE.NO);
        List<OrdDisOrderDistributionDetail> distributionStoreList = ordDisOrderDistributionDetailService.list(query);
        Map<String, List<OrderCartOut>> storeOrderCartMap = new LinkedHashMap<>();
        LinkedHashSet<String> errorSet = new LinkedHashSet<>();
        Map<String, List<OrdDisOrderDistributionDetail>> storeGoodsMap = distributionStoreList.stream()
                .collect(Collectors.groupingBy(OrdDisOrderDistributionDetail::getStoreCode));
        storeGoodsMap.forEach((storeCode, ordDisOrderDistributionDetails) -> {
            List<OrderCartGoodsOut> orderCartOutList = new ArrayList<>();
            ordDisOrderDistributionDetails.forEach(distributionDetail -> {
                OrderCartOut orderCartOut = new OrderCartOut();
                orderCartOut.setGoodsCode(distributionDetail.getGoodsCode());
                orderCartOut.setQuantity(distributionDetail.getPackingNumber());
                OrderCartGoodsOut orderCartGoodsOut = this.initWaiteDistributionSku(bizOrgCode, storeCode, orderCartOut);
                orderCartOutList.add(orderCartGoodsOut);
            });
            DisCheckDistributionSkuOut disCheckDistributionSkuOut = this.filterSkuForDistributionOrder(orderCartOutList, SourceTypeEnum.DISTRIBUTION.getKey());
            log.info("配销分货单{}下门店{}校验商品信息{}--------->", distributionOrderId, storeCode, JSON.toJSON(disCheckDistributionSkuOut));
            storeOrderCartMap.put(storeCode, disCheckDistributionSkuOut.getLegalOrderCartGoodsOutList());
            if (CollectionUtils.isNotEmpty(disCheckDistributionSkuOut.getErrorMessageSet())) {
                errorSet.addAll(disCheckDistributionSkuOut.getErrorMessageSet());
            }
        });
        if (CollectionUtils.isNotEmpty(errorSet)) {
            String content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_FILTER_SKU.getKey(),
                    errorSet.stream().map(String::valueOf).collect(Collectors.joining(SystemConstant.COMMA)));
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                    String.valueOf(distributionOrderId), OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                    content, new Date(), SystemConstant.SYSTEM_USER);
            logService.sendAsyncSaveLogByMq(businessLog);
        }
        return storeOrderCartMap;
    }

    /**
     * @param bizOrgCode:
     * @param storeCode:
     * @param orderCartOut:
     * @Description: 初始化分货转订货单购物车商品
     * @Author: ZhangYao
     * @Date: 2023/3/10 16:20
     * @return: com.edc.erp.directly.distribution.model.out.OrderCartGoodsOut
     **/
    private OrderCartGoodsOut initWaiteDistributionSku(String bizOrgCode, String storeCode, OrderCartOut orderCartOut) {
        OrderCartGoodsOut orderCartGoodsOut = new OrderCartGoodsOut();
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setGoodsCode(orderCartOut.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        //查询是否允许商品订货信息
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DISTRIBUTION.getType());
        orderGoodsIn.setSourceCode(SourceTypeEnum.DISTRIBUTION.getKey());
        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        orderCartGoodsOut.setGoodsCode(orderCartOut.getGoodsCode());
        if (Objects.isNull(orderGoodsOut)) {
            orderCartGoodsOut.setIsEnable(ModelConst.ENABLE.NO);
        } else {
            orderCartGoodsOut.setPackageQuantity(orderCartOut.getQuantity());
            orderCartGoodsOut.setIsEnable(ModelConst.ENABLE.YES);
            orderCartGoodsOut.setIsShelves(orderGoodsOut.getIsShelves());
            // 是否可以加购
            FlashSaleCheckOut flashSaleCheckOut = disShoppingCartService.isCanBuyFlashSaleGoods(orderCartOut.getGoodsCode(), bizOrgCode);
            orderCartGoodsOut.setIsCanBuyFlashSale(flashSaleCheckOut.getIsCanBuyFlashSale());
        }
        return orderCartGoodsOut;
    }

    /**
     * @param orderCartGoodsOutList:
     * @param sourceCode:
     * @Description: 过滤购物车商品
     * @Author: ZhangYao
     * @Date: 2023/3/10 16:27
     * @return: com.edc.erp.directly.distribution.model.out.DirCheckDistributionSkuOut
     **/
    private DisCheckDistributionSkuOut filterSkuForDistributionOrder(List<OrderCartGoodsOut> orderCartGoodsOutList, String sourceCode) {
        LinkedHashSet<String> errorSet = new LinkedHashSet<>();
        // 合法购物车商品数据集合
        List<OrderCartOut> legalOrderCartGoodsOutList = Lists.newArrayList();
        orderCartGoodsOutList.forEach(orderCartGoodsOut -> {
            String errorMessage = null;
            if (ModelConst.ENABLE.NO.equals(orderCartGoodsOut.getIsEnable())) {
                log.error(MessageFormat.format(DistErrorMessageEnum.SKU_BUS_GATE.getValue(), orderCartGoodsOut.getGoodsCode()));
                errorMessage = MessageFormat.format(DistErrorMessageEnum.SKU_BUS_GATE.getValue(), orderCartGoodsOut.getGoodsCode());
            } else {
                if (ModelConst.DELETE.YES.equals(orderCartGoodsOut.getIsShelves()) && !SourceTypeEnum.DISTRIBUTION.getKey().equals(sourceCode)) {
                    log.error(MessageFormat.format(DistErrorMessageEnum.SKU_SHELVES.getValue(), orderCartGoodsOut.getGoodsCode()));
                    errorMessage = MessageFormat.format(DistErrorMessageEnum.SKU_SHELVES.getValue(), orderCartGoodsOut.getGoodsCode());
                }
                if (ModelConst.DELETE.NO.equals(orderCartGoodsOut.getIsCanBuyFlashSale())) {
                    log.error(MessageFormat.format(DistErrorMessageEnum.FLASH_SALE.getValue(), orderCartGoodsOut.getGoodsCode()));
                    errorMessage = MessageFormat.format(DistErrorMessageEnum.FLASH_SALE.getValue(), orderCartGoodsOut.getGoodsCode());
                }
            }
            if (StringUtils.isBlank(errorMessage)) {
                OrderCartOut orderCartOut = new OrderCartOut();
                orderCartOut.setGoodsCode(orderCartGoodsOut.getGoodsCode());
                orderCartOut.setQuantity(orderCartGoodsOut.getPackageQuantity());
                legalOrderCartGoodsOutList.add(orderCartOut);
            } else {
                errorSet.add(errorMessage);
            }
        });
        DisCheckDistributionSkuOut checkDistributionSkuOut = new DisCheckDistributionSkuOut();
        checkDistributionSkuOut.setLegalOrderCartGoodsOutList(legalOrderCartGoodsOutList);
        checkDistributionSkuOut.setErrorMessageSet(errorSet);
        return checkDistributionSkuOut;
    }

//    /**
//     * 配销分货单创建订货单
//     *
//     * @param storeCode               门店code
//     * @param ordDisOrderDistribution 分货单结果
//     * @param effectiveTime           生肖实现
//     * @param orderCartOuts           单据购物车结果
//     * @param loginUsername           操作者
//     * @param bizOrgCode              业务组织
//     * @return
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Integer createOrder(String storeCode, OrdDisOrderDistribution ordDisOrderDistribution, LocalDateTime effectiveTime, List<OrderCartOut> orderCartOuts, String loginUsername, String bizOrgCode) {
//        List<CreateOrderSkuIn> createOrderSkuInList = new ArrayList<CreateOrderSkuIn>();
//        for (OrderCartOut orderCartOut : orderCartOuts) {
//            CreateOrderSkuIn createOrderSkuIn = new CreateOrderSkuIn();
//            createOrderSkuIn.setGoodsCode(orderCartOut.getGoodsCode());
//            createOrderSkuIn.setPackageQuantity(orderCartOut.getQuantity());
//            createOrderSkuInList.add(createOrderSkuIn);
//        }
//        //生成订单
//        orderHandle.createDistributionOrderOld(storeCode, loginUsername, ordDisOrderDistribution.getId(), createOrderSkuInList, bizOrgCode);
//        return 1;
//    }


    /**
     * 校验分货订单是否已审核状态
     *
     * @param distributionOrderId 分货单id
     * @return
     */
    @Override
    public Response<String> checkDistributionOrderStatus(Long distributionOrderId) {
        OrdDisOrderDistribution ordDisOrderDistribution = this.getOrdDisOrderDistribution(distributionOrderId);
        if (Objects.isNull(ordDisOrderDistribution)) {
            return Response.error("无效的分货订单");
        }
        if (OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(ordDisOrderDistribution.getDistributionOrderStatus())) {
            return Response.error("该分货订单已审核不能删除");
        }

        return Response.success();
    }


    /**
     * 查询分货订单列表
     *
     * @param disJoinOrderIn 作废分货单关联的订货单列表查询入参类
     * @return
     */
    @Override
    public Page<DisJoinOrderOut> findDisJoinOrderListByDisId(DisJoinOrderIn disJoinOrderIn) {
        //查询分货单相关的订单
        List<DisJoinOrderOut> orderOuts = ordDisOrderDistributionMapper.findDisJoinOrderListByPage(disJoinOrderIn);
        orderOuts.forEach(item -> {
            //状态中文转换
            item.setOrderStatusCodeStr(OrderStatusEnum.getValueByKey(item.getOrderStatusCode()));
            //门店代码
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(item.getStoreCode());
            if (Objects.nonNull(storeOut)) {
                item.setStoreName(storeOut.getStoreName());
            }
        });

        Page<DisJoinOrderOut> orderOutPage = new Page(disJoinOrderIn);
        orderOutPage.setList(orderOuts);
        return orderOutPage;
    }


    /**
     * 配销分货单导出
     *
     * @param queryOrderDistributionDetailIn 配销分货门店商品 查询入参
     * @return
     */
    @Override
    public String export(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn) {
        //分页查询出货单明细
        Page<OrdDisOrderDistributionDetailOut> detailOutPage = ordDisOrderDistributionDetailService.findByPage(queryOrderDistributionDetailIn);
        //获取分页数据
        List<OrdDisOrderDistributionDetailOut> detailOuts = detailOutPage.getList();
        //导出Excel实体
        List<ExportOrdDisOrderDetail> exportDetails = new ArrayList<>();
        for (OrdDisOrderDistributionDetailOut detailOut : detailOuts) {
            //导出实体
            ExportOrdDisOrderDetail exportDetail = new ExportOrdDisOrderDetail();
            BeanUtils.copy(detailOut, exportDetail);
            //添加excel导出结果集
            exportDetails.add(exportDetail);
        }

        //导入excel标题
        String title = "配销分货单信息";
        byte[] fileBytesByData = FileExportUtil.getFileBytesByData(
                exportDetails,
                title,
                title,
                ExportOrdDisOrderDetail.class,
                true);

        return fileService.uploadFile(
                title + ".xlsx",
                fileBytesByData,
                SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }


//    /**
//     * 分货单初始化订货单任务
//     *
//     * @param distributionOrderId 订货单id
//     * @param effectiveTime       生效时间
//     * @param bizOrgCode          业务组织
//     * @param loginUsername       当前操作者
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void disDistributionInitOrder(Long distributionOrderId, LocalDateTime effectiveTime, String bizOrgCode, String loginUsername) {
//        //初始化订货单任务
//        DisDistributionInitOrderIn disDistributionInitOrderIn = new DisDistributionInitOrderIn();
//        //业务组织
//        disDistributionInitOrderIn.setBizOrgCode(bizOrgCode);
//        //分货单id
//        disDistributionInitOrderIn.setDistributionOrderId(distributionOrderId);
//        //生效时间
//        disDistributionInitOrderIn.setEffectiveTime(effectiveTime);
//        //当前操作者
//        disDistributionInitOrderIn.setLoginUsername(loginUsername);
//        //提交任务
//        String tackData = JSONObject.toJSONString(disDistributionInitOrderIn);
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_DISTRIBUTION_TO_ORDER, tackData);
//        log.info("配销分货单审核成功后创建订货单任务入参-------------------{}", tackData);
//
////        this.updatedisDistributionOrderExexuted(distributionOrderId, loginUsername);
//    }

    /**
     * 修改配销分货单状态为已生效
     *
     * @param distributionOrderId
     * @param loginUsername
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatedisDistributionOrderExexuted(Long distributionOrderId, String loginUsername) {
        OrdDisOrderDistribution distribution = new OrdDisOrderDistribution();
        distribution.setId(distributionOrderId);
        distribution.setIsEnd(NumberUtil.INTEGER_ONE);
        distribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.EXECUTED.getKey());
        this.updateByPrimaryKeySelective(distribution);
        //添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(distributionOrderId),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_EXECUTED.getName(), new Date(), loginUsername);
        logService.sendAsyncSaveLogByMq(businessLog);
    }


    /**
     * 根据时间和状态查询分货单
     *
     * @return
     */
    @Override
    public List<OrdDisOrderDistributionOrderOut> findOrderByNewTimeAndStatus() {
        return ordDisOrderDistributionMapper.findOrderByNewTimeAndStatus(OrderDistributionOrderStatusEnum.APPROVAL.getKey(), DateUtils.format(LocalDateTime.now()));
    }

    /**
     * 查找配销分货单操作人集合
     *
     * @param bizOrgCode 业务组织
     * @return
     */
    @Override
    public List<UserNameOut> findDistributionOrderCreatorList(String bizOrgCode) {
        //查询分货单创建者信息
        return ordDisOrderDistributionMapper.findDistributionOrderCreatorList(bizOrgCode);
    }


    /**
     * 配销分货单保存或修改
     *
     * @param ordDisOrderDistIn 保存配销分货单和门店商品明细入参
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDisOrderDistOut saveUpdateDisOrder(OrdDisOrderDistIn ordDisOrderDistIn) {
        //校验
        CheckDisOrderOut checkDisOrderOut = this.verifyOrder(ordDisOrderDistIn);
        //添加待审核状态
        ordDisOrderDistIn.getOrdDisOrderDistribution().setDistributionOrderStatus(OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey());
        //判断分货单id是否存在 存在则为更新，否则为新增
        boolean idIsNull = Objects.isNull(ordDisOrderDistIn.getOrdDisOrderDistribution().getId());
        OrdDisOrderDistOut ordDisOrderDistOut = this.saveUpdate(ordDisOrderDistIn, checkDisOrderOut);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDisOrderDistIn.getOrdDisOrderDistribution().getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                idIsNull ? OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_SAVE.getName() : OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_UPDATE.getName(),
                new Date(), ordDisOrderDistIn.getOrdDisOrderDistribution().getUpdater());
        logService.sendAsyncSaveLogByMq(businessLog);
        return ordDisOrderDistOut;
    }

    /**
     * 查询配销分货单
     *
     * @param distributionOrderId 分货单id
     * @return
     */
    @Override
    public Response<OrdDisOrderDistOut> getDisOrderDist(Long distributionOrderId) {
        //查询分货单
        OrdDisOrderDistribution dbDisOrder = this.getOrdDisOrderDistribution(distributionOrderId);
        if (Objects.isNull(dbDisOrder)) {
            return Response.error("配销分货单不存在");
        }
        //查询明细
        List<OrdDisOrderDistributionDetail> details = ordDisOrderDistributionDetailService.getDetailByOrdDistributionOrderId(distributionOrderId);
        //结果集
        OrdDisOrderDistOut ordDisOrderDistOut = new OrdDisOrderDistOut();
        //明细处理
        this.addDistDetails(ordDisOrderDistOut, dbDisOrder, details);
        return Response.data(ordDisOrderDistOut);
    }

//    /**
//     * 配销分货单审核
//     *
//     * @param ordDisOrderDistIn 配销分货单入参
//     * @return
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public OrdDisOrderDistOut audit(OrdDisOrderDistIn ordDisOrderDistIn) {
//        //校验
//        CheckDisOrderOut checkDisOrderOut = this.verifyOrder(ordDisOrderDistIn);
//        //保存
//        OrdDisOrderDistOut ordDisOrderDistOut = this.saveUpdate(ordDisOrderDistIn, checkDisOrderOut);
//        //获取配销分货单
//        OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistIn.getOrdDisOrderDistribution();
//
//        //立即生效则创建订货单任务
//        if (NumberUtil.INTEGER_ONE.equals(ordDisOrderDistribution.getIsEffectiveImmediately())) {
//            String loginUsername = UserUtil.getNickname() + "【" + UserUtil.getJobNumber() + "】";
//            this.disDistributionInitOrder(ordDisOrderDistribution.getId(), ordDisOrderDistribution.getEffectiveTime(), ordDisOrderDistribution.getBizOrgCode(), loginUsername);
//        }
//        return ordDisOrderDistOut;
//    }

//    /**
//     * 分货单处理分货商品信息
//     *
//     * @param distributionOrderId
//     * @param effectiveTime
//     * @param loginUsername
//     * @param bizOrgCode
//     * @return
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public String handlePurchaseListForDistributionOrder(Long distributionOrderId, LocalDateTime effectiveTime, String loginUsername, String bizOrgCode) {
//        //逗号拼接 门店信息和门店code
//        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
//        AtomicInteger failedStoreNumber = new AtomicInteger();
//        OrdDisOrderDistribution ordDisOrderDistribution = this.getOrdDisOrderDistribution(distributionOrderId);
//        if (Objects.isNull(ordDisOrderDistribution)) {
//            log.error("配销分货单不存在-----------------------------------");
//            return null;
//        }
//        if (!OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(ordDisOrderDistribution.getDistributionOrderStatus())) {
//            log.error("配销分货单{}状态不正确", ordDisOrderDistribution.getDistributionOrderNo());
//            return null;
//        }
//        // 将门店分货商品转化为购物车模式
//        Map<String, List<OrderCartOut>> distributionGoodsOrderCartMap = this.initDistributionGoodsToOrderCartOut(distributionOrderId, loginUsername, bizOrgCode);
//        for (Map.Entry<String, List<OrderCartOut>> stringListEntry : distributionGoodsOrderCartMap.entrySet()) {
//            //门店code
//            String storeCode = stringListEntry.getKey();
//            //购物车结果集
//            List<OrderCartOut> orderCartOuts = stringListEntry.getValue();
//            //根据门店code查询门店信息
//            StoreInfoIn storeInfoIn = new StoreInfoIn();
//            storeInfoIn.setStoreCode(storeCode);
//            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(storeCode);
//            if (Objects.isNull(storeOut)) {
//                log.error("不存在的门店{}", storeCode);
//                continue;
//            }
//            if (CollectionUtils.isEmpty(orderCartOuts)) {
//                log.error("配销分货单{}门店{}分货检查可订货商品异常。", ordDisOrderDistribution.getDistributionOrderNo(), storeCode);
//                errorJoiner.add(storeCode);
//                failedStoreNumber.getAndIncrement();
//                continue;
//            }
//            try {
//                this.createOrder(storeCode, ordDisOrderDistribution, effectiveTime, orderCartOuts, loginUsername, bizOrgCode);
////                // 添加生成订订货单日志
////                BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
////                        OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
////                        String.valueOf(distributionOrderId),
////                        OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
////                        OrdLogTypeEnum.DIS_ORDER_DISTRIBUTION_CREATE_ORDER.getName(), new Date(), loginUsername);
////                logService.saveLog(businessLog);
//            } catch (Exception e) {
//                log.error("门店" + storeCode + "分货异常,分货单号：" + ordDisOrderDistribution.getDistributionOrderNo(), e);
//                errorJoiner.add(storeCode);
//                failedStoreNumber.getAndIncrement();
//            }
//        }
//        // 更新分货单状态
//        OrdExamineDistributionOrderIn orderIn = new OrdExamineDistributionOrderIn();
//        orderIn.setEffectiveTime(effectiveTime);
//        orderIn.setDistributionOrderId(ordDisOrderDistribution.getId());
//        this.submitDistributionOrder(orderIn, NumberUtil.INTEGER_ONE);
//        this.updatedisDistributionOrderExexuted(ordDisOrderDistribution.getId(), loginUsername);
//        String content = "";
//        int successStoreNumber = distributionGoodsOrderCartMap.size() - failedStoreNumber.get();
//        //门店全部成功生成订货单
//        if (NumberUtil.INTEGER_ZERO.equals(failedStoreNumber.get())) {
//            content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_ORDER_SUCCESS.getKey(), successStoreNumber, failedStoreNumber.get());
//        } else {
//            content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_ORDER_ERROR.getKey(), successStoreNumber, failedStoreNumber.get(), errorJoiner.toString());
//        }
//        // 添加生成订订货单日志
//        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
//                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
//                String.valueOf(distributionOrderId),
//                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
//                content, new Date(), loginUsername);
//        logService.sendAsyncSaveLogByMq(businessLog);
//        return errorJoiner.toString();
//    }

    @Override
    public Response<String> handleAudit(OrdDisDistributionAuditIn ordDirDistributionAuditIn, String loginUsername) {
        OrdDisOrderDistribution distributionOrder = ordDisOrderDistributionMapper.selectByPrimaryKey(ordDirDistributionAuditIn.getDistributionOrderId());
        if (Objects.isNull(distributionOrder)) {
            return Response.error("配销分货单不存在");
        }
        String key = SystemConstant.ORD_DIS_DISTRIBUTION_ORDER_AUDIT + SystemConstant.COLON + distributionOrder.getBizOrgCode()
                + SystemConstant.COLON + distributionOrder.getDistributionOrderNo();
        if (!redisService.setIfAbsent(key, distributionOrder.getDistributionOrderNo(), 20l, TimeUnit.MINUTES)) {
            return Response.error("审核中，请勿重复审核");
        }
        if (OrderDistributionOrderStatusEnum.INVALID.getKey().equals(distributionOrder.getDistributionOrderStatus())) {
            return Response.error("分货单已作废");
        }
        if (!OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(distributionOrder.getDistributionOrderStatus())) {
            return Response.error("配销分货单状态不正确");
        }
        int detailCount = ordDisOrderDistributionDetailService.countByDistributionOrderId(ordDirDistributionAuditIn.getDistributionOrderId());
        if (detailCount == NumberUtil.INTEGER_ZERO) {
            return Response.error("配销分货单明细为空");
        }
        disDistributionOrderAuditHandle.asyncCheckByAudit(ordDirDistributionAuditIn, loginUsername, key);
        return Response.success("审核中，请稍后查看");
    }

    @Override
    public String handleDistributionCreateOrder(Long distributionOrderId, String loginUsername) {
        OrdDisOrderDistribution ordDisOrderDistribution = ordDisOrderDistributionMapper.selectByPrimaryKey(distributionOrderId);
        String key = DisSystemConstant.REDIS_DIS_HANDLE_DISTRIBUTION_CREATE_ORDER + ordDisOrderDistribution.getBizOrgCode() + SystemConstant.COLON + ordDisOrderDistribution.getDistributionOrderNo();
        if (!redisService.setIfAbsent(key, ordDisOrderDistribution.getDistributionOrderNo(), 2L, TimeUnit.MINUTES)) {
            return "重复消费";
        }
        String beforeStatusCode = ordDisOrderDistribution.getDistributionOrderStatus();
        if (!OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(beforeStatusCode)) {
            return "分货单状态不正确";
        }
        Map<String, List<CreateOrderSkuIn>> storeOrderCartMap = this.initStoreOrderCartMap(ordDisOrderDistribution.getId());
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        AtomicInteger failedStoreNumber = new AtomicInteger();
        storeOrderCartMap.entrySet().forEach(entry -> {
            String storeCode = entry.getKey();
            if (NumberUtil.INTEGER_ZERO.equals(ordDisOrderDistribution.getIsEffectiveImmediately())) {
                //校验门店与业务状态开关
                StoreAndStatusSwitchOut storeAndStatusSwitch = storeCenterService.getStoreAndStatusSwitch(storeCode, ordDisOrderDistribution.getBizOrgCode());
                if (Objects.isNull(storeAndStatusSwitch)) {
                    log.error("{}不存在或不可用", storeCode);
                    errorJoiner.add(storeCode);
                    return;
                }
                StoreStatusBusinessSwitch storeStatusBusinessSwitch = storeAndStatusSwitch.getStoreStatusBusinessSwitch();
                if (Objects.isNull(storeStatusBusinessSwitch)) {
                    log.error("{}业务状态未配置", storeCode);
                    errorJoiner.add(storeCode);
                    return;
                }
                if (ModelConst.DELETE.NO.equals(storeStatusBusinessSwitch.getIsAllotDisSc())) {
                    log.error("{}不允许分货", storeCode);
                    errorJoiner.add(storeCode);
                    return;
                }
            }
            List<CreateOrderSkuIn> createOrderSkuInList = entry.getValue();
            //生成订单
            try {
                orderHandle.createDistributionOrder(storeCode, loginUsername, ordDisOrderDistribution.getId(),
                        createOrderSkuInList, ordDisOrderDistribution.getBizOrgCode(), ordDisOrderDistribution.getDistributionIdentification());
            } catch (Exception e) {
                log.error("加盟分货单{}创建门店{}订货单异常", ordDisOrderDistribution.getDistributionOrderNo(), storeCode, e);
                errorJoiner.add(storeCode);
                failedStoreNumber.getAndIncrement();
                // 记录异常
                OrdDisOrderDistributionResult ordDisOrderDistributionResult = new OrdDisOrderDistributionResult();
                ordDisOrderDistributionResult.setDistributionOrderId(ordDisOrderDistribution.getId());
                ordDisOrderDistributionResult.setStoreCode(storeCode);
                ordDisOrderDistributionResult.setIsDone(2);
                ordDisOrderDistributionResult.setUpdater(loginUsername);
                ordDisOrderDistributionResult.setUpdateTime(LocalDateTime.now());
                ordDisOrderDistributionResult.setRemark(e.getMessage());
                ordDisOrderDistributionResultService.updateByDistributionOrderIdAndStoreCode(ordDisOrderDistributionResult);
            }
        });
        ordDisOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.EXECUTED.getKey());
        ordDisOrderDistribution.setIsEnd(NumberUtil.INTEGER_ONE);
        ordDisOrderDistribution.setUpdater(loginUsername);
        ordDisOrderDistribution.setUpdateTime(LocalDateTime.now());
        ordDisOrderDistributionMapper.updateByPrimaryKeySelective(ordDisOrderDistribution);
        int successStoreNumber = storeOrderCartMap.size() - failedStoreNumber.get();
        String content;
        //门店全部成功生成订货单
        if (NumberUtil.INTEGER_ZERO.equals(failedStoreNumber.get())) {
            content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_ORDER_SUCCESS.getKey(), successStoreNumber, failedStoreNumber.get());
        } else {
            content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_ORDER_ERROR.getKey(), successStoreNumber, failedStoreNumber.get(), errorJoiner.toString());
        }
        // 添加生成订订货单日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDisOrderDistribution.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                content, new Date(), loginUsername);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return errorJoiner.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDisDistributionOrder(OrdDisOrderDistribution ordDisOrderDistribution) {
        String distributionOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KFH.getCode(), ordDisOrderDistribution.getBizOrgCode(), uniqueUtils, 4);
        ordDisOrderDistribution.setDistributionOrderNo(distributionOrderNo);
        ordDisOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey());
        ordDisOrderDistribution.setIsEnd(ModelConst.ENABLE.NO);
        ordDisOrderDistribution.setIsDelete(ModelConst.DELETE.NO);
        ordDisOrderDistributionMapper.insert(ordDisOrderDistribution);
        return ordDisOrderDistribution.getId();
    }

    @Override
    public Response<Long> asyncImportDistributionDetail(String fileId, OrdDisOrderDistribution ordDisOrderDistribution,
                                                        String loginUsername, String distributionIdentification) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DisDistributionAsyncListener listener = new DisDistributionAsyncListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdDistributionStoreGoodsVO.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(0).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > 0) {
            return Response.data(ordDisOrderDistribution.getId(), listener.getImportErrorMessage(totalErrorMap));
        }
        // 异步导入入库
        disDistributionOrderImportHandle.handleAsyncDirDistribution(listener.getImportResultMap(),
                ordDisOrderDistribution.getId(), loginUsername, "", distributionIdentification);
        return Response.data(ordDisOrderDistribution.getId(), "文件导入中，稍后刷新查看！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDisOrderDistribution getDisOrderDistributionForImport(Long distributionOrderId, String loginUsername, Integer isEffectiveImmediately,
                                                                    LocalDateTime effectiveTime, String distributionIdentification) {
        OrdDisOrderDistribution ordDisOrderDistribution = new OrdDisOrderDistribution();
        ordDisOrderDistribution.setUpdater(loginUsername);
        ordDisOrderDistribution.setIsEffectiveImmediately(isEffectiveImmediately);
        ordDisOrderDistribution.setEffectiveTime(effectiveTime);
        ordDisOrderDistribution.setDistributionIdentification(distributionIdentification);
        if (Objects.isNull(distributionOrderId)) {
            ordDisOrderDistribution.setBizOrgCode(UserUtil.getBizOrgCode());
            ordDisOrderDistribution.setCreator(loginUsername);
            ordDisOrderDistribution.setUpdater(loginUsername);
            String distributionOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KFH.getCode(), ordDisOrderDistribution.getBizOrgCode(), uniqueUtils, 4);
            ordDisOrderDistribution.setDistributionOrderNo(distributionOrderNo);
            ordDisOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey());
            ordDisOrderDistribution.setIsEnd(ModelConst.ENABLE.NO);
            ordDisOrderDistribution.setIsDelete(ModelConst.DELETE.NO);
            ordDisOrderDistributionMapper.insert(ordDisOrderDistribution);
            // 添加日志
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                    String.valueOf(ordDisOrderDistribution.getId()),
                    OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                    OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_SAVE.getName(),
                    new Date(), ordDisOrderDistribution.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        } else {
            ordDisOrderDistribution = ordDisOrderDistributionMapper.selectByPrimaryKey(distributionOrderId);
            ordDisOrderDistributionMapper.updateByPrimaryKeySelective(ordDisOrderDistribution);
            // 添加日志
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                    String.valueOf(ordDisOrderDistribution.getId()),
                    OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                    OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_UPDATE.getName(),
                    new Date(), ordDisOrderDistribution.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return ordDisOrderDistribution;
    }

    @Override
    public List<Long> findNeedExecuteList() {
        return ordDisOrderDistributionMapper.findNeedExecuteList(OrderDistributionOrderStatusEnum.APPROVAL.getKey(), DateUtils.format(LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> updateHead(UpdateDistributionEffectiveTimeIn updateDistributionEffectiveTimeIn) {
        OrdDisOrderDistribution distributionOrder = ordDisOrderDistributionMapper.selectByPrimaryKey(updateDistributionEffectiveTimeIn.getDistributionOrderId());
        if (Objects.isNull(distributionOrder)) {
            return Response.error("配销分货单不存在");
        }
        if (OrderDistributionOrderStatusEnum.INVALID.getKey().equals(distributionOrder.getDistributionOrderStatus())) {
            return Response.error("分货单已作废");
        }
        //立即生效则创建订货单任务
        if (NumberUtil.INTEGER_ONE.equals(updateDistributionEffectiveTimeIn.getIsEffectiveImmediately())) {
            distributionOrder.setEffectiveTime(LocalDateTime.now());
        } else {
            distributionOrder.setEffectiveTime(updateDistributionEffectiveTimeIn.getEffectiveTime());
        }
        distributionOrder.setIsEffectiveImmediately(updateDistributionEffectiveTimeIn.getIsEffectiveImmediately());
        distributionOrder.setUpdater(UserUtil.getUserName());
        distributionOrder.setUpdateTime(LocalDateTime.now());
        ordDisOrderDistributionMapper.updateByPrimaryKey(distributionOrder);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getName(),
                String.valueOf(distributionOrder.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIS_ORDER_DISTRIBUTION_UPDATE.getName(), new Date(), distributionOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success();
    }


    private Map<String, List<CreateOrderSkuIn>> initStoreOrderCartMap(Long distributionOrderId) {
        // 按门店提交分货单下商品到订货清单与订单中
        List<OrdDisOrderDistributionDetail> distributionStoreList = ordDisOrderDistributionDetailService.getDetailByOrdDistributionOrderId(distributionOrderId);
        Map<String, List<CreateOrderSkuIn>> storeOrderCartMap = new LinkedHashMap<>();
        Map<String, List<OrdDisOrderDistributionDetail>> storeGoodsMap = distributionStoreList.stream()
                .collect(Collectors.groupingBy(OrdDisOrderDistributionDetail::getStoreCode));
        storeGoodsMap.forEach((storeCode, ordDirOrderDistributionDetails) -> {
            List<CreateOrderSkuIn> createOrderSkuInList = new ArrayList<>();
            ordDirOrderDistributionDetails.forEach(distributionDetail -> {
                CreateOrderSkuIn createOrderSkuIn = new CreateOrderSkuIn();
                createOrderSkuIn.setGoodsCode(distributionDetail.getGoodsCode());
                createOrderSkuIn.setPackageQuantity(distributionDetail.getPackingNumber());
                createOrderSkuInList.add(createOrderSkuIn);
            });
            storeOrderCartMap.put(storeCode, createOrderSkuInList);
        });
        return storeOrderCartMap;
    }


    @Override
    public String getDistributionOrderNoByOrderId(Long orderId, String bizOrgCode) {
        return ordDisOrderDistributionMapper.getDistributionOrderNoByOrderId(orderId, bizOrgCode);
    }

    /**
     * 保存或更新
     *
     * @param ordDisOrderDistIn 配销分货单保存更新入参
     * @param checkDisOrderOut  校验通过返回结果
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public OrdDisOrderDistOut saveUpdate(OrdDisOrderDistIn ordDisOrderDistIn, CheckDisOrderOut checkDisOrderOut) {
        //获取分货单信息
        OrdDisOrderDistribution ordDisOrder = ordDisOrderDistIn.getOrdDisOrderDistribution();
        //获取明细信息
        List<OrdDisOrderDistributionDetail> details = ordDisOrderDistIn.getOrdDisOrderDistributionDetails();
        //获取总数量和总金额
        Map<String, BigDecimal> sumMap = this.quantityAndAmountSum(details, checkDisOrderOut.getGoodsOut(), ordDisOrder.getBizOrgCode());
        //配销分货总金额 初始为
        ordDisOrder.setDistributionTotalAmount(sumMap.get(OrdDisMapKeyConstant.TOTAL_AMOUNT));
        //配销分货总数量
        ordDisOrder.setDistributionTotalQuantity(sumMap.get(OrdDisMapKeyConstant.TOTAL_QUANTITY));
//        //仓位代码
//        ordDisOrder.setStockCode(checkDisOrderOut.getGoodsOut().getStockCode());
//        //仓位名称
//        ordDisOrder.setStockName(checkDisOrderOut.getGoodsOut().getStockName());
        if (Objects.isNull(ordDisOrder.getId())) {
            /** 新增 */
            //生成分货单单号
            ordDisOrder.setDistributionOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KXF.getCode(), ordDisOrder.getBizOrgCode(), uniqueUtils, 4));
            //已完结 初始为0
            ordDisOrder.setIsEnd(NumberUtil.INTEGER_ZERO);
            ordDisOrderDistributionMapper.insertSelective(ordDisOrder);
        } else {
            /** 更新 */
            OrdDisOrderDistribution dbOrder = this.getOrdDisOrderDistribution(ordDisOrder.getId());
            if (Objects.isNull(dbOrder)) {
                throw new BusinessException("配销分货单不存在");
            }
            if (!OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(dbOrder.getDistributionOrderStatus())) {
                throw new BusinessException("不可修改：配销出货单状态为=>" + OrderDistributionOrderStatusEnum.getValueByKey(dbOrder.getDistributionOrderStatus()));
            }
            ordDisOrderDistributionMapper.updateByPrimaryKeySelective(ordDisOrder);
        }
        //结果集
        OrdDisOrderDistOut ordDisOrderDistOut = new OrdDisOrderDistOut();
        //copy出货单
        BeanUtils.copy(ordDisOrder, ordDisOrderDistOut);

        /** 保存明细 */
        ordDisOrderDistributionDetailService.save(ordDisOrder, details, NumberUtil.INTEGER_ZERO);
//        //添加明细并中文转换
//        this.addDistDetails(ordDisOrderDistOut, ordDisOrder, details);
        //添加出货单id
        ordDisOrderDistOut.setDistributionOrderId(ordDisOrder.getId());
        return ordDisOrderDistOut;
    }


    /**
     * 添加分货门店/商品明细并且中文转换
     *
     * @param ordDisOrderDistOut 结果集
     * @param ordDisOrder        配销分货单
     * @param details            明细
     */
    private void addDistDetails(OrdDisOrderDistOut ordDisOrderDistOut,
                                OrdDisOrderDistribution ordDisOrder,
                                List<OrdDisOrderDistributionDetail> details) {
        //copy同属性
        BeanUtils.copy(ordDisOrder, ordDisOrderDistOut);
        //添加出货单id
        ordDisOrderDistOut.setDistributionOrderId(ordDisOrder.getId());
        //提交人
        ordDisOrderDistOut.setCreatorId(ordDisOrder.getCreator());
        //分货总数量
        ordDisOrderDistOut.setTotalDistributionQuantity(ordDisOrder.getDistributionTotalQuantity());
        //状态中文转换
        ordDisOrderDistOut.setDistributionOrderStatusValue(OrderDistributionOrderStatusEnum.getValueByKey(ordDisOrder.getDistributionOrderStatus()));
        ordDisOrderDistOut.setDistributionIdentificationStr(DistributionIdentificationEnum.getNameByCode(ordDisOrder.getDistributionIdentification()));
        //处理明细
        List<OrdDisOrderDistributionDetailOut> detailOuts = details.stream().map(detail -> {
            //明细结果集
            OrdDisOrderDistributionDetailOut ordDisOrderDistributionDetailOut = new OrdDisOrderDistributionDetailOut();
            //copy同属性
            BeanUtils.copy(detail, ordDisOrderDistributionDetailOut);
            return ordDisOrderDistributionDetailOut;

        }).collect(Collectors.toList());

        //添加明细
        ordDisOrderDistOut.setOrderDistributionDetailOuts(detailOuts);
    }


    /**
     * 统计分货总数量和总金额
     *
     * @param details    门店/商品集合
     * @param goodsOut   商品信息
     * @param bizOrgCode 业务组织
     * @return
     */
    public Map<String, BigDecimal> quantityAndAmountSum(List<OrdDisOrderDistributionDetail> details, OrderGoodsOut goodsOut, String bizOrgCode) {
        Map<String, BigDecimal> sumMap = new HashMap<>();
        //分货总数量
        BigDecimal distributionTotalQuantity = details.stream().map(OrdDisOrderDistributionDetail::getDistributionQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        //分货总金额 商品信息为null  明细金额直接求和  不为null 则需要用商品信息的单价相乘再求和
        BigDecimal distributionTotalAmount = BigDecimal.ZERO;
//        if (Objects.isNull(goodsOut)) {
//            //明细金额直接求和
//            distributionTotalAmount = details.stream().map(
//                    OrdDisOrderDistributionDetail::getDistributionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//        } else {
        //有商品信息说明校验通过 则单价相乘再求和
        distributionTotalAmount = details.stream().map(
                x -> x.getDistributionQuantity().multiply((Objects.isNull(x.getOriginalPrice()) ? BigDecimal.ZERO : x.getOriginalPrice()))
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
//        }

        //添加总数量和总金额
        sumMap.put(OrdDisMapKeyConstant.TOTAL_AMOUNT, distributionTotalAmount);
        sumMap.put(OrdDisMapKeyConstant.TOTAL_QUANTITY, distributionTotalQuantity);

        return sumMap;
    }


    /**
     * 校验分货单及明细是否合法
     *
     * @param ordDisOrderDistIn 保存配销分货单和门店商品明细入参
     */
    public CheckDisOrderOut verifyOrder(OrdDisOrderDistIn ordDisOrderDistIn) {
        CheckDisOrderOut checkDisOrderOut = new CheckDisOrderOut();
        //获取分货单信息
//        OrdDisOrderDistribution ordDisOrder = ordDisOrderDistIn.getOrdDisOrderDistribution();
        //获取明细信息
        List<OrdDisOrderDistributionDetail> details = ordDisOrderDistIn.getOrdDisOrderDistributionDetails();
        //当前列表商品去重
        Map<String, String> checkMap = new HashMap<>(2);
        for (OrdDisOrderDistributionDetail detail : details) {
            //拼接门店code和商品code
            String joinCode = detail.getGoodsCode() + "&" + detail.getStoreCode();
            //校验门店code和商品code组合是否重复
            if (checkMap.containsKey(joinCode)) {
                throw new BusinessException("商品:" + detail.getGoodsCode() + "和门店：" + detail.getStoreCode() + "组合重复");
            }
//            checkDisOrderOut = this.checkDisOrder(detail.getGoodsCode(), detail.getStoreCode(), ordDisOrder.getBizOrgCode());
            checkMap.put(joinCode, joinCode);
        }
        return checkDisOrderOut;
    }


    /**
     * 校验门店/商品信息
     *
     * @param goodsCode  商品代码
     * @param storeCode  门店代码
     * @param bizOrgCode 业务组织
     */
    public CheckDisOrderOut checkDisOrder(String goodsCode, String storeCode, String bizOrgCode) {
        if (StringUtils.isBlank(storeCode)) {
            throw new BusinessException("门店信息不能为空");
        }
        if (StringUtils.isBlank(goodsCode)) {
            throw new BusinessException("商品信息不能为空");
        }
        //校验通过保存门店商品信息
        CheckDisOrderOut checkDisOrderOut = new CheckDisOrderOut();
        //查询门店是否存在且符合规则
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(storeCode);
        if (Objects.isNull(storeOut)) {
            throw new BusinessException("门店" + storeCode + "不存在");
        }
        if (!StoreConstant.StoreProperty.FRANCHISE.getMytValue().equals(storeOut.getStoreType())) {
            throw new BusinessException(storeCode + "门店是直营门店，不可在配销管理分货！");
        }
        // 获取商品信息
        OrderGoodsIn goodsIn = new OrderGoodsIn();
        goodsIn.setStoreCode(storeCode);
        goodsIn.setBizOrgCode(bizOrgCode);
        goodsIn.setGoodsCode(goodsCode);
        goodsIn.setStoreProperty(storeOut.getStoreType());
        OrderGoodsOut goodsOut = orderGoodsServer.getSwitchGoodsInfo(goodsIn);
        // 商品校验
        if (Objects.isNull(goodsOut)) {
            throw new BusinessException("商品" + goodsCode + "不存在");
        }
        //门店商品上下架
//        if (NumberUtil.INTEGER_ONE.equals(goodsOut.getIsShelves())) {
//            throw new BusinessException(storeCode + "门店下的" + goodsCode + "商品已下架");
//        }
        //商品开关信息
        GoodsStatusBusinessSwitch goodsStatusBusinessSwitch = goodsOut.getGoodsStatusBusinessSwitch();
        if (Objects.isNull(goodsStatusBusinessSwitch)) {
            throw new BusinessException("商品" + goodsCode + "开关未配置，请联系业务配置人员");
        }

        if (!NumberUtil.INTEGER_ONE.equals(goodsStatusBusinessSwitch.getIsAllotDis())) {
            throw new BusinessException("商品" + goodsCode + "不可被分货");
        }
        Integer qpc = goodsOut.getDistributionSpecification().getQpc();
        if (Objects.isNull(qpc)) {
            throw new BusinessException("商品" + goodsCode + "配货规格数量不合法");
        }

        if (Objects.isNull(goodsOut.getDistributionUnitPrice())) {
            throw new BusinessException("商品" + goodsCode + "配送价为空");
        }

        if (StringUtils.isEmpty(goodsOut.getDistributionWay())) {
            throw new BusinessException("门店" + storeCode + "所属配送方案不存在该商品" + goodsCode);
        }

        if (!DistributionWaysEnum.UNIFIEDDIS.getType().equals(goodsOut.getDistributionWay())
                && !DistributionWaysEnum.TRANSFER.getType().equals(goodsOut.getDistributionWay())) {
            throw new BusinessException("当前配送方式是" + DistributionWaysEnum.getNameByType(goodsOut.getDistributionWay()) + "不是统配或中转");
        }

        if (StringUtils.isEmpty(goodsOut.getStockCode()) || StringUtils.isEmpty(goodsOut.getStockName())) {
            throw new BusinessException("商品" + goodsCode + "仓位为空");
        }

        if (Objects.isNull(goodsOut.getDistributionSpecification()) || StringUtils.isEmpty(goodsOut.getDistributionSpecification().getQpcStr())) {
            throw new BusinessException("商品" + goodsCode + "配货规格为空");
        }

        //校验通过 保存信息
        checkDisOrderOut.setStoreOut(storeOut);
        checkDisOrderOut.setGoodsOut(goodsOut);

        return checkDisOrderOut;
    }
}
