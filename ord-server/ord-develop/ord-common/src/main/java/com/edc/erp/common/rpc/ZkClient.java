package com.edc.erp.common.rpc;

import com.edc.erp.common.model.in.zk.ZKWholesaleReturnBackIn;
import com.edc.erp.common.model.in.zk.ZKWholesaleShipmentBackIn;
import com.edc.erp.common.model.in.zk.ZKDeliveryOrderIn;
import com.edc.erp.common.model.in.zk.ZKReturnOrderIn;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "zk", configuration = GlobalFeignErrorDecoderConfiguration.class)
@RequestMapping(value = "/zk")
public interface ZkClient {

//    @GetMapping("/goodsImg/findZKGoodsImgBySkuCode")
//    Response<List<GoodsImgOut>> findZKGoodsImgBySkuCode(@RequestParam("skuCodeList") List<String> skuCodeList);
//
//    @PostMapping("/goodsImg/findZKGoodsImg")
//    Response<Page<GoodsImgOut>> findZKGoodsImg(@RequestBody Page<?> page);

    /**
     * 根据时间段查询出入库单据列表
     * @author: lishaobo
     * @date: 2021-04-13 17:26
     * @param outInInvOrderIn
     * @return
     */
//    @GetMapping(value = "/outInInvOrder/findZKOutInInvOrderByTimeQuantum")
//    Response<List<OutInInvOrder>> findZKOutInInvOrderByTimeQuantum(@RequestBody OutInInvOrderIn outInInvOrderIn);

    /**
     * 根据单号查询出入库单据明细
     * @author: lishaobo
     * @date: 2021-04-13 17:26
     * @param no
     * @return
     */
//    @GetMapping(value = "/outInInvOrder/findZKOutInInvOrderDetailByNo")
//    Response<OutInInvOrderOut> findZKOutInInvOrderDetailByNo(@RequestParam(value = "no") String no);


    /**
     * 发送中科订货单
     *
     * @param zkDeliveryOrderIn
     * @return
     */
    @PostMapping("/storeOrder/sendUniOrderToZk")
    Response<String> sendUniOrderToZk(@RequestBody ZKDeliveryOrderIn zkDeliveryOrderIn);


    /**
     * 发送中科退货单
     *
     * @param zkReturnOrderIn
     * @return
     */
    @PostMapping("/storeOrder/sendUniReOrderToZk")
    Response<String> sendUniReOrderToZk(@RequestBody ZKReturnOrderIn zkReturnOrderIn);

    /**
     * 中科批发出回传
     *
     * @param zkWholesaleShipmentBackIn
     * @return
     */
    @PostMapping("/storeOrder/sendWholesaleShipmentBack")
    Response<String> sendWholesaleShipmentBack(@RequestBody ZKWholesaleShipmentBackIn zkWholesaleShipmentBackIn);

    /**
     * 中科批发出回传
     *
     * @param zkWholesaleReturnBackIn
     * @return
     */
    @PostMapping("/storeOrder/sendWholesaleReturnBack")
    Response<String> sendWholesaleReturnBack(@RequestBody ZKWholesaleReturnBackIn zkWholesaleReturnBackIn);
}
