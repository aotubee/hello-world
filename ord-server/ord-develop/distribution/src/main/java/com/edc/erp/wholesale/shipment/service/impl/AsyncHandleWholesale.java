package com.edc.erp.wholesale.shipment.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.OperateLogTypeEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrderNoPreEnum;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.model.in.customer.QueryClientDistInfoIn;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.ClientDistInfoService;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.wholesale.enumeration.PushPurProgressEnum;
import com.edc.erp.wholesale.model.excel.shipment.ExportErrorWholesaleShipmentInfo;
import com.edc.erp.wholesale.model.excel.shipment.ImportShipmentOrder;
import com.edc.erp.wholesale.model.in.shipment.ShipmentWithDetailIn;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * description 批发单异步执行任务
 *
 * @author gusiyuan
 * @since 2023/10/16 09:41
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncHandleWholesale {

    private final WholesaleShipmentService wholesaleShipmentService;
    private final AsyncExportHandle asyncExportHandle;
    private final AsyncLogService asyncLogService;
    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;
    private final StockServer stockServer;
    private final ClientDistInfoService clientDistInfoService;
    private final UniqueUtils uniqueUtils;
    private final RedisService redisService;

    @Async
    @Transactional(rollbackFor = Exception.class)
    public void asyncCheckAndSaveShipment(String bizOrgCode, String userName, List<ImportShipmentOrder> importShipmentOrders, String key) {
        try {
            List<ExportErrorWholesaleShipmentInfo> errorList = new ArrayList<>();
            List<ShipmentWithDetailIn> successList = new ArrayList<>();
            // 校验与初始化数据
            this.checkAndInit(bizOrgCode, userName, importShipmentOrders, errorList, successList);
            // 入库
            if (CollectionUtils.isNotEmpty(successList)) {
                this.batchSaveOrderAndDetail(successList);
            }
            // 错误列表若不为空则发送小铃铛
            if (CollectionUtils.isNotEmpty(errorList)) {
                String fileName = "批发出货单导入错误信息";
                String sheetName = fileName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
                asyncExportHandle.asyncExport(fileName, sheetName, userName, errorList, ExportErrorWholesaleShipmentInfo.class);
            }
        } catch (Exception e) {
            log.error("批发出货单列表导入异常", e);
            ExportErrorWholesaleShipmentInfo errorResult = new ExportErrorWholesaleShipmentInfo();
            errorResult.setErrMsg("批发出货单列表导入异常，请检查商品配置");
            String sheetName = "批发出货单列表导入问题清单-导入异常";
            String fileName = sheetName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
            asyncExportHandle.asyncExport(fileName, sheetName, userName, Collections.singletonList(errorResult), ExportErrorWholesaleShipmentInfo.class);
        } finally {
            redisService.del(key);
        }
    }

    private void batchSaveOrderAndDetail(List<ShipmentWithDetailIn> successList) {
        for (ShipmentWithDetailIn shipmentWithDetailIn : successList) {
            WholesaleShipment wholesaleShipment = shipmentWithDetailIn.getWholesaleShipment();
            // 保存单据
            wholesaleShipmentService.insertSelective(wholesaleShipment);
            if (wholesaleShipment.getId() == null) {
                throw new BusinessException("批发出客户：" + wholesaleShipment.getClientCode() + "导入单据保存错误！");
            }
            //保存批发出货单日志
            BusinessLog businessLog = new BusinessLog(
                    SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                    String.valueOf(wholesaleShipment.getId()),
                    OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                    OperateLogTypeEnum.SAVE.getName(),
                    new Date(),
                    wholesaleShipment.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            AtomicReference<Integer> line = new AtomicReference<>(NumberUtil.INTEGER_ONE);
            List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList = shipmentWithDetailIn.getWholesaleShipmentDetailList().stream().map(item -> {
                item.setWholesaleShipmentId(wholesaleShipment.getId());
                //添加创建人
                item.setCreator(wholesaleShipment.getCreator());
                //添加创建时间
                item.setCreateTime(LocalDateTime.now());
                //添加更新者
                item.setUpdater(wholesaleShipment.getUpdater());
                //添加更新时间
                item.setUpdateTime(LocalDateTime.now());
                item.setLine(line.get());
                line.getAndSet(line.get() + NumberUtil.INTEGER_ONE);
                return item;
            }).collect(Collectors.toList());
            // 保存明细
            wholesaleShipmentDetailService.batchSave(wholesaleShipmentDetailList);
        }
    }

    private void checkAndInit(String bizOrgCode, String userName, List<ImportShipmentOrder> importShipmentOrders,
                              List<ExportErrorWholesaleShipmentInfo> errorList, List<ShipmentWithDetailIn> successList) {
        Map<String, StockInfoOut> stockInfoMap = stockServer.findAll(bizOrgCode);
        // 拆分单据
        Map<String, List<ImportShipmentOrder>> orderMap = importShipmentOrders.stream()
                .collect(Collectors.groupingBy(item -> buildSpiltOrderKey(item.getClientCode(), item.getShipmentStockCode(), item.getSourceNo())));

        for (Map.Entry<String, List<ImportShipmentOrder>> order : orderMap.entrySet()) {
            List<ImportShipmentOrder> shipmentOrderList = order.getValue();
            boolean isErr = true;
            // 单据循环
            ShipmentWithDetailIn shipmentWithDetailIn = new ShipmentWithDetailIn();
            WholesaleShipment wholesaleShipment = new WholesaleShipment();
            wholesaleShipment.setShipmentStatus(ShipmentStatusEnum.PENDING.getCode());
            List<WholesaleShipmentDetailIn> detailList = new ArrayList<>();
            // 明细循环，初始化并校验数据正确性
            for (ImportShipmentOrder importShipmentOrder : shipmentOrderList) {
                // 错误信息
                ExportErrorWholesaleShipmentInfo errorInfo = new ExportErrorWholesaleShipmentInfo();
                BeanUtils.copy(importShipmentOrder, errorInfo);
                // 1. 校验
                // 客户校验
                List<ClientDistInfoOut> clientDistInfos = clientDistInfoService.findClientDistInfo(new QueryClientDistInfoIn(importShipmentOrder.getClientCode(), bizOrgCode));
                if (CollectionUtils.isEmpty(clientDistInfos)) {
                    errorInfo.setErrMsg("客户不存在;");
                    errorList.add(errorInfo);
                    isErr = false;
                    break;
                }
                // 仓位校验
                StockInfoOut stockInfoOut = stockInfoMap.get(importShipmentOrder.getShipmentStockCode());
                if (stockInfoOut == null) {
                    errorInfo.setErrMsg("仓位不存在;");
                    errorList.add(errorInfo);
                    isErr = false;
                    break;
                }
                // 获取商品信息
                WholesaleShipmentDetailIn detail = new WholesaleShipmentDetailIn();
                BeanUtils.copy(importShipmentOrder, detail);
                detail.setStockId(stockInfoOut.getId());
                detail.setStockCode(importShipmentOrder.getShipmentStockCode());
                detail.setShipmentWrh(stockInfoOut.getWarehouseCode());
                SaleGoodsInfoOut saleGoodsInfo;
                try {
                    ClientDistInfoOut clientDistInfoOut = clientDistInfos.get(NumberUtil.INTEGER_ZERO);
                    saleGoodsInfo = wholesaleShipmentService.getSaleGoodsInfo(detail, bizOrgCode, clientDistInfoOut.getPriceGroupCode());
                } catch (BusinessException e) {
                    errorInfo.setErrMsg(e.getMessage());
                    errorList.add(errorInfo);
                    isErr = false;
                    break;
                }
                // 2.赋值
                // 客户可改价并且导入了单价则取导入的，否则取批发价格组的单价
                boolean isCanEditPrice = clientDistInfoService.getIsCanEditPrice(importShipmentOrder.getClientCode(), bizOrgCode);
                if (isCanEditPrice && Objects.nonNull(importShipmentOrder.getUnitPrice())) {
                    detail.setUnitPrice(importShipmentOrder.getUnitPrice());
                } else {
                    detail.setUnitPrice(saleGoodsInfo.getSalePrice());
                }
                detail.setApplyAmount(detail.getUnitPrice().multiply(new BigDecimal(detail.getApplyQuantity())));
                detail.setAuditQuantity(importShipmentOrder.getApplyQuantity());
                // 明细审核金额
                detail.setAuditAmount(detail.getUnitPrice().multiply(new BigDecimal(detail.getApplyQuantity())));
                detail.setInTax(saleGoodsInfo.getInTax());
                detail.setOutTax(saleGoodsInfo.getOutTax());
                //计算包装数
                String applyPackageNum;
                if (Objects.isNull(saleGoodsInfo.getQpc())) {
                    applyPackageNum = NumberUtil.INTEGER_ZERO.toString();
                } else {
                    if (detail.getApplyQuantity() % saleGoodsInfo.getQpc() != NumberUtil.INTEGER_ZERO) {
                        applyPackageNum = detail.getApplyQuantity() / saleGoodsInfo.getQpc() + "+" + detail.getApplyQuantity() % saleGoodsInfo.getQpc();
                    } else {
                        applyPackageNum = String.valueOf(detail.getApplyQuantity() / saleGoodsInfo.getQpc());
                    }
                }
                detail.setApplyPackageNum(applyPackageNum);
                detail.setGoodsCode(saleGoodsInfo.getGoodsCode());
                detail.setGoodsName(saleGoodsInfo.getGoodsName());
                detail.setGoodsType(saleGoodsInfo.getGoodsType());
                detail.setBarCode(saleGoodsInfo.getBarCode());
                detail.setPackageSpecification(saleGoodsInfo.getPackageSpecification());
                detail.setPackageUnit(saleGoodsInfo.getPackageUnit());
                detail.setInventoryPrice(saleGoodsInfo.getInventoryPrice());
                // 退货原则
                detail.setReturnPrinciple(saleGoodsInfo.getReturnPrinciple());
                // 是否赠品
                detail.setIsGift(NumberUtil.INTEGER_ZERO);
                if(DistributionWaysEnum.TRANSFER.getType().equals(importShipmentOrder.getDistributionType()) && StringUtils.isBlank(saleGoodsInfo.getVendorCode())){
                    errorInfo.setErrMsg("订单方不存在;");
                    errorList.add(errorInfo);
                    isErr = false;
                    break;
                }
                // 订单方
                detail.setVendorCode(saleGoodsInfo.getVendorCode());
                // 有票
                detail.setInvoiceType(saleGoodsInfo.getInvoiceType());
                // 出货单主表信息
                wholesaleShipment.setClientCode(importShipmentOrder.getClientCode());
                wholesaleShipment.setShipmentWrh(stockInfoOut.getWarehouseCode());
                wholesaleShipment.setShipmentStockCode(importShipmentOrder.getShipmentStockCode());
                wholesaleShipment.setDistributionType(importShipmentOrder.getDistributionType());
                // 客户配送信息
                ClientDistInfoOut clientDistInfoOut = clientDistInfos.get(0);
                wholesaleShipment.setConsignee(clientDistInfoOut.getConsignee());
                wholesaleShipment.setConsigneePhone(clientDistInfoOut.getConsigneePhone());
                wholesaleShipment.setAddressDetail(clientDistInfoOut.getAddressDetail());
                wholesaleShipment.setPriceGroupCode(clientDistInfoOut.getPriceGroupCode());
                wholesaleShipment.setDistributionInfoId(clientDistInfoOut.getId().intValue());
                wholesaleShipment.setSourceNo(importShipmentOrder.getSourceNo());
                wholesaleShipment.setOrderPriority(clientDistInfoOut.getOrderPriority());
                wholesaleShipment.setPushPurProgress(PushPurProgressEnum.PENDING.getProgress());
                detailList.add(detail);
            }
            if (!isErr) {
                // 若有明细行出错，则整单移除不生成
                Predicate<ShipmentWithDetailIn> condition = obj ->
                        obj.getWholesaleShipment().getClientCode().equals(wholesaleShipment.getClientCode())
                                && obj.getWholesaleShipment().getShipmentStockCode().equals(wholesaleShipment.getShipmentStockCode())
                                && obj.getWholesaleShipment().getSourceNo().equals(wholesaleShipment.getSourceNo());
                // 使用 removeIf 方法来删除符合条件的对象
                successList.removeIf(condition);
                continue;
            }
            wholesaleShipment.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
            wholesaleShipment.setIsReversal(NumberUtil.INTEGER_ZERO);
            wholesaleShipment.setBizOrgCode(bizOrgCode);
            wholesaleShipment.setCreator(userName);
            wholesaleShipment.setCreateTime(LocalDateTime.now());
            wholesaleShipment.setUpdater(userName);
            wholesaleShipment.setUpdateTime(LocalDateTime.now());
            //统计当前所有明细集合的申请数量并求和
            int applicationQuantity = detailList.stream().mapToInt(WholesaleShipmentDetailIn::getApplyQuantity).sum();
            //申请数量 * 单价并求和
            BigDecimal amount = detailList.stream().map(x -> x.getUnitPrice().multiply(new BigDecimal(x.getApplyQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
            //添加申请数量
            wholesaleShipment.setApplicationQuantity(applicationQuantity);
            //添加审核数量
            wholesaleShipment.setAuditQuantity(applicationQuantity);
            //添加审请金额
            wholesaleShipment.setApplicationAmount(amount);
            //添加审核金额
            wholesaleShipment.setAuditAmount(amount);
            // 单号
            wholesaleShipment.setShipmentNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KFC.getCode(), wholesaleShipment.getBizOrgCode(), uniqueUtils, 4));
            shipmentWithDetailIn.setWholesaleShipment(wholesaleShipment);
            shipmentWithDetailIn.setWholesaleShipmentDetailList(detailList);

            successList.add(shipmentWithDetailIn);
        }
    }

    private String buildSpiltOrderKey(String clientCode, String stockCode, String sourceNo) {
        String preKey = clientCode + "$" + stockCode;
        if (StringUtils.isNotEmpty(sourceNo)) {
            preKey += "$" + sourceNo;
        }
        return preKey;
    }

}
