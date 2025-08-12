package com.edc.erp.wholesale.returns.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.handel.SyncOrdDisOrderHandle;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.enumeration.InvoiceTypeEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrderNoPreEnum;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.in.returns.ClientWholesaleConfigIn;
import com.edc.erp.common.model.out.InvBizRsnTransOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.model.out.goods.StandardSpecOut;
import com.edc.erp.common.model.out.returns.ClientWholesaleConfigOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.stock.StockWarehouseOut;
import com.edc.erp.common.rpc.SaleGoodsInfoClient;
import com.edc.erp.common.service.*;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.enumeration.ReversalEnum;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.enumeration.WholesaleOrderTypeEnum;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.wholesale.model.excel.returns.ExportWholesaleReturnOrder;
import com.edc.erp.wholesale.model.in.returns.*;
import com.edc.erp.wholesale.model.in.shipment.QueryShipmentDetailIn;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnAndDetailOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDateInfoOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnDetailAndGoodsStrOut;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnsListOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.returns.mapper.WholesaleReturnsMapper;
import com.edc.erp.wholesale.returns.service.WholesaleReturnDetailService;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsGoodsService;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.erp.wholesale.shipment.mapper.WholesaleShipmentMapper;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.export.AsyncExportExecutor;
import com.edc.plugins.export.ExportExcelByPage;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.model.order.vo.WholesaleReBillDtlVO;
import com.edc.sdk.dts.model.order.vo.WholesaleReBillVO;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * 批发退货单(WholesaleReturns)表服务实现类
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
@Service
@Slf4j
public class WholesaleReturnsServiceImpl extends BaseServiceImpl<WholesaleReturns> implements WholesaleReturnsService {

    @Autowired
    private WholesaleReturnDetailService wholesaleReturnDetailService;
    @Autowired
    private WholesaleReturnsMapper wholesaleReturnsMapper;
    @Autowired
    private WholesaleService wholesaleService;
    @Autowired
    private AsyncLogService asyncLogService;
    @Autowired
    private UniqueUtils uniqueUtils;
    @Autowired
    private WholesaleReturnsGoodsService wholesaleReturnsGoodsService;
    @Autowired
    private WholesaleShipmentMapper wholesaleShipmentMapper;
    @Autowired
    private StockServer stockServer;
    @Autowired
    private SaleGoodsInfoClient saleGoodsInfoClient;
    @Autowired
    private SyncOrdDisOrderHandle syncOrdDisOrderHandle;
    @Autowired
    private WholesaleShipmentDetailService wholesaleShipmentDetailService;
    @Autowired
    private StockFlowService stockFlowService;
    @Autowired
    private ClientDistInfoService clientDistInfoService;

    @Autowired
    private AsyncPushTaskService asyncPushTaskService;

    @Autowired
    private AsyncExportExecutor asyncExportExecutor;

    @Autowired
    private WarehouseServer warehouseServer;

    @Autowired
    private EquipmentBusinessReasonServer equipmentBusinessReasonServer;
//
//    @Autowired
//    @Qualifier("hsWholesaleReturnSender")
//    private MessageSender hsWholesaleReturnSender;

    @Autowired
    @Qualifier("hsWholesaleReturnDtsBackSender")
    private MessageSender hsWholesaleReturnDtsBackSender;

    private final int batchSize = 8000;

    @Autowired
    @Qualifier("zKWholesaleReturnBackSender")
    private MessageSender zKWholesaleReturnBackSender;

    /**
     * 批发退货单列表查询
     *
     * @param wholesaleReturnsListIn
     * @return
     */
    @Override
    public List<WholesaleReturnsListOut> findWholesaleReturnsForPage(WholesaleReturnsListIn wholesaleReturnsListIn) {
        wholesaleReturnsListIn.setBizOrgCode(UserUtil.getBizOrgCode());
        wholesaleReturnsListIn.setIsDelete(ModelConst.DELETE.NO);
        List<WholesaleReturnsListOut> wholesaleReturnsList = wholesaleReturnsMapper.findWholesaleReturnsByPage(wholesaleReturnsListIn);
        for (WholesaleReturnsListOut wholesaleReturnsListOut : wholesaleReturnsList) {
            ClientWholesaleConfigIn clientWholesaleConfigIn = new ClientWholesaleConfigIn();
            clientWholesaleConfigIn.setPriceGroupCode(wholesaleReturnsListOut.getPriceGroupCode());
            clientWholesaleConfigIn.setBizOrgCode(UserUtil.getBizOrgCode());
            clientWholesaleConfigIn.setClientCode(wholesaleReturnsListOut.getClientCode());
            List<ClientWholesaleConfigOut> wholesalePriceGroupList = wholesaleService.findWholesalePriceGroup(clientWholesaleConfigIn);
            lo:
            for (ClientWholesaleConfigOut clientWholesaleConfigOut : wholesalePriceGroupList) {
                if (clientWholesaleConfigOut.getPriceGroupCode().equals(wholesaleReturnsListOut.getPriceGroupCode())) {
                    String priceGroup = clientWholesaleConfigOut.getPriceGroup();
                    wholesaleReturnsListOut.setPriceGroupName(clientWholesaleConfigOut.getPriceGroupName());
                    wholesaleReturnsListOut.setPriceGroup(priceGroup);
                    wholesaleReturnsListOut.setClientName(clientWholesaleConfigOut.getClientName());
                    wholesaleReturnsListOut.setClientMessage("【" + wholesaleReturnsListOut.getClientCode() + "】" + clientWholesaleConfigOut.getClientName());
                }
                break lo;
            }
            //退货单状态中文转换
            wholesaleReturnsListOut.setReturnStatusStr(ShipmentStatusEnum.getNameByCode(wholesaleReturnsListOut.getReturnStatus()));
        }
        return wholesaleReturnsList;
    }

