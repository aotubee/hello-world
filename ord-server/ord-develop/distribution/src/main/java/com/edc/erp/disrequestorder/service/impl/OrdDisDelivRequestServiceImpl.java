package com.edc.erp.disrequestorder.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.FundReturnTypeEnum;
import com.edc.erp.common.enumeration.OrderNoPreEnum;
import com.edc.erp.common.enumeration.RequestOrderStatusEnum;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequest;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDetail;
import com.edc.erp.disrequestorder.mapper.OrdDisDelivRequestMapper;
import com.edc.erp.disrequestorder.model.in.BackQueryRequestOrderDtlPageIn;
import com.edc.erp.disrequestorder.model.in.BackQueryRequestOrderPageIn;
import com.edc.erp.disrequestorder.model.out.BackRequestOrderDetailOut;
import com.edc.erp.disrequestorder.model.out.BackRequestOrderOut;
import com.edc.erp.disrequestorder.model.out.ExcelRequestOrderOut;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestDetailService;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestService;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.model.out.AppOrderDetailOut;
import com.edc.erp.enumeration.OrderCycleProcessConfigItemCodeEnum;
import com.edc.erp.enumeration.OrderLogEnum;
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
import org.apache.ibatis.annotations.Param;
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
 * 集货单(OrdDisDelivRequest)表服务实现类
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrdDisDelivRequestServiceImpl extends BaseServiceImpl<OrdDisDelivRequest> implements OrdDisDelivRequestService {

    @Autowired
    private OrdDisDelivRequestMapper ordDisDelivRequestMapper;
    @Autowired
    private StoreCenterService storeCenterService;
    @Autowired
    private OrdDisDelivRequestDetailService ordDisDelivRequestDetailService;
    @Autowired
    private FileService fileService;

    @Autowired
    private UniqueUtils uniqueUtils;

    @Autowired
    private DisOrderHandle orderHandle;

    @Autowired
    private AsyncExportExecutor asyncExportExecutor;

    @Override
    public Page<BackRequestOrderOut> findRequestOrderListForPage(BackQueryRequestOrderPageIn backQueryRequestOrderPageIn) {
        // 当门店名称，门店区域等不为空时，先得到所有的门店代码集合
        if (StringUtils.isNotEmpty(backQueryRequestOrderPageIn.getStoreArea()) || StringUtils.isNotEmpty(backQueryRequestOrderPageIn.getStoreName())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(backQueryRequestOrderPageIn.getStoreArea(),
                    backQueryRequestOrderPageIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return new Page<>(backQueryRequestOrderPageIn);
            }
            backQueryRequestOrderPageIn.setStoreCodeList(storeCodeList);
        }

        List<BackRequestOrderOut> list = ordDisDelivRequestMapper.findRequestOrderListByPage(backQueryRequestOrderPageIn);
        list.forEach(this::setNosAndCode);
        Page<BackRequestOrderOut> resPage = new Page<>(backQueryRequestOrderPageIn);
        resPage.setList(list);
        return resPage;
    }

    @Override
    public BackRequestOrderOut getRequestOrderById(Long requestOrderId) {
        BackRequestOrderOut out = ordDisDelivRequestMapper.getRequestOrderById(requestOrderId);
        if (Objects.isNull(out)) {
            return null;
        }
        //明细
        BackQueryRequestOrderDtlPageIn pageIn = new BackQueryRequestOrderDtlPageIn();
        pageIn.setRequestOrderId(requestOrderId);
        Page<BackRequestOrderDetailOut> requestOrderDetailList = ordDisDelivRequestDetailService.findRequestOrderDetailList(pageIn);
        List<BackRequestOrderDetailOut> disRequestOrderDetailOuts = requestOrderDetailList.getList();
        out.setOrderDetailOutList(disRequestOrderDetailOuts);
        //关联订货单
        List<OrdDisOrder> ordDisOrders = ordDisDelivRequestMapper.findOrderByRequestOrderId(requestOrderId, out.getBizOrgCode());
        out.setOrdDisOrders(ordDisOrders);
        //关联配销单
        List<OrdDisDelivery> ordDisDeliveries = ordDisDelivRequestMapper.findDelivOrderByRequestOrderId(requestOrderId, out.getBizOrgCode());
        out.setOrdDisDeliveries(ordDisDeliveries);
        setNosAndCode(out);
        out.setSkuNumber(ordDisDelivRequestDetailService.getSkuNumberById(out.getRequestOrderId()));
        return out;
    }

    @Override
    public String export(BackQueryRequestOrderPageIn backQueryRequestOrderPageIn) {
        // 设置每次查询条数
        backQueryRequestOrderPageIn.setPageSize(10000);
        String title = "集货单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "集货单列表",
                        // 导出模板实体
                        ExcelRequestOrderOut.class,
                        // 分页查询对象
                        backQueryRequestOrderPageIn,
                        // 分页查询方法
                        page -> {
                            Page<BackRequestOrderOut> requestOrderOutPage = this.findRequestOrderListForPage(backQueryRequestOrderPageIn);
                            List<ExcelRequestOrderOut> excelRequestOrderOuts = parseDataToExcel(requestOrderOutPage.getList());
                            log.info("导出集货单列表集合大小是--{}", excelRequestOrderOuts.size());
                            return excelRequestOrderOuts;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    /**
     * 创建集货单
     *
     * @param orderCycle
     * @param legalOrderMap
     * @param itemCode
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDisDelivRequest createRequestOrder(OrdDisOrderCycle orderCycle, Map<OrdDisOrder,
            List<AppOrderDetailOut>> legalOrderMap, String itemCode, Map<String, BigDecimal> illegalSkuAmountMap) {
        OrdDisDelivRequest requestOrder = new OrdDisDelivRequest();
        requestOrder.setStoreCode(orderCycle.getStoreCode());
        requestOrder.setOrderCycleId(orderCycle.getId());
        requestOrder.setOrderTypeConfigId(orderCycle.getOrderTypeConfigId());
        requestOrder.setTruncationDateTime(orderCycle.getTruncationDateTime());
        String requestOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KJH.getCode(), orderCycle.getBizOrgCode(), uniqueUtils, 4);
        log.info("门店" + orderCycle.getStoreCode() + "订货周期" + orderCycle.getTruncationDateTime() + "创建集货单" + requestOrderNo);
        requestOrder.setRequestOrderNo(requestOrderNo);
        requestOrder.setStatusCode(RequestOrderStatusEnum.COLLECTED.getKey());
        requestOrder.setTotalAmount(BigDecimal.ZERO);
        requestOrder.setOrgCode(orderCycle.getOrgCode());
        requestOrder.setBizOrgCode(orderCycle.getBizOrgCode());
        requestOrder.setCreator(SystemConstant.SYSTEM_USER);
        requestOrder.setUpdater(SystemConstant.SYSTEM_USER);
        requestOrder.setIsDelete(ModelConst.DELETE.NO);
        this.saveRequestOrder(requestOrder);
        List<OrdDisDelivRequestDetail> requestOrderDetailList = null;
        // 累加
        if (OrderCycleProcessConfigItemCodeEnum.ACCUMULATION.getCode().equals(itemCode)) {
            requestOrderDetailList = this.accumulation(legalOrderMap, requestOrder);
        }
        // 覆盖 如果后期加盟需要覆盖和取大值则需要将illegalSkuAmountMap 传入下面两个方法中去累加或放入返款金额（2023-02-14）
        if (OrderCycleProcessConfigItemCodeEnum.COVER.getCode().equals(itemCode)) {
            requestOrderDetailList = this.cover(legalOrderMap, requestOrder);
        }
        // 取大值
        if (OrderCycleProcessConfigItemCodeEnum.TAKE_BIG_VALUE.getCode().equals(itemCode)) {
            requestOrderDetailList = this.takeBigValue(legalOrderMap, requestOrder);
        }
        requestOrder.setTotalAmount(requestOrder.getTotalAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.CEILING));
        List<OrdDisOrder> orderList = legalOrderMap.keySet().stream().collect(Collectors.toList());
        ordDisDelivRequestMapper.updateByPrimaryKeySelective(requestOrder);
        ordDisDelivRequestDetailService.batchSave(requestOrderDetailList);
        orderHandle.changeRequestOrder(orderList, OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), SystemConstant.SYSTEM_USER,
                FundReturnTypeEnum.DIS_REQUEST_ORDER_ILLEGAL_GOODS.getName(), illegalSkuAmountMap);
        orderHandle.batchUpdateOrderRequestRel(orderList, requestOrder.getId(), SystemConstant.SYSTEM_USER);
        return requestOrder;
    }

    /**
     * 保存集货单
     *
     * @param requestOrder
     */
    @Override
    public void saveRequestOrder(OrdDisDelivRequest requestOrder) {
        ordDisDelivRequestMapper.insertSelective(requestOrder);
    }

    /**
     * 根据集货单id查询集货单信息
     *
     * @param requestOrderId
     */
    @Override
    public OrdDisDelivRequest getRequestOrderByRequestOrderId(Long requestOrderId) {
        return ordDisDelivRequestMapper.selectByPrimaryKey(requestOrderId);
    }

    /**
     * 根据配销单id、业务组织代码查询集货单
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    @Override
    public OrdDisDelivRequest getRequestOrderByDeliveryOrderIdAndBizOrgCode(Long id, String bizOrgCode) {
        return ordDisDelivRequestMapper.getRequestOrderByDeliveryOrderIdAndBizOrgCode(id, bizOrgCode);
    }

    /**
     * 修改集货单状态
     *
     * @param id
     * @param requestStatusCode
     * @param systemUser
     * @param bizOrgCode
     */
    @Override
    public void updateRequestOrderStatus(Long id, String requestStatusCode, String systemUser, String bizOrgCode) {
        OrdDisDelivRequest ordDisDelivRequest = new OrdDisDelivRequest();
        ordDisDelivRequest.setId(id);
        ordDisDelivRequest.setStatusCode(requestStatusCode);
        ordDisDelivRequest.setUpdater(systemUser);
        ordDisDelivRequest.setBizOrgCode(bizOrgCode);
        ordDisDelivRequestMapper.updateByPrimaryKeySelective(ordDisDelivRequest);
    }

    @Override
    public List<String> findNotToDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode) {
        return ordDisDelivRequestMapper.findNotToDeliveryOrderList(createTimeBegin, createTimeEnd, RequestOrderStatusEnum.COLLECTED.getKey(), bizOrgCode);
    }

    @Override
    public BigDecimal requestOrderSummary(BackQueryRequestOrderPageIn backQueryRequestOrderPageIn) {
        // 当门店名称，门店区域等不为空时，先得到所有的门店代码集合
        if (StringUtils.isNotEmpty(backQueryRequestOrderPageIn.getStoreArea()) || StringUtils.isNotEmpty(backQueryRequestOrderPageIn.getStoreName())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(backQueryRequestOrderPageIn.getStoreArea(),
                    backQueryRequestOrderPageIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return BigDecimal.ZERO;
            }
            backQueryRequestOrderPageIn.setStoreCodeList(storeCodeList);
        }
        return ordDisDelivRequestMapper.requestOrderSummary(backQueryRequestOrderPageIn);
    }

    @Override
    public Long findOrderCycleIdByDeliveryOrderId(Long deliveryOrderId, String bizOrgCode) {
        return ordDisDelivRequestMapper.findOrderCycleIdByDeliveryOrderId(deliveryOrderId, bizOrgCode);
    }

    /**
     * 累加创建集货单明细，并且更新集货单总金额
     *
     * @param legalOrderMap
     * @param requestOrder
     * @return
     */
    private List<OrdDisDelivRequestDetail> accumulation(Map<OrdDisOrder, List<AppOrderDetailOut>> legalOrderMap, OrdDisDelivRequest requestOrder) {
        Map<String, OrdDisDelivRequestDetail> skuMap = new HashMap<>();
        Map<String, Map<String, OrdDisDelivRequestDetail>> giftMap = new HashMap<>();
        legalOrderMap.forEach((order, orderDetailList) -> orderDetailList.forEach(orderDetail -> {
            if (CollectionUtils.isNotEmpty(orderDetail.getGiftOutList())) {
                Map<String, OrdDisDelivRequestDetail> giftOrderDetails = giftMap.containsKey(orderDetail.getGoodsCode()) ? giftMap.get(orderDetail.getGoodsCode()) : new HashMap<>();
                orderDetail.getGiftOutList().forEach(gift -> {
                    OrdDisDelivRequestDetail giftDtl;
                    if (giftOrderDetails.containsKey(gift.getGoodsCode())) {
                        giftDtl = giftOrderDetails.get(gift.getGoodsCode());
                        giftDtl.setPackageQuantity(giftDtl.getPackageQuantity().add(gift.getPackageQuantity()));
                        giftDtl.setQuantity(giftDtl.getQuantity().add(gift.getQuantity()));
                    } else {
                        giftDtl = this.initOrdDisDelivRequestDetailByOrderDtl(gift, requestOrder.getId());
                    }
                    giftOrderDetails.put(gift.getGoodsCode(), giftDtl);
                });
                giftMap.put(orderDetail.getGoodsCode(), giftOrderDetails);
            }
            String key = orderDetail.getGoodsCode() + SystemConstant.SHORT_LINE + orderDetail.getIsGift();
            OrdDisDelivRequestDetail requestOrderDetail = skuMap.get(key);
            if (Objects.isNull(requestOrderDetail)) {
                requestOrderDetail = this.initOrdDisDelivRequestDetailByOrderDtl(orderDetail, requestOrder.getId());
            } else {
                requestOrderDetail.setPackageQuantity(requestOrderDetail.getPackageQuantity().add(orderDetail.getPackageQuantity()));
                requestOrderDetail.setQuantity(requestOrderDetail.getQuantity().add(orderDetail.getQuantity()));
            }
            requestOrderDetail.setRequestOrderAmount(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity()));
            requestOrder.setTotalAmount(requestOrder.getTotalAmount().add(requestOrderDetail.getOriginalUnitPrice().multiply(orderDetail.getQuantity())));
            skuMap.put(key, requestOrderDetail);
        }));
        if (giftMap.size() > 0) {
            giftMap.forEach((key, orderDtlGiftMap) -> orderDtlGiftMap.forEach((giftGoodsCode, value) -> {
                String giftKey = giftGoodsCode + SystemConstant.SHORT_LINE + value.getBaseGoodsCode() + SystemConstant.SHORT_LINE + value.getIsGift();
                skuMap.put(giftKey, value);
            }));
        }
        return skuMap.values().stream().collect(Collectors.toList());
    }

    /**
     * 覆盖创建集货单明细，并且更新集货单总金额
     *
     * @param legalOrderMap
     * @param requestOrder
     * @return
     */
    private List<OrdDisDelivRequestDetail> cover(Map<OrdDisOrder, List<AppOrderDetailOut>> legalOrderMap, OrdDisDelivRequest requestOrder) {
//        Map<String, List<OrdDisDelivRequestDetail>> skuMap = new TreeMap<>();
//        legalOrderMap.entrySet().forEach(legalEntry -> {
//            List<AppOrderDetailOut> orderDetailList = legalEntry.getValue();
//            orderDetailList.stream().forEach(orderDetail -> {
//                String key = orderDetail.getGoodsCode() + SystemConstant.SHORT_LINE + orderDetail.getIsGift();
//                List<OrdDisDelivRequestDetail> requestOrderDetailList = skuMap.get(key);
//                if (CollectionUtils.isEmpty(requestOrderDetailList)) {
//                    requestOrderDetailList = Lists.newArrayList();
//                }
//                OrdDisDelivRequestDetail requestOrderDetail = new OrdDisDelivRequestDetail();
//                BeanUtils.copy(orderDetail, requestOrderDetail);
//                requestOrderDetail.setRequestOrderId(requestOrder.getId());
//                requestOrderDetail.setStockCode(orderDetail.getPosition());
//                requestOrderDetail.setOriginalUnitPrice(orderDetail.getOrderUnitPrice());
//                requestOrderDetail.setRequestOrderAmount(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity()));
//                requestOrderDetailList.add(requestOrderDetail);
//                skuMap.put(key, requestOrderDetailList);
//            });
//        });
//        List<OrdDisDelivRequestDetail> requestOrderDetailList = Lists.newArrayList();
//        skuMap.entrySet().stream().forEach(entry -> {
//            OrdDisDelivRequestDetail requestOrderDetail = entry.getValue().stream().max(Comparator.comparing(OrdDisDelivRequestDetail::getCreateTime)).get();
//            requestOrderDetailList.add(requestOrderDetail);
//            requestOrder.setTotalAmount(requestOrder.getTotalAmount().add(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity())));
//        });
        Map<String, AppOrderDetailOut> skuMap = new HashMap<>();
        legalOrderMap.entrySet().forEach(legalEntry -> {
            List<AppOrderDetailOut> orderDetailList = legalEntry.getValue();
            orderDetailList.forEach(orderDetail -> {
                String key = orderDetail.getGoodsCode();
                AppOrderDetailOut original = skuMap.get(key);
                if (Objects.nonNull(original)) {
                    skuMap.put(key, orderDetail.getCreateTime().isAfter(original.getCreateTime()) ? orderDetail : original);
                } else {
                    skuMap.put(key, orderDetail);
                }
            });
        });
        return this.initDetailListAndFigureUpOrderAmount(skuMap, requestOrder);
    }

    /**
     * 将订货单明细对象转为集货单明细对象
     *
     * @param orderDetail
     * @param requestOrderId
     * @return
     */
    private OrdDisDelivRequestDetail initOrdDisDelivRequestDetailByOrderDtl(OrdDisOrderDetail orderDetail, Long requestOrderId) {
        OrdDisDelivRequestDetail requestOrderDetail = new OrdDisDelivRequestDetail();
        BeanUtils.copy(orderDetail, requestOrderDetail);
        requestOrderDetail.setRequestOrderId(requestOrderId);
        requestOrderDetail.setStockCode(orderDetail.getPosition());
        requestOrderDetail.setOriginalUnitPrice(orderDetail.getOrderUnitPrice());
        requestOrderDetail.setRequestOrderAmount(orderDetail.getOrderUnitPrice().multiply(orderDetail.getQuantity()));
        return requestOrderDetail;
    }

    /**
     * 封装集货单明细集合并计算集货单总金额
     *
     * @param skuMap
     * @param requestOrder
     * @return
     */
    private List<OrdDisDelivRequestDetail> initDetailListAndFigureUpOrderAmount(Map<String, AppOrderDetailOut> skuMap, OrdDisDelivRequest requestOrder) {
        List<OrdDisDelivRequestDetail> requestOrderDetailList = Lists.newArrayList();
        skuMap.forEach((key, orderDetail) -> {
            OrdDisDelivRequestDetail requestOrderDetail = initOrdDisDelivRequestDetailByOrderDtl(orderDetail, requestOrder.getId());
            requestOrderDetail.setRequestOrderAmount(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity()));
            requestOrderDetailList.add(requestOrderDetail);
            if (CollectionUtils.isNotEmpty(orderDetail.getGiftOutList())) {
                orderDetail.getGiftOutList().forEach(originalGift -> {
                    OrdDisDelivRequestDetail gift = initOrdDisDelivRequestDetailByOrderDtl(originalGift, requestOrder.getId());
                    requestOrderDetailList.add(gift);
                });
            }
            requestOrder.setTotalAmount(requestOrder.getTotalAmount().add(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity())));
        });
        return requestOrderDetailList;
    }


    /**
     * 取大值
     *
     * @param legalOrderMap
     * @param requestOrder
     * @return
     */
    private List<OrdDisDelivRequestDetail> takeBigValue(Map<OrdDisOrder, List<AppOrderDetailOut>> legalOrderMap, OrdDisDelivRequest requestOrder) {
//        Map<String, OrdDisDelivRequestDetail> skuMap = new TreeMap<>();
//        Map<String, List<OrdDisOrderDetail>> baseOrderDetailsMap = new LinkedHashMap<>();
//        Map<Long, List<OrdDisOrderDetail>> giftOrderDetailMap = new HashMap<>(NumberUtil.INTEGER_TWO);
//        legalOrderMap.entrySet().forEach(legalEntry -> {
//            List<AppOrderDetailOut> orderDetailList = legalEntry.getValue();
//            orderDetailList.stream().forEach(orderDetail -> {
//                if (null != orderDetail.getIsGift() && orderDetail.getIsGift() == 1) {
//                    List<OrdDisOrderDetail> giftOrderDetailList = giftOrderDetailMap.get(orderDetail.getId());
//                    if (CollectionUtils.isEmpty(giftOrderDetailList)) {
//                        giftOrderDetailList = Lists.newArrayList();
//                    }
//                    giftOrderDetailList.add(orderDetail);
//                    giftOrderDetailMap.put(orderDetail.getId(), giftOrderDetailList);
//                }
//                if (null != orderDetail.getIsGift() && orderDetail.getIsGift() == 0) {
//                    List<OrdDisOrderDetail> baseOrderDetailList = baseOrderDetailsMap.get(orderDetail.getGoodsCode());
//                    if (CollectionUtils.isEmpty(baseOrderDetailList)) {
//                        baseOrderDetailList = Lists.newArrayList();
//                    }
//                    baseOrderDetailList.add(orderDetail);
//                    baseOrderDetailsMap.put(orderDetail.getGoodsCode(), baseOrderDetailList);
//                }
//            });
//        });
//        baseOrderDetailsMap.entrySet().forEach(entry -> {
//            entry.getValue().stream().sorted();
//            OrdDisOrderDetail baseOrderDetail = entry.getValue().stream().sorted(Comparator.comparing(OrdDisOrderDetail::getQuantity).reversed()).collect(Collectors.toList()).get(0);
//            OrdDisDelivRequestDetail requestOrderDetail = new OrdDisDelivRequestDetail();
//            BeanUtils.copy(baseOrderDetail, requestOrderDetail);
//            requestOrderDetail.setRequestOrderId(requestOrder.getId());
//            requestOrderDetail.setStockCode(baseOrderDetail.getPosition());
//            requestOrderDetail.setOriginalUnitPrice(baseOrderDetail.getOrderUnitPrice());
//            requestOrderDetail.setRequestOrderAmount(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity()));
//            skuMap.put(baseOrderDetail.getGoodsCode() + "-" + baseOrderDetail.getIsGift(), requestOrderDetail);
//            requestOrder.setTotalAmount(requestOrder.getTotalAmount().add(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity())));
//            // 查找是否存在赠品
//            List<OrdDisOrderDetail> giftOrderDetailList = giftOrderDetailMap.get(baseOrderDetail.getId());
//            if (CollectionUtils.isNotEmpty(giftOrderDetailList)) {
//                giftOrderDetailList.forEach(giftOrderDetail -> {
//                    OrdDisDelivRequestDetail giftRequestOrderDetail = new OrdDisDelivRequestDetail();
//                    BeanUtils.copy(giftOrderDetail, giftRequestOrderDetail);
//                    giftRequestOrderDetail.setRequestOrderId(requestOrder.getId());
//                    giftRequestOrderDetail.setRequestOrderAmount(giftRequestOrderDetail.getOriginalUnitPrice().multiply(giftRequestOrderDetail.getQuantity()));
//                    skuMap.put(giftOrderDetail.getGoodsCode() + "-" + giftOrderDetail.getIsGift(), giftRequestOrderDetail);
//                    requestOrder.setTotalAmount(requestOrder.getTotalAmount().add(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity())));
//                });
//            }
//        });
//        List<OrdDisDelivRequestDetail> requestOrderDetailList = skuMap.values().stream().collect(Collectors.toList());

        Map<String, AppOrderDetailOut> skuMap = new HashMap<>();
        legalOrderMap.entrySet().forEach(legalEntry -> {
            List<AppOrderDetailOut> orderDetailList = legalEntry.getValue();
            orderDetailList.forEach(orderDetail -> {
                String key = orderDetail.getGoodsCode();
                AppOrderDetailOut original = skuMap.get(key);
                if (Objects.nonNull(original)) {
                    skuMap.put(key, orderDetail.getQuantity().compareTo(original.getQuantity()) > 0 ? orderDetail : original);
                } else {
                    skuMap.put(key, orderDetail);
                }
            });
        });
        return this.initDetailListAndFigureUpOrderAmount(skuMap, requestOrder);
    }

    /**
     * 将查询得到的列表集合转换为导出集合
     *
     * @param list
     * @return List<ExcelRequestOrderOut>
     */
    private List<ExcelRequestOrderOut> parseDataToExcel(List<BackRequestOrderOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 将backRequestOrderOut转换为导出ExcelRequestOrderOut
     *
     * @param backRequestOrderOut
     * @return ExcelRequestOrderOut
     */
    private ExcelRequestOrderOut convertExcel(BackRequestOrderOut backRequestOrderOut, int index) {
        ExcelRequestOrderOut excelRequestOrderOut = new ExcelRequestOrderOut();
        BeanUtils.copy(backRequestOrderOut, excelRequestOrderOut);
        excelRequestOrderOut.setIndex(index + 1);
        excelRequestOrderOut.setShortOrderType(backRequestOrderOut.getOrderTypeName() + "[" + backRequestOrderOut.getOrderTypeCode() + "]");
        return excelRequestOrderOut;
    }

    private void setNosAndCode(BackRequestOrderOut item) {
        String deliveryOrderNos = ordDisDelivRequestMapper.getDeliveryOrderNosById(item.getRequestOrderId(), item.getBizOrgCode());
        String orderNos = ordDisDelivRequestMapper.getOrderNosById(item.getRequestOrderId(), item.getBizOrgCode());
        Integer goodsItemNumber = ordDisDelivRequestDetailService.getGoodsItemNumber(item.getRequestOrderId());
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
}
