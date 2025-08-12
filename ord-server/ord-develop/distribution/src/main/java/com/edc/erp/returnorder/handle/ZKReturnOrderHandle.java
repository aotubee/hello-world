package com.edc.erp.returnorder.handle;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.enumeration.OrgRelationEnum;
import com.edc.erp.common.model.in.zk.ZKReturnOrderDetailIn;
import com.edc.erp.common.model.in.zk.ZKReturnOrderIn;
import com.edc.erp.common.model.out.zk.ZkGoodsOut;
import com.edc.erp.common.service.ZKServer;
import com.edc.erp.disdeliveryorder.enumeration.ZKPositionEnum;
import com.edc.erp.disdeliveryorder.enumeration.ZKReturnStockTypeEnum;
import com.edc.erp.enumeration.ReturnOrderLogEnum;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import com.edc.erp.returnorder.enumeration.DisOrdReturnOrderLogEnum;
import com.edc.erp.returnorder.service.OrdDisReturnDetailService;
import com.edc.erp.returnorder.service.OrdDisReturnService;
import com.edc.plugins.common.response.Response;
import com.edc.sdk.dictionary.service.SystemDictService;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName ZKReturnOrderHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/15 9:28
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class ZKReturnOrderHandle {

    private final SystemDictService systemDictService;

    private final OrdDisReturnDetailService ordDisReturnDetailService;

    private final OrdDisReturnService ordDisReturnService;

    private final ZKServer zkServer;

    private final AsyncLogService asyncLogService;


    public Response<List<ZKReturnOrderDetailIn>> handleBeforeSendReturnOrderToZk(OrdDisReturn ordDisReturn, String loginUsername) {
        if (!OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(ordDisReturn.getBizOrgCode())) {
            return Response.error("非中科{}故不推送中科", ordDisReturn.getReturnOrderNo());
        }
        List<OrdDisReturnDetail> returnOrderDetails = ordDisReturnDetailService.findByReturnOrderId(ordDisReturn.getId());
        List<ZKReturnOrderDetailIn> detailList = Lists.newArrayList();
        returnOrderDetails.forEach(returnOrderDetail -> {
            if (StringUtils.isBlank(returnOrderDetail.getOtherGoodsCode())) {
                log.error("中科退货单{}商品{}没有映射,故不推送", ordDisReturn.getReturnOrderNo(), returnOrderDetail.getGoodsCode());
                return;
            }
            ZKReturnOrderDetailIn zkReturnOrderDetailIn = new ZKReturnOrderDetailIn();
            zkReturnOrderDetailIn.setSource_order_no(ordDisReturn.getReturnOrderNo());
            zkReturnOrderDetailIn.setItem_no(returnOrderDetail.getOtherGoodsCode());
            zkReturnOrderDetailIn.setReal_qty(returnOrderDetail.getApplyReturnQuantity());
            zkReturnOrderDetailIn.setValid_price(returnOrderDetail.getReturnUnitPrice());
            zkReturnOrderDetailIn.setSub_amt(returnOrderDetail.getApplyReturnAmount());
            zkReturnOrderDetailIn.setLine(returnOrderDetail.getLine());
            detailList.add(zkReturnOrderDetailIn);
        });
        if (CollectionUtils.isEmpty(detailList)) {
            log.info("中科审核退货单{}没有商品映射，故不推中科", ordDisReturn.getReturnOrderNo());
            ordDisReturn.setUpdater(loginUsername);
            ordDisReturn.setUpdateTime(LocalDateTime.now());
            ordDisReturnService.invalidatedOrdReturn(ordDisReturn);
            String content = DisOrdReturnOrderLogEnum.ORD_DIS_RETURN_EMPTY_OTHER_GOODS.getValue();
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
                    OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), content, new Date(), loginUsername);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.error("推送中科退货单没有商品映射,作废不推中科");
        }
        return Response.data(detailList);
    }

    public Response<String> sendReturnOrderToZK(OrdDisReturn ordDisReturn, List<ZKReturnOrderDetailIn> detailList) {
        ZKReturnOrderIn zkReturnOrderIn = new ZKReturnOrderIn();
        zkReturnOrderIn.setSource_order_no(ordDisReturn.getReturnOrderNo());
        zkReturnOrderIn.setStock_type(ZKReturnStockTypeEnum.ZK_TEMPERATURE.getZkStockType());
        String branchNo;
        String zkMemo = ordDisReturn.getStockCode() + "-" + ZKPositionEnum.getNameByCode(ordDisReturn.getStockCode());
        if (ZKPositionEnum.ZK_FREEZE_LOW_RETURN.getCode().equals(ordDisReturn.getStockCode())) {
            branchNo = ZKPositionEnum.ZK_RETURN.getCode();
        } else {
            branchNo = ordDisReturn.getStockCode();
        }
        zkReturnOrderIn.setBranch_no(branchNo);
        zkReturnOrderIn.setD_branch_no(ordDisReturn.getStoreCode());
        zkReturnOrderIn.setOper_id(ordDisReturn.getCreator());
        zkReturnOrderIn.setMemo(zkMemo + ":" + systemDictService.getSystemDictName(ordDisReturn.getReturnOrderReason()));
        zkReturnOrderIn.setDetail_list(detailList);
        log.info("推送中科退货单审核入参---------{}", JSONObject.toJSONString(zkReturnOrderIn));
        Response<String> response = zkServer.sendUniReOrderToZk(zkReturnOrderIn);
        log.info("推送中科退货单{}请求返回---------->{}", zkReturnOrderIn.getSource_order_no(), JSONObject.toJSONString(response));
        if (null != response && response.isSuccess()) {
            String content = MessageFormat.format(ReturnOrderLogEnum.RETURN_ORDER_SEND_TO_ZK.getKey(), ordDisReturn.getReturnOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_RETURN.getName(), String.valueOf(ordDisReturn.getId()),
                    OrdLogTypeEnum.ORD_DIS_RETURN.getCode(), content, new Date(), ordDisReturn.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.success();
        } else {
            log.info("发送中科退货单审核异常---{}", response.getMessage());
            return Response.error("发送中科退货单审核异常");
        }
    }
}


