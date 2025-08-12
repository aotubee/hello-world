package com.edc.erp.wholesale.returns.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.handel.SyncOrdDisOrderHandle;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.customer.QueryClientDistInfoIn;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.in.returns.ClientWholesaleConfigIn;
import com.edc.erp.common.model.out.InvBizRsnTransOut;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.returns.ClientWholesaleConfigOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.rpc.SaleGoodsInfoClient;
import com.edc.erp.common.service.*;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.erp.enumeration.*;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.wholesale.model.excel.returns.ExportWholesaleReturnDetail;
import com.edc.erp.wholesale.model.excel.returns.ImportWholesaleReturnDetail;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnDetailFilterIn;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnsDetailIn;
import com.edc.erp.wholesale.model.in.shipment.BackToHsBaseInfo;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.model.listener.returns.ImportWholesaleReturnDetailListener;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnAndDetailOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDateInfoOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDetailAndGoodsStrOut;
import com.edc.erp.wholesale.model.out.shipment.ShipmentWithDetailOut;
import com.edc.erp.wholesale.model.out.shipment.WholesaleShipmentDetailOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.returns.mapper.WholesaleReturnDetailMapper;
import com.edc.erp.wholesale.returns.service.WholesaleReturnDetailService;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsGoodsService;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.model.order.in.WholesaleReBillDtlIn;
import com.edc.sdk.dts.model.order.in.WholesaleReBillIn;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * 批发退货明细单(WholesaleReturnDetail)表服务实现类
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WholesaleReturnDetailServiceImpl extends BaseServiceImpl<WholesaleReturnDetail> implements WholesaleReturnDetailService {

    @Autowired
    private WholesaleReturnDetailMapper wholesaleReturnDetailMapper;
    @Autowired
    private WholesaleReturnsService wholesaleReturnsService;
    @Autowired
    private WholesaleShipmentService wholesaleShipmentService;
    @Autowired
    private SyncOrdDisOrderHandle syncOrdDisOrderHandle;
    @Autowired
    private AsyncPushTaskService asyncPushTaskService;
    @Autowired
    private WholesaleService wholesaleService;
    @Autowired
    private SaleGoodsInfoClient saleGoodsInfoClient;
    @Autowired
    private AsyncLogService asyncLogService;
    @Autowired
    private FileService fileService;
    @Autowired
    private OrderGoodsServer orderGoodsServer;
    @Autowired
    private StockServer stockServer;

    private final WholesaleReturnsGoodsService wholesaleReturnsGoodsService;

    private final StockFlowService stockFlowService;

    private final ClientDistInfoService clientDistInfoService;

    private final EquipmentBusinessReasonServer equipmentBusinessReasonServer;

    private final RedisService redisService;

    private static final String CHECK_WHOLESALE_RETURN_AUDIT = "checkWholesaleReturnAudit:";
    @Qualifier("wholesaleReturnToDtsSender")
    private final MessageSender wholesaleReturnToDtsSender;

    @Autowired
    @Qualifier("hsWholesaleRefuseReturnSender")
    private MessageSender hsWholesaleRefuseReturnSender;

    /**
     * 根据条件查询批发退货详情
     *
     * @param wholesaleReturnDetail
     * @return
     */
    @Override
    public List<WholesaleReturnDetail> findWholesaleReturnDetail(WholesaleReturnDetail wholesaleReturnDetail) {
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnDetailMapper.select(wholesaleReturnDetail);
        return wholesaleReturnDetailList;
    }

    /**
     * 批量保存批发退货单详情
     *
     * @param wholesaleReturnDetailList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertWholesaleReturnDetailList(List<WholesaleReturnDetail> wholesaleReturnDetailList) {
        wholesaleReturnDetailMapper.insertWholesaleReturnDetailList(wholesaleReturnDetailList);
    }

    /**
     * 更新批发退货单详情
     *
     * @param wholesaleReturnDetail
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(WholesaleReturnDetail wholesaleReturnDetail) {
        return wholesaleReturnDetailMapper.updateByPrimaryKey(wholesaleReturnDetail);
    }

    /**
     * 审核批发退货单
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response auditWholesaleReturn(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        wholesaleReturns.setBizOrgCode(UserUtil.getBizOrgCode());
        wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
        WholesaleReturns wholesaleReturnsTemp = wholesaleReturnsService.selectOne(wholesaleReturns);
        if (ObjectUtil.isNull(wholesaleReturnsTemp)) {
            //执行新增逻辑
            Response response = wholesaleReturnsService.saveWholesaleReturnsDetail(wholesaleReturnsDetailIn);
            if (!response.isSuccess()) {
                return Response.error(response.getMessage());
            }
        }
        String key = CHECK_WHOLESALE_RETURN_AUDIT + wholesaleReturnsTemp.getBizOrgCode() +
                SystemConstant.COLON + wholesaleReturnsTemp.getClientCode() + SystemConstant.WAIT + wholesaleReturnsTemp.getWholesaleReturnNo();
        if (!redisService.setIfAbsent(key, wholesaleReturnsTemp.getWholesaleReturnNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("短时间内重复审核" + wholesaleReturnsTemp.getWholesaleReturnNo());
        }
        if (!wholesaleReturnsTemp.getReturnStatus().equals(ShipmentStatusEnum.PENDING.getCode())) {
            return Response.error("当前订单不能进行审核操作");
        }
        //校验当前信息
        Response response = wholesaleReturnsService.checkWholesaleReturn(wholesaleReturnsDetailIn);
        if (response.isSuccess()) {
            wholesaleReturns.setReturnStatus(ShipmentStatusEnum.APPROVED.getCode());
            //设置更新时间作为审核时间
            wholesaleReturns.setUpdateTime(LocalDateTime.now());
            wholesaleReturns.setAuditTime(LocalDateTime.now());
            //更新批发退货信息
            wholesaleReturnsService.updateByPrimaryKeySelective(wholesaleReturns);

            List<WholesaleReturnDetail> wholesaleReturnDetailList = (List<WholesaleReturnDetail>) response.getData();
            //物理删除之前的退货单详情信息
            for (WholesaleReturnDetail wholesaleReturnDetail : wholesaleReturnDetailList) {
                wholesaleReturnDetailMapper.deleteByPrimaryKey(wholesaleReturnDetail);
            }
            //批量新增详情
            AtomicInteger line = new AtomicInteger(NumberUtil.INTEGER_ONE);
            Map<String, Integer> goodsLineMap = new HashMap<>();
            wholesaleReturnDetailList.forEach(wholesaleReturnDetail -> {
                wholesaleReturnDetail.setId(null);
                wholesaleReturnDetail.setWholesaleReturnId(wholesaleReturns.getId());
                wholesaleReturnDetail.setIsDelete(ModelConst.DELETE.NO);
                wholesaleReturnDetail.setLine(line.get());
                if (Objects.isNull(wholesaleReturnDetail.getInventoryPrice())) {
                    wholesaleReturnDetail.setInventoryPrice(wholesaleReturnDetail.getReturnsPrice());
                }
                goodsLineMap.put(wholesaleReturnDetail.getGoodsCode(), wholesaleReturnDetail.getLine());
                line.incrementAndGet();
            });
            wholesaleReturnDetailMapper.insertWholesaleReturnDetailList(wholesaleReturnDetailList);
            // 重新赋值行号
            wholesaleReturnsDetailIn.getWholesaleReturnDetailList().forEach(wholesaleReturnDetail -> wholesaleReturnDetail.setLine(goodsLineMap.get(wholesaleReturnDetail.getGoodsCode())));
            wholesaleReturnsDetailIn.setStockCode(wholesaleReturnsTemp.getStorageStockCode());
            //发送消息到DTS
            if (stockServer.isSendWms(wholesaleReturnsTemp.getStorageStockCode(), wholesaleReturnsTemp.getBizOrgCode())) {
                sendMessageToDTS(wholesaleReturnsDetailIn);
            }
            //保存日志
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                    String.valueOf(wholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(), "审核批发退货单",
                    new Date(), wholesaleReturns.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.success();
        }
        return Response.error(response.getMessage());
    }

    /**
     * 作废批发退货单
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response invalidWholesaleReturnDetail(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
        wholesaleReturns.setBizOrgCode(UserUtil.getBizOrgCode());
        WholesaleReturns wholesaleReturnsTemp = wholesaleReturnsService.selectOne(wholesaleReturns);
        if (ObjectUtil.isEmpty(wholesaleReturnsTemp)) {
            return Response.error("当前订单不存在");
        }
        if (wholesaleReturnsTemp.getReturnStatus().equals(ShipmentStatusEnum.PENDING.getCode()) || wholesaleReturnsTemp.getReturnStatus().equals(ShipmentStatusEnum.APPROVED.getCode())) {
        } else {
            return Response.error("当前订单不可作废");
        }
        //更新批发退货单
        wholesaleReturnsTemp.setReturnStatus(ShipmentStatusEnum.INVALID.getCode());
        wholesaleReturnsService.updateByPrimaryKeySelective(wholesaleReturnsTemp);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                String.valueOf(wholesaleReturnsTemp.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(), "作废批发退货单",
                new Date(), wholesaleReturnsTemp.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        if (StringUtils.isNotBlank(wholesaleReturns.getSourceNo()) && wholesaleReturns.getSourceNo().startsWith("HS")) {
            // 单据状态回传 HS拒绝退货
            BackToHsBaseInfo backToHsBaseInfo = new BackToHsBaseInfo();
            backToHsBaseInfo.setOrderNo(wholesaleReturns.getSourceNo());
            backToHsBaseInfo.setErpOrderNo(wholesaleReturns.getWholesaleReturnNo());
            backToHsBaseInfo.setBizOrgCode(wholesaleReturns.getBizOrgCode());
            hsWholesaleRefuseReturnSender.sendSync(JSONObject.toJSONString(backToHsBaseInfo).getBytes(),System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
        }
        return Response.success();
    }

    /**
     * 删除批发退货详情数据
     *
     * @param wholesaleReturnDetail
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer delete(WholesaleReturnDetail wholesaleReturnDetail) {
        return wholesaleReturnDetailMapper.delete(wholesaleReturnDetail);
    }

    /**
     * 根据商品代码或者商品名称筛查订单物品
     *
     * @param wholesaleReturnDetailFilterIn
     * @return
     */
    @Override
    public Response<WholesaleReturnAndDetailOut> filterWholesaleReturnDetail(WholesaleReturnDetailFilterIn wholesaleReturnDetailFilterIn) {
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnDetailFilterIn.getWholesaleReturnDetailList();
        Long wholesaleReturnId = wholesaleReturnDetailFilterIn.getWholesaleReturnId();
        String goodsCode = wholesaleReturnDetailFilterIn.getGoodsCode();
        String goodsName = wholesaleReturnDetailFilterIn.getGoodsName();
        List<WholesaleReturnDetail> filterList = new ArrayList<>();
        WholesaleReturnAndDetailOut wholesaleReturnDetailOut = new WholesaleReturnAndDetailOut();
        //判断有没有批发退货单主键id---没有退货单主键证明没有保存下进行筛查
        if (ObjectUtil.isEmpty(wholesaleReturnId)) {
            if (StrUtil.isBlank(goodsCode) && StrUtil.isBlank(goodsName)) {
                filterList = wholesaleReturnDetailList;
            }
            if (StrUtil.isNotBlank(goodsCode) && StrUtil.isNotBlank(goodsName)) {
                filterList = wholesaleReturnDetailList.stream().filter(c -> c.getGoodsCode().equals(goodsCode)).filter(c -> c.getGoodsName().contains(goodsName)).collect(Collectors.toList());
            }
            if (StrUtil.isNotBlank(goodsCode) && StrUtil.isBlank(goodsName)) {
                filterList = wholesaleReturnDetailList.stream().filter(c -> c.getGoodsCode().equals(goodsCode)).collect(Collectors.toList());
            }
            if (StrUtil.isBlank(goodsCode) && StrUtil.isNotBlank(goodsName)) {
                filterList = wholesaleReturnDetailList.stream().filter(c -> c.getGoodsName().contains(goodsName)).collect(Collectors.toList());
            }
        } else {
            //根据主键查询批发退货单信息
            WholesaleReturns wholesaleReturns = wholesaleReturnsService.selectOne(WholesaleReturns.builder().id(wholesaleReturnId).build());
            BeanUtil.copyProperties(wholesaleReturns, wholesaleReturnDetailOut);
            filterList = wholesaleReturnDetailMapper.findFilterWholesaleReturnDetailByPage(wholesaleReturnId, goodsCode, goodsName);
        }
        //商品品类属性转中文
        List<WholesaleReturnDetailAndGoodsStrOut> wholesaleReturnDetailAndStrOutList = new ArrayList<>();
        for (WholesaleReturnDetail returnDetail : filterList) {
            WholesaleReturnDetailAndGoodsStrOut wholesaleReturnDetailAndStrOut = new WholesaleReturnDetailAndGoodsStrOut();
            BeanUtil.copyProperties(returnDetail, wholesaleReturnDetailAndStrOut);
            wholesaleReturnDetailAndStrOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(returnDetail.getGoodsType()));
            wholesaleReturnDetailAndStrOutList.add(wholesaleReturnDetailAndStrOut);
        }
        wholesaleReturnDetailOut.setWholesaleReturnDetailList(wholesaleReturnDetailAndStrOutList);
        //退货状态转中文
        wholesaleReturnDetailOut.setReturnStatusStr(ShipmentStatusEnum.getNameByCode(wholesaleReturnDetailOut.getReturnStatus()));
        //退货原因转中文
        wholesaleReturnDetailOut.setReturnsReasonStr(ReturnReasonEnum.getNameByCode(wholesaleReturnDetailOut.getReturnsReason()));
        Page page = new Page<>(wholesaleReturnDetailFilterIn);
        page.setList(filterList);
        return Response.data(wholesaleReturnDetailOut);
    }

    /**
     * 冲销批发退货单详情
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response writeOffWholesaleReturnDetail(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        WholesaleReturns wholesaleReturns = wholesaleReturnsService.selectByPrimaryKey(wholesaleReturnsDetailIn.getWholesaleReturns().getId());
        //已收货状态、是否冲销单为否且冲销标识字段为否的单据可以冲销，原单仍为已收货不变且冲销标识为是
        if (!wholesaleReturns.getReturnStatus().equals(ShipmentStatusEnum.RECEIPT.getCode()) ||
                wholesaleReturns.getIsReversalOrder().equals(ReversalEnum.IS_REVERSAL_ORDER_TRUE.getCode()) ||
                wholesaleReturns.getIsReversal().equals(ReversalEnum.IS_REVERSAL_TRUE.getCode())) {
            return Response.error("当前订单不可进行冲销");
        }

        //将原单冲销标识设置为是
        wholesaleReturns.setIsReversal(ReversalEnum.IS_REVERSAL_TRUE.getCode());
        //更新原单
        wholesaleReturnsService.updateByPrimaryKeySelective(wholesaleReturns);

        //生成冲销单
        WholesaleReturns writeOffWholesaleReturns = this.saveNewWholesaleReturns(wholesaleReturns);
        //冲销扣减库存 使用冲销之后的单号
        wholesaleReturnsDetailIn.getWholesaleReturns().setWholesaleReturnNo(writeOffWholesaleReturns.getWholesaleReturnNo());

        //查询当前退货单是否关联出货单并将出货单明细转换map
        Map<String, WholesaleShipmentDetailIn> shipmentMap = wholesaleReturnsService.getShipmentByWholesaleShipmentNo(wholesaleReturns.getWholesaleShipmentNo(), wholesaleReturns.getBizOrgCode());
        //查询冲销批发退货单主键
        Long wholesaleReturnId = writeOffWholesaleReturns.getId();
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnsDetailIn.getWholesaleReturnDetailList();
        List<WholesaleReturnDetail> writeOffWholesaleReturnDetailList = new ArrayList<>();
        for (WholesaleReturnDetail wholesaleReturnDetail : wholesaleReturnDetailList) {
            WholesaleReturnDetail writeOffWholesaleReturnDetail = new WholesaleReturnDetail();
            BeanUtil.copyProperties(wholesaleReturnDetail, writeOffWholesaleReturnDetail);
            writeOffWholesaleReturnDetail.setWholesaleReturnId(wholesaleReturnId);
            //入库数量设置为负数
            writeOffWholesaleReturnDetail.setStorageQuantity(ObjectUtil.isNull(wholesaleReturnDetail.getStorageQuantity()) ? 0 : NumberUtil.INTEGER_ZERO - wholesaleReturnDetail.getStorageQuantity());
            QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
            querySaleGoodsInfoIn.setGoodsCode(wholesaleReturnDetail.getGoodsCode());
            querySaleGoodsInfoIn.setStockCode(wholesaleReturnsDetailIn.getStockCode());
            querySaleGoodsInfoIn.setBizOrgCode(UserUtil.getBizOrgCode());
            querySaleGoodsInfoIn.setWarehouseCode(wholesaleReturnsDetailIn.getWarehouseCode());
            querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.RETURNS.getCode());
            //校验商品信息
            String clientCode = wholesaleReturns.getClientCode();
            String stockCode = wholesaleReturnsDetailIn.getStockCode();
            String warehouseCode = wholesaleReturnsDetailIn.getWarehouseCode();
            Integer stockId = wholesaleReturnsDetailIn.getStockId();
            //根据商品代码获取对应的出货单明细
            WholesaleShipmentDetailIn detailIn = shipmentMap.get(wholesaleReturnDetail.getGoodsCode());
            Response response = wholesaleReturnsGoodsService.checkWholesaleReturnDetail(writeOffWholesaleReturnDetail, clientCode, stockCode, warehouseCode, stockId, detailIn, null);
            if (!response.isSuccess()) {
                return Response.error(response.getMessage());
            }
            WholesaleReturnDetail data = (WholesaleReturnDetail) response.getData();
            //实际入库金额
            data.setPracticalStorageAmount(wholesaleReturnDetail.getPracticalStorageAmount().negate());
            //入库去税金额
            data.setStorageNetProfit(wholesaleReturnDetail.getStorageNetProfit().negate());
            //入库税额
            data.setStorageTax(wholesaleReturnDetail.getStorageTax().negate());
            //成本金额
            data.setCostAmount(wholesaleReturnDetail.getCostAmount().negate());
            //成本去税金额
            data.setCostNetProfitAmount(wholesaleReturnDetail.getCostNetProfitAmount().negate());
            //成本税额
            data.setCostTax(wholesaleReturnDetail.getCostTax().negate());
            writeOffWholesaleReturnDetailList.add(data);
        }
        wholesaleReturnsDetailIn.setWholesaleReturns(writeOffWholesaleReturns);
        wholesaleReturnsDetailIn.setWholesaleReturnDetailList(writeOffWholesaleReturnDetailList);
        //调用统计方法统计参数
        wholesaleReturnsDetailIn = wholesaleReturnsService.countParam(wholesaleReturnsDetailIn);
        //将统计数据更新进数据库
        wholesaleReturnsService.updateByPrimaryKeySelective(wholesaleReturnsDetailIn.getWholesaleReturns());

        //批量新增冲销单详情
        wholesaleReturnDetailMapper.insertWholesaleReturnDetailList(writeOffWholesaleReturnDetailList);
        //库存调用参数转换
        List<StockFlowIn> stockFlowIns = this.addOrSubStock(wholesaleReturnsDetailIn, ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode());
        //调用库存rpc调整库存
        Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
        if (!stockFlow.isSuccess()) {
            throw new BusinessException(stockFlow.getMessage());
        }
        if (!clientDistInfoService.getIsIndependentAccountingByCode(wholesaleReturns.getClientCode(), wholesaleReturns.getBizOrgCode())) {
            //资管调用参数转换
            RechargeLiquidationIn rechargeLiquidationIn = this.chargeParamToRechargeLiquidationIn(wholesaleReturns, writeOffWholesaleReturns.getWholesaleReturnNo(), ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode());
            if (BigDecimal.ZERO.compareTo(rechargeLiquidationIn.getLiquidationAmount()) != 0) {
                //调用rpc调整资金
                Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
                if (!response.isSuccess()) {
                    throw new BusinessException("资管调整失败");
                }
            }
        }

        //日志存储
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                String.valueOf(writeOffWholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(), "新增" + wholesaleReturns.getWholesaleReturnNo() + "冲销单",
                new Date(), writeOffWholesaleReturns.getCreator());

        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return Response.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public WholesaleReturns saveNewWholesaleReturns(WholesaleReturns wholesaleReturns) {
        WholesaleReturns writeOffWholesaleReturns = new WholesaleReturns();
        BeanUtil.copyProperties(wholesaleReturns, writeOffWholesaleReturns);
        writeOffWholesaleReturns.setId(null);
        //生成冲销单单号
        writeOffWholesaleReturns.setWholesaleReturnNo(wholesaleReturnsService.createWholesaleReturnNo());
        //冲销单状态设置为已收货状态
        writeOffWholesaleReturns.setReturnStatus(ShipmentStatusEnum.RECEIPT.getCode());
        //冲销单入库数量为原单相反的
        writeOffWholesaleReturns.setStorageQuantity(NumberUtil.INTEGER_ZERO - wholesaleReturns.getStorageQuantity());
        //冲销单入库金额为原单相反的
        writeOffWholesaleReturns.setStorageAmount(wholesaleReturns.getStorageAmount().negate());
        //冲销单冲销标识为否
        writeOffWholesaleReturns.setIsReversal(ReversalEnum.IS_REVERSAL_ORDER_FALSE.getCode());
        //冲销单冲销字段设置为是
        writeOffWholesaleReturns.setIsReversalOrder(ReversalEnum.IS_REVERSAL_ORDER_TRUE.getCode());
        //设置来自那个原始单的冲销单
        writeOffWholesaleReturns.setSourceNo(wholesaleReturns.getWholesaleReturnNo());
        //设置创建订单时间
        writeOffWholesaleReturns.setCreateTime(LocalDateTime.now());
        writeOffWholesaleReturns.setAuditTime(writeOffWholesaleReturns.getCreateTime());
        writeOffWholesaleReturns.setReceiveTime(wholesaleReturns.getCreateTime());
        writeOffWholesaleReturns.setCreator(null);
        writeOffWholesaleReturns.setUpdateTime(null);
        writeOffWholesaleReturns.setUpdater(null);
        wholesaleReturnsService.insert(writeOffWholesaleReturns);
        return writeOffWholesaleReturns;
    }

    /**
     * 导入批发退货单明细
     *
     * @param fileId
     * @param clientCode
     * @param stockCode
     * @param warehouseCode
     * @param stockId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<List<WholesaleReturnDetailAndGoodsStrOut>> importWholesaleReturnDetail(String fileId, String clientCode, String stockCode, String warehouseCode, Integer stockId) {
        //获取传输数据的字符数组
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        Assert.notNull(bytes, () -> {
            throw new BusinessException("无效的Excel模板");
        });
        //创建文件输入流
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        boolean isCanEditPrice = clientDistInfoService.getIsCanEditPrice(clientCode, UserUtil.getBizOrgCode());
        // 查询客户
        List<ClientDistInfoOut> clientDistInfos = clientDistInfoService.findClientDistInfo(new QueryClientDistInfoIn(clientCode, UserUtil.getBizOrgCode()));
        if (CollectionUtils.isEmpty(clientDistInfos)) {
            log.error("批发退查询客户代码{}异常", clientCode);
            throw new BusinessException("批发退查询客户" + clientCode + "校验失败");
        }
        //创建模板读取类对象
        ImportWholesaleReturnDetailListener listener = new ImportWholesaleReturnDetailListener(clientCode, stockCode, warehouseCode,
                stockId, saleGoodsInfoClient, isCanEditPrice, clientDistInfos.get(NumberUtil.INTEGER_ZERO).getPriceGroupCode());
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportWholesaleReturnDetail.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(com.edc.erp.common.util.NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        return listener.getResponse();
    }

    /**
     * 导出批发退货单明细
     *
     * @param wholesaleReturnsId
     */
    @Override
    public String exportWholesaleReturnDetail(Long wholesaleReturnsId) {
        WholesaleReturnDetail wholesaleReturnDetail = new WholesaleReturnDetail();
        wholesaleReturnDetail.setWholesaleReturnId(wholesaleReturnsId);
        wholesaleReturnDetail.setIsDelete(ModelConst.DELETE.NO);
        //根据批发退货单主键查询批发退货单详情集合
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnDetailMapper.select(wholesaleReturnDetail);
        //创建导出实体类集合
        List<ExportWholesaleReturnDetail> exportWholesaleReturnDetailList = new ArrayList<>();
        //将查询出来的实体类转化为导出实体类并设置序号
        for (int i = 0; i < wholesaleReturnDetailList.size(); i++) {
            ExportWholesaleReturnDetail exportWholesaleReturnDetail = new ExportWholesaleReturnDetail();
            BeanUtil.copyProperties(wholesaleReturnDetailList.get(i), exportWholesaleReturnDetail);
            exportWholesaleReturnDetail.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(wholesaleReturnDetailList.get(i).getGoodsType()));
            exportWholesaleReturnDetail.setNo(i + 1);
            exportWholesaleReturnDetailList.add(exportWholesaleReturnDetail);
        }
        byte[] bytes = FileExportUtil.getFileBytesByData(exportWholesaleReturnDetailList, "批发退货单商品", "批发退货单商品", ExportWholesaleReturnDetail.class, true);
        return fileService.uploadFile("批发退货单明细" + ".xlsx", bytes, OrdSystemConstant.SYSTEM_CODE, OrdSystemConstant.SYSTEM_NAME);
    }

    /**
     * 发送信息给DTS
     *
     * @return
     */
    public void sendMessageToDTS(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        //批发退货单DTS入参转换
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        WholesaleReBillIn wholesaleReBillIn = new WholesaleReBillIn();
        //设置退货单号
        String wholesaleReturnNo = wholesaleReturns.getWholesaleReturnNo();
        wholesaleReBillIn.setPlatform_bill_id(wholesaleReturnNo);
        //设置退货时间
        wholesaleReBillIn.setBill_create_date(LocalDate.from(wholesaleReturns.getCreateTime()));
        //设置客户id(需要远程调用)
        wholesaleReBillIn.setCustomer_id(wholesaleService.getClientIdByCode(wholesaleReturns.getClientCode(), wholesaleReturns.getBizOrgCode()).toString());
        //设置客户代码
        wholesaleReBillIn.setCustomer_code(wholesaleReturns.getClientCode());
        //设置仓储代码
        wholesaleReBillIn.setWarehouse_id(wholesaleReturnsDetailIn.getWarehouseCode());
        //设置退货仓位代码
        wholesaleReBillIn.setSource_stock_id(wholesaleReturnsDetailIn.getStockCode());
        //设置退货地址
        // 收货人|电话|收货地址|remark，
        String consignee = wholesaleShipmentService.replaceStr(wholesaleReturns.getConsignee());
        String consigneePhone = wholesaleShipmentService.replaceStr(wholesaleReturns.getConsigneePhone());
        String addressDetail = wholesaleShipmentService.replaceStr(wholesaleReturns.getAddressDetail());
        String address_i = consignee + SystemConstant.VERTICAL_BAR + consigneePhone + SystemConstant.VERTICAL_BAR + addressDetail;
        wholesaleReBillIn.setAddress_i(address_i);
        //设置填单人
        wholesaleReBillIn.setCreater(wholesaleReturns.getCreator());
        //设置生成时间(审核时间)更新时间为空时，将创建时间设置为审核时间
        wholesaleReBillIn.setGenerate_time(ObjectUtil.isEmpty(wholesaleReturns.getUpdateTime()) ? wholesaleReturns.getCreateTime() : wholesaleReturns.getUpdateTime());
        //设置备注
        InvBizRsnTransOut invBizRsnTransOut = equipmentBusinessReasonServer.getWarehouseBizRsnTransByCode(wholesaleReturns.getReturnsReason(), wholesaleReturns.getBizOrgCode(), NumberUtil.INTEGER_ZERO);
        String memo = (Objects.isNull(invBizRsnTransOut) ? wholesaleReturns.getRemark() : wholesaleReturns.getRemark() + SystemConstant.SHORT_LINE + invBizRsnTransOut.getBusinessReasonName());
        wholesaleReBillIn.setMemo(memo);
        //设置来源单号
        if (StringUtils.isNotBlank(wholesaleReturns.getSourceNo())) {
            wholesaleReBillIn.setFrom_num(wholesaleReturns.getSourceNo());
        } else if (StringUtils.isNotBlank(wholesaleReturns.getWholesaleShipmentNo())) {
            wholesaleReBillIn.setFrom_num(wholesaleReturns.getWholesaleShipmentNo());
        } else {
            log.info("批发退{}下发DTS来源单号为空", wholesaleReturns.getWholesaleReturnNo());
        }
        //设置来源组织
        wholesaleReBillIn.setSource_organization(ObjectUtil.isEmpty(wholesaleReturns.getBizOrgCode()) ? UserUtil.getBizOrgCode() : wholesaleReturns.getBizOrgCode());
        //设置目标组织(目标组织设置为来源组织,因为流转发生于当前组织)
        wholesaleReBillIn.setTarget_organization(wholesaleReBillIn.getSource_organization());
        //转换详情信息
        List<WholesaleReBillDtlIn> wholesaleReBillDtlInList = new ArrayList<>();
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnsDetailIn.getWholesaleReturnDetailList();
        for (WholesaleReturnDetail item : wholesaleReturnDetailList) {
            WholesaleReBillDtlIn data = new WholesaleReBillDtlIn();
            //设置单号
            data.setPlatform_bill_id(wholesaleReturnNo);
            //设置行号
            data.setLine(item.getLine());
            //设置组织商品id(查询数据库获取组织商品信息)
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setGoodsCode(item.getGoodsCode());
            orderGoodsIn.setBizOrgCode(wholesaleReturns.getBizOrgCode());
            OrderGoodsOut orderGoodsOut = orderGoodsServer.getOrderGoods(orderGoodsIn);
            if (Objects.nonNull(orderGoodsOut)) {
                data.setSku_id(String.valueOf(orderGoodsOut.getOrgGoodsId()));
            }
            //设置组织商品代码
            data.setSku_code(item.getGoodsCode());
            //设置数量(数据类型怎么为BigDecimal)
            data.setQuantity(BigDecimal.valueOf(item.getCheckQuantity()));
            //设置价格
            data.setPrice_i(item.getReturnsPrice());
            //设置来源组织
            data.setSource_organization(wholesaleReBillIn.getSource_organization());
            //设置目标组织
            data.setTarget_organization(wholesaleReBillIn.getSource_organization());
            // 放置退货原则
            data.setMemo(item.getReturnPrinciple());
            //将转换对象添加到List集合中
            wholesaleReBillDtlInList.add(data);
        }
        //设置详情
        wholesaleReBillIn.setDetail_list(wholesaleReBillDtlInList);
        //存储消息
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.WHOLESALE_RETURN_TO_DTS, JSONObject.toJSONString(wholesaleReBillIn), wholesaleReturns.getBizOrgCode(), wholesaleReturns.getWholesaleReturnNo());
        SendResponse sendResponse = wholesaleReturnToDtsSender.sendSync(JSONObject.toJSONString(wholesaleReBillIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("DTS批发出货退数据下发{}下发DTS消息ID---{}", wholesaleReturns.getWholesaleReturnNo(), sendResponse.getMessageId());
    }

    /**
     * 批发退货单库存调整参数转换
     *
     * @param wholesaleReturnsDetailIn
     * @param returnStatus
     * @return
     */
    @Override
    public List<StockFlowIn> addOrSubStock(WholesaleReturnsDetailIn wholesaleReturnsDetailIn, String returnStatus) {
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnsDetailIn.getWholesaleReturnDetailList();
        //库存流水入参
        StockFlowIn stockFlowIn = new StockFlowIn();
        //添加业务组织
        stockFlowIn.setBizOrgCode(wholesaleReturns.getBizOrgCode());
        //公司业务组织
        stockFlowIn.setOrgCode(wholesaleReturns.getOrgCode());
        //公司业务类型
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.WHOLESALE_RETREAT.getCode());
        //公司业务类型名称
        stockFlowIn.setSourceName(InvBusinessTypeEnum.WHOLESALE_RETREAT.getName());
        //业务发生日期
        stockFlowIn.setFlowDate(LocalDateTime.now());
        //创建人
        stockFlowIn.setCreator(wholesaleReturns.getCreator());
        if (ShipmentStatusEnum.RECEIPT.getCode().equals(returnStatus)) {
            stockFlowIn.setOperationType(OrderTypeEnum.COLLECTED.getCode());
        } else {
            //冲销减
            stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        }

        //单号/流水号
        stockFlowIn.setSourceNo(wholesaleReturns.getWholesaleReturnNo());
        // 外部三方单号
        stockFlowIn.setOtherOrderNo(wholesaleReturns.getSourceNo());
        //库存流水详情入参
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        //处理出货单详情
        for (WholesaleReturnDetail data : wholesaleReturnDetailList) {
            //流水详情入参
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            //copy 同属性字段
            BeanUtils.copy(data, stockFlowGoodsIn);
            //添加仓位code
            stockFlowGoodsIn.setStockCode(wholesaleReturns.getStorageStockCode());
            //添加仓储code
            stockFlowGoodsIn.setWarehouseCode(wholesaleReturns.getStorageWrh());
            //根据仓位code查询仓位信息和仓储信息
            StockInfoOut stockInfoOut = stockServer.getByCode(wholesaleReturns.getStorageStockCode(), wholesaleReturns.getBizOrgCode());
            //添加仓位名称
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            //添加仓储名称
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            //已收货
            if (ShipmentStatusEnum.RECEIPT.getCode().equals(returnStatus)) {
                //实际增
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
            }
            //冲单
            if (ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode().equals(returnStatus)) {
                //实际减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
            }
            // 实际数
            stockFlowGoodsIn.setActualQty(BigDecimal.valueOf(data.getStorageQuantity()).abs());
            //成本不含税金额
//            data.getStorageQuantity()
            stockFlowGoodsIn.setCostNonTaxAmount(data.getCostNetProfitAmount());
            //成本含税金额
            stockFlowGoodsIn.setCostTaxAmount(data.getCostAmount());
            //成本税额
            stockFlowGoodsIn.setCostTax(data.getCostTax());
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(data.getStorageNetProfit());
            //含税金额
            stockFlowGoodsIn.setTaxAmount(data.getPracticalStorageAmount());
            //税额
            stockFlowGoodsIn.setTax(data.getStorageTax());
            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
            //未关联出货单 则发生价按照 退货单价
            if (StringUtils.isBlank(wholesaleReturns.getWholesaleShipmentNo())) {
                //发生价 = 退货单价
                stockFlowGoodsIn.setPrice(data.getReturnsPrice());
            } else {
                //发生价 = 库存价
                stockFlowGoodsIn.setPrice(data.getInventoryPrice());
            }
            //流水单号
            stockFlowGoodsIn.setSourceNo(wholesaleReturns.getWholesaleReturnNo());
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
     * 根据批发出货单主键生成批发退货单数据
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    @Override
    public Response<WholesaleReturnAndDetailOut> outToReturn(Long id, String bizOrgCode) {
        //根据批发退货单主键和业务组织代码查询批发出货单详情
        ShipmentWithDetailOut shipmentWithDetailOut = wholesaleShipmentService.getShipment(id, bizOrgCode);
        //判断当前退货单状态是否是已发货状态
        if (!shipmentWithDetailOut.getShipmentStatus().equals(ShipmentStatusEnum.SHIPPED.getCode())) {
            throw new BusinessException("非发货状态不能申请退货");
        }
        /**
         * 组装批发退货单数据
         */
        WholesaleReturnAndDetailOut wholesaleReturnAndDetailOut = new WholesaleReturnAndDetailOut();
        //设置批发出货单单号
        wholesaleReturnAndDetailOut.setWholesaleShipmentNo(shipmentWithDetailOut.getShipmentNo());
        //设置客户代码
        wholesaleReturnAndDetailOut.setClientCode(shipmentWithDetailOut.getClientCode());
        //设置价格组代码
        wholesaleReturnAndDetailOut.setPriceGroupCode(shipmentWithDetailOut.getPriceGroupCode());
        //设置入库仓储
        wholesaleReturnAndDetailOut.setStorageWrh(shipmentWithDetailOut.getShipmentWrh());
        //设置入库仓位
//        wholesaleReturnAndDetailOut.setStorageStockCode(shipmentWithDetailOut.getShipmentStockCode());
        wholesaleReturnAndDetailOut.setIsReversal(ReversalEnum.IS_REVERSAL_FALSE.getCode());
        wholesaleReturnAndDetailOut.setIsReversalOrder(ReversalEnum.IS_REVERSAL_ORDER_FALSE.getCode());
        //设置申请数量
        wholesaleReturnAndDetailOut.setApplicationQuantity(shipmentWithDetailOut.getShipmentQuantity());
        //设置申请金额
        wholesaleReturnAndDetailOut.setApplicationAmount(shipmentWithDetailOut.getPracticalShipmentAmount());
        //设置收货人
        wholesaleReturnAndDetailOut.setConsignee(shipmentWithDetailOut.getConsignee());
        //设置配送信息id
        wholesaleReturnAndDetailOut.setDistributionInfoId(shipmentWithDetailOut.getDistributionInfoId());
        //设置收货人手机号
        wholesaleReturnAndDetailOut.setConsigneePhone(shipmentWithDetailOut.getConsigneePhone());
        //设置收货详细地址
        wholesaleReturnAndDetailOut.setAddressDetail(shipmentWithDetailOut.getAddressDetail());
        //设置业务组织代码
        wholesaleReturnAndDetailOut.setBizOrgCode(UserUtil.getBizOrgCode());
        //远程调用根据客户代码及批发价格组代码查询其他信息
        ClientWholesaleConfigIn clientWholesaleConfigIn = new ClientWholesaleConfigIn();
        clientWholesaleConfigIn.setPriceGroupCode(shipmentWithDetailOut.getPriceGroupCode());
        clientWholesaleConfigIn.setClientCode(shipmentWithDetailOut.getClientCode());
        clientWholesaleConfigIn.setBizOrgCode(shipmentWithDetailOut.getBizOrgCode());
        List<ClientWholesaleConfigOut> wholesalePriceGroup = wholesaleService.findWholesalePriceGroup(clientWholesaleConfigIn);
        lo:
        for (ClientWholesaleConfigOut clientWholesaleConfigOut : wholesalePriceGroup) {
            if (clientWholesaleConfigOut.getPriceGroupCode().equals(shipmentWithDetailOut.getPriceGroupCode())) {
                String priceGroupName = clientWholesaleConfigOut.getPriceGroupName();
                String priceGroup = clientWholesaleConfigOut.getPriceGroup();
                //设置价格组名称
                wholesaleReturnAndDetailOut.setPriceGroupName(priceGroupName);
                //设置价格组
                wholesaleReturnAndDetailOut.setPriceGroup(priceGroup);
                String clientName = clientWholesaleConfigOut.getClientName();
                //设置客户名称
                wholesaleReturnAndDetailOut.setClientName(clientName);
                //设置客户信息
                wholesaleReturnAndDetailOut.setClientMessage("【" + clientWholesaleConfigOut.getClientCode() + "】" + clientName);
            }
            break lo;
        }
        //退货状态转中文
        wholesaleReturnAndDetailOut.setReturnStatusStr(ShipmentStatusEnum.getNameByCode(wholesaleReturnAndDetailOut.getReturnStatus()));
        /**
         * 组装批发退货单详情数据
         */
        //获取批发出货单详情数据
        List<WholesaleShipmentDetailOut> wholesaleShipmentDetailOuts = shipmentWithDetailOut.getWholesaleShipmentDetailOuts();
        List<WholesaleReturnDetailAndGoodsStrOut> wholesaleReturnDetailList = new ArrayList<>();
        for (WholesaleShipmentDetailOut data : wholesaleShipmentDetailOuts) {
            WholesaleReturnDetailAndGoodsStrOut returnDetail = new WholesaleReturnDetailAndGoodsStrOut();
            returnDetail.setGoodsCode(data.getGoodsCode());
            returnDetail.setGoodsName(data.getGoodsName());
            returnDetail.setGoodsType(data.getGoodsType());
            returnDetail.setBarCode(data.getBarCode());
            returnDetail.setPackageSpecification(data.getPackageSpecification());
            returnDetail.setPackageUnit(data.getPackageUnit());
            //设置申请数量
            returnDetail.setApplyQuantity(data.getShipmentQuantity());
            //设置申请金额
            returnDetail.setApplyAmount(data.getApplyAmount());
            //设置审核数量
            returnDetail.setCheckQuantity(data.getApplyQuantity());
            //设置申请包装数量
            returnDetail.setApplyPackageNum(data.getApplyPackageNum());
            //设置退货单价
            returnDetail.setReturnsPrice(data.getUnitPrice());
            //设置库存价
            returnDetail.setInventoryPrice(data.getInventoryPrice());
            //品类属性转中文
            returnDetail.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(data.getGoodsType()));
            returnDetail.setInvoiceType(data.getInvoiceType());
            wholesaleReturnDetailList.add(returnDetail);
        }
        wholesaleReturnAndDetailOut.setWholesaleReturnDetailList(wholesaleReturnDetailList);
        return Response.data(wholesaleReturnAndDetailOut);
    }

    /**
     * 批量更新批发退货单明细
     *
     * @param wholesaleReturnDetailList 批发退货单明细
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<WholesaleReturnDetail> wholesaleReturnDetailList) {
        //批量保存出货单明细
        int pages = wholesaleReturnDetailList.size() % SystemConstant.PAGE_SIZE == 0 ? wholesaleReturnDetailList.size() / SystemConstant.PAGE_SIZE : wholesaleReturnDetailList.size() / SystemConstant.PAGE_SIZE + 1;
        for (int i = 0; i < pages; i++) {
            wholesaleReturnDetailMapper.batchUpdate(wholesaleReturnDetailList.subList(i * SystemConstant.PAGE_SIZE, i == pages - 1 ? wholesaleReturnDetailList.size() : (i + 1) * SystemConstant.PAGE_SIZE));
        }
    }

//    @Override
//    public Response<String> batchAuditWholesaleReturn(List<Long> wholesaleReturnsIdList) {
//        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
//        wholesaleReturnsIdList.forEach(id -> {
//            WholesaleReturns wholesaleReturns = null;
//            try {
//                wholesaleReturns = wholesaleReturnsService.selectByPrimaryKey(id);
//                if (Objects.isNull(wholesaleReturns)) {
//                    return;
//                }
//                if (!wholesaleReturns.getReturnStatus().equals(ShipmentStatusEnum.PENDING.getCode())) {
//                    log.error("批发单{}不能进行审核操作", wholesaleReturns.getWholesaleReturnNo());
//                    errorJoiner.add(wholesaleReturns.getWholesaleReturnNo() + "不能进行审核操作");
//                    return;
//                }
//                WholesaleReturnDetail detailParameter = new WholesaleReturnDetail();
//                detailParameter.setWholesaleReturnId(id);
//                detailParameter.setIsDelete(ModelConst.DELETE.NO);
//                List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnDetailMapper.select(detailParameter);
//                if (CollectionUtils.isEmpty(wholesaleReturnDetailList)) {
//                    log.error("批发退{}没有明细", wholesaleReturns.getWholesaleReturnNo());
//                    errorJoiner.add(wholesaleReturns.getWholesaleReturnNo() + "没有明细");
//                    return;
//                }
//                WholesaleReturnsDetailIn wholesaleReturnsDetailIn = new WholesaleReturnsDetailIn();
//                wholesaleReturnsDetailIn.setWholesaleReturns(wholesaleReturns);
//                wholesaleReturnsDetailIn.setWarehouseCode(wholesaleReturns.getStorageWrh());
//                wholesaleReturnsDetailIn.setWholesaleReturnDetailList(wholesaleReturnDetailList);
//                wholesaleReturnsDetailIn.setStockCode(wholesaleReturns.getStorageStockCode());
//                //校验当前信息
//                Response response = wholesaleReturnsService.checkWholesaleReturn(wholesaleReturnsDetailIn);
//                if (!response.isSuccess()) {
//                    log.error("批发退{}校验失败：{}", wholesaleReturns.getWholesaleReturnNo(), response.getMessage());
//                    errorJoiner.add(wholesaleReturns.getWholesaleReturnNo() + "校验失败");
//                    return;
//                }
//                wholesaleReturns.setReturnStatus(ShipmentStatusEnum.APPROVED.getCode());
//                //设置更新时间作为审核时间
//                wholesaleReturns.setUpdateTime(LocalDateTime.now());
//                //更新批发退货信息
//                wholesaleReturnsService.updateByPrimaryKeySelective(wholesaleReturns);
//                //发送消息到DTS
//                if (stockServer.isSendWms(wholesaleReturns.getStorageStockCode(), wholesaleReturns.getBizOrgCode())) {
//                    sendMessageToDTS(wholesaleReturnsDetailIn);
//                }
//                //保存日志
//                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
//                        String.valueOf(wholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(), "审核批发退货单",
//                        new Date(), wholesaleReturns.getUpdater());
//                asyncLogService.sendAsyncSaveLogByMq(businessLog);
//            } catch (Exception e) {
//                errorJoiner.add(Objects.isNull(wholesaleReturns) ? id.toString() : wholesaleReturns.getWholesaleReturnNo() + "审核异常");
//                log.error("批发退ID{}审核异常", id.toString(), e);
//            }
//        });
//        if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
//            return Response.success("批发退：" + errorJoiner + "审核失败");
//        } else {
//            return Response.success("审核成功");
//        }
//    }

    @Override
    public List<WholesaleReturnDetail> findListByWholesaleReturnId(Long wholesaleReturnId) {
        WholesaleReturnDetail wholesaleReturnDetail = new WholesaleReturnDetail();
        wholesaleReturnDetail.setWholesaleReturnId(wholesaleReturnId);
        wholesaleReturnDetail.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleReturnDetailMapper.select(wholesaleReturnDetail);
    }

    @Override
    public WholesaleReturnDateInfoOut sumWholesaleReturnDateInfoByIdList(List<Long> idList) {
        return wholesaleReturnDetailMapper.sumWholesaleReturnDateInfoByIdList(idList);
    }

    /**
     * 批发退货单资管调整参数转换
     *
     * @param wholesaleReturns
     * @param returnStatus
     */
    @Override
    public RechargeLiquidationIn chargeParamToRechargeLiquidationIn(WholesaleReturns wholesaleReturns, String newWholesaleReturnNo, String returnStatus) {
        //创建资管调整对象
        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
        String businessNo = wholesaleReturns.getWholesaleReturnNo();
        String businessType = null;
        String originalBusinessNo = wholesaleReturns.getWholesaleReturnNo();
        String direction = FundDirectionEnum.PAY.getCode();
        //已收货
        if (ShipmentStatusEnum.RECEIPT.getCode().equals(returnStatus)) {
            //设置收入方客户编码
            rechargeLiquidationIn.setRecipientPrincipalCode(wholesaleReturns.getClientCode());
            //设置收入方主体类型
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
            rechargeLiquidationIn.setPayOrPrincipalCode(wholesaleReturns.getBizOrgCode());
            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            rechargeLiquidationIn.setRemark(FundReturnTypeEnum.WHOLESALE_RETURN_CHARGE.getName());
//            businessType = FundTypeEnum.DIS_WHOLESALE_RETURN.getCode();
            direction = FundDirectionEnum.RETURN.getCode();
        }
        //冲销
        if (ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode().equals(returnStatus)) {
            //支出方编码
            rechargeLiquidationIn.setPayOrPrincipalCode(wholesaleReturns.getClientCode());
            //支出方主体类型
            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.CUSTOMER.getCode());
            rechargeLiquidationIn.setRecipientPrincipalCode(wholesaleReturns.getBizOrgCode());
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            businessNo = newWholesaleReturnNo;
            //业务模块
            rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
//            businessType = FundTypeEnum.DIS_WHOLESALE_PAY.getCode();
            direction = FundDirectionEnum.PAY.getCode();
        }
        // 2023-06-02 应刘应高要求将批发的业务类型全部调整为批发退货
        businessType = FundTypeEnum.WHOLESALE_RETURNS.getCode();
        //设置清算金额
        rechargeLiquidationIn.setLiquidationAmount(wholesaleReturns.getStorageAmount().abs());
        //设置业务组织代码
        rechargeLiquidationIn.setBizOrgCode(wholesaleReturns.getBizOrgCode());
        //设置业务单号
        rechargeLiquidationIn.setBusinessNo(businessNo);
        //设置业务类型
        rechargeLiquidationIn.setBusinessType(businessType);
        rechargeLiquidationIn.setOriginalBusinessNo(originalBusinessNo);
        rechargeLiquidationIn.setDirection(direction);
        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        return rechargeLiquidationIn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateForAudit(List<WholesaleReturnDetail> wholesaleReturnDetailList) {
        //批量保存出货单明细
        int pages = wholesaleReturnDetailList.size() % SystemConstant.PAGE_SIZE == 0 ? wholesaleReturnDetailList.size() / SystemConstant.PAGE_SIZE : wholesaleReturnDetailList.size() / SystemConstant.PAGE_SIZE + 1;
        for (int i = 0; i < pages; i++) {
            wholesaleReturnDetailMapper.batchUpdateForAudit(wholesaleReturnDetailList.subList(i * SystemConstant.PAGE_SIZE, i == pages - 1 ? wholesaleReturnDetailList.size() : (i + 1) * SystemConstant.PAGE_SIZE));
        }
    }
}
