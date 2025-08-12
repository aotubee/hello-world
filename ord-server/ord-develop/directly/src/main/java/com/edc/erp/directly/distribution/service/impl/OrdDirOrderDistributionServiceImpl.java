package com.edc.erp.directly.distribution.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
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
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirordercart.service.ShoppingCartService;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionDetail;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionResult;
import com.edc.erp.directly.distribution.excel.ExportOrdDirOrderDetail;
import com.edc.erp.directly.distribution.handle.DirDistributionOrderAuditHandle;
import com.edc.erp.directly.distribution.handle.DirDistributionOrderImportHandle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.listener.DirDistributionAsyncListener;
import com.edc.erp.directly.distribution.listener.OrdDirOrderStoreGoodsListener;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDistributionMapper;
import com.edc.erp.directly.distribution.model.excel.OrdDirDistributionImportErrorResult;
import com.edc.erp.directly.distribution.model.in.*;
import com.edc.erp.directly.distribution.model.out.*;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionDetailService;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionResultService;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionService;
import com.edc.erp.directly.distribution.util.FileExportUtil;
import com.edc.erp.directly.enumeration.DistErrorMessageEnum;
import com.edc.erp.directly.enumeration.DistributionOrderLogEnum;
import com.edc.erp.directly.enumeration.OrderDistributionOrderStatusEnum;
import com.edc.erp.directly.enumeration.OrderStatusEnum;
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
 * 直营分货单(OrdDirOrderDistribution)表服务实现类
 *
 * @author lixuejun
 * @since 2022-09-26 11:46:13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDirOrderDistributionServiceImpl extends BaseServiceImpl<OrdDirOrderDistribution> implements OrdDirOrderDistributionService {

    private final OrdDirOrderDistributionMapper ordDirOrderDistributionMapper;

    private final AsyncLogService asyncLogService;

    private final UniqueUtils uniqueUtils;

    private final StoreCenterService storeCenterService;

    private final OrdDirOrderDistributionDetailService ordDirOrderDistributionDetailService;

    private final OrderHandle orderHandle;

    private final AsyncPushTaskService asyncPushTaskService;

    private final OrderGoodsServer orderGoodsServer;

    private final FileService fileService;

    private final ShoppingCartService shoppingCartService;

    private final DirDistributionOrderImportHandle dirDistributionOrderImportHandle;

    private final OrdDirOrderDistributionResultService ordDirOrderDistributionResultService;

    private final DirDistributionOrderAuditHandle dirDistributionOrderAuditHandle;

    private final RedisService redisService;

    private final AsyncExportHandle asyncExportHandle;

    /**
     * 逻辑删除直营分货单
     *
     * @param orderDistribution 直营分货单实体
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int logicDelOrdDirOrderDistribution(OrdDirOrderDistribution orderDistribution) {
        int delete = ordDirOrderDistributionMapper.logicDeleteByPrimaryKey(orderDistribution);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                String.valueOf(orderDistribution.getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_DELETE.getName(), new Date(), orderDistribution.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return delete;
    }


    /**
     * 直营分货单分货单更新
     *
     * @param orderDistribution 直营分货单实体
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrdDirOrderDistribution(OrdDirOrderDistribution orderDistribution) {
        int update = ordDirOrderDistributionMapper.updateByPrimaryKeySelective(orderDistribution);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                String.valueOf(orderDistribution.getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_UPDATE.getName(), new Date(), orderDistribution.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return update;
    }


    /**
     * 查询直营分货单表头
     *
     * @param distributionOrderId 分货单主键
     * @return
     */
    @Override
    public BackHeaderOrdDistributionOrderOut getHeaderOrdDistributionOrderOutById(Long distributionOrderId) {
        OrdDirOrderDistribution ordDirOrderDistribution = this.getOrdDirOrderDistribution(distributionOrderId);
        if (Objects.isNull(ordDirOrderDistribution)) {
            return new BackHeaderOrdDistributionOrderOut();
        }
        BackHeaderOrdDistributionOrderOut ordDistributionOrderOut = new BackHeaderOrdDistributionOrderOut();
        BeanUtils.copy(ordDirOrderDistribution, ordDistributionOrderOut);
        ordDistributionOrderOut.setDistributionOrderId(distributionOrderId);
        ordDistributionOrderOut.setCreatorId(ordDirOrderDistribution.getCreator());
        ordDistributionOrderOut.setCreateTime(ordDirOrderDistribution.getCreateTime());
        //分货总数量
        ordDistributionOrderOut.setTotalDistributionQuantity(ordDirOrderDistribution.getDistributionTotalQuantity());
        //状态转中文
        ordDistributionOrderOut.setDistributionOrderStatusValue(OrderDistributionOrderStatusEnum.getValueByKey(ordDirOrderDistribution.getDistributionOrderStatus()));
        return ordDistributionOrderOut;
    }


    /**
     * 分页查询直营分货单
     *
     * @param orderDistributionIn 直营分货单实体
     * @return
     */
    @Override
    public Page<OrdDirOrderDistributionOrderOut> findOrdDistributionOrder(OrdDirOrderDistributionIn orderDistributionIn) {
        orderDistributionIn.setIsDelete(0);
        // 根据门店代码得到门店id
        String storeCode = orderDistributionIn.getStoreCode();
        if (StringUtils.isNotEmpty(storeCode)) {
            //查询门店信息
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(storeCode);
            //门店id
            orderDistributionIn.setStoreId(Objects.nonNull(storeOut) ? storeOut.getStoreId() : NumberUtil.INTEGER_ZERO);
        }
        List<OrdDirOrderDistributionOrderOut> ordDirOrderDistributionOuts = ordDirOrderDistributionMapper.findOrdDirOrderDistributionByPage(orderDistributionIn);
        ordDirOrderDistributionOuts.forEach(dirOrderDistribution -> {
            // 分货状态中文
            dirOrderDistribution.setDistributionOrderStatusValue(OrderDistributionOrderStatusEnum.getValueByKey(dirOrderDistribution.getDistributionOrderStatus()));
        });
        Page<OrdDirOrderDistributionOrderOut> page = new Page(orderDistributionIn);
        page.setList(ordDirOrderDistributionOuts);
        return page;
    }


    /**
     * 查询直营分货单
     *
     * @param id 主键
     * @return
     */
    @Override
    public OrdDirOrderDistribution getOrdDirOrderDistribution(Long id) {
        return ordDirOrderDistributionMapper.selectByPrimaryKey(id);
    }


    /**
     * 校验分货订单是否已审核状态
     *
     * @param distributionOrderId 分货单id
     * @return
     */
    @Override
    public Response<String> checkDistributionOrderStatus(Long distributionOrderId) {
        OrdDirOrderDistribution ordDirOrderDistribution = this.getOrdDirOrderDistribution(distributionOrderId);
        if (Objects.isNull(ordDirOrderDistribution)) {
            return Response.error("无效的分货订单");
        }
        if (OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(ordDirOrderDistribution.getDistributionOrderStatus())) {
            return Response.error("该分货订单已审核不能删除");
        }

        return Response.success();
    }


//    /**
//     * 直营分货单提交
//     *
//     * @param submitDistributionOrderIn 审核入参
//     * @param isEnd                     是否完结
//     * @return
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public int submitDistributionOrder(OrdExamineDistributionOrderIn submitDistributionOrderIn, Integer isEnd) {
//        OrdDirOrderDistribution distributionOrder = this.selectByPrimaryKey(submitDistributionOrderIn.getDistributionOrderId());
//        //审核
//        distributionOrder.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.APPROVAL.getKey());
//        distributionOrder.setId(submitDistributionOrderIn.getDistributionOrderId());
//        distributionOrder.setUpdateTime(LocalDateTime.now());
//        distributionOrder.setIsEnd(isEnd);
//        distributionOrder.setIsEffectiveImmediately(submitDistributionOrderIn.getIsEffectiveImmediately());
//        distributionOrder.setEffectiveTime(submitDistributionOrderIn.getEffectiveTime());
//        //更新出货单
//        int updateCount = this.updateOrdDirOrderDistribution(distributionOrder);
//        //更新和保存明细
//        // 单据审核，明细不能在变动
////        ordDirOrderDistributionDetailService.save(distributionOrder, submitDistributionOrderIn.getOrdDirOrderDistributionDetails(), NumberUtil.INTEGER_ONE);
//
//        return updateCount;
//    }
//
//
//    /**
//     * @param distributionOrderId:
//     * @param loginUsername:
//     * @param bizOrgCode:
//     * @Description: 初始化订单
//     * @Author: ZhangYao
//     * @Date: 2023/3/10 16:18
//     * @return: java.util.Map<java.lang.String, java.util.List < com.edc.erp.directly.distribution.model.out.OrderCartOut>>
//     **/
//    @Override
//    public Map<String, List<OrderCartOut>> initDistributionGoodsToOrderCartOut(Long distributionOrderId, String loginUsername, String bizOrgCode) {
//        // 按门店提交分货单下商品到订货清单与订单中
//        OrdDirOrderDistributionDetail query = new OrdDirOrderDistributionDetail();
//        query.setDistributionOrderId(distributionOrderId);
//        query.setIsDelete(ModelConst.DELETE.NO);
//        List<OrdDirOrderDistributionDetail> distributionStoreList = ordDirOrderDistributionDetailService.list(query);
//        Map<String, List<OrderCartOut>> storeOrderCartMap = new LinkedHashMap<>();
//        LinkedHashSet<String> errorSet = new LinkedHashSet<>();
//        Map<String, List<OrdDirOrderDistributionDetail>> storeGoodsMap = distributionStoreList.stream()
//                .collect(Collectors.groupingBy(OrdDirOrderDistributionDetail::getStoreCode));
//        storeGoodsMap.forEach((storeCode, ordDirOrderDistributionDetails) -> {
//            List<OrderCartGoodsOut> orderCartOutList = new ArrayList<>();
//            ordDirOrderDistributionDetails.forEach(distributionDetail -> {
//                OrderCartOut orderCartOut = new OrderCartOut();
//                orderCartOut.setGoodsCode(distributionDetail.getGoodsCode());
//                orderCartOut.setQuantity(distributionDetail.getPackingNumber());
//                OrderCartGoodsOut orderCartGoodsOut = this.initWaiteDistributionSku(bizOrgCode, storeCode, orderCartOut);
//                orderCartOutList.add(orderCartGoodsOut);
//            });
//            DirCheckDistributionSkuOut dirCheckDistributionSkuOut = this.filterSkuForDistributionOrder(orderCartOutList, SourceTypeEnum.DISTRIBUTION.getKey());
//            storeOrderCartMap.put(storeCode, dirCheckDistributionSkuOut.getLegalOrderCartGoodsOutList());
//            if (CollectionUtils.isNotEmpty(dirCheckDistributionSkuOut.getErrorMessageSet())) {
//                errorSet.addAll(dirCheckDistributionSkuOut.getErrorMessageSet());
//            }
//        });
//        if (CollectionUtils.isNotEmpty(errorSet)) {
//            String content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_FILTER_SKU.getKey(),
//                    errorSet.stream().map(String::valueOf).collect(Collectors.joining(SystemConstant.COMMA)));
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
//                    String.valueOf(distributionOrderId), OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
//                    content, new Date(), SystemConstant.SYSTEM_USER);
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        }
//        return storeOrderCartMap;
//    }
//
//    /**
//    /**
//     * @param bizOrgCode:
//     * @param storeCode:
//     * @param orderCartOut:
//     * @Description: 初始化分货转订货单购物车商品
//     * @Author: ZhangYao
//     * @Date: 2023/3/10 16:20
//     * @return: com.edc.erp.directly.distribution.model.out.OrderCartGoodsOut
//     **/
//    private OrderCartGoodsOut initWaiteDistributionSku(String bizOrgCode, String storeCode, OrderCartOut orderCartOut) {
//        OrderCartGoodsOut orderCartGoodsOut = new OrderCartGoodsOut();
//        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
//        orderGoodsIn.setStoreCode(storeCode);
//        orderGoodsIn.setGoodsCode(orderCartOut.getGoodsCode());
//        orderGoodsIn.setBizOrgCode(bizOrgCode);
//        //查询是否允许商品订货信息
//        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DISTRIBUTION.getType());
//        orderGoodsIn.setSourceCode(SourceTypeEnum.DISTRIBUTION.getKey());
//        OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
//        orderCartGoodsOut.setGoodsCode(orderCartOut.getGoodsCode());
//        if (Objects.isNull(orderGoodsOut)) {
//            orderCartGoodsOut.setIsEnable(ModelConst.ENABLE.NO);
//        } else {
//            orderCartGoodsOut.setPackageQuantity(orderCartOut.getQuantity());
//            orderCartGoodsOut.setIsEnable(ModelConst.ENABLE.YES);
//            orderCartGoodsOut.setIsShelves(orderGoodsOut.getIsShelves());
//            // 是否可以加购
//            FlashSaleCheckOut flashSaleCheckOut = shoppingCartService.isCanBuyFlashSaleGoods(orderCartOut.getGoodsCode(), bizOrgCode);
//            orderCartGoodsOut.setIsCanBuyFlashSale(flashSaleCheckOut.getIsCanBuyFlashSale());
//        }
//        return orderCartGoodsOut;
//    }
//
//    /**
//     * @param orderCartGoodsOutList:
//     * @param sourceCode:
//     * @Description: 过滤购物车商品
//     * @Author: ZhangYao
//     * @Date: 2023/3/10 16:27
//     * @return: com.edc.erp.directly.distribution.model.out.DirCheckDistributionSkuOut
//     **/
//    private DirCheckDistributionSkuOut filterSkuForDistributionOrder(List<OrderCartGoodsOut> orderCartGoodsOutList, String sourceCode) {
//        LinkedHashSet<String> errorSet = new LinkedHashSet<>();
//        // 合法购物车商品数据集合
//        List<OrderCartOut> legalOrderCartGoodsOutList = Lists.newArrayList();
//        orderCartGoodsOutList.forEach(orderCartGoodsOut -> {
//            String errorMessage = null;
//            if (ModelConst.ENABLE.NO.equals(orderCartGoodsOut.getIsEnable())) {
//                log.error(MessageFormat.format(DistErrorMessageEnum.SKU_BUS_GATE.getValue(), orderCartGoodsOut.getGoodsCode()));
//                errorMessage = MessageFormat.format(DistErrorMessageEnum.SKU_BUS_GATE.getValue(), orderCartGoodsOut.getGoodsCode());
//            } else {
//                if (ModelConst.DELETE.YES.equals(orderCartGoodsOut.getIsShelves()) && !SourceTypeEnum.DISTRIBUTION.getKey().equals(sourceCode)) {
//                    log.error(MessageFormat.format(DistErrorMessageEnum.SKU_SHELVES.getValue(), orderCartGoodsOut.getGoodsCode()));
//                    errorMessage = MessageFormat.format(DistErrorMessageEnum.SKU_SHELVES.getValue(), orderCartGoodsOut.getGoodsCode());
//                }
//                if (ModelConst.DELETE.NO.equals(orderCartGoodsOut.getIsCanBuyFlashSale())) {
//                    log.error(MessageFormat.format(DistErrorMessageEnum.FLASH_SALE.getValue(), orderCartGoodsOut.getGoodsCode()));
//                    errorMessage = MessageFormat.format(DistErrorMessageEnum.FLASH_SALE.getValue(), orderCartGoodsOut.getGoodsCode());
//                }
//            }
//            if (StringUtils.isBlank(errorMessage)) {
//                OrderCartOut orderCartOut = new OrderCartOut();
//                orderCartOut.setGoodsCode(orderCartGoodsOut.getGoodsCode());
//                orderCartOut.setQuantity(orderCartGoodsOut.getPackageQuantity());
//                legalOrderCartGoodsOutList.add(orderCartOut);
//            } else {
//                errorSet.add(errorMessage);
//            }
//        });
//        DirCheckDistributionSkuOut checkDistributionSkuOut = new DirCheckDistributionSkuOut();
//        checkDistributionSkuOut.setLegalOrderCartGoodsOutList(legalOrderCartGoodsOutList);
//        checkDistributionSkuOut.setErrorMessageSet(errorSet);
//        return checkDistributionSkuOut;
//    }
//
//
//    /**
//     * 直营分货单创建订货单
//     *
//     * @param storeCode               门店code
//     * @param ordDirOrderDistribution 分货单结果
//     * @param effectiveTime           生肖实现
//     * @param orderCartOuts           单据购物车结果
//     * @param loginUsername           操作者
//     * @param bizOrgCode              业务组织
//     * @return
//     */
//    @Override
//    public void createOrder(String storeCode, OrdDirOrderDistribution ordDirOrderDistribution, LocalDateTime effectiveTime, List<OrderCartOut> orderCartOuts, String loginUsername, String bizOrgCode) {
//        List<CreateOrderSkuIn> createOrderSkuInList = new ArrayList<>();
//        for (OrderCartOut orderCartOut : orderCartOuts) {
//            CreateOrderSkuIn createOrderSkuIn = new CreateOrderSkuIn();
//            createOrderSkuIn.setGoodsCode(orderCartOut.getGoodsCode());
//            createOrderSkuIn.setPackageQuantity(orderCartOut.getQuantity());
//            createOrderSkuInList.add(createOrderSkuIn);
//        }
//        //生成订单
//        orderHandle.createDistributionOrderOld(storeCode, loginUsername, ordDirOrderDistribution.getId(), createOrderSkuInList, bizOrgCode);
//    }
//
//
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
//    public void dirDistributionInitOrder(Long distributionOrderId, LocalDateTime effectiveTime, String bizOrgCode, String loginUsername) {
//        //初始化订货单任务
//        DirDistributionInitOrderIn dirDistributionInitOrderIn = new DirDistributionInitOrderIn();
//        //业务组织
//        dirDistributionInitOrderIn.setBizOrgCode(bizOrgCode);
//        //分货单id
//        dirDistributionInitOrderIn.setDistributionOrderId(distributionOrderId);
//        //生效时间
//        dirDistributionInitOrderIn.setEffectiveTime(effectiveTime);
//        //当前操作者
//        dirDistributionInitOrderIn.setLoginUsername(loginUsername);
//        //提交任务
//        String tackData = JSONObject.toJSONString(dirDistributionInitOrderIn);
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_DISTRIBUTION_TO_ORDER, tackData);
//        log.info("直营分货单审核成功后创建订货单任务入参-------------------{}", tackData);
//
////        this.updateDirDistributionOrderExecuted(distributionOrderId, loginUsername);
//    }

