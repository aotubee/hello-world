package com.edc.erp.wholesale.handle;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OperateLogTypeEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.model.in.customer.QueryClientDistInfoIn;
import com.edc.erp.common.model.in.goods.QuerySaleGoodsInfoIn;
import com.edc.erp.common.model.in.zk.ZKWholesaleReturnBackIn;
import com.edc.erp.common.model.in.zk.ZKWholesaleReturnDetailBackIn;
import com.edc.erp.common.model.in.zk.ZKWholesaleShipmentBackIn;
import com.edc.erp.common.model.in.zk.ZKWholesaleShipmentDetailBackIn;
import com.edc.erp.common.model.out.customer.ClientDistInfoOut;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.rpc.SaleGoodsInfoClient;
import com.edc.erp.common.service.ClientDistInfoService;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.ZKServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.model.in.zk.*;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.enumeration.WholesaleOrderTypeEnum;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.returns.service.WholesaleReturnDetailService;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName ZKWholesaleBusinessHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/6 13:39
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class ZKWholesaleBusinessHandle {

    private final SaleGoodsInfoClient saleGoodsInfoClient;

    private final StockServer stockServer;

    private final ClientDistInfoService clientDistInfoService;

    private final ZKWholesaleBusinessDBHandle zkWholesaleBusinessDBHandle;

    private final WholesaleReturnsService wholesaleReturnsService;

    private final WholesaleShipmentService wholesaleShipmentService;

    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;

    private final ZKServer zkServer;

    private final AsyncLogService asyncLogService;

    private final WholesaleReturnDetailService wholesaleReturnDetailService;

    private final RedisService redisService;

    /**
     * @Description: 中科申请创建批发出货单
     * @Author: ZhangYao
     * @Date: 2023/12/12 15:11
     * @param taskZKWholesaleShipmentSaveIn:
     * @return: com.edc.plugins.common.response.Response<java.lang.String>
     **/
    public Response<String> handleZkWholesaleShipment(TaskZKWholesaleShipmentSaveIn taskZKWholesaleShipmentSaveIn) {
//        JSONObject jsonObject = JSONObject.parseObject(message);
//        if (null == jsonObject) {
//            return Response.success("中科创建批发出参数为空");
//        }
//        log.info("中科创建批发出消费者入参----------------->" + jsonObject.toJSONString());
//        TaskZKWholesaleShipmentSaveIn taskZKWholesaleShipmentSaveIn = JSONObject.parseObject(jsonObject.toJSONString(), TaskZKWholesaleShipmentSaveIn.class);
        ZKWholesaleShipmentIn zkWholesaleShipmentIn = taskZKWholesaleShipmentSaveIn.getZkWholesaleShipmentIn();
        int count = wholesaleShipmentService.countOneBySourceNo(zkWholesaleShipmentIn.getSourceNo(), taskZKWholesaleShipmentSaveIn.getBizOrgCode());
        if (count > NumberUtil.INTEGER_ZERO) {
            log.info("中科{}创建ERP批发出,来源单号重复请求", zkWholesaleShipmentIn.getSourceNo());
            return Response.success();
        }
        taskZKWholesaleShipmentSaveIn.getZkWholesaleShipmentDetailInList();
        StockInfoOut stockInfoOut = stockServer.getTransInfo(zkWholesaleShipmentIn.getShipmentStockCode());
        if (Objects.isNull(stockInfoOut)) {
            log.error("中科{}创建ERP批发出{},出库仓位{}不存在", zkWholesaleShipmentIn.getSourceNo(), taskZKWholesaleShipmentSaveIn.getWholesaleShipmentNo(), zkWholesaleShipmentIn.getShipmentStockCode());
            throw new BusinessException("中科" + zkWholesaleShipmentIn.getSourceNo() + "创建ERP批发出{}" + taskZKWholesaleShipmentSaveIn.getWholesaleShipmentNo() + "出库仓位" + zkWholesaleShipmentIn.getShipmentStockCode() + "不存在");
        }
        List<ZKWholesaleShipmentDetailIn> zkWholesaleShipmentDetailInList = taskZKWholesaleShipmentSaveIn.getZkWholesaleShipmentDetailInList();
        // 查询客户信息
        List<ClientDistInfoOut> clientDistInfos = clientDistInfoService.findClientDistInfo(new QueryClientDistInfoIn(zkWholesaleShipmentIn.getClientCode(), taskZKWholesaleShipmentSaveIn.getBizOrgCode()));
        if (CollectionUtils.isEmpty(clientDistInfos)) {
            log.error("中科{}创建批发出查询客户代码{}-----收货信息校验异常", zkWholesaleShipmentIn.getSourceNo(), zkWholesaleShipmentIn.getClientCode());
            throw new BusinessException("中科" + zkWholesaleShipmentIn.getSourceNo() + "创建批发出查询客户" + zkWholesaleShipmentIn.getClientCode() + "与收货人校验失败");
        }
        ClientDistInfoOut clientDistInfo = clientDistInfos.get(0);
        List<String> goodsCodeList = zkWholesaleShipmentDetailInList.stream().map(ZKWholesaleShipmentDetailIn::getGoodsCode).collect(Collectors.toList());
        //调用rpc获取商品信息以及包装规格、批发价、库存价
        QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
        querySaleGoodsInfoIn.setBizOrgCode(taskZKWholesaleShipmentSaveIn.getBizOrgCode());
        querySaleGoodsInfoIn.setGoodsCodeList(goodsCodeList);
        querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.OUT.getCode());
        querySaleGoodsInfoIn.setClientCode(zkWholesaleShipmentIn.getClientCode());
//        querySaleGoodsInfoIn.setStockId(stockInfoOut.getId());
        querySaleGoodsInfoIn.setStockCode(zkWholesaleShipmentIn.getShipmentStockCode());
        querySaleGoodsInfoIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
        querySaleGoodsInfoIn.setPriceGroupCode(clientDistInfo.getPriceGroupCode());
        //查询商品信息
        Response<List<SaleGoodsInfoOut>> response = saleGoodsInfoClient.findGoodsInfo(querySaleGoodsInfoIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error("中科{}创建批发出查询商品异常", zkWholesaleShipmentIn.getSourceNo(), response.getMessage());
            throw new BusinessException(response.getMessage());
        }
        Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap = response.getData().stream().collect(Collectors.toMap(SaleGoodsInfoOut::getGoodsCode, Function.identity()));
        taskZKWholesaleShipmentSaveIn.setShipmentWrh(stockInfoOut.getWarehouseCode());
        zkWholesaleBusinessDBHandle.saveZKWholesaleShipment(taskZKWholesaleShipmentSaveIn, saleGoodsInfoOutMap, clientDistInfo);
        return Response.success("创建成功");
    }

    /**
     * @Description: 回传中科批发出货单
     * @Author: ZhangYao
     * @Date: 2023/12/12 15:11
     * @param wholesaleShipment:
     * @return: com.edc.plugins.common.response.Response<java.lang.String>
     **/
    public Response<String> handleZkWholesaleShipmentBack(WholesaleShipment wholesaleShipment) {

        WholesaleShipment dbWholesaleShipment = wholesaleShipmentService.getOneByNo(wholesaleShipment.getShipmentNo(), wholesaleShipment.getBizOrgCode());
        if (Objects.isNull(dbWholesaleShipment)) {
            return Response.success("批发出货单不存在");
        }
        if (!ShipmentStatusEnum.SHIPPED.getCode().equals(dbWholesaleShipment.getShipmentStatus()) && !ShipmentStatusEnum.INVALID.getCode().equals(dbWholesaleShipment.getShipmentStatus())) {
            return Response.success("批发出货单状态不正确");
        }
        List<WholesaleShipmentDetail> wholesaleShipmentDetailList = wholesaleShipmentDetailService.findListByShipmentId(dbWholesaleShipment.getId());
        List<ZKWholesaleShipmentDetailBackIn> detailList = wholesaleShipmentDetailList.stream().map(wholesaleShipmentDetail -> {
            ZKWholesaleShipmentDetailBackIn zkWholesaleShipmentDetailBackIn = new ZKWholesaleShipmentDetailBackIn();
            zkWholesaleShipmentDetailBackIn.setItemNo(wholesaleShipmentDetail.getGoodsCode());
            if (ShipmentStatusEnum.INVALID.getCode().equals(dbWholesaleShipment.getShipmentStatus())) {
                zkWholesaleShipmentDetailBackIn.setItemQty(NumberUtil.INTEGER_ZERO.toString());
                log.info("批发出{}作废回传", dbWholesaleShipment.getShipmentNo());
            } else {
                if (Objects.isNull(wholesaleShipmentDetail.getShipmentQuantity()) || wholesaleShipmentDetail.getShipmentQuantity().compareTo(NumberUtil.INTEGER_ZERO) == NumberUtil.INTEGER_ZERO) {
                    zkWholesaleShipmentDetailBackIn.setItemQty(NumberUtil.INTEGER_ZERO.toString());
                } else {
                    zkWholesaleShipmentDetailBackIn.setItemQty(wholesaleShipmentDetail.getShipmentQuantity().toString());
                }
            }
            zkWholesaleShipmentDetailBackIn.setLine(wholesaleShipmentDetail.getLine().toString());
            return zkWholesaleShipmentDetailBackIn;
        }).collect(Collectors.toList());
        ZKWholesaleShipmentBackIn zkWholesaleShipmentBackIn = new ZKWholesaleShipmentBackIn();
        zkWholesaleShipmentBackIn.setOrderNo(dbWholesaleShipment.getShipmentNo());
        zkWholesaleShipmentBackIn.setSourceOrderNo(dbWholesaleShipment.getSourceNo());
        if (ShipmentStatusEnum.SHIPPED.getCode().equals(dbWholesaleShipment.getShipmentStatus())) {
            zkWholesaleShipmentBackIn.setOrderNoDate(DateUtils.format(dbWholesaleShipment.getDeliveryTime()));
        } else {
            zkWholesaleShipmentBackIn.setOrderNoDate(DateUtils.format(dbWholesaleShipment.getUpdateTime()));
        }
        zkWholesaleShipmentBackIn.setDetailList(detailList);
        Response<String> response = zkServer.sendWholesaleShipmentBack(zkWholesaleShipmentBackIn);
        log.info("中科批发出回传{}请求返回---------->{}", zkWholesaleShipmentBackIn.getOrderNo(), JSONObject.toJSONString(response));
        if (null != response && response.isSuccess()) {
            //保存出货单审核日志
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getName(),
                    String.valueOf(dbWholesaleShipment.getId()), OrdLogTypeEnum.ORD_WHOLESALE_SHIPMEN.getCode(),
                    OperateLogTypeEnum.ZK_WHOLESALE_SHIPMENT_BACK.getName(), new Date(), SystemConstant.SYSTEM_USER);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.success();
        } else {
            log.info("{}发送中科批发出回传异常---{}", zkWholesaleShipmentBackIn.getOrderNo(), response.getMessage());
            throw new RuntimeException("发送中科批发出回传异常" + response.getMessage());
        }
    }

    /**
     * @Description: 中科创建批发退货单
     * @Author: ZhangYao
     * @Date: 2023/12/12 15:12
     * @param taskZKWholesaleReturnSaveIn:
     * @return: com.edc.plugins.common.response.Response<java.lang.String>
     **/
    public Response<String> handleZkWholesaleReturn(TaskZKWholesaleReturnSaveIn taskZKWholesaleReturnSaveIn) {
//        JSONObject jsonObject = JSONObject.parseObject(message);
//        if (null == jsonObject) {
//            return Response.error("中科创建批发退参数为空");
//        }
//        log.info("中科创建批发退消费者入参----------------->" + jsonObject.toJSONString());
//        TaskZKWholesaleReturnSaveIn taskZKWholesaleReturnSaveIn = JSONObject.parseObject(jsonObject.toJSONString(), TaskZKWholesaleReturnSaveIn.class);
        ZKWholesaleReturnIn zkWholesaleReturnIn = taskZKWholesaleReturnSaveIn.getZkWholesaleReturnIn();
        String wholesaleShipmentNo = zkWholesaleReturnIn.getWholesaleShipmentNo();
        String bizOrgCode = taskZKWholesaleReturnSaveIn.getBizOrgCode();
        int count = wholesaleReturnsService.countOneBySourceNo(zkWholesaleReturnIn.getSourceNo(), bizOrgCode);
        if (count > NumberUtil.INTEGER_ZERO) {
            log.info("中科创建ERP批发退,来源单号{}重复请求", zkWholesaleReturnIn.getSourceNo());
            return Response.success();
        }
        List<ZKWholesaleReturnDetailIn> wholesaleReturnDetailInList = taskZKWholesaleReturnSaveIn.getZkWholesaleReturnDetailInList();
        List<String> goodsCodeList = wholesaleReturnDetailInList.stream().map(ZKWholesaleReturnDetailIn::getGoodsCode).collect(Collectors.toList());
        StockInfoOut stockInfoOut = stockServer.getTransInfo(zkWholesaleReturnIn.getStorageStockCode());
        if (Objects.isNull(stockInfoOut)) {
            log.error("中科{}创建ERP批发退{},入库仓位{}不存在", zkWholesaleReturnIn.getSourceNo(), taskZKWholesaleReturnSaveIn.getWholesaleReturnNo(), zkWholesaleReturnIn.getStorageStockCode());
            throw new BusinessException("中科" + zkWholesaleReturnIn.getSourceNo() + "创建ERP批发出{}" + taskZKWholesaleReturnSaveIn.getWholesaleReturnNo() + "入库仓位" + zkWholesaleReturnIn.getStorageStockCode() + "不存在");
        }
        taskZKWholesaleReturnSaveIn.setStorageWrh(stockInfoOut.getWarehouseCode());
        // 查询客户
        List<ClientDistInfoOut> clientDistInfos = clientDistInfoService.findClientDistInfo(new QueryClientDistInfoIn(zkWholesaleReturnIn.getClientCode(),
                taskZKWholesaleReturnSaveIn.getBizOrgCode()));
        if (CollectionUtils.isEmpty(clientDistInfos)) {
            log.error("中科{}创建批发退查询客户代码{}-----收货信息校验异常", zkWholesaleReturnIn.getSourceNo(), zkWholesaleReturnIn.getClientCode());
            throw new BusinessException("中科" + zkWholesaleReturnIn.getSourceNo() + "创建批发退查询客户" + zkWholesaleReturnIn.getClientCode() + "与收货人校验失败");
        }
        ClientDistInfoOut clientDistInfo = clientDistInfos.get(0);
        //根据退货单关联的出货单单号查询出货单
        Map<String, WholesaleShipmentDetailIn> shipmentMap = wholesaleReturnsService.getShipmentByWholesaleShipmentNo(wholesaleShipmentNo, bizOrgCode);
        QuerySaleGoodsInfoIn querySaleGoodsInfoIn = new QuerySaleGoodsInfoIn();
        querySaleGoodsInfoIn.setGoodsCodeList(goodsCodeList);
        querySaleGoodsInfoIn.setClientCode(zkWholesaleReturnIn.getClientCode());
        querySaleGoodsInfoIn.setStockCode(zkWholesaleReturnIn.getStorageStockCode());
//        querySaleGoodsInfoIn.setStockId(stockInfoOut.getId());
        querySaleGoodsInfoIn.setBizOrgCode(bizOrgCode);
        querySaleGoodsInfoIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
        querySaleGoodsInfoIn.setWholesaleOrderType(WholesaleOrderTypeEnum.RETURNS.getCode());
        querySaleGoodsInfoIn.setPriceGroupCode(clientDistInfo.getPriceGroupCode());
        //查询商品信息
        Response<List<SaleGoodsInfoOut>> response = saleGoodsInfoClient.findGoodsInfo(querySaleGoodsInfoIn);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error("中科{}创建批发退查询商品异常", zkWholesaleReturnIn.getSourceNo(), response.getMessage());
            throw new BusinessException(response.getMessage());
        }
        Map<String, SaleGoodsInfoOut> saleGoodsInfoOutMap = response.getData().stream().collect(Collectors.toMap(SaleGoodsInfoOut::getGoodsCode, Function.identity()));
        zkWholesaleBusinessDBHandle.saveZKWholesaleReturn(taskZKWholesaleReturnSaveIn, saleGoodsInfoOutMap, shipmentMap, clientDistInfo);
        return Response.success("创建成功");
    }

    /**
     * @Description: 中科批发退货单回传
     * @Author: ZhangYao
     * @Date: 2023/12/12 15:12
     * @param wholesaleReturns:
     * @return: com.edc.plugins.common.response.Response<java.lang.String>
     **/
    public Response<String> handleZkWholesaleReturnBack(WholesaleReturns wholesaleReturns) {
//        JSONObject jsonObject = JSONObject.parseObject(message);
//        if (null == jsonObject) {
//            return Response.error("中科创建批发退回传参数为空");
//        }
//        log.info("中科创建批发退回传消费者入参----------------->" + jsonObject.toJSONString());
//        WholesaleReturns wholesaleReturns = JSONObject.parseObject(jsonObject.toJSONString(), WholesaleReturns.class);
        WholesaleReturns dbWholesaleReturns = wholesaleReturnsService.getOneByWholesaleReturnNo(wholesaleReturns.getWholesaleReturnNo(), wholesaleReturns.getBizOrgCode());
        if (Objects.isNull(dbWholesaleReturns)) {
            return Response.error("批发退货单不存在");
        }
        if (!ShipmentStatusEnum.RECEIPT.getCode().equals(dbWholesaleReturns.getReturnStatus())) {
            return Response.error("批发退货单状态不正确");
        }
        List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnDetailService.findListByWholesaleReturnId(dbWholesaleReturns.getId());
        List<ZKWholesaleReturnDetailBackIn> detailList = wholesaleReturnDetailList.stream().map(wholesaleReturnDetail -> {
            ZKWholesaleReturnDetailBackIn zkWholesaleReturnDetailBackIn = new ZKWholesaleReturnDetailBackIn();
            zkWholesaleReturnDetailBackIn.setItemNo(wholesaleReturnDetail.getGoodsCode());
            if (Objects.isNull(wholesaleReturnDetail.getStorageQuantity()) || wholesaleReturnDetail.getStorageQuantity().compareTo(NumberUtil.INTEGER_ZERO) == NumberUtil.INTEGER_ZERO) {
                zkWholesaleReturnDetailBackIn.setItemQty(NumberUtil.INTEGER_ZERO.toString());
            } else {
                zkWholesaleReturnDetailBackIn.setItemQty(wholesaleReturnDetail.getStorageQuantity().toString());
            }
            zkWholesaleReturnDetailBackIn.setLine(wholesaleReturnDetail.getLine().toString());
            return zkWholesaleReturnDetailBackIn;
        }).collect(Collectors.toList());
        ZKWholesaleReturnBackIn zkWholesaleReturnBackIn = new ZKWholesaleReturnBackIn();
        zkWholesaleReturnBackIn.setOrderNo(dbWholesaleReturns.getWholesaleReturnNo());
        zkWholesaleReturnBackIn.setSourceOrderNo(dbWholesaleReturns.getSourceNo());
        zkWholesaleReturnBackIn.setOrderNoDate(DateUtils.format(dbWholesaleReturns.getReceiveTime()));
        zkWholesaleReturnBackIn.setDetailList(detailList);
        Response<String> response = zkServer.sendWholesaleReturnBack(zkWholesaleReturnBackIn);
        log.info("中科批发退回传{}请求返回---------->{}", zkWholesaleReturnBackIn.getOrderNo(), JSONObject.toJSONString(response));
        if (null != response && response.isSuccess()) {
            //保存出货单审核日志
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getName(),
                    String.valueOf(dbWholesaleReturns.getId()), OrdLogTypeEnum.ORD_WHOLESALE_RETURN.getCode(), OperateLogTypeEnum.ZK_WHOLESALE_RETURN_BACK.getName(),
                    new Date(), SystemConstant.SYSTEM_USER);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.success();
        } else {
            log.info("发送中科批发退回传异常---{}", response.getMessage());
            throw new RuntimeException("发送中科批发退回传异常" + response.getMessage());
        }
    }

}