    /**
     * 根据批发退货单单号查询退货单详情
     *
     * @param wholesaleReturnNo
     * @return
     */
    @Override
    public WholesaleReturnAndDetailOut getDetailByWholesaleReturnNo(String wholesaleReturnNo) {
        WholesaleReturnAndDetailOut wholesaleReturnDetailOut = new WholesaleReturnAndDetailOut();
        WholesaleReturns wholesaleReturns = new WholesaleReturns();
        wholesaleReturns.setWholesaleReturnNo(wholesaleReturnNo);
        wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
        //根据退货单单号查询退货单信息
        WholesaleReturns wholesaleReturnsTemp = wholesaleReturnsMapper.selectOne(wholesaleReturns);
        BeanUtil.copyProperties(wholesaleReturnsTemp, wholesaleReturnDetailOut);
        ClientWholesaleConfigIn clientWholesaleConfigIn = new ClientWholesaleConfigIn();
        clientWholesaleConfigIn.setClientCode(wholesaleReturnsTemp.getClientCode());
        clientWholesaleConfigIn.setPriceGroupCode(wholesaleReturnsTemp.getPriceGroupCode());
        clientWholesaleConfigIn.setBizOrgCode(wholesaleReturnsTemp.getBizOrgCode());
        List<ClientWholesaleConfigOut> wholesalePriceGroupList = wholesaleService.findWholesalePriceGroup(clientWholesaleConfigIn);
        for (ClientWholesaleConfigOut clientWholesaleConfigOut : wholesalePriceGroupList) {
            if (clientWholesaleConfigOut.getPriceGroupCode().equals(wholesaleReturnsTemp.getPriceGroupCode())) {
                String priceGroupName = clientWholesaleConfigOut.getPriceGroupName();
                String priceGroup = clientWholesaleConfigOut.getPriceGroup();
                wholesaleReturnDetailOut.setPriceGroupName(priceGroupName);
                wholesaleReturnDetailOut.setPriceGroup(priceGroup);
                String clientName = clientWholesaleConfigOut.getClientName();
                wholesaleReturnDetailOut.setClientName(clientName);
                wholesaleReturnDetailOut.setClientMessage("【" + wholesaleReturnDetailOut.getClientCode() + "】" + clientName);
            }
            break;
        }

        //查询明细
        List<WholesaleReturnDetail> wholesaleReturnDetailList = this.findDetailByWholesaleReturnId(wholesaleReturnsTemp.getId());
        Map<String, StockWarehouseOut> stockInvMap = new HashMap<>();
        Map<String, List<StandardSpecOut>> goodsStandardSpecMap = new HashMap<>();
        if (ShipmentStatusEnum.PENDING.getCode().equals(wholesaleReturnsTemp.getReturnStatus())) {
            List<String> goodsCodeList = wholesaleReturnDetailList.stream().map(WholesaleReturnDetail::getGoodsCode).collect(Collectors.toList());
            stockInvMap = warehouseServer.findStockInv(wholesaleReturnsTemp.getStorageStockCode(), goodsCodeList, wholesaleReturnsTemp.getBizOrgCode());
            // 查询商品规格集合
            Response<Map<String, List<StandardSpecOut>>> response = saleGoodsInfoClient.findByGoodsCodes(goodsCodeList);
            if (null != response && response.isSuccess()) {
                goodsStandardSpecMap = response.getData();
            }
        }
        List<WholesaleReturnDetailAndGoodsStrOut> wholesaleReturnDetailAndStrOutList = new ArrayList<>();
        //商品品类属性转中文
        for (WholesaleReturnDetail returnDetail : wholesaleReturnDetailList) {
            WholesaleReturnDetailAndGoodsStrOut wholesaleReturnDetailAndStrOut = new WholesaleReturnDetailAndGoodsStrOut();
            BeanUtil.copyProperties(returnDetail, wholesaleReturnDetailAndStrOut);
            wholesaleReturnDetailAndStrOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(returnDetail.getGoodsType()));
            wholesaleReturnDetailAndStrOut.setCheckAmount(returnDetail.getReturnsPrice().multiply(new BigDecimal(returnDetail.getCheckQuantity())).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            StockWarehouseOut stockWarehouseOut = stockInvMap.get(returnDetail.getGoodsCode());
            wholesaleReturnDetailAndStrOut.setBusinessQty(Objects.nonNull(stockWarehouseOut) ? stockWarehouseOut.getBusinessQty() : null);
            wholesaleReturnDetailAndStrOut.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(returnDetail.getInvoiceType()));
            wholesaleReturnDetailAndStrOut.setStandardSpecs(goodsStandardSpecMap.get(returnDetail.getGoodsCode()));
            wholesaleReturnDetailAndStrOutList.add(wholesaleReturnDetailAndStrOut);
        }
        wholesaleReturnDetailOut.setWholesaleReturnDetailList(wholesaleReturnDetailAndStrOutList);
        //退货状态转中文
        wholesaleReturnDetailOut.setReturnStatusStr(ShipmentStatusEnum.getNameByCode(wholesaleReturnDetailOut.getReturnStatus()));
        //退货原因转中文
        InvBizRsnTransOut invBizRsnTransOut = equipmentBusinessReasonServer.getWarehouseBizRsnTransByCode(wholesaleReturnsTemp.getReturnsReason(),
                wholesaleReturnsTemp.getBizOrgCode(), NumberUtil.INTEGER_ZERO);
        wholesaleReturnDetailOut.setReturnsReasonStr(Objects.isNull(invBizRsnTransOut) ? wholesaleReturnsTemp.getReturnsReason() : invBizRsnTransOut.getBusinessReasonName());