//    /**
//     * 修改直营分货单状态为已生效
//     *
//     * @param distributionOrderId
//     * @param loginUsername
//     */
//    @Transactional(rollbackFor = Exception.class)
//    public void updateDirDistributionOrderExecuted(Long distributionOrderId, String loginUsername) {
//        //修改配货分货单状态为已完结
//        OrdDirOrderDistribution dirOrder = new OrdDirOrderDistribution();
//        dirOrder.setId(distributionOrderId);
//        dirOrder.setIsEnd(NumberUtil.INTEGER_ONE);
//        dirOrder.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.EXECUTED.getKey());
//        this.updateByPrimaryKeySelective(dirOrder);
//        //添加日志
//        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
//                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
//                String.valueOf(distributionOrderId),
//                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
//                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_EXECUTED.getName(), new Date(), loginUsername);
//
//        asyncLogService.sendAsyncSaveLogByMq(businessLog);
//    }
//
//
//    /**
//     * 根据时间和状态查询分货单
//     *
//     * @return
//     */
//    @Override
//    public List<OrdDirOrderDistributionOrderOut> findOrderByNewTimeAndStatus() {
//        return ordDirOrderDistributionMapper.findOrderByNewTimeAndStatus(OrderDistributionOrderStatusEnum.APPROVAL.getKey(), DateUtils.format(LocalDateTime.now()));
//    }


    /**
     * 导入门店/商品分货
     *
     * @param importStoreDirOrderIn 直营分货 门店/商品导入 入参
     * @param userName              登录人
     * @param bizOrgCode            业务组织
     * @return
     */
    @Override
    public Response<List<OrdDirOrderDistributionDetailOut>> importDistributionOrder(ImportStoreDirOrderIn importStoreDirOrderIn,
                                                                                    String userName, String bizOrgCode) {
        //结果集
        List<OrdDirOrderDistributionDetailOut> outs = new ArrayList<>();
        //获取商品和分货数量集合
        List<ImportStoreGoodsIn> importStoreGoodsInList = importStoreDirOrderIn.getImportStoreGoodsInList();
        if (CollectionUtils.isEmpty(importStoreGoodsInList)) {
            return Response.error("商品代码不能为空");
        }
        importStoreDirOrderIn.getStoreCodeList().forEach(storeCode -> {
            importStoreDirOrderIn.getImportStoreGoodsInList().forEach(importStoreGoodsIn -> {
                //明细结果实体
                OrdDirOrderDistributionDetailOut detailOut = new OrdDirOrderDistributionDetailOut();
                //校验通过 获取门店和商品信息
                CheckDirOrderOut checkDirOrderOut = this.checkDirOrder(importStoreGoodsIn.getSkuCode(), storeCode, bizOrgCode);
                //获取门店信息
                StoreOut storeOut = checkDirOrderOut.getStoreOut();
                //获取商品信息
                OrderGoodsOut goodsOut = checkDirOrderOut.getGoodsOut();
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
                //配货价
                detailOut.setOriginalPrice(goodsOut.getDistributionUnitPrice());
                //分货金额
                detailOut.setDistributionAmount(goodsOut.getDistributionUnitPrice().multiply(importStoreGoodsIn.getDistributionQuantity()));
                //添加至结果集
                outs.add(detailOut);
            });
        });
        return Response.data(outs, "导入门店/商品成功");
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
//    public void handleDistributionOrder(Long ordDistributionOrderId, String userName, List<OrdOrderOrderStoreGoodsIn> dataList) {
//        for (OrdOrderOrderStoreGoodsIn goodsIn : dataList) {
//            OrdDirOrderDistributionDetail dbDetail = ordDirOrderDistributionDetailService.getOrdDisOrderDistribution(ordDistributionOrderId, goodsIn.getSkuCode(), goodsIn.getStoreCode());
//            //配货分货金额
//            BigDecimal distributionAmount = goodsIn.getOriginalPrice().multiply(goodsIn.getDistributionQuantity());
//            //不存在则新增
//            if (Objects.isNull(dbDetail)) {
//                dbDetail = new OrdDirOrderDistributionDetail();
//                BeanUtils.copy(goodsIn, dbDetail);
//                dbDetail.setCreator(userName);
//                dbDetail.setUpdater(userName);
//                dbDetail.setGoodsCode(goodsIn.getSkuCode());
//                dbDetail.setDistributionAmount(distributionAmount);
//                dbDetail.setDistributionOrderId(ordDistributionOrderId);
//                dbDetail.setWrhInvQty(goodsIn.getWrhInvQty());
//                ordDirOrderDistributionDetailService.insertSelective(dbDetail);
//            } else {
//                //存在则更新
//                dbDetail.setDistributionQuantity(goodsIn.getDistributionQuantity());
//                dbDetail.setPackingNumber(goodsIn.getPackingNumber());
//                dbDetail.setDistributionAmount(distributionAmount);
//                dbDetail.setWrhInvQty(goodsIn.getWrhInvQty());
//                ordDirOrderDistributionDetailService.updateByPrimaryKeySelective(dbDetail);
//            }
//        }
//        OrdDirOrderDistribution ordDisOrderDistribution = ordDirOrderDistributionMapper.selectByPrimaryKey(ordDistributionOrderId);
//        if (Objects.nonNull(ordDisOrderDistribution)) {
//            //查询当前商品门店明细 重新统计
//            List<OrdDirOrderDistributionDetail> dbDetails = ordDirOrderDistributionDetailService.getDetailByOrdDistributionOrderId(ordDistributionOrderId);
//            //获取总数量和总金额
//            Map<String, BigDecimal> sumMap = this.quantityAndAmountSum(dbDetails, null, ordDisOrderDistribution.getBizOrgCode());
//            //分货总数量
//            ordDisOrderDistribution.setDistributionTotalQuantity(sumMap.get(OrdDisMapKeyConstant.TOTAL_QUANTITY));
//            //分货总金额
//            ordDisOrderDistribution.setDistributionTotalAmount(sumMap.get(OrdDisMapKeyConstant.TOTAL_AMOUNT));
//            ordDisOrderDistribution.setStockCode(dataList.get(0).getStorageCode());
//            ordDisOrderDistribution.setStockName(dataList.get(0).getStorageName());
//        }
//        ordDirOrderDistributionMapper.updateByPrimaryKeySelective(ordDisOrderDistribution);
//    }
//

    /**
     * 直营分货单作废
     *
     * @param ordDirOrderDistribution 直营分货单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidDistributionOrder(OrdDirOrderDistribution ordDirOrderDistribution) {
        OrdDirOrderDistribution dbDistribution = this.getOrdDirOrderDistribution(ordDirOrderDistribution.getId());
        if (!OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(dbDistribution.getDistributionOrderStatus())
                && !OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(dbDistribution.getDistributionOrderStatus())) {
            throw new BusinessException(dbDistribution.getDistributionOrderStatus() + "状态不可作废");
        }

        ordDirOrderDistributionMapper.updateByPrimaryKeySelective(ordDirOrderDistribution);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDirOrderDistribution.getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_INVALID.getName(), new Date(), ordDirOrderDistribution.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }


    /**
     * 直营分货单导出
     *
     * @param queryOrderDistributionDetailIn 直营出货单详情入参
     * @return
     */
    @Override
    public String export(QueryOrderDistributionDetailIn queryOrderDistributionDetailIn) {
        //分页查询出货单明细
        Page<OrdDirOrderDistributionDetailOut> detailOutPage = ordDirOrderDistributionDetailService.findByPage(queryOrderDistributionDetailIn);
        //获取分页数据
        List<OrdDirOrderDistributionDetailOut> detailOuts = detailOutPage.getList();
        //导出Excel实体
        List<ExportOrdDirOrderDetail> exportDetails = new ArrayList<>();
        for (OrdDirOrderDistributionDetailOut detailOut : detailOuts) {
            //导出实体
            ExportOrdDirOrderDetail exportDetail = new ExportOrdDirOrderDetail();
            BeanUtils.copy(detailOut, exportDetail);
            //添加excel导出结果集
            exportDetails.add(exportDetail);
        }

        //导入excel标题
        String title = "直营分货单信息";
        byte[] fileBytesByData = FileExportUtil.getFileBytesByData(
                exportDetails,
                title,
                title,
                ExportOrdDirOrderDetail.class,
                true);

        return fileService.uploadFile(
                title + ".xlsx",
                fileBytesByData,
                SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }


    /**
     * 批量导入直营分货单
     *
     * @param fileId 文件id
     * @return
     */
    @Override
    public Response<List<OrdDirOrderDistributionDetailOut>> initDirOrderStoreGoodsListener(String fileId) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        OrdDirOrderStoreGoodsListener listener = new OrdDirOrderStoreGoodsListener(storeCenterService, orderGoodsServer);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdOrderStoreGoodsVO.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(0).build();
        excelReader.read(readSheet).finish();
        return listener.getResponse();
    }

    /**
     * 查询直营分货订单列表
     *
     * @param dirJoinOrderIn 作废直营分货单关联的订货单列表查询入参类
     * @return
     */
    @Override
    public Page<DirJoinOrderOut> findDirJoinOrderListByDisId(DirJoinOrderIn dirJoinOrderIn) {
        //查询分货单相关的订单
        List<DirJoinOrderOut> orderOuts = ordDirOrderDistributionMapper.findDirJoinOrderListByPage(dirJoinOrderIn);
        orderOuts.forEach(item -> {
            //状态中文转换
            item.setOrderStatusCodeStr(OrderStatusEnum.getValueByKey(item.getOrderStatusCode()));
            //门店代码
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(item.getStoreCode());
            if (Objects.nonNull(storeOut)) {
                item.setStoreName(storeOut.getStoreName());
            }
        });

        Page<DirJoinOrderOut> orderOutPage = new Page(dirJoinOrderIn);
        orderOutPage.setList(orderOuts);
        return orderOutPage;
    }


    /**
     * 查找直营分货单操作人集合
     *
     * @param bizOrgCode 业务组织
     * @return
     */
    @Override
    public List<UserNameOut> findDirOrderCreatorList(String bizOrgCode) {
        return ordDirOrderDistributionMapper.findDirOrderCreatorList(bizOrgCode);
    }


    /**
     * 查询直营分货单
     *
     * @param distributionOrderId 直营分货单id
     * @return
     */
    @Override
    public Response<OrdDirOrderDistOut> getDisOrderDist(Long distributionOrderId) {
        //查询分货单
        OrdDirOrderDistribution dbDisOrder = this.getOrdDirOrderDistribution(distributionOrderId);
        if (Objects.isNull(dbDisOrder)) {
            return Response.error("配货分货单不存在");
        }
        //查询明细
        List<OrdDirOrderDistributionDetail> details = ordDirOrderDistributionDetailService.getDetailByOrdDistributionOrderId(distributionOrderId);
        //结果集
        OrdDirOrderDistOut ordDirOrderDistOut = new OrdDirOrderDistOut();
        //明细处理
        this.addDistDetails(ordDirOrderDistOut, dbDisOrder, details);
        return Response.data(ordDirOrderDistOut);
    }


    /**
     * 直营分货单保存或修改
     *
     * @param ordDirOrderDistIn 保存直营分货单和门店商品明细入参
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDirOrderDistOut saveUpdateDirOrder(OrdDirOrderDistIn ordDirOrderDistIn) {
        //校验
        CheckDirOrderOut checkDisOrderOut = this.verifyOrder(ordDirOrderDistIn);
        //添加待审核状态
        ordDirOrderDistIn.getOrdDirOrderDistribution().setDistributionOrderStatus(OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey());
        //判断分货单id是否存在 存在则为更新，否则为新增
        boolean idIsNull = Objects.isNull(ordDirOrderDistIn.getOrdDirOrderDistribution().getId());
        OrdDirOrderDistOut ordDirOrderDistOut = this.saveUpdate(ordDirOrderDistIn, checkDisOrderOut);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDirOrderDistIn.getOrdDirOrderDistribution().getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                idIsNull ? OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_SAVE.getName() : OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_UPDATE.getName(),
                new Date(), ordDirOrderDistIn.getOrdDirOrderDistribution().getUpdater());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return ordDirOrderDistOut;
    }

//    /**
//     * 直营分货单审核
//     *
//     * @param ordDirOrderDistIn 直营分货单入参
//     * @return
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public OrdDirOrderDistOut audit(OrdDirOrderDistIn ordDirOrderDistIn) {
//        //校验
//        CheckDirOrderOut checkDirOrderOut = this.verifyOrder(ordDirOrderDistIn);
//        //保存
//        OrdDirOrderDistOut ordDirOrderDistOut = this.saveUpdate(ordDirOrderDistIn, checkDirOrderOut);
//        //获取直营分货单
//        OrdDirOrderDistribution ordDirOrderDistribution = ordDirOrderDistIn.getOrdDirOrderDistribution();
//
//        //立即生效则创建订货单任务
//        if (NumberUtil.INTEGER_ONE.equals(ordDirOrderDistribution.getIsEffectiveImmediately())) {
//            String loginUsername = UserUtil.getNickname() + "【" + UserUtil.getJobNumber() + "】";
//            this.dirDistributionInitOrder(ordDirOrderDistribution.getId(), ordDirOrderDistribution.getEffectiveTime(), ordDirOrderDistribution.getBizOrgCode(), loginUsername);
//        }
//        return ordDirOrderDistOut;
//    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public String handlePurchaseListForDistributionOrderOld(Long distributionOrderId, LocalDateTime effectiveTime, String loginUsername, String bizOrgCode) {
//        //逗号拼接 门店信息和门店code
//        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
//        AtomicInteger failedStoreNumber = new AtomicInteger();
//        OrdDirOrderDistribution ordDirOrderDistribution = this.getOrdDirOrderDistribution(distributionOrderId);
//        if (Objects.isNull(ordDirOrderDistribution)) {
//            log.error("直营分货单不存在-----------------------------------");
//            return null;
//        }
//        if (!OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(ordDirOrderDistribution.getDistributionOrderStatus())) {
//            log.error("直营分货单{}状态不正确", ordDirOrderDistribution.getDistributionOrderNo());
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
//                log.error("分货单{}门店{}分货检查可订货商品异常。", ordDirOrderDistribution.getDistributionOrderNo(), storeCode);
//                errorJoiner.add(storeCode);
//                failedStoreNumber.getAndIncrement();
//                continue;
//            }
//            try {
//                this.createOrder(storeCode, ordDirOrderDistribution, effectiveTime, orderCartOuts, loginUsername, bizOrgCode);
//            } catch (Exception e) {
//                log.error("门店" + storeCode + "分货异常,分货单号：" + ordDirOrderDistribution.getDistributionOrderNo(), e);
//                errorJoiner.add(storeCode);
//                failedStoreNumber.getAndIncrement();
//            }
//        }
//        // 更新分货单状态
//        OrdExamineDistributionOrderIn orderIn = new OrdExamineDistributionOrderIn();
//        orderIn.setEffectiveTime(effectiveTime);
//        orderIn.setDistributionOrderId(ordDirOrderDistribution.getId());
//        this.submitDistributionOrder(orderIn, NumberUtil.INTEGER_ONE);
//        this.updateDirDistributionOrderExecuted(ordDirOrderDistribution.getId(), loginUsername);
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
//                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
//                String.valueOf(distributionOrderId),
//                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
//                content, new Date(), loginUsername);
//
//        asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        return errorJoiner.toString();
//    }

    @Override
    public Response<String> handleAudit(OrdDirDistributionAuditIn ordDirDistributionAuditIn, String loginUsername) {
        OrdDirOrderDistribution distributionOrder = ordDirOrderDistributionMapper.selectByPrimaryKey(ordDirDistributionAuditIn.getDistributionOrderId());
        if (Objects.isNull(distributionOrder)) {
            return Response.error("直营分货单不存在");
        }
        String key = SystemConstant.ORD_DIR_DISTRIBUTION_ORDER_AUDIT + SystemConstant.COLON + distributionOrder.getBizOrgCode()
                + SystemConstant.COLON + distributionOrder.getDistributionOrderNo();
        if (!redisService.setIfAbsent(key, distributionOrder.getDistributionOrderNo(), 20l, TimeUnit.MINUTES)) {
            return Response.error("审核中，请勿重复审核");
        }
        if (OrderDistributionOrderStatusEnum.INVALID.getKey().equals(distributionOrder.getDistributionOrderStatus())) {
            return Response.error("分货单已作废");
        }
        if (!OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(distributionOrder.getDistributionOrderStatus())) {
            return Response.error("直营分货单状态不正确");
        }
        int detailCount = ordDirOrderDistributionDetailService.countByDistributionOrderId(ordDirDistributionAuditIn.getDistributionOrderId());
        if (detailCount == NumberUtil.INTEGER_ZERO) {
            return Response.error("直营分货单明细为空");
        }
        dirDistributionOrderAuditHandle.asyncCheckByAudit(ordDirDistributionAuditIn, loginUsername, key);
        return Response.success("审核中，请稍后查看");
    }

    @Override
    public String handleDistributionCreateOrder(Long distributionOrderId, String loginUsername) {
        OrdDirOrderDistribution ordDirOrderDistribution = ordDirOrderDistributionMapper.selectByPrimaryKey(distributionOrderId);
        String key = DirSystemConstant.REDIS_DIR_HANDLE_DISTRIBUTION_CREATE_ORDER + ordDirOrderDistribution.getBizOrgCode() + SystemConstant.COLON + ordDirOrderDistribution.getDistributionOrderNo();
        if (!redisService.setIfAbsent(key, ordDirOrderDistribution.getDistributionOrderNo(), 2L, TimeUnit.MINUTES)) {
            return "重复消费";
        }
        String beforeStatusCode = ordDirOrderDistribution.getDistributionOrderStatus();
        if (!OrderDistributionOrderStatusEnum.APPROVAL.getKey().equals(beforeStatusCode)) {
            return "分货单状态不正确";
        }
        Map<String, List<CreateOrderSkuIn>> storeOrderCartMap = this.initStoreOrderCartMap(ordDirOrderDistribution.getId());
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        AtomicInteger failedStoreNumber = new AtomicInteger();
        storeOrderCartMap.entrySet().forEach(entry -> {
            String storeCode = entry.getKey();
            if (NumberUtil.INTEGER_ZERO.equals(ordDirOrderDistribution.getIsEffectiveImmediately())) {
                //校验门店与业务状态开关
                StoreAndStatusSwitchOut storeAndStatusSwitch = storeCenterService.getStoreAndStatusSwitch(storeCode, ordDirOrderDistribution.getBizOrgCode());
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
                if (ModelConst.DELETE.NO.equals(storeStatusBusinessSwitch.getIsAllotSc())) {
                    log.error("{}不允许分货", storeCode);
                    errorJoiner.add(storeCode);
                    return;
                }
            }
            List<CreateOrderSkuIn> createOrderSkuInList = entry.getValue();
            //生成订单
            try {
                orderHandle.createDistributionOrder(storeCode, loginUsername, ordDirOrderDistribution.getId(),
                        createOrderSkuInList, ordDirOrderDistribution.getBizOrgCode());
            } catch (Exception e) {
                log.error("直营分货单{}创建门店{}订货单异常", ordDirOrderDistribution.getDistributionOrderNo(), storeCode, e);
                errorJoiner.add(storeCode);
                failedStoreNumber.getAndIncrement();
                // 记录异常
                OrdDirOrderDistributionResult ordDirOrderDistributionResult = new OrdDirOrderDistributionResult();
                ordDirOrderDistributionResult.setDistributionOrderId(ordDirOrderDistribution.getId());
                ordDirOrderDistributionResult.setStoreCode(storeCode);
                ordDirOrderDistributionResult.setIsDone(2);
                ordDirOrderDistributionResult.setUpdater(loginUsername);
                ordDirOrderDistributionResult.setUpdateTime(LocalDateTime.now());
                ordDirOrderDistributionResult.setRemark(e.getMessage());
                ordDirOrderDistributionResultService.updateByDistributionOrderIdAndStoreCode(ordDirOrderDistributionResult);
            }
        });
        ordDirOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.EXECUTED.getKey());
        ordDirOrderDistribution.setIsEnd(NumberUtil.INTEGER_ONE);
        ordDirOrderDistribution.setUpdater(loginUsername);
        ordDirOrderDistribution.setUpdateTime(LocalDateTime.now());
        ordDirOrderDistributionMapper.updateByPrimaryKeySelective(ordDirOrderDistribution);
        int successStoreNumber = storeOrderCartMap.size() - failedStoreNumber.get();
        String content;
        //门店全部成功生成订货单
        if (NumberUtil.INTEGER_ZERO.equals(failedStoreNumber.get())) {
            content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_CREATE_ORDER_ORDER_SUCCESS.getKey(),
                    OrderDistributionOrderStatusEnum.getValueByKey(beforeStatusCode), OrderDistributionOrderStatusEnum.getValueByKey(ordDirOrderDistribution.getDistributionOrderStatus()),
                    successStoreNumber, failedStoreNumber.get());
        } else {
            content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_CREATE_ORDER_ERROR.getKey(),
                    OrderDistributionOrderStatusEnum.getValueByKey(beforeStatusCode), OrderDistributionOrderStatusEnum.getValueByKey(ordDirOrderDistribution.getDistributionOrderStatus()),
                    successStoreNumber, failedStoreNumber.get(), errorJoiner.toString());
        }
        // 添加生成订订货单日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                String.valueOf(ordDirOrderDistribution.getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                content, new Date(), loginUsername);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return errorJoiner.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDirDistributionOrder(OrdDirOrderDistribution ordDirOrderDistribution) {
        String distributionOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KFH.getCode(), ordDirOrderDistribution.getBizOrgCode(), uniqueUtils, 4);
        ordDirOrderDistribution.setDistributionOrderNo(distributionOrderNo);
        ordDirOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey());
        ordDirOrderDistribution.setIsEnd(ModelConst.ENABLE.NO);
        ordDirOrderDistribution.setIsDelete(ModelConst.DELETE.NO);
        ordDirOrderDistributionMapper.insert(ordDirOrderDistribution);
        return ordDirOrderDistribution.getId();
    }

    @Override
    public Response<Long> asyncImportDistributionDetail(String fileId, OrdDirOrderDistribution
            ordDirOrderDistribution, String loginUsername) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DirDistributionAsyncListener listener = new DirDistributionAsyncListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportOrdOrderStoreGoodsVO.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(0).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > 0) {
            return Response.data(ordDirOrderDistribution.getId(), listener.getImportErrorMessage(totalErrorMap));
        }
        // 异步导入入库
        dirDistributionOrderImportHandle.handleAsyncDirDistribution(listener.getImportResultMap(), ordDirOrderDistribution.getId(), loginUsername, "");
        return Response.data(ordDirOrderDistribution.getId(), "文件导入中，稍后刷新查看！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDirOrderDistribution getDirOrderDistributionForImport(Long distributionOrderId, String
            loginUsername, Integer isEffectiveImmediately, LocalDateTime effectiveTime) {
        OrdDirOrderDistribution ordDirOrderDistribution = new OrdDirOrderDistribution();
        ordDirOrderDistribution.setUpdater(loginUsername);
        ordDirOrderDistribution.setEffectiveTime(effectiveTime);
        ordDirOrderDistribution.setIsEffectiveImmediately(isEffectiveImmediately);
        if (Objects.isNull(distributionOrderId)) {
            ordDirOrderDistribution.setBizOrgCode(UserUtil.getBizOrgCode());
            ordDirOrderDistribution.setCreator(loginUsername);
            String distributionOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KFH.getCode(), ordDirOrderDistribution.getBizOrgCode(), uniqueUtils, 4);
            ordDirOrderDistribution.setDistributionOrderNo(distributionOrderNo);
            ordDirOrderDistribution.setDistributionOrderStatus(OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey());
            ordDirOrderDistribution.setIsEnd(ModelConst.ENABLE.NO);
            ordDirOrderDistribution.setIsDelete(ModelConst.DELETE.NO);
            ordDirOrderDistributionMapper.insert(ordDirOrderDistribution);
            // 添加日志
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                    String.valueOf(ordDirOrderDistribution.getId()),
                    OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                    OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_SAVE.getName(),
                    new Date(), ordDirOrderDistribution.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        } else {
            ordDirOrderDistribution = ordDirOrderDistributionMapper.selectByPrimaryKey(distributionOrderId);
            ordDirOrderDistributionMapper.updateByPrimaryKeySelective(ordDirOrderDistribution);
            // 添加日志
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                    String.valueOf(ordDirOrderDistribution.getId()),
                    OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                    OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_UPDATE.getName(),
                    new Date(), ordDirOrderDistribution.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return ordDirOrderDistribution;
    }

    @Override
    public List<Long> findNeedExecuteList() {
        return ordDirOrderDistributionMapper.findNeedExecuteList(OrderDistributionOrderStatusEnum.APPROVAL.getKey(), DateUtils.format(LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> updateHead(UpdateDistributionEffectiveTimeIn updateDistributionEffectiveTimeIn) {
        OrdDirOrderDistribution distributionOrder = ordDirOrderDistributionMapper.selectByPrimaryKey(updateDistributionEffectiveTimeIn.getDistributionOrderId());
        if (Objects.isNull(distributionOrder)) {
            return Response.error("直营分货单不存在");
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
        ordDirOrderDistributionMapper.updateByPrimaryKey(distributionOrder);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getName(),
                String.valueOf(distributionOrder.getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION.getCode(),
                OrdLogTypeEnum.ORD_DIR_ORDER_DISTRIBUTION_UPDATE.getName(), new Date(), distributionOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success();
    }

    private Map<String, List<CreateOrderSkuIn>> initStoreOrderCartMap(Long distributionOrderId) {
        // 按门店提交分货单下商品到订货清单与订单中
        List<OrdDirOrderDistributionDetail> distributionStoreList = ordDirOrderDistributionDetailService.getDetailByOrdDistributionOrderId(distributionOrderId);
        Map<String, List<CreateOrderSkuIn>> storeOrderCartMap = new LinkedHashMap<>();
        Map<String, List<OrdDirOrderDistributionDetail>> storeGoodsMap = distributionStoreList.stream()
                .collect(Collectors.groupingBy(OrdDirOrderDistributionDetail::getStoreCode));
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
        return ordDirOrderDistributionMapper.getDistributionOrderNoByOrderId(orderId, bizOrgCode);
    }

    /**
     * 保存或更新
     *
     * @param ordDirOrderDistIn 直营分货单保存更新入参
     * @param checkDisOrderOut  校验通过返回结果
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public OrdDirOrderDistOut saveUpdate(OrdDirOrderDistIn ordDirOrderDistIn,
                                         CheckDirOrderOut checkDisOrderOut) {
        //获取分货单信息
        OrdDirOrderDistribution ordDirOrder = ordDirOrderDistIn.getOrdDirOrderDistribution();
        //获取明细信息
        List<OrdDirOrderDistributionDetail> details = ordDirOrderDistIn.getOrdDirOrderDistributionDetails();
        //获取总数量和总金额
        Map<String, BigDecimal> sumMap = this.quantityAndAmountSum(details, checkDisOrderOut.getGoodsOut(), ordDirOrder.getBizOrgCode());
        //配货分货总金额 初始为
        ordDirOrder.setDistributionTotalAmount(sumMap.get(OrdDisMapKeyConstant.TOTAL_AMOUNT));
        //配货分货总数量
        ordDirOrder.setDistributionTotalQuantity(sumMap.get(OrdDisMapKeyConstant.TOTAL_QUANTITY));
//        //仓位代码
//        ordDisOrder.setStockCode(checkDisOrderOut.getGoodsOut().getStockCode());
//        //仓位名称
//        ordDisOrder.setStockName(checkDisOrderOut.getGoodsOut().getStockName());
        if (Objects.isNull(ordDirOrder.getId())) {
            /** 新增 */
            //生成分货单单号
            ordDirOrder.setDistributionOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KFH.getCode(), ordDirOrder.getBizOrgCode(), uniqueUtils, 4));
            //已完结 初始为0
            ordDirOrder.setIsEnd(NumberUtil.INTEGER_ZERO);
            ordDirOrderDistributionMapper.insertSelective(ordDirOrder);
        } else {
            /** 更新 */
            OrdDirOrderDistribution dbOrder = this.getOrdDirOrderDistribution(ordDirOrder.getId());
            if (Objects.isNull(dbOrder)) {
                throw new BusinessException("直营分货单不存在");
            }
            if (!OrderDistributionOrderStatusEnum.PENDING_APPROVAL.getKey().equals(dbOrder.getDistributionOrderStatus())) {
                throw new BusinessException("不可修改：直营出货单状态为=>" + OrderDistributionOrderStatusEnum.getValueByKey(dbOrder.getDistributionOrderStatus()));
            }
            ordDirOrderDistributionMapper.updateByPrimaryKeySelective(ordDirOrder);
        }
        //结果集
        OrdDirOrderDistOut ordDirOrderDistOut = new OrdDirOrderDistOut();
        //copy出货单
        BeanUtils.copy(ordDirOrder, ordDirOrderDistOut);

        /** 保存明细 */
        ordDirOrderDistributionDetailService.save(ordDirOrder, details, NumberUtil.INTEGER_ZERO);
