package com.edc.erp.directly.dirrequestorder.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OrderNoPreEnum;
import com.edc.erp.common.enumeration.RequestOrderStatusEnum;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequest;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDetail;
import com.edc.erp.directly.dirrequestorder.mapper.OrdDirDelivRequestMapper;
import com.edc.erp.directly.dirrequestorder.model.in.DirRequestOrderDtlPageIn;
import com.edc.erp.directly.dirrequestorder.model.in.DirRequestOrderPageIn;
import com.edc.erp.directly.dirrequestorder.model.out.DirRequestOrderDetailOut;
import com.edc.erp.directly.dirrequestorder.model.out.DirRequestOrderOut;
import com.edc.erp.directly.dirrequestorder.model.out.ExcelRequestOrderOut;
import com.edc.erp.directly.dirrequestorder.service.OrdDirDelivRequestDetailService;
import com.edc.erp.directly.dirrequestorder.service.OrdDirDelivRequestService;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.model.out.AppOrderDetailOut;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigItemCodeEnum;
import com.edc.erp.directly.enumeration.OrderLogEnum;
import com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.directly.orderscheduing.handle.DirOrderProcessSchedulingHandle;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.export.AsyncExportExecutor;
import com.edc.plugins.export.ExportExcelByPage;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 要货单(OrdDirDelivRequest)表服务实现类
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrdDirDelivRequestServiceImpl extends BaseServiceImpl<OrdDirDelivRequest> implements OrdDirDelivRequestService {
    @Autowired
    private OrdDirDelivRequestMapper ordDirDelivRequestMapper;
    @Autowired
    private StoreCenterService storeCenterService;
    @Autowired
    private OrdDirDelivRequestDetailService ordDirDelivRequestDetailService;
    @Autowired
    private UniqueUtils uniqueUtils;
    @Autowired
    private OrderHandle orderHandle;
    @Autowired
    private DirOrderProcessSchedulingHandle dirOrderProcessSchedulingHandle;
    @Autowired
    private AsyncExportExecutor asyncExportExecutor;


    @Override
    public Page<DirRequestOrderOut> findRequestOrderListForPage(DirRequestOrderPageIn dirRequestOrderPageIn) {
        // 当门店名称，门店区域等不为空时，先得到所有的门店代码集合
        if (StringUtils.isNotEmpty(dirRequestOrderPageIn.getStoreArea()) || StringUtils.isNotEmpty(dirRequestOrderPageIn.getStoreName())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(dirRequestOrderPageIn.getStoreArea(),
                    dirRequestOrderPageIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return new Page<>(dirRequestOrderPageIn);
            }
            dirRequestOrderPageIn.setStoreCodeList(storeCodeList);
        }

        List<DirRequestOrderOut> list = ordDirDelivRequestMapper.findRequestOrderListByPage(dirRequestOrderPageIn);
        list.forEach(this::setNosAndCode);
        Page<DirRequestOrderOut> resPage = new Page<>(dirRequestOrderPageIn);
        resPage.setList(list);
        return resPage;
    }

    @Override
    public DirRequestOrderOut getRequestOrderById(Long requestOrderId) {
        DirRequestOrderOut out = ordDirDelivRequestMapper.getRequestOrderById(requestOrderId);
        if (Objects.isNull(out)) {
            return null;
        }
        //明细
        DirRequestOrderDtlPageIn pageIn = new DirRequestOrderDtlPageIn();
        pageIn.setRequestOrderId(requestOrderId);
        Page<DirRequestOrderDetailOut> requestOrderDetailList = ordDirDelivRequestDetailService.findRequestOrderDetailList(pageIn);
        List<DirRequestOrderDetailOut> dirRequestOrderDetailOuts = requestOrderDetailList.getList();
        out.setOrderDetailOutList(dirRequestOrderDetailOuts);
        //关联订货单集合
        List<OrdDirOrder> ordDirOrders = ordDirDelivRequestMapper.findOrderByRequestOrderId(requestOrderId, out.getBizOrgCode());
        out.setOrdDirOrders(ordDirOrders);
        //关联配货单集合
        List<OrdDirDelivery> ordDirDeliveries = ordDirDelivRequestMapper.findDelivOrderByRequestOrderId(requestOrderId, out.getBizOrgCode());
        out.setOrdDirDeliveries(ordDirDeliveries);
        setNosAndCode(out);
        out.setSkuNumber(ordDirDelivRequestDetailService.getSkuNumberById(out.getRequestOrderId()));
        return out;
    }

    @Override
    public String export(DirRequestOrderPageIn dirRequestOrderPageIn) {
        // 设置每次查询条数
        dirRequestOrderPageIn.setPageSize(10000);
        String title = "门店要货单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "门店要货单列表",
                        // 导出模板实体
                        ExcelRequestOrderOut.class,
                        // 分页查询对象
                        dirRequestOrderPageIn,
                        // 分页查询方法
                        page -> {
                            Page<DirRequestOrderOut> requestOrderListForPage = this.findRequestOrderListForPage(dirRequestOrderPageIn);
                            List<ExcelRequestOrderOut> excelRequestOrderOuts = parseDataToExcel(requestOrderListForPage.getList());
                            log.info("导出门店要货单列表集合大小是--{}", excelRequestOrderOuts.size());
                            return excelRequestOrderOuts;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    /**
     * 根据要货单id查询要货单信息
     *
     * @param requestOrderId
     * @return
     */
    @Override
    public OrdDirDelivRequest getRequestOrderByRequestOrderId(Long requestOrderId) {
        return ordDirDelivRequestMapper.selectByPrimaryKey(requestOrderId);
    }

    /**
     * 创建要货单
     *
     * @param orderCycle
     * @param legalOrderMap
     * @param itemCode
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDirDelivRequest createRequestOrder(OrdDirOrderCycle orderCycle, Map<OrdDirOrder, List<AppOrderDetailOut>> legalOrderMap, String itemCode) {
        OrdDirDelivRequest requestOrder = new OrdDirDelivRequest();
        requestOrder.setStoreCode(orderCycle.getStoreCode());
        requestOrder.setOrderCycleId(orderCycle.getId());
        requestOrder.setOrderTypeConfigId(orderCycle.getOrderTypeConfigId());
        requestOrder.setTruncationDateTime(orderCycle.getTruncationDateTime());
        String requestOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.MY.getCode(), orderCycle.getBizOrgCode(), uniqueUtils, 4);
        log.info("门店" + orderCycle.getStoreCode() + "订货周期" + orderCycle.getTruncationDateTime() + "创建要货单" + requestOrderNo);
        requestOrder.setRequestOrderNo(requestOrderNo);
        requestOrder.setStatusCode(RequestOrderStatusEnum.COLLECTED.getKey());
        requestOrder.setTotalAmount(BigDecimal.ZERO);
        requestOrder.setOrgCode(orderCycle.getOrgCode());
        requestOrder.setBizOrgCode(orderCycle.getBizOrgCode());
        requestOrder.setCreator(SystemConstant.SYSTEM_USER);
        requestOrder.setUpdater(SystemConstant.SYSTEM_USER);
        requestOrder.setIsDelete(ModelConst.DELETE.NO);
        this.saveRequestOrder(requestOrder);
        List<OrdDirDelivRequestDetail> requestOrderDetailList = null;
        // 累加
        if (OrderCycleProcessConfigItemCodeEnum.ACCUMULATION.getCode().equals(itemCode)) {
            requestOrderDetailList = this.accumulation(legalOrderMap, requestOrder);
        }
        // 覆盖
        if (OrderCycleProcessConfigItemCodeEnum.COVER.getCode().equals(itemCode)) {
            requestOrderDetailList = this.cover(legalOrderMap, requestOrder);
        }
        // 取大值
        if (OrderCycleProcessConfigItemCodeEnum.TAKE_BIG_VALUE.getCode().equals(itemCode)) {
            requestOrderDetailList = this.takeBigValue(legalOrderMap, requestOrder);
        }
        requestOrder.setTotalAmount(requestOrder.getTotalAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        List<OrdDirOrder> orderList = legalOrderMap.keySet().stream().collect(Collectors.toList());
        ordDirDelivRequestMapper.updateByPrimaryKeySelective(requestOrder);
        ordDirDelivRequestDetailService.batchSave(requestOrderDetailList);
        orderHandle.changeRequestOrder(orderList, OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), SystemConstant.SYSTEM_USER);
        orderHandle.batchUpdateOrderRequestRel(orderList, requestOrder.getId(), SystemConstant.SYSTEM_USER);
        return requestOrder;
    }

    /**
     * 根据配货单id查询要货单信息
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    @Override
    public OrdDirDelivRequest getRequestOrderByDeliveryOrderIdAndBizOrgCode(Long id, String bizOrgCode) {
        return ordDirDelivRequestMapper.getRequestOrderByDeliveryOrderIdAndBizOrgCode(id, bizOrgCode);
    }

    /**
     * 修改要货单状态
     *
     * @param id
     * @param requestStatusCode
     * @param systemUser
     * @param bizOrgCode
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRequestOrderStatus(Long id, String requestStatusCode, String systemUser, String bizOrgCode) {
        OrdDirDelivRequest ordDisDelivRequest = new OrdDirDelivRequest();
        ordDisDelivRequest.setId(id);
        ordDisDelivRequest.setStatusCode(requestStatusCode);
        ordDisDelivRequest.setUpdater(systemUser);
        ordDisDelivRequest.setBizOrgCode(bizOrgCode);
        ordDirDelivRequestMapper.updateByPrimaryKeySelective(ordDisDelivRequest);
    }

    /**
     * 保存要货单
     *
     * @param requestOrder
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRequestOrder(OrdDirDelivRequest requestOrder) {
        ordDirDelivRequestMapper.insertSelective(requestOrder);
    }

    @Override
    public List<String> findNotToDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode) {
        return ordDirDelivRequestMapper.findNotToDeliveryOrderList(createTimeBegin, createTimeEnd, RequestOrderStatusEnum.COLLECTED.getKey(), bizOrgCode);
    }

    @Override
    public BigDecimal requestOrderSummary(DirRequestOrderPageIn dirRequestOrderPageIn) {
        // 当门店名称，门店区域等不为空时，先得到所有的门店代码集合
        if (StringUtils.isNotEmpty(dirRequestOrderPageIn.getStoreArea()) || StringUtils.isNotEmpty(dirRequestOrderPageIn.getStoreName())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(dirRequestOrderPageIn.getStoreArea(),
                    dirRequestOrderPageIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return BigDecimal.ZERO;
            }
            dirRequestOrderPageIn.setStoreCodeList(storeCodeList);
        }
        return ordDirDelivRequestMapper.requestOrderSummary(dirRequestOrderPageIn);
    }

    /**
     * 累加创建要货单明细，并且更新要货单总金额
     *
     * @param legalOrderMap
     * @param requestOrder
     * @return
     */
    private List<OrdDirDelivRequestDetail> accumulation(Map<OrdDirOrder, List<AppOrderDetailOut>> legalOrderMap, OrdDirDelivRequest requestOrder) {
//        Map<String, OrdDirDelivRequestDetail> skuMap = new TreeMap<>();
//        legalOrderMap.entrySet().forEach(legalEntry -> {
//            List<OrdDirOrderDetail> orderDetailList = legalEntry.getValue();
//            orderDetailList.stream().forEach(orderDetail -> {
//                String key = orderDetail.getGoodsCode() + "-" + orderDetail.getIsGift();
//                OrdDirDelivRequestDetail requestOrderDetail = skuMap.get(key);
//                if (Objects.isNull(requestOrderDetail)) {
//                    requestOrderDetail = new OrdDirDelivRequestDetail();
//                    BeanUtils.copy(orderDetail, requestOrderDetail);
//                    requestOrderDetail.setStockCode(orderDetail.getPosition());
//                    requestOrderDetail.setOriginalUnitPrice(orderDetail.getOrderUnitPrice());
//                    requestOrderDetail.setRequestOrderId(requestOrder.getId());
//                } else {
//                    requestOrderDetail.setPackageQuantity(requestOrderDetail.getPackageQuantity().add(orderDetail.getPackageQuantity()));
//                    requestOrderDetail.setQuantity(requestOrderDetail.getQuantity().add(orderDetail.getQuantity()));
//                }
//                requestOrderDetail.setRequestOrderAmount(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity()));
//                requestOrder.setTotalAmount(requestOrder.getTotalAmount().add(requestOrderDetail.getOriginalUnitPrice().multiply(orderDetail.getQuantity())));
//                skuMap.put(key, requestOrderDetail);
//            });
//            OrdDirOrder order = legalEntry.getKey();
//        });
        Map<String, OrdDirDelivRequestDetail> skuMap = new HashMap<>();
        Map<String, Map<String, OrdDirDelivRequestDetail>> giftMap = new HashMap<>();
        legalOrderMap.forEach((ordDirOrder, orderDetailList) -> orderDetailList.forEach(orderDetail -> {
            if (CollectionUtils.isNotEmpty(orderDetail.getGiftOutList())) {
                Map<String, OrdDirDelivRequestDetail> giftOrderDetails = giftMap.containsKey(orderDetail.getGoodsCode()) ? giftMap.get(orderDetail.getGoodsCode()) : new HashMap<>();
                orderDetail.getGiftOutList().forEach(gift -> {
                    OrdDirDelivRequestDetail giftDtl;
                    if (giftOrderDetails.containsKey(gift.getGoodsCode())) {
                        giftDtl = giftOrderDetails.get(gift.getGoodsCode());
                        giftDtl.setPackageQuantity(giftDtl.getPackageQuantity().add(gift.getPackageQuantity()));
                        giftDtl.setQuantity(giftDtl.getQuantity().add(gift.getQuantity()));
                    } else {
                        giftDtl = this.initOrdDirDelivRequestDetailByOrderDtl(gift, requestOrder.getId());
                    }
                    giftOrderDetails.put(gift.getGoodsCode(), giftDtl);
                });
                giftMap.put(orderDetail.getGoodsCode(), giftOrderDetails);
            }
            String key = orderDetail.getGoodsCode() + SystemConstant.SHORT_LINE + orderDetail.getIsGift();
            OrdDirDelivRequestDetail requestOrderDetail = skuMap.get(key);
            if (Objects.isNull(requestOrderDetail)) {
                requestOrderDetail = this.initOrdDirDelivRequestDetailByOrderDtl(orderDetail, requestOrder.getId());
            } else {
                requestOrderDetail.setPackageQuantity(requestOrderDetail.getPackageQuantity().add(orderDetail.getPackageQuantity()));
                requestOrderDetail.setQuantity(requestOrderDetail.getQuantity().add(orderDetail.getQuantity()));
            }
            requestOrderDetail.setRequestOrderAmount(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity()));
            requestOrder.setTotalAmount(requestOrder.getTotalAmount().add(requestOrderDetail.getOriginalUnitPrice().multiply(orderDetail.getQuantity())));
            skuMap.put(key, requestOrderDetail);
        }));
        if (giftMap.size() > 0) {
            giftMap.forEach((key, orderDtlGiftMap) -> orderDtlGiftMap.forEach((giftGoodsCode, value) -> skuMap.put(giftGoodsCode + SystemConstant.SHORT_LINE + value.getBaseGoodsCode() + SystemConstant.SHORT_LINE + value.getIsGift(), value)));
        }
        List<OrdDirDelivRequestDetail> requestOrderDetailList = new ArrayList<>(skuMap.values());
        log.info("累加创建要货单明细条数是-{},要货单号是--{}", requestOrderDetailList.size(), requestOrder.getRequestOrderNo());
        return requestOrderDetailList;
    }

    /**
     * 覆盖创建要货单明细，并且更新要货单总金额
     *
     * @param legalOrderMap
     * @param requestOrder
     * @return
     */
    private List<OrdDirDelivRequestDetail> cover(Map<OrdDirOrder, List<AppOrderDetailOut>> legalOrderMap, OrdDirDelivRequest requestOrder) {
        Map<String, AppOrderDetailOut> skuMap = new HashMap<>();
        legalOrderMap.forEach((ordDirOrder, orderDetailList) -> orderDetailList.forEach(orderDetail -> {
            String key = orderDetail.getGoodsCode();
            AppOrderDetailOut original = skuMap.get(key);
            if (Objects.nonNull(original)) {
                skuMap.put(key, orderDetail.getCreateTime().isAfter(original.getCreateTime()) ? orderDetail : original);
            } else {
                skuMap.put(key, orderDetail);
            }
        }));
        return this.initDetailListAndFigureUpOrderAmount(skuMap, requestOrder);
    }

    /**
     * 取大值
     *
     * @param legalOrderMap
     * @param requestOrder
     * @return
     */
    private List<OrdDirDelivRequestDetail> takeBigValue(Map<OrdDirOrder, List<AppOrderDetailOut>> legalOrderMap, OrdDirDelivRequest requestOrder) {
        Map<String, AppOrderDetailOut> skuMap = new HashMap<>();
        legalOrderMap.forEach((ordDirOrder, orderDetailList) -> orderDetailList.forEach(orderDetail -> {
            String key = orderDetail.getGoodsCode();
            AppOrderDetailOut original = skuMap.get(key);
            if (Objects.nonNull(original)) {
                skuMap.put(key, orderDetail.getQuantity().compareTo(original.getQuantity()) > 0 ? orderDetail : original);
            } else {
                skuMap.put(key, orderDetail);
            }
        }));
        return this.initDetailListAndFigureUpOrderAmount(skuMap, requestOrder);
    }

    /**
     * 将订货单明细对象转为要货单明细对象
     *
     * @param orderDetail
     * @param requestOrderId
     * @return
     */
    private OrdDirDelivRequestDetail initOrdDirDelivRequestDetailByOrderDtl(OrdDirOrderDetail orderDetail, Long requestOrderId) {
        OrdDirDelivRequestDetail requestOrderDetail = new OrdDirDelivRequestDetail();
        BeanUtils.copy(orderDetail, requestOrderDetail);
        requestOrderDetail.setRequestOrderId(requestOrderId);
        requestOrderDetail.setStockCode(orderDetail.getPosition());
        requestOrderDetail.setOriginalUnitPrice(orderDetail.getOrderUnitPrice());
        requestOrderDetail.setRequestOrderAmount(orderDetail.getOrderUnitPrice().multiply(orderDetail.getQuantity()));
        return requestOrderDetail;
    }

    /**
     * 封装要货单明细集合并计算要货单总金额
     *
     * @param skuMap
     * @param requestOrder
     * @return
     */
    private List<OrdDirDelivRequestDetail> initDetailListAndFigureUpOrderAmount(Map<String, AppOrderDetailOut> skuMap, OrdDirDelivRequest requestOrder) {
        List<OrdDirDelivRequestDetail> requestOrderDetailList = Lists.newArrayList();
        skuMap.forEach((key, orderDetail) -> {
            OrdDirDelivRequestDetail requestOrderDetail = this.initOrdDirDelivRequestDetailByOrderDtl(orderDetail, requestOrder.getId());
            requestOrderDetail.setRequestOrderAmount(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity()));
            requestOrderDetailList.add(requestOrderDetail);
            if (CollectionUtils.isNotEmpty(orderDetail.getGiftOutList())) {
                orderDetail.getGiftOutList().forEach(originalGift -> {
                    OrdDirDelivRequestDetail gift = this.initOrdDirDelivRequestDetailByOrderDtl(originalGift, requestOrder.getId());
                    requestOrderDetailList.add(gift);
                });
            }
            requestOrder.setTotalAmount(requestOrder.getTotalAmount().add(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity())));
        });
        return requestOrderDetailList;
    }


    /**
     * 将查询得到的列表集合转换为导出集合
     *
     * @param list
     * @return
     */
    private List<ExcelRequestOrderOut> parseDataToExcel(List<DirRequestOrderOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 将dirRequestOrderOut转换为导出ExcelRequestOrderOut
     *
     * @param dirRequestOrderOut
     * @param index
     * @return
     */
    private ExcelRequestOrderOut convertExcel(DirRequestOrderOut dirRequestOrderOut, int index) {
        ExcelRequestOrderOut excelRequestOrderOut = new ExcelRequestOrderOut();
        BeanUtils.copy(dirRequestOrderOut, excelRequestOrderOut);
        excelRequestOrderOut.setIndex(index + 1);
        excelRequestOrderOut.setShortOrderType(dirRequestOrderOut.getOrderTypeName() + "[" + dirRequestOrderOut.getOrderTypeCode() + "]");
        return excelRequestOrderOut;
    }

    private void setNosAndCode(DirRequestOrderOut item) {
        String deliveryOrderNos = ordDirDelivRequestMapper.getDeliveryOrderNosById(item.getRequestOrderId(), item.getBizOrgCode());
        String orderNos = ordDirDelivRequestMapper.getOrderNosById(item.getRequestOrderId(), item.getBizOrgCode());
        Integer goodsItemNumber = ordDirDelivRequestDetailService.getGoodsItemNumber(item.getRequestOrderId());
        item.setGoodsItemNumber(goodsItemNumber);
        item.setDeliveryOrderNos(deliveryOrderNos);
        item.setOrderNos(orderNos);

        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(item.getStoreCode());
        if (Objects.nonNull(storeOut)) {
            item.setStoreName(storeOut.getStoreName());
            item.setStoreArea(storeOut.getArea());
        }
        item.setStatusCodeStr(RequestOrderStatusEnum.getValueByKey(item.getStatusCode()));
    }

    /**
     * 处理截单-发送订货单更新状态至可创建要货单前消息
     *
     * @param orderList
     * @param orderCycle
     * @param loginUsername
     */
    public void sendBeforeCreateRequestOrderMqForCutOrder(List<OrdDirOrder> orderList, OrdDirOrderCycle orderCycle, String loginUsername, Boolean addPushFlag, String auditType) {
        SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = new SendBeforeCreateRequestOrderMqIn();
        sendBeforeCreateRequestOrderMqIn.setOrderCycleId(orderCycle.getId());
        sendBeforeCreateRequestOrderMqIn.setBizOrgCode(orderCycle.getBizOrgCode());
        sendBeforeCreateRequestOrderMqIn.setOrderList(orderList);
        sendBeforeCreateRequestOrderMqIn.setLoginUsername(loginUsername);
        sendBeforeCreateRequestOrderMqIn.setAddPushFlag(addPushFlag);
        sendBeforeCreateRequestOrderMqIn.setAuditType(auditType);
        log.info("门店" + orderCycle.getStoreCode() + "订货周期" + orderCycle.getTruncationDateTime() +
                orderCycle.getShortOrderType() + "即将发起处理创建要货单");
        dirOrderProcessSchedulingHandle.handleBeforeCreateRequestOrder(sendBeforeCreateRequestOrderMqIn);
    }
}
