package com.edc.erp.directly.dirfirstorder.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.goods.OrdQueryGoodsIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.GoodsForOrdOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.OrgSortOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.model.in.OrdDirDeliveryIn;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDelivery;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDetail;
import com.edc.erp.directly.dirfirstorder.handle.DirFirstOrderAsyncImportHandle;
import com.edc.erp.directly.dirfirstorder.handle.DirFirstOrderAuditHandle;
import com.edc.erp.directly.dirfirstorder.listener.DirFirstDirOrderAsyncListener;
import com.edc.erp.directly.dirfirstorder.mapper.OrdDirOrderFirstMapper;
import com.edc.erp.directly.dirfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.erp.directly.dirfirstorder.model.in.InsertFirstOrderDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirFirstOrderAuditIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstDetailIn;
import com.edc.erp.directly.dirfirstorder.model.in.OrdDirOrderFirstIn;
import com.edc.erp.directly.dirfirstorder.model.out.FirstOrderConfigOut;
import com.edc.erp.directly.dirfirstorder.model.out.FirstOrderSortOut;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDerDirOrderFirstOut;
import com.edc.erp.directly.dirfirstorder.model.out.OrdDirOrderFirstDetailOut;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstDeliveryService;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstDetailService;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 铺货单(OrdDirOrderFirst)表服务实现类
 *
 * @author weichao
 * @since 2022-11-10 14:09:10
 */
@Slf4j
@Service
public class OrdDirOrderFirstServiceImpl extends BaseServiceImpl<OrdDirOrderFirst> implements OrdDirOrderFirstService {
    @Autowired
    private OrdDirOrderFirstMapper ordDirOrderFirstMapper;
    @Autowired
    private OrdDirOrderFirstDeliveryService ordDirOrderFirstDeliveryService;
    @Autowired
    private OrdDirDeliveryService ordDirDeliveryService;
    @Autowired
    private UniqueUtils uniqueUtils;
    @Autowired
    private AsyncPushTaskService asyncPushTaskService;
    @Autowired
    private OrdDirOrderFirstDetailService ordDirOrderFirstDetailService;
    @Autowired
    private OrderGoodsServer orderGoodsServer;
    @Autowired
    private StoreCenterService storeCenterService;

    private static final Integer MAX_NUMBER = 500;
    @Autowired
    private WarehouseServer warehouseServer;
    @Autowired
    private StockServer stockServer;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private DirFirstOrderAsyncImportHandle dirFirstOrderAsyncImportHandle;

    @Autowired
    private FileService fileService;

    @Autowired
    private StoreChannelHandle storeChannelHandle;

    @Autowired
    private RedisService redisService;

    @Autowired
    private DirFirstOrderAuditHandle dirFirstOrderAuditHandle;

