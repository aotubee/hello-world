package com.edc.erp.wholesale.returns.service;

import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.plugins.common.response.Response;

import java.math.BigDecimal;


/**
 * 批发退货单(WholesaleReturns)商品服务接口
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
public interface WholesaleReturnsGoodsService {

    /**
     * 校验单条批发退货单详情数据
     * @param wholesaleReturnDetail
     * @param clientCode
     * @param stockCode
     * @param warehouseCode
     * @param stockId
     * @param shipmentDetailIn
     * @param status
     * @return
     */
    Response checkWholesaleReturnDetail(WholesaleReturnDetail wholesaleReturnDetail, String clientCode, String stockCode,
                                        String warehouseCode, Integer stockId, WholesaleShipmentDetailIn shipmentDetailIn,String status);

    /**
     * 计算申请金额等信息
     * @param wholesaleReturnDetail
     * @param goodsInfo
     * @param inventoryPrice 批发出货 库存价
     * @return
     */
    void getObjectResponse(WholesaleReturnDetail wholesaleReturnDetail, SaleGoodsInfoOut goodsInfo, BigDecimal inventoryPrice);

    void computeWholesaleReturnTaxByDTSBack(WholesaleReturnDetail wholesaleReturnDetail, SaleGoodsInfoOut goodsInfo, BigDecimal inventoryPrice);

}