//        //添加明细并中文转换
//        this.addDistDetails(ordDirOrderDistOut, ordDisOrder, details);
        //添加出货单id
        ordDirOrderDistOut.setDistributionOrderId(ordDirOrder.getId());
        return ordDirOrderDistOut;
    }


    /**
     * 统计分货总数量和总金额
     *
     * @param details    门店/商品集合
     * @param goodsOut   商品信息
     * @param bizOrgCode 业务组织
     * @return
     */
    public Map<String, BigDecimal> quantityAndAmountSum
    (List<OrdDirOrderDistributionDetail> details, OrderGoodsOut goodsOut, String bizOrgCode) {
        Map<String, BigDecimal> sumMap = new HashMap<>();
        //分货总数量
        BigDecimal distributionTotalQuantity = details.stream().map(OrdDirOrderDistributionDetail::getDistributionQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        //分货总金额 商品信息为null  明细金额直接求和  不为null 则需要用商品信息的单价相乘再求和
        BigDecimal distributionTotalAmount = BigDecimal.ZERO;
//        if (Objects.isNull(goodsOut)) {
//            //明细金额直接求和
//            distributionTotalAmount = details.stream().map(OrdDirOrderDistributionDetail::getDistributionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//        } else {
        //商品信息的单价相乘再求和
        distributionTotalAmount = details.stream().map(
                x -> x.getDistributionQuantity().multiply(
                        (Objects.isNull(x.getOriginalPrice()) ? BigDecimal.ZERO : x.getOriginalPrice()))
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
     * @param ordDirOrderDistIn 配货分货单和门店商品保存明细入参
     */
    public CheckDirOrderOut verifyOrder(OrdDirOrderDistIn ordDirOrderDistIn) {
        CheckDirOrderOut checkDirOrderOut = new CheckDirOrderOut();
        //获取分货单信息
//        OrdDirOrderDistribution ordDisOrder = ordDirOrderDistIn.getOrdDirOrderDistribution();
        //获取明细信息
        List<OrdDirOrderDistributionDetail> details = ordDirOrderDistIn.getOrdDirOrderDistributionDetails();
        //当前列表商品去重
        Map<String, String> checkMap = new HashMap<>(2);
        for (OrdDirOrderDistributionDetail detail : details) {
            //拼接门店code和商品code
            String joinCode = detail.getGoodsCode() + "&" + detail.getStoreCode();
            //校验门店code和商品code组合是否重复
            if (checkMap.containsKey(joinCode)) {
                throw new BusinessException("商品:" + detail.getGoodsCode() + "和门店：" + detail.getStoreCode() + "组合重复");
            }
//            checkDirOrderOut = this.checkDirOrder(detail.getGoodsCode(), detail.getStoreCode(), ordDisOrder.getBizOrgCode());
            checkMap.put(joinCode, joinCode);
        }
        return checkDirOrderOut;
    }


    /**
     * 添加分货门店/商品明细并且中文转换
     *
     * @param ordDirOrderDistOut 结果集
     * @param ordDirOrder        直营分货单
     * @param details            明细
     */
    private void addDistDetails(OrdDirOrderDistOut ordDirOrderDistOut,
                                OrdDirOrderDistribution ordDirOrder,
                                List<OrdDirOrderDistributionDetail> details) {
        //copy同属性
        BeanUtils.copy(ordDirOrder, ordDirOrderDistOut);
        //添加出货单id
        ordDirOrderDistOut.setDistributionOrderId(ordDirOrder.getId());
        //提交人
        ordDirOrderDistOut.setCreatorId(ordDirOrder.getCreator());
        //分货总数量
        ordDirOrderDistOut.setTotalDistributionQuantity(ordDirOrder.getDistributionTotalQuantity());
        //状态中文转换
        ordDirOrderDistOut.setDistributionOrderStatusValue(OrderDistributionOrderStatusEnum.getValueByKey(ordDirOrder.getDistributionOrderStatus()));
        //处理明细
        List<OrdDirOrderDistributionDetailOut> detailOuts = details.stream().map(detail -> {
            //明细结果集
            OrdDirOrderDistributionDetailOut ordDirOrderDistributionDetailOut = new OrdDirOrderDistributionDetailOut();
            //copy同属性
            BeanUtils.copy(detail, ordDirOrderDistributionDetailOut);
            return ordDirOrderDistributionDetailOut;

        }).collect(Collectors.toList());

        //添加明细
        ordDirOrderDistOut.setOrderDistributionDetailOuts(detailOuts);
    }


    /**
     * 校验门店/商品信息
     *
     * @param goodsCode  商品代码
     * @param storeCode  门店代码
     * @param bizOrgCode 业务组织
     */
    public CheckDirOrderOut checkDirOrder(String goodsCode, String storeCode, String bizOrgCode) {
        if (StringUtils.isBlank(storeCode)) {
            throw new BusinessException("门店信息不能为空");
        }
        if (StringUtils.isBlank(goodsCode)) {
            throw new BusinessException("商品信息不能为空");
        }
        //校验通过保存门店商品信息
        CheckDirOrderOut checkDirOrderOut = new CheckDirOrderOut();
        //查询门店是否存在且符合规则
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(storeCode);
        if (Objects.isNull(storeOut)) {
            throw new BusinessException("门店" + storeCode + "不存在");
        }

        if (!StoreConstant.StoreProperty.DIRECTLY.getMytValue().equals(storeOut.getStoreType())) {
            throw new BusinessException(storeCode + "门店是加盟门店，不可在直营订货管理分货！");
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

//        //门店商品上下架
//        if (NumberUtil.INTEGER_ONE.equals(goodsOut.getIsShelves())) {
//            throw new BusinessException(storeCode + "门店下的" + goodsCode + "商品已下架");
//        }

        //获取商品开关信息
        GoodsStatusBusinessSwitch goodsStatusBusinessSwitch = goodsOut.getGoodsStatusBusinessSwitch();
        if (Objects.isNull(goodsStatusBusinessSwitch)) {
            throw new BusinessException("商品" + goodsCode + "不可被分货");
        }

        if (!NumberUtil.INTEGER_ONE.equals(goodsStatusBusinessSwitch.getIsAllot())) {
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
        checkDirOrderOut.setStoreOut(storeOut);
        checkDirOrderOut.setGoodsOut(goodsOut);

        return checkDirOrderOut;
    }
}
