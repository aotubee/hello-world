package com.edc.erp.wholesale.shipment.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.handel.SyncOrdDisOrderHandle;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.customer.QueryClientDistInfoIn;
import com.edc.erp.common.model.in.fund.*;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.fund.ForeignAccountFundOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.model.out.goods.StandardSpecOut;
import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.common.model.out.stock.StockWarehouseOut;
import com.edc.erp.common.rpc.SaleGoodsInfoClient;
import com.edc.erp.common.service.*;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdifferenceorder.mapper.OrdDisDelivDifferenceMapper;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.erp.enumeration.ShipmentInvAdjustStatusEnum;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.enumeration.WholesaleOrderTypeEnum;
import com.edc.erp.model.in.QueryWarehouseIn;
import com.edc.erp.model.out.StockInfoOut;
import com.edc.erp.model.out.WarehouseInfoOut;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.wholesale.enumeration.PushPurProgressEnum;
import com.edc.erp.wholesale.model.excel.shipment.*;
import com.edc.erp.wholesale.model.in.PushPurUpdateIn;
import com.edc.erp.wholesale.model.in.shipment.*;
import com.edc.erp.wholesale.model.listener.shipment.ImportShipmentOrderDtlListener;
import com.edc.erp.wholesale.model.listener.shipment.ImportShipmentOrderListener;
import com.edc.erp.wholesale.model.out.shipment.QueryShipmentReportOut;
import com.edc.erp.wholesale.model.out.shipment.ShipmentWithDetailOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleDateInfoOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleShipmentDetailOut;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.erp.wholesale.shipment.mapper.WholesaleShipmentMapper;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.export.AsyncExportExecutor;
import com.edc.plugins.export.ExportExcelByPage;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.enumeration.DtsOrderPatternTypeEnum;
import com.edc.sdk.dts.model.order.in.WholesaleBillDtlIn;
import com.edc.sdk.dts.model.order.in.WholesaleBillIn;
import com.edc.sdk.dts.model.order.vo.WholesaleBillDtlVO;
import com.edc.sdk.dts.model.order.vo.WholesaleBillVO;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 批发出货单(WholesaleShipment)表服务实现类
 *
 * @author lx
 * @since 2022-10-18 11:59:38
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WholesaleShipmentServiceImpl extends BaseServiceImpl<WholesaleShipment> implements WholesaleShipmentService {

    private static final String CHECK_WHOLESALE_SHIPMENT_AUDIT = "CHECK_WHOLESALE_SHIPMENT_AUDIT:";

    private final WholesaleShipmentMapper wholesaleShipmentMapper;

    private final ClientDistInfoService clientDistInfoService;

    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;

    private final AsyncLogService asyncLogService;

    private final UniqueUtils uniqueUtils;

    private final SyncOrdDisOrderHandle syncOrdDisOrderHandle;

    private final FileService fileService;

    private final StockServer stockServer;

    private final AsyncPushTaskService asyncPushTaskService;

    private final WholesaleService wholesaleService;

    private final OrdDisDelivDifferenceMapper ordDisDelivDifferenceMapper;

    private final FundServer fundServer;

    private final OrderGoodsServer orderGoodsServer;

    private final SaleGoodsInfoClient saleGoodsInfoClient;

    private final StockFlowService stockFlowService;

    private final AsyncExportExecutor asyncExportExecutor;

    private final RedisService redisService;

    private final AsyncFundService asyncFundService;

    private final WarehouseServer warehouseServer;

    @Autowired
    @Qualifier("hsWholesaleDiffSender")
    private MessageSender hsWholesaleDiffSender;

    @Autowired
    @Qualifier("hsWholesaleShipmentDtsBackSender")
    private MessageSender hsWholesaleShipmentDtsBackSender;

    private final int batchSize = 8000;

    @Qualifier("wholesaleShipmentToDtsSender")
    private final MessageSender wholesaleShipmentToDtsSender;

    @Qualifier("zKWholesaleShipmentBackSender")
    private final MessageSender zKWholesaleShipmentBackSender;

    /**
     * 分页查询批发出货单
     *
     * @param queryShipmentIn 批发出货单 查询入参类
     * @return
     */
    @Override
    public Page<ShipmentWithDetailOut> findShipmentByPage(QueryShipmentIn queryShipmentIn) {
        //出货单结果集
        List<ShipmentWithDetailOut> wholesaleShipmentOuts = wholesaleShipmentMapper.findShipmentByPage(queryShipmentIn);
        //中文转换
        wholesaleShipmentOuts.forEach(item -> {
            this.addClientDistInfoAndShipmentConvert(item, null);
            if (Objects.nonNull(item.getAuditTime())) {
                item.setAuditTimeStr(DateUtils.format(item.getAuditTime()));
            }
        });
        //分页对象
        Page<ShipmentWithDetailOut> wholesaleShipmentPage = new Page<>(queryShipmentIn);
        wholesaleShipmentPage.setList(wholesaleShipmentOuts);
        return wholesaleShipmentPage;
    }


    /**
     * 查询允许批发的仓位信息
     *
     * @param queryWarehouseIn 仓位信息入参
     * @return
     */
    @Override
    public List<StockInfoOut> findStockInfo(QueryWarehouseIn queryWarehouseIn) {
        //获取批发单类型
        String wholesaleOrderType = queryWarehouseIn.getWholesaleOrderType();
        if (WholesaleOrderTypeEnum.OUT.getCode().equals(wholesaleOrderType)) {
            //允许批发出货条件
            queryWarehouseIn.setIsOutReturn(IsOutReturnEnum.WHOLESALE_BIZ.getCode() + SystemConstant.WAIT + ModelConst.ENABLE.YES);
        } else if (WholesaleOrderTypeEnum.RETURNS.getCode().equals(wholesaleOrderType)) {
            //允许批发退货条件
            queryWarehouseIn.setIsOutReturn(IsOutReturnEnum.FRANCHISEE_BIZ.getCode() + SystemConstant.WAIT + ModelConst.ENABLE.YES);
        } else {
            //如果不是出货或退货 则查询返回空
            return Collections.emptyList();
        }

        return wholesaleShipmentMapper.findStockInfo(queryWarehouseIn);
    }


    /**
     * 根据允许批发的仓位信息的仓储id查询仓储信息
     *
     * @param bizOrgCode         业务组织code
     * @param wholesaleOrderType 批发单类型(out：出货，returns：退货)
     * @return
     */
    @Override
    public List<WarehouseInfoOut> findWarehouseInfo(String bizOrgCode, String wholesaleOrderType) {
        //根据允许批发仓位信息的仓储id查询仓储信息
        List<StockInfoOut> stockInfoList = this.findStockInfoOut(bizOrgCode, wholesaleOrderType);
        //如果无仓位信息 则返回空
        if (CollectionUtils.isEmpty(stockInfoList)) {
            return Collections.emptyList();
        }
        //查询仓储信息
        return wholesaleShipmentMapper.findWarehouseInfo(stockInfoList);
    }


    /**
     * 查询仓储与仓位信息(二级联动)
     *
     * @param bizOrgCode         业务组织code
     * @param wholesaleOrderType 批发单类型(out：出货，returns：退货)
     * @return
     */
    @Override
    public List<WarehouseInfoOut> findWarehouseStockInfo(String bizOrgCode, String wholesaleOrderType) {
        //根据允许批发仓位信息的仓储id查询仓储信息
        List<StockInfoOut> dbStockInfos = this.findStockInfoOut(bizOrgCode, wholesaleOrderType);
        //如果无仓位信息 则返回空
        if (CollectionUtils.isEmpty(dbStockInfos)) {
            return Collections.emptyList();
        }
        //查询仓储信息
        List<WarehouseInfoOut> warehouseInfo = wholesaleShipmentMapper.findWarehouseInfo(dbStockInfos);
        //利用仓位信息中的仓储id分组并转map
        Map<Long, List<StockInfoOut>> stockInfoMap = dbStockInfos.stream().collect(Collectors.groupingBy(StockInfoOut::getWarehouseId));
        //将仓位信息添加至对应的仓储信息内
        warehouseInfo.forEach(item -> {
            //根据仓储id获取对应的仓位信息
            List<StockInfoOut> stockInfoOuts = stockInfoMap.get(item.getId());
            item.setStockInfoOuts(stockInfoOuts);
        });
        return warehouseInfo;
    }


    /**
     * 保存或修改批发出货单
     *
     * @param shipmentWithDetailIn 批发出货单和明细新增入参类
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShipmentWithDetailOut saveUpdate(ShipmentWithDetailIn shipmentWithDetailIn) {
        //规则校验
        this.verifyDetailIsExist(shipmentWithDetailIn, null);
        //添加出货单待审核状态
        shipmentWithDetailIn.getWholesaleShipment().setShipmentStatus(ShipmentStatusEnum.PENDING.getCode());
        shipmentWithDetailIn.getWholesaleShipment().setPushPurProgress(PushPurProgressEnum.PENDING.getProgress());
        //判断出货单id是否存在 存在则为更新，否则为新增
        boolean idIsNull = Objects.isNull(shipmentWithDetailIn.getWholesaleShipment().getId());
        //保存出货单
        ShipmentWithDetailOut shipmentWithDetailOut = this.saveOrUpdateShipment(shipmentWithDetailIn);
        //保存批发出货单日志
        BusinessLog businessLog = new BusinessLog(
                SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                String.valueOf(shipmentWithDetailIn.getWholesaleShipment().getId()),
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                idIsNull ? OperateLogTypeEnum.SAVE.getName() : OperateLogTypeEnum.UPDATE.getName(),
                new Date(),
                shipmentWithDetailIn.getWholesaleShipment().getUpdater());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return shipmentWithDetailOut;
    }

    /**
     * 出货单审核
     *
     * @param shipmentWithDetailIn 批发出货单和明细新增入参类
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShipmentWithDetailOut audit(ShipmentWithDetailIn shipmentWithDetailIn) {
        if (StringUtils.isNotBlank(shipmentWithDetailIn.getWholesaleShipment().getBizOrgCode()) &&
                !UserUtil.getBizOrgCode().equals(shipmentWithDetailIn.getWholesaleShipment().getBizOrgCode())) {
            throw new BusinessException("此出货单无审核权限");
        } else {
            //添加公司代码和业务组织代码
            shipmentWithDetailIn.getWholesaleShipment().setOrgCode(UserUtil.getOrgCode());
            shipmentWithDetailIn.getWholesaleShipment().setBizOrgCode(UserUtil.getBizOrgCode());
        }

        if (CollectionUtils.isEmpty(shipmentWithDetailIn.getWholesaleShipmentDetailList())) {
            throw new BusinessException("请至少录入一条出货单明细!");
        }
        String distributionType = shipmentWithDetailIn.getWholesaleShipment().getDistributionType();
        shipmentWithDetailIn.getWholesaleShipment().setDistributionType(DistributionWaysEnum.getTypeByName(distributionType));
        //获取出货单id
        Long wholesaleShipmentId = shipmentWithDetailIn.getWholesaleShipment().getId();
        if (Objects.isNull(wholesaleShipmentId)) {
            throw new BusinessException("出货单ID不能为空");
        }
        //查询出货单信息
        WholesaleShipment dbShipment = this.getShipmentById(wholesaleShipmentId, shipmentWithDetailIn.getWholesaleShipment().getBizOrgCode());
        String key = CHECK_WHOLESALE_SHIPMENT_AUDIT + dbShipment.getBizOrgCode() +
                SystemConstant.COLON + dbShipment.getClientCode() + SystemConstant.WAIT + dbShipment.getShipmentNo();
        if (!redisService.setIfAbsent(key, dbShipment.getShipmentNo(), 1L, TimeUnit.MINUTES)) {
            throw new BusinessException("短时间内重复审核" + dbShipment.getShipmentNo());
        }
        if (!ShipmentStatusEnum.PENDING.getCode().equals(dbShipment.getShipmentStatus())) {
            throw new BusinessException("只有待审核的出货单才可审核！");
        }
        //规则校验
        this.verifyDetailIsExist(shipmentWithDetailIn, ShipmentStatusEnum.APPROVED.getCode());
        // 申请数对比可用库存
        this.recalculateNeedUpdateDetailForAudit(shipmentWithDetailIn.getWholesaleShipmentDetailList(), dbShipment);
        //保存出货单和明细
        ShipmentWithDetailOut shipmentWithDetailOut = this.saveOrUpdateShipment(shipmentWithDetailIn);
        dbShipment.setUpdater(UserUtil.getUserName());
        // 如果审核数都为0，整单作废
        if (!DistributionWaysEnum.TRANSFER.getType().equals(dbShipment.getDistributionType())
                && dbShipment.getAuditQuantity().compareTo(NumberUtil.INTEGER_ZERO) == NumberUtil.INTEGER_ZERO) {
            log.info("批发出货单{}审核库存都为0，整单作废", dbShipment.getShipmentNo());
            this.invalid(dbShipment);
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                    String.valueOf(shipmentWithDetailIn.getWholesaleShipment().getId()),
                    OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                    "批发单审核库存都为0",
                    new Date(), shipmentWithDetailIn.getWholesaleShipment().getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return null;
        }
        dbShipment.setAuditTime(LocalDateTime.now());
        //添加已审核状态
        dbShipment.setShipmentStatus(ShipmentStatusEnum.APPROVED.getCode());

        //更新已审核状态
        this.updateShipment(dbShipment);
        //获取初始化数据
        this.initInventory(shipmentWithDetailIn);
        //调整库存
        List<StockFlowIn> stockFlowIns = this.addOrSubStock(shipmentWithDetailIn.getWholesaleShipment(), shipmentWithDetailIn.getWholesaleShipmentDetailList(), ShipmentStatusEnum.APPROVED.getCode());
        if (!DistributionWaysEnum.TRANSFER.getType().equals(dbShipment.getDistributionType())) {
            //调用库存rpc调整库存
            Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
            if (!stockFlow.isSuccess()) {
                throw new BusinessException("库存调整失败");
            }
        }
        //资管调用参数转换
        if (!clientDistInfoService.getIsIndependentAccountingByCode(dbShipment.getClientCode(), dbShipment.getBizOrgCode())) {
            if (BigDecimal.ZERO.compareTo(dbShipment.getAuditAmount().abs()) != 0) {
                List<FrozenOrderIn> frozenOrderIns = Lists.newArrayList();
                FrozenOrderIn frozenOrderIn = new FrozenOrderIn();
                frozenOrderIn.setBusinessNo(dbShipment.getShipmentNo());
                frozenOrderIn.setAmount(dbShipment.getAuditAmount().abs());
                frozenOrderIn.setBusinessType(FundTypeEnum.WHOLESALE_SHIPMENT_AUDIT.getCode());
                frozenOrderIns.add(frozenOrderIn);
                StoreFrozenIn storeFrozenIn = new StoreFrozenIn();
                storeFrozenIn.setFrozenOrders(frozenOrderIns);
                storeFrozenIn.setPrincipalCode(dbShipment.getClientCode());
                storeFrozenIn.setPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
                storeFrozenIn.setBizOrgCode(dbShipment.getBizOrgCode());
                Response response = asyncFundService.asyncFrozen(storeFrozenIn);
                if (!response.isSuccess()) {
                    log.error("批发出货单{}冻结审核金额失败{}", dbShipment.getShipmentNo(), response.getMessage());
                    throw new BusinessException("资管冻结失败");
                }
            }
        }
        String logContent = OperateLogTypeEnum.APPROVED.getName();
        //审核之后批发出货单下发DTS
        if (stockServer.isSendWms(shipmentWithDetailOut.getShipmentStockCode(), shipmentWithDetailOut.getBizOrgCode())) {
            this.wholesaleShipmentToDts(shipmentWithDetailOut);
            // 是否延迟推采购
            if (Objects.nonNull(dbShipment.getPushPurTime()) && dbShipment.getPushPurTime().isAfter(dbShipment.getAuditTime())) {
                logContent = OrdLogTypeEnum.SHIPMENT_AUDIT_DELAY_PUSH_PUR.getName();
            }
        }
        //保存出货单审核日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                String.valueOf(shipmentWithDetailIn.getWholesaleShipment().getId()),
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                logContent,
                new Date(), shipmentWithDetailIn.getWholesaleShipment().getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return shipmentWithDetailOut;
    }

    /**
     * 出货单作废
     *
     * @param wholesaleShipment 出货单实体
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalid(WholesaleShipment wholesaleShipment) {
        //查询出货单是否存在
        WholesaleShipment dbShipment = wholesaleShipmentMapper.selectByPrimaryKey(wholesaleShipment.getId());
        if (Objects.isNull(dbShipment)) {
            throw new BusinessException("批发出货单不存在不可作废");
        }
        //获取出货单最新状态
        String shipmentStatus = dbShipment.getShipmentStatus();
        if (!ShipmentStatusEnum.PENDING.getCode().equals(shipmentStatus) &&
                !ShipmentStatusEnum.APPROVED.getCode().equals(shipmentStatus)) {
            throw new BusinessException("不可作废：出货单状态为=>" + ShipmentStatusEnum.getNameByCode(dbShipment.getShipmentStatus()));
        }
        //添加已作废状态
        dbShipment.setShipmentStatus(ShipmentStatusEnum.INVALID.getCode());
        //更新状态
        int updateCount = this.updateShipment(dbShipment);

        //批发出入参实体
        ShipmentWithDetailIn shipmentWithDetailIn = new ShipmentWithDetailIn();
        //添加出货单
        shipmentWithDetailIn.setWholesaleShipment(dbShipment);
        //添加出货单明细
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailIn = this.findShipmentDetailByShipmentId(dbShipment.getId());
        shipmentWithDetailIn.setWholesaleShipmentDetailList(wholesaleShipmentDetailIn);
        //审核后作废 调用库存和资管
        if (ShipmentStatusEnum.APPROVED.getCode().equals(shipmentStatus)) {
            //获取初始化数据
            this.initInventory(shipmentWithDetailIn);
            if (!DistributionWaysEnum.TRANSFER.getType().equals(dbShipment.getDistributionType())) {
                //调整库存
                List<StockFlowIn> stockFlowIns = this.addOrSubStock(shipmentWithDetailIn.getWholesaleShipment(),
                        shipmentWithDetailIn.getWholesaleShipmentDetailList(), ShipmentInvAdjustStatusEnum.APPROVED_INVALID.getCode());
                //调用库存rpc调整库存
                Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
                if (!stockFlow.isSuccess()) {
                    throw new BusinessException("库存调整失败");
                }
            }
            if (!clientDistInfoService.getIsIndependentAccountingByCode(wholesaleShipment.getClientCode(), wholesaleShipment.getBizOrgCode())) {
//                //资管调用参数转换
//                RechargeLiquidationIn shipmentLiquidationIn = this.chargeParamToShipmentLiquidationIn(dbShipment, null,
//                        ShipmentInvAdjustStatusEnum.APPROVED_INVALID.getCode());
//                if (BigDecimal.ZERO.compareTo(shipmentLiquidationIn.getLiquidationAmount()) != 0) {
//                    //调用rpc调整资金
//                    Response fund = syncOrdDisOrderHandle.syncOrderToFund(shipmentLiquidationIn);
//                    if (!fund.isSuccess()) {
//                        throw new BusinessException("资管调整失败");
//                    }
//                }
                UnFrozenIn unFrozenIn = new UnFrozenIn();
                unFrozenIn.setUnFrozenBusinessNos(Collections.singletonList(dbShipment.getShipmentNo()));
                Response response = asyncFundService.asyncUnFrozen(unFrozenIn);
                if (!response.isSuccess()) {
                    log.error("批发出货单{}释放审核金额失败{}", dbShipment.getShipmentNo(), response.getMessage());
                    throw new BusinessException("资管释放审核金额失败");
                }
            }
        }
        // ZK的单子作废通知ZK
        if (StringUtils.isNotBlank(dbShipment.getSourceNo()) && dbShipment.getSourceNo().startsWith("YH")) {
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.ZK_WHOLESALE_SHIPMENT_BACK, JSONObject.toJSONString(dbShipment),
//                    dbShipment.getBizOrgCode(), dbShipment.getShipmentNo());
            SendResponse sendResponse = zKWholesaleShipmentBackSender.sendSync(JSONObject.toJSONString(dbShipment).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
            log.info("作废---中科请求创建批发出货单回传中科{}消息ID---{}", wholesaleShipment.getShipmentNo(), sendResponse.getMessageId());
        }
        //  HS回传
        if (StringUtils.isNotBlank(wholesaleShipment.getSourceNo()) && wholesaleShipment.getSourceNo().startsWith("HS")) {
            // 单据状态回传
            BackToHsBaseInfo backToHsBaseInfo = new BackToHsBaseInfo();
            backToHsBaseInfo.setOrderNo(wholesaleShipment.getSourceNo());
            backToHsBaseInfo.setErpOrderNo(wholesaleShipment.getShipmentNo());
            backToHsBaseInfo.setBizOrgCode(wholesaleShipment.getBizOrgCode());
            hsWholesaleShipmentDtsBackSender.sendSync(JSONObject.toJSONString(backToHsBaseInfo).getBytes(),System.currentTimeMillis() + SystemConstant.HS_DIFF_DELAY_TIME);
            // 获取HS差异发送延迟消息
            HSWholesaleDifferenceOrder hsWholesaleDifferenceOrder = wholesaleShipmentDetailService.initHsOrderDifference(wholesaleShipment.getId());
            if (Objects.nonNull(hsWholesaleDifferenceOrder)) {
                hsWholesaleDiffSender.sendSync(JSONObject.toJSONString(hsWholesaleDifferenceOrder).getBytes(), System.currentTimeMillis() + SystemConstant.HS_DIFF_DELAY_TIME);
            }
        }
        //保存作废单审核日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                String.valueOf(wholesaleShipment.getId()), OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                OperateLogTypeEnum.INVALID.getName(),
                new Date(), wholesaleShipment.getUpdater());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return updateCount;
    }

    /**
     * 批发出货单查询
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    @Override
    public ShipmentWithDetailOut getShipment(Long id, String bizOrgCode) {
        WholesaleShipment dbShipment = this.getShipmentById(id, bizOrgCode);
        //结果集
        ShipmentWithDetailOut shipmentWithDetailOut = new ShipmentWithDetailOut();
        //copy出货单至结果集
        BeanUtils.copy(dbShipment, shipmentWithDetailOut);
        if (Objects.nonNull(dbShipment.getAuditTime())) {
            shipmentWithDetailOut.setAuditTimeStr(DateUtils.format(dbShipment.getAuditTime()));
        }
        //根据出货单id查询明细
        List<WholesaleShipmentDetailIn> dbShipmentDetails = this.findShipmentDetailByShipmentId(id);
        //添加出货单明细并中文转换
        this.addClientDistInfoAndShipmentConvert(shipmentWithDetailOut, dbShipmentDetails);
        if (StringUtils.isBlank(dbShipment.getPriceGroupCode())) {
            // 查询客户信息
            List<ClientDistInfoOut> clientDistInfos = clientDistInfoService.findClientDistInfo(new QueryClientDistInfoIn(dbShipment.getClientCode(), dbShipment.getBizOrgCode()));
            if (CollectionUtils.isEmpty(clientDistInfos)) {
                throw new BusinessException("客户查询失败");
            }
            ClientDistInfoOut clientDistInfo = clientDistInfos.get(0);
            shipmentWithDetailOut.setPriceGroupCode(clientDistInfo.getPriceGroupCode());
        }
        return shipmentWithDetailOut;
    }


    /**
     * 出货单冲销
     *
     * @param shipmentOrderWriteOffIn 出货单冲销入参类
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WholesaleShipment writeOff(ShipmentOrderWriteOffIn shipmentOrderWriteOffIn, String loginUsername) {
        //查询出货单
        WholesaleShipment wholesaleShipment = wholesaleShipmentMapper.selectByPrimaryKey(shipmentOrderWriteOffIn.getWholesaleShipmentId());
        if (Objects.isNull(wholesaleShipment)) {
            throw new BusinessException("出货单不存在");
        }
        if (!ShipmentStatusEnum.SHIPPED.getCode().equals(wholesaleShipment.getShipmentStatus())) {
            throw new BusinessException("已发货状态单据才可冲销");
        }
        if (NumberUtil.INTEGER_ONE.equals(wholesaleShipment.getIsReversalOrder())) {
            throw new BusinessException("该单据已经被红冲");
        }

        if (NumberUtil.INTEGER_ONE.equals(wholesaleShipment.getIsReversal())) {
            throw new BusinessException("该单据已被红冲");
        }

        //冲销后更新原单并生成已发货状态且数量和金额较原单据相反的冲销单(包含明细)
        WholesaleShipment newShipment = this.saveShipmentDetailAndUpdateOldShipment(wholesaleShipment, shipmentOrderWriteOffIn.getWholesaleShipmentDetailList());
        //批发出入参实体
        ShipmentWithDetailIn shipmentWithDetailIn = new ShipmentWithDetailIn();
        //添加出货单
        shipmentWithDetailIn.setWholesaleShipment(newShipment);
        //添加出货单明细
        shipmentWithDetailIn.setWholesaleShipmentDetailList(shipmentOrderWriteOffIn.getWholesaleShipmentDetailList());

        //冲销时调整库存
        List<StockFlowIn> stockFlowIns = this.addOrSubStock(newShipment, shipmentOrderWriteOffIn.getWholesaleShipmentDetailList(), ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode());
        //调用库存rpc调整库存
        Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
        if (!stockFlow.isSuccess()) {
            throw new BusinessException("库存调整失败");
        }
        if (!clientDistInfoService.getIsIndependentAccountingByCode(wholesaleShipment.getClientCode(), wholesaleShipment.getBizOrgCode())) {
            //资管调用参数转换
            RechargeLiquidationIn shipmentLiquidationIn = this.chargeParamToShipmentLiquidationIn(wholesaleShipment, newShipment.getShipmentNo(), ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode());
            if (BigDecimal.ZERO.compareTo(shipmentLiquidationIn.getLiquidationAmount()) != 0) {
                //调用rpc调整资金
                Response fund = syncOrdDisOrderHandle.syncOrderToFund(shipmentLiquidationIn);
                if (!fund.isSuccess()) {
                    throw new BusinessException("资管调整失败");
                }
            }
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                String.valueOf(wholesaleShipment.getId()), OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                wholesaleShipment.getShipmentNo() + "已冲销",
                new Date(), loginUsername);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return newShipment;
    }

    /**
     * 导出批发出货单
     *
     * @param wholesaleShipmentId 出货单详情入参
     * @return
     */
    @Override
    public String export(Long wholesaleShipmentId) {
        //获取分页数据
        List<WholesaleShipmentDetail> detailOuts = wholesaleShipmentDetailService.findListByShipmentId(wholesaleShipmentId);
        //导出Excel实体
        List<ExportShipmentOrder> exportShipmentOrders = new ArrayList<>();
        //商品属性转换中文
        for (WholesaleShipmentDetail detailOut : detailOuts) {
            WholesaleShipmentDetailOut wholesaleShipmentDetailOut = new WholesaleShipmentDetailOut();
            BeanUtils.copy(detailOut, wholesaleShipmentDetailOut);
            wholesaleShipmentDetailOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(detailOut.getGoodsType()));
            //Excel实体
            ExportShipmentOrder exportShipmentOrder = new ExportShipmentOrder();
            //copy属性和值
            BeanUtils.copy(wholesaleShipmentDetailOut, exportShipmentOrder);
            exportShipmentOrders.add(exportShipmentOrder);
        }
        //导入excel标题
        String title = "批发出货单信息";
        byte[] fileBytesByData = FileExportUtil.getFileBytesByData(
                exportShipmentOrders,
                title,
                title,
                ExportShipmentOrder.class,
                true);

        return fileService.uploadFile(
                title + ".xlsx",
                fileBytesByData,
                SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }


    /**
     * 批量导入批发出货单
     *
     * @param importShipmentOrderIn 批发出货单批量导入 入参类
     * @return
     */
    @Override
    public Response<List<WholesaleShipmentDetailOut>> importShipmentOrderDtl(ImportShipmentOrderIn importShipmentOrderIn) {
        byte[] bytes = fileService.getFileBytesByFileId(importShipmentOrderIn.getFileId(), SystemConstant.SYSTEM_CODE);
        if (Objects.isNull(bytes)) {
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        boolean isCanEditPrice = clientDistInfoService.getIsCanEditPrice(importShipmentOrderIn.getClientCode(), UserUtil.getBizOrgCode());
        ImportShipmentOrderDtlListener importShipmentOrderDtlListener = new ImportShipmentOrderDtlListener(importShipmentOrderIn, saleGoodsInfoClient, isCanEditPrice);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportShipmentOrderDtl.class, importShipmentOrderDtlListener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(0).build();
        excelReader.read(readSheet).finish();
        return importShipmentOrderDtlListener.getResponse();
    }


    /**
     * 保存、更新出货单以及明细
     *
     * @param shipmentWithDetailIn 批发出货单和明细新增入参类
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ShipmentWithDetailOut saveOrUpdateShipment(ShipmentWithDetailIn shipmentWithDetailIn) {
        //获取出货单
        WholesaleShipment wholesaleShipment = shipmentWithDetailIn.getWholesaleShipment();
        //获取出货单明细集合
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList = shipmentWithDetailIn.getWholesaleShipmentDetailList();
        //当前列表商品去重
        Map<String, String> goodsMap = new HashMap<>(2);
        //统计当前所有明细集合的申请数量并求和
        int applicationQuantity = wholesaleShipmentDetailList.stream().filter(a -> {
            boolean flag = !goodsMap.containsKey(a.getGoodsCode());
            goodsMap.put(a.getGoodsCode(), a.getGoodsCode());
            return flag;
        }).mapToInt(WholesaleShipmentDetailIn::getApplyQuantity).sum();
        //明细 申请数量 * 单价并求和
        Map<String, String> checkMap = new HashMap<>(2);
        BigDecimal amount = wholesaleShipmentDetailList.stream().filter(a -> {
            boolean flag = !checkMap.containsKey(a.getGoodsCode());
            checkMap.put(a.getGoodsCode(), a.getGoodsCode());
            return flag;
        }).map(x -> x.getUnitPrice().multiply(new BigDecimal(x.getApplyQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);

        //添加申请数量
        wholesaleShipment.setApplicationQuantity(applicationQuantity);
        //添加审核数量
        wholesaleShipment.setAuditQuantity(applicationQuantity);
        //添加审请金额
        wholesaleShipment.setApplicationAmount(amount);
        //添加审核金额
        wholesaleShipment.setAuditAmount(amount);

        if (Objects.isNull(wholesaleShipment.getId())) {
            /** 新增 */
            //新增时：是否红冲 是否红冲单 默认0
            wholesaleShipment.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
            wholesaleShipment.setIsReversal(NumberUtil.INTEGER_ZERO);
            //生成出货单号
            wholesaleShipment.setShipmentNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KFC.getCode(), wholesaleShipment.getBizOrgCode(), uniqueUtils, 4));
            wholesaleShipmentMapper.insertSelective(wholesaleShipment);
        } else {
            /** 更新 */
            //查询出货单并校验是否存在且可修改
            this.getShipmentById(wholesaleShipment);
            //更新
            this.updateShipment(wholesaleShipment);

        }
        //出货单结果集实体
        ShipmentWithDetailOut shipmentWithDetailOut = new ShipmentWithDetailOut();
        //将出货单保存成功结果添加至返回结果集
        BeanUtils.copy(wholesaleShipment, shipmentWithDetailOut);

        //保存出货单明细
        wholesaleShipmentDetailService.save(wholesaleShipmentDetailList, wholesaleShipment);
        //添加出货单明细并中文转换
        this.addClientDistInfoAndShipmentConvert(shipmentWithDetailOut, wholesaleShipmentDetailList);
        return shipmentWithDetailOut;
    }


    /**
     * 查询批发出货单并校验是否存在且可修改
     *
     * @param wholesaleShipment 批发出货单
     * @return
     */
    public WholesaleShipment getShipmentById(WholesaleShipment wholesaleShipment) {
        //查询出货单是否存在
        WholesaleShipment dbShipment = wholesaleShipmentMapper.selectByPrimaryKey(wholesaleShipment.getId());
        if (Objects.isNull(dbShipment)) {
            throw new BusinessException("批发出货单不存在");
        }
        if (!ShipmentStatusEnum.PENDING.getCode().equals(dbShipment.getShipmentStatus())) {
            throw new BusinessException("不可修改：出货单状态为=>" + ShipmentStatusEnum.getNameByCode(dbShipment.getShipmentStatus()));
        }

        return dbShipment;
    }


    /**
     * 根据出货单id查询明细
     *
     * @param shipmentId 出货单id
     * @return
     */
    private List<WholesaleShipmentDetailIn> findShipmentDetailByShipmentId(Long shipmentId) {
        //查询出货单详情
        QueryShipmentDetailIn queryShipmentDetailIn = new QueryShipmentDetailIn();
        queryShipmentDetailIn.setWholesaleShipmentId(shipmentId);
        queryShipmentDetailIn.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleShipmentDetailService.findShipmentDetailList(queryShipmentDetailIn);
    }

    /**
     * 根据商品代码和出货单状态 校验出货单是否已存在
     *
     * @param shipmentWithDetailIn 出货单入参
     * @param status               状态用来区分是否是审核
     * @return
     */
    public void verifyDetailIsExist(ShipmentWithDetailIn shipmentWithDetailIn, String status) {
        WholesaleShipment wholesaleShipment = shipmentWithDetailIn.getWholesaleShipment();
        // 查询客户信息
        List<ClientDistInfoOut> clientDistInfos = clientDistInfoService.findClientDistInfo(new QueryClientDistInfoIn(wholesaleShipment.getClientCode(), wholesaleShipment.getBizOrgCode()));
        if (CollectionUtils.isEmpty(clientDistInfos)) {
            log.error("批发出查询客户代码{}-----收货信息校验异常", wholesaleShipment.getClientCode());
            throw new BusinessException("批发出查询客户代码" + wholesaleShipment.getClientCode() + "收货信息校验失败");
        }
        ClientDistInfoOut clientDistInfo = clientDistInfos.get(0);
        List<String> goodsCodeList = shipmentWithDetailIn.getWholesaleShipmentDetailList().stream().map(WholesaleShipmentDetailIn::getGoodsCode).collect(Collectors.toList());
        //调用rpc获取商品信息以及包装规格、批发价、库存价
        QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
        querySaleGoodsInfoIn.setBizOrgCode(wholesaleShipment.getBizOrgCode());
        querySaleGoodsInfoIn.setGoodsCodeList(goodsCodeList);
        querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.OUT.getCode());
        querySaleGoodsInfoIn.setClientCode(wholesaleShipment.getClientCode());
        querySaleGoodsInfoIn.setStockId(shipmentWithDetailIn.getStockId());
        querySaleGoodsInfoIn.setStockCode(wholesaleShipment.getShipmentStockCode());
        querySaleGoodsInfoIn.setWarehouseCode(wholesaleShipment.getShipmentWrh());
        querySaleGoodsInfoIn.setPriceGroupCode(clientDistInfo.getPriceGroupCode());
        //查询商品信息
        Response<List<SaleGoodsInfoOut>> response = saleGoodsInfoClient.findGoodsInfo(querySaleGoodsInfoIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error("批发客户代码{}查询仓位{}商品异常", wholesaleShipment.getClientCode(), wholesaleShipment.getShipmentStockCode(), response.getMessage());
            throw new BusinessException(response.getMessage());
        }
        Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap = response.getData().stream().collect(Collectors.toMap(SaleGoodsInfoOut::getGoodsCode, Function.identity()));
        //当前列表商品去重
        Map<String, String> checkMap = new HashMap<>();
        //获取出货单详情
        for (WholesaleShipmentDetailIn wholesaleShipmentDetailIn : shipmentWithDetailIn.getWholesaleShipmentDetailList()) {
            //获取商品代码
            String goodsCode = wholesaleShipmentDetailIn.getGoodsCode();
            //校验校验商品代码是否存在、组织商品存在且配货方式是统配、商品状态允许做批发出货业务、有批发价
            if (StringUtils.isBlank(goodsCode)) {
                throw new BusinessException("商品代码不能为空");
            }
            //调用rpc获取商品信息以及包装规格、批发价、库存价
            SaleGoodsInfoOut saleGoodsInfo = saleGoodsInfoOutMap.get(wholesaleShipmentDetailIn.getGoodsCode());
            if (Objects.isNull(saleGoodsInfo)) {
                throw new BusinessException(wholesaleShipmentDetailIn.getGoodsCode() + ":商品状态不允许做批发出货业务");
            }
            if (!saleGoodsInfo.getDistributionWay().equals(shipmentWithDetailIn.getWholesaleShipment().getDistributionType())) {
                throw new BusinessException("商品" + goodsCode + "配送方式与所选批发单不匹配");
            }
//            //查询详情 校验是否已存在
//            WholesaleShipmentDetail dbDetail = wholesaleShipmentMapper.getWholesaleShipmentDetail(
//                    wholesaleShipmentDetailIn.getGoodsCode(),
//                    ShipmentStatusEnum.PENDING.getCode(),
//                    shipmentWithDetailIn.getWholesaleShipment().getBizOrgCode());
//
//            if(Objects.isNull(wholesaleShipmentDetailIn.getWholesaleShipmentId()) && Objects.nonNull(dbDetail)){
//                throw new BusinessException(goodsCode + "：该商品在出货单中已存在");
//            }
//
//            if (Objects.nonNull(wholesaleShipmentDetailIn.getWholesaleShipmentId()) && (Objects.nonNull(dbDetail.getWholesaleShipmentId()) && !wholesaleShipmentDetailIn.getWholesaleShipmentId().equals(dbDetail.getWholesaleShipmentId()))) {
//                throw new BusinessException(goodsCode + "：该商品在出货单中已存在");
//            }

            if (checkMap.containsKey(wholesaleShipmentDetailIn.getGoodsCode())) {
                throw new BusinessException(wholesaleShipmentDetailIn.getGoodsCode() + "：此出货单商品代码重复");
            }

            //如果是审核 则校验商品库存是否足够
            if (StringUtils.isNotBlank(status) && ShipmentStatusEnum.APPROVED.getCode().equals(status)) {
                if (!clientDistInfoService.getIsIndependentAccountingByCode(shipmentWithDetailIn.getWholesaleShipment().getClientCode(), shipmentWithDetailIn.getWholesaleShipment().getBizOrgCode())) {
                    //判断资金是否满足
                    ForeignAccountFundIn foreignAccountFundIn = new ForeignAccountFundIn();
                    //业务组织code
                    foreignAccountFundIn.setBizOrgCode(shipmentWithDetailIn.getWholesaleShipment().getBizOrgCode());
                    //主体类型
                    foreignAccountFundIn.setPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
                    //客户编码
                    foreignAccountFundIn.setPrincipalCode(shipmentWithDetailIn.getWholesaleShipment().getClientCode());
                    //查询可用金额
                    ForeignAccountFundOut fundOut = fundServer.getAvailableAmount(foreignAccountFundIn);
                    //可用金额是否
                    if (Objects.isNull(fundOut)) {
                        String errorMsg = "客户" + shipmentWithDetailIn.getWholesaleShipment().getClientCode() + "资金账户查询失败";
                        log.error(errorMsg);
                        throw new BusinessException(errorMsg);
                    }
                    if (NumberUtil.INTEGER_ZERO < wholesaleShipmentDetailIn.getAuditAmount().compareTo(fundOut.getAvailableAmount().add(fundOut.getCredit()))) {
                        throw new BusinessException("可用金额不足");
                    }
                }
                // 2023-10-13 天岁批发出货单审核不在校验负库存
//                //判断是否负库存 负库存不校验数量
//                com.edc.erp.common.model.out.stock.StockInfoOut stockInfoOut = stockServer.getByCode(shipmentWithDetailIn.getWholesaleShipment().getShipmentStockCode(), shipmentWithDetailIn.getWholesaleShipment().getBizOrgCode());
//                if (NumberUtil.INTEGER_ZERO.equals(stockInfoOut.getIsAllowNegativeStocks())) {
//                    //如果是0 则不允许负库存  查询当前库存数量并校验 数量是否足够扣减
//                    BigDecimal stockPrice = ordDisDelivDifferenceMapper.checkStockInv(
//                            shipmentWithDetailIn.getWholesaleShipment().getBizOrgCode(),
//                            wholesaleShipmentDetailIn.getGoodsCode(),
//                            shipmentWithDetailIn.getWholesaleShipment().getShipmentStockCode());
//                    BigDecimal bigDecimalTar = Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice;
//                    if (NumberUtil.INTEGER_ZERO > bigDecimalTar.compareTo(new BigDecimal(wholesaleShipmentDetailIn.getAuditQuantity().toString()))) {
//                        throw new BusinessException("商品【" + wholesaleShipmentDetailIn.getGoodsCode() + "】" + "仓储可用库存不足");
//                    }
//                }
            }

            //将当前列表商品code添加 去重
            checkMap.put(wholesaleShipmentDetailIn.getGoodsCode(), wholesaleShipmentDetailIn.getGoodsCode());
            //校验批发数量、包装数
            this.verify(wholesaleShipmentDetailIn, saleGoodsInfo);
            //校验通过 获取最新的库存价
            wholesaleShipmentDetailIn.setInventoryPrice(Objects.isNull(saleGoodsInfo.getInventoryPrice()) ? BigDecimal.ZERO : saleGoodsInfo.getInventoryPrice());
            // 退货原则
            wholesaleShipmentDetailIn.setReturnPrinciple(saleGoodsInfo.getReturnPrinciple());
            if (DistributionWaysEnum.TRANSFER.getType().equals(wholesaleShipment.getDistributionType()) && StringUtils.isBlank(saleGoodsInfo.getVendorCode())) {
                throw new BusinessException("商品" + goodsCode + "订单方不存在");
            }
            wholesaleShipmentDetailIn.setVendorCode(saleGoodsInfo.getVendorCode());
            wholesaleShipmentDetailIn.setInvoiceType(saleGoodsInfo.getInvoiceType());
        }
    }


    /**
     * 校验批发数量、包装数、批发单价
     *
     * @param details       出货单明细
     * @param saleGoodsInfo 商品信息
     */
    public void verify(WholesaleShipmentDetailIn details, SaleGoodsInfoOut saleGoodsInfo) {
        //获取批发数量
        Integer applyQuantity = details.getApplyQuantity();
        //获取申请包装数
        String applyPackageNum = details.getApplyPackageNum();
        //获取单价
        BigDecimal unitPrice = details.getUnitPrice();

        if (Objects.isNull(applyQuantity)) {
            throw new BusinessException(details.getGoodsCode() + "：批发数量不能为空");
        }

        if (ModelConst.DELETE.NO.equals(applyQuantity)) {
            throw new BusinessException(details.getGoodsCode() + "：批发数量不能为0");
        }

        if (ModelConst.DELETE.NO > applyQuantity) {
            throw new BusinessException(details.getGoodsCode() + "：批发数量不可为负数");
        }

//        if (Objects.isNull(applyPackageNum)) {
        if (StringUtils.isBlank(applyPackageNum)) {
            throw new BusinessException(details.getGoodsCode() + "：包装数不能为空");
        }

        if (ModelConst.DELETE.NO.equals(applyPackageNum)) {
            throw new BusinessException(details.getGoodsCode() + "：包装数不能为0");
        }
//        if (ModelConst.DELETE.NO > applyPackageNum) {
//            throw new BusinessException(details.getGoodsCode() + "：包装数不可为负数");
//        }

//        if (applyQuantity % saleGoodsInfo.getQpc() != NumberUtil.INTEGER_ZERO) {
//            throw new BusinessException(details.getGoodsCode() + "：申请数量必须符合整批包装数");
//        }

        if (Objects.isNull(unitPrice)) {
            throw new BusinessException(details.getGoodsCode() + "：单价不能为空");
        }
        if (ModelConst.DELETE.NO > unitPrice.compareTo(BigDecimal.ZERO)) {
            throw new BusinessException(details.getGoodsCode() + "：单价不能为负数");
        }
    }


    /**
     * 查询商品信息
     *
     * @param detail     出货单明细
     * @param bizOrgCode 业务组织
     * @return
     */
    @Override
    public SaleGoodsInfoOut getSaleGoodsInfo(WholesaleShipmentDetailIn detail, String bizOrgCode, String priceGroupCode) {
        //调用rpc获取商品信息以及包装规格、批发价、库存价
        QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
        //业务组织
        querySaleGoodsInfoIn.setBizOrgCode(bizOrgCode);
        //商品代码
        querySaleGoodsInfoIn.setGoodsCode(detail.getGoodsCode());
        //批发出货类型
        querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.OUT.getCode());
        //客户代码
        querySaleGoodsInfoIn.setClientCode(detail.getClientCode());
        //仓位id
        querySaleGoodsInfoIn.setStockId(detail.getStockId());
        //仓位
        querySaleGoodsInfoIn.setStockCode(detail.getStockCode());
        //仓储
        querySaleGoodsInfoIn.setWarehouseCode(detail.getShipmentWrh());
        //查询商品信息
        Response<SaleGoodsInfoOut> goodsInfo = saleGoodsInfoClient.getGoodsInfo(querySaleGoodsInfoIn);
        if (!goodsInfo.isSuccess() || Objects.isNull(goodsInfo.getData())) {
            throw new BusinessException(goodsInfo.getMessage());
        }
        return goodsInfo.getData();
    }


    /**
     * 查询允许批发的仓位信息 - 公共方法
     *
     * @param bizOrgCode         业务组织
     * @param wholesaleOrderType 批发单类型(out：出货，returns：退货)
     * @return
     */
    public List<StockInfoOut> findStockInfoOut(String bizOrgCode, String wholesaleOrderType) {
        //查询允许批发的仓位信息
        QueryWarehouseIn queryWarehouseIn = new QueryWarehouseIn();
        queryWarehouseIn.setBizOrgCode(bizOrgCode);
        queryWarehouseIn.setWholesaleOrderType(wholesaleOrderType);
        queryWarehouseIn.setIsEnable(ModelConst.ENABLE.YES);
        //查询仓位信息
        return this.findStockInfo(queryWarehouseIn);
    }

    /**
     * 根据主键 更新出货单
     *
     * @param wholesaleShipment 出货单实体
     * @return
     */
    public int updateShipment(WholesaleShipment wholesaleShipment) {
        //更新
        return wholesaleShipmentMapper.updateByPrimaryKeySelective(wholesaleShipment);
    }


    /**
     * 增加或减少库存
     *
     * @param wholesaleShipment 出货单
     * @param shipmentDetails   出货单详情
     * @param shipmentStatus    出货单状态
     * @return
     */
    @Override
    public List<StockFlowIn> addOrSubStock(WholesaleShipment wholesaleShipment, List<WholesaleShipmentDetailIn> shipmentDetails, String shipmentStatus) {
        //库存流水入参
        StockFlowIn stockFlowIn = new StockFlowIn();
        //添加业务组织
        stockFlowIn.setBizOrgCode(wholesaleShipment.getBizOrgCode());
        //公司业务组织
        stockFlowIn.setOrgCode(wholesaleShipment.getOrgCode());
        //公司业务类型
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.WHOLESALE_OUT.getCode());
        //公司业务类型名称
        stockFlowIn.setSourceName(InvBusinessTypeEnum.WHOLESALE_OUT.getName());
        //业务发生日期
        stockFlowIn.setFlowDate(LocalDateTime.now());
        //创建人
        stockFlowIn.setCreator(wholesaleShipment.getCreator());

        if (ShipmentInvAdjustStatusEnum.APPROVED_INVALID.getCode().equals(shipmentStatus)) {
            stockFlowIn.setOperationType(OrderTypeEnum.INVALID.getCode());
        }
        if (ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode().equals(shipmentStatus)) {
            stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        }
        if (ShipmentStatusEnum.APPROVED.getCode().equals(shipmentStatus)) {
            stockFlowIn.setOperationType(OrderTypeEnum.APPROVED.getCode());
        }
        if (ShipmentStatusEnum.SHIPPED.getCode().equals(shipmentStatus)) {
            stockFlowIn.setOperationType(OrderTypeEnum.SHIPPED.getCode());
        }
        //单号/流水号
        stockFlowIn.setSourceNo(wholesaleShipment.getShipmentNo());
        // 外部三方单号
        stockFlowIn.setOtherOrderNo(wholesaleShipment.getSourceNo());
        //库存流水详情入参
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        //处理出货单详情
        for (WholesaleShipmentDetailIn shipmentDetail : shipmentDetails) {
            //流水详情入参
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            //copy 同属性字段
            BeanUtils.copy(shipmentDetail, stockFlowGoodsIn);
            //添加仓位code
            stockFlowGoodsIn.setStockCode(wholesaleShipment.getShipmentStockCode());
            //添加仓储code
            stockFlowGoodsIn.setWarehouseCode(wholesaleShipment.getShipmentWrh());
            //根据仓位code查询仓位信息和仓储信息
            com.edc.erp.common.model.out.stock.StockInfoOut stockInfoOut = stockServer.getByCode(
                    wholesaleShipment.getShipmentStockCode(), wholesaleShipment.getBizOrgCode());

            //添加仓位名称
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            //添加仓储名称
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            //审核数量
            BigDecimal auditQuantity = BigDecimal.valueOf(shipmentDetail.getAuditQuantity()).abs();
            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
            //单号
            stockFlowGoodsIn.setSourceNo(wholesaleShipment.getShipmentNo());
            //审核状态
            if (ShipmentStatusEnum.APPROVED.getCode().equals(shipmentStatus)) {
                if (auditQuantity.compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ZERO) {
                    continue;
                }
                //申请数占用
                stockFlowGoodsIn.setApplyQty(auditQuantity);
                //申请增
                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.ADD.getCode());
                //成本不含税金额
                stockFlowGoodsIn.setCostNonTaxAmount(shipmentDetail.getCostNetProfitAmount());
                //成本含税金额
                stockFlowGoodsIn.setCostTaxAmount(shipmentDetail.getCostAmount());
                //成本税额
                stockFlowGoodsIn.setCostTax(shipmentDetail.getCostTax());
                //不含税金额
                stockFlowGoodsIn.setNonTaxAmount(shipmentDetail.getShipmentNetProfit());
                //含税金额
                stockFlowGoodsIn.setTaxAmount(shipmentDetail.getPracticalShipmentAmount());
                //税额
                stockFlowGoodsIn.setTax(shipmentDetail.getShipmentTax());
                //发生价
                stockFlowGoodsIn.setPrice(shipmentDetail.getUnitPrice());
            }
            //已发货状态
            if (ShipmentStatusEnum.SHIPPED.getCode().equals(shipmentStatus)) {
                if (auditQuantity.compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ZERO) {
                    continue;
                }
                //申请数释放（回传数量不管多少 全部释放）
                stockFlowGoodsIn.setApplyQty(auditQuantity);
                if (!DistributionWaysEnum.TRANSFER.getType().equals(wholesaleShipment.getDistributionType())) {
                    //申请减
                    stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
                }
                //申请减
//                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
                //实际减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
                //实际数
                stockFlowGoodsIn.setActualQty(BigDecimal.valueOf(shipmentDetail.getShipmentQuantity()).abs());
                //成本不含税金额
                stockFlowGoodsIn.setCostNonTaxAmount(shipmentDetail.getCostNetProfitAmount());
                //成本含税金额
                stockFlowGoodsIn.setCostTaxAmount(shipmentDetail.getCostAmount());
                //成本税额
                stockFlowGoodsIn.setCostTax(shipmentDetail.getCostTax());
                //不含税金额
                stockFlowGoodsIn.setNonTaxAmount(shipmentDetail.getShipmentNetProfit().negate());
                //含税金额
                stockFlowGoodsIn.setTaxAmount(shipmentDetail.getPracticalShipmentAmount().negate());
                //税额
                stockFlowGoodsIn.setTax(shipmentDetail.getShipmentTax().negate());
                //发生价
                stockFlowGoodsIn.setPrice(shipmentDetail.getInventoryPrice());
            }
            //冲单
            if (ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode().equals(shipmentStatus)) {
                //实际增
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
                //实际数
                stockFlowGoodsIn.setActualQty(BigDecimal.valueOf(shipmentDetail.getShipmentQuantity()).abs());
                //成本不含税金额
                stockFlowGoodsIn.setCostNonTaxAmount(shipmentDetail.getCostNetProfitAmount().abs());
                //成本含税金额
                stockFlowGoodsIn.setCostTaxAmount(shipmentDetail.getCostAmount().abs());
                //成本税额
                stockFlowGoodsIn.setCostTax(shipmentDetail.getCostTax().abs());
                //不含税金额
                stockFlowGoodsIn.setNonTaxAmount(shipmentDetail.getShipmentNetProfit().abs());
                //含税金额
                stockFlowGoodsIn.setTaxAmount(shipmentDetail.getPracticalShipmentAmount().abs());
                //税额
                stockFlowGoodsIn.setTax(shipmentDetail.getShipmentTax().abs());
                //发生价
                stockFlowGoodsIn.setPrice(shipmentDetail.getInventoryPrice().abs());
            }
            //审核后作废
            if (ShipmentInvAdjustStatusEnum.APPROVED_INVALID.getCode().equals(shipmentStatus)) {
                //申请数冲单(释放)
                stockFlowGoodsIn.setApplyQty(auditQuantity);
                //申请减
                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
                //成本不含税金额
                stockFlowGoodsIn.setCostNonTaxAmount(shipmentDetail.getCostNetProfitAmount());
                //成本含税金额
                stockFlowGoodsIn.setCostTaxAmount(shipmentDetail.getCostAmount());
                //成本税额
                stockFlowGoodsIn.setCostTax(shipmentDetail.getCostTax());
                //不含税金额
                stockFlowGoodsIn.setNonTaxAmount(shipmentDetail.getShipmentNetProfit());
                //含税金额
                stockFlowGoodsIn.setTaxAmount(shipmentDetail.getPracticalShipmentAmount());
                //税额
                stockFlowGoodsIn.setTax(shipmentDetail.getShipmentTax());
                //发生价
                stockFlowGoodsIn.setPrice(shipmentDetail.getUnitPrice());
            }
            //添加结果集
            stockFlowGoodsIns.add(stockFlowGoodsIn);
        }
        //库存发生位置
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        //添加结果集
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        //转换list
        return Arrays.asList(stockFlowIn);
    }

    /**
     * 冲销生成新的出货单和明细
     *
     * @param wholesaleShipment 出货单
     * @param shipmentDetails   出货单详情
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public WholesaleShipment saveShipmentDetailAndUpdateOldShipment(WholesaleShipment wholesaleShipment, List<WholesaleShipmentDetailIn> shipmentDetails) {
        /** 修改原单冲销状态 */
        //是否红冲单
        wholesaleShipment.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        //是否红冲
        wholesaleShipment.setIsReversal(NumberUtil.INTEGER_ONE);
        //修改
        this.updateShipment(wholesaleShipment);

        /** 冲销生成新的出货单 - 数量和金额较原单据相反 */
        WholesaleShipment newShipment = new WholesaleShipment();
        //copy无需修改属性
        BeanUtils.copy(wholesaleShipment, newShipment);
        //取消id
        newShipment.setId(null);
        //生成新的单号
        newShipment.setShipmentNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KFC.getCode(), wholesaleShipment.getBizOrgCode(), uniqueUtils, 4));
        //来源单号即原单
        newShipment.setSourceNo(wholesaleShipment.getShipmentNo());
        //是否红冲单
        newShipment.setIsReversalOrder(NumberUtil.INTEGER_ONE);
        //是否红冲
        newShipment.setIsReversal(NumberUtil.INTEGER_ZERO);
        //申请数量
        newShipment.setApplicationQuantity(NumberUtil.INTEGER_ZERO - wholesaleShipment.getApplicationQuantity());
        //申请金额
        newShipment.setApplicationAmount(wholesaleShipment.getApplicationAmount().negate());
        //审核数量
        newShipment.setAuditQuantity(NumberUtil.INTEGER_ZERO - wholesaleShipment.getAuditQuantity());
        //审核金额
        newShipment.setAuditAmount(wholesaleShipment.getAuditAmount().negate());
        //出库数量
        newShipment.setShipmentQuantity(NumberUtil.INTEGER_ZERO - wholesaleShipment.getShipmentQuantity());
        //实际出库金额
        newShipment.setPracticalShipmentAmount(wholesaleShipment.getPracticalShipmentAmount().negate());
        //创建时间
        newShipment.setCreateTime(LocalDateTime.now());
        //更新时间
        newShipment.setUpdateTime(LocalDateTime.now());
        //新增出货单
        wholesaleShipmentMapper.insertSelective(newShipment);

        /** 生成新的出货单明细 */
        //出货单明细 批量保存入参
        ArrayList<WholesaleShipmentDetailIn> wholesaleShipmentDetailIns = new ArrayList<>();
        for (WholesaleShipmentDetailIn shipmentDetail : shipmentDetails) {
            //添加新的出货单关联id
            shipmentDetail.setWholesaleShipmentId(newShipment.getId());
            //申请数量
            shipmentDetail.setApplyQuantity(NumberUtil.INTEGER_ZERO - shipmentDetail.getApplyQuantity());
            //申请金额
            shipmentDetail.setApplyAmount(shipmentDetail.getApplyAmount().negate());
            //审核数量
            shipmentDetail.setAuditQuantity(NumberUtil.INTEGER_ZERO - shipmentDetail.getAuditQuantity());
            //审核金额
            shipmentDetail.setAuditAmount(shipmentDetail.getAuditAmount().negate());
            //出库数量
            shipmentDetail.setShipmentQuantity(NumberUtil.INTEGER_ZERO - shipmentDetail.getShipmentQuantity());
            //实际出库金额
            shipmentDetail.setPracticalShipmentAmount(shipmentDetail.getPracticalShipmentAmount().negate());
            //出库去税金额
            shipmentDetail.setShipmentNetProfit(shipmentDetail.getShipmentNetProfit().negate());
            //出库税额
            shipmentDetail.setShipmentTax(shipmentDetail.getShipmentTax().negate());
            //成本金额
            shipmentDetail.setCostAmount(shipmentDetail.getCostAmount().negate());
            //成本去税金额
            shipmentDetail.setCostNetProfitAmount(shipmentDetail.getCostNetProfitAmount().negate());
            //成本税额
            shipmentDetail.setCostTax(shipmentDetail.getCostTax().negate());
            //创建时间
            shipmentDetail.setCreateTime(LocalDateTime.now());
            //更新时间
            shipmentDetail.setUpdateTime(LocalDateTime.now());

            //添加新的出货单明细
            wholesaleShipmentDetailIns.add(shipmentDetail);
        }
        //批量保存出货单明细
        wholesaleShipmentDetailService.batchSave(wholesaleShipmentDetailIns);

        //冲销单 日志保存
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                String.valueOf(newShipment.getId()), OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                OperateLogTypeEnum.SAVE.getName() + "冲销单：" + newShipment.getShipmentNo(),
                new Date(), newShipment.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return newShipment;
    }


    /**
     * 批发出货单下发DTS
     *
     * @param out 出货单结果集
     */
    private void wholesaleShipmentToDts(ShipmentWithDetailOut out) {
        if (Objects.isNull(out)) {
            return;
        }
        //批发出货单下发DTS入参
        WholesaleBillIn wholesaleBillIn = new WholesaleBillIn();
        //单号
        wholesaleBillIn.setPlatform_bill_id(out.getShipmentNo());
        //批发日期
        wholesaleBillIn.setBill_create_date(out.getCreateTime().toLocalDate());
        //客户ID
        wholesaleBillIn.setCustomer_id(wholesaleService.getClientIdByCode(out.getClientCode(), out.getBizOrgCode()).toString());
        //客户代码
        wholesaleBillIn.setCustomer_code(out.getClientCode());
        //仓储代码
        wholesaleBillIn.setWarehouse_id(out.getShipmentWrh());
        //出货仓位
        wholesaleBillIn.setSource_stock_id(out.getShipmentStockCode());
        //送货地址
        // 收货人|电话|收货地址|remark，
        String consignee = this.replaceStr(out.getConsignee());
        String consigneePhone = this.replaceStr(out.getConsigneePhone());
        String addressDetail = this.replaceStr(out.getAddressDetail());
        String address_i = consignee + SystemConstant.VERTICAL_BAR + consigneePhone + SystemConstant.VERTICAL_BAR + addressDetail;
        wholesaleBillIn.setAddress_i(address_i);
        //优惠价
        wholesaleBillIn.setAmount_ii(out.getAuditAmount());
        //总要货金额
        wholesaleBillIn.setAmount_i(out.getAuditAmount());
        //订单模式
        wholesaleBillIn.setOrder_pattern(DtsOrderPatternTypeEnum.PURCHASE_RETURN.getCode());
        //填单人
        wholesaleBillIn.setCreater(out.getCreator());
        //生成时间
        wholesaleBillIn.setGenerate_time(LocalDateTime.now());
        //明细数量
        wholesaleBillIn.setCount(out.getWholesaleShipmentDetailOuts().size());
        //未收金额
        wholesaleBillIn.setAmount_iii(out.getAuditAmount());
        //出货单明细下发DTS 数据转换
        List<WholesaleBillDtlIn> wholesaleBillDtlIns = this.initShipmentDetailOut(out);
        wholesaleBillIn.setDetail_list(wholesaleBillDtlIns);
        //来源组织
        wholesaleBillIn.setSource_organization(out.getBizOrgCode());
        //目标组织
        wholesaleBillIn.setTarget_organization(out.getBizOrgCode());
        wholesaleBillIn.setMemo(out.getRemark());
        wholesaleBillIn.setFrom_num(out.getSourceNo());
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_TO_DTS, JSONObject.toJSONString(wholesaleBillIn), out.getBizOrgCode(), out.getShipmentNo());
        SendResponse sendResponse = wholesaleShipmentToDtsSender.sendSync(JSONObject.toJSONString(wholesaleBillIn).getBytes(),System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("DTS批发出货单数据下发{}下发DTS消息ID---{}", out.getShipmentNo(), sendResponse.getMessageId());
    }


    /**
     * 出货单明细下发DTS 数据转换
     *
     * @param shipmentOut 出货单结果集
     */
    private List<WholesaleBillDtlIn> initShipmentDetailOut(ShipmentWithDetailOut shipmentOut) {
        //出货单下发DTS 明细保存
        ArrayList<WholesaleBillDtlIn> wholesaleBillDtlIns = new ArrayList<>();
        //获取明细
        List<WholesaleShipmentDetailOut> wholesaleShipmentDetailOuts = shipmentOut.getWholesaleShipmentDetailOuts();
        //为空 则返回空列表
        if (CollectionUtils.isEmpty(wholesaleShipmentDetailOuts)) {
            return new ArrayList<>();
        }
        //记录当前行号
//        int line = 1;
        for (WholesaleShipmentDetailOut detailOut : wholesaleShipmentDetailOuts) {
            if (detailOut.getAuditQuantity().compareTo(NumberUtil.INTEGER_ZERO) == NumberUtil.INTEGER_ZERO) {
                log.info("批发出{}明细{}审核数量为0，故不下发DTS", shipmentOut.getShipmentNo(), detailOut.getGoodsCode());
                continue;
            }
            //出货单下发DTS 明细入参
            WholesaleBillDtlIn wholesaleBillDtlIn = new WholesaleBillDtlIn();
            //单号
            wholesaleBillDtlIn.setPlatform_bill_id(shipmentOut.getShipmentNo());
            //行号
            wholesaleBillDtlIn.setLine(detailOut.getLine());
            //组织商品id
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setGoodsCode(detailOut.getGoodsCode());
            orderGoodsIn.setBizOrgCode(shipmentOut.getBizOrgCode());
            OrderGoodsOut orderGoodsOut = orderGoodsServer.getOrderGoods(orderGoodsIn);
            if (Objects.nonNull(orderGoodsOut)) {
                wholesaleBillDtlIn.setSku_id(String.valueOf(orderGoodsOut.getOrgGoodsId()));
            }
            //商品代码
            wholesaleBillDtlIn.setSku_code(detailOut.getGoodsCode());
            //数量
            wholesaleBillDtlIn.setQuantity(BigDecimal.valueOf(detailOut.getAuditQuantity()));
            //价格
            wholesaleBillDtlIn.setPrice_i(detailOut.getUnitPrice());
            //批发价格
            wholesaleBillDtlIn.setPrice_ii(detailOut.getUnitPrice());
            //优惠金额
            wholesaleBillDtlIn.setDiscount_amount(detailOut.getAuditAmount());
            //退货仓位代码
            wholesaleBillDtlIn.setSource_stock_id(shipmentOut.getShipmentStockCode());
            //来源组织
            wholesaleBillDtlIn.setSource_organization(shipmentOut.getBizOrgCode());
            //目标组织
            wholesaleBillDtlIn.setTarget_organization(shipmentOut.getBizOrgCode());
            // 放置退货原则
            wholesaleBillDtlIn.setMemo(detailOut.getReturnPrinciple());
            //保存明细
            wholesaleBillDtlIns.add(wholesaleBillDtlIn);

//            line++;
        }

        return wholesaleBillDtlIns;
    }


    /**
     * 批发出货单DTS回传入参 转换出货单明细
     *
     * @param wholesaleBillVO 批发单返回入参类
     * @param dbShipment
     * @return
     */
    @Override
    public ShipmentWithDetailIn wholesaleBillVOToShipmentDetail(WholesaleBillVO wholesaleBillVO, WholesaleShipment dbShipment) {
        //出货单入参类
        ShipmentWithDetailIn shipmentWithDetailIn = new ShipmentWithDetailIn();

        //物流单号
        dbShipment.setTrackingNo(wholesaleBillVO.getNum());
        //时间
        dbShipment.setUpdateTime(wholesaleBillVO.getFsendtime());
        //备注
        dbShipment.setRemark(wholesaleBillVO.getFnote());
        // 司机
        dbShipment.setDriverInfo(wholesaleBillVO.getFwmsnote());

        //获取 dts回传明细
        List<WholesaleBillDtlVO> wholesaleBillVODetail = wholesaleBillVO.getDetail();
        //根据出货单id查询明细
        List<WholesaleShipmentDetailIn> dbShipmentDetails = this.findShipmentDetailByShipmentId(dbShipment.getId());
        //明细转map
//        Map<String, WholesaleShipmentDetailIn> detailInMap = dbShipmentDetails.stream().collect(Collectors.toMap(WholesaleShipmentDetailIn::getGoodsCode, item -> item));
        Map<String, WholesaleShipmentDetailIn> detailInMap = dbShipmentDetails.stream()
                .collect(Collectors.toMap(wholesaleShipmentDetailIn -> wholesaleShipmentDetailIn.getGoodsCode() + wholesaleShipmentDetailIn.getLine(), Function.identity()));
        for (WholesaleBillDtlVO wholesaleBillDtlVO : wholesaleBillVODetail) {
            //根据商品code 获取单个明细
            WholesaleShipmentDetailIn detailIn = detailInMap.get(wholesaleBillDtlVO.getFarticlecode() + wholesaleBillDtlVO.getLine());
            //查询商品信息入参初始化
            this.initSaleGoodsInfo(dbShipment, detailIn);
            //查询商品信息
            SaleGoodsInfoOut saleGoodsInfo = this.getSaleGoodsInfo(detailIn, dbShipment.getBizOrgCode(), null);
            //销项税率 如果为null 则为 1 + 0 否则 税率 + 1
            BigDecimal outTaxAddOne = Objects.nonNull(saleGoodsInfo) && Objects.nonNull(saleGoodsInfo.getOutTax()) ? saleGoodsInfo.getOutTax().add(BigDecimal.ONE) : BigDecimal.ZERO.add(BigDecimal.ONE);
            //税率取消 + 1
//            BigDecimal outTax = Objects.nonNull(saleGoodsInfo) && Objects.nonNull(saleGoodsInfo.getOutTax()) ? saleGoodsInfo.getOutTax() : BigDecimal.ZERO;
            //出库数量
            int shipmentQuantity = wholesaleBillDtlVO.getFqty().intValue();
            detailIn.setShipmentQuantity(shipmentQuantity);
            //出库包装数
            String shipmentPackageQuantity = this.getShipmentPackageQuantity(saleGoodsInfo, shipmentQuantity);
            detailIn.setShipmentPackageQuantity(shipmentPackageQuantity);
            //最新库存价
            BigDecimal inventoryPrice = Objects.isNull(saleGoodsInfo) || Objects.isNull(saleGoodsInfo.getInventoryPrice()) ? BigDecimal.ZERO : saleGoodsInfo.getInventoryPrice();
            detailIn.setInventoryPrice(inventoryPrice);
            //计算实际出库金额
            BigDecimal practicalShipmentAmount = detailIn.getUnitPrice().multiply(wholesaleBillDtlVO.getFqty());
            detailIn.setPracticalShipmentAmount(practicalShipmentAmount);
            //出库去税金额 四舍五入保留两位
            BigDecimal shipmentNetProfit = practicalShipmentAmount.divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            detailIn.setShipmentNetProfit(shipmentNetProfit);
            //出库税额 四舍五入保留两位
            detailIn.setShipmentTax(practicalShipmentAmount.subtract(shipmentNetProfit).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //成本金额 四舍五入保留两位
            BigDecimal costAmount = inventoryPrice.multiply(wholesaleBillDtlVO.getFqty()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP).negate();
            detailIn.setCostAmount(costAmount);
            //成本去税金额 四舍五入保留两位
            BigDecimal costNetProfitAmount = costAmount.divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            detailIn.setCostNetProfitAmount(costNetProfitAmount);
            //成本税额 四舍五入保留两位
            detailIn.setCostTax(costAmount.subtract(costNetProfitAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //最新时间
            detailIn.setUpdateTime(LocalDateTime.now());
        }

        //添加处理之后的出货单
        shipmentWithDetailIn.setWholesaleShipment(dbShipment);
        //添加处理之后的出货单明细
        shipmentWithDetailIn.setWholesaleShipmentDetailList(dbShipmentDetails);
        //根据处理之后的明细初始化出货单
        this.initShipmentOrder(shipmentWithDetailIn);

        return shipmentWithDetailIn;
    }

    private static String getShipmentPackageQuantity(SaleGoodsInfoOut saleGoodsInfo, int shipmentQuantity) {
        String shipmentPackageQuantity;
        if (Objects.isNull(saleGoodsInfo) || Objects.isNull(saleGoodsInfo.getPackageSpecification())) {
            shipmentPackageQuantity = NumberUtil.INTEGER_ZERO.toString();
        } else {
            if (shipmentQuantity % saleGoodsInfo.getQpc() != NumberUtil.INTEGER_ZERO) {
                shipmentPackageQuantity = shipmentQuantity / saleGoodsInfo.getQpc() + "+" + shipmentQuantity % saleGoodsInfo.getQpc();
            } else {
                shipmentPackageQuantity = String.valueOf(shipmentQuantity / saleGoodsInfo.getQpc());
            }
        }
        return shipmentPackageQuantity;
    }


    /**
     * 资管调用参数转换
     *
     * @param wholesaleShipment       资管调用入参类
     * @param newShipmentNo           新批发出单号
     * @param shipmentInvAdjustStatus 出货单状态
     * @return
     */
    @Override
    public RechargeLiquidationIn chargeParamToShipmentLiquidationIn(WholesaleShipment wholesaleShipment, String newShipmentNo, String shipmentInvAdjustStatus) {
        //资管调用入参类
        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
        //业务单号
        String businessNo = wholesaleShipment.getShipmentNo();
        String businessType = "";
        // 原单号
        String originalBusinessNo = wholesaleShipment.getShipmentNo();
        String direction = FundDirectionEnum.PAY.getCode();
        //冲单
        if (ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode().equals(shipmentInvAdjustStatus)) {
            //收入方客户编码
            rechargeLiquidationIn.setRecipientPrincipalCode(wholesaleShipment.getClientCode());
            //收入方主体类型
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
            //清算金额
            rechargeLiquidationIn.setLiquidationAmount(wholesaleShipment.getPracticalShipmentAmount().abs());
            rechargeLiquidationIn.setRemark(FundReturnTypeEnum.WHOLESALE_SHIPMENT_CHARGE.getName());
            businessNo = newShipmentNo;
//            businessType = FundTypeEnum.DIS_WHOLESALE_RETURN.getCode();
            direction = FundDirectionEnum.RETURN.getCode();
        }
        //审核后作废
        if (ShipmentInvAdjustStatusEnum.APPROVED_INVALID.getCode().equals(shipmentInvAdjustStatus)) {
            //收入方客户编码
            rechargeLiquidationIn.setRecipientPrincipalCode(wholesaleShipment.getClientCode());
            //收入方主体类型
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
            //清算金额
            rechargeLiquidationIn.setLiquidationAmount(wholesaleShipment.getAuditAmount().abs());
            rechargeLiquidationIn.setRemark(FundReturnTypeEnum.WHOLESALE_SHIPMENT_INVALID.getName());
//            businessType = FundTypeEnum.DIS_WHOLESALE_RETURN.getCode();
            direction = FundDirectionEnum.RETURN.getCode();
        }
        //审核
        if (ShipmentStatusEnum.APPROVED.getCode().equals(shipmentInvAdjustStatus)) {
            //支出方客户编码
            rechargeLiquidationIn.setPayOrPrincipalCode(wholesaleShipment.getClientCode());
            //支出方主体类型
            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
            rechargeLiquidationIn.setRecipientPrincipalCode(wholesaleShipment.getBizOrgCode());
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            //清算金额
            rechargeLiquidationIn.setLiquidationAmount(wholesaleShipment.getAuditAmount().abs());
//            businessType = FundTypeEnum.WHOLESALE_SHIPMENT.getCode();
            direction = FundDirectionEnum.PAY.getCode();
        }
        //少发货
        if (ShipmentStatusEnum.SHIPPED.getCode().equals(shipmentInvAdjustStatus)) {
//            //收入方客户编码
//            rechargeLiquidationIn.setRecipientPrincipalCode(wholesaleShipment.getClientCode());
//            //收入方主体类型
//            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
//            rechargeLiquidationIn.setPayOrPrincipalCode(wholesaleShipment.getBizOrgCode());
//            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            //支出方客户编码
            rechargeLiquidationIn.setPayOrPrincipalCode(wholesaleShipment.getClientCode());
            //支出方主体类型
            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
            rechargeLiquidationIn.setRecipientPrincipalCode(wholesaleShipment.getBizOrgCode());
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            //清算金额
            rechargeLiquidationIn.setLiquidationAmount(wholesaleShipment.getPracticalShipmentAmount().abs());
            rechargeLiquidationIn.setRemark(FundReturnTypeEnum.WHOLESALE_SHIPMENT_SHIPPED.getName());
//            businessType = FundTypeEnum.DIS_WHOLESALE_RETURN.getCode();
//            direction = FundDirectionEnum.RETURN.getCode();
            direction = FundDirectionEnum.PAY.getCode();
        }
        // 2023-06-02 应刘应高要求将批发的业务类型全部调整为批发出货
        businessType = FundTypeEnum.WHOLESALE_SHIPMENT.getCode();
        //业务模块
        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        //业务组织代码
        rechargeLiquidationIn.setBizOrgCode(wholesaleShipment.getBizOrgCode());
        rechargeLiquidationIn.setBusinessNo(businessNo);
        //业务类型
        rechargeLiquidationIn.setBusinessType(businessType);
        // 原单号
        rechargeLiquidationIn.setOriginalBusinessNo(originalBusinessNo);
        rechargeLiquidationIn.setDirection(direction);
        return rechargeLiquidationIn;
    }

    /**
     * 初始化出货单
     *
     * @param shipmentWithDetailIn 根据批发出货DTS回传 处理之后的出货单结果
     */
    public void initShipmentOrder(ShipmentWithDetailIn shipmentWithDetailIn) {
        //获取明细
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList = shipmentWithDetailIn.getWholesaleShipmentDetailList();
        //统计出库数量
        shipmentWithDetailIn.getWholesaleShipment().setShipmentQuantity(wholesaleShipmentDetailList.stream().filter(detail -> Objects.nonNull(detail.getShipmentQuantity())).mapToInt(WholesaleShipmentDetailIn::getShipmentQuantity).sum());
        //实际出库金额
        shipmentWithDetailIn.getWholesaleShipment().setPracticalShipmentAmount(wholesaleShipmentDetailList.stream().filter(detail -> Objects.nonNull(detail.getShipmentQuantity())).map(WholesaleShipmentDetailIn::getPracticalShipmentAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }


    /**
     * 批量更新出货单和明细
     *
     * @param shipmentWithDetailIn 批发出货单 DTS回传处理之后的数据
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdate(ShipmentWithDetailIn shipmentWithDetailIn, String beforeStatus) {
        //更新出货单
//        int updateCount = this.updateShipment(shipmentWithDetailIn.getWholesaleShipment());
        int updateCount = wholesaleShipmentMapper.updateOrder(shipmentWithDetailIn.getWholesaleShipment(), beforeStatus);
        if (updateCount == 0) {
            throw new BusinessException("批发出更新状态失败或者已处理");
        }
        //批量更新出货单明细
        wholesaleShipmentDetailService.batchUpdate(shipmentWithDetailIn.getWholesaleShipmentDetailList());
        return updateCount;
    }

    /**
     * 批发出-发货
     *
     * @param shipmentsIn 批发出-手动发货 入参
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> shipmentSendOut(ShipmentsIn shipmentsIn) {
        //获取出货单id
        List<Long> wholesaleShipmentIds = shipmentsIn.getWholesaleShipmentIds();
        for (Long wholesaleShipmentId : wholesaleShipmentIds) {
            //查询批发出货单
            WholesaleShipment wholesaleShipment = this.getShipmentById(wholesaleShipmentId, shipmentsIn.getBizOrgCode());
            if (!ShipmentStatusEnum.APPROVED.getCode().equals(wholesaleShipment.getShipmentStatus())) {
                return Response.error("已审核状态下才能发货");
            }
            //查询明细
            List<WholesaleShipmentDetailIn> wholesaleShipmentDetailIns = this.findShipmentDetailByShipmentId(wholesaleShipmentId);
            //构建初始化参数
            ShipmentWithDetailIn shipmentWithDetailIn = new ShipmentWithDetailIn();
            //添加出货单
            shipmentWithDetailIn.setWholesaleShipment(wholesaleShipment);
            //添加明细
            shipmentWithDetailIn.setWholesaleShipmentDetailList(wholesaleShipmentDetailIns);
            //初始化出货单和明细
            this.initInventory(shipmentWithDetailIn);
            //修改已发货状态
            shipmentWithDetailIn.getWholesaleShipment().setShipmentStatus(ShipmentStatusEnum.SHIPPED.getCode());
            //更新时间
            shipmentWithDetailIn.getWholesaleShipment().setUpdateTime(LocalDateTime.now());
            shipmentWithDetailIn.getWholesaleShipment().setDeliveryTime(LocalDateTime.now());
            //更新出货单和明细
            int updateCount = this.batchUpdate(shipmentWithDetailIn, ShipmentStatusEnum.APPROVED.getCode());
            if (NumberUtil.INTEGER_ZERO.equals(updateCount)) {
                return Response.error(wholesaleShipment.getShipmentNo() + "发货失败");
            }
            //发货扣减库存
            List<StockFlowIn> stockFlowIns = this.addOrSubStock(
                    shipmentWithDetailIn.getWholesaleShipment(),
                    shipmentWithDetailIn.getWholesaleShipmentDetailList(),
                    ShipmentStatusEnum.SHIPPED.getCode());
            //调用库存rpc调整库存
            Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
            if (!stockFlow.isSuccess()) {
                return Response.error("库存调整失败");
            }
            // 门店非独立核算则释放冻结资金并实扣
            if (!clientDistInfoService.getIsIndependentAccountingByCode(wholesaleShipment.getClientCode(), wholesaleShipment.getBizOrgCode())) {
                //根据审核数量和回传出库数量对比 如果是0 则说明回传和审核一致，则不扣减 如果不是则返还剩余金额
                int contrastResult = wholesaleShipment.getAuditQuantity().compareTo(shipmentWithDetailIn.getWholesaleShipment().getShipmentQuantity());
                if (!NumberUtil.INTEGER_ZERO.equals(wholesaleShipment.getAuditQuantity())) {
//                //资管调用参数转换
                    RechargeLiquidationIn shipmentLiquidationIn = this.chargeParamToShipmentLiquidationIn(shipmentWithDetailIn.getWholesaleShipment(), null, ShipmentStatusEnum.SHIPPED.getCode());
                    UnFrozenAndPayIn unFrozenAndPayIn = new UnFrozenAndPayIn();
                    unFrozenAndPayIn.setUnFrozenBusinessNo(shipmentWithDetailIn.getWholesaleShipment().getShipmentNo());
                    unFrozenAndPayIn.setRechargeLiquidationIn(shipmentLiquidationIn);
                    Response response = asyncFundService.asyncUnFrozenAndSettlement(unFrozenAndPayIn);
                    if (!response.isSuccess()) {
                        log.error("批发出货单{}物流发货回传释放+实扣失败", unFrozenAndPayIn.getUnFrozenBusinessNo(), response.getMessage());
                        throw new BusinessException(response.getMessage());
                    }
                }
            }
            //保存日志
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                    String.valueOf(shipmentWithDetailIn.getWholesaleShipment().getId()),
                    OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(), OrdLogTypeEnum.ORD_WHOLESALE_ARTIFICIAL.getName(),
                    new Date(), shipmentWithDetailIn.getWholesaleShipment().getUpdater());

            asyncLogService.sendAsyncSaveLogByMq(businessLog);

        }

        return Response.success("发货成功");
    }

    /**
     * 批发出货单回传
     *
     * @param wholesaleBillVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String wholesaleOrderCallBack(WholesaleBillVO wholesaleBillVO, WholesaleShipment wholesaleShipment) {
        if (ShipmentStatusEnum.INVALID.getCode().equals(wholesaleShipment.getShipmentStatus())) {
            //保存日志
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                    String.valueOf(wholesaleShipment.getId()),
                    OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(), OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN_DTS_INVALID.getName(),
                    new Date(), wholesaleShipment.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return "批发单已作废：" + wholesaleShipment.getShipmentNo();
        }
        if (ShipmentStatusEnum.SHIPPED.getCode().equals(wholesaleShipment.getShipmentStatus())) {
            return "批发单已发货：" + wholesaleShipment.getShipmentNo();
        }
        //批发出货单DTS回传入参 转换出货单以及明细
        ShipmentWithDetailIn shipmentWithDetailIn = this.wholesaleBillVOToShipmentDetail(wholesaleBillVO, wholesaleShipment);
        //已发货状态
        shipmentWithDetailIn.getWholesaleShipment().setShipmentStatus(ShipmentStatusEnum.SHIPPED.getCode());
        shipmentWithDetailIn.getWholesaleShipment().setUpdateTime(LocalDateTime.now());
        shipmentWithDetailIn.getWholesaleShipment().setDeliveryTime(LocalDateTime.now());
        shipmentWithDetailIn.getWholesaleShipment().setUpdater(wholesaleBillVO.getFfiller());
        //更新出货单以及明细
        this.batchUpdate(shipmentWithDetailIn, ShipmentStatusEnum.APPROVED.getCode());
        //出货扣减库存
        List<StockFlowIn> stockFlowIns = this.addOrSubStock(
                shipmentWithDetailIn.getWholesaleShipment(),
                shipmentWithDetailIn.getWholesaleShipmentDetailList(),
                ShipmentStatusEnum.SHIPPED.getCode());
        long effectiveCount = stockFlowService.getEffectiveStockFlowCount(stockFlowIns);
        if (effectiveCount > NumberUtil.INTEGER_ZERO) {
            //调用库存rpc调整库存
            Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
            if (!stockFlow.isSuccess()) {
                throw new BusinessException(stockFlow.getMessage());
            }
        }
        // 门店非独立核算则释放冻结资金并实扣
        if (!clientDistInfoService.getIsIndependentAccountingByCode(wholesaleShipment.getClientCode(), wholesaleShipment.getBizOrgCode())) {
            //根据审核数量和回传出库数量对比 如果是0 则说明回传和审核一致，则不扣减 如果不是则返还剩余金额
//            int contrastResult = wholesaleShipment.getAuditQuantity().compareTo(shipmentWithDetailIn.getWholesaleShipment().getShipmentQuantity());
            if (!NumberUtil.INTEGER_ZERO.equals(wholesaleShipment.getAuditQuantity())) {
//                //资管调用参数转换
                RechargeLiquidationIn shipmentLiquidationIn = this.chargeParamToShipmentLiquidationIn(shipmentWithDetailIn.getWholesaleShipment(), null, ShipmentStatusEnum.SHIPPED.getCode());
//                String jsonStr = JSONUtil.toJsonStr(shipmentLiquidationIn);
//                log.info(jsonStr);
//                //调用rpc调整资金
//                Response response = syncOrdDisOrderHandle.syncOrderToFund(shipmentLiquidationIn);
//                if (!response.isSuccess()) {
//                    throw new BusinessException(response.getMessage());
//                }
                UnFrozenAndPayIn unFrozenAndPayIn = new UnFrozenAndPayIn();
                unFrozenAndPayIn.setUnFrozenBusinessNo(shipmentWithDetailIn.getWholesaleShipment().getShipmentNo());
                unFrozenAndPayIn.setRechargeLiquidationIn(shipmentLiquidationIn);
                Response response = asyncFundService.asyncUnFrozenAndSettlement(unFrozenAndPayIn);
                if (!response.isSuccess()) {
                    log.error("批发出货单{}物流发货回传释放+实扣失败", unFrozenAndPayIn.getUnFrozenBusinessNo(), response.getMessage());
                    throw new BusinessException(response.getMessage());
                }
            }
        }
        //保存日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                String.valueOf(wholesaleShipment.getId()),
                OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(), ShipmentStatusEnum.SHIPPED.getName(),
                new Date(), wholesaleShipment.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        if (StringUtils.isNotBlank(wholesaleShipment.getSourceNo()) && wholesaleShipment.getSourceNo().startsWith("YH")) {
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.ZK_WHOLESALE_SHIPMENT_BACK, JSONObject.toJSONString(wholesaleShipment),
//                    wholesaleShipment.getBizOrgCode(), wholesaleShipment.getShipmentNo());
            SendResponse sendResponse = zKWholesaleShipmentBackSender.sendSync(JSONObject.toJSONString(wholesaleShipment).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
            log.info("中科请求创建批发出货单回传中科{}消息ID---{}", wholesaleShipment.getShipmentNo(), sendResponse.getMessageId());
        }
        if (StringUtils.isNotBlank(wholesaleShipment.getSourceNo()) && wholesaleShipment.getSourceNo().startsWith("HS")) {
            // 单据状态回传
            BackToHsBaseInfo backToHsBaseInfo = new BackToHsBaseInfo();
            backToHsBaseInfo.setOrderNo(wholesaleShipment.getSourceNo());
            backToHsBaseInfo.setErpOrderNo(wholesaleShipment.getShipmentNo());
            backToHsBaseInfo.setBizOrgCode(wholesaleShipment.getBizOrgCode());
            hsWholesaleShipmentDtsBackSender.sendSync(JSONObject.toJSONString(backToHsBaseInfo).getBytes(), System.currentTimeMillis() + SystemConstant.HS_DIFF_DELAY_TIME);
            // 获取HS差异发送延迟消息
            HSWholesaleDifferenceOrder hsWholesaleDifferenceOrder = wholesaleShipmentDetailService.initHsOrderDifference(wholesaleShipment.getId());
            if (Objects.nonNull(hsWholesaleDifferenceOrder)) {
                hsWholesaleDiffSender.sendSync(JSONObject.toJSONString(hsWholesaleDifferenceOrder).getBytes(), System.currentTimeMillis() + SystemConstant.HS_DIFF_DELAY_TIME);
            }
        }
        return "成功";
    }

    /**
     * 批量导出批发出货单
     *
     * @param queryShipmentIn 批发出货单查询入参
     * @return
     */
    @Override
    public String exportDetail(QueryShipmentIn queryShipmentIn) {
        //设置每次查询条数
        queryShipmentIn.setPageSize(5000);
        //excel 文件名
        String fileName = "批发出货单明细信息";
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx"),
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        fileName,
                        //导出模板实体
                        BatchExportShipmentOrderDetail.class,
                        // 分页查询对象
                        queryShipmentIn,
                        //excel对象集合
                        page -> {
                            List<BatchExportShipmentOrderDetail> batchExportShipmentOrderDetails = this.buildExcel(queryShipmentIn);
                            log.info("批发出货单明细导出集合大小是----------{}", batchExportShipmentOrderDetails.size());
                            return batchExportShipmentOrderDetails;
                        }

                )
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    @Override
    public QueryShipmentReportOut queryShipmentReportForPage(QueryShipmentIn queryShipmentIn) {
        //出货单结果集
        QueryShipmentReportOut queryShipmentReportOut = wholesaleShipmentMapper.queryShipmentReportForPage(queryShipmentIn);
        if (Objects.isNull(queryShipmentReportOut)) {
            queryShipmentReportOut = new QueryShipmentReportOut();
        }
        queryShipmentReportOut.setApplicationQuantity(Objects.isNull(queryShipmentReportOut.getApplicationQuantity()) ? NumberUtil.INTEGER_ZERO : queryShipmentReportOut.getApplicationQuantity());
        queryShipmentReportOut.setApplicationAmount(Objects.isNull(queryShipmentReportOut.getApplicationAmount()) ? BigDecimal.ZERO : queryShipmentReportOut.getApplicationAmount());
        queryShipmentReportOut.setShipmentQuantity(Objects.isNull(queryShipmentReportOut.getShipmentQuantity()) ? NumberUtil.INTEGER_ZERO : queryShipmentReportOut.getShipmentQuantity());
        queryShipmentReportOut.setPracticalShipmentAmount(Objects.isNull(queryShipmentReportOut.getPracticalShipmentAmount()) ? BigDecimal.ZERO : queryShipmentReportOut.getPracticalShipmentAmount());
        return queryShipmentReportOut;
    }

    @Override
    public ImportShipmentOrderListener importShipmentOrder(String fileId) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (null == bytes) {
            throw new BusinessException("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ImportShipmentOrderListener importShipmentOrderListener = new ImportShipmentOrderListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportShipmentOrder.class, importShipmentOrderListener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(0).build();
        excelReader.read(readSheet).finish();

        return importShipmentOrderListener;
    }

//    @Override
//    public Response<String> batchAudit(List<Long> shipmentIdList) {
//        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
//        Map<String, com.edc.erp.common.model.out.stock.StockInfoOut> stockInfoMap = stockServer.findAll(UserUtil.getBizOrgCode());
//        shipmentIdList.forEach(id -> {
//            WholesaleShipment wholesaleShipment = null;
//            try {
//                wholesaleShipment = wholesaleShipmentMapper.selectByPrimaryKey(id);
//                if (Objects.isNull(wholesaleShipment)) {
//                    log.error("批发出货{}不存在", id);
//                    return;
//                }
//                if (StringUtils.isNotBlank(wholesaleShipment.getBizOrgCode()) &&
//                        !UserUtil.getBizOrgCode().equals(wholesaleShipment.getBizOrgCode())) {
//                    errorJoiner.add(wholesaleShipment.getShipmentNo() + "此出货单无审核权限");
//                    return;
//                }
//                WholesaleShipmentDetail detailParameter = new WholesaleShipmentDetail();
//                detailParameter.setWholesaleShipmentId(id);
//                detailParameter.setIsDelete(ModelConst.DELETE.NO);
//                List<WholesaleShipmentDetail> wholesaleShipmentDetailList = wholesaleShipmentDetailService.list(detailParameter);
//                if (CollectionUtils.isEmpty(wholesaleShipmentDetailList)) {
//                    errorJoiner.add(wholesaleShipment.getShipmentNo() + "请至少录入一条出货单明细");
//                    return;
//                }
//                //审核
//                if (!ShipmentStatusEnum.PENDING.getCode().equals(wholesaleShipment.getShipmentStatus())) {
//                    errorJoiner.add(wholesaleShipment.getShipmentNo() + "只有待审核的出货单才可审核");
//                    return;
//                }
//                ShipmentWithDetailIn shipmentWithDetailIn = new ShipmentWithDetailIn();
//                shipmentWithDetailIn.setWholesaleShipment(wholesaleShipment);
//                WholesaleShipment finalWholesaleShipment = wholesaleShipment;
//                List<WholesaleShipmentDetailIn> wholesaleShipmentDetailIns = wholesaleShipmentDetailList.stream().map(wholesaleShipmentDetail -> {
//                    WholesaleShipmentDetailIn wholesaleShipmentDetailIn = new WholesaleShipmentDetailIn();
//                    BeanUtils.copy(wholesaleShipmentDetail, wholesaleShipmentDetailIn);
//                    wholesaleShipmentDetailIn.setClientCode(finalWholesaleShipment.getClientCode());
//                    wholesaleShipmentDetailIn.setShipmentWrh(finalWholesaleShipment.getShipmentWrh());
//                    wholesaleShipmentDetailIn.setStockCode(finalWholesaleShipment.getShipmentStockCode());
//                    com.edc.erp.common.model.out.stock.StockInfoOut stockInfoOut = stockInfoMap.get(finalWholesaleShipment.getShipmentStockCode());
//                    if (stockInfoOut == null) {
//                        String stockNoExistMsg = "批发出" + finalWholesaleShipment.getShipmentNo() + "下商品" + finalWholesaleShipment.getShipmentStockCode() + "仓位不存在";
//                        errorJoiner.add(stockNoExistMsg);
//                        throw new BusinessException(stockNoExistMsg);
//                    }
//                    wholesaleShipmentDetailIn.setStockId(stockInfoOut.getId());
//                    return wholesaleShipmentDetailIn;
//                }).collect(Collectors.toList());
//                shipmentWithDetailIn.setWholesaleShipmentDetailList(wholesaleShipmentDetailIns);
//                //规则校验
//                this.verifyDetailIsExist(shipmentWithDetailIn, ShipmentStatusEnum.APPROVED.getCode());
//                // 申请数对比可用库存
//                List<WholesaleShipmentDetail> updateAuditDetail = this.recalculateNeedUpdateDetailForAudit(shipmentWithDetailIn.getWholesaleShipmentDetailList(),
//                        wholesaleShipment);
//                if (CollectionUtils.isNotEmpty(updateAuditDetail)) {
//                    wholesaleShipmentDetailService.batchUpdateAudit(updateAuditDetail);
//                }
//                // 如果审核数都为0，整单作废
//                if (wholesaleShipment.getAuditQuantity().compareTo(NumberUtil.INTEGER_ZERO) == NumberUtil.INTEGER_ZERO) {
//                    log.info("批发出货单{}审核库存都为0，整单作废", wholesaleShipment.getShipmentNo());
//                    this.invalid(wholesaleShipment);
//                    return;
//                }
//                //添加已审核状态
//                wholesaleShipment.setShipmentStatus(ShipmentStatusEnum.APPROVED.getCode());
//                wholesaleShipment.setAuditTime(LocalDateTime.now());
//                //更新已审核状态
//                this.updateShipment(wholesaleShipment);
//                //获取初始化数据
//                this.initInventory(shipmentWithDetailIn);
//                //调整库存
//                List<StockFlowIn> stockFlowIns = this.addOrSubStock(shipmentWithDetailIn.getWholesaleShipment(),
//                        shipmentWithDetailIn.getWholesaleShipmentDetailList(), ShipmentStatusEnum.APPROVED.getCode());
//                //调用库存rpc调整库存
//                Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
//                if (!stockFlow.isSuccess()) {
//                    throw new BusinessException("库存调整失败");
//                }
//                //资管调用参数转换
//                if (!clientDistInfoService.getIsIndependentAccountingByCode(wholesaleShipment.getClientCode(), wholesaleShipment.getBizOrgCode())) {
//                    if (BigDecimal.ZERO.compareTo(wholesaleShipment.getAuditAmount().abs()) != 0) {
//                        List<FrozenOrderIn> frozenOrderIns = Lists.newArrayList();
//                        FrozenOrderIn frozenOrderIn = new FrozenOrderIn();
//                        frozenOrderIn.setBusinessNo(wholesaleShipment.getShipmentNo());
//                        frozenOrderIn.setAmount(wholesaleShipment.getAuditAmount().abs());
//                        frozenOrderIn.setBusinessType(FundTypeEnum.WHOLESALE_SHIPMENT_AUDIT.getCode());
//                        frozenOrderIns.add(frozenOrderIn);
//                        StoreFrozenIn storeFrozenIn = new StoreFrozenIn();
//                        storeFrozenIn.setFrozenOrders(frozenOrderIns);
//                        storeFrozenIn.setPrincipalCode(wholesaleShipment.getClientCode());
//                        storeFrozenIn.setPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
//                        storeFrozenIn.setBizOrgCode(wholesaleShipment.getBizOrgCode());
//                        Response response = asyncFundService.asyncFrozen(storeFrozenIn);
//                        if (!response.isSuccess()) {
//                            log.error("批发出货单{}冻结审核金额失败{}", wholesaleShipment.getShipmentNo(), response.getMessage());
//                            throw new BusinessException("资管冻结失败");
//                        }
//                    }
//                }
//                //审核之后批发出货单下发DTS
//                if (stockServer.isSendWms(wholesaleShipment.getShipmentStockCode(), wholesaleShipment.getBizOrgCode())) {
//                    //明细中文转换
//                    List<WholesaleShipmentDetailOut> detailOuts = wholesaleShipmentDetailIns.stream().map(detail -> {
//                        //明细结果集
//                        WholesaleShipmentDetailOut wholesaleShipmentDetailOut = new WholesaleShipmentDetailOut();
//                        //copy 同属性
//                        BeanUtils.copy(detail, wholesaleShipmentDetailOut);
//                        //品类属性 - 中文
//                        wholesaleShipmentDetailOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(detail.getGoodsType()));
//                        return wholesaleShipmentDetailOut;
//
//                    }).collect(Collectors.toList());
//                    ShipmentWithDetailOut shipmentWithDetailOut = new ShipmentWithDetailOut();
//                    BeanUtils.copy(wholesaleShipment, shipmentWithDetailOut);
//                    //结果集添加明细集合
//                    shipmentWithDetailOut.setWholesaleShipmentDetailOuts(detailOuts);
//                    this.wholesaleShipmentToDts(shipmentWithDetailOut);
//                }
//                //保存出货单审核日志
//                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
//                        OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
//                        String.valueOf(shipmentWithDetailIn.getWholesaleShipment().getId()),
//                        OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
//                        OperateLogTypeEnum.APPROVED.getName(),
//                        new Date(), shipmentWithDetailIn.getWholesaleShipment().getUpdater());
//                asyncLogService.sendAsyncSaveLogByMq(businessLog);
//            } catch (Exception e) {
//                errorJoiner.add(Objects.isNull(wholesaleShipment) ? id.toString() : wholesaleShipment.getShipmentNo() + "审核异常");
//                log.error("批发出ID{}审核异常", id.toString(), e);
//            }
//        });
//        if (errorJoiner.length() > 0) {
//            return Response.success("以下批发出审核失败：" + errorJoiner);
//        } else {
//            return Response.success();
//        }
//    }

    @Override
    public List<WholesaleShipmentDetail> recalculateNeedUpdateDetailForAudit(List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList,
                                                                             WholesaleShipment wholesaleShipment) {
        AtomicReference<Integer> auditQuantity = new AtomicReference<>(NumberUtil.INTEGER_ZERO);
        AtomicReference<BigDecimal> auditAmount = new AtomicReference<>(BigDecimal.ZERO);
        List<WholesaleShipmentDetail> updateAuditDetail = Lists.newArrayList();
        wholesaleShipmentDetailList.forEach(detail -> {
            BigDecimal stockQty;
            if (DistributionWaysEnum.TRANSFER.getType().equals(wholesaleShipment.getDistributionType())) {
                stockQty = BigDecimal.valueOf(detail.getApplyQuantity());
            } else {
                stockQty = ordDisDelivDifferenceMapper.checkStockInv(
                        wholesaleShipment.getBizOrgCode(),
                        detail.getGoodsCode(),
                        wholesaleShipment.getShipmentStockCode());
            }
            com.edc.erp.common.model.out.stock.StockInfoOut stockInfoOut = stockServer.getByCode(wholesaleShipment.getShipmentStockCode(), wholesaleShipment.getBizOrgCode());
            // 不允许负库存
            if (NumberUtil.INTEGER_ZERO.equals(stockInfoOut.getIsAllowNegativeStocks())) {
                stockQty = null != stockQty && stockQty.compareTo(BigDecimal.ZERO) >= NumberUtil.INTEGER_ZERO ? stockQty : BigDecimal.ZERO;
                log.info("批发单{}商品{}库存数{}", wholesaleShipment.getShipmentNo(), detail.getGoodsCode(), stockQty);
                BigDecimal auditQty = BigDecimal.valueOf(detail.getAuditQuantity()).abs();
                log.info("批发单{}商品{}审核数{}", wholesaleShipment.getShipmentNo(), detail.getGoodsCode(), auditQty);
                if (auditQty.compareTo(stockQty) == NumberUtil.INTEGER_ONE) {
                    detail.setAuditQuantity(Integer.valueOf(stockQty.intValue()));
                    detail.setAuditAmount(stockQty.multiply(detail.getUnitPrice()));
                    detail.setUpdateTime(LocalDateTime.now());
                    detail.setUpdater(wholesaleShipment.getUpdater());
                    updateAuditDetail.add(detail);
                    log.info("批发单{}商品{}审核数大于库存数,当前商品审核数为{}", wholesaleShipment.getShipmentNo(), detail.getGoodsCode(), detail.getAuditQuantity());
                }
            }
            auditQuantity.set(auditQuantity.get() + detail.getAuditQuantity());
            auditAmount.getAndSet(auditAmount.get().add(detail.getAuditAmount()));
        });
        wholesaleShipment.setAuditQuantity(auditQuantity.get());
        wholesaleShipment.setAuditAmount(auditAmount.get());
        return updateAuditDetail;
    }

    @Override
    public int countOneBySourceNo(String sourceNo, String bizOrgCode) {
        WholesaleShipment wholesaleShipment = new WholesaleShipment();
        wholesaleShipment.setSourceNo(sourceNo);
        wholesaleShipment.setBizOrgCode(bizOrgCode);
        wholesaleShipment.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleShipmentMapper.selectCount(wholesaleShipment);
    }

    @Override
    public WholesaleShipment getOneByNo(String shipmentNo, String bizOrgCode) {
        WholesaleShipment wholesaleShipment = new WholesaleShipment();
        wholesaleShipment.setShipmentNo(shipmentNo);
        wholesaleShipment.setBizOrgCode(bizOrgCode);
        wholesaleShipment.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleShipmentMapper.selectOne(wholesaleShipment);
    }

    @Override
    public List<Long> findNeedPushPurWholesaleShipmentId(QueryPushPurIn queryPushPurIn) {
        return wholesaleShipmentMapper.findNeedPushPurWholesaleShipmentId(queryPushPurIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePurBatchNumberByIdList(PushPurUpdateIn pushPurUpdateIn) {
        return wholesaleShipmentMapper.updatePurBatchNumberByIdList(pushPurUpdateIn);
    }

    @Override
    public String exportOrder(QueryShipmentIn queryShipmentIn) {
        //设置每次查询条数
        queryShipmentIn.setPageSize(5000);
        //excel 文件名
        String fileName = "批发出货单信息";
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx"),
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        fileName,
                        //导出模板实体
                        BatchExportShipmentOrder.class,
                        // 分页查询对象
                        queryShipmentIn,
                        //excel对象集合
                        page -> {
                            List<BatchExportShipmentOrder> batchExportShipmentOrders = this.buildExcelOrders(queryShipmentIn);
                            log.info("批发出货单列表导出集合大小是----------{}", batchExportShipmentOrders.size());
                            return batchExportShipmentOrders;
                        }

                )
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    @Override
    public WholesaleDateInfoOut sumWholesaleDateInfo(QueryShipmentIn queryShipmentIn) {
        WholesaleDateInfoOut totalWholesaleDateInfoOut = new WholesaleDateInfoOut();
        totalWholesaleDateInfoOut.setTotalApplyQuantity(NumberUtil.INTEGER_ZERO);
        totalWholesaleDateInfoOut.setTotalApplyAmount(BigDecimal.ZERO);
        totalWholesaleDateInfoOut.setTotalAuditQuantity(NumberUtil.INTEGER_ZERO);
        totalWholesaleDateInfoOut.setTotalAuditAmount(BigDecimal.ZERO);
        totalWholesaleDateInfoOut.setTotalShipmentQuantity(NumberUtil.INTEGER_ZERO);
        totalWholesaleDateInfoOut.setTotalPracticalShipmentAmount(BigDecimal.ZERO);
        List<Long> idList = wholesaleShipmentMapper.findNeedSumWholesaleOrderIdList(queryShipmentIn);
        if (CollectionUtils.isEmpty(idList)) {
            return totalWholesaleDateInfoOut;
        }
        int listSize = idList.size();
        for (int i = 0; i < listSize; i += batchSize) {
            // 获取当前批次的子列表
            int endIndex = Math.min(i + batchSize, listSize);
            List<Long> subIdList = idList.subList(i, endIndex);
            WholesaleDateInfoOut wholesaleDateInfoOut = wholesaleShipmentDetailService.sumWholesaleDateInfoByIdList(subIdList);
            if (Objects.nonNull(wholesaleDateInfoOut)) {
                if (null != wholesaleDateInfoOut.getTotalApplyQuantity()) {
                    totalWholesaleDateInfoOut.setTotalApplyQuantity(totalWholesaleDateInfoOut.getTotalApplyQuantity() + wholesaleDateInfoOut.getTotalApplyQuantity());
                    totalWholesaleDateInfoOut.setTotalApplyAmount(totalWholesaleDateInfoOut.getTotalApplyAmount().add(wholesaleDateInfoOut.getTotalApplyAmount()));
                }
                if (null != wholesaleDateInfoOut.getTotalAuditQuantity()) {
                    totalWholesaleDateInfoOut.setTotalAuditQuantity(totalWholesaleDateInfoOut.getTotalAuditQuantity() + wholesaleDateInfoOut.getTotalAuditQuantity());
                    totalWholesaleDateInfoOut.setTotalAuditAmount(totalWholesaleDateInfoOut.getTotalAuditAmount().add(wholesaleDateInfoOut.getTotalAuditAmount()));
                }
                if (null != wholesaleDateInfoOut.getTotalShipmentQuantity()) {
                    totalWholesaleDateInfoOut.setTotalShipmentQuantity(totalWholesaleDateInfoOut.getTotalShipmentQuantity() + wholesaleDateInfoOut.getTotalShipmentQuantity());
                    totalWholesaleDateInfoOut.setTotalPracticalShipmentAmount(totalWholesaleDateInfoOut.getTotalPracticalShipmentAmount()
                            .add(wholesaleDateInfoOut.getTotalPracticalShipmentAmount()));
                }
            }
        }
        totalWholesaleDateInfoOut.setTotalApplyAmount(totalWholesaleDateInfoOut.getTotalApplyAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        totalWholesaleDateInfoOut.setTotalAuditAmount(totalWholesaleDateInfoOut.getTotalAuditAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        totalWholesaleDateInfoOut.setTotalPracticalShipmentAmount(totalWholesaleDateInfoOut.getTotalPracticalShipmentAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        return totalWholesaleDateInfoOut;
    }

    @Override
    public ShipmentWithDetailIn initShipmentWithDetailInForBatchAudit(WholesaleShipment wholesaleShipment, String bizOrgCode) {
        ShipmentWithDetailIn shipmentWithDetailIn = new ShipmentWithDetailIn();
        wholesaleShipment.setDistributionType(DistributionWaysEnum.getNameByType(wholesaleShipment.getDistributionType()));
        //根据仓位code查询仓位信息和仓储信息
        com.edc.erp.common.model.out.stock.StockInfoOut stockInfoOut = stockServer.getTransInfo(wholesaleShipment.getShipmentStockCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException(wholesaleShipment.getShipmentStockCode() + "仓位不存在");
        }
        List<WholesaleShipmentDetail> list = wholesaleShipmentDetailService.findListByShipmentId(wholesaleShipment.getId());
        WholesaleShipment finalWholesaleShipment = wholesaleShipment;
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList = list.stream().map(detail -> {
            WholesaleShipmentDetailIn wholesaleShipmentDetailIn = new WholesaleShipmentDetailIn();
            BeanUtils.copy(detail, wholesaleShipmentDetailIn);
            wholesaleShipmentDetailIn.setClientCode(finalWholesaleShipment.getClientCode());
            wholesaleShipmentDetailIn.setStockId(stockInfoOut.getId());
            wholesaleShipmentDetailIn.setStockCode(finalWholesaleShipment.getShipmentStockCode());
            wholesaleShipmentDetailIn.setShipmentWrh(finalWholesaleShipment.getShipmentWrh());
            wholesaleShipmentDetailIn.setStockCode(finalWholesaleShipment.getShipmentStockCode());
            SaleGoodsInfoOut saleGoodsInfo = this.getSaleGoodsInfo(wholesaleShipmentDetailIn, bizOrgCode, finalWholesaleShipment.getPriceGroupCode());
            wholesaleShipmentDetailIn.setInTax(saleGoodsInfo.getInTax());
            wholesaleShipmentDetailIn.setOutTax(saleGoodsInfo.getOutTax());
            return wholesaleShipmentDetailIn;
        }).collect(Collectors.toList());
        shipmentWithDetailIn.setWholesaleShipment(wholesaleShipment);
        shipmentWithDetailIn.setWholesaleShipmentDetailList(wholesaleShipmentDetailList);
        return shipmentWithDetailIn;
    }

    @Override
    public WholesaleShipment getOneByNo(String shipmentNo) {
        WholesaleShipment wholesaleShipment = new WholesaleShipment();
        wholesaleShipment.setShipmentNo(shipmentNo);
        wholesaleShipment.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleShipmentMapper.selectOne(wholesaleShipment);
    }

    @Override
    public boolean checkIsHsShipment(String sourceNo) {
        if (StringUtils.isBlank(sourceNo)) {
            return false;
        }
        return sourceNo.startsWith("HS");
    }

    @Override
    public String replaceStr(String str) {
        return StringUtils.isBlank(str) ? "" : str.replace(SystemConstant.VERTICAL_BAR, "");
    }

    @Override
    public List<Long> findNeedDelayPushPurWholesaleShipmentId(QueryPushPurIn queryPushPurIn) {
        return wholesaleShipmentMapper.findNeedDelayPushPurWholesaleShipmentId(queryPushPurIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePushPurTime(WholesaleShipment wholesaleShipment) {
        return wholesaleShipmentMapper.updatePushPurTime(wholesaleShipment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateShipmentPurchaseNoByPurBatchNumber(TransferShipmentPushPurchaseBackVO transferShipmentPushPurchaseBackVO, String updater, LocalDateTime updateTime) {
        wholesaleShipmentDetailService.updatePurchaseNoByPurBatchNumber(transferShipmentPushPurchaseBackVO, updater, updateTime);
        wholesaleShipmentMapper.updateShipmentPurchaseNoByPurBatchNumber(transferShipmentPushPurchaseBackVO, updater, updateTime);
        return 1;
    }

    private List<BatchExportShipmentOrder> buildExcelOrders(QueryShipmentIn queryShipmentIn) {
        List<ShipmentWithDetailOut> wholesaleShipmentOuts = wholesaleShipmentMapper.findShipmentByPage(queryShipmentIn);
        if (CollectionUtils.isEmpty(wholesaleShipmentOuts)) {
            log.info("无单据可导出");
            return Lists.newArrayList();
//            throw new BusinessException("无单据可导出");
        }
        wholesaleShipmentOuts.forEach(item -> {
            this.addClientDistInfoAndShipmentConvert(item, null);
        });

        List<BatchExportShipmentOrder> exportShipmentOrders = new ArrayList<>();
        for (ShipmentWithDetailOut wholesaleShipmentOut : wholesaleShipmentOuts) {
            BatchExportShipmentOrder exportShipmentOrder = new BatchExportShipmentOrder();
            BeanUtils.copy(wholesaleShipmentOut, exportShipmentOrder);

            ClientDistInfoOut clientDistInfoOut = wholesaleShipmentOut.getClientDistInfoOut();
            exportShipmentOrder.setClientCodeStr(clientDistInfoOut.getClientCodeStr());
            exportShipmentOrder.setPriceGroupCodeStr(clientDistInfoOut.getPriceGroupCodeStr());
            exportShipmentOrder.setDistributionWayStr(clientDistInfoOut.getDistributionWayStr());
            exportShipmentOrder.setWarehouseStr("【" + wholesaleShipmentOut.getShipmentWrh() + "】" + wholesaleShipmentOut.getWarehouseName());
            exportShipmentOrder.setStockStr("【" + wholesaleShipmentOut.getShipmentStockCode() + "】" + wholesaleShipmentOut.getStockName());
            exportShipmentOrder.setIsReversalOrderStr(NumberUtil.INTEGER_ONE.equals(wholesaleShipmentOut.getIsReversalOrder()) ? "是" : "否");
            exportShipmentOrder.setIsReversalStr(NumberUtil.INTEGER_ONE.equals(wholesaleShipmentOut.getIsReversal()) ? "是" : "否");
            exportShipmentOrders.add(exportShipmentOrder);
        }
        return exportShipmentOrders;
    }

    /**
     * 构建批量导出excel对象集合
     *
     * @param queryShipmentIn 批发出货单查询入参
     * @return
     */
    private List<BatchExportShipmentOrderDetail> buildExcel(QueryShipmentIn queryShipmentIn) {
        //根据条件查询 出货单
        List<ShipmentWithDetailOut> outList = wholesaleShipmentMapper.findShipmentByPage(queryShipmentIn);
        if (CollectionUtils.isEmpty(outList)) {
            log.info("无单据明细可导出");
            return Lists.newArrayList();
//            throw new BusinessException("无单据明细可导出");
        }
        //按照出货单id 转换map
        Map<Long, ShipmentWithDetailOut> outListMap = outList.stream().filter(s -> Objects.nonNull(s.getId())).collect(Collectors.toMap(ShipmentWithDetailOut::getId, item -> item));
        //根据出货单id集合 查询明细
        QueryShipmentDetailIn queryShipmentDetailIn = new QueryShipmentDetailIn();
        queryShipmentDetailIn.setShipmentWithDetailOuts(outList);
        queryShipmentDetailIn.setIsDelete(ModelConst.DELETE.NO);
        //根据出货单id集合 查询明细
        List<WholesaleShipmentDetailIn> detailList = wholesaleShipmentDetailService.findShipmentDetailList(queryShipmentDetailIn);
        if (CollectionUtils.isEmpty(detailList)) {
            log.info("单据下的明细不存在");
            return Lists.newArrayList();
//            throw new BusinessException("单据下的明细不存在");
        }
        //处理合并 excel对象集合
        List<BatchExportShipmentOrderDetail> batchExportShipmentOrderDetails = new ArrayList<>();
        for (WholesaleShipmentDetailIn detail : detailList) {
            //copy 同属性
            BatchExportShipmentOrderDetail batchOrder = new BatchExportShipmentOrderDetail();
            BeanUtils.copy(detail, batchOrder);
            //获取单头信息
            ShipmentWithDetailOut shipmentWithDetailOut = outListMap.get(detail.getWholesaleShipmentId());
            batchOrder.setAuditTime(shipmentWithDetailOut.getAuditTime());
            //添加单号
            batchOrder.setShipmentNo(shipmentWithDetailOut.getShipmentNo());
            //添加单据状态 转中文
            batchOrder.setShipmentStatusStr(ShipmentStatusEnum.getNameByCode(shipmentWithDetailOut.getShipmentStatus()));
            //品类属性 转中文
            batchOrder.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(detail.getGoodsType()));
            batchOrder.setSourceNo(shipmentWithDetailOut.getSourceNo());
            batchOrder.setRemark(shipmentWithDetailOut.getRemark());
            batchOrder.setDeliveryTime(shipmentWithDetailOut.getDeliveryTime());
            //添加至excel对象集合
            batchExportShipmentOrderDetails.add(batchOrder);
        }

        return batchExportShipmentOrderDetails;
    }

    /**
     * 查询允许批发出入参初始化
     *
     * @param dbShipment 出货单结果集
     * @param detailIn   出货单明细入参实体
     */
    private void initSaleGoodsInfo(WholesaleShipment dbShipment, WholesaleShipmentDetailIn detailIn) {
        //查询仓位信息
        com.edc.erp.common.model.out.stock.StockInfoOut stockInfo = stockServer.getByCode(
                dbShipment.getShipmentStockCode(), dbShipment.getBizOrgCode());
        //仓位id
        detailIn.setStockId(Objects.isNull(stockInfo) ? null : stockInfo.getId());
        //仓位code
        detailIn.setStockCode(dbShipment.getShipmentStockCode());
        //仓储code
        detailIn.setShipmentWrh(dbShipment.getShipmentWrh());
        //客户代码
        detailIn.setClientCode(dbShipment.getClientCode());
    }

    /**
     * 审核初始化扣减库存数据
     *
     * @param shipmentWithDetailIn 批发出货单入参
     * @return
     */
    public void initInventory(ShipmentWithDetailIn shipmentWithDetailIn) {
        //获取批发出货单
        WholesaleShipment wholesaleShipment = shipmentWithDetailIn.getWholesaleShipment();
        //  获取明细
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList = shipmentWithDetailIn.getWholesaleShipmentDetailList();
        for (WholesaleShipmentDetailIn detailIn : wholesaleShipmentDetailList) {
            //查询商品信息入参初始化
            this.initSaleGoodsInfo(shipmentWithDetailIn.getWholesaleShipment(), detailIn);
            //查询商品信息
            SaleGoodsInfoOut saleGoodsInfo = this.getSaleGoodsInfo(detailIn, wholesaleShipment.getBizOrgCode(), null);
            //获取审核数量
            Integer auditQuantity = detailIn.getAuditQuantity();
            //销项税率 如果为null 则为 1 + 0 否则 税率 + 1
            BigDecimal outTaxAddOne = Objects.nonNull(saleGoodsInfo) && Objects.nonNull(saleGoodsInfo.getOutTax()) ? saleGoodsInfo.getOutTax().add(BigDecimal.ONE) : BigDecimal.ZERO.add(BigDecimal.ONE);
            //税率取消 + 1
            BigDecimal outTax = Objects.nonNull(saleGoodsInfo) && Objects.nonNull(saleGoodsInfo.getOutTax()) ? saleGoodsInfo.getOutTax() : BigDecimal.ZERO;
            //最新库存价
            BigDecimal inventoryPrice = Objects.isNull(saleGoodsInfo) || Objects.isNull(saleGoodsInfo.getInventoryPrice()) ? BigDecimal.ZERO : saleGoodsInfo.getInventoryPrice();
            detailIn.setInventoryPrice(inventoryPrice);
            //出库数量即审核数量
            detailIn.setShipmentQuantity(auditQuantity);
            //出库包装数
            String shipmentPackageQuantity = this.getShipmentPackageQuantity(saleGoodsInfo, auditQuantity);
            detailIn.setShipmentPackageQuantity(shipmentPackageQuantity);
            //计算实际出库金额
            BigDecimal practicalShipmentAmount = detailIn.getUnitPrice().multiply(BigDecimal.valueOf(auditQuantity));
            detailIn.setPracticalShipmentAmount(practicalShipmentAmount);
            //出库去税金额 四舍五入保留两位
            BigDecimal shipmentNetProfit = practicalShipmentAmount.divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            detailIn.setShipmentNetProfit(shipmentNetProfit);
            //出库税额 四舍五入保留两位
            detailIn.setShipmentTax(practicalShipmentAmount.subtract(shipmentNetProfit).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //成本金额 四舍五入保留两位
            BigDecimal costAmount = inventoryPrice.multiply(BigDecimal.valueOf(auditQuantity)).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP).negate();
            detailIn.setCostAmount(costAmount);
            //成本去税金额 四舍五入保留两位
            BigDecimal costNetProfitAmount = costAmount.divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            detailIn.setCostNetProfitAmount(costNetProfitAmount);
            //成本税额 四舍五入保留两位
            detailIn.setCostTax(costAmount.subtract(costNetProfitAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //更新时间
            detailIn.setUpdateTime(LocalDateTime.now());
        }
        //根据处理之后的明细初始化出货单
        this.initShipmentOrder(shipmentWithDetailIn);
    }


    /**
     * 根据批发出货单id查询出货单
     *
     * @param wholesaleShipmentId 批发出货单id
     * @param bizOrgCode          业务组织
     * @return
     */
    private WholesaleShipment getShipmentById(Long wholesaleShipmentId, String bizOrgCode) {
        //根据主键查询出货单
        WholesaleShipment dbShipment = wholesaleShipmentMapper.selectByPrimaryKey(wholesaleShipmentId);
        if (Objects.isNull(dbShipment)) {
            throw new BusinessException("批发出货单不存在");
        }
        if (!bizOrgCode.equals(dbShipment.getBizOrgCode())) {
            throw new BusinessException("该组织下批发出货单不存在");
        }

        return dbShipment;
    }

    /**
     * 添加客户配送信息 并 出货单中文转换
     *
     * @param item      出货单结果集
     * @param detailIns
     */
    public void addClientDistInfoAndShipmentConvert(ShipmentWithDetailOut item, List<WholesaleShipmentDetailIn> detailIns) {
        //出货状态 中文转换
        item.setShipmentStatusStr(ShipmentStatusEnum.getNameByCode(item.getShipmentStatus()));
        item.setDistributionTypeStr(DistributionWaysEnum.getNameByType(item.getDistributionType()));
        /** 客户信息 客户配送信息和客户价格组中文转换 */
        if (StringUtils.isNotBlank(item.getClientCode())) {
            //查询客户信息 入参
            QueryClientDistInfoIn queryClientDistInfoIn = new QueryClientDistInfoIn();
            //添加客户代码
            queryClientDistInfoIn.setClientCode(item.getClientCode());
            //添加业务组织
            queryClientDistInfoIn.setBizOrgCode(item.getBizOrgCode());
            //添加客户配送信息
            List<ClientDistInfoOut> clientDistInfo = clientDistInfoService.findClientDistInfo(queryClientDistInfoIn);
            item.setClientDistInfoOut(CollectionUtils.isNotEmpty(clientDistInfo) ? clientDistInfo.get(0) : null);
        }
        if (CollectionUtils.isEmpty(detailIns)) {
            return;
        }
        Map<String, StockWarehouseOut> stockInvMap;
        Map<String, List<StandardSpecOut>> goodsStandardSpecMap = new HashMap<>();
        if (ShipmentStatusEnum.PENDING.getCode().equals(item.getShipmentStatus())) {
            List<String> goodsCodeList = detailIns.stream().map(WholesaleShipmentDetailIn::getGoodsCode).collect(Collectors.toList());
            stockInvMap = warehouseServer.findStockInv(item.getShipmentStockCode(), goodsCodeList, item.getBizOrgCode());
            // 查询商品规格集合
            Response<Map<String, List<StandardSpecOut>>> response = saleGoodsInfoClient.findByGoodsCodes(goodsCodeList);
            if (null != response && response.isSuccess()) {
                goodsStandardSpecMap = response.getData();
            }
        } else {
            stockInvMap = new HashMap<>();
        }

        //明细中文转换
        Map<String, List<StandardSpecOut>> finalGoodsStandardSpecMap = goodsStandardSpecMap;
        List<WholesaleShipmentDetailOut> detailOuts = detailIns.stream().map(detail -> {
            //明细结果集
            WholesaleShipmentDetailOut wholesaleShipmentDetailOut = new WholesaleShipmentDetailOut();
            //copy 同属性
            BeanUtils.copy(detail, wholesaleShipmentDetailOut);
            //品类属性 - 中文
            wholesaleShipmentDetailOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(detail.getGoodsType()));
            StockWarehouseOut stockWarehouseOut = stockInvMap.get(detail.getGoodsCode());
            wholesaleShipmentDetailOut.setBusinessQty(Objects.nonNull(stockWarehouseOut) ? stockWarehouseOut.getBusinessQty() : null);
            wholesaleShipmentDetailOut.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(detail.getInvoiceType()));
            wholesaleShipmentDetailOut.setStandardSpecs(finalGoodsStandardSpecMap.get(detail.getGoodsCode()));
            return wholesaleShipmentDetailOut;

        }).collect(Collectors.toList());

        //结果集添加明细集合
        item.setWholesaleShipmentDetailOuts(detailOuts);
    }

}
