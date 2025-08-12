package com.edc.erp.common.rpc;

import com.edc.erp.common.model.in.QueryPurchaseIn;
import com.edc.erp.common.model.in.purchase.FindVendorTransIn;
import com.edc.erp.common.model.in.purchase.TransferNoticePurchaseIn;
import com.edc.erp.common.model.in.purchase.TransferShipmentPushPurchaseIn;
import com.edc.erp.common.model.out.QueryPurchaseOut;
import com.edc.erp.common.model.out.purchase.VendorTransInfoVO;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author fxw
 * @description: 中转商品采购单rpc接口
 * @since 2022/11/3 16:44
 */
@FeignClient(name = "purchase", contextId = "PurchaseOrderClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface PurchaseOrderClient {

    /**
     * 配货单中转商品生成采购订单
     *
     * @param transferNoticePurchaseIns
     * @return
     */
    @PostMapping("/pur/business/wrh/purchaseOrder/saveDistributionOrder")
    Response saveDistributionOrder(@RequestBody List<TransferNoticePurchaseIn> transferNoticePurchaseIns);

    @PostMapping("/pur/business/wrh/purchaseOrder/saveWholesaleShipmentOrder")
    Response saveWholesaleShipmentOrder(@RequestBody List<TransferShipmentPushPurchaseIn> shipmentPushPurchaseIns);

    @PostMapping("/pur/supplier/vendor/findTransByCodes")
    Response<List<VendorTransInfoVO>> findTransByCodes(@RequestBody FindVendorTransIn findVendorTransIn);

    @PostMapping("/pur/business/wrh/purchaseOrder/findValidityInfo")
    Response<List<QueryPurchaseOut>> findValidityInfo(@RequestBody List<QueryPurchaseIn> purchaseInList);
}