        return wholesaleReturnDetailOut;
    }

    /**0
     * 保存批发退货单和批发退货单详情
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response saveWholesaleReturnsDetail(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        wholesaleReturns.setReturnStatus(ShipmentStatusEnum.PENDING.getCode());
        //校验当前订单是否允许被创建
        Response response = this.checkWholesaleReturn(wholesaleReturnsDetailIn);
        if (!response.isSuccess()) {
            return Response.error(response.getMessage());
        }
        List<WholesaleReturnDetail> data = (List<WholesaleReturnDetail>) response.getData();
        wholesaleReturnsDetailIn.setWholesaleReturnDetailList(data);
        wholesaleReturnsDetailIn = this.countParam(wholesaleReturnsDetailIn);
        //保存批发退货单
        Response saveWholesaleReturnsResponse = this.saveWholesaleReturns(wholesaleReturnsDetailIn);
        if (!saveWholesaleReturnsResponse.isSuccess()) {
            return Response.error(saveWholesaleReturnsResponse.getMessage());
        }
        WholesaleReturns dataTemp = (WholesaleReturns) saveWholesaleReturnsResponse.getData();
        return Response.data(dataTemp);
    }

    /**
     * 新增或保存批发退货单
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Response saveWholesaleReturns(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        //获取批发退货单信息
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        wholesaleReturns.setBizOrgCode(UserUtil.getBizOrgCode());
        String wholesaleReturnNo = StrUtil.isBlank(wholesaleReturns.getWholesaleReturnNo()) ? this.createWholesaleReturnNo() : wholesaleReturns.getWholesaleReturnNo();
        //获取批发退货单详细信息
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnsDetailIn.getWholesaleReturnDetailList();
        if (Objects.isNull(wholesaleReturns.getId())) {
            //新增
            wholesaleReturns.setWholesaleReturnNo(wholesaleReturnNo);
            wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
            wholesaleReturns.setIsReversal(ReversalEnum.IS_REVERSAL_FALSE.getCode());
            wholesaleReturns.setIsReversalOrder(ReversalEnum.IS_REVERSAL_ORDER_FALSE.getCode());
            wholesaleReturnsMapper.insertSelective(wholesaleReturns);
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                    String.valueOf(wholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(), "新增批发退货单",
                    new Date(), wholesaleReturns.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        } else {
            //更新
            if (!wholesaleReturns.getReturnStatus().equals(ShipmentStatusEnum.PENDING.getCode())) {
                return Response.error("当前订单状态不允许进行修改");
            }

            //根据传输的信息新增退货单信息
            wholesaleReturns.setIsReversal(ObjectUtil.isNull(wholesaleReturns.getIsReversal()) ? ReversalEnum.IS_REVERSAL_FALSE.getCode() : wholesaleReturns.getIsReversal());
            wholesaleReturns.setIsReversalOrder(ObjectUtil.isNull(wholesaleReturns.getIsReversalOrder()) ? ReversalEnum.IS_REVERSAL_ORDER_FALSE.getCode() : wholesaleReturns.getIsReversalOrder());
            //更新
            wholesaleReturnsMapper.updateByPrimaryKeySelective(wholesaleReturns);
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                    String.valueOf(wholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(), "更新批发退货单",
                    new Date(), wholesaleReturns.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            //根据退货单主键删除退货单详情信息
            WholesaleReturnDetail wholesaleReturnDetail = new WholesaleReturnDetail();
            wholesaleReturnDetail.setWholesaleReturnId(wholesaleReturns.getId());
            wholesaleReturnDetailService.delete(wholesaleReturnDetail);

        }
        //批量新增详情
        AtomicInteger line = new AtomicInteger(NumberUtil.INTEGER_ONE);
        wholesaleReturnDetailList.forEach(wholesaleReturnDetail -> {
            wholesaleReturnDetail.setId(null);
            wholesaleReturnDetail.setWholesaleReturnId(wholesaleReturns.getId());
            wholesaleReturnDetail.setIsDelete(ModelConst.DELETE.NO);
            wholesaleReturnDetail.setLine(line.get());
            line.incrementAndGet();
        });
        wholesaleReturnDetailService.insertWholesaleReturnDetailList(wholesaleReturnDetailList);
        return Response.data(wholesaleReturns);
    }

    /**
     * 校验订单是否允许被创建或审核通过
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    @Override
    public Response checkWholesaleReturn(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        //校验退货原因是否为空
        if (StrUtil.isBlank(wholesaleReturns.getReturnsReason())) {
            return Response.error("退货原因不能为空");
        }
        //校验客户状态及客户是否存在
        Response response = wholesaleService.selectClientStatus(wholesaleReturns.getClientCode(), wholesaleReturns.getBizOrgCode());
        if (!response.isSuccess()) {
            return Response.error(response.getMessage());
        }
        //校验商品信息
        Response checkGoodsCodeResponse = this.checkGoodsCode(wholesaleReturnsDetailIn);
        if (!checkGoodsCodeResponse.isSuccess()) {
            return Response.error(checkGoodsCodeResponse.getMessage());
        }
        List<WholesaleReturnDetail> data = (List<WholesaleReturnDetail>) checkGoodsCodeResponse.getData();
        return Response.data(data);
    }

    /**
     * 生成批发退货单单号
     */
    @Override
    public String createWholesaleReturnNo() {
        return CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KFT.getCode(), UserUtil.getBizOrgCode(), uniqueUtils, 4);
    }

    /**
     * 统计申请数量、申请金额、入库数量及实际入库金额，并更新进批发退货单
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    @Override
    public WholesaleReturnsDetailIn countParam(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        //获取申请数量
        Integer applicationQuantity = NumberUtil.INTEGER_ZERO;
        //获取申请金额
        BigDecimal applicationAmount = BigDecimal.ZERO;
        //获取入库数量
        Integer storageQuantity = NumberUtil.INTEGER_ZERO;
        //获取实际入库金额
        BigDecimal storageAmount = BigDecimal.ZERO;
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnsDetailIn.getWholesaleReturnDetailList();
        for (WholesaleReturnDetail wholesaleReturnDetail : wholesaleReturnDetailList) {
            applicationQuantity += wholesaleReturnDetail.getApplyQuantity();
            applicationAmount = applicationAmount.add(wholesaleReturnDetail.getApplyAmount());
            storageQuantity += ObjectUtil.isNull(wholesaleReturnDetail.getStorageQuantity()) ? 0 : wholesaleReturnDetail.getStorageQuantity();
            storageAmount = storageAmount.add(ObjectUtil.isNull(wholesaleReturnDetail.getPracticalStorageAmount()) ? new BigDecimal(0) : wholesaleReturnDetail.getPracticalStorageAmount());
        }
        wholesaleReturns.setApplicationQuantity(applicationQuantity);
        wholesaleReturns.setApplicationAmount(applicationAmount);
        wholesaleReturns.setStorageQuantity(storageQuantity);
        wholesaleReturns.setStorageAmount(storageAmount);
        wholesaleReturnsDetailIn.setWholesaleReturns(wholesaleReturns);
        return wholesaleReturnsDetailIn;
    }


    /**
     * 批发退-手动收货
     * @param wholesaleRetReceivingIn 批发退-手动收货 入参
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> wholesaleReturnsReceiving(WholesaleRetReceivingIn wholesaleRetReceivingIn) {
        //获取退货单id集合
        List<Long> wholesaleReturnIds = wholesaleRetReceivingIn.getWholesaleReturnIds();
        for (Long wholesaleReturnId : wholesaleReturnIds) {
            //查询退货单是否存在
            WholesaleReturns wholesaleReturns = wholesaleReturnsMapper.selectByPrimaryKey(wholesaleReturnId);
            if (Objects.isNull(wholesaleReturns)) {
                return Response.error("退货单不存在");
            }
            if (!ShipmentStatusEnum.APPROVED.getCode().equals(wholesaleReturns.getReturnStatus())) {
                return Response.error("已审核状态下才能收货");
            }
            //查询明细
            List<WholesaleReturnDetail> wholesaleReturnDetails = this.findDetailByWholesaleReturnId(wholesaleReturnId);
            //构建初始化入参
            WholesaleReturnsDetailIn wholesaleReturnsDetailIn = new WholesaleReturnsDetailIn();
            //退货单
            wholesaleReturnsDetailIn.setWholesaleReturns(wholesaleReturns);
            //明细
            wholesaleReturnsDetailIn.setWholesaleReturnDetailList(wholesaleReturnDetails);
            //初始化退货单和明细
            this.initInventory(wholesaleReturnsDetailIn);

            //出货单收货状态和时间
            wholesaleReturnsDetailIn.getWholesaleReturns().setReturnStatus(ShipmentStatusEnum.RECEIPT.getCode());
            //修改退货单和明细
            wholesaleReturnsDetailIn.getWholesaleReturns().setUpdateTime(LocalDateTime.now());
            wholesaleReturnsDetailIn.getWholesaleReturns().setReceiveTime(LocalDateTime.now());

            //批量更新批发退和明细
            int updateCount = this.batchUpdate(wholesaleReturnsDetailIn);
            if (NumberUtil.INTEGER_ZERO.equals(updateCount)) {
                return Response.error(wholesaleReturnsDetailIn.getWholesaleReturns().getWholesaleReturnNo() + "收货失败");
            }

            //扣减库存初始化
            List<StockFlowIn> stockFlowIns = wholesaleReturnDetailService.addOrSubStock(wholesaleReturnsDetailIn, ShipmentStatusEnum.RECEIPT.getCode());
            //调用库存rpc调整库存
            Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
            if (!stockFlow.isSuccess()) {
                return Response.error("库存调整失败");
            }
            if (!clientDistInfoService.getIsIndependentAccountingByCode(wholesaleReturns.getClientCode(), wholesaleReturns.getBizOrgCode())) {
                //资管调用参数转换
                RechargeLiquidationIn rechargeLiquidationIn = wholesaleReturnDetailService.chargeParamToRechargeLiquidationIn(wholesaleReturnsDetailIn.getWholesaleReturns(), null, ShipmentStatusEnum.RECEIPT.getCode());
                if (BigDecimal.ZERO.compareTo(rechargeLiquidationIn.getLiquidationAmount()) != 0) {
                    //调用rpc调整资金
                    Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
                    if (!response.isSuccess()) {
                        throw new BusinessException("资管调整失败");
                    }
                }
            }
            //保存日志
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                    String.valueOf(wholesaleReturnsDetailIn.getWholesaleReturns().getId()),
                    OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(), OrdLogTypeEnum.ORD_RETURN_ARTIFICIAL.getName(),
                    new Date(), wholesaleReturnsDetailIn.getWholesaleReturns().getUpdater());

            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return Response.success("收货成功");
    }

    /**
     * 批发退货单回传
     *
     * @param wholesaleReBillVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean wholesaleReOrderCallBack(WholesaleReBillVO wholesaleReBillVO) {
//        WholesaleReBillVO wholesaleReBillVO = JSON.parseObject(messageJson, WholesaleReBillVO.class);
        WholesaleReturnsDetailIn wholesaleReturnsDetailIn = this.wholesaleReBillVOToWholesaleReturnsDetailIn(wholesaleReBillVO);
        //退货单
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        if (Objects.isNull(wholesaleReturns)) {
            log.error("退货单不存在");
//            throw new BusinessException("退货单不存在");
            return true;
        }
        if (!ShipmentStatusEnum.APPROVED.getCode().equals(wholesaleReturns.getReturnStatus())) {
            log.error("已审核状态下才能收货{}", wholesaleReturns.getWholesaleReturnNo());
//            return "已审核状态下才能收货";
            return true;
        }
        //已收货状态
        wholesaleReturns.setReturnStatus(ShipmentStatusEnum.RECEIPT.getCode());
        wholesaleReturns.setReceiveTime(LocalDateTime.now());
        wholesaleReturns.setUpdateTime(LocalDateTime.now());
        wholesaleReturns.setUpdater(wholesaleReBillVO.getFfiller());
        //更新订单状态为已收货状态并根据返回数据修改批发退货单数据
        int i = wholesaleReturnsMapper.updateOrder(wholesaleReturns, ShipmentStatusEnum.APPROVED.getCode());
        if (i == 0) {
            log.error("批发退{}处理失败或者已处理", wholesaleReturns.getWholesaleReturnNo());
//            throw new BusinessException("批发退处理失败或者已处理");
            return true;
        }
//        this.updateByPrimaryKeySelective(wholesaleReturns);

        //更新批发退货单详情数据
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnsDetailIn.getWholesaleReturnDetailList();
        for (WholesaleReturnDetail wholesaleReturnDetail : wholesaleReturnDetailList) {
            //根据主键更新每一条详情数据
            wholesaleReturnDetailService.updateByPrimaryKeySelective(wholesaleReturnDetail);
        }
        //库存调用参数转换
        List<StockFlowIn> stockFlowIns = wholesaleReturnDetailService.addOrSubStock(wholesaleReturnsDetailIn, ShipmentStatusEnum.RECEIPT.getCode());
        //调用库存rpc调整库存
        Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
        if (!stockFlow.isSuccess()) {
            throw new BusinessException(stockFlow.getMessage());
        }
        //  HS 省烟草HS不调用资管
        if (!clientDistInfoService.getIsIndependentAccountingByCode(wholesaleReturns.getClientCode(), wholesaleReturns.getBizOrgCode())
                && !this.checkIsHsReturn(wholesaleReturns.getSourceNo())) {
            //资管调用参数转换
            RechargeLiquidationIn rechargeLiquidationIn = wholesaleReturnDetailService.chargeParamToRechargeLiquidationIn(wholesaleReturns, null, ShipmentStatusEnum.RECEIPT.getCode());
            if (BigDecimal.ZERO.compareTo(rechargeLiquidationIn.getLiquidationAmount()) != 0) {
                //调用rpc调整资金
                Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
                if (!response.isSuccess()) {
                    throw new BusinessException("资管调整失败");
                }
            }
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                String.valueOf(wholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(), ShipmentStatusEnum.RECEIPT.getName(),
                new Date(), wholesaleReturns.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        if (StringUtils.isNotBlank(wholesaleReturns.getSourceNo()) && wholesaleReturns.getSourceNo().startsWith("DR")) {
            // 中科未上线，临时注释
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.ZK_WHOLESALE_RETURN_BACK, JSONObject.toJSONString(wholesaleReturnsDetailIn.getWholesaleReturns()),
//                    wholesaleReturnsDetailIn.getWholesaleReturns().getBizOrgCode(), wholesaleReturnsDetailIn.getWholesaleReturns().getWholesaleReturnNo());
            zKWholesaleReturnBackSender.sendSync(JSONObject.toJSONString(wholesaleReturnsDetailIn.getWholesaleReturns()).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
        }
        // 回调通知HS
        if (StringUtils.isNotBlank(wholesaleReturns.getSourceNo()) && wholesaleReturns.getSourceNo().startsWith("HS")) {
            HSWholesaleReturnOrderBackIn returnOrderBackIn = this.initHsWholesaleReturnOrderBackIn(wholesaleReturnDetailList, wholesaleReturns);
            // 单据状态回传
            hsWholesaleReturnDtsBackSender.sendSync(JSONObject.toJSONString(returnOrderBackIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
        }
//        return "成功";
        return true;
    }

    /**
     * @Description: 初始化物流回传HS批发退参数
     * @Author: ZhangYao
     * @Date: 2025/5/10 15:16
     * @param wholesaleReturnDetailList:
     * @param wholesaleReturns:
     * @return: com.edc.erp.wholesale.model.in.returns.HSWholesaleReturnOrderBackIn
     **/
    private static HSWholesaleReturnOrderBackIn initHsWholesaleReturnOrderBackIn(List<WholesaleReturnDetail> wholesaleReturnDetailList, WholesaleReturns wholesaleReturns) {
        List<HSWholesaleReturnDetail> detailList = wholesaleReturnDetailList.stream().filter(detail -> Objects.nonNull(detail.getStorageQuantity())).map(detail -> {
            HSWholesaleReturnDetail hsWholesaleReturnDetail = new HSWholesaleReturnDetail();
            hsWholesaleReturnDetail.setLine(detail.getLine());
            hsWholesaleReturnDetail.setGoodsCode(detail.getGoodsCode());
            hsWholesaleReturnDetail.setBarCode(detail.getBarCode());
            hsWholesaleReturnDetail.setIsGift(NumberUtil.INTEGER_ONE.toString());
            hsWholesaleReturnDetail.setQty(new BigDecimal(detail.getStorageQuantity()));
            return hsWholesaleReturnDetail;
        }).collect(Collectors.toList());
        HSWholesaleReturnOrderBackIn returnOrderBackIn = new HSWholesaleReturnOrderBackIn();
        returnOrderBackIn.setOrderNo(wholesaleReturns.getSourceNo());
        returnOrderBackIn.setErpOrderNo(wholesaleReturns.getWholesaleReturnNo());
        returnOrderBackIn.setBizOrgCode(wholesaleReturns.getBizOrgCode());
        returnOrderBackIn.setDetailList(detailList);
        return returnOrderBackIn;
    }

    /**
     * 将DTS回传数据转换为批发退货单详情入参数据
     *
     * @param wholesaleReBillVO
     * @return
     */
    private WholesaleReturnsDetailIn wholesaleReBillVOToWholesaleReturnsDetailIn(WholesaleReBillVO wholesaleReBillVO) {
        WholesaleReturnsDetailIn wholesaleReturnsDetailIn = new WholesaleReturnsDetailIn();
        WholesaleReturns wholesaleReturns = new WholesaleReturns();
        //根据批发退货单单号查询批发退货单相关信息
        String wholesaleReturnNo = wholesaleReBillVO.getFsrcnum();
        WholesaleReturnAndDetailOut wholesaleReturnDetailOut = this.getDetailByWholesaleReturnNo(wholesaleReturnNo);
        if (ObjectUtil.isNull(wholesaleReturnDetailOut)) {
            throw new BusinessException("回传订单不存在，请查证，回传订单号：" + wholesaleReturnNo);
        }
        //根据退货单关联的出货单单号查询出货单
        Map<String, WholesaleShipmentDetailIn> shipmentMap = this.getShipmentByWholesaleShipmentNo(wholesaleReturnDetailOut.getWholesaleShipmentNo(), wholesaleReturnDetailOut.getBizOrgCode());
        //拷贝相同的数据
        BeanUtil.copyProperties(wholesaleReturnDetailOut, wholesaleReturns);
        //设置物流单号
        wholesaleReturns.setTrackingNo(wholesaleReBillVO.getNum());
        //设置备注
        wholesaleReturns.setRemark(wholesaleReBillVO.getFmemo());
        //设置仓位代码
        wholesaleReturns.setStorageStockCode(wholesaleReBillVO.getFwrh());
        //设置修改时间
        wholesaleReturns.setUpdateTime(wholesaleReBillVO.getFsendtime());
        //将申请数量、申请金额、入库数量及实际入库金额设置为0
        wholesaleReturns.setApplicationQuantity(0);
        wholesaleReturns.setApplicationAmount(new BigDecimal(0));
        wholesaleReturns.setStorageQuantity(0);
        wholesaleReturns.setStorageAmount(new BigDecimal(0));
        //获取根据单号查询出来的商品详情信息
        List<WholesaleReturnDetailAndGoodsStrOut> wholesaleReturnDetailList = wholesaleReturnDetailOut.getWholesaleReturnDetailList();
        List<WholesaleReturnDetail> returnDetailList = new ArrayList<>();
        for (WholesaleReturnDetailAndGoodsStrOut wholesaleReturnDetailAndGoodsStrOut : wholesaleReturnDetailList) {
            WholesaleReturnDetail wholesaleReturnDetail = new WholesaleReturnDetail();
            BeanUtil.copyProperties(wholesaleReturnDetailAndGoodsStrOut, wholesaleReturnDetail);
            returnDetailList.add(wholesaleReturnDetail);
        }
        List<WholesaleReBillDtlVO> detail = wholesaleReBillVO.getDetail();
        //转换批发退货详情数据
        for (WholesaleReBillDtlVO wholesaleReBillDtlVO : detail) {
            for (WholesaleReturnDetail wholesaleReturnDetail : returnDetailList) {
                String dbDetailKey = wholesaleReturnDetail.getGoodsCode() + wholesaleReturnDetail.getLine();
                String dtsDetailKey = wholesaleReBillDtlVO.getFarticlecode() + wholesaleReBillDtlVO.getLine();
                //判断如果当前对象的商品代码与DTS回传的商品代码相同
                if (dbDetailKey.equals(dtsDetailKey)) {
                    //相同，根据回传信息更新当前商品详情数据
                    //设置实际入库数量
                    wholesaleReturnDetail.setStorageQuantity(wholesaleReBillDtlVO.getFqty().intValue());
                    //设置实际入库金额
                    wholesaleReturnDetail.setPracticalStorageAmount(wholesaleReturnDetail.getReturnsPrice().multiply(wholesaleReBillDtlVO.getFqty()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    //远程调用查询商品信息详情
                    QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
                    querySaleGoodsInfoIn.setClientCode(wholesaleReturnDetailOut.getClientCode());
                    querySaleGoodsInfoIn.setGoodsCode(wholesaleReturnDetail.getGoodsCode());
                    querySaleGoodsInfoIn.setStockCode(wholesaleReturns.getStorageStockCode());
                    //查询仓位id
                    StockInfoOut stockInfoOut = stockServer.getByCode(wholesaleReturns.getStorageStockCode(), wholesaleReturns.getBizOrgCode());
                    querySaleGoodsInfoIn.setStockId(stockInfoOut.getId());
                    querySaleGoodsInfoIn.setBizOrgCode(wholesaleReturns.getBizOrgCode());
                    querySaleGoodsInfoIn.setWarehouseCode(wholesaleReturns.getStorageWrh());
                    querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.RETURNS.getCode());
                    Response<SaleGoodsInfoOut> goodsResult = saleGoodsInfoClient.getGoodsInfo(querySaleGoodsInfoIn);
                    if (!goodsResult.isSuccess() || Objects.isNull(goodsResult.getData())) {
                        throw new BusinessException(goodsResult.getMessage());
                    }
                    //获取批发退货单对应的出货单明细
                    WholesaleShipmentDetailIn detailIn = shipmentMap.get(wholesaleReturnDetail.getGoodsCode());
                    //计算其他属性
                    wholesaleReturnsGoodsService.computeWholesaleReturnTaxByDTSBack(wholesaleReturnDetail, goodsResult.getData(), Objects.nonNull(detailIn) ? detailIn.getInventoryPrice() : null);
                    //设置修改时间
                    wholesaleReturnDetail.setUpdateTime(wholesaleReBillDtlVO.getFsendtime());
                }
            }
        }
        wholesaleReturnsDetailIn.setWholesaleReturnDetailList(returnDetailList);
        wholesaleReturnsDetailIn.setWholesaleReturns(wholesaleReturns);
        return this.countParam(wholesaleReturnsDetailIn);
    }


    /**
     * 初始化退货单和明细
     * @param wholesaleReturnsDetailIn 构建初始化入参类
     */
    private void initInventory(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        //获取出货单
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        //获取明细
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnsDetailIn.getWholesaleReturnDetailList();
        for (WholesaleReturnDetail wholesaleReturnDetail : wholesaleReturnDetailList) {
            //查询商品信息
            SaleGoodsInfoOut saleGoodsInfo = this.getSaleGoodsInfo(wholesaleReturns, wholesaleReturnDetail.getGoodsCode(), wholesaleReturns.getBizOrgCode());
            //获取审核数量
            Integer checkQuantity = wholesaleReturnDetail.getCheckQuantity();
            //销项税率 如果为null 则为 1 + 0 否则 税率 + 1
            BigDecimal outTaxAddOne = Objects.nonNull(saleGoodsInfo) && Objects.nonNull(saleGoodsInfo.getOutTax()) ? saleGoodsInfo.getOutTax().add(BigDecimal.ONE) : BigDecimal.ZERO.add(BigDecimal.ONE);
            //税率取消 + 1
//            BigDecimal outTax = Objects.nonNull(saleGoodsInfo) && Objects.nonNull(saleGoodsInfo.getOutTax()) ? saleGoodsInfo.getOutTax() : BigDecimal.ZERO;
            //入库数量即审核数量
            wholesaleReturnDetail.setStorageQuantity(checkQuantity);
            //实际入库金额
            BigDecimal practicalStorageAmount = wholesaleReturnDetail.getReturnsPrice().multiply(BigDecimal.valueOf(checkQuantity));
            wholesaleReturnDetail.setPracticalStorageAmount(practicalStorageAmount);
            //入库去税金额
            BigDecimal storageNetProfit = practicalStorageAmount.divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setStorageNetProfit(storageNetProfit);
            //入库税额
            wholesaleReturnDetail.setStorageTax(practicalStorageAmount.subtract(storageNetProfit).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));

            //根据是否关联出货单 进行库存价调整以及成本金额 税额计算
            BigDecimal inventoryPrice = BigDecimal.ZERO;
            //成本金额计算值 关联出货单 则为库存价 不关联则取退货单价
            BigDecimal costPrice = BigDecimal.ZERO;
            if (StringUtils.isBlank(wholesaleReturns.getWholesaleShipmentNo())) {
                //不存在 则取最新库存价
                inventoryPrice = Objects.isNull(saleGoodsInfo) || Objects.isNull(saleGoodsInfo.getInventoryPrice()) ? BigDecimal.ZERO : saleGoodsInfo.getInventoryPrice();
                //退货单价
                costPrice = wholesaleReturnDetail.getReturnsPrice();
            } else {
                //如果关联出货单 则取已经存在的库存价即关联的出货单库存价
                inventoryPrice = wholesaleReturnDetail.getInventoryPrice();
                //库存价
                costPrice = inventoryPrice;
            }
            //库存价
            wholesaleReturnDetail.setInventoryPrice(inventoryPrice);
            //成本金额
            BigDecimal costAmount = costPrice.multiply(BigDecimal.valueOf(checkQuantity).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            wholesaleReturnDetail.setCostAmount(costAmount);
            //成本去税金额
            BigDecimal costNetProfitAmount = costAmount.divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            wholesaleReturnDetail.setCostNetProfitAmount(costNetProfitAmount);
            //成本税额
            wholesaleReturnDetail.setCostTax(costAmount.subtract(costNetProfitAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //更新时间
            wholesaleReturnDetail.setUpdateTime(LocalDateTime.now());
        }
        //统计明细中的入库数量
        wholesaleReturns.setStorageQuantity(wholesaleReturnDetailList.stream().mapToInt(WholesaleReturnDetail::getStorageQuantity).sum());
        //统计入库金额
        wholesaleReturns.setStorageAmount(wholesaleReturnDetailList.stream().map(WholesaleReturnDetail::getPracticalStorageAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }


    /**
     * 根据退货单id查询退货单明细
     * @param wholesaleReturnId 退货单id
     * @return
     */
    private List<WholesaleReturnDetail> findDetailByWholesaleReturnId(Long wholesaleReturnId) {
        WholesaleReturnDetail wholesaleReturnDetail = new WholesaleReturnDetail();
        wholesaleReturnDetail.setWholesaleReturnId(wholesaleReturnId);
        wholesaleReturnDetail.setIsDelete(ModelConst.DELETE.NO);
        //根据退货单主键获取退货单详情信息
        return wholesaleReturnDetailService.list(wholesaleReturnDetail);
    }


    /**
     * 查询商品信息
     * @param wholesaleReturns 退货单信息
     * @param goodsCode 商品代码
     * @param bizOrgCode    业务组织
     * @return
     */
    private SaleGoodsInfoOut getSaleGoodsInfo(WholesaleReturns wholesaleReturns, String goodsCode, String bizOrgCode) {
        //调用rpc获取商品信息以及包装规格、批发价、库存价
        QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
        //业务组织
        querySaleGoodsInfoIn.setBizOrgCode(bizOrgCode);
        //商品代码
        querySaleGoodsInfoIn.setGoodsCode(goodsCode);
        //批发退类型
        querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.RETURNS.getCode());
        //客户代码
        querySaleGoodsInfoIn.setClientCode(wholesaleReturns.getClientCode());
        //仓位
        querySaleGoodsInfoIn.setStockCode(wholesaleReturns.getStorageStockCode());
        //仓储
        querySaleGoodsInfoIn.setWarehouseCode(wholesaleReturns.getStorageWrh());
        //查询商品信息
        Response<SaleGoodsInfoOut> goodsInfo = saleGoodsInfoClient.getGoodsInfo(querySaleGoodsInfoIn);
        if (!goodsInfo.isSuccess() || Objects.isNull(goodsInfo.getData())) {
            throw new BusinessException(goodsInfo.getMessage());
        }
        return goodsInfo.getData();
    }

    /**
     * 批量更新批发退和明细
     * @param wholesaleReturnsDetailIn 批发退保存入参
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdate(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        //更新批发退货单
        int updateCount = wholesaleReturnsMapper.updateOrder(wholesaleReturnsDetailIn.getWholesaleReturns(), ShipmentStatusEnum.APPROVED.getCode());
        if (updateCount == 0) {
            throw new BusinessException("批发退处理失败或者已处理");
        }
//        int updateCount = this.updateByPrimaryKeySelective(wholesaleReturnsDetailIn.getWholesaleReturns());
        //更新明细
        wholesaleReturnDetailService.batchUpdate(wholesaleReturnsDetailIn.getWholesaleReturnDetailList());
        return updateCount;
    }

    /**
     * 校验商品代码
     *
     * @param wholesaleReturnsDetailIn
     * @return
     */
    public Response checkGoodsCode(WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        WholesaleReturns wholesaleReturns = wholesaleReturnsDetailIn.getWholesaleReturns();
        //获取批发出货单单号
        String wholesaleShipmentNo = wholesaleReturns.getWholesaleShipmentNo();
        //根据退货单关联的出货单单号查询出货单
        Map<String, WholesaleShipmentDetailIn> shipmentMap = this.getShipmentByWholesaleShipmentNo(wholesaleShipmentNo, wholesaleReturns.getBizOrgCode());
        //查询仓位
        StockInfoOut stockInfoOut = stockServer.getByCode(wholesaleReturns.getStorageStockCode(), wholesaleReturns.getBizOrgCode());
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnsDetailIn.getWholesaleReturnDetailList();
        List<WholesaleReturnDetail> wholesaleReturnDetailListTemp = new ArrayList<>();
        for (WholesaleReturnDetail wholesaleReturnDetail : wholesaleReturnDetailList) {
            //根据商品代码获取对应的出货单明细
            WholesaleShipmentDetailIn detailIn = shipmentMap.get(wholesaleReturnDetail.getGoodsCode());
            //调用校验每一条详情数据是否正确
            String stockCode = wholesaleReturns.getStorageStockCode();
            String warehouseCode = wholesaleReturnsDetailIn.getWarehouseCode();
            Integer stockId = Objects.nonNull(stockInfoOut) ? stockInfoOut.getId() : null;
            String clientCode = wholesaleReturns.getClientCode();
            Response response = wholesaleReturnsGoodsService.checkWholesaleReturnDetail(wholesaleReturnDetail, clientCode, stockCode, warehouseCode, stockId, detailIn, wholesaleReturns.getReturnStatus());
            if (!response.isSuccess()) {
                return Response.error(response.getMessage());
            }
            WholesaleReturnDetail data = (WholesaleReturnDetail) response.getData();
            wholesaleReturnDetailListTemp.add(data);
        }
        return Response.data(wholesaleReturnDetailListTemp);
    }


    /**
     * 根据退货单关联的出货单单号查询出货单
     * @param wholesaleShipmentNo 出货单单号
     * @param bizOrgCode 业务组织
     * @return
     */
    @Override
    public Map<String, WholesaleShipmentDetailIn> getShipmentByWholesaleShipmentNo(String wholesaleShipmentNo, String bizOrgCode) {
        if (StringUtils.isBlank(wholesaleShipmentNo)) {
            return new HashMap<>();
        }
        //查询批发出货单
        WholesaleShipment wholesaleShipment = new WholesaleShipment();
        wholesaleShipment.setShipmentNo(wholesaleShipmentNo);
        wholesaleShipment.setBizOrgCode(bizOrgCode);
        wholesaleShipment.setIsDelete(ModelConst.DELETE.NO);
        WholesaleShipment dbShipment = wholesaleShipmentMapper.selectOne(wholesaleShipment);
        //查询批发出货单明细
        QueryShipmentDetailIn queryShipmentDetailIn = new QueryShipmentDetailIn();
        queryShipmentDetailIn.setWholesaleShipmentId(Objects.nonNull(dbShipment) ? dbShipment.getId() : 0L);
        List<WholesaleShipmentDetailIn> shipmentDetailList = wholesaleShipmentDetailService.findShipmentDetailList(queryShipmentDetailIn);
        if (CollectionUtils.isEmpty(shipmentDetailList)) {
            return new HashMap<>();
        }
        //根据商品代码转换出货单明细map
        return shipmentDetailList.stream().filter(d -> StringUtils.isNotBlank(d.getGoodsCode())).collect(Collectors.toMap(WholesaleShipmentDetail::getGoodsCode, item -> item, (v1, v2) -> v2));
    }

    @Override
    public WholesaleReturns getOneById(Long id) {
        return wholesaleReturnsMapper.selectByPrimaryKey(id);
    }

    @Override
    public int countOneBySourceNo(String sourceNo, String bizOrgCode) {
        WholesaleReturns wholesaleReturns = new WholesaleReturns();
        wholesaleReturns.setSourceNo(sourceNo);
        wholesaleReturns.setBizOrgCode(bizOrgCode);
        wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleReturnsMapper.selectCount(wholesaleReturns);
    }

    @Override
    public WholesaleReturns getOneByWholesaleReturnNo(String wholesaleReturnNo, String bizOrgCode) {
        WholesaleReturns wholesaleReturns = new WholesaleReturns();
        wholesaleReturns.setWholesaleReturnNo(wholesaleReturnNo);
        wholesaleReturns.setBizOrgCode(bizOrgCode);
        wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleReturnsMapper.selectOne(wholesaleReturns);
    }

    @Override
    public String exportWholesaleReturnOrder(WholesaleReturnsListIn wholesaleReturnsListIn) {
        //设置每次查询条数
        wholesaleReturnsListIn.setPageSize(5000);
        //excel 文件名
        String fileName = "批发退货单信息";
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx"),
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        fileName,
                        //导出模板实体
                        ExportWholesaleReturnOrder.class,
                        // 分页查询对象
                        wholesaleReturnsListIn,
                        //excel对象集合
                        page -> {
                            List<ExportWholesaleReturnOrder> batchExportShipmentOrders = this.buildExcelReturnOrders(wholesaleReturnsListIn);
                            log.info("批发退货单列表导出集合大小是----------{}", batchExportShipmentOrders.size());
                            return batchExportShipmentOrders;
                        }

                )
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    @Override
    public WholesaleReturnDateInfoOut sumWholesaleReturnDateInfo(WholesaleReturnsListIn wholesaleReturnsListIn) {
        WholesaleReturnDateInfoOut totalWholesaleReturnDateInfoOut = new WholesaleReturnDateInfoOut();
        totalWholesaleReturnDateInfoOut.setTotalApplyQuantity(NumberUtil.INTEGER_ZERO);
        totalWholesaleReturnDateInfoOut.setTotalApplyAmount(BigDecimal.ZERO);
        totalWholesaleReturnDateInfoOut.setTotalCheckQuantity(NumberUtil.INTEGER_ZERO);
        totalWholesaleReturnDateInfoOut.setTotalCheckAmount(BigDecimal.ZERO);
        totalWholesaleReturnDateInfoOut.setTotalStorageQuantity(NumberUtil.INTEGER_ZERO);
        totalWholesaleReturnDateInfoOut.setTotalPracticalStorageAmount(BigDecimal.ZERO);
        List<Long> idList = wholesaleReturnsMapper.findNeedSumWholesaleReturnIdList(wholesaleReturnsListIn);
        if (CollectionUtils.isEmpty(idList)) {
            return totalWholesaleReturnDateInfoOut;
        }
        int listSize = idList.size();
        for (int i = 0; i < listSize; i += batchSize) {
            // 获取当前批次的子列表
            int endIndex = Math.min(i + batchSize, listSize);
            List<Long> subIdList = idList.subList(i, endIndex);
            WholesaleReturnDateInfoOut wholesaleReturnDateInfoOut = wholesaleReturnDetailService.sumWholesaleReturnDateInfoByIdList(subIdList);
            if (Objects.nonNull(wholesaleReturnDateInfoOut)) {
                if (null != wholesaleReturnDateInfoOut.getTotalApplyQuantity()) {
                    totalWholesaleReturnDateInfoOut.setTotalApplyQuantity(totalWholesaleReturnDateInfoOut.getTotalApplyQuantity() + wholesaleReturnDateInfoOut.getTotalApplyQuantity());
                    totalWholesaleReturnDateInfoOut.setTotalApplyAmount(totalWholesaleReturnDateInfoOut.getTotalApplyAmount().add(wholesaleReturnDateInfoOut.getTotalApplyAmount()));
                }
                if (null != wholesaleReturnDateInfoOut.getTotalCheckQuantity()) {
                    totalWholesaleReturnDateInfoOut.setTotalCheckQuantity(totalWholesaleReturnDateInfoOut.getTotalCheckQuantity() + wholesaleReturnDateInfoOut.getTotalCheckQuantity());
                    totalWholesaleReturnDateInfoOut.setTotalCheckAmount(totalWholesaleReturnDateInfoOut.getTotalCheckAmount().add(wholesaleReturnDateInfoOut.getTotalCheckAmount()));
                }
                if (null != wholesaleReturnDateInfoOut.getTotalStorageQuantity()) {
                    totalWholesaleReturnDateInfoOut.setTotalStorageQuantity(totalWholesaleReturnDateInfoOut.getTotalStorageQuantity() + wholesaleReturnDateInfoOut.getTotalStorageQuantity());
                    totalWholesaleReturnDateInfoOut.setTotalPracticalStorageAmount(totalWholesaleReturnDateInfoOut.getTotalPracticalStorageAmount().add(wholesaleReturnDateInfoOut.getTotalPracticalStorageAmount()));
                }
            }
        }
        totalWholesaleReturnDateInfoOut.setTotalApplyAmount(totalWholesaleReturnDateInfoOut.getTotalApplyAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        totalWholesaleReturnDateInfoOut.setTotalCheckAmount(totalWholesaleReturnDateInfoOut.getTotalCheckAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        totalWholesaleReturnDateInfoOut.setTotalPracticalStorageAmount(totalWholesaleReturnDateInfoOut.getTotalPracticalStorageAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        return totalWholesaleReturnDateInfoOut;
    }

    @Override
    public WholesaleReturns getOneByOrderNo(String orderNo) {
        WholesaleReturns wholesaleReturns = new WholesaleReturns();
        wholesaleReturns.setWholesaleReturnNo(orderNo);
        wholesaleReturns.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleReturnsMapper.selectOne(wholesaleReturns);
    }

    @Override
    public boolean checkIsHsReturn(String sourceNo) {
        if (StringUtils.isBlank(sourceNo)) {
            return false;
        }
        //  hs
        return sourceNo.startsWith("HS");
    }

    private List<ExportWholesaleReturnOrder> buildExcelReturnOrders(WholesaleReturnsListIn wholesaleReturnsListIn) {
        List<WholesaleReturnsListOut> wholesaleReturnsList = wholesaleReturnsMapper.findWholesaleReturnsByPage(wholesaleReturnsListIn);
        if (CollectionUtils.isEmpty(wholesaleReturnsList)) {
            log.info("无单据可导出");
            return Lists.newArrayList();
//            throw new BusinessException("无单据可导出");
        }
        List<ExportWholesaleReturnOrder> exportWholesaleReturnOrderList = wholesaleReturnsList.stream().map(wholesaleReturnsOut -> {
            ExportWholesaleReturnOrder exportWholesaleReturnOrder = new ExportWholesaleReturnOrder();
            BeanUtils.copy(wholesaleReturnsOut, exportWholesaleReturnOrder);

            ClientWholesaleConfigIn clientWholesaleConfigIn = new ClientWholesaleConfigIn();
            clientWholesaleConfigIn.setPriceGroupCode(wholesaleReturnsOut.getPriceGroupCode());
            clientWholesaleConfigIn.setBizOrgCode(wholesaleReturnsListIn.getBizOrgCode());
            clientWholesaleConfigIn.setClientCode(wholesaleReturnsOut.getClientCode());
            List<ClientWholesaleConfigOut> wholesalePriceGroupList = wholesaleService.findWholesalePriceGroup(clientWholesaleConfigIn);
            for (ClientWholesaleConfigOut clientWholesaleConfigOut : wholesalePriceGroupList) {
                if (clientWholesaleConfigOut.getPriceGroupCode().equals(wholesaleReturnsOut.getPriceGroupCode())) {
                    String priceGroup = clientWholesaleConfigOut.getPriceGroup();
                    exportWholesaleReturnOrder.setPriceGroup(priceGroup);
                    exportWholesaleReturnOrder.setClientMessage("【" + wholesaleReturnsOut.getClientCode() + "】" + clientWholesaleConfigOut.getClientName());
                }
                break;
            }
            //退货单状态中文转换
            exportWholesaleReturnOrder.setReturnStatusStr(ShipmentStatusEnum.getNameByCode(wholesaleReturnsOut.getReturnStatus()));
            exportWholesaleReturnOrder.setIsReversalOrderStr(NumberUtil.INTEGER_ZERO.equals(wholesaleReturnsOut.getIsReversalOrder()) ? "否" : "是");
            exportWholesaleReturnOrder.setIsReversalStr(NumberUtil.INTEGER_ZERO.equals(wholesaleReturnsOut.getIsReversal()) ? "否" : "是");
            return exportWholesaleReturnOrder;
        }).collect(Collectors.toList());
        return exportWholesaleReturnOrderList;
    }


}