    @Override
    public Page<OrdDerDirOrderFirstOut> findFirstOrderPage(OrdDirOrderFirstIn ordDirOrderFirstIn) {

        List<OrdDerDirOrderFirstOut> ordDerDisOrderFirstOuts = ordDirOrderFirstMapper.findFirstOrderByPage(ordDirOrderFirstIn);
        ordDerDisOrderFirstOuts.forEach(item -> {
            item.setFirstOrderStatusStr(FirstOrderStatusEnum.getNameByCode(item.getFirstOrderStatus()));
            item.setTotalAmount(item.getTotalAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        });

        Page<OrdDerDirOrderFirstOut> outPage = new Page<>(ordDirOrderFirstIn);
        outPage.setList(ordDerDisOrderFirstOuts);
        return outPage;
    }

    @Override
    public OrdDerDirOrderFirstOut getFirstOrderOut(Long ordDisOrderFirstId) {
        OrdDerDirOrderFirstOut orderDirOrderFirstOut = new OrdDerDirOrderFirstOut();
        OrdDirOrderFirst orderFirst = ordDirOrderFirstMapper.selectByPrimaryKey(ordDisOrderFirstId);
        if (Objects.isNull(orderFirst)) {
            throw new BusinessException("此铺货单不存在");
        }
        BeanUtils.copy(orderFirst, orderDirOrderFirstOut);
        orderDirOrderFirstOut.setFirstOrderStatusStr(FirstOrderStatusEnum.getNameByCode(orderDirOrderFirstOut.getFirstOrderStatus()));
        // 配货单关联
        OrdDirOrderFirstDelivery orderFirstDelivery = new OrdDirOrderFirstDelivery();
        orderFirstDelivery.setFirstOrderId(orderFirst.getId());
        List<OrdDirOrderFirstDelivery> orderFirstDeliveries = ordDirOrderFirstDeliveryService.list(orderFirstDelivery);
        List<OrdDirDelivery> ordDisDeliveries = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orderFirstDeliveries)) {

            for (OrdDirOrderFirstDelivery ordDirOrderFirstDelivery : orderFirstDeliveries) {
                // 配货单
                OrdDirDelivery ordDirDelivery = ordDirDeliveryService.selectByPrimaryKey(ordDirOrderFirstDelivery.getDeliveryOrderId());
                if (ordDirDelivery == null) {
                    continue;
                }
                ordDisDeliveries.add(ordDirDelivery);
            }
        }
        orderDirOrderFirstOut.setDeliveryOrders(ordDisDeliveries);
        return orderDirOrderFirstOut;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertFirstOrder(OrdDirOrderFirst firstOrder) {
        firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.PENDING.getCode());
        String orgOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.SP.getCode(), firstOrder.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR);
        firstOrder.setFirstOrderNo(orgOrderNo);
        firstOrder.setIsDelete(0);
        insertSelective(firstOrder);
        //  保存日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
                String.valueOf(firstOrder.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(), OrdLogTypeEnum.FIRST_ORDER_GOODS_SAVE.getName(), new Date(), firstOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return firstOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidFirstOrder(OrdDirOrderFirst firstOrder) {
        if (FirstOrderStatusEnum.EXECUTED.getCode().equals(firstOrder.getFirstOrderStatus())) {
            throw new BusinessException("已生效的铺货单不可作废");
        }
        if (FirstOrderStatusEnum.INVALID.getCode().equals(firstOrder.getFirstOrderStatus())) {
            throw new BusinessException("已作废的铺货单不可作废");
        }
        firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.INVALID.getCode());
        int count = ordDirOrderFirstMapper.updateByPrimaryKeySelective(firstOrder);
        // 作废日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
                String.valueOf(firstOrder.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(), OrdLogTypeEnum.FIRST_ORDER_GOODS_INVALID.getName(), new Date(), firstOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }

//    @Override
//    public Response<OrdDirOrderFirst> auditFirstOrder(InsertFirstOrderDetailIn insertFirstOrderDetailIn) {
//        Response<OrdDirOrderFirst> response = this.saveFirstOrderDetail(insertFirstOrderDetailIn);
//        if (!response.isSuccess()) {
//            return response;
//        }
//        return this.audit(response);
//    }

//    @Transactional(rollbackFor = Exception.class)
//    public Response<OrdDirOrderFirst> audit(Response<OrdDirOrderFirst> response) {
//        OrdDirOrderFirst ordFirstOrder = response.getData();
//        OrdDirOrderFirst firstOrder = this.selectByPrimaryKey(ordFirstOrder.getId());
//        // 更改审核状态
//        if (NumberUtil.INTEGER_ONE.equals(firstOrder.getIsEffectiveImmediately())) {
//            // 若没有设置生效时间则默认为当前时间
//            firstOrder.setEffectiveTime(LocalDateTime.now());
//            firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.EXECUTED.getCode());
//
//        } else {
//            firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.APPROVED.getCode());
//
//        }
//        firstOrder.setApprovalTime(LocalDateTime.now());
//        firstOrder.setApprover(UserUtil.getUserName());
//        int update = ordDirOrderFirstMapper.updateByPrimaryKeySelective(firstOrder);
//        if (update == 0) {
//            return Response.error("铺货单审核失败");
//        }
//        // 如果为生效状态，则拆分为配货单
//        if (FirstOrderStatusEnum.EXECUTED.getCode().equals(firstOrder.getFirstOrderStatus())) {
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_FIRST_TO_DELIVERY, JSONObject.toJSONString(firstOrder), firstOrder.getBizOrgCode());
//            //生效日志
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
//                    String.valueOf(firstOrder.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(), OrdLogTypeEnum.FIRST_ORDER_GOODS_EXECUTED.getName(), new Date(), firstOrder.getCreator());
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        }
//        //审核日志
//        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
//                String.valueOf(firstOrder.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(), OrdLogTypeEnum.FIRST_ORDER_GOODS_AUDIT.getName(), new Date(), firstOrder.getCreator());
//        asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        return Response.success();
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<List<OrdDirDeliveryIn>> spiltFirstOrderByConfig(OrdDirOrderFirst firstOrder) {
        String bizOrgCode = firstOrder.getBizOrgCode();
        //获取铺货单拆单配置
        FirstOrderConfigOut firstOrderConfigOut = this.getFirstOrderConfigOut(bizOrgCode);
        // 商品明细
        OrdDirOrderFirstDetailIn query = new OrdDirOrderFirstDetailIn();
        query.setFirstOrderId(firstOrder.getId());
        Page<OrdDirOrderFirstDetailOut> ordDirOrderFirstDetailPage = this.findOrdDirOrderFirstDetailPage(query, bizOrgCode);
        List<OrdDirOrderFirstDetailOut> orderDetailList = ordDirOrderFirstDetailPage.getList();

        if (CollectionUtils.isEmpty(orderDetailList)) {
            throw new BusinessException("没有需要拆单的明细");
        }
        Map<String, OrdDirOrderFirstDetailOut> skuOrderDetailMap = orderDetailList.stream().collect(Collectors.toMap(OrdDirOrderFirstDetailOut::getGoodsCode, Function.identity()));
        List<String> skuCodes = orderDetailList.stream().map(OrdDirOrderFirstDetailOut::getGoodsCode).collect(Collectors.toList());
        // 筛出符合配货商品范围的商品集合
        List<String> legalOrderCodeList = checkGoods(firstOrder, bizOrgCode, skuCodes);
        if (CollectionUtils.isEmpty(legalOrderCodeList)) {
            log.info("直营配销铺货单{}没有需要拆单的符合商品范围的明细", firstOrder.getFirstOrderNo());
            String content = MessageFormat.format(OrdLogTypeEnum.DIR_FIRST_ORDER_EMPTY.getName(), firstOrder.getFirstOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getName(),
                    String.valueOf(firstOrder.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(), content, new Date(), firstOrder.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.data(null, "没有可拆分的商品");
        }
        // 合规的商品集合
        List<OrdDirOrderFirstDetailOut> legalOrderDetails = new ArrayList<>();
        for (String legalGoodsCode : legalOrderCodeList) {
            if (skuOrderDetailMap.containsKey(legalGoodsCode)) {
                legalOrderDetails.add(skuOrderDetailMap.get(legalGoodsCode));
            }
        }
        // 拆分的明细集合
        Map<String, List<OrdDirOrderFirstDetailOut>> firstOrderDetailMap = new TreeMap<>();
        for (OrdDirOrderFirstDetailOut orderDetailOut : legalOrderDetails) {
            if (Objects.isNull(orderDetailOut.getDistributionPrice())) {
                log.error(orderDetailOut.getGoodsCode() + "此商品无配货价，该条明细作废");
                continue;
            }
            StringJoiner keys = new StringJoiner("-");
            // 增加仓位
            if (NumberUtil.INTEGER_ONE.equals(firstOrderConfigOut.getIsStock())) {
                keys.add(orderDetailOut.getStockCode());
            }
            // 增加配送方式
            if (NumberUtil.INTEGER_ONE.equals(firstOrderConfigOut.getIsDistributionMode())) {
                keys.add(orderDetailOut.getDistributionType());
            }
            // 增加分类
            if (NumberUtil.INTEGER_ONE.equals(firstOrderConfigOut.getIsSort())) {
                splitSorts(firstOrderConfigOut, orderDetailOut, keys);
            }
            String key = keys.toString();
            List<OrdDirOrderFirstDetailOut> firstOrderDetailOuts = firstOrderDetailMap.get(key);
            if (CollectionUtils.isEmpty(firstOrderDetailOuts)) {
                firstOrderDetailOuts = Lists.newArrayList();
            }
            firstOrderDetailOuts.add(orderDetailOut);
            // 拆分好的明细集合
            firstOrderDetailMap.put(key, firstOrderDetailOuts);
        }
        List<OrdDirDeliveryIn> deliveryOrderList = Lists.newArrayList();
        firstOrderDetailMap.forEach((key, value) -> {
            // 初始化配货单
            OrdDirDeliveryIn deliveryOrder = this.initOrdDirDeliveryIn(firstOrder, bizOrgCode, key);
            //初始化配货单明细
            List<OrdDirDeliveryDetail> deliveryOrderDetailsList = Lists.newArrayList();
            value.forEach(firstOrderDetailOut -> {
                this.initOrdDirDeliveryDetail(bizOrgCode, deliveryOrder, deliveryOrderDetailsList, firstOrderDetailOut);
                deliveryOrder.setDetailList(deliveryOrderDetailsList);
            });
            deliveryOrderList.add(deliveryOrder);
        });
        log.info("铺货单{}拆单配销单集合数量是--{}", firstOrder.getFirstOrderNo(), deliveryOrderList.size());
        List<Long> ordDirDeliveryIds = new ArrayList<>();
        for (OrdDirDeliveryIn ordDirDelivery : deliveryOrderList) {
            ordDirDelivery.setSourceCode(DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType());
            ordDirDelivery.setAuditType(SalvageAuditTypeEnum.NOW_AUTO_AUDIT.getCode());
            Long orderId = ordDirDeliveryService.saveOrUpdate(ordDirDelivery);
            ordDirDelivery.setId(orderId);
            ordDirDeliveryIds.add(orderId);
        }
        ordDirOrderFirstDeliveryService.batchSave(ordDirDeliveryIds, firstOrder.getId(), firstOrder.getUpdater());
        firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.EXECUTED.getCode());
        firstOrder.setUpdateTime(LocalDateTime.now());
        ordDirOrderFirstMapper.updateByPrimaryKeySelective(firstOrder);
        List<BusinessLog> businessLogList = Lists.newArrayList();
        //生效日志
        BusinessLog executeBusinessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), OrdLogTypeEnum.FIRST_ORDER_GOODS_EXECUTED.getName(), new Date(),
                firstOrder.getUpdater());
        businessLogList.add(executeBusinessLog);
        Map<String, OrdDirOrderFirstDetailOut> legalSkuOrderDetailMap = legalOrderDetails.stream().collect(Collectors.toMap(OrdDirOrderFirstDetailOut::getGoodsCode, Function.identity()));
        // 不合格的商品
        String illegalGoodsStr = orderDetailList.stream().filter(detail -> !legalSkuOrderDetailMap.containsKey(detail.getGoodsCode()))
                .map(OrdDirOrderFirstDetail::getGoodsCode).collect(Collectors.joining(SystemConstant.COMMA));
        if (StringUtils.isNotBlank(illegalGoodsStr)) {
            String illegalGoodsStrContent = MessageFormat.format(OrdLogTypeEnum.DIR_FIRST_ORDER_ILLEGAL_GOODS.getName(), firstOrder.getFirstOrderNo(), illegalGoodsStr);
            BusinessLog illegalGoodsBusinessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getName(),
                    String.valueOf(firstOrder.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(), illegalGoodsStrContent, new Date(), firstOrder.getCreator());
            businessLogList.add(illegalGoodsBusinessLog);
        }
        businessLogList.forEach(businessLog -> asyncLogService.sendAsyncSaveLogByMq(businessLog));
        return Response.data(deliveryOrderList);
    }

    private List<String> checkGoods(OrdDirOrderFirst firstOrder, String bizOrgCode, List<String> skuCodes) {
        //校验是否可铺货
        OrderGoodsIn orderGoodsInIsFirst = new OrderGoodsIn();
        orderGoodsInIsFirst.setStoreCode(firstOrder.getStoreCode());
        orderGoodsInIsFirst.setGoodsCodeList(skuCodes);
        orderGoodsInIsFirst.setBizOrgCode(bizOrgCode);
        orderGoodsInIsFirst.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
        List<String> checkFirstlegalOrderCodeList = orderGoodsServer.findBusinessGoodsCodeList(orderGoodsInIsFirst);
        //所有商品不满足铺货条件
        if (CollectionUtils.isEmpty(checkFirstlegalOrderCodeList)) {
            return null;
        }
        //校验配货
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setStoreCode(firstOrder.getStoreCode());
        orderGoodsIn.setGoodsCodeList(skuCodes);
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DISTRIBUTION_BILL.getType());
        List<String> legalOrderCodeList = orderGoodsServer.findBusinessGoodsCodeList(orderGoodsIn);
        //所有商品不能生成配销单
        if (CollectionUtils.isEmpty(legalOrderCodeList)) {
            return null;
        }
        //取交集
        List<String> codeList = checkFirstlegalOrderCodeList.stream().filter(legalOrderCodeList::contains).collect(Collectors.toList());
        return codeList;
    }

    /**
     * 获取铺货单拆单配置
     *
     * @param bizOrgCode
     * @return
     */
    private FirstOrderConfigOut getFirstOrderConfigOut(String bizOrgCode) {
        FirstOrderConfigOut firstOrderConfigOut = ordDirOrderFirstMapper.getOrderFirstConfig(bizOrgCode);
        if (Objects.isNull(firstOrderConfigOut)) {
            throw new BusinessException("未配置拆单规则");
        }
        if (firstOrderConfigOut.getIsSort().equals(NumberUtil.INTEGER_ONE)) {
            List<FirstOrderSortOut> firstOrderSortOuts = ordDirOrderFirstMapper.findOrderFirstConfigSort(firstOrderConfigOut.getId());
            firstOrderSortOuts.forEach(firstOrderSortOut -> {
                splitSort(firstOrderSortOut, bizOrgCode);
            });
            firstOrderConfigOut.setFirstOrderSortOuts(firstOrderSortOuts);
        }
        return firstOrderConfigOut;
    }

    /**
     * 初始化配货单单
     *
     * @param firstOrder
     * @param bizOrgCode
     * @param key
     * @return
     */
    private OrdDirDeliveryIn initOrdDirDeliveryIn(OrdDirOrderFirst firstOrder, String bizOrgCode, String key) {
        String[] splits = key.split(SystemConstant.SHORT_LINE);
        OrdDirDeliveryIn deliveryOrder = new OrdDirDeliveryIn();
        String type = DistributionWaysEnum.getTypeByName(splits[1]);
        deliveryOrder.setDistributionType(type);
        deliveryOrder.setStockCode(splits[0]);
        StockInfoOut stockInfoOut = stockServer.getTransInfo(deliveryOrder.getStockCode());
        if (Objects.isNull(stockInfoOut) || StringUtil.isEmpty(stockInfoOut.getWarehouseCode())) {
            throw new BusinessException("仓储不存在");
        }
        deliveryOrder.setWrhCode(stockInfoOut.getWarehouseCode());
        deliveryOrder.setOrgCode(firstOrder.getOrgCode());
        deliveryOrder.setBizOrgCode(bizOrgCode);
        deliveryOrder.setStoreCode(firstOrder.getStoreCode());
        deliveryOrder.setStoreName(firstOrder.getStoreName());
        deliveryOrder.setOrderQuantity(BigDecimal.valueOf(firstOrder.getTotalNum()));
        deliveryOrder.setOrderAmount(firstOrder.getTotalAmount());
        deliveryOrder.setDeliveryStatusCode(DeliveryOrderEnum.PENDING.getKey());
        deliveryOrder.setUpdater(firstOrder.getUpdater());
        deliveryOrder.setCreator(firstOrder.getCreator());
        deliveryOrder.setIsDelete(NumberUtil.INTEGER_ZERO);
//        deliveryOrder.setIsFirstOrderSource(NumberUtil.INTEGER_ONE);
        return deliveryOrder;
    }

    /**
     * 初始化配货单明细
     *
     * @param bizOrgCode
     * @param deliveryOrder
     * @param deliveryOrderDetailsList
     * @param firstOrderDetailOut
     */
    private void initOrdDirDeliveryDetail(String bizOrgCode, OrdDirDeliveryIn deliveryOrder, List<OrdDirDeliveryDetail> deliveryOrderDetailsList, OrdDirOrderFirstDetailOut firstOrderDetailOut) {
        OrdDirDeliveryDetail deliveryOrderDetail = new OrdDirDeliveryDetail();
        BeanUtils.copy(firstOrderDetailOut, deliveryOrderDetail);
        OrderGoodsOut orderGoods = getOrderGoodsOut(bizOrgCode, firstOrderDetailOut);
        StockInfoOut stockInfoOut = stockServer.getTransInfo(deliveryOrder.getStockCode());
        BigDecimal warehousePrice = warehouseServer.getWarehousePrice(deliveryOrder.getWrhCode(), deliveryOrder.getStockCode(),
                deliveryOrderDetail.getGoodsCode(), stockInfoOut.getBizOrgCode(), orderGoods.getVendorCode());
        deliveryOrderDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);
        BigDecimal stockPrice = warehouseServer.getStockPrice(deliveryOrder.getStoreCode(), deliveryOrderDetail.getGoodsCode(), bizOrgCode);
        deliveryOrderDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
        deliveryOrderDetail.setSellTax(orderGoods.getOutTax());
        deliveryOrderDetail.setDeliveryOrderId(deliveryOrder.getId());
