//package com.edc.erp.disdeliveryorder.handle;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.enumeration.OrgRelationEnum;
//import com.edc.erp.common.model.in.zk.*;
//import com.edc.erp.returnorder.entity.OrdDisReturn;
//import com.edc.plugins.common.exception.BusinessException;
//import com.edc.plugins.common.response.Response;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Objects;
//
///**
// * @ClassName ZKOrderCallBackHandle
// * @Description TODO
// * @Author ZhangYao
// * @CreateTime 2023/8/10 16:21
// **/
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class ZKBusinessCallBackHandle {
//    private final ZKBusinessHandle zkBusinessHandle;
//
//    /**
//     * @param message:
//     * @Description: 处理中科审核回传
//     * @Author: ZhangYao
//     * @Date: 2023/8/10 16:05
//     * @return: void
//     **/
//    public Response<String> handleZkBusinessAuditCallBack(String message) {
//        JSONObject jsonObject = JSONObject.parseObject(message);
//        if (null == jsonObject) {
//            return Response.error("参数为空");
//        }
//        log.info("中科统一单据审核消费者入参----------------->【单据审核】" + jsonObject.toJSONString());
//        String orderType = jsonObject.getString("orderType");
//        if (StringUtils.isBlank(orderType)) {
//            return Response.error("中科审核回传OrderType为空");
//        }
//        String listDataKey;
//        Response response = null;
//        // 配货单审核
//        if (orderType.equals("1")) {
//            String deliveryOrderNo = jsonObject.getString("deliveryOrderNo");
//            if (StringUtils.isBlank(deliveryOrderNo)) {
//                log.error("中科配销单审核回调，配货单为空", jsonObject.toJSONString());
//                return Response.error("中科配销单审核回调，配货单为空");
//            }
//            listDataKey = "handleDeliveryOrderDetailAuditList";
//            List<HandleDeliveryOrderDetailAuditIn> handleDeliveryGoodsDetailList = JSONArray.parseArray(jsonObject.getString(listDataKey), HandleDeliveryOrderDetailAuditIn.class);
//            if (CollectionUtils.isEmpty(handleDeliveryGoodsDetailList)) {
//                log.error("中科配销单{}审核回调，商品明细为空", deliveryOrderNo);
//                return Response.error("中科配销单审核回调，商品明细为空");
//            }
//            jsonObject.remove(listDataKey);
//            HandleDeliveryOrderAuditIn handleDeliveryOrderAuditIn = JSON.toJavaObject(jsonObject, HandleDeliveryOrderAuditIn.class);
//            handleDeliveryOrderAuditIn.setHandleDeliveryOrderDetailAuditList(handleDeliveryGoodsDetailList);
//            handleDeliveryOrderAuditIn.setOrgCode(handleDeliveryOrderAuditIn.getOrgCode());
//            zkBusinessHandle.handleZkDeliveryOrderAudit(handleDeliveryOrderAuditIn);
//            response = Response.success("中科配销单审核成功");
//        }
//        // 退货单审核
//        if (orderType.equals("2")) {
//            listDataKey = "handleReturnOrderDetailAuditList";
//            List<HandleReturnOrderDetailAuditIn> handleReturnOrderDetailAuditList = JSONArray.parseArray(jsonObject.getString(listDataKey), HandleReturnOrderDetailAuditIn.class);
//            if (CollectionUtils.isEmpty(handleReturnOrderDetailAuditList)) {
//                log.error("中科退货单审核回调，商品明细为空", jsonObject.toJSONString());
//                return Response.error("中科退货单审核回调，商品明细为空");
//            }
//            jsonObject.remove(listDataKey);
//            HandleReturnOrderAuditIn handleReturnOrderAuditIn = JSON.toJavaObject(jsonObject, HandleReturnOrderAuditIn.class);
//            handleReturnOrderAuditIn.setHandleReturnOrderDetailAuditList(handleReturnOrderDetailAuditList);
//            handleReturnOrderAuditIn.setOrgCode(handleReturnOrderAuditIn.getOrgCode());
//            zkBusinessHandle.handleZkReturnOrderAudit(handleReturnOrderAuditIn);
//            response = Response.success("中科退货单审核成功");
//        }
//        if (Objects.nonNull(response) && response.isSuccess()) {
//            return Response.success("中科单据审核回传成功");
//        } else {
//            return response;
//        }
//    }
//
//    /**
//     * @param message:
//     * @Description: 处理中科确认（发货/收货）回传
//     * @Author: ZhangYao
//     * @Date: 2023/8/10 16:05
//     * @return: void
//     **/
//    public Response<String> handleZkBusinessConfirmCallBack(String message) {
//        JSONObject jsonObject = JSONObject.parseObject(message);
//        if (null == jsonObject) {
//            return Response.error("中科确认回传参数为空");
//        }
//        log.info("中科统一单据消费者入参----------------->【单据发货/收货】" + jsonObject.toJSONString());
//        String orderType = jsonObject.getString("orderType");
//        if (StringUtils.isBlank(orderType)) {
//            return Response.error("中科确认回传单据类型为空");
//        }
//        String listDataKey;
//        Response<String> response = null;
//        // 配货单发货
//        if (orderType.equals("1")) {
//            listDataKey = "goodsDetailList";
//            List<HandleDeliveryOrderDetailIn> goodsDetailList = JSONArray.parseArray(jsonObject.getString(listDataKey), HandleDeliveryOrderDetailIn.class);
//            jsonObject.remove(listDataKey);
//            HandleDeliveryOrderIn handleDeliveryOrderIn = JSON.toJavaObject(jsonObject, HandleDeliveryOrderIn.class);
//            handleDeliveryOrderIn.setGoodsDetailList(goodsDetailList);
//            handleDeliveryOrderIn.setOrgCode(OrgRelationEnum.getMytCodeByHdValue(handleDeliveryOrderIn.getOrgCode()));
//            response = zkBusinessHandle.zkShipped(handleDeliveryOrderIn);
//        }
//        // 退货单收货
//        if (orderType.equals("2")) {
//            listDataKey = "directReturnDetailList";
//            List<HandleReturnOrderDetailReceiveIn> handleReturnOrderDetailAuditList = JSONArray.parseArray(jsonObject.getString(listDataKey), HandleReturnOrderDetailReceiveIn.class);
//            if (CollectionUtils.isEmpty(handleReturnOrderDetailAuditList)) {
//                log.error("中科退货单收货回传，商品明细为空", jsonObject.toJSONString());
//                return Response.error("中科退货单收货回传，商品明细为空");
//            }
//            jsonObject.remove(listDataKey);
//            HandleReturnOrderReceiveIn handleReturnOrderReceiveIn = JSON.toJavaObject(jsonObject, HandleReturnOrderReceiveIn.class);
//            handleReturnOrderReceiveIn.setDirectReturnDetailList(handleReturnOrderDetailAuditList);
//            handleReturnOrderReceiveIn.setOrgCode(OrgRelationEnum.getMytCodeByHdValue(handleReturnOrderReceiveIn.getOrgCode()));
//            response = zkBusinessHandle.zkReceiving(handleReturnOrderReceiveIn);
//        }
//        if (Objects.nonNull(response) && response.isSuccess()) {
//            return Response.success("中科单据确认回传成功");
//        } else {
//            return response;
//        }
//    }
//
//}