//        deliveryOrderDetail.setDistributionPackageQuantity(firstOrderDetailOut.getDistributionPackageNum());
        deliveryOrderDetail.setDistributionUnitPrice(firstOrderDetailOut.getDistributionPrice());
//        deliveryOrderDetail.setDistributionAmount(firstOrderDetailOut.getAmount());
        deliveryOrderDetail.setDistributionSpecification(firstOrderDetailOut.getQpcStr());
        deliveryOrderDetail.setDistributionSpecificationNum(firstOrderDetailOut.getDistributionSpecificationNum());
        deliveryOrderDetail.setDistributionSpecificationUnit(firstOrderDetailOut.getDistributionSpecificationUnit());
        deliveryOrderDetail.setOrderQuantity(BigDecimal.valueOf(firstOrderDetailOut.getNum()));
        deliveryOrderDetail.setOrderPackageQuantity(firstOrderDetailOut.getDistributionPackageNum());
        deliveryOrderDetail.setOrderAmount(firstOrderDetailOut.getAmount());
        deliveryOrderDetail.setOrderUnitPrice(firstOrderDetailOut.getDistributionPrice());
        BigDecimal sellTax = Objects.isNull(deliveryOrderDetail.getSellTax()) ? BigDecimal.ZERO : deliveryOrderDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        //配货去金额
        deliveryOrderDetail.setDistributionExceptTaxAmount(deliveryOrderDetail.getOrderAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        deliveryOrderDetail.setDistributionTaxAmount(deliveryOrderDetail.getOrderAmount().subtract(deliveryOrderDetail.getDistributionExceptTaxAmount()));
        deliveryOrderDetail.setWrhCostAmount(deliveryOrderDetail.getWrhPrice().multiply(deliveryOrderDetail.getOrderQuantity()));
        deliveryOrderDetail.setWrhExceptTaxAmount(deliveryOrderDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        deliveryOrderDetail.setWrhTaxAmount(deliveryOrderDetail.getWrhCostAmount().subtract(deliveryOrderDetail.getWrhExceptTaxAmount()));
        deliveryOrderDetail.setStoreCostAmount(deliveryOrderDetail.getStoreStockPrice().multiply(deliveryOrderDetail.getOrderQuantity()));
        deliveryOrderDetail.setStoreExceptTaxAmount(deliveryOrderDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        deliveryOrderDetail.setStoreTaxAmount(deliveryOrderDetail.getStoreCostAmount().subtract(deliveryOrderDetail.getStoreExceptTaxAmount()));
        deliveryOrderDetail.setSmallSort(firstOrderDetailOut.getSort());
        deliveryOrderDetail.setGoodsType(orderGoods.getGoodsType());
//        if (Objects.nonNull(orderGoods.getDistributionSpecification())) {
//            StandardSpecTransInfoOut distributionSpecification = orderGoods.getDistributionSpecification();
//            deliveryOrderDetail.setDistributionSpecification(distributionSpecification.getQpcStr());
//        }
        // 铺货单不参加活动
        deliveryOrderDetail.setIsGift(NumberUtil.INTEGER_ZERO);
        deliveryOrderDetail.setCreator(SystemConstant.SYSTEM_USER);
        deliveryOrderDetail.setUpdater(SystemConstant.SYSTEM_USER);
        deliveryOrderDetail.setIsDelete(NumberUtil.INTEGER_ZERO);
        deliveryOrderDetail.setInvoiceType(orderGoods.getInvoiceType());
        deliveryOrderDetailsList.add(deliveryOrderDetail);
    }

    private void splitSorts(FirstOrderConfigOut firstOrderConfigOut, OrdDirOrderFirstDetailOut orderDetailOut, StringJoiner keys) {
        boolean isOther = true;
        // 配置分类的集合
        for (FirstOrderSortOut firstOrderSortOut : firstOrderConfigOut.getFirstOrderSortOuts()) {
            String[] split = firstOrderSortOut.getSortCode().split(",");
            // 多个分类情况
            if (split.length > 1) {
                for (int i = 0; i < split.length; i++) {
                    if (orderDetailOut.getSort().startsWith(split[i])) {
                        keys.add("group").add(String.valueOf(firstOrderSortOut.getId()));
                        isOther = false;
                        break;
                    }
                }
            } else if (orderDetailOut.getSort().startsWith(firstOrderSortOut.getSortCode())) {
                keys.add(firstOrderSortOut.getSortCode());
                isOther = false;
            }
        }
        // 其他分类
        if (isOther) {
            keys.add("other");
        }
    }

    /**
     * 分割品类名称
     *
     * @param firstOrderSortOut
     * @param bizOrgCode
     */
    private void splitSort(FirstOrderSortOut firstOrderSortOut, String bizOrgCode) {
        StringJoiner categoryName = new StringJoiner(",");
        String[] split = firstOrderSortOut.getSortCode().split(",");
        for (String sortCode : split) {
            OrgSortOut orgSortOut = orderGoodsServer.getByCode(sortCode, bizOrgCode);
            categoryName.add(Objects.nonNull(orgSortOut) ? orgSortOut.getSortName() : "");
        }
        firstOrderSortOut.setSortName(categoryName.toString());
    }

    @Override
    public Page<OrdDirOrderFirstDetailOut> findOrdDirOrderFirstDetailPage(OrdDirOrderFirstDetailIn ordDirOrderFirstDetailIn, String bizOrgCode) {
        List<OrdDirOrderFirstDetailOut> ordDirOrderFirstDetailOuts = ordDirOrderFirstDetailService.findOrdDirOrderFirstDetail(ordDirOrderFirstDetailIn);
        Long firstOrderId = ordDirOrderFirstDetailIn.getFirstOrderId();
        OrdDirOrderFirst query = new OrdDirOrderFirst();
        query.setId(firstOrderId);
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        OrdDirOrderFirst ordDisOrderFirst = this.selectOne(query);
        Map<String, GoodsForOrdOut> goodsOutMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(ordDirOrderFirstDetailOuts)) {
            List<String> goodsCodeList = ordDirOrderFirstDetailOuts.stream().map(OrdDirOrderFirstDetailOut::getGoodsCode).collect(Collectors.toList());
            // 获取商品信息
            OrdQueryGoodsIn ordQueryGoodsIn = new OrdQueryGoodsIn();
            ordQueryGoodsIn.setStoreCode(ordDisOrderFirst.getStoreCode());
            ordQueryGoodsIn.setBizOrgCode(ordDisOrderFirst.getBizOrgCode());
            ordQueryGoodsIn.setGoodsCodes(goodsCodeList);
            List<GoodsForOrdOut> goodsCodesList = orderGoodsServer.findGoodsInfos(ordQueryGoodsIn);
            if (CollectionUtils.isNotEmpty(goodsCodesList)) {
                goodsOutMap = goodsCodesList.stream().collect(Collectors.toMap(GoodsForOrdOut::getGoodsCode, Function.identity()));
            }
        }
        for (OrdDirOrderFirstDetailOut ordDirOrderFirstDetailOut : ordDirOrderFirstDetailOuts) {
            GoodsForOrdOut goodsForOrdOut = goodsOutMap.get(ordDirOrderFirstDetailOut.getGoodsCode());
            if (Objects.isNull(goodsForOrdOut)) {
                continue;
            }
            ordDirOrderFirstDetailOut.setStockCode(goodsForOrdOut.getStockCode());
            ordDirOrderFirstDetailOut.setStockName(goodsForOrdOut.getStockName());
            ordDirOrderFirstDetailOut.setInvoiceType(goodsForOrdOut.getInvoiceType());
            if (goodsForOrdOut.getDistributionSpecification() != null) {
                ordDirOrderFirstDetailOut.setQpcStr(goodsForOrdOut.getDistributionSpecification().getQpcStr());
                BigDecimal specificationNum = BigDecimal.valueOf(goodsForOrdOut.getDistributionSpecification().getQpc());

                // 铺货包装数 （铺货数量/配货规格）
                BigDecimal distributionPackageNum = new BigDecimal(ordDirOrderFirstDetailOut.getNum()).divide(specificationNum, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
                ordDirOrderFirstDetailOut.setDistributionPackageNum(distributionPackageNum);
                ordDirOrderFirstDetailOut.setDistributionSpecificationNum(specificationNum);
                ordDirOrderFirstDetailOut.setDistributionSpecificationUnit(goodsForOrdOut.getDistributionSpecification().getUnitName());
            }
            ordDirOrderFirstDetailOut.setSort(goodsForOrdOut.getSort());
            ordDirOrderFirstDetailOut.setSortName(goodsForOrdOut.getSortName());
            ordDirOrderFirstDetailOut.setDistributionPrice(goodsForOrdOut.getDistributionUnitPrice());
            ordDirOrderFirstDetailOut.setBarCode(goodsForOrdOut.getBarCode());
            ordDirOrderFirstDetailOut.setDistributionType(DistributionWaysEnum.getNameByType(goodsForOrdOut.getDistributionWay()));
        }
        Page<OrdDirOrderFirstDetailOut> outPage = new Page<>(ordDirOrderFirstDetailIn);
        outPage.setList(ordDirOrderFirstDetailOuts);
        return outPage;
    }

    @Override
    public Response checkRepeatParam(InsertFirstOrderDetailIn insertFirstOrderDetailIn, String bizOrgCode) {
        List<OrdDirOrderFirstDetail> firstOrderDetails = insertFirstOrderDetailIn.getFirstOrderDetails();
        // 查找重复的goodsCode
        Map<String, Long> collect = firstOrderDetails.stream().collect(Collectors.groupingBy(OrdDirOrderFirstDetail::getGoodsCode, Collectors.counting()));
        List<String> repeatList = collect.keySet().stream().filter(key -> collect.get(key) > NumberUtil.INTEGER_ONE).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(repeatList)) {
            return Response.error("商品：" + repeatList + "重复维护");
        }
        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<OrdDirOrderFirst> saveFirstOrderDetail(InsertFirstOrderDetailIn insertFirstOrderDetailIn) {
        List<OrdDirOrderFirstDetail> firstOrderDetails = insertFirstOrderDetailIn.getFirstOrderDetails();
        String channelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(insertFirstOrderDetailIn.getStoreCode(), UserUtil.getBizOrgCode());
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrdDirOrderFirstDetail item : firstOrderDetails) {
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setBizOrgCode(channelBizOrgCode);
            orderGoodsIn.setGoodsCode(item.getGoodsCode());
            orderGoodsIn.setStoreCode(insertFirstOrderDetailIn.getStoreCode());
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
            OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
            if (Objects.nonNull(orderGoodsOut)) {
                if (Objects.isNull(orderGoodsOut.getDistributionUnitPrice()) || BigDecimal.ZERO.compareTo(orderGoodsOut.getDistributionUnitPrice()) == NumberUtil.INTEGER_ZERO) {
                    throw new BusinessException(item.getGoodsCode() + "-此商品无配销价");
                }
                item.setAmount(orderGoodsOut.getDistributionUnitPrice().multiply(new BigDecimal(item.getNum())));
            } else {
                throw new BusinessException(item.getGoodsCode() + "-此商品不可铺货");
            }
            totalAmount = totalAmount.add(item.getAmount());
        }
        Long firstOrderId = insertFirstOrderDetailIn.getFirstOrderId();
        OrdDirOrderFirst firstOrder = new OrdDirOrderFirst();
        firstOrder.setTotalAmount(totalAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        firstOrder.setIsEffectiveImmediately(insertFirstOrderDetailIn.getIsEffectiveImmediately());
        firstOrder.setEffectiveTime(insertFirstOrderDetailIn.getEffectiveTime());
        if (NumberUtil.INTEGER_ONE.equals(insertFirstOrderDetailIn.getIsEffectiveImmediately())) {
            firstOrder.setEffectiveTime(LocalDateTime.now());
        }
        if (firstOrderId == null) {
            // 无单则新增
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(insertFirstOrderDetailIn.getStoreCode());
            if (storeOut == null) {
                return Response.error("门店不存在");
            }
            firstOrder.setStoreName(storeOut.getStoreName());
            firstOrder.setBizOrgCode(UserUtil.getBizOrgCode());
            firstOrder.setStoreCode(insertFirstOrderDetailIn.getStoreCode());
            firstOrderId = this.insertFirstOrder(firstOrder);
            if (firstOrderId == null) {
                return Response.error("首单铺货订单创建失败");
            }
        } else {
            // 有单则删除整单明细

            ordDirOrderFirstDetailService.deleteByFirstOrderId(firstOrderId);
        }
        // 计算分批入库的批次
        int limit = (firstOrderDetails.size() + MAX_NUMBER - 1) / MAX_NUMBER;
        Long finalFirstOrderId = firstOrderId;
        Stream.iterate(0, n -> n + 1).limit(limit).forEach(a -> {
            // 拿到这个参数的流的 （a * applyIdSelectSize）后面的数据  .limit（applyIdSelectSize）->后面数据的500条  .collect(Collectors.toList()->组成一个toList
            List<OrdDirOrderFirstDetail> insertList = firstOrderDetails.stream().skip(a * MAX_NUMBER).limit(MAX_NUMBER).collect(Collectors.toList());
            // 分批入库
            ordDirOrderFirstDetailService.batchInsertFirstOrderDetail(insertList, UserUtil.getNickname(), finalFirstOrderId);
        });

        // 计算铺货单统计数值
        firstOrder.setId(firstOrderId);
        firstOrder.setGoodsNum(firstOrderDetails.size());
        Integer distributionTotalNum = firstOrderDetails.stream().mapToInt(OrdDirOrderFirstDetail::getNum).sum();
        firstOrder.setTotalNum(distributionTotalNum);
        this.updateByPrimaryKeySelective(firstOrder);
        return Response.data(firstOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDirOrderFirst getOrdDirOrderFirstForImport(Long firstOrderId, StoreOut storeOut, String loginUsername, Integer isEffectiveImmediately, LocalDateTime effectiveTime) {
        OrdDirOrderFirst ordDirOrderFirst = new OrdDerDirOrderFirstOut();
        ordDirOrderFirst.setUpdater(loginUsername);
        ordDirOrderFirst.setIsEffectiveImmediately(isEffectiveImmediately);
        ordDirOrderFirst.setEffectiveTime(effectiveTime);
        if (Objects.isNull(firstOrderId)) {
            ordDirOrderFirst.setStoreCode(storeOut.getStoreCode());
            ordDirOrderFirst.setStoreName(storeOut.getStoreName());
            ordDirOrderFirst.setTotalNum(NumberUtil.INTEGER_ZERO);
            ordDirOrderFirst.setTotalAmount(BigDecimal.ZERO);
            ordDirOrderFirst.setGoodsNum(NumberUtil.INTEGER_ZERO);
            ordDirOrderFirst.setBizOrgCode(UserUtil.getBizOrgCode());
            ordDirOrderFirst.setOrgCode(UserUtil.getOrgCode());
            ordDirOrderFirst.setCreator(loginUsername);
            String firstOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.SP.getCode(), ordDirOrderFirst.getBizOrgCode(), uniqueUtils, 4);
            ordDirOrderFirst.setFirstOrderNo(firstOrderNo);
            ordDirOrderFirst.setFirstOrderStatus(FirstOrderStatusEnum.PENDING.getCode());
            ordDirOrderFirst.setIsDelete(ModelConst.DELETE.NO);
            ordDirOrderFirst.setCreateTime(LocalDateTime.now());
            ordDirOrderFirstMapper.insert(ordDirOrderFirst);
            // 添加日志
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
                    String.valueOf(ordDirOrderFirst.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
                    OrdLogTypeEnum.FIRST_ORDER_GOODS_SAVE.getName(), new Date(), ordDirOrderFirst.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        } else {
            ordDirOrderFirst = ordDirOrderFirstMapper.selectByPrimaryKey(firstOrderId);
            ordDirOrderFirstMapper.updateByPrimaryKeySelective(ordDirOrderFirst);
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
                    String.valueOf(ordDirOrderFirst.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(),
                    OrdLogTypeEnum.FIRST_ORDER_GOODS_UPDATE.getName(), new Date(), ordDirOrderFirst.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return ordDirOrderFirst;
    }

    @Override
    public Response<Long> asyncImportFirstOrderDetail(String fileId, OrdDirOrderFirst ordDirOrderFirst, String loginUsername) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (null == bytes) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DirFirstDirOrderAsyncListener listener = new DirFirstDirOrderAsyncListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportFirstOrderDetail.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > 0) {
            return Response.data(ordDirOrderFirst.getId(), "导入失败：" + listener.getImportErrorMessage(totalErrorMap));
        }
        List<ImportFirstOrderDetail> importFirstOrderDetailList = listener.getOutDetails();
        dirFirstOrderAsyncImportHandle.handleAsyncFirstOrder(ordDirOrderFirst.getId(), importFirstOrderDetailList, loginUsername);
        return Response.data(ordDirOrderFirst.getId(), "文件导入中，稍后刷新查看！");
    }

    @Override
    public Response<String> auditFirstOrder(OrdDirFirstOrderAuditIn ordDirFirstOrderAuditIn, String loginUsername) {
        OrdDirOrderFirst ordDirOrderFirst = ordDirOrderFirstMapper.selectByPrimaryKey(ordDirFirstOrderAuditIn.getFirstOrderId());
        if (Objects.isNull(ordDirOrderFirst)) {
            return Response.error("直营铺货单不存在");
        }
        if (FirstOrderStatusEnum.INVALID.getCode().equals(ordDirOrderFirst.getFirstOrderStatus())) {
            return Response.error("铺货单已作废");
        }
        if (FirstOrderStatusEnum.APPROVED.getCode().equals(ordDirOrderFirst.getFirstOrderStatus())) {
            return Response.error("请勿重复审核");
        }
        int detailCount = ordDirOrderFirstDetailService.countByFirstOrderId(ordDirFirstOrderAuditIn.getFirstOrderId());
        if (detailCount == NumberUtil.INTEGER_ZERO) {
            return Response.error("直营铺货单明细为空");
        }
        String key = SystemConstant.ORD_DIR_FIRST_ORDER_AUDIT + SystemConstant.COLON + ordDirOrderFirst.getBizOrgCode()
                + SystemConstant.COLON + ordDirOrderFirst.getFirstOrderNo();
        if (!redisService.setIfAbsent(key, ordDirOrderFirst.getFirstOrderNo(), 20l, TimeUnit.MINUTES)) {
            return Response.error("审核中，请勿重复审核");
        }
        dirFirstOrderAuditHandle.asyncCheckByAuditFirstOrder(ordDirFirstOrderAuditIn, loginUsername, key);
        return Response.success("审核中，请稍后查看");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDirFirstOrder(OrdDirOrderFirst ordDirOrderFirst) {
        String firstOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.SP.getCode(), ordDirOrderFirst.getBizOrgCode(), uniqueUtils, 4);
        ordDirOrderFirst.setFirstOrderNo(firstOrderNo);
        ordDirOrderFirst.setFirstOrderStatus(FirstOrderStatusEnum.PENDING.getCode());
        ordDirOrderFirst.setOrgCode(UserUtil.getOrgCode());
        ordDirOrderFirst.setCreateTime(LocalDateTime.now());
        ordDirOrderFirst.setUpdateTime(LocalDateTime.now());
        ordDirOrderFirst.setIsDelete(ModelConst.DELETE.NO);
        ordDirOrderFirstMapper.insert(ordDirOrderFirst);
        return ordDirOrderFirst.getId();
    }

    /**
     * 校验商品信息
     *
     * @param goodsCode
     * @param storeCode
     * @param bizOrgCode
     */
    public void checkOrderGoods(String goodsCode, String storeCode, String bizOrgCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setGoodsCode(goodsCode);
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
        OrderGoodsOut storeOrderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.nonNull(storeOrderGoods)) {
            if (Objects.isNull(storeOrderGoods.getDistributionPrice()) || BigDecimal.ZERO.compareTo(storeOrderGoods.getDistributionPrice()) == NumberUtil.INTEGER_ZERO) {
                throw new BusinessException(goodsCode + "-此商品无配销价");
            }
        } else {
            throw new BusinessException(goodsCode + "-此商品不可铺货");
        }

    }

    /**
     * 获取商品信息
     *
     * @param bizOrgCode
     * @param firstOrderDetailOut
     * @return
     */
    private OrderGoodsOut getOrderGoodsOut(String bizOrgCode, OrdDirOrderFirstDetailOut firstOrderDetailOut) {
        OrderGoodsIn orderGoodsInTar = new OrderGoodsIn();
        orderGoodsInTar.setGoodsCode(firstOrderDetailOut.getGoodsCode());
        orderGoodsInTar.setBizOrgCode(bizOrgCode);
        OrderGoodsOut orderGoods = orderGoodsServer.getOrderGoods(orderGoodsInTar);
        if (Objects.isNull(orderGoods)) {
            throw new BusinessException(firstOrderDetailOut.getGoodsCode() + "商品信息不存在");
        }
        return orderGoods;
    }
}
